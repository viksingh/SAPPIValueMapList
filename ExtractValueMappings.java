package org.saki.maps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.DatatypeConverter;

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
import org.apache.log4j.varia.NullAppender;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class ExtractValueMappings {
	
	public static final String url = "http:/host:port/rep/support/SimpleQuery";
	public static final String username = "user";
	public static final String password = "password";
	public static final String combinePasswd = username.concat(":".concat(password));
	public static final String zipBaseDir = "C:\\temp\\Zip\\maps_out";
	public static final String excelFilename = "C:/temp/ValueMaps.xls";

	public static String decode(String _str) {
		byte[] valueDecoded = Base64.decodeBase64(_str);
		return new String(valueDecoded);

	}

	public String encode(String _str) {
		return Base64.encodeBase64String(StringUtils.getBytesUtf8(_str));
	}

	private static void print(String _msg, Object... _args) {
		System.out.println(String.format(_msg, _args));
	}

	private static String trim(String _str, int _width) {
		if (_str.length() > _width)
			return _str.substring(0, _width - 1) + ".";
		else
			return _str;
	}

	private static void setCredentials(WebClient _webClient) {
		String base64encodedUsernameAndPassword = base64Encode(combinePasswd);
		_webClient.addRequestHeader("Authorization", "Basic "
				+ base64encodedUsernameAndPassword);
	}

	private static String base64Encode(String stringToEncode) {
		return DatatypeConverter.printBase64Binary(stringToEncode.getBytes());
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws FailingHttpStatusCodeException
	 */

	static void print(String _str) {
		System.out.println(_str);
	}

	public static void main(String[] args)
	throws FailingHttpStatusCodeException, MalformedURLException,
	IOException, IllegalArgumentException, IllegalAccessException,
	SecurityException, NoSuchFieldException {

// Remove log4j warnings 		
		org.apache.log4j.BasicConfigurator.configure(new NullAppender());
		
		int i= 0,j = 0;
		File newFile = null;
		File newFile1 = null;

		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("ValueMap List");
		
		HSSFRow rowhead = sheet.createRow((short) 0);
		rowhead.createCell(0).setCellValue("MapName");
		rowhead.createCell(1).setCellValue("ValueMap ?");
		resetEncoding();
	
		String resultPageText = fillSimpleQueryParams();

		try {
			Document docResultPage = Jsoup.parse(resultPageText, "UTF-8");
			Elements links = docResultPage.select("a[href]");

//This can be changed to use htmlunit as well....
			CloseableHttpClient httpclient = HttpClients.createDefault();
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(combinePasswd));
			HttpClientContext localContext = HttpClientContext.create();
			localContext.setCredentialsProvider(credentialsProvider);

			outerloop:for (Element link : links) {

				// Loop through all links
				String url1 = link.attr("abs:href");
				String mapName = url1.substring(79, url1.indexOf("%"));

				HttpPost httpPost = new HttpPost(url1);

				CloseableHttpResponse response = httpclient.execute(httpPost,
						localContext);

				HttpEntity responseEntity = response.getEntity();

				String content = removeExtraTags(responseEntity);
				
				
				

				Document docResponse = Jsoup.parse(content, "", Parser
						.xmlParser());
				for (Element e : docResponse.select("xiObj")) {
					for (Element e1 : e.select("content")) {

						for (Element e2 : e1.select("XiTrafo")) {

							for (Element e3 : e2.select("SourceCode")) {
								i++;
								for (Element e4 : e3.select("blob")) {
									String ZipText = e4.text().substring(5);
									String deCodedZip = decode(ZipText);

									String zippedFileOut = zipBaseDir
										+ i + ".zip";
									String unzipDir = zipBaseDir
										+ i + "\\";
									File zippedFile = new File(zippedFileOut);
									FileUtils.writeStringToFile(zippedFile,
											deCodedZip);

									File fileUnZipDir = new File(unzipDir);
									fileUnZipDir.mkdirs();

									// Unzip the file
									ZipFile zipFile = new ZipFile(zippedFileOut);
									Enumeration<?> enu = zipFile.entries();
									while (enu.hasMoreElements()) {
										ZipEntry zipEntry = (ZipEntry) enu
										.nextElement();

										InputStream is = zipFile.getInputStream(zipEntry);

										String fileNameUnZipped = zipEntry
										.getName();
										newFile = new File(unzipDir
												+ File.separator
												+ fileNameUnZipped);

										FileOutputStream fos = new FileOutputStream(
												newFile);
										byte[] bytes = new byte[1024];
										int length;
										while ((length = is.read(bytes)) >= 0) {
											fos.write(bytes, 0, length);
										}
										is.close();
										fos.close();
									}

									// Unzip the second file
									String zippedFileOut1 = zipBaseDir
										+ i + "\\value";
									String unzipDir1 = zipBaseDir
										+ i + "\\value.1";
									File fileUnZipDir1 = new File(unzipDir1);
									fileUnZipDir1.mkdirs();
									ZipFile zipFile1 = new ZipFile(
											zippedFileOut1);
									Enumeration<?> enu1 = zipFile1.entries();
									while (enu1.hasMoreElements()) {

										ZipEntry zipEntry1 = (ZipEntry) enu1
										.nextElement();

										InputStream is1 = zipFile1
										.getInputStream(zipEntry1);

										String javaFileName = unzipDir1
										+ File.separator + mapName
										+ "_.java";
										newFile1 = new File(javaFileName);

										FileOutputStream fos1 = new FileOutputStream(
												newFile1);
										byte[] bytes = new byte[1024];
										int length;
										while ((length = is1.read(bytes)) >= 0) {
											fos1.write(bytes, 0, length);
										}
										is1.close();
										fos1.close();
										
										
										HSSFRow row = sheet.createRow((short)++j);
										

											row.createCell(0).setCellValue(mapName);
										if (FileUtils
												.readFileToString(newFile1)
												.contains("ValueMapService") == true) {

											System.out.println("url # " + (j+1)
													+ "  " + mapName);
											row.createCell(1).setCellValue("Yes");											
										}
										else{
											row.createCell(1).setCellValue("No");
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

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		FileOutputStream fileOut = new FileOutputStream(excelFilename);
		workbook.write(fileOut);
		fileOut.close();
	
	}

	/**
	 * @param responseEntity
	 * @return
	 * @throws IOException
	 */
	static String removeExtraTags(HttpEntity responseEntity) throws IOException {
		// Retrieve a String from the response entity
		// Remove extra tags as JSoup can't parse them !
		String content = EntityUtils.toString(responseEntity);
		content = content.replace("<p1:", "<");
		content = content.replace("</p1:", "</");
		content = content.replace("<tr:", "<");
		content = content.replace("</tr:", "</");
		return content;
	}

	static String fillSimpleQueryParams() throws IOException,
			MalformedURLException {
		WebClient webClient = new WebClient();
		setCredentials(webClient);

		DefaultCredentialsProvider creds = new DefaultCredentialsProvider();
		HtmlPage currentPage = webClient.getPage(url);
		HtmlForm form = (HtmlForm) currentPage.getByXPath("/html/body/form").get(0);

		// Get SWC
		HtmlRadioButtonInput choseSWC = form
		.getInputByValue("All software components");
		choseSWC.click();

		// Don't do anything , it comes checked by default		
		HtmlCheckBoxInput considerUnderLyingSWC = form.getInputByName("underL");
		

//Unclick changeList User		
		HtmlCheckBoxInput changeListUser = form.getInputByName("changeL");
		changeListUser.click();

		// Select Message Mapping
		HtmlSelect eSRObjType = form.getOneHtmlElementByAttribute("select",
				"name", "types");
		List<HtmlOption> options = eSRObjType.getOptions();
		for (HtmlOption op : options) {
			if (op.getValueAttribute().equals("XI_TRAFO")) {
				op.setSelected(true);
			}
		}

		HtmlSelect resAttr = form.getOneHtmlElementByAttribute("select",
				"name", "result");
		List<HtmlOption> options1 = resAttr.getOptions();
		for (HtmlOption op : options1) {
			if (op.getValueAttribute().equals("RA_XILINK")) {
				op.setSelected(true);
			}
		}

		HtmlSubmitInput submit = form.getInputByValue("Start query");
		HtmlPage resultPage = submit.click();
		String resultPageText = resultPage.asXml().toString();
		return resultPageText;
	}

	private static void resetEncoding() throws NoSuchFieldException,
			IllegalAccessException {
		// Reset encoding, causes lot of headache when decoding files
		// specifying the values in the method doesn't help as the value is set
		// when JVM is initialised
		// This is a hack which forces JVM to set the value

		System.setProperty("file.encoding", "Cp1256");
		java.lang.reflect.Field charset = Charset.class
		.getDeclaredField("defaultCharset");
		charset.setAccessible(true);
		charset.set(null, null);
	}

}
