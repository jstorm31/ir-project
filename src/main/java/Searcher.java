import model.SearchResult;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {
    protected Configuration config;
    protected IndexSearcher searcher;
    protected IndexReader reader;

    Searcher(Configuration config) throws IOException {
        this.config = config;
        Directory dir = FSDirectory.open(Paths.get(config.getIndexDirectoryPath()));
        reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);
        searcher.setSimilarity(config.getSimilarity());
    }

    /**
     * Searches given query over indexed document content and returns top n results
     *
     * @param text searched query
     * @param n    top results to search
     * @return top results
     * @throws ParseException
     * @throws IOException
     */
    public SearchResult search(String text, Integer n) throws ParseException, IOException {
        Query titleQuery = queryParser().parse(text);
        return runQuery(titleQuery, n);
    }

    public QueryParser queryParser() {
        return  new QueryParser("content", config.getAnalyzer());
    }

    public SearchResult runQuery(Query query, Integer n) throws IOException {
        TopDocs hits = searcher.search(query, n);
        return new SearchResult(hits, query);
    }

    public IndexReader getIndexReader() {
        return this.reader;
    }
}
