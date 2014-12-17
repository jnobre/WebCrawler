package webcrawler;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TopicMessageSender {
	
	private Properties environment;
	private String connectionFactoryLookupAddress;
	private String destinationLookupAddress;
	private ConnectionFactory cf;
	private Connection c;
	private Session s;
	private Destination t;
	private MessageProducer mp;
	private static final String PROVIDER_URL = "http-remoting://127.0.0.1:8081";

	  
	public TopicMessageSender() 
	{

		connectionFactoryLookupAddress = new String("jms/RemoteConnectionFactory");
		destinationLookupAddress = new String("jms/topic/movieCatalog");
		environment = new Properties();
		environment.put(Context.SECURITY_PRINCIPAL, "testuser");
		environment.put(Context.SECURITY_CREDENTIALS, "is");
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		environment.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
        
	}
	
	public void openConnection() throws NamingException, JMSException 
	{
		int ola;
		
		System.out.println("Entra....");
		InitialContext iCtx = new InitialContext(environment);
		System.out.println("Sai....");
		
		this.cf = (ConnectionFactory) iCtx.lookup(connectionFactoryLookupAddress);
		this.t = (Destination) iCtx.lookup(destinationLookupAddress);
		this.c = this.cf.createConnection("testuser",
										   "is");
		this.c.start();
		this.s = this.c.createSession(false, Session.AUTO_ACKNOWLEDGE);
		this.mp = this.s.createProducer(this.t);
		System.out.println("Sai do metodo openconnection!!!  ");
		
	}
	
	public void sendMessage(String string) throws JMSException {
		TextMessage tm = this.s.createTextMessage(string);
		this.mp.send(tm);
	}

	public void closeConnection() throws JMSException {
		this.c.close();
	}

}
