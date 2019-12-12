package model;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

public class SearchResult {
    public TopDocs docs;
    public Query query;

    public SearchResult(TopDocs docs, Query query) {
        this.docs = docs;
        this.query = query;
    }
}
