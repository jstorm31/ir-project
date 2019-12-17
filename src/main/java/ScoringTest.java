import model.SearchResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.*;

import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.DoubleStream;
import java.util.Scanner;


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

        // Similarity similarity = new ClassicSimilarity();
        Similarity similarity = new BM25Similarity(1.2f, 0.25f);
        // Similarity similarity = new DFRSimilarity(new BasicModelG(), new AfterEffectL(), new NormalizationH1());
        // Similarity similarity = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH1());
        // Similarity similarity = new LMDirichletSimilarity();
        // Similarity similarity = new LMJelinekMercerSimilarity(0.1f);
        // Similarity similarity = new DFISimilarity(new IndependenceStandardized());

        try {
            Configuration config = new Configuration();
            config.setDocDirectoryPath(docDirPath);
            config.setIndexDirectoryPath(indexDirPath);
            config.setIndexTitle(false);
            config.setSimilarity(similarity);

            // build index
            Scanner command = new Scanner(System.in);

            System.out.println("Do you wish to build the index? [y/n]");
            String buildIndex = command.nextLine();

            if (buildIndex.equalsIgnoreCase("y")) {
                config.buildIndex();
            }

            Searcher searcher = new Searcher(config);

            // run test
            ScoringTest test = new ScoringTest(searcher);
            double[] recall = test.run(5000);

            // Print results
            StringJoiner sj = new StringJoiner(",");
            DoubleStream.of(recall).forEach(x -> sj.add(String.valueOf(x)));
            System.out.println(sj.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
