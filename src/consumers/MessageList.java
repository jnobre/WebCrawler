package consumers;

import java.util.ArrayList;
import java.util.Observable;

public class MessageList extends Observable{
	private ArrayList<String> messageList;
	private Object lock;
	
	public MessageList() {
		this.messageList = new ArrayList<String>();
		this.lock = new Object();
	}
	
	/**
	 * Adds a new String Message to the list.
	 * This method is thread safe with every other operation on the list.
	 * 
	 * @param message - String
	 */
	public void addMessage(String message) {
		synchronized (lock) {
			messageList.add(message);
		}
		this.setChanged();
		this.notifyObservers();
	}
	
	/**
	 * Removes the older {@link String} Message from the list.
	 * This method is thread safe with every other operation on the list.
	 * 
	 * @return the message in the list, or <b><code>null</code></b> if the list is empty
	 */
	public String popMessage() {
		synchronized (lock) {
			if (messageList.isEmpty())
				return null;
			else
				return messageList.remove(0);
		}
	}
	
	public ArrayList<String> removeAllMessages() {
		synchronized (lock) {
			return new ArrayList<String>(messageList);
		}
	}
	
	

}
