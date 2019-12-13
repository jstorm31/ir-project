import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;

public class Configuration {
    private Similarity similarity = new BM25Similarity();
    private Analyzer analyzer = new EnglishAnalyzer();

    private boolean indexTitle = true;
    private boolean indexTags = true;
    private boolean indexAnswers = true;

    private String docDirectoryPath;
    private String indexDirectoryPath;

    public IndexBuilderConfig indexBuilderConfig() {
        IndexBuilderConfig indexBuilderConfig = new IndexBuilderConfig(this.indexDirectoryPath);
        indexBuilderConfig.setSimilarity(getSimilarity());
        indexBuilderConfig.setAnalyzer(getAnalyzer());

        indexBuilderConfig.setIndexTitle(isIndexTitle());
        indexBuilderConfig.setIndexTags(isIndexTags());
        indexBuilderConfig.setIndexAnswers(isIndexAnswers());

        return indexBuilderConfig;
    }

    public void buildIndex() throws IOException {
        indexBuilderConfig().buildIndex(getDocDirectoryPath());
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

    public boolean isIndexTitle() {
        return indexTitle;
    }

    public void setIndexTitle(boolean indexTitle) {
        this.indexTitle = indexTitle;
    }

    public boolean isIndexTags() {
        return indexTags;
    }

    public void setIndexTags(boolean indexTags) {
        this.indexTags = indexTags;
    }

    public boolean isIndexAnswers() {
        return indexAnswers;
    }

    public void setIndexAnswers(boolean indexAnswers) {
        this.indexAnswers = indexAnswers;
    }

    public String getDocDirectoryPath() {
        return docDirectoryPath;
    }

    public void setDocDirectoryPath(String docDirectoryPath) {
        this.docDirectoryPath = docDirectoryPath;
    }

    public String getIndexDirectoryPath() {
        return indexDirectoryPath;
    }

    public void setIndexDirectoryPath(String indexDirectoryPath) {
        this.indexDirectoryPath = indexDirectoryPath;
    }
}
