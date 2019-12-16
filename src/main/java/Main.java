import model.SearchResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import sun.security.krb5.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    static String indexDirPath = "/Users/jstorm31/stackoverflow_index";
    static String docDirPath = "/Users/jstorm31/stackoverflow_xs";

    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setIndexDirectoryPath(indexDirPath);
        config.setDocDirectoryPath(docDirPath);

        Scanner command = new Scanner(System.in);

        System.out.println("Do you wish to build the index? [y/n]");
        String buildIndex = command.nextLine();

        if (buildIndex.equalsIgnoreCase("y")) {
            buildIndex(config);
        }
        search(config);
    }

    public static void buildIndex(Configuration config) {
        System.out.println("Building index...");
        try {
            config.buildIndex();
            System.out.println("Index has been successfully built");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void search(Configuration config) {
        Scanner command = new Scanner(System.in);

        try {
            RocchioSearcher searcher = new RocchioSearcher(config, 1, 0.75);

            while (true) {
                System.out.println("\nEnter query:");
                long startTime = System.currentTimeMillis();

                String query = command.nextLine();
                int queryTermsCount = query.split("\\s+").length;
                SearchResult searchResult = searcher.search(query, 5);

                long stopTime = System.currentTimeMillis();
                long duration = stopTime - startTime;

                System.out.println("\nFound " + searchResult.docs.totalHits.value + " documents in " + duration + " ms ");
                System.out.println("Top 5 results:");
                for (ScoreDoc sd : searchResult.docs.scoreDocs) {
                    Document d = searcher.searcher.doc(sd.doc);
                    System.out.println(String.format(d.get("name")) + "(" + sd.score + "): " + String.format(d.get("title")) + "\"");
                }

                System.out.println("\nDo you wish to search for a new query (n), add a feedback manually to the current one (m) or use pseudo feedback(f)? [n/f]");
                String input = command.nextLine();
                if (input.equalsIgnoreCase("n")) {
                    continue;
                }

                // Relevance feedback part
                SearchResult feedbackSearchResult = null;
                List<ScoreDoc> scoreDocs = new ArrayList<ScoreDoc>(Arrays.asList(searchResult.docs.scoreDocs));

                if (input.equalsIgnoreCase("m")) {
                    List<Integer> relevantDocs = chooseRelevantDocuments();
                    feedbackSearchResult = searcher.expandQuery(query, scoreDocs, relevantDocs, 5, queryTermsCount + 2);
                } else if (input.equalsIgnoreCase("f")) {
                    feedbackSearchResult = searcher.expandQuery(query, scoreDocs, 5, queryTermsCount + 2);
                } else {
                    System.out.println("Invalid input");
                    continue;
                }

                System.out.println("\nFound " + feedbackSearchResult.docs.totalHits.value + " documents in " + duration + " ms ");
                System.out.println("\nTop 5 results with Rocchio relevant feedback:");
                for (ScoreDoc sd : feedbackSearchResult.docs.scoreDocs) {
                    Document d = searcher.searcher.doc(sd.doc);
                    System.out.println(String.format(d.get("name")) + "(" + sd.score + "): " + String.format(d.get("title")) + "\"");
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get relevant documents input from a user and parse it to integers
     */
    public static List<Integer> chooseRelevantDocuments() {
        System.out.println("Enter relevant documents positions indexed from 0 and separated by a comma (e.g. '0,3'):");
        Scanner command = new Scanner(System.in);

        String input = command.nextLine();
        String[] docs = input.split(",");
        List relevantDocs = new ArrayList<Integer>();

        for (String doc : docs) {
            relevantDocs.add(Integer.parseInt(doc));
        }
        return relevantDocs;
    }
}