package consumers.summarycreator;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import consumers.MessageList;
import consumers.TopicSubscriber;

public class HTMLSummaryCreator implements Observer{
	public static final String directoryName = "output";
	public static final String xsltFileName = "movie_database_by_genre";
	public static final String htmlOutputFileName = "movie_database_";
	
	private TopicSubscriber tsub;
	private MessageList msgList;
	
	
	public HTMLSummaryCreator() throws NamingException, JMSException {
		msgList = new MessageList();
		msgList.addObserver(this);
		tsub = new TopicSubscriber(msgList,"htmlsummarycreator");
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof MessageList) {
			System.out.println("Processing message...");
			MessageList msgList = (MessageList) o;
			String xml_data = msgList.popMessage();
			if (xml_data != null) {
				processXML(xml_data);
			}
		}
	}
	
	/**
	 * Close the Topic Subscriber
	 * Save the messages to file if any
	 */
	@SuppressWarnings("unused")
	public void shutdown() {
		try {
			tsub.close();
		} catch (JMSException e) {
			System.out.println("> Error while closing the topic subscriber");
		}
		ArrayList<String> list = msgList.removeAllMessages();
		for (String s : list) {
			// TODO: save unprocessed messages to file
		}
	}
	
	private void processXML(String xml_data) {
		// process the xml
		String outputFileName;
		File movieCatalogHTML;
		
		do {
			outputFileName = new String( directoryName + "/" + htmlOutputFileName + 
				String.valueOf( (new GregorianCalendar()).getTimeInMillis() ) + ".html");
			movieCatalogHTML = new File(outputFileName);
		} while (movieCatalogHTML.exists());
		
		File movieCatalogXSL = new File(xsltFileName+".xsl");
		
		exportXMLtoHTML(xml_data, movieCatalogXSL, movieCatalogHTML);
	}
	
	private void exportXMLtoHTML(String xml_data, File xslFile, File htmlFile) {

		try {

			// Create an HTML with all the information gathered into the xml
			TransformerFactory factory = TransformerFactory.newInstance();
			Source xslt = new StreamSource(xslFile);
			Transformer transformer = factory.newTransformer(xslt);

			Source text = new StreamSource( new StringReader(xml_data) );
			transformer.transform(text, new StreamResult(htmlFile));
			
			/*TODO guardar XML com linha do XSL*/
			
		} catch (TransformerConfigurationException e) {
			/*TODO review*/
		} catch (TransformerException e1) {
			/*TODO review*/
			
		}
		
	}

	public static void main(String[] args)
	{
		//prepare output directory
		if (directoryName!=null) {
			if (!directoryName.isEmpty()) {
				File f = new File(directoryName);
				if (!f.exists()) f.mkdir();
			}
		}

		System.out.println("<Running HTMLSummaryCreator>");
		HTMLSummaryCreator sumCreator;
		try {
			sumCreator = new HTMLSummaryCreator();
		} catch (NamingException | JMSException e) {
			System.out.println("Can't connect to server");
			System.out.println("Terminating...");
			return;
		}
		
		Scanner sc = new Scanner(System.in);
		String input;
		System.out.println(":Enter exit to shutdown");
		do {
			input = sc.nextLine();
		} while (!input.equals("exit"));
		
		System.out.println("Terminating...");
		sc.close();
		sumCreator.shutdown();
	}

	

}
