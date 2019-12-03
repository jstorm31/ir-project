import model.DocumentParsingException;
import model.StackOverflowDocument;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static String docDirPath = "/Users/jstorm31/Downloads/stackoverflow";

    public static void main(String[] args) {
        File dir = new File(docDirPath);
        File[] files = dir.listFiles(new XmlFileFilter());
        List<StackOverflowDocument> documents = new ArrayList<StackOverflowDocument>();

        try {
            for (File file: files) {
                documents.add(new StackOverflowDocument(file));
            }
        } catch (DocumentParsingException e) {
            e.printStackTrace();
        }
    }
}