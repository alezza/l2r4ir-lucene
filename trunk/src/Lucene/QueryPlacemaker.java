package Lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import database.MSSQLServer;

// Java class demonstrating access to the Yahoo! Placemaker Web service
public class QueryPlacemaker {

	private HashMap<String, String> administrativeScope = new HashMap<String, String>();
	
	// The Yahoo! application ID (please change in order to use your own)
	private String appid = "FwlGQlfV34G5rx.N7eOkm4XJkG3dxLxrjcKRJNB7ZQoo82Pgv.NCnG6_Bj8bG6ZS6II-";
	// 3kQKSkbV34FWLSGlOnY0xL.Mf.QXbbxVyZNLMObtSL4JcxDi5GtQd8NJmTUIu.pHby1RDA-
	//private String appid = "UycaMUTV34EdWHmbojYbqY1Pq888ZRlP3tePPp.1G0G2g2LhkW9PU7HWnY.JJh7L.XA-";
	private HashMap<Integer, String> localScopeMap = new HashMap<Integer, String>();
	private HashMap<Integer, String> placeDetailsMap = new HashMap<Integer, String>();
	
	private HashMap<Integer, String> localScopeNewsMap = new HashMap<Integer, String>();
	private HashMap<Integer, String> placeDetailsNewsMap = new HashMap<Integer, String>();

	public QueryPlacemaker() { }
	
	// Print the contents of an XML document to the standard output
	public String printXML ( Document doc ) throws TransformerException {
		return printXML(doc.getDocumentElement());
	}
	
	// Print the contents of an XML element to the standard output
	public String printXML ( Element element ) throws TransformerException {
		 TransformerFactory transfac = TransformerFactory.newInstance();
	     Transformer trans = transfac.newTransformer();
	     trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	     trans.setOutputProperty(OutputKeys.INDENT, "yes");
	     StringWriter sw = new StringWriter();
	     StreamResult result = new StreamResult(sw);
	     DOMSource source = new DOMSource(element);
	     trans.transform(source, result);
	     String xmlString = sw.toString();
	     //System.out.println(xmlString);
	     return xmlString;
	}
	
