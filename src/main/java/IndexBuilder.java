/* Sources:
 - https://www.tutorialspoint.com/lucene/lucene_first_application.htm
 - https://howtodoinjava.com/lucene/lucene-index-search-examples/
*/

import model.DocumentParsingException;
import model.StackOverflowDocument;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig writerConfig = new IndexWriterConfig(new StandardAnalyzer());
        writer = new IndexWriter(indexDirectory, writerConfig);
    }

    /**
     * Builds an index from XML documents in a specified directory
     *
     * @param srcDir
     */
    public void build(String srcDir) {
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
    }

    private Document createDocument(File file) throws DocumentParsingException {
        StackOverflowDocument soDocument = new StackOverflowDocument(file);
        Document document = new Document();

        // Question fields
        document.add(new TextField("questionTitle", soDocument.getTitle(), Field.Store.YES));
        document.add(new TextField("questionBody", soDocument.getQuestionBody(), Field.Store.NO));
        document.add(new TextField("questionTags", soDocument.getTags(), Field.Store.YES));

        // Answers fields
        for (int i = 0; i < soDocument.getAnswers().size(); i++) {
            String answer = soDocument.getAnswers().get(i);
            document.add(new TextField("answer_" + i, answer, Field.Store.NO));
        }

        return document;
    }
}
