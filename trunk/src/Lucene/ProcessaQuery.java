package Lucene;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

//import util.QueryPlacemaker;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class ProcessaQuery {
	
	
	
	public static String point;
	public static String polygon;
	public static String woeid;
	public static String placeDetails = "MULTIPOINT(";
	public static String placeDetailsWoeids="";
	public static String placeDetailsPolygon = "MULTIPOLYGON(";
	
	
	public static HashMap<String, Double> areasPolygons = new HashMap<String, Double>();
	
	

	public void processaQuery(String query) throws XPathExpressionException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException{
		
		QueryPlacemaker qPlacemaker = new QueryPlacemaker();
		qPlacemaker.getInformationFromPlaceMaker(query);
		polygon = qPlacemaker.woeidToPolygon(woeid);

		
//		if(placeDetailsWoeids.startsWith("MULTIPOINT()")){
		String[] woeIdsPlaceDetails = placeDetailsWoeids.split("\\s+");

		int j = 0;
		for (int i = 0; i<woeIdsPlaceDetails.length-1;i++) {
			j++;
			String temp = qPlacemaker.woeidToPolygon(woeIdsPlaceDetails[i])+",";
			placeDetailsPolygon+=temp.replaceAll("POLYGON", "");
		}
		String temp = qPlacemaker.woeidToPolygon(woeIdsPlaceDetails[j]);
		placeDetailsPolygon+=temp.replaceAll("POLYGON", "");

		placeDetailsPolygon+=")";


	}
	
	
	
	
	public void getDocuments(String query) throws IOException, ParseException, XPathExpressionException, ParserConfigurationException, SAXException{
		
		
		String[] wordsD = query.split("\\s+");

        Directory luceneIdx2 = FSDirectory.open(new File("lucene"));
        IndexReader reader = IndexReader.open(luceneIdx2);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        Query q = new QueryParser(Version.LUCENE_35, "text", new StandardAnalyzer(Version.LUCENE_35)).parse(query);
        
        ScoreDoc[] hits = searcher.search(q, 5).scoreDocs;
        

		FileWriter fstream = new FileWriter("featureFile", true);
	    BufferedWriter ficheiro = new BufferedWriter(fstream);
        
        System.out.println("Found " + hits.length + " hits.");
        
        
        Features f = new Features();
        double areaQuery = f.areaPolygon(polygon);
    	int queryLength = wordsD.length;
        
    	
    	
    	
    	for(int i=0;i<hits.length;++i) {
        	
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);

            System.out.println("NEW DOCUMENT");

            String docPoint = d.get("point");
            String newID = d.get("id");
            String document = d.get("text");
            String docPolygon = d.get("polygon");
            String docWoeid = d.get("woeid");
            String docPlaceDetailsPolygon = d.get("placeDetailsPolygon");
            String docPlaceDetails = d.get("placeDetails");

            System.out.println("DOC ID = "+newID);
            
            //features textuais
        	double tf=f.tf(query, document);
        	double idf=f.idf(query);
        	double tfidf =f.tfidf(tf, idf);
        	double bm25=f.bm25(query, document);
        	String docLength = d.get("tamanho");

        	
  	
        	//features geográficas
        	
        	double areaJanee = f.areaJanee(polygon, docPolygon);
        	double distanciaGeoPoints = f.distanciaPontos(docPoint, point);
        	double areaHill = f.areaHill(polygon, docPolygon);        	
        	
        	double areaDocument = f.areaPolygon(docPolygon);
        	double hierarquiaScore = f.taxonomia(woeid, docWoeid);
	
            Vector<Double> placeDetailsAreaHill = f.placeDetailsAreaHill(placeDetailsPolygon, docPlaceDetailsPolygon);
        	Vector<Double> placeDetailsAreaJanee = f.placeDetailsAreaJanee(placeDetailsPolygon, docPlaceDetailsPolygon);

        	Vector<Double> placeDetailsDistGeoScopes = f.placeDetailsDistGeoScopes(placeDetails.replaceAll("\\), \\)", "\\)\\)"), docPlaceDetails);
        	Vector<Double> placeDetailsAreaOverlap = f.placeDetailsAreaOverlap(placeDetailsPolygon, docPlaceDetailsPolygon);
        	
 
        	 /*   	
        	
            System.out.println("TF = "+tf);
            System.out.println("IDF = "+idf);
            System.out.println("TFIDF = "+tfidf);
            System.out.println("BM25 = "+bm25);
            System.out.println("DocLength = "+docLength);
            System.out.println("QueryLength = "+queryLength);
            System.out.println("Area Janee = "+areaJanee);
            System.out.println("Area Hill = "+areaHill);
            System.out.println("Area query = "+areaQuery);
            System.out.println("Area doc = "+areaDocument);
            System.out.println("Hiearquia score = "+hierarquiaScore);
            System.out.println("Distancia: = "+distanciaGeoPoints);
            
            
            System.out.println("Max placeDetailsAreaHill = "+f.max(placeDetailsAreaHill));
            System.out.println("Min placeDetailsAreaHill = "+f.min(placeDetailsAreaHill));
            System.out.println("Avg placeDetailsAreaHill = "+f.mean(placeDetailsAreaHill));
            
            System.out.println("Max placeDetailsAreaJanee = "+f.max(placeDetailsAreaJanee));
            System.out.println("Min placeDetailsAreaJanee = "+f.min(placeDetailsAreaJanee));
            System.out.println("Avg placeDetailsAreaJanee = "+f.mean(placeDetailsAreaJanee));
         
            System.out.println("Max placeDetailsDistGeoScopes = "+f.max(placeDetailsDistGeoScopes));
            System.out.println("Min placeDetailsDistGeoScopes = "+f.min(placeDetailsDistGeoScopes));
            System.out.println("Avg placeDetailsDistGeoScopes = "+f.mean(placeDetailsDistGeoScopes));
            
            System.out.println("Max placeDetailsAreaOverlap = "+f.max(placeDetailsAreaOverlap));
            System.out.println("Min placeDetailsAreaOverlap = "+f.min(placeDetailsAreaOverlap));
            System.out.println("Avg placeDetailsAreaOverlap = "+f.mean(placeDetailsAreaOverlap));
           
            
            
            
          
            
           */

    		String separador = "\t";
    	    ficheiro.write(
    	    		
    	    		"?"+separador+"qid:1"+separador+
    	    		
    	    		
    	    		"1:"+bm25+separador+
    	    		"2:"+tf+separador+
    	    		"3:"+idf+separador+
    	    		"4:"+tfidf+separador+
    	    		"5:"+docLength+separador+
    	    		"6:"+queryLength+separador+
    	    		"7:"+"1"+separador+ //in
    	    		"8:"+"0"+separador+ //near
    	    		"9:"+"0"+separador+ //th
    	    		"10:"+areaJanee+separador+
    	    		"11:"+distanciaGeoPoints+separador+
    	    		"12:"+areaHill+separador+
    	    		"13:"+areaQuery+separador+
    	    		"14:"+areaDocument+separador+
    	    		"15:"+hierarquiaScore+separador+
    	    		"16:"+f.max(placeDetailsAreaHill)+separador+
    	    		"17:"+f.min(placeDetailsAreaHill)+separador+
    	    		"18:"+f.mean(placeDetailsAreaHill)+separador+
    	    		"19:"+f.max(placeDetailsAreaJanee)+separador+
    	    		"20:"+f.min(placeDetailsAreaJanee)+separador+
    	    		"21:"+f.mean(placeDetailsAreaJanee)+separador+
    	    		"22:"+f.max(placeDetailsDistGeoScopes)+separador+
    	    		"23:"+f.min(placeDetailsDistGeoScopes)+separador+
    	    		"24:"+f.mean(placeDetailsDistGeoScopes)+separador+
    	    		"25:"+f.max(placeDetailsAreaOverlap)+separador+
    	    		"26:"+f.min(placeDetailsAreaOverlap)+separador+
    	    		"27:"+f.mean(placeDetailsAreaOverlap)+separador
   	    		
    	    		+"#"+newID +"\n"
    	    		
    	    		);
    	    
    	    System.out.println("processou "+ (i+1) +" documentos");


       }
		
	       ficheiro.close();
	    		
    }
	
	
	
	/**
	 * @param args
	 * @throws TransformerException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws ClassNotFoundException 
	 * @throws XPathExpressionException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws XPathExpressionException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, ParseException {
		
		String query = "Earthquakes in Europe";
		long lStartTime = new Date().getTime(); //start time
		
		ProcessaQuery p = new ProcessaQuery();
		p.processaQuery(query);
		p.getDocuments(query);
	     
		long lEndTime = new Date().getTime(); //end time

		long elapsedTime= lEndTime - lStartTime;
		System.out.println("DEMOROU "+elapsedTime+" milliseconds");
		
		
		//	System.out.println("TAMANHO HASH = "+areasPolygons.size());
	
	}
}
