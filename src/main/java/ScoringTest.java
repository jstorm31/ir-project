import model.SearchResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
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
    private Configuration config;
    private int randomSeed;
    private int windowSize;

    ScoringTest(Searcher searcher, Configuration config) {
        this.searcher = searcher;
        this.config = config;

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
            String titleQuery = qp.escape(document.get("title"));
            config.setFeedbackRelevantDocs(windowSize);
            config.setFeedbackExpansionTerms(titleQuery.split(" ").length + 4   );
            SearchResult result = null;

            try {
                result = searcher.runTestSearch(titleQuery, this.windowSize);
            } catch (ParseException e) {
                System.out.println("Skipping query \"" + titleQuery + "\" due to raised ParseException.");
                continue;
            }

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
        //String docDirPath = "/var/run/media/iasoon/Elements/posts_minimal/";
        //String indexDirPath = "/var/run/media/iasoon/Elements/index_minimal";
        String docDirPath = "/Users/jstorm31/stackoverflow_xs";
        String indexDirPath = "/Users/jstorm31/stackoverflow_index";
        Scanner command = new Scanner(System.in);

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
            System.out.println("Do you wish to build the index? [y/n]");
            String input = command.nextLine();
            if (input.equalsIgnoreCase("y")) {
                config.buildIndex();
            }

            System.out.println("Run for normal search (n) or psudo-relevant feedback search (p)? [n/p]");
            input = command.nextLine();
            Searcher searcher = null;
            if (input.equalsIgnoreCase("p")) {
                searcher = new RocchioSearcher(config);
            } else {
                searcher = new Searcher(config);
            }

            // run test
            ScoringTest test = new ScoringTest(searcher, config);
            double[] recall = test.run(1000);

            // Print results
            StringJoiner sj = new StringJoiner(",");
            DoubleStream.of(recall).forEach(x -> sj.add(String.valueOf(x)));
            System.out.println(sj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
