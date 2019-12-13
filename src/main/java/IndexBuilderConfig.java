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
    private boolean includeTitle = true;
    private boolean includeTags = true;
    private boolean includeAnswers = true;

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

    public boolean isIncludeTitle() {
        return includeTitle;
    }

    public void setIncludeTitle(boolean includeTitle) {
        this.includeTitle = includeTitle;
    }

    public boolean isIncludeAnswers() {
        return includeAnswers;
    }

    public void setIncludeAnswers(boolean includeAnswers) {
        this.includeAnswers = includeAnswers;
    }

    public boolean isIncludeTags() {
        return includeTags;
    }

    public void setIncludeTags(boolean includeTags) {
        this.includeTags = includeTags;
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
