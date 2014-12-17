package consumers.statsproducer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import util.XMLHelper;

import consumers.MessageList;
import consumers.TopicSubscriber;

import movies.Movie;
import movies.MovieCatalog;


public class StatsProducer implements Observer {
	public static final String directoryName = "output";
	public static final String statsFileName = "StatsProducer";
	public static final int rankSize = 5;
	
	private TopicSubscriber tsub;
	private MessageList msgList;
	private MovieCatalog moviesRank;

	public StatsProducer() throws NamingException, JMSException {
		msgList = new MessageList();
		msgList.addObserver(this);
		tsub = new TopicSubscriber(msgList,"statsproducer");
		moviesRank = null;
	}

	
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof MessageList) {
			MessageList msgList = (MessageList) o;
			String xml_data = msgList.popMessage();
			if (xml_data != null) {
				System.out.println("Processing message...");
				processXML(xml_data);
			}
		}
	}
	
	/**
	 * Close the Topic Subscriber
	 * Save the messages to file if any
	 */
	public void shutdown() {
		try {
			tsub.close();
		} catch (JMSException e) {
			System.out.println("> Error while closing the topic subscriber");
		}
		ArrayList<String> list = msgList.removeAllMessages();
		for (String s : list) {
			//TODO save unprocessed messages to file
		}
	}
	
	private void processXML(String xml_data) {
		MovieCatalog movieCatalog;
		try {
			movieCatalog = (MovieCatalog) XMLHelper.unmarshal(MovieCatalog.class, xml_data);
			
			if (moviesRank == null) {
				try {
					moviesRank = (MovieCatalog) XMLHelper.unmarshal(MovieCatalog.class, new File(directoryName+"/"+statsFileName+".xml"));
				} catch (FileNotFoundException fe) {
					moviesRank = new MovieCatalog();
				}
			}
		} catch (JAXBException e) {
			/*ignore*/
			return;
		}
		moviesRank = produceStats(moviesRank,movieCatalog);
	}

	private MovieCatalog produceStats(MovieCatalog moviesRank,MovieCatalog movieCatalog) {
		
		MovieCatalog final_mc = new MovieCatalog();
		MovieCatalog rated_mc = new MovieCatalog();
		
		// Remove movies without score
		for (Movie m : movieCatalog.getMovie()) {
			if (m.getScore() != null) rated_mc.getMovie().add(m);
		}

		boolean exists;
		for (Movie m : moviesRank.getMovie()) {
			exists = false;
			
			for (Movie m2 : rated_mc.getMovie()) {
				if (m.getTitle().compareTo(m2.getTitle()) == 0)
				{
					exists = true;
					break;
				}
			}
			if(exists)
				continue;
			else
				rated_mc.getMovie().add(m);
		}

		Movie temp;
		for(int i=0;i<rated_mc.getMovie().size();i++)
		{
			for(int j=i+1;j<rated_mc.getMovie().size();j++)
			{
				if(rated_mc.getMovie().get(i).getScore().intValue() < rated_mc.getMovie().get(j).getScore().intValue())
				{
					temp = rated_mc.getMovie().get(j);

					rated_mc.getMovie().set(j, rated_mc.getMovie().get(i));
					rated_mc.getMovie().set(i, temp);
				}
			}
		}

		for(int i=0;i < rated_mc.getMovie().size() && i < rankSize; i++)
		{
			final_mc.getMovie().add((rated_mc.getMovie().get(i)));
		}

		try {
			XMLHelper.marshal(final_mc, new File(directoryName+"/"+statsFileName+".xml"), true);
		} catch (JAXBException e){
			/*ignore*/
		} catch (IOException e1){
			/*ignore*/
		}
		
		return final_mc;
	}

	public static void main(String[]args)
	{
		//prepare output directory
		if (directoryName!=null) {
			if (!directoryName.isEmpty()) {
				File f = new File(directoryName);
				if (!f.exists()) f.mkdir();
			}
		}
		
		
		System.out.println("<Running StatsProducer>");
		StatsProducer statsp;
		try
		{
			statsp = new StatsProducer();
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
		statsp.shutdown();
	}

	

}