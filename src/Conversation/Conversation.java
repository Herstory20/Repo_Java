package Conversation;


import java.io.IOException;
import java.net.InetAddress;

import Message.Message;
import Network.TCP.TCP_Receiver;
import Network.TCP.TCP_Sender;

public class Conversation implements Runnable{
	private TCP_Sender tcp_send_thread;
	private TCP_Receiver tcp_receive_thread;
	private InetAddress ipDistant;
	private int portLocal;
	private int portDistant;
	private boolean isOpen;
	
	// lève IOException si le port n'est pas disponible
	public Conversation(InetAddress ipDistant, int portLocal) throws IOException {
		this.ipDistant = ipDistant;
		this.portLocal = portLocal;
		this.isOpen = false;

		this.tcp_receive_thread = new TCP_Receiver(this.portLocal);
		Thread t_tcprcv = new Thread(tcp_receive_thread);
		t_tcprcv.start();
	}

	
	public void setPortDistant(int portDistant) {
		this.portDistant = portDistant;
		try {
			this.initSendThread();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public InetAddress getIpDistant() {
		return ipDistant;
	}


	public void initSendThread() throws IOException {
		this.tcp_send_thread = new TCP_Sender(this.ipDistant, this.portDistant);
		Thread t_tcpsend = new Thread(this.tcp_send_thread);
		t_tcpsend.start();
		this.isOpen = true;	// conversation ouverte ssi on a initialisé le thread de réception (constructeur) & envoi sans erreur
	}
	
	public void send(Message message) {
		this.tcp_send_thread.setMessage(message);
	}
	
	public boolean isConversationOpen() {
		return this.isOpen;
	}


	@Override
	public void run() {
		System.out.println("[Conversation] : running");
		Message recu;
		while(true) {
			
			recu = this.tcp_receive_thread.getMessage();
			if(recu != null){
				// ajouter message à la DB
				// ajouter message au listener de l'interface
				System.out.println("[Conversation] : Message reçu de la part de " + ipDistant.getHostName() + " : \n>> " + recu.getContenu());
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
