import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConversationsManager {
	private static ConversationsManager instance;
	private List<Conversation> conversations;
	
	private ConversationsManager() {
		this.conversations = new ArrayList<>();
	}
	

	public static ConversationsManager getInstance()
	{
		if (ConversationsManager.instance == null)
		{
			return new ConversationsManager();
		}
		else
		{
			return ConversationsManager.instance;
		}
	}
	
	
	public boolean creerConversation(InetAddress ipDistant, int portLocal) {
		Conversation tmpConv;
		boolean succes = true;
		try {
			tmpConv = new Conversation(ipDistant, portLocal);
			this.conversations.add(tmpConv);
		} catch (IOException e) {
			succes = false;
			System.out.println("[creerConversation] : port " + portLocal + " non libre !");
		}
		// renverra false si echec, et on relancera dans network manager avec nouveau port
		return succes;
	}
	
	public void setPortDistantConversation(InetAddress ipDistant, int portDistant) throws ConversationNotFound {
		boolean trouve = false;
		for (Iterator<Conversation> iterator = conversations.iterator(); iterator.hasNext();) {
			Conversation conversation = (Conversation) iterator.next();
			
			if(conversation.getIpDistant().equals(ipDistant)) {
				conversation.setPortDistant(portDistant);
				trouve = true;
				break;
			}
			
		}
		if(!trouve) {
			throw new ConversationNotFound();
		}
	}
	
	public void lancerConversation(InetAddress ipDistant) throws ConversationNotFound {
		boolean trouve = false;
		for (Iterator<Conversation> iterator = conversations.iterator(); iterator.hasNext();) {
			Conversation conversation = (Conversation) iterator.next();
			
			if(conversation.getIpDistant().equals(ipDistant)) {
				Thread threadConv = new Thread(conversation);
				threadConv.start();
				trouve = true;
				break;
			}
			
		}
		if(!trouve) {
			throw new ConversationNotFound();
		}
	}
	

	
	public void sendSomething(InetAddress ipDistant) throws ConversationNotFound {
		boolean trouve = false;
		for (Iterator<Conversation> iterator = conversations.iterator(); iterator.hasNext();) {
			Conversation conversation = (Conversation) iterator.next();
			
			if(conversation.getIpDistant().equals(ipDistant)) {
				conversation.send(new Message("Coucou bro !",MessageType.COMMUNICATION));
				trouve = true;
				break;
			}
			
		}
		if(!trouve) {
			throw new ConversationNotFound();
		}
	}
	
}
