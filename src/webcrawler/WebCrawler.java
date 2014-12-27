package webcrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import util.XMLHelper;

public class WebCrawler extends Thread implements Observer {
	
	public static final String crawlerTempDatabase = "tempDatabase.crawler";
	public static final String jobListBackupFile = "job_list_backup.crawler";
	public static final String xsdFile = "movie_database.xsd";
	private JobList jobList;
	private Object lock;
	private Boolean toShutdown;
	private TopicMessageSender tms;
	private Boolean offTopicMode;
	
	public WebCrawler(JobList jobList) {
		
		toShutdown = new Boolean(false);
		lock = new Object();
		this.jobList = jobList;
		tms = new TopicMessageSender();
		this.offTopicMode = false;
	
	}
	
	@Override
	public void run() {

		System.out.println("The crawler <"+this.getName()+"> has started...");
		
		System.out.println("Checking connection to the Topic Server");
		
		try {
			
			tms.openConnection();
		
			// if successful process previous sessions unsent messages
			System.out.println("Processing unsent messages from previous sessions...");
			if (processUnsentMessages() == false) {
				
				// Activate offTopicMode
				this.enterOffTopicMode();
			}
			
		} catch (NamingException | JMSException e1) {
			
			e1.printStackTrace();
			// Activate offTopicMode
			this.enterOffTopicMode();
		}
		
		
		System.out.println("Processing Jobs...");
		String job;
		while (!isSetToShutdown()) {
			
			if ((job = getNextJob(this.jobList)) == null) {
				if (this.isOffTopic()) {
					System.out.println("No more jobs... terminating...");
					setToShutdown();
				} else {
					System.out.println("No jobs... waiting...");
					waitFor(this.jobList);
				}
				continue;
			}
			
			System.out.println("Fetching <"+job+">");
			IMDBMovieCatalog movies = new IMDBMovieCatalog();
			
			try {
				movies.fetchImdbCatalog(job);
			} catch (IOException e1) {
				System.out.println("Could not process the given URL");
				continue;
			}
			System.out.println("Teste04");
			String xml_message = new String();
			try {
				xml_message = XMLHelper.marshal(movies.movCatalog, WebCrawler.xsdFile, false);
			} catch (JAXBException | IOException | SAXException e) {
				System.out.println("The xml did not pass the verification... droped!");
				continue;
			}
			
			if (this.isOffTopic()) {
				try {
					appendLineToFile(xml_message, crawlerTempDatabase);
				} catch (IOException e) {
					/*ignore*/
				}
			} else {
				
				System.out.println("Sending XML to topic... ");
				if (sendXMLMessage(xml_message) == true)
					System.out.println("XML Sent with success!");
				else {
					System.out.println("Unable to send to topic... Saving the message");
					try {
						appendLineToFile(xml_message, crawlerTempDatabase);
					} catch (IOException e) {
						/*ignore*/
					}
					// Activate offTopicMode
					this.enterOffTopicMode();
				}
				
			}
		}
		
		if(jobList.size() > 0)
			saveRemainingJobs(jobList);
		
		// Close the topic server connection if still open
		if (!this.isOffTopic()) {
			try {
				tms.closeConnection();
			} catch (JMSException e) {
				// ignore and continue the shutdown
			}
		}
		
		System.out.println("Crawler <"+this.getName()+"> says: Bye Bye");
	}
	
	@Override
	public void update(Observable o, Object arg) {
		synchronized (lock) {
			lock.notify();
		}		
	}
	
	public void setToShutdown() {
		synchronized (lock) {
			toShutdown = true;
			lock.notify();
		}
	}
	
	public boolean isSetToShutdown() {
		synchronized (lock) {
			return toShutdown;
		}
	}
	
