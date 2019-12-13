import model.DocumentParsingException;
import model.StackOverflowDocument;
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

    private IndexBuilderConfig config;

    public IndexBuilder(IndexBuilderConfig config) throws IOException {
        this.config = config;
        Directory indexDirectory = FSDirectory.open(Paths.get(config.getIndexDirectoryPath()));

        // Config
        IndexWriterConfig writerConfig = new IndexWriterConfig(config.getAnalyzer());
        writerConfig.setRAMBufferSizeMB(this.config.getBufferSize());
        writerConfig.setSimilarity(this.config.getSimilarity());

        writer = new IndexWriter(indexDirectory, writerConfig);
    }


    public IndexBuilder(String indexDirectoryPath) throws IOException {
    }


    /**
     * Builds an index from XML documents in a specified directory
     *
     * @param srcDir
     */
    public void build(String srcDir) throws IOException {
        long startTime = System.currentTimeMillis();
        int i = 1;

        // Load XML files
        Path dir = FileSystems.getDefault().getPath(srcDir);
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.xml");

        // Delete previous index
        writer.deleteAll();
        System.out.println("Deleted old index");

        // Build index
        for (Path path : stream) {
            Document doc = null;
            try {
                doc = createDocument(path.toFile());
                writer.addDocument(doc);
            } catch (DocumentParsingException | IOException e) {
                System.out.println("Problem adding document " + doc.getField("name") + ".xml - it hasn't been added");
            }

            if (i % 10000 == 0) {
                System.out.println("Indexed " + String.format("%,d", i) + " files...");
            }
            i++;
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
        StringBuilder content = new StringBuilder();
        if (config.isIndexTitle()) {
            content.append(soDocument.getTitle());
            content.append("\n");
        }
        if (config.isIndexTags()) {
            content.append(soDocument.getTags());
            content.append("\n");
        }
        content.append(soDocument.getQuestionBody());

        for (int i = 0; i < soDocument.getAnswers().size(); i++) {
            content.append("\n");
            content.append(soDocument.getAnswers().get(i));
        }
        document.add(new TextField("content", content.toString(), Field.Store.NO));

        return document;
    }
}
