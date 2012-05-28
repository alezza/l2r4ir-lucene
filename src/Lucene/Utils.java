package Lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * 
 * Set of functions that might be useful to several components of the application.
 * 
 * @author ivo
 *
 */
public final class Utils {
    
    private Utils() {}


    /**
     * Lucene text tokenizer.
     * 
     * @param documentContent A string corresponding to a document's text content.
     * @param a The Lucene analyzer to use.
     * @return Returns all the words considered and processed by the analyzer, 
     * as well as a count for the number of occurrences of that word in the string.
     */
    public static Map<String, Integer> tokenizeDocument(String documentContent, Analyzer a){
        Map<String, Integer> documentTokenization = new HashMap<String, Integer>();
        
        TokenStream ts = a.tokenStream("", new StringReader(documentContent));
        
        CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
        try {
            ts.reset();
            while (ts.incrementToken()) {
                String term = new String(termAtt.buffer(), 0, termAtt.length());
                Integer count = documentTokenization.get(term);
                if (count == null) {
                    count = 1;
                } else {
                    count++;
                }
                documentTokenization.put(term, count);
            }
            ts.end();
            ts.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return documentTokenization;
    }
    
    /**
     * 
     * @param a
     * @param text
     * @return
     */
    public static String getAnalyzedText(Analyzer a, String text) {

        TokenStream ts = a.tokenStream( null, new StringReader(text));
        CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);

        StringBuilder sb = new StringBuilder();

        try {
            ts.reset();
            while (ts.incrementToken()) {
                sb.append(termAtt.buffer(), 0, termAtt.length());
                sb.append(' ');
            }

            ts.end();
            ts.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
    
    
    
    /**
     * Creates a new array, with the new element in the last position.
     * 
     * @param array
     * @param s
     * @return
     */
    public static String[] arrayAppend(String[] array, String s) {
        
        String[] newArray = new String[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = s;
        
        return newArray;
    }
}
