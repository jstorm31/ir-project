import model.SearchResult;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {
    IndexSearcher searcher;
    IndexReader reader;

    Searcher(String indexDir) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);
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
        QueryParser qp = new QueryParser("content", new EnglishAnalyzer());
        Query titleQuery = qp.parse(text);
        TopDocs hits = searcher.search(titleQuery, n);
        return new SearchResult(hits, titleQuery);
    }
}
