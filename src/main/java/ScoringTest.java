import model.SearchResult;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
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
    static int DEFAULT_WINDOW_SIZE = 100;

    private Searcher searcher;
    private int randomSeed;
    private int windowSize;

    ScoringTest(Searcher searcher) {
        this.searcher = searcher;

        this.randomSeed = DEFAULT_SEED;
        this.windowSize = DEFAULT_WINDOW_SIZE;
    }

    public double[] run(int numTrials) throws Exception {
        QueryParser qp = this.searcher.queryParser();

        Random random = new Random(this.randomSeed);

        int[] recallCounts = new int[this.windowSize];

        for (int trial = 0; trial < numTrials; trial++) {
            int docId = random.nextInt(searcher.getIndexReader().numDocs());
            Document document = searcher.getIndexReader().document(docId);
            Query titleQuery = qp.parse(qp.escape(document.get("title")));
            SearchResult result = searcher.runQuery(titleQuery, this.windowSize);


            for (int i = 0; i < result.docs.scoreDocs.length; i++) {
                if (result.docs.scoreDocs[i].doc == docId) {
                    recallCounts[i] += 1;
                    break;
                }
            }
        }

        double[] recall = new double[this.windowSize];
        int accumulator = 0;

        for (int i = 0; i < this.windowSize; i++) {
            accumulator += recallCounts[i];
            recall[i] = (double) accumulator / numTrials;
        }

        int[] thresholds = {1, 5, 10, 25};

        for (int i = 0; i < thresholds.length; i++) {
            System.out.printf("recall @ %d: %f\n", thresholds[i], recall[thresholds[i]-1]);
        }
        return recall;
    }

    public static void main(String[] args) {
        String docDirPath = "/var/run/media/iasoon/Elements/posts_minimal/";
        String indexDirPath = "/var/run/media/iasoon/Elements/index_minimal";

        // Similarity similarity = new DFRSimilarity(new BasicModelG(), new AfterEffectL(), new NormalizationH1());
        Similarity similarity = new BM25Similarity();

        try {
            Configuration config = new Configuration();
            config.setDocDirectoryPath(docDirPath);
            config.setIndexDirectoryPath(indexDirPath);
            config.setIndexTitle(false);
            config.setSimilarity(similarity);

            // build index
            config.buildIndex();

            Searcher searcher = new Searcher(config);

            // run test
            ScoringTest test = new ScoringTest(searcher);
            test.run(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
