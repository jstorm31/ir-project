/* Originally taken from:
 - https://github.com/uiucGSLIS/ir-tools/blob/master/src/main/java/edu/gslis/lucene/expansion/Rocchio.java
 - https://github.com/gtsherman/lucene/blob/master/src/main/java/org/retrievable/lucene/searching/expansion/Rocchio.java
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.SearchResult;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import helper.FeatureVector;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;

public class RocchioSearcher extends Searcher {

    private double alpha;
    private double beta;
    private double k1;
    private double b;

    /**
     * Default parameter values taken from:
     * https://nlp.stanford.edu/IR-book/html/htmledition/the-rocchio71-algorithm-1.html
     */
    RocchioSearcher(String indexDir) throws IOException {
        this(indexDir, 1.0, 0.75);
    }

    public RocchioSearcher(String indexDir, double alpha, double beta) throws IOException {
        this(indexDir, alpha, beta, 1.2, 0.75);
    }

    public RocchioSearcher(String indexDir, double alpha, double beta, double k1, double b) throws IOException {
        super(indexDir);
        this.alpha = alpha;
        this.beta = beta;
        this.k1 = k1;
        this.b = b;
    }

    /**
     * Manually label relevant documents
     */
    public SearchResult expandQuery(String text, List<ScoreDoc> initialResults, List<Integer> relevantDocs, int fbDocs, int fbTerms) throws IOException, ParseException {
        List<ScoreDoc> relevantScoreDocs = new ArrayList<ScoreDoc>();

        // Filter only relevant documents
        for (ScoreDoc doc : initialResults) {
            if (relevantDocs.contains(doc.doc)) {
                relevantScoreDocs.add(doc);
            }
        }

        return expandQuery(text, relevantScoreDocs, fbDocs, fbTerms);
    }

    public SearchResult expandQuery(String text, List<ScoreDoc> initialResults, int fbDocs, int fbTerms) throws IOException, ParseException {
        FeatureVector summedTermVec = new FeatureVector(null);

        for (ScoreDoc doc : initialResults) {
            Document document = searcher.doc(doc.doc);
            String docText = document.getField("content").stringValue();

            // Get the document tokens and add to the doc vector
            FeatureVector docVec = new FeatureVector(null);
            parseText(docText, docVec);

            // Compute the BM25 weights
            computeBM25Weights(searcher, docVec, summedTermVec);
        }

        // Multiply the summed term vector by beta / |Dr|
        FeatureVector relDocTermVec = new FeatureVector(null);

        for (String term : summedTermVec.getFeatures()) {
            double weight = summedTermVec.getFeatureWeight(term);
            relDocTermVec.addTerm(term, weight * beta / fbDocs);
        }

        // Create a query vector and scale by alpha
        FeatureVector rawQueryVec = new FeatureVector(null);
        parseText(text, rawQueryVec);

        FeatureVector summedQueryVec = new FeatureVector(null);
        computeBM25Weights(searcher, rawQueryVec, summedQueryVec);

        FeatureVector queryTermVec = new FeatureVector(null);
        for (String term : rawQueryVec.getFeatures()) {
            queryTermVec.addTerm(term, summedQueryVec.getFeatureWeight(term) * alpha);
        }

        // Combine query and rel doc vectors
        for (String term : queryTermVec.getFeatures()) {
            relDocTermVec.addTerm(term, queryTermVec.getFeatureWeight(term));
        }

        // Get top terms
        relDocTermVec.clip(fbTerms);

        StringBuffer expandedQuery = new StringBuffer();
        for (String term : relDocTermVec.getFeatures()) {
            expandedQuery.append(term + "^" + relDocTermVec.getFeatureWeight(term) + " ");
        }
        System.out.println("Expanded query: " + expandedQuery.toString());

        return search(expandedQuery.toString(), fbDocs);
    }

    private void parseText(String text, FeatureVector vector) throws IOException {
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream(null, text);
        CharTermAttribute tokens = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            String token = tokens.toString();
            vector.addTerm(token);
        }
        analyzer.close();
    }

    private void computeBM25Weights(IndexSearcher index, FeatureVector docVec, FeatureVector summedTermVec) throws IOException {
        for (String term : docVec.getFeatures()) {
            int docCount = index.getIndexReader().numDocs();
            int docOccur = index.getIndexReader().docFreq(new Term("content", term));
            double avgDocLen = index.getIndexReader().getSumTotalTermFreq("content") / docCount;

            double idf = Math.log( (docCount + 1) / (docOccur + 0.5) );
            double tf = docVec.getFeatureWeight(term);

            double weight = (idf * k1 * tf) / (tf + k1 * (1 - b + b * docVec.getLength() / avgDocLen));
            summedTermVec.addTerm(term, weight);
        }
    }

}