	// Send an HTTP POST request and return result as an XML document
	public Document httpPost ( String location, String[] params ) {
		try {
			String data = "";
			for ( int i = 0; i < params.length; i++) {
				if ( data.length() > 0 ) data += "&";
				data += URLEncoder.encode(params[i], "UTF-8") + "=" + URLEncoder.encode(params[++i], "UTF-8");
			}		
			URL url = new URL(location);
			URLConnection conn = url.openConnection ();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			while ((data = rd.readLine()) != null) sb.append(data);
			rd.close();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(sb.toString()));
	        Document doc = db.parse(is);
			return doc;
		} catch ( Exception e ) {
			return null;
		}
	}

	// Send an HTTP GET request and return result as an XML document
	public Document httpGet ( String location ) {
		try {
			URL url = new URL(location);
			URLConnection conn = url.openConnection ();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) sb.append(line);
			rd.close();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(sb.toString()));
	        Document doc = db.parse(is);
			return doc;
		} catch ( Exception e ) {
			return null;
		}
	}

	// Process an input file with Yahoo! Placemaker and return the resulting XML document
	public Document parseDocument ( File doc ) throws IOException {
		return parseDocument ( new FileReader(doc) );
	}
	
	// Process an input reader with Yahoo! Placemaker and return the resulting XML document
	public Document parseDocument ( Reader doc ) throws IOException {
		BufferedReader reader = new BufferedReader(doc);
		StringBuffer sb = new StringBuffer();
		String aux = reader.readLine();
		while ( aux != null ) {
			sb.append(aux);
			aux = reader.readLine();
			if ( aux != null ) sb.append("\n");
		}
		return parseDocument ( sb.toString() );
	}
	
	// Process an input string with Yahoo! Placemaker and return the resulting XML document
	public Document parseDocument ( String doc ) {
		String url = "http://wherein.yahooapis.com/v1/document";
		String params[] = { "appid" , appid, "documentType", "text/plain", "documentContent", doc };
		return httpPost(url,params);
	}
	
	// Produce a WKT POLYGON representation from a Yahoo! Geoplanet WOEID
	public String woeidToPolygon ( String woeid ) {
		
		String url = "http://where.yahooapis.com/v1/place/" + woeid + "?appid=" + appid;
						
		Document content = httpGet(url);
		
		StringBuffer str = new StringBuffer();
		
		if(content == null)
			return "not available";
		try {
		File tempFile = new File("whatever");
		  BufferedWriter out;
		
			out = new BufferedWriter(new FileWriter(tempFile));
		
	      out.write(printXML(content));
	      out.close();
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(false); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(tempFile);
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		String xpathStr = "//boundingBox";
	
		XPathExpression expr = xpath.compile(xpathStr);
		Element element = (Element) expr.evaluate(doc, XPathConstants.NODE);
		
		str.append("POLYGON (( ");
		String latitudeSW = xpath.compile(xpathStr+"//southWest/latitude/text()").evaluate(element);
		String longitudeSW = xpath.compile(xpathStr+"//southWest/longitude/text()").evaluate(element);
		String latitudeNE = xpath.compile(xpathStr+"//northEast/latitude/text()").evaluate(element);
		String longitudeNE = xpath.compile(xpathStr+"//northEast/longitude/text()").evaluate(element);
		
		str.append(longitudeSW + " " + latitudeSW);
		str.append(" , ");
		str.append(longitudeSW + " " + latitudeNE);
		str.append(" , ");
		str.append(longitudeNE + " " + latitudeNE);
		str.append(" , ");
		str.append(longitudeNE + " " + latitudeSW);
		str.append(" , ");
		str.append(longitudeSW + " " + latitudeSW);
		str.append(" ))");
		
		tempFile.delete();
		} catch (IOException e) {
		
		} catch (TransformerException e) {
			
		} catch (ParserConfigurationException e) {
			
		} catch (XPathExpressionException e) {
			
		} catch (SAXException e) {
			
		}
		return str.toString();
	}

	// Return a scope from a Yahoo! Placemaker XML document
	public String placemakerDocumentToScope ( Document doc, boolean geo, boolean woeid ) throws XPathExpressionException {
		String xpathAdmWOEID = "//administrativeScope/woeId";
		String xpathAdmName = "//administrativeScope/name";
		String xpathGeoWOEID = "//geographicScope/woeId";
		String xpathGeoName = "//geographicScope/name";
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = null;
		if (geo) {
			if (woeid) expr = xpath.compile(xpathGeoWOEID);
			else expr = xpath.compile(xpathGeoName);
		} else {
			if (woeid) expr = xpath.compile(xpathAdmWOEID);
			else expr = xpath.compile(xpathAdmName);			
		}
		String result = expr.evaluate(doc);
		if (result.indexOf("CDATA[") >=0 ) result=result.substring(7,result.length()-2);
		return result;
	}

	// Return list of places from a Yahoo! Placemaker XML document
	public String[] placemakerDocumentToPlaces ( Document doc, boolean woeid ) throws XPathExpressionException {
		String xpathWOEID = "//localScope/woeId/text()";
		String xpathName = "//localScope/name/text()";
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = null;
		if (woeid) expr = xpath.compile(xpathWOEID);
		else expr = xpath.compile(xpathName);
		NodeList elemList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		ArrayList<String> strList = new ArrayList<String>();
		for ( int i = 0; i < elemList.getLength(); i++ ) {
			String aux = elemList.item(i).getNodeValue();
			if (aux.indexOf("CDATA[") >= 0) aux=aux.substring(7,aux.length()-2);
			strList.add( aux );
		}
		return strList.toArray(new String[0]);
	}

	// Return WKT POINT representation for the scope from a Yahoo! Placemaker XML document
	public String placemakerDocumentToScopePoint ( Document doc, boolean geo ) throws XPathExpressionException {
		String xpathAdm = "//administrativeScope/centroid";
		String xpathGeo = "//geographicScope/centroid";
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = null;
		if (geo) expr = xpath.compile(xpathGeo);
		else expr = xpath.compile(xpathAdm);
		Element element = (Element) expr.evaluate(doc,XPathConstants.NODE);
		String latitude = xpath.compile(".//latitude/text()").evaluate(element);
		String longitude = xpath.compile(".//longitude/text()").evaluate(element);
		return "POINT ( " + longitude + " " + latitude + " )";
	}

	// Return WKT POLYGON representation for the scope from a Yahoo! Placemaker XML document
	public String placemakerDocumentToScopePolygon ( String filename, boolean toDatabase ) throws XPathExpressionException, TransformerException, SAXException, IOException, ParserConfigurationException {
		String woeid = administrativeScope.get(filename);
		
		String polygon = woeidToPolygon(woeid);
		
		if(toDatabase){
			BufferedWriter out = new BufferedWriter(new FileWriter("ScopePolygon.sql", true));
		    out.write("INSERT INTO dbo.noticiaTemScopePolygon VALUES('" + filename + "', '" + polygon + "');\n");
		    out.close();
		}
		return polygon;
	}
	
	public void waitTime(){
        long t0,t1;
        t0=System.currentTimeMillis();
        do{
            t1=System.currentTimeMillis();
        }
        while (t1-t0<1000);
	}
	
	public void placemakerDocumentToScopePolygonNews ( ) {
		
		MSSQLServer database = new MSSQLServer("Sigr");
		
		Set<String> newsID = database.getNews();
		try {
		
		
		for(String s: newsID){
			Statement stmt;
			
				stmt = database.connect().createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT idNoticia FROM noticiaTemPlaceDetails where woeID = '" + s + "'");
			String polygon = woeidToPolygon(s);
			while(rs.next()){
				String woeID = rs.getString(1);
				System.out.println(s + " " + woeID);
				
				BufferedWriter out = new BufferedWriter(new FileWriter("ScopePolygon.sql", true));
				 out.write("UPDATE noticiaTemPlaceDetails SET Polygon = geometry::STGeomFromText('" + polygon + "', 4326) WHERE idNoticia ='" + woeID + "' AND woeID = '"+s+ "'\n");
				 out.close();
				 //waitTime();
			}
			
		}
		} catch (SQLException e) {
			System.err.println("[SQL EXCEPTION]");
		} catch (IOException e) {
			System.err.println("[IOException]");
		}    
	}
	
	
	// Return an array of WKT POLYGON representations for the place references in a Yahoo! Placemaker XML document
	public String[] placemakerDocumentToPolygons ( Document doc ) throws XPathExpressionException, TransformerException, SAXException, IOException, ParserConfigurationException {
		String woeids[] = placemakerDocumentToPlaces(doc, true);
		ArrayList<String> polygons = new ArrayList<String>();
		for ( String woeid : woeids ) polygons.add(woeidToPolygon(woeid));
		return polygons.toArray(new String[0]);
	}

	// Return an array of WKT POINT representations for the place references in a Yahoo! Placemaker XML document
	public String[] placemakerDocumentToPoints ( Document doc ) throws XPathExpressionException {
		String xpathStr = "//localScope/centroid";
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile(xpathStr);
		NodeList elemList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		ArrayList<String> strList = new ArrayList<String>();
		for ( int i = 0; i < elemList.getLength(); i++ ) {
			String latitude = xpath.compile(".//latitude/text()").evaluate(elemList.item(i));
			String longitude = xpath.compile(".//longitude/text()").evaluate(elemList.item(i));
			String point = "POINT ( " + longitude + " " + latitude + " )";
			strList.add( point );
		}
		return strList.toArray(new String[0]);
	}
	
	// Return a WKT MULTIPOINT representation for the place references in a Yahoo! Placemaker XML document
	public String placemakerDocumentToMultipoint ( Document doc ) throws XPathExpressionException {
		String xpathStr = "//localScope/centroid";
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile(xpathStr);
		NodeList elemList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		StringBuffer str = new StringBuffer();
		if ( elemList.getLength() > 0 ) str.append("MULTIPOINT (");
		for ( int i = 0; i < elemList.getLength(); i++ ) {
			String latitude = xpath.compile(".//latitude/text()").evaluate(elemList.item(i));
			String longitude = xpath.compile(".//longitude/text()").evaluate(elemList.item(i));
			String point = " ( " + longitude + " " + latitude + " )";
			str.append( point );
		}
		str.append(" )");
		return str.toString();
	}
	
	public void getNewsFromPlaceMaker(String args) throws ParserConfigurationException, SAXException, IOException, TransformerException{
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setValidating(false);
		
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.parse(new File(args));
	    doc.getDocumentElement().normalize();
		
	    NodeList nList = doc.getElementsByTagName("noticia");
	    
	    @SuppressWarnings("unused")
		String texto = null;
	    for (int temp = 0; temp < nList.getLength(); temp++) {
	        Node nNode = nList.item(temp);	    
	        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	  
	           Element eElement = (Element) nNode;
	  
	          
	           String id =  eElement.getElementsByTagName("id").item(0).getTextContent();
	           String titulo =  eElement.getElementsByTagName("titulo").item(0).getTextContent();
	           
	           //texto =  eElement.getElementsByTagName("texto").item(0).getTextContent();
	           System.out.println("ID : "  + id);
	           
	           //System.out.println("TITULO : "  + titulo);
	           //System.out.println("TEXTO : "  + texto);
	           
	           BufferedWriter out = new BufferedWriter(new FileWriter("SIGR\\topicos\\" + id));
	           out.write(printXML(parseDocument(titulo))); // + " " + texto
	           out.close();
	        } 	  
	         
	     }
		
	}

	public String getAdministrativeScope(String filename, boolean toDatabase) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		
		String name = null;
		String woeId = null;
		String type = null;
		String latitude = null;
		String longitude = null;
		String administrativeScope = "";
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(false); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc2 = builder.parse(filename);
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		XPathExpression expr = xpath.compile("//administrativeScope/name/text()");
		Object result = expr.evaluate(doc2, XPathConstants.NODESET);
		
		NodeList nodes = (NodeList) result;
		for (int i = 0; i < nodes.getLength(); i++) {
		    name = nodes.item(i).getNodeValue();
		}
		
		 expr = xpath.compile("//administrativeScope/woeId/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		 for (int i = 0; i < nodes.getLength(); i++) {
			    woeId = nodes.item(i).getNodeValue();
			}
		 
		 expr = xpath.compile("//administrativeScope/centroid/latitude/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		 for (int i = 0; i < nodes.getLength(); i++) {
			    latitude = nodes.item(i).getNodeValue();
			}
		 
		 expr = xpath.compile("//administrativeScope/centroid/longitude/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		 for (int i = 0; i < nodes.getLength(); i++) {
			    longitude = nodes.item(i).getNodeValue();
			}
		 expr = xpath.compile("//administrativeScope/type/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		 for (int i = 0; i < nodes.getLength(); i++) {
			    type = nodes.item(i).getNodeValue();
			}
		 
		if(toDatabase){
		 BufferedWriter out = new BufferedWriter(new FileWriter("administrativeScope.sql", true));
		 if(name !=null){ name = name.replace("'", ""); type = type.replace("'", ""); }
         out.write("INSERT INTO dbo.noticiaTemAdministrativeScope VALUES('"+ filename + "', '" + woeId + "', '" + name + "', '" +  type + "', '" + latitude + "', '" + longitude + "');\n");
         out.close(); 
		} else { administrativeScope = name + "\t" +  type + "\t" + latitude + "\t" + longitude;}
		
		 return administrativeScope;
		 
	}
	
	public HashMap<Integer, String> getPlaceDetails(String filename, boolean toDatabase) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		
		String name = null;
		String woeId = null;
		String type = null;
		String latitude = null;
		String longitude = null;
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(false); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc2 = builder.parse(filename);
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		XPathExpression expr = xpath.compile("//placeDetails");
		Object result = expr.evaluate(doc2, XPathConstants.NODESET);
		
		HashMap<Integer, String> placeDeatilsMap = new HashMap<Integer, String>();
		
		NodeList nodes = (NodeList) result;
		
		for (int i = 0; i < nodes.getLength(); i++) {
		    
		    expr = xpath.compile("//placeDetails["+(i+1)+"]/place/name/text()");
			result = expr.evaluate(doc2, XPathConstants.NODESET);
		
			NodeList nodes2 = (NodeList) result;
			name = nodes2.item(0).getNodeValue();
			
			expr = xpath.compile("//placeDetails["+(i+1)+"]/place/woeId/text()");
			result = expr.evaluate(doc2, XPathConstants.NODESET);
			nodes2 = (NodeList) result;
			woeId = nodes2.item(0).getNodeValue();
		
			expr = xpath.compile("//placeDetails["+(i+1)+"]/place/centroid/latitude/text()");
			result = expr.evaluate(doc2, XPathConstants.NODESET);
			nodes2 = (NodeList) result;
		    latitude = nodes2.item(0).getNodeValue();
			
		 	 expr = xpath.compile("//placeDetails["+(i+1)+"]/place/centroid/longitude/text()");
		 	 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 	 nodes2 = (NodeList) result;
		     longitude = nodes2.item(0).getNodeValue();
			
		     expr = xpath.compile("//placeDetails["+(i+1)+"]/place/type/text()");
		     result = expr.evaluate(doc2, XPathConstants.NODESET);
		     nodes2 = (NodeList) result;
		     type = nodes2.item(0).getNodeValue();
		 
		     placeDeatilsMap.put(Integer.parseInt(woeId), name.replace("'", "") + "\t" +  type.replace("'", "") + "\t" + latitude + "\t" + longitude);
		     if(toDatabase){
		    	 BufferedWriter out = new BufferedWriter(new FileWriter("placeDetails.sql", true));
		    	 if(name !=null){ name = name.replace("'", ""); type = type.replace("'", ""); }
		    	 out.write("INSERT INTO dbo.noticiaTemPlaceDetails VALUES('"+ filename + "', '" + woeId + "', '" + name + "', '" +  type + "', '" + latitude + "', '" + longitude + "');\n");
		    	 out.close();
		     }
		   // RUN TIME SYSTEM
		   ProcessaQuery.placeDetails += "("+longitude+" "+latitude+"), ";  
		   ProcessaQuery.placeDetailsWoeids += woeId+" ";
		  
		}
		ProcessaQuery.placeDetails+=")";

		
		  return placeDeatilsMap;
	}
	
	public String getGeographicScope(String filename, boolean toDatabase) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		
		String name = null;
		String woeId = null;
		String type = null;
		String latitude = null;
		String longitude = null;
		String geographicScope = null;
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(false); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc2 = builder.parse(filename);
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		XPathExpression expr = xpath.compile("//geographicScope/name/text()");
		Object result = expr.evaluate(doc2, XPathConstants.NODESET);
		
		NodeList nodes = (NodeList) result;
		for (int i = 0; i < nodes.getLength(); i++) { 
		    name = nodes.item(i).getNodeValue();
		}
		
		 expr = xpath.compile("//geographicScope/woeId/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		 for (int i = 0; i < nodes.getLength(); i++) {
			    woeId = nodes.item(i).getNodeValue();
			}
		 
		 expr = xpath.compile("//geographicScope/centroid/latitude/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		 for (int i = 0; i < nodes.getLength(); i++) {
			    latitude = nodes.item(i).getNodeValue();
			}
		 
		 expr = xpath.compile("//geographicScope/centroid/longitude/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		 for (int i = 0; i < nodes.getLength(); i++) {
			    longitude = nodes.item(i).getNodeValue();
			}
		 expr = xpath.compile("//geographicScope/type/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		 for (int i = 0; i < nodes.getLength(); i++) {
			    type = nodes.item(i).getNodeValue();
			}
		 

		 
		 administrativeScope.put(filename, woeId);
		 if(name !=null){ name = name.replace("'", ""); type = type.replace("'", ""); }
		 
		 if(toDatabase){
		 BufferedWriter out = new BufferedWriter(new FileWriter("geographicScope.sql", true));
         out.write("INSERT INTO dbo.noticiaTemGeographicScope VALES('"+ filename + "', '" + woeId + "', '" + name + "', '" +  type + "', '" + latitude + "', '" + longitude + "');\n");
         out.close();
		 }else { geographicScope = name + "\t" +  type + "\t" + latitude + "\t" + longitude; }
		 

		 // SISTEMA RUN TIME
		 if(latitude==null||longitude==null){
		 ProcessaQuery.point = "POINT()";
		 }
		 else{
		 ProcessaQuery.point = "POINT("+latitude+" "+longitude+")";
		 }

			 ProcessaQuery.woeid = woeId;
		 //
		 
		 return geographicScope;

	}
	
	public HashMap<Integer, String> getLocalScopes(String filename, boolean toDatabase) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		
		System.out.println("LOCAL SCOPE");
		String name = null;
		String woeId = null;
		String type = null;
		String latitude = null;
		String longitude = null;
		HashMap<Integer, String> results = new HashMap<Integer, String>();
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(false); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc2 = builder.parse(filename);
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		XPathExpression expr = xpath.compile("//localScope");
		Object result = expr.evaluate(doc2, XPathConstants.NODESET);
		
		//para cada nó localScope
		NodeList nodes = (NodeList) result;
		for (int i = 0; i < nodes.getLength(); i++) { 
			 expr = xpath.compile("//localScope["+(i+1)+"]/name/text()");
		     Object result2 = expr.evaluate(doc2, XPathConstants.NODESET);
		     NodeList nodes2 = (NodeList) result2;
		     name = nodes2.item(0).getTextContent();
		     
		     expr = xpath.compile("//localScope["+(i+1)+"]/woeId/text()");
		     result2 = expr.evaluate(doc2, XPathConstants.NODESET);
		     nodes2 = (NodeList) result2;
		     woeId = nodes2.item(0).getNodeValue();
		  
		     expr = xpath.compile("//localScope["+(i+1)+"]/type/text()");
		     result2 = expr.evaluate(doc2, XPathConstants.NODESET);
		     nodes2 = (NodeList) result2;
		     type = nodes2.item(0).getNodeValue();
		
		     expr = xpath.compile("//localScope["+(i+1)+"]//latitude/text()");
		     result2 = expr.evaluate(doc2, XPathConstants.NODESET);
		     nodes2 = (NodeList) result2;
		     latitude = nodes2.item(0).getNodeValue();
		     
		     expr = xpath.compile("//localScope["+(i+1)+"]//longitude/text()");
		     result2 = expr.evaluate(doc2, XPathConstants.NODESET);
		     nodes2 = (NodeList) result2;
		     longitude = nodes2.item(0).getNodeValue();
		     
		     if(name !=null){ name = name.replace("'", ""); type = type.replace("'", ""); }
		     if(toDatabase){
			     BufferedWriter out = new BufferedWriter(new FileWriter("localScope.sql", true));
		         out.write("INSERT INTO dbo.noticiaTemLocalScope VALES('"+ filename + "', '" + woeId + "', '" + name + "', '" +  type + "', '" + latitude + "', '" + longitude + "');\n");
		         
		         out.close();			    
			    
		     }else { results.put(Integer.parseInt(woeId),  name + "\t" +  type + "\t" + latitude + "\t" + longitude); }
		    }
		return results;
		}
		
	public void getInformationFromPlaceMaker(String news) throws ClassNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException{
		
		String filename = news.split("\t")[0];
		
		BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write(printXML(parseDocument(news))); 
        out.close();
        
		File f = new File(filename);
		if(f.length() ==0) return;
		try{
   //     getAdministrativeScope(filename, true);
        getGeographicScope(filename, false);
    //    getLocalScopes(filename, true);
      //  placemakerDocumentToScopePolygon(filename, true);
        getPlaceDetails(filename, false);
		}catch( com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException e ){ System.err.println("BUMMMM");}    
        f.delete();
	}
		
	public void getExtent(String filename) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		
		String latitudeCE = null;
		String longitudeCE = null;
		String latitudeSW = null;
		String longitudeSW = null;
		String latitudeNE = null;
		String longitudeNE = null;
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc2 = builder.parse("noticias\\"+filename);
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		XPathExpression expr = xpath.compile("//extents/center/latitude/text()");
		Object result = expr.evaluate(doc2, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		latitudeCE = nodes.item(0).getNodeValue();
				
		 expr = xpath.compile("//extents/center/longitude/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		
		 longitudeCE = nodes.item(0).getNodeValue();
			
		 expr = xpath.compile("//extents/southWest/latitude/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		
		 latitudeSW = nodes.item(0).getNodeValue();
			
		 expr = xpath.compile("//extents/southWest/longitude/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		
		 longitudeSW = nodes.item(0).getNodeValue();
		 
		 expr = xpath.compile("//extents/northEast/latitude/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		
		 latitudeNE = nodes.item(0).getNodeValue();
			
		 expr = xpath.compile("//extents/northEast/longitude/text()");
		 result = expr.evaluate(doc2, XPathConstants.NODESET);
		 nodes = (NodeList) result;
		
		 longitudeNE = nodes.item(0).getNodeValue();
		 BufferedWriter out = new BufferedWriter(new FileWriter("extents.sql", true));
         out.write("INSERT INTO dbo.noticiaTemExtent VALES('"+ filename + "', '" + latitudeCE + "', '" + longitudeCE  + "', '" + latitudeSW + "', '" + longitudeSW + "', '" +  latitudeNE + "', '" + longitudeNE  + "');\n");
         out.close();
	}			        

	
	public ArrayList<String> processUserQuery(String query, boolean news) throws IOException, TransformerException, XPathExpressionException, ParserConfigurationException, SAXException{
		
		//send query to placemaker and save response in a temp file
		 BufferedWriter out = new BufferedWriter(new FileWriter("temp"));
         out.write(printXML(parseDocument(query))); 
         out.close();
         
         ArrayList<String> results = new ArrayList<String>();
	         try{
		         //grab file and ...
				 File f = new File("temp");
				 		 
				//...compute administrativeScope
				 String adminScope = getAdministrativeScope("temp", false);
				 results.add(adminScope);
				 				 
				 //...compute geographicScope
				 String geoScope = getGeographicScope("temp", false);
				 results.add(geoScope);
						     
				 //...compute localScopes
				 HashMap<Integer, String> map1 = getLocalScopes("temp", false);
				 for(Integer i: map1.keySet()){
					 if(news) localScopeNewsMap.put(i, map1.get(i));
					 else localScopeMap.put(i, map1.get(i));
				 }
				 
				 //...get the corresponding polygon
			     String polygon = placemakerDocumentToScopePolygon("temp", false);
			     //System.out.println(polygon);
			     results.add(polygon);
			     
			     HashMap<Integer, String> map2 = getPlaceDetails("temp", false);
			     for(Integer i: map2.keySet()){
			    	 if(news) placeDetailsNewsMap.put(i, map2.get(i));
			    	 else placeDetailsMap.put(i, map2.get(i));
			     }
			     
			     f.delete();
		}catch( com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException e){ }
		    	
		 return results;   
	}
	
	public HashMap<String, String> computeFeatures(String query, HashMap<String, String> news) throws SQLException, XPathExpressionException, IOException, TransformerException, ParserConfigurationException, SAXException{
		
		ArrayList<String> queryData = processUserQuery(query, false);
		String queryType = queryData.get(1).split("\t")[1];
		System.out.println("##################################################");
		System.out.println("THE TYPE OF THE QUERY IS: " + queryType);
		System.out.println("##################################################\n");
		ArrayList<String> newsData = null;
		
		MSSQLServer database = new MSSQLServer("Sigr");
		
		HashMap<String, String> results = new HashMap<String, String>();
		
		for(String s: news.keySet()) {
			
			System.out.println("[PLACEMAKER] processing news " + s );
			newsData = processUserQuery(news.get(s), true);
			
			if(newsData.size() == 0) continue;
			
			System.err.println("["+s+"] computing features...\n");
			
			float BM25 = Float.parseFloat(news.get(s).split("\t")[0]);
			float tf = database.computeTermFrequency(query, news.get(s));
			float idf = database.computeInverseDocumentFrequency(query, news.get(s));
			float tfidf = tf * idf;
			
			float distAdminScope = database.computeDistance(queryData.get(0).split("\t")[3], queryData.get(0).split("\t")[2], newsData.get(0).split("\t")[3], newsData.get(0).split("\t")[2]);
			float distGeoScope = database.computeDistance(queryData.get(1).split("\t")[3], queryData.get(1).split("\t")[2], newsData.get(1).split("\t")[3], newsData.get(1).split("\t")[2]);
			
			float distMinLocalScope = 10000;
			float distMaxLocalScope = -1000;
			float distAvgLocalScope = 0;
			float temp;
			 
			for(Integer n: getLocalScopeNewsMap().keySet()){
				for(Integer q: getLocalScopeMap().keySet()){
					
					temp = database.computeDistance(getLocalScopeMap().get(q).split("\t")[3], getLocalScopeMap().get(q).split("\t")[2], getLocalScopeNewsMap().get(n).split("\t")[3], getLocalScopeNewsMap().get(n).split("\t")[3]); 
					distAvgLocalScope += temp;
					
					if(distMaxLocalScope < temp) distMaxLocalScope = temp;
					if(distMinLocalScope > temp) distMinLocalScope = temp;
				}
			}
			distAvgLocalScope = getLocalScopeMap().size() + getLocalScopeNewsMap().size();
				
			float distMaxPlaceDetails = -10000;
			float distMinPlaceDetails = 10000;
			float distAvgPlaceDetails = 0;
			float temp2;
			
			for(Integer n: getPlaceDetailsNewsMap().keySet()){
				for(Integer q: getPlaceDetailsMap().keySet()){
					
					temp2 = database.computeDistance(getPlaceDetailsMap().get(q).split("\t")[3], getPlaceDetailsMap().get(q).split("\t")[2], getPlaceDetailsNewsMap().get(n).split("\t")[3], getPlaceDetailsNewsMap().get(n).split("\t")[3]); 
					distAvgLocalScope += temp2;
					
					if(distMaxPlaceDetails < temp2) distMaxPlaceDetails = temp2;
					if(distMinPlaceDetails > temp2) distMinPlaceDetails = temp2;
				}
			}
			distAvgPlaceDetails = getPlaceDetailsNewsMap().size() + getPlaceDetailsMap().size();
				
			
			float area_1 = 0, area_2 = 0;
			if(queryData.get(2).length() == 0 || queryData.get(2).contains("not available") || newsData.get(2).length() == 0 || newsData.get(2).contains("not available")){
				area_1 = 0; area_2 = 0;
			}else{
				area_1 = database.computeAreaJanee(queryData.get(2), newsData.get(2));
				area_2 = database.computeAreaHill(queryData.get(2), newsData.get(2));
			}
			results.put(s, BM25 + "\t" + area_1 + "\t" + distGeoScope + "\t" + tf + "\t" + idf + "\t" + tfidf + "\t" + area_2 + "\t" + distAdminScope + "\t" + distMinLocalScope+ "\t" + distMaxLocalScope+ "\t" + distAvgLocalScope+ "\t" + distMinPlaceDetails+ "\t" + distMaxPlaceDetails+ "\t" + distAvgPlaceDetails + "\t" + queryType);
		} 
		return results;
		
	}
	
	
	/**
	 * @return the localScopeMap
	 */
	public HashMap<Integer, String> getLocalScopeMap() {
		return localScopeMap;
	}

	/**
	 * @return the placeDetailsMap
	 */
	public HashMap<Integer, String> getPlaceDetailsMap() {
		return placeDetailsMap;
	}

	/**
	 * @return the localScopeNewsMap
	 */
	public HashMap<Integer, String> getLocalScopeNewsMap() {
		return localScopeNewsMap;
	}

	/**
	 * @return the placeDetailsNewsMap
	 */
	public HashMap<Integer, String> getPlaceDetailsNewsMap() {
		return placeDetailsNewsMap;
	}
}

