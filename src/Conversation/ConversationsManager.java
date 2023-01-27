package Conversation;


import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Conversation.Exceptions.ConversationNotFound;
import Message.Message;
import Message.MessageType;
import Network.NetworkManager;

public class ConversationsManager {
	private static ConversationsManager instance;
	private static List<Conversation> conversations;
	
	private ConversationsManager() {
		ConversationsManager.conversations = new ArrayList<>();
	}
	

	public static ConversationsManager getInstance()
	{
		if (ConversationsManager.instance == null)
		{
			ConversationsManager.instance = new ConversationsManager();
		}
		return ConversationsManager.instance;
	}
	
	
	public synchronized boolean creerConversation(InetAddress ipDistant, int portLocal) {
		Conversation tmpConv;
		System.out.println("[ CONVERSATION_MANAGER ] - CREATION CONVERSATION");
		boolean succes = true;
		try {
			System.out.println("[ CONVERSATION_MANAGER ] - list AVANT AJOUT : " + ConversationsManager.conversations);
			tmpConv = new Conversation(ipDistant, portLocal);
			ConversationsManager.conversations.add(tmpConv);
			System.out.println("[ CONVERSATION_MANAGER ] - list APRES AJOUT : " + ConversationsManager.conversations);
		} catch (IOException e) {
			succes = false;
			System.out.println("[creerConversation] : port " + portLocal + " non libre !");
		}
		// renverra false si echec, et on relancera dans network manager avec nouveau port
		return succes;
	}
	
	public synchronized void setPortDistantConversation(InetAddress ipDistant, int portDistant) throws ConversationNotFound {
		boolean trouve = false;
		System.out.println("[ CONVERSATION_MANAGER ] - list : " + ConversationsManager.conversations);
		for (Iterator<Conversation> iterator = ConversationsManager.conversations.iterator(); iterator.hasNext();) {
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
	
	public synchronized void lancerConversation(InetAddress ipDistant) throws ConversationNotFound {
		boolean trouve = false;
		for (Iterator<Conversation> iterator = ConversationsManager.conversations.iterator(); iterator.hasNext();) {
			Conversation conversation = (Conversation) iterator.next();
			
			if(conversation.getIpDistant().equals(ipDistant)) {
				Thread threadConv = new Thread(conversation);
				threadConv.start();
				trouve = true;
				System.out.println("[ConversationsManager] - lancerConversation : conversation lancee avec " + ipDistant.getHostName());
				break;
			}
			
		}
		if(!trouve) {
			throw new ConversationNotFound();
		}
	}
	
	public boolean isConversationExist(InetAddress ipDistant) {
		boolean trouve = false;
		for (Iterator<Conversation> iterator = ConversationsManager.conversations.iterator(); iterator.hasNext();) {
			Conversation conversation = (Conversation) iterator.next();
			
			if(conversation.getIpDistant().equals(ipDistant)) {
				trouve = true;
				break;
			}
			
		}
		return trouve;
	}
	
	public boolean isConversationOpen(InetAddress ipDistant) throws ConversationNotFound {
		boolean trouve = false;
		boolean result = false;
		for (Iterator<Conversation> iterator = ConversationsManager.conversations.iterator(); iterator.hasNext();) {
			Conversation conversation = (Conversation) iterator.next();
			
			if(conversation.getIpDistant().equals(ipDistant)) {
				trouve = true;
				result = conversation.isConversationOpen();
				break;
			}
			
		}
		if(!trouve) {
			throw new ConversationNotFound();
		}
		return result;
	}

	
	public void send(InetAddress ipDistant, String message) throws ConversationNotFound {
		System.out.println("[ConversationsManager] - send : SENDING...");
		boolean trouve = false;
		int timeout_ms = 5000;
		long start = System.currentTimeMillis();
		long elapsedTime = System.currentTimeMillis() - start;
		while(!trouve && elapsedTime < timeout_ms) {
			for (Iterator<Conversation> iterator = ConversationsManager.conversations.iterator(); iterator.hasNext();) {
				Conversation conversation = (Conversation) iterator.next();
				
				if(conversation.getIpDistant().equals(ipDistant)) {
					if(conversation.isConversationOpen()) {
						conversation.send(new Message(message,MessageType.COMMUNICATION));
						trouve = true;
						System.out.println("[ConversationsManager] - send : conv trouvee : " + ipDistant.getHostAddress() + " et " + conversation.getIpDistant());
						break;
					}
				}
				else {
					//System.out.println("[ConversationsManager] - send : conv trouvee mais mauvaise car ip suivants differents : " + ipDistant.getHostAddress() + " et " + conversation.getIpDistant());
				}
			}
			elapsedTime = System.currentTimeMillis() - start;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!trouve) {
			if(elapsedTime >= timeout_ms) {
				System.out.println("[ConversationsManager ] - send : " + ConversationsManager.conversations);
				System.out.println("[ConversationsManager] - send : TimeOut ! ");
			}
			throw new ConversationNotFound();
		}
	}
	
}