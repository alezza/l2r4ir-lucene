package Lucene;

import Hierarchy.Hierarchy;
import Vincenty.VincentyDistanceCalculator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.SAXException;


public class Features {

	static Geometry geo;
	static Geometry geo1;
	Hierarchy h = new Hierarchy();
	
	
	public static boolean flagArea = false;
	
    public Integer numDocs;
    
    private IndexReader reader;
    
    private static Map<String, Integer> docFreqCache;
	
	Features() {
		
        try {
            reader = IndexReader.open(
                    FSDirectory.open(new File("lucene")), true);

            numDocs = reader.maxDoc();
            docFreqCache = new HashMap<String, Integer>();
            TermEnum e = reader.terms();
            while( e.next() ) {
                docFreqCache.put(e.term().text(), e.docFreq());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}



	
	public double areaPolygon(String polygon) {
		
		if (polygon.compareTo("POLYGON ((-180 -90, -180 90, 180 90, 180 -90, -180 -90))")==0)
			return 4.0015727379475427E8;
		
		if(!ProcessaQuery.areasPolygons.containsKey(polygon)){
		
		WKTReader wkt = new WKTReader();
		double dist1;
		double dist2;
		Features f = new Features();
				
		try {
			geo = wkt.read(polygon);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String a = ("POINT"+geo.getCoordinates()[0]).replace(", NaN", "").replace(",", "");
		String b = ("POINT"+geo.getCoordinates()[1]).replace(", NaN", "").replace(",", "");
		String c = ("POINT"+geo.getCoordinates()[2]).replace(", NaN", "").replace(",", "");
	
		/**
		 * Flag para calcular a distancia de pontos. Verifica se esta vai 
		 * ser usada para calcular a area dos polygnos, ou apenas a distancia
		 * mais curta entre dois pontos.
		 */
		
		flagArea = true;
		dist1 = f.distanciaPontos(a, b);
		dist2 = f.distanciaPontos(b, c);
		double area = dist1*dist2;
		ProcessaQuery.areasPolygons.put(polygon, area);
		flagArea = false;
		
		return area;
		}
		else
			return ProcessaQuery.areasPolygons.get(polygon);
	}
	
	public String intersectionPolygon(String qPolygon, String nPolygon) {
		WKTReader wkt = new WKTReader();
		WKTReader wtk2 = new WKTReader();
		
		try {
			geo = wkt.read(qPolygon);
			geo1 = wtk2.read(nPolygon);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Geometry intersection = geo.intersection(geo1);
		return intersection.toString();
	}
	
	public String unionPlygon(String qPolygon, String nPolygon) {
		WKTReader wkt = new WKTReader();
		WKTReader wtk2 = new WKTReader();
		
		try {
			geo = wkt.read(qPolygon);
			geo1 = wtk2.read(nPolygon);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Geometry union = geo.union(geo1);
		return union.toString();
	}
	
	public double mean(Vector<Double> vector) {
		if(vector.size()!=0){
		double sum = 0;
		if (vector.size()!=0)
		for(int i=0;i<vector.size();i++){
			sum += vector.get(i);
			
		}
		return sum/vector.size();
		
		
	}
	else return 0.0;
	}
	
	public String max(Vector<Double> vector){
		
		if(vector.size()!=0){
		Object obj = Collections.max(vector);
		return obj.toString();
		}
		else return "0";
		
	}

	public String min(Vector<Double> vector){
		if(vector.size()!=0){
		Object obj = Collections.min(vector);
		return obj.toString();
		}
		else return "0";
		
	}
	
	public double areaJanee(String qPolygon, String nPolygon) {
		
		WKTReader wkt = new WKTReader();
		WKTReader wtk2 = new WKTReader();
		Features f = new Features();
		
		try {
			geo = wkt.read(qPolygon);
			geo1 = wtk2.read(nPolygon);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String intersection = f.intersectionPolygon(qPolygon, nPolygon);
		 if(intersection.compareTo("GEOMETRYCOLLECTION EMPTY")==0) {
			 return 0;
		 }
		 else {
		
		return (f.areaPolygon(intersection) / (f.areaPolygon(unionPlygon(qPolygon, nPolygon))));
		
		 }
		
	}
	
	public double areaHill(String qPolygon, String nPolygon) {
		
		WKTReader wkt = new WKTReader();
		WKTReader wtk2 = new WKTReader();
		Features f = new Features();
		
		try {
			geo = wkt.read(qPolygon);
			geo1 = wtk2.read(nPolygon);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String intersection = f.intersectionPolygon(qPolygon, nPolygon);
		 if(intersection.compareTo("GEOMETRYCOLLECTION EMPTY")==0) {
			 return 0;
		 }
		 else {
			 
/*			 
			 System.out.println("AREA Q = "+areaPolygon(qPolygon));
			 System.out.println("AREA N = "+f.areaPolygon(nPolygon));
			 System.out.println("INTER = "+areaPolygon(intersection));
			 System.out.println("POLYGON N = "+nPolygon);
			 System.out.println("POLYGON Q = "+qPolygon);
			 System.out.println("POLYGON INTER = "+intersection);
*/
		
		return ((2*f.areaPolygon(intersection)) / (f.areaPolygon(qPolygon) + f.areaPolygon(nPolygon)));
		
		 }
		
	}
	
	
	public Vector<Double> placeDetailsAreaHill(String qPolygon, String nPolygon) {
		
		WKTReader wkt = new WKTReader();
		WKTReader wtk2 = new WKTReader();
		Features f = new Features();
		Vector<Double> v = new Vector<Double>();
		Geometry geoA = null;
		Geometry geoB = null;
		
		
		try {
			geoA = wkt.read(qPolygon);
			geoB = wtk2.read(nPolygon);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int geoSize = geoA.getNumGeometries();
		int geo1Size = geoB.getNumGeometries();
		
		for (int i=0; i<geoSize;i++) {
			for (int j=0; j<geo1Size;j++) {
				if(geoA.getGeometryN(i)!=null && geoB.getGeometryN(j)!=null) {
					v.add(f.areaHill(geoA.getGeometryN(i).toString(), geoB.getGeometryN(j).toString()));
				}
				else
					continue;
					}
			}
		
		return v;

	}
		
	
	public Vector<Double> placeDetailsAreaJanee(String qPolygon, String nPolygon) {
		
		WKTReader wkt = new WKTReader();
		WKTReader wtk2 = new WKTReader();
		Features f = new Features();
		Vector<Double> v = new Vector<Double>();
		Geometry geoA = null;
		Geometry geoB = null;
		
		
		try {
			geoA = wkt.read(qPolygon);
			geoB = wtk2.read(nPolygon);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int geoSize = geoA.getNumGeometries();
		int geo1Size = geoB.getNumGeometries();
		
		for (int i=0; i<geoSize;i++) {
			for (int j=0; j<geo1Size;j++) {
				if(geoA.getGeometryN(i)!=null && geoB.getGeometryN(j)!=null) {
					v.add(f.areaJanee(geoA.getGeometryN(i).toString(), geoB.getGeometryN(j).toString()));
				}
				else
					continue;
					}
			}
		
		return v;

	}
	
	
	public Vector<Double> placeDetailsDistGeoScopes(String qPoint, String nPoint) {
		
		WKTReader wkt = new WKTReader();
		WKTReader wtk2 = new WKTReader();

		Geometry geoA = null;
		Geometry geoB = null;
		
		
		Vector<Double> v = new Vector<Double>();
		
		try {
		geoA = wkt.read(qPoint);
		geoB = wtk2.read(nPoint);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		int geoSize = geoA.getNumGeometries();
		int geo1Size = geoB.getNumGeometries();
		

		for (int i=0; i<geoSize;i++) {

			for (int j=0; j<geo1Size;j++) {
				if(geoA.getGeometryN(i)!=null && geoB.getGeometryN(j)!=null) {
					Features f = new Features();

					String a = geoA.getGeometryN(i).toString();
					String b = geoB.getGeometryN(j).toString();

					double temp = f.distanciaPontos(a, b);
					v.add(temp);
					
				}
				else
					continue;
					}
			}
		
		return v;

	}
	
	
	public Vector<Double> placeDetailsAreaOverlap(String qPolygon, String nPolygon) {
		
		WKTReader wkt = new WKTReader();
		WKTReader wtk2 = new WKTReader();
		Features f = new Features();
		Vector<Double> v = new Vector<Double>();
		Geometry geoA = null;
		Geometry geoB = null;
		
		
		try {
			geoA = wkt.read(qPolygon);
			geoB = wtk2.read(nPolygon);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int geoSize = geoA.getNumGeometries();
		int geo1Size = geoB.getNumGeometries();
		
		for (int i=0; i<geoSize;i++) {
			for (int j=0; j<geo1Size;j++) {
				if(geoA.getGeometryN(i)!=null && geoB.getGeometryN(j)!=null) {
					Geometry overlap = geoA.getGeometryN(i).intersection(geoB.getGeometryN(j));
					 if(overlap.toString().compareTo("GEOMETRYCOLLECTION EMPTY")==0) {
						 v.add(0.0);
					 }
					 else {
					
					v.add(f.areaPolygon(overlap.toString()));
							}
		
			}
		
			}
		}
			return v;

	}
	
	
	public Vector<Double> placeDetailsAreaPolygon(String polygon) {
		

		WKTReader wkt = new WKTReader();
		Geometry geoA = null;
		Vector<Double> v = new Vector<Double>();
		
		try {
		geoA = wkt.read(polygon);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		int geoSize = geoA.getNumGeometries();
		

		for (int i=0; i<geoSize;i++) {

			if(geoA.getGeometryN(i)!=null) {
					Features f = new Features();

					String a = geoA.getGeometryN(i).toString();
					double temp = f.areaPolygon(a);
					v.add(temp);
					
				
					}
			}
		
		return v;
				
	}
	
	
	public double taxonomia(String qWoeid, String nWoeid) throws MalformedURLException, XPathExpressionException, ParserConfigurationException, IOException, SAXException{
		
		return h.calculaScore(h.constroiListaDeListas(h.calculaListas(qWoeid)), h.constroiListaDeListas(h.calculaListas(nWoeid)));
		
	}
	
	
	
	/*
	 * Textual Features
	 * 
	 */
	

	public int queryLength(String query) {
		
		return query.length();
	}
	
	
	public double tf(String q, String d) {
		
		if (q == null || d == null || q.length() == 0 || d.length() == 0) return 0.0;
        
        Map<String, Integer> wordsQ = Utils.tokenizeDocument(q, new StandardAnalyzer(Version.LUCENE_35));
        Map<String, Integer> wordsD = Utils.tokenizeDocument(d, new StandardAnalyzer(Version.LUCENE_35));
        Double tf = 0.0;
        
        for (Map.Entry<String, Integer> e : wordsQ.entrySet()) {
            String t = e.getKey();
            Integer tfd = wordsD.get(t);
     
            if (tfd != null) {
            	tf += e.getValue();
            
            }
              
        }
        return tf/wordsD.size();
	}

	
	
	
	public double idf(String query){

		return 1.0 + Math.log(numDocs / (df(query) + 1.0));
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
	
    
    public double tfidf(double tf, double idf){
    	return tf*idf;
    }
 
    
    public double bm25(String query, String document){
    	Features f = new Features();
    	double tf = f.tf(query, document);
    	double idf = f.idf(query);
    	int docSize = document.split("//s+").length;
    	double avgDocSize = 2.43626457E8/4515;
    	
    	
    	double numerador = tf*idf*2.2;
    	double denominador = tf+1.2*(0.25 + 0.75*(docSize/avgDocSize));

    	return numerador/denominador;
    	
   }
	
    
    //2.43626457E8
	
    
    
    public double distanciaPontos(String pontoA, String pontoB){
    	
		WKTReader wkt = new WKTReader();
		WKTReader wtk2 = new WKTReader();
		double distancia = 0;

		try {
			geo = wkt.read(pontoA);
			geo1 = wtk2.read(pontoB);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(flagArea){
		
		if(geo.getCoordinate().y*geo1.getCoordinate().y<0){
			distancia = VincentyDistanceCalculator.getDistance(geo.getCoordinate().y, geo.getCoordinate().x, 0, geo1.getCoordinate().x) + VincentyDistanceCalculator.getDistance(0, geo.getCoordinate().x, geo1.getCoordinate().y, geo1.getCoordinate().x);
			return distancia;
		}

		if(geo.getCoordinate().x*geo1.getCoordinate().x<0){
			double d1 = VincentyDistanceCalculator.getDistance(geo.getCoordinate().y, geo.getCoordinate().x, geo1.getCoordinate().y, 0);
			double d2 = VincentyDistanceCalculator.getDistance(geo.getCoordinate().y, 0, geo1.getCoordinate().y, geo1.getCoordinate().x);
			return d1+d2;
		}
		else 
			return VincentyDistanceCalculator.getDistance(geo.getCoordinate().y, geo.getCoordinate().x, geo1.getCoordinate().y, geo1.getCoordinate().x);
		
		}
		else 
			return VincentyDistanceCalculator.getDistance(geo.getCoordinate().y, geo.getCoordinate().x, geo1.getCoordinate().y, geo1.getCoordinate().x);
		
		
    	
    	
    	
    }
    
    
    
    
    
    
    
    
    
    
    
	public static void main(String[] args) {
		Features f = new Features();
		
		String aeiou = "POINT (180 90)";
		String aeiou2 = "POINT (-180 -90)";
		String pol = "POLYGON ((-180 -90, -180 90, 180 90, 180 -90, -180 -90))";
		System.out.println(f.areaPolygon(pol));

		
	//	System.out.println(10001.965729229858*2*20003.931458459716);
		
	}
	
	
	
	
}
