import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    static String indexDirPath = "/Users/jstorm31/Downloads/stackoverflow/index";
    static String docDirPath = "/Users/jstorm31/Downloads/stackoverflow";

    public static void main(String[] args) {
        buildIndex();
        search();
    }

    public static void buildIndex() {
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

                String query = command.nextLine();
                TopDocs foundDocs = searcher.search(query);

                System.out.println("\nFound documents: " + foundDocs.totalHits);
                for (ScoreDoc sd : foundDocs.scoreDocs) {
                    Document d = searcher.searcher.doc(sd.doc);
                    System.out.println(String.format(d.get("name")) + ": \"" + String.format(d.get("questionTitle")) + "\"");
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }
}