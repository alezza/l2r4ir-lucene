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


public class Main {

    /*
    
    public static void main(String[] args) throws Exception {
        
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, 
                new StandardAnalyzer(Version.LUCENE_35));
        
        Directory luceneIdx = FSDirectory.open(new File("lucene"));
        
        IndexWriter namesWriter = new IndexWriter(luceneIdx, conf); 
        
        String test = "Hello world, i'm very happy with lucene.";
        String id = "12a";
        
        Document doc = new Document();
        doc.add(new Field("text", test, Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("id", id, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("polygon", "POINT(1,2)", Field.Store.YES, Field.Index.NO));
        
        namesWriter.addDocument(doc);
        
        namesWriter.close();
        luceneIdx.close();
        
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
 */   
}
