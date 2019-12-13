import model.SearchResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    static String indexDirPath = "/Users/jstorm31/stackoverflow_index";
    static String docDirPath = "/Users/jstorm31/stackoverflow_xs";

    public static void main(String[] args) {
        Scanner command = new Scanner(System.in);

        System.out.println("Do you wish to build the index? [y/n]");
        String buildIndex = command.nextLine();

        if (buildIndex.equalsIgnoreCase("y")) {
            buildIndex();
        }
        search();
    }

    public static void buildIndex() {
        System.out.println("Building index...");
        try {
            IndexBuilder builder = new IndexBuilder(indexDirPath);
            builder.build(docDirPath);
            System.out.println("Index has been successfully built");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void search() {
        Scanner command = new Scanner(System.in);

        try {
            RocchioSearcher searcher = new RocchioSearcher(indexDirPath, 1, 1.25);

            while (true) {
                System.out.println("\nEnter query:");
                long startTime = System.currentTimeMillis();

                String query = command.nextLine();

                int queryTermsCount = query.split("\\s+").length;
                SearchResult searchResult = searcher.search(query, 5);
                SearchResult pseudoFeedbackSearchResult = searcher.expandQuery(query, 5, queryTermsCount + 2);

                long stopTime = System.currentTimeMillis();
                long duration = stopTime - startTime;

                System.out.println("\nFound " + searchResult.docs.totalHits.value + " documents in " + duration + " ms ");
                System.out.println("Top 5 results:");
                for (ScoreDoc sd : searchResult.docs.scoreDocs) {
                    Document d = searcher.searcher.doc(sd.doc);
                    System.out.println(String.format(d.get("name")) + "(" + sd.score + "): " + String.format(d.get("title")) + "\"");
                }

                System.out.println("\nTop 5 results with Rocchio relevant feedback:");
                for (ScoreDoc sd : pseudoFeedbackSearchResult.docs.scoreDocs) {
                    Document d = searcher.searcher.doc(sd.doc);
                    Explanation explanation = searcher.searcher.explain(searchResult.query, sd.doc);
                    System.out.println(String.format(d.get("name")) + "(" + sd.score + "): " + String.format(d.get("title")) + "\"");
                    //System.out.println("Explanation: " + explanation.toString());
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }
}