	private void appendLineToFile(String line, String fileName) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName,true)));
		out.println(line);
		out.close();
	}
	
	private void saveRemainingJobs(JobList jobList)
	{
		String job;
		try 
		{
			System.out.println("Saving Remaining Jobs");
		
			while((job = jobList.removeJob()) != null)
			{
				appendLineToFile(job,jobListBackupFile);
			}
			
		} 
		catch (IOException e1) 
		{ /*ignore*/ }
	}
	
	private boolean processUnsentMessages() 
	{	
		
		ArrayList<String> savedCrawling = new ArrayList<String>();
		
		try {
			
			String line = null;
			File file = new File(crawlerTempDatabase);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null) {
				savedCrawling.add(line);
			}
			
			reader.close();
			file.delete();
			
			file.createNewFile();
			
			boolean cancelSending = false;
			for (String s : savedCrawling) {
				if (!cancelSending) {
					if (sendXMLMessage(s) == false) {
						cancelSending = true;
					}
				} else {
					appendLineToFile(s, crawlerTempDatabase);
				}
			}
			if (cancelSending) return false;
			
		} catch (FileNotFoundException e) {
			// there is no log
			return true;
		} catch (IOException e) {
			System.out.println(":erro: processamento do ficheiro " + crawlerTempDatabase);
		}
		return true;
	}
	
	/**
	 * Retrieves a job from the given {@link JobList}.
	 * @param jobList - JobList from where to remove the job.
	 * @return String with the job or null if no job is present
	 */
	private String getNextJob(JobList jobList) {
		
		String job = jobList.removeJob();
		
		if (job != null)
			return job;
		else
			return null;
	}
	
	/**
	 * Waits for changes in the given {@link Observable} Object. 
	 * Upon notification from the Object the method {@link WebCrawler#update(Observable, Object)}
	 *  is called waking this crawler.
	 * @param subject - Observable Object to wait for.
	 */
	private void waitFor(Observable subject) {
		subject.addObserver(this);
		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
			//Graceful termination
			this.setToShutdown();
		}
		subject.deleteObserver(this);
	}

	private boolean sendXMLMessage(String xml_message) {
		int retry_count = 0, max_retries = 2;
		long retry_interval = 1000;
		
		try {
			tms.sendMessage(xml_message);
			return true;
		} catch (JMSException e) {
			// unable to send the message... try again more 2 times
			try {
				System.out.print("Unable to send to topic... Trying again in "+retry_interval/1000+" second...");
				Thread.sleep(retry_interval);
			} catch (InterruptedException e1) { /*ignore*/ }
			
			do {
				System.out.println("Trying again...");
				retry_count++;
				
				try {
					tms.openConnection();
					try {
						tms.sendMessage(xml_message);
						return true;
					} catch (JMSException e2) {
						// unable to send the message...
					}
				} catch (NamingException | JMSException e3) {
					// unable to establish connection...
				}
				
				System.out.print("Unable to send... Trying again in "+retry_interval/1000+" second...");
				try {
					Thread.sleep(retry_interval);
				} catch (InterruptedException e1) { /*ignore*/ }
				
			} while (retry_count < max_retries);
		}
		
		
		return false;
	}
	
	public boolean isOffTopic() {
		return this.offTopicMode;
	}
	
	
	private void enterOffTopicMode() {
		System.out.println("Entering off-Topic mode...");
		this.offTopicMode = true;
	}

	public static void main(String [] args){
		
		System.out.println(":Enter 'exit' to shutdown, or 'start' to execute crawlers");
		
		
		JobList jobList = new JobList();
		try {
			jobList.addJobBatch(jobListBackupFile);
		} catch (IOException e) { /*ignore*/ }	
	
		jobList.addJob("http://www.imdb.com/movies-coming-soon/2014-01/");
		jobList.addJob("http://www.imdb.com/movies-coming-soon/2014-02/");
		jobList.addJob("http://www.imdb.com/movies-coming-soon/2014-03/");
		jobList.addJob("http://www.imdb.com/movies-coming-soon/2014-04/");
		
		ArrayList<WebCrawler> crawlerList = startCrawlers(1, jobList);
		
		Scanner sc = new Scanner(System.in);
		String line;
		do {
			
			line = sc.nextLine();
			if (line.equals("exit")) {
				break;
			
			} else if (line.equals("start")) {
			
				if (crawlersAlive(crawlerList)) {
					System.out.println(":error:crawling still in progress");
				
				} else {
					System.out.println(":releasing new crabs...");
					crawlerList = startCrawlers(1, jobList);
					
				}
			} 
			else if(line.startsWith("file:"))
			{
				try {
					jobList.addJobBatch(line.substring(5));
				} catch (IOException e) {
					System.out.println(":error:file not found");
				}
			
			} else if(line.startsWith("job:")) {
				jobList.addJob(line.substring(4));
			}
			else
			{
				System.out.println(":error:invalid command");
			}
			

			
		} while (true);
		
		sc.close();
		System.out.println("Terminating... Waiting for the crawler...");
		
		for (WebCrawler webCrawler : crawlerList) {
		
			webCrawler.setToShutdown();// terminate crawler
	
			try {
				webCrawler.join();
			} catch (InterruptedException e) {
				/*ignore*/
			}
			
		}
		
		System.out.println("Bye Bye");
		
	}
	
	public static boolean crawlersAlive(ArrayList<WebCrawler> crawlerList)
	{
		
		for (WebCrawler webCrawler : crawlerList) {
			if(webCrawler.isAlive())
				return true;
		}
		return false;

	}
	
	public static ArrayList<WebCrawler> startCrawlers(int maxCrawlers, JobList jobList)
	{
		
		ArrayList<WebCrawler> crawlerList = new ArrayList<WebCrawler>();
		for(int i=0;i<maxCrawlers;i++)
		{
			crawlerList.add(new WebCrawler(jobList));
			crawlerList.get(i).setName("crawler"+i);
			crawlerList.get(i).start();
		}
		
		return crawlerList;
	}
	
	
}
