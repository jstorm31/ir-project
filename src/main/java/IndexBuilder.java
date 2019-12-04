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
import java.nio.file.*;

public class IndexBuilder {
    private IndexWriter writer;

    static private Double BUFFER_SIZE = 512.0;

    public IndexBuilder(String indexDirectoryPath) throws IOException {
        Analyzer analyzer = new EnglishAnalyzer();
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));

        // Config
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        writerConfig.setRAMBufferSizeMB(BUFFER_SIZE);

        writer = new IndexWriter(indexDirectory, writerConfig);
    }

    /**
     * Builds an index from XML documents in a specified directory
     *
     * @param srcDir
     */
    public void build(String srcDir) throws IOException {
        long startTime = System.currentTimeMillis();

        // Load XML files
        Path dir = FileSystems.getDefault().getPath(srcDir);
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.xml");

        // Delete previous index
        writer.deleteAll();

        // Build index
        for (Path path : stream) {
            Document doc = null;
            try {
                doc = createDocument(path.toFile());
                writer.addDocument(doc);
            } catch (DocumentParsingException | IOException e) {
                System.out.println("Problem adding document " + doc.getField("name") + ".xml - it hasn't been added");
            }
        }

        // Commit and clean
        writer.commit();
        writer.close();
        stream.close();

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
