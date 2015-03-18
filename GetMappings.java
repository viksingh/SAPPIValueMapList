package org.saki.maps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;


public class GetMappings {

	public static String decode(String s) {
//	    return StringUtils.newStringUtf8(Base64.decodeBase64(s));
		byte[] valueDecoded= Base64.decodeBase64(s );		
		return new String(valueDecoded);
		
	}
	public String encode(String s) {
	    return Base64.encodeBase64String(StringUtils.getBytesUtf8(s));
	}
	
	 private static void print(String msg, Object... args) {
	        System.out.println(String.format(msg, args));
	    }
	 
	 private static String trim(String s, int width) {
	        if (s.length() > width)
	            return s.substring(0, width-1) + ".";
	        else
	            return s;
	    }	 

	/**
	 * @param args
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws ZipException 
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException, ZipException {

		int i = 0;
		File newFile = null;
		File newFile1 = null;
		
		
// Reset encoding, causes lot of headache when decoding files
// specifying the values in the method doesn't help as the value is set when JVM is initialised
// This is a hack which forces JVM to set the value		
			System.setProperty("file.encoding","Cp1256");
			java.lang.reflect.Field charset = Charset.class.getDeclaredField("defaultCharset");
			charset.setAccessible(true);
			charset.set(null,null);		   		
		
		try {

// Read the file obtained from SimpleQuery 			
			File file = new File("C:\\temp\\SID1_maps.txt");
			
// Get the document links			
			Document doc = Jsoup.parse(file, "UTF-8");
			 Elements links = doc.select("a[href]");
			 

	        	CloseableHttpClient httpclient = HttpClients.createDefault();
	        	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	        	credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("user:password"));
	        	HttpClientContext localContext = HttpClientContext.create();
	        	localContext.setCredentialsProvider(credentialsProvider);

		 
		        for (Element link : links) {

//Loop through all links		        	
		        	String url = link.attr("abs:href");
		        	String mapName = url.substring(79,url.indexOf("%"));


		        	HttpPost httpPost = new HttpPost(url);

		        	CloseableHttpResponse response = httpclient.execute(httpPost, localContext);

		        	HttpEntity responseEntity = response.getEntity();

// Retrieve a String from the response entity
// Remove extra tags as JSoup can't parse them !		        	

		        	String content = EntityUtils.toString(responseEntity);
		        	content = content.replace("<p1:", "<");
		        	content = content.replace("</p1:", "</");
		        	content = content.replace("<tr:", "<");
		        	content = content.replace("</tr:", "</");
		        	
		        	
		        	Document docResponse = Jsoup.parse(content, "", Parser.xmlParser());		        	
		        	    for (Element e : docResponse.select("xiObj")) {		        	    	
		        	    	for (Element e1 : e.select("content")){
		        	    			 
		        	    		for (Element e2 : e1.select("XiTrafo")){
		        	    			
		        	    			for (Element e3 : e2.select("SourceCode")){
		        	    				i++;
		        	    				for (Element e4 : e3.select("blob")){		        	    				
		        	    					boolean verify = false;
		        	    					String ZipText = e4.text().substring(5);
		        	    					String deCodedZip = decode(ZipText);
		        	    					
		        	    					String zippedFileOut = "C:\\temp\\Zips\\SID1_maps_out"+i+".zip";
		        	    					String unzipDir = "C:\\temp\\Zips\\SID1_maps_out"+i+"\\";
											File zippedFile = new File(zippedFileOut );
											FileUtils.writeStringToFile(zippedFile, deCodedZip);		        	    					
		        	    					
		        	    					File fileUnZipDir = new File(unzipDir);
		        	    					fileUnZipDir.mkdirs();
		        	    					
// Unzip the file											
											ZipFile zipFile = new ZipFile(zippedFileOut);
											Enumeration<?> enu =  zipFile.entries();
											while (enu.hasMoreElements()) {
												ZipEntry zipEntry = (ZipEntry) enu.nextElement();

												String name = zipEntry.getName();
												long size = zipEntry.getSize();
												long compressedSize = zipEntry.getCompressedSize();
												

												InputStream is = zipFile.getInputStream(zipEntry);

												   String fileNameUnZipped = zipEntry.getName();
												   newFile = new File(unzipDir + File.separator + fileNameUnZipped );												
												
												
												FileOutputStream fos = new FileOutputStream(newFile);
												byte[] bytes = new byte[1024];
												int length;
												while ((length = is.read(bytes)) >= 0) {
													fos.write(bytes, 0, length);
												}
												is.close();
												fos.close();
											}
											
											
// Unzip the second file
		        	    					String zippedFileOut1 = "C:\\temp\\Zips\\SID1_maps_out"+i+"\\value";
		        	    					String unzipDir1 = "C:\\temp\\Zips\\SID1_maps_out"+i+"\\value.1";
		        	    					File fileUnZipDir1 = new File(unzipDir1);
		        	    					fileUnZipDir1.mkdirs();
		        	    					ZipFile zipFile1 = new ZipFile(zippedFileOut1);
		        	    					Enumeration<?> enu1 =  zipFile1.entries();
											while (enu1.hasMoreElements()) {

												ZipEntry zipEntry1 = (ZipEntry) enu1.nextElement();

												String name1 = zipEntry1.getName();
												long size1 = zipEntry1.getSize();
												long compressedSize1 = zipEntry1.getCompressedSize();												
												InputStream is1 = zipFile1.getInputStream(zipEntry1);

												   String fileNameUnZipped1 = zipEntry1.getName();
												   String javaFileName = unzipDir1 + File.separator + mapName+"_.java"; 
												   newFile1 = new File(javaFileName);												
												
												
												FileOutputStream fos1 = new FileOutputStream(newFile1);
												byte[] bytes = new byte[1024];
												int length;
												while ((length = is1.read(bytes)) >= 0) {
													fos1.write(bytes, 0, length);
												}
												is1.close();
												fos1.close();												
											
												if ( FileUtils.readFileToString(newFile1).contains("ValueMapService") == true ){
													
													System.out.println("url # " +i +"  "+mapName);
												}											
											}
											zipFile.close();
											zipFile1.close();
					
											
											
											
											
		        	    				}        	    			
		        	    		}
		        	    	} 		        	        
		        	    }
		        	try {
		        	    EntityUtils.consume(response.getEntity());
		        	} finally {
		        	    response.close();
		        
		        
		        }
			
		}
		        
		}} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			
		

	}

}
