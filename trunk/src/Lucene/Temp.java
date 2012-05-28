package Lucene;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.MSSQLServer;

public class Temp {

	/**
	 * @param args
	 */
	
	
	   
	private final static String BD = "Sigr";
	
	
	
	
	public static void main(String[] args) throws SQLException, IOException {
		// TODO Auto-generated method stub
		
		
		MSSQLServer db = new MSSQLServer(BD);
		
		Connection conn = db.connect();
		
		String query = new String();
		query = "select distinct woeid from noticiaTemPlaceDetailsPolygon where polygon NOT LIKE 'POLY%'";
				
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		QueryPlacemaker qPlacemaker = new QueryPlacemaker();
		
		FileWriter fstream = new FileWriter("C:\\novasQuerys.sql", true);
	    BufferedWriter ficheiro = new BufferedWriter(fstream);
		
		while(rs.next()){
			String sql = "UPDATE noticiaTemPlaceDetailsPolygon SET polygon = '"+qPlacemaker.woeidToPolygon(rs.getString(1))+"' where woeid = '"+rs.getString(1)+"'"+"\n";
			ficheiro.write(sql);
	}
	}
}
