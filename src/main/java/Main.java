import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Scanner;

public class Main {

//    static String indexDirPath = "/Volumes/jiri_drive/stackoverflow_index";
//    static String docDirPath = "/Volumes/jiri_drive/stackoverflow_xs";

    static String indexDirPath = "/var/run/media/iasoon/Elements/stackoverflow_index";
    static String docDirPath = "/var/run/media/iasoon/Elements/posts_reduced";


    public static void main(String[] args) {
        Scanner command = new Scanner(System.in);

        System.out.println("Do you wish to build the index? [y/n]");
        String buildIndex = command.nextLine().trim();
        System.out.println(buildIndex);

        if (buildIndex.equals("y")) {
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
            Searcher searcher = new Searcher(indexDirPath);

            while (true) {
                System.out.println("\nEnter query:");
                long startTime = System.currentTimeMillis();

                String query = command.nextLine();
                TopDocs foundDocs = searcher.search(query, 5);

                long stopTime = System.currentTimeMillis();
                long duration = stopTime - startTime;

                System.out.println("\nFound " + foundDocs.totalHits.value + " documents in " + duration + " ms ");
                System.out.println("Top 5 results:");
                for (ScoreDoc sd : foundDocs.scoreDocs) {
                    Document d = searcher.searcher.doc(sd.doc);
                    System.out.println(String.format(d.get("name")) + ": \"" + String.format(d.get("title")) + "\"");
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }
}