import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;

public class IndexBuilderConfig {
    private String indexDirectoryPath;
    private Similarity similarity = new BM25Similarity();
    private Analyzer analyzer = new EnglishAnalyzer();
    private double bufferSize = 120.0;
    private boolean indexTitle = true;
    private boolean indexTags = true;
    private boolean indexAnswers = true;

    public IndexBuilderConfig(String path) {
        this.indexDirectoryPath = path;
    }

    public void buildIndex(String docDirPath) throws IOException {
        IndexBuilder builder = new IndexBuilder(this);
        builder.build(docDirPath);
    }

    public double getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(double bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isIndexTitle() {
        return indexTitle;
    }

    public void setIndexTitle(boolean indexTitle) {
        this.indexTitle = indexTitle;
    }

    public boolean isIndexAnswers() {
        return indexAnswers;
    }

    public void setIndexAnswers(boolean indexAnswers) {
        this.indexAnswers = indexAnswers;
    }

    public boolean isIndexTags() {
        return indexTags;
    }

    public void setIndexTags(boolean indexTags) {
        this.indexTags = indexTags;
    }

    public String getIndexDirectoryPath() {
        return indexDirectoryPath;
    }

    public void setIndexDirectoryPath(String indexDirectoryPath) {
        this.indexDirectoryPath = indexDirectoryPath;
    }

    public Similarity getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Similarity similarity) {
        this.similarity = similarity;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }
}
