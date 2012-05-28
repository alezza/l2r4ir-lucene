package Lucene;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import database.MSSQLServer;


public class IndexDocuments {

	IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, 
            new StandardAnalyzer(Version.LUCENE_35));

	
	
	public void readFolder() {
		
		File dir = new File("d:\\Tese\\Lucene\\Noticias");
		double somaTamanho = 0.0;
		String[] children = dir.list();
		if (children == null) {
		    // Either dir does not exist or is not a directory
		} else {
		    for (int i=0; i<children.length; i++) {
		        // Get filename of file or directory
		        String filename = children[i];
		        IndexDocuments idx = new IndexDocuments();
		        
		        File file = new File(dir+"\\"+filename);
		        try {
		    	    FileInputStream fis = null;
		    	    BufferedInputStream bis = null;
		    	    DataInputStream dis = null;
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
				    dis = new DataInputStream(bis);
				    String texto = "";
				  
				    while (dis.available() != 0) {
				    	
				    	texto += dis.readLine();
				    	somaTamanho += texto.split("\\s+").length;

				    	
				    }
			    	String id = filename.replaceAll(".txt", "");
			    	String ponto = idx.getPonto(id);
			    	String polygon = idx.getPolygon(id);
			    	String placeDetails = idx.getPlaceDetails(id);
			    	String placeDetailsPolygon = idx.getPlaceDetailsPolygon(id);
			    	String woeid = idx.getWoeid(id);

			    	
			    	idx.constroiIndex(texto, id, ponto, polygon, placeDetails, placeDetailsPolygon, woeid, ""+texto.split("\\s+").length);

		        } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}
	}
	
	
	
   public void constroiIndex(String text, String id, String point, String polygon, String placeDetails, String placeDetailsPolygon, String woeid, String tamanho) throws Exception {
	   
	   Directory luceneIdx = FSDirectory.open(new File("lucene"));
	   IndexWriter namesWriter = new IndexWriter(luceneIdx, conf);
	   
       Document doc = new Document();
       doc.add(new Field("text", text, Field.Store.YES, Field.Index.ANALYZED));
       doc.add(new Field("id", id, Field.Store.YES, Field.Index.NOT_ANALYZED));
       doc.add(new Field("point", point, Field.Store.YES, Field.Index.NO));
       doc.add(new Field("polygon", polygon, Field.Store.YES, Field.Index.NO));
       doc.add(new Field("placeDetails", placeDetails, Field.Store.YES, Field.Index.NO));
       doc.add(new Field("placeDetailsPolygon", placeDetailsPolygon, Field.Store.YES, Field.Index.NO));
       doc.add(new Field("woeid", woeid, Field.Store.YES, Field.Index.NOT_ANALYZED));
       doc.add(new Field("tamanho", tamanho, Field.Store.YES, Field.Index.NOT_ANALYZED));
       
       namesWriter.addDocument(doc);
       
       namesWriter.close();
       luceneIdx.close();
	   
   }
   
   
   
	private final static String BD = "Sigr";
	
	
	
	public String getPonto(String idNoticia) throws SQLException, IOException
	{
		String ponto = null;
		MSSQLServer db = new MSSQLServer(BD);
		
		Connection conn = db.connect();
		
		String query = new String();
		query = "select latitude, longitude from noticiaTemGeographicScope where idNoticia = '"+idNoticia+"'";
				
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while(rs.next()){
			
		ponto = "POINT("+rs.getString(1) + " " + rs.getString(2)+")";

		}
		//System.out.println(ponto);
		return ponto;
		
	}
	
	  
	public String getPolygon(String idNoticia) throws SQLException, IOException
	{
		String polygon = null;
		MSSQLServer db = new MSSQLServer(BD);
		
		Connection conn = db.connect();
		
		String query = new String();
		query = "select polygon from noticiaTemGeographicScopePolygon where idNoticia = '"+idNoticia+"'";
				
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while(rs.next()){
			
			polygon = rs.getString(1);

		}
	//	System.out.println(polygon);
		return polygon;
		
	}
	
	
	public String getPlaceDetails(String idNoticia) throws SQLException, IOException
	{
		String pontos = "";
		MSSQLServer db = new MSSQLServer(BD);
		
		Connection conn = db.connect();
		
		String query = new String();
		query = "select latitude, longitude from noticiaTemPlaceDetails where idNoticia = '"+idNoticia+"'";
				
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		String resultado = "MULTIPOINT(";
		
		while(rs.next()){
			
			pontos += "("+rs.getString(1)+" "+rs.getString(2)+")"+",";

		}
		pontos = resultado + pontos + ")";
		pontos = pontos.replace("),)", "))");

		
		if(pontos.startsWith("MULTIPOINT()")) {
			System.out.println("MULTIPOINT empty");
			return "MULTIPOINT empty";
		}
		else{
			System.out.println(pontos);
			return pontos;
		}
		
		
	}
	
	
	public String getPlaceDetailsPolygon(String idNoticia) throws SQLException, IOException
	{
		String polygons = "";
		MSSQLServer db = new MSSQLServer(BD);
		
		Connection conn = db.connect();
		
		String query = new String();
		query = "select polygon from noticiaTemPlaceDetailsPolygon where idNoticia = '"+idNoticia+"'";
				
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		String resultado = "MULTIPOLYGON(";
		
		
		while(rs.next()){
			
			polygons += rs.getString(1).replace("POLYGON", "")+",";

		}
		resultado += polygons+")";
		resultado = resultado.replace(")),)", ")))");

		
		if(resultado.startsWith("MULTIPOLYGON()")) {
			System.out.println("MULTIPOLYGON empty");
			return "MULTIPOLYGON empty";
		}
		else{
			
			System.out.println(resultado);
			return resultado;
		}
	}
	
	public String getWoeid(String idNoticia) throws SQLException, IOException{
		
		
		MSSQLServer db = new MSSQLServer(BD);
		
		Connection conn = db.connect();
		
		String query = new String();
		query = "select woeId from noticiaTemGeographicScope where idNoticia = '"+idNoticia+"'";
				
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		String resultado="";
		
		while(rs.next()){
			
			resultado = rs.getString(1);

		}
				
		return resultado;
		
	}
   
   
   
   
   public static void main(String[] args) { 
	   IndexDocuments idx = new IndexDocuments();
	   idx.readFolder();
	
	/*   
	   try {
		idx.getWoeid("GH950102-000019");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
   }
	
	
	
}
