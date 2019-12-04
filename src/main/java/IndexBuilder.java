/* Sources:
 - https://www.tutorialspoint.com/lucene/lucene_first_application.htm
 - https://howtodoinjava.com/lucene/lucene-index-search-examples/
*/

import model.DocumentParsingException;
import model.StackOverflowDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IndexBuilder {
    private IndexWriter writer;

    public IndexBuilder(String indexDirectoryPath) throws IOException {
        Analyzer analyzer = new EnglishAnalyzer();
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(indexDirectory, writerConfig);
    }

    /**
     * Builds an index from XML documents in a specified directory
     *
     * @param srcDir
     */
    public void build(String srcDir) {
        long startTime = System.currentTimeMillis();
        File dir = new File(srcDir);
        File[] files = dir.listFiles(new XmlFileFilter());
        List<Document> documents = new ArrayList();

        try {
            for (File file : files) {
                documents.add(createDocument(file));
            }

            // Build an index from documents
            writer.deleteAll();
            writer.addDocuments(documents);
            writer.commit();
            writer.close();
        } catch (DocumentParsingException | IOException e) {
            e.printStackTrace();
        }

        long stopTime = System.currentTimeMillis();
        long duration = stopTime - startTime;
        System.out.println("Built index in " + duration / 1000.0 + "s");
    }

    private Document createDocument(File file) throws DocumentParsingException {
        StackOverflowDocument soDocument = new StackOverflowDocument(file);
        Document document = new Document();

        // Meta fields
        String noSuffixName = soDocument.getName().substring(0, soDocument.getName().length() - 4); // Remove '.xml' suffix
        document.add(new StringField("name", noSuffixName, Field.Store.YES));
        document.add(new TextField("title", soDocument.getTitle(), Field.Store.YES));
        document.add(new TextField("tags", soDocument.getTags(), Field.Store.YES));

        // Content
        String content = soDocument.getTitle() + "\n" + soDocument.getTags() + "\n" + soDocument.getQuestionBody();
        for (int i = 0; i < soDocument.getAnswers().size(); i++) {
            String answer = soDocument.getAnswers().get(i);
            content += "\n" + answer;
        }
        document.add(new TextField("content", content, Field.Store.NO));

        return document;
    }
}
