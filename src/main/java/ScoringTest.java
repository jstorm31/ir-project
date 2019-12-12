import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.Random;

public class ScoringTest {
    static int DEFAULT_SEED = 42;
    // how many documents to consider
    static int DEFAULT_WINDOW_SIZE = 25;

    private IndexSearcher searcher;
    private int randomSeed;
    private int windowSize;

    ScoringTest(IndexSearcher searcher) {
        this.searcher = searcher;

        this.randomSeed = DEFAULT_SEED;
        this.windowSize = DEFAULT_WINDOW_SIZE;
    }

    public void run(int numTrials) throws Exception {
        QueryParser qp = new QueryParser("content", new EnglishAnalyzer());

        Random random = new Random(this.randomSeed);

        int numHits = 0;
        int numMisses = 0;

        for (int trial = 0; trial < numTrials; trial++) {
            int docId = random.nextInt(searcher.getIndexReader().numDocs());
            Document document = searcher.doc(docId);

            Query titleQuery = qp.parse(qp.escape(document.get("title")));

            TopDocs hits = searcher.search(titleQuery, this.windowSize);

            boolean found = false;
            for (int i = 0; i < hits.scoreDocs.length; i++) {
                if (hits.scoreDocs[i].doc == docId) {
                    found = true;
                    break;
                }
            }

            if (found) {
                numHits++;
            } else {
                numMisses++;
            }
        }

        System.out.printf("%d hits; %d misses\n", numHits, numMisses);
        System.out.printf("ratio: %f ", (float)numHits / numTrials);
    }

    public static void main(String[] args) {
        String docDirPath = "/var/run/media/iasoon/Elements/posts_minimal/";
        String indexDirPath = "/var/run/media/iasoon/Elements/index_minimal";

        // Similarity similarity = new DFRSimilarity(new BasicModelG(), new AfterEffectL(), new NormalizationH1());
        Similarity similarity = new BM25Similarity();

        try {
            // build index
            Directory indexDir = FSDirectory.open(Paths.get(indexDirPath));
            IndexBuilder builder = new IndexBuilder(indexDirPath, similarity);
            builder.build(docDirPath);

            // open index
            IndexReader indexReader = DirectoryReader.open(indexDir);
            IndexSearcher searcher = new IndexSearcher(indexReader);
            searcher.setSimilarity(similarity);

            // run test
            ScoringTest test = new ScoringTest(searcher);
            test.run(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
