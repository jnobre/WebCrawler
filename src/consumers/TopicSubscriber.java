package consumers;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TopicSubscriber implements MessageListener {
	private ConnectionFactory cf;
	private Connection c;
	private Session s;
	private Topic t;
	private MessageConsumer mc;
	private MessageList desList;
	
	public TopicSubscriber(MessageList destination, String clientID) throws NamingException, JMSException {
		Properties environment = new Properties();
		environment.put(Context.SECURITY_PRINCIPAL, "isuser");
		environment.put(Context.SECURITY_CREDENTIALS, "is");
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		environment.put(Context.PROVIDER_URL, "remote://localhost:4447");
		InitialContext iCtx = new InitialContext(environment);
		this.cf = (ConnectionFactory) iCtx.lookup("jms/RemoteConnectionFactory");
		this.t =  (Topic) iCtx.lookup("jms/topic/movieCatalog");
		this.c =  this.cf.createConnection("isuser", "is");
		this.c.setClientID(clientID);
		this.c.start();
		this.s = this.c.createSession(false, Session.AUTO_ACKNOWLEDGE);
		this.mc = this.s.createDurableSubscriber( this.t, "");
		
		this.desList = destination;
		this.mc.setMessageListener(this);
	}
	
	@Override public void onMessage(Message msg) {
		TextMessage tmsg = (TextMessage) msg;
		try {
			System.out.println("> Received message "+tmsg.getJMSMessageID());
			this.desList.addMessage(tmsg.getText());
		} catch (JMSException e) {
			System.out.println("> Got a bad message");
			return;
		}
	}
	
	public void close() throws JMSException{
		this.c.close();
	}

}


