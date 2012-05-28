package Lucene;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import Lucene.Utils;

/*
import dmir.delta.commons.FifoCache;

import dmir.delta.learn.FeatureExtractor;
import dmir.delta.learn.enums.Feature;
import dmir.delta.objects.Reference;
import dmir.delta.objects.Referent;
import dmir.delta.objects.Document;
import dmir.delta.workflow.Configurator;
*/
/**
 * 
 * Implements the vector space model for computing the similarity between two documents.
 * It assumes the existence of a document collection indexed by Lucene, from where it gets the 
 * document frequency of each term. 
 * 
 * @author ivo
 *
 */
public final class TextSimilarities {

    public Integer numDocs;
    
    private IndexReader reader;
    
    private static Map<String, Integer> docFreqCache;
    
   // private final FifoCache<String, Map<String, Integer>> queryTokenizationsCache;
   // private final FifoCache<String, Map<String, Integer>> docTokenizationsCache;
    
    /** Singleton. **/
    public static final TextSimilarities INSTANCE = new TextSimilarities();
    
    private TextSimilarities() {
        
    //    this.queryTokenizationsCache = new FifoCache<String, Map<String,Integer>>(3);
    //    this.docTokenizationsCache = new FifoCache<String, Map<String,Integer>>(1);
        
        try {
            reader = IndexReader.open(
                    FSDirectory.open(new File("lucene")), true);

            numDocs = reader.maxDoc();
            docFreqCache = new HashMap<String, Integer>();
            TermEnum e = reader.terms( );
            while( e.next() ) {
                docFreqCache.put(e.term().text(), e.docFreq());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Inverse Document Frequency, as computed in Lucene.
     * 
     * @param t The term.
     * @return The idf score.
     */
    public double idf(String t) {
        return 1.0 + Math.log(numDocs / (df(t) + 1.0));
    }
    
    public int df(String t) {
        try {
            Integer df = docFreqCache.get(t);
            return df == null ? 0 : df;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return 0;
    }
     

    /**
     * 
     * Get the vector space model score, according to the cosine similarity and tf-idf weights.
     * Texts are pre-processed with Lucene's StandardAnalyzer.
     * 
     * @param q Documtent's text content.
     * @param d Documtent's text content.
     * @return The similarity value (>= 0.0).
     */
    protected Double getSimilarity(String q, String d) {

        if (q == null || d == null || q.length() == 0 || d.length() == 0) return 0.0;
        
        Map<String, Integer> wordsQ = Utils.tokenizeDocument(q, new StandardAnalyzer(Version.LUCENE_35));
        Map<String, Integer> wordsD = Utils.tokenizeDocument(d, new StandardAnalyzer(Version.LUCENE_35));
        
        Double normQ = 0.0;
        Double normD = 0.0;
        Double sim = 0.0;
        
        for (Map.Entry<String, Integer> e : wordsQ.entrySet()) {
            String t = e.getKey();
            Integer tfd = wordsD.get(t);
            Double idf = idf(t);
            
            if (tfd != null) {
                sim += e.getValue()*tfd*Math.pow(idf,2);
                normD += Math.pow(tfd*idf,2);
            }
            
            normQ += Math.pow(e.getValue()*idf, 2);
        }
        
        for (Map.Entry<String, Integer> e : wordsD.entrySet()) {            
            if (!wordsQ.containsKey(e.getKey())) {
                normD += Math.pow(e.getValue()*idf(e.getKey()),2);
            }            
        }
        
        sim = sim / (Math.sqrt(normQ)*Math.sqrt(normD));
        
        return sim.isNaN() ? 0.0 : sim;
    }


    
    public static void main(String[] args) {
    	TextSimilarities t = new TextSimilarities();
    	System.out.println(t.getSimilarity("ola mundo, quero morrer", "Neste mundo so apetece morrer"));
    }
}