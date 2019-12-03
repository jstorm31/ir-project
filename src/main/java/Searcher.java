import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {
    IndexSearcher searcher;

    Searcher(String indexDir) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(dir);
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
    public TopDocs search(String text, Integer n) throws ParseException, IOException {
        QueryParser qp = new QueryParser("content", new StandardAnalyzer());
        Query titleQuery = qp.parse(text);
        TopDocs hits = searcher.search(titleQuery, n);
        return hits;
    }
}
