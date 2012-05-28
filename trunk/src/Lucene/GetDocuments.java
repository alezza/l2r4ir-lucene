package Lucene;
import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
public class GetDocuments {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Directory luceneIdx2 = FSDirectory.open(new File("lucene"));
        IndexReader reader = IndexReader.open(luceneIdx2);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        Query q = new QueryParser(Version.LUCENE_35, "text", new StandardAnalyzer(Version.LUCENE_35)).parse("hello woRLd");
        
        ScoreDoc[] hits = searcher.search(q, 20).scoreDocs;
        
        
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("polygon") + " - " + d.get("id") + " - " + d.get("text"));
        }
    }



}
