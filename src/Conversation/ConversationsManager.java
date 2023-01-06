package Conversation;


import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Conversation.Exceptions.ConversationNotFound;
import Message.Message;
import Message.MessageType;

public class ConversationsManager {
	private static ConversationsManager instance;
	private static List<Conversation> conversations;
	
	private ConversationsManager() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>><CREATION CONVERSATION MANAGER");
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
	
	
	public boolean creerConversation(InetAddress ipDistant, int portLocal) {
		Conversation tmpConv;
		System.out.println("[ CONVERSATION_MANAGER ] - CREATION CONVERSATION");
		boolean succes = true;
		try {
			tmpConv = new Conversation(ipDistant, portLocal);
			ConversationsManager.conversations.add(tmpConv);
			System.out.println("[ CONVERSATION_MANAGER ] - list : " + ConversationsManager.conversations);
		} catch (IOException e) {
			succes = false;
			System.out.println("[creerConversation] : port " + portLocal + " non libre !");
		}
		// renverra false si echec, et on relancera dans network manager avec nouveau port
		return succes;
	}
	
	public void setPortDistantConversation(InetAddress ipDistant, int portDistant) throws ConversationNotFound {
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
	
	public void lancerConversation(InetAddress ipDistant) throws ConversationNotFound {
		boolean trouve = false;
		for (Iterator<Conversation> iterator = ConversationsManager.conversations.iterator(); iterator.hasNext();) {
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
		boolean trouve = false;
		for (Iterator<Conversation> iterator = ConversationsManager.conversations.iterator(); iterator.hasNext();) {
			Conversation conversation = (Conversation) iterator.next();
			
			if(conversation.getIpDistant().equals(ipDistant)) {
				conversation.send(new Message(message,MessageType.COMMUNICATION));
				trouve = true;
				break;
			}
			
		}
		if(!trouve) {
			throw new ConversationNotFound();
		}
	}
	
}
