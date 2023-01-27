package Network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import BDD.JDBC;
import Conversation.ConversationsManager;
import Conversation.Exceptions.ConversationNotFound;
import IHM.Home;
import Message.Message;
import Message.MessageType;
import Network.Exceptions.InvalidConnexionMessageException;
import Network.Exceptions.InvalidIpException;
import Network.Exceptions.InvalidPseudoException;
import Network.Exceptions.MyOwnIpException;
import Network.TCP.TCP_Receiver;
import Network.TCP.TCP_Sender;
import Network.UDP.UDP_Receiver;
import Network.UDP.UDP_Sender;
import Message.Exceptions.InvalidMessageFormatException;

import java.net.NetworkInterface;
import java.net.ServerSocket;

public class NetworkManager implements Runnable{
	
	// threads udp pour connexion
	private UDP_Sender udp_send_thread;
	private UDP_Receiver udp_receive_thread;
	
	// threads tcp pour ouverture de discussion & négociation de port
	private TCP_Sender tcp_send_thread;
	private TCP_Receiver tcp_receive_thread;
	
	private ConversationsManager convManager;
	
	private InetAddress monIp;
	private InetAddress tcp_ipDistant;
	private static final int UDP_PORT = 1236;
	private static final int TCP_PORT = 1237;
	private int futurPortTcpLocal, futurPortTcpDistant;
	private static final int CONNEXION_DELAI_ATTENTE_REPONSE_MS = 1000;
	private String pseudo;
	private Hashtable<String, String> coordonneesUtilisateur;
	private static NetworkManager instance;
	private static boolean connected;
	private static boolean running;
	private static boolean tcpRcvOn;
	private Thread myThread;
	private JDBC db;
	private boolean nouveauPseudoOK;
	
	public static NetworkManager getInstance() throws IOException
	{
		if (NetworkManager.instance == null)
		{
			NetworkManager.instance = new NetworkManager();
		}
		return NetworkManager.instance;
	}
	
	private NetworkManager() throws IOException {
		this.pseudo = "";
		NetworkManager.connected = false;
		NetworkManager.running = true;
		NetworkManager.tcpRcvOn = false;
		this.tcp_ipDistant = null;
		this.futurPortTcpLocal = 0;
		this.futurPortTcpDistant = 0;
		this.coordonneesUtilisateur = new Hashtable<String, String>();
		this.setIPAddress();
		this.convManager = ConversationsManager.getInstance();
		
		// UDP thread config
		this.udp_send_thread = UDP_Sender.getInstance(null, NetworkManager.UDP_PORT);
		Thread t_udpsend = new Thread(this.udp_send_thread);
		this.udp_receive_thread = UDP_Receiver.getInstance(NetworkManager.UDP_PORT);
		Thread t_udprcv = new Thread(this.udp_receive_thread);
		
		this.db = JDBC.getInstance();
		
		t_udpsend.start();
		t_udprcv.start();

		this.myThread = new Thread(this);
		this.myThread.start();
	}

	
	public void stop()
	{
		NetworkManager.running = false;
	}

	public void connexion(String pseudo) throws IOException, InvalidPseudoException {
		NetworkManager.connected = true;
		// envoi des informations de connexion
		String message = "Bonjour;" + monIp.getHostAddress() + ";" + pseudo;
		this.udp_send_thread.setBroadcastEnabled();
		this.udp_send_thread.setMessage(new Message(message, MessageType.CONNECTIVITE));
		
		// attente des réponses des autres utilisateurs
		String recu = "";
		
		long start = System.currentTimeMillis();
		long elapsedTime = System.currentTimeMillis() - start;
		
		System.out.println("[NETWORK MANAGER] - connexion : Attente réponse autres utilisateurs...");
		while(elapsedTime < NetworkManager.CONNEXION_DELAI_ATTENTE_REPONSE_MS) {

			recu = this.recevoirUDP();
			if(!recu.isEmpty()) {
				if (this.isConnectivite(recu)) {
					String[] coord;	// contiendra IP / PSEUDO / OK ou KO
					try {
						coord = this.getCoordonneesFromReponseConnexion(recu);
						if(coord[2] == "OK")
						{
							this.coordonneesUtilisateur.put(coord[0], coord[1]);
						}
						else {
							System.out.println("Coordonnees recues erronnees");
							NetworkManager.connected = false;
						}
					} catch (InvalidMessageFormatException | InvalidIpException e) {
						e.printStackTrace();
					} catch (MyOwnIpException e) {
						System.out.println("[NETWORK MANAGER] - connexion : Réception de notre propre paquet ignorée");
					}
				}
			}
			elapsedTime = System.currentTimeMillis() - start;
		}
		
		System.out.println("[NETWORK MANAGER] - connexion : Coordonnees recues : \n" + this.coordonneesUtilisateur);
		
		if(NetworkManager.connected) {
			System.out.println("[NETWORK MANAGER] - connexion : Mise à jour de l'Annuaire...");

			System.out.println("avant modif ...");
			this.db.refreshA();
			this.db.selectAllA();
			// on met à jour l'Annuaire avec les nouveaux IP et Pseudo
			Set<String> setOfKeys = this.coordonneesUtilisateur.keySet();
			for (String key : setOfKeys) {
				String ipTmp = key;
				String pseudoTmp = this.coordonneesUtilisateur.get(key);
				this.db.insertAwithoutP(ipTmp, pseudoTmp);
			}
			System.out.println("après modif ...");
			this.db.selectAllA();
			this.pseudo = pseudo;
			this.initTCPRcvThread();
		}
		else {
			System.out.println("[NETWORK MANAGER] - connexion : Echec de la mise à jour de l'Annuaire, pseudo invalidé par les autres utilisateurs");
			throw new InvalidPseudoException(pseudo);
		}
		
		System.out.println("[NETWORK MANAGER] - connexion : Fin connexion");
	}
	

	@Override
	public void run() {
		System.out.println("[NETWORK MANAGER] - RUN : running");
		while(NetworkManager.running)
		{
			this.update();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		System.out.println("[NetworkManager] : end of run");
	}
		
	
	public void update() {
		if(NetworkManager.tcpRcvOn) {
			String recuUdp = this.recevoirUDP();

			if(!recuUdp.isEmpty()) {
				System.out.println("[NETWORK MANAGER] - update : MESSAGE UDP " + recuUdp + " RECU ! Réponse en cours...");
				if(this.isConnectivite(recuUdp)) {
					if(this.isDeconnexionMessage(recuUdp)) {
						this.traiterDeconnexion(recuUdp);
					}
					else {
						this.repondreTentativeConnexionUDP(recuUdp);
					}
				}
				else if (this.isChangementPseudo(recuUdp)) {
					this.repondreTentativeChangementPseudo(recuUdp);
				}
			}
		
			Message recuTcp = this.recevoirTCP();
			// /!\ vérifier qu'on reçoive bien un paquet du meme ip pendant une co !
			// + vérifier que l'ip est dans le dictionnaire ! = sécu
			if(recuTcp != null) {
				System.out.println("[NETWORK MANAGER] - update : MESSAGE TCP " + recuTcp + " RECU ! Réponse en cours...");
				this.negociationDePorts(recuTcp);
			}
		}
	}
	
	public void newDiscussion(InetAddress ipToReach) {
		this.tcp_ipDistant = ipToReach;
		try {
			this.initTCPSendThread();
			this.tcp_send_thread.setMessage(new Message("Discussion", MessageType.CONNECTIVITE));
			
		} catch (IOException e) {
			
		}
	}
	

	public boolean changePseudo(String nouveauPseudo) throws IOException {
		this.nouveauPseudoOK = true;
		String message = "Demande" + ";" + monIp.getHostAddress() + ";" + nouveauPseudo;
		//String message = "2;" + monIp.getHostAddress() + ";" + nouveauPseudo;
		this.udp_send_thread.setBroadcastEnabled();
		this.udp_send_thread.setMessage(new Message(message, MessageType.CHANGEMENT_PSEUDO));
		
		// attente des réponses des autres utilisateurs
		
		long start = System.currentTimeMillis();
		long elapsedTime = System.currentTimeMillis() - start;
		
		System.out.println("[NETWORK MANAGER] - changePseudo : Attente réponse autres utilisateurs...");
		while(elapsedTime < NetworkManager.CONNEXION_DELAI_ATTENTE_REPONSE_MS) {
			
			elapsedTime = System.currentTimeMillis() - start;
		}
				
		if(this.nouveauPseudoOK) {
			System.out.println("[NETWORK MANAGER] - changePseudo : Mise à jour du pseudo...");

			message = "DemandeOK" + ";" + monIp.getHostAddress() + ";" + nouveauPseudo;
			this.udp_send_thread.setBroadcastEnabled();
			this.udp_send_thread.setMessage(new Message(message, MessageType.CHANGEMENT_PSEUDO));
			
			this.pseudo = nouveauPseudo;
		}
		else {
			System.out.println("[NETWORK MANAGER] - changePseudo : Echec de la mise à jour du pseudo, pseudo invalidé par les autres utilisateurs");
		}
		
		System.out.println("[NETWORK MANAGER] - changePseudo : Fin");
		return this.nouveauPseudoOK;
	}
	


	public void deconnexion() {
		String message = "Deconnexion" + ";" + monIp.getHostAddress();
		this.udp_send_thread.setBroadcastEnabled();
		this.udp_send_thread.setMessage(new Message(message, MessageType.CONNECTIVITE));
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("[NETWORK MANAGER] - deconnexion : Terminee");
	}
	
	
	
	
	
	/** Méthodes privées  **/
	
	
	private void initTCPRcvThread() throws IOException {
		// TCP thread config
		try {
			this.tcp_receive_thread = new TCP_Receiver(NetworkManager.TCP_PORT);
			Thread t_tcprcv = new Thread(tcp_receive_thread);
			t_tcprcv.start();
			NetworkManager.tcpRcvOn = true;
		} catch (IOException e) {
			System.out.println("Connexion sur le port " + NetworkManager.TCP_PORT + " impossible");
			throw new IOException();
		}
	}
	

	private void initTCPSendThread() throws IOException{
		// TCP thread config
		try {
			System.out.println("[initTCPSendThread] - creation du thread tcp");
			this.tcp_send_thread = new TCP_Sender(this.tcp_ipDistant, NetworkManager.TCP_PORT);
			Thread t_tcpsend = new Thread(this.tcp_send_thread);
			t_tcpsend.start();
		} catch (IOException e) {
			System.out.println("Connexion avec " + this.tcp_ipDistant + " sur le port " + NetworkManager.TCP_PORT + " impossible");
			throw new IOException();
		}
	}
	
	
	
	private void repondreTentativeConnexionUDP(String recu) {
		System.out.println("[repondreTentativeConnexionUDP] : Réponse en cours à [ " + recu + " ]");
		if (this.isConnectivite(recu)) {
			String[] coord = null ;
			try {
				coord = getCoordonneesFromDemandeConnexion(recu);
				
				// on envoit OK si tout est bon avec nos coordonnées / KO sinon
				String message = coord[2] + ";" + monIp.getHostAddress() + ";" + pseudo;
				
				try {
					this.udp_send_thread.setIp(InetAddress.getByName(coord[0]));
					this.udp_send_thread.setMessage(new Message(message, MessageType.CONNECTIVITE));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				
				// On stocke son IP et son PSEUDO dans l'Annuaire
				if(coord[2].equals("OK")) {
					System.out.println("MAJ de la table Annuaire (ajout du pseudo)...");
					String ipTmp = coord[0];
					String pseudoTmp = coord[1];
					this.db.insertAwithoutP(ipTmp, pseudoTmp);
					Home.updateUsersList();// faire un getinstance !! no need for static
				}
			} catch (InvalidMessageFormatException | InvalidIpException | InvalidConnexionMessageException e) {
				e.printStackTrace();
			} catch (MyOwnIpException e) {
				// si c'est notre IP, on ignore le paquet
				System.out.println("[repondreTentativeConnexionUDP] : Propre paquet reçu");
			}
		}
	}

	private void repondreTentativeChangementPseudo(String recu) {
		System.out.println("[repondreTentativeChangementPseudo] : Réponse en cours à [ " + recu + " ]");
		if (this.isChangementPseudo(recu)) {
			String[] coord = null ;
				try {
					coord = getInfosFromDemandeChangementPseudo(recu);
					String typeMessage = coord[0];
					String ipSender = coord[1];
					
					if(typeMessage.equals("Demande")) {
						String nouveauPseudo = coord[2];
						
						// on envoit OK si tout est bon avec notre pseudo / KO sinon
						String reponse;
						if(!nouveauPseudo.equals(this.pseudo)) {
							reponse = "OK";
						}
						else {
							reponse = "KO";
						}
						String message = "Reponse" + ";" + nouveauPseudo + ";" + reponse;

						try {
							this.udp_send_thread.setIp(InetAddress.getByName(ipSender));
							this.udp_send_thread.setMessage(new Message(message, MessageType.CHANGEMENT_PSEUDO));
						} catch (UnknownHostException e) {
							e.printStackTrace();
						}
						
					}
					else if(typeMessage.equals("DemandeOK")) {
						String nouveauPseudo = coord[2];
						JDBC.getInstance().updateLogin(nouveauPseudo, ipSender);
						Home.updateUsersList();
						System.out.println("[repondreTentativeConnexionUDP] : Changement de pseudo effectif pour " + ipSender + " => " + nouveauPseudo);
					}
					else if (typeMessage.equals("Reponse")) {
						String reponse = coord[2];
						if(reponse.equals("OK")) {
							System.out.println("[repondreTentativeConnexionUDP] : Pseudo Validé");
						}
						else {
							this.nouveauPseudoOK = false;
							System.out.println("[repondreTentativeConnexionUDP] : Pseudo invalidé !");
						}
					}
				
				} catch (InvalidMessageFormatException | InvalidIpException | InvalidPseudoException e) {
					e.printStackTrace();
				} catch (MyOwnIpException e) {
					// si c'est notre IP, on ignore le paquet
					System.out.println("[repondreTentativeConnexionUDP] : Propre paquet reçu");
				}
		}
	}
	
	
	public InetAddress getTcp_ipDistant() {
		return tcp_ipDistant;
	}

	// fonction qui s'applique :
	//		- au serveur (celui à qui on demande une connexion)
	//		- au client  (celui qui demande la connexion)
	private void negociationDePorts(Message recu) {
		System.out.println("[negociationDePorts] : Réponse en cours à [ " + recu + " ]");
		// si on a recu "Discussion", c'est une premiere demande de discussion
		//	=> on répond par un de notre port qui est libre
		if(recu.getContenu().equals("Discussion") && this.futurPortTcpLocal == 0) {
			try {
				this.mettreAJourTcpIpDistant();
				this.envoyerPortLibre();
			} catch (IOException e) {
				this.annulationNegociationPorts();
				System.out.println("Annulation de la négiciation de ports");
			}
		}
		// sinon, si on reçoit "DiscussionOK", c'est que :
		//		- nous sommes client, auquel cas le serveur nous a dit ok suite à "Discussion"
		//		  et nous a envoyé son port libre ; il attend le notre
		//		- nous sommes serveur, et nous reçevons la réponse dont nous parlions ci-dessus
		//		  que le client nous envoie avec son port libre.
		// si les messages reçus ont un format et des informations valides, la connexion est 
		//	lancée via le threadManager (il ouvrira une connexion TCP spécifiquement pour 
		//	cette discussion)
		else if (recu.getContenu().contains("DiscussionOK")) {
			if(this.futurPortTcpLocal == 0) {
				this.envoyerPortLibre();
			}
			System.out.println("[negociationDePorts] : Connexion ok ! : \n\t>> " + recu);
			String messagesSepares[] = recu.getContenu().split(":");
			if(messagesSepares.length==2) {
				this.futurPortTcpDistant = Integer.parseInt(messagesSepares[1]);
				System.out.println("[negociationDePorts] : Port distant : " + this.futurPortTcpDistant);
				try {
					this.convManager.setPortDistantConversation(this.tcp_ipDistant, this.futurPortTcpDistant);
					this.tcp_receive_thread.fermerConnexion();
					this.tcp_send_thread.stop();
					
					// mise à jour du port dans la bdd concernant ce destinataire
					this.db.updateport(this.tcp_ipDistant.getHostAddress(), String.valueOf(this.futurPortTcpDistant));
					this.convManager.lancerConversation(this.tcp_ipDistant);
					System.out.println("[negociationDePorts] : La discussion peut commencer.");
				} catch (ConversationNotFound e) {
					this.annulationNegociationPorts();
					System.out.println("[negociationDePorts] : Erreur d'ajout de port distant, annulation de la négociation de ports");
				}
			}
			else {
				this.annulationNegociationPorts();
				System.out.println("[negociationDePorts] : recu incorrect, annulation negociation de ports");
			}
		}
	}
	
	private void mettreAJourTcpIpDistant() throws IOException {
		// dans le cas où on est serveur, c'est-à-dire où quelqu'un nous demande la connexion,
		//	on récupère son ip
		if(this.tcp_ipDistant == null) {
			this.tcp_ipDistant = this.tcp_receive_thread.getIpDest();
			try {
				this.initTCPSendThread();
			} catch (IOException e) {
				System.out.println("[mettreAJourTcpIpDistant] : Destinataire injoignable");
				throw e;
			}
		}
	}
	
	private void annulationNegociationPorts() {
		this.futurPortTcpLocal = 0;
		this.futurPortTcpLocal = 0;
		this.tcp_receive_thread.fermerConnexion();
		if(this.tcp_send_thread != null) {
			this.tcp_send_thread.stop();
		}
		// réinitialiser l'instance de Conversation
	}
	
	private void envoyerPortLibre() {
		boolean resReservationPort;
		do {
				
			this.futurPortTcpLocal = this.generateRandomPort();
			// on réserve le port directement avec ConversationManager
			resReservationPort = this.convManager.creerConversation(this.tcp_ipDistant, this.futurPortTcpLocal);
			
		}while(!resReservationPort);
		
		System.out.println("port libre : " + this.futurPortTcpLocal);
		
		// si tout s'est bien passé, on envoie DiscussionOK avec le port concerné.
		this.tcp_send_thread.setMessage(new Message("DiscussionOK:" + this.futurPortTcpLocal, MessageType.CONNECTIVITE));
	}
	
	@Override
	public void finalize() throws IOException
	{
		this.deconnexion();
		this.udp_send_thread.stop();
		this.udp_receive_thread.stop();
		if(this.tcp_send_thread != null) {
			this.tcp_send_thread.stop();
		}
		if(this.tcp_send_thread != null) {
			this.tcp_receive_thread.stop();
		}
		System.out.println("[Network Manager] : Fin network manager");
	}
	
	private String recevoirUDP() {
		String recu = "";
		recu = this.udp_receive_thread.getMessageString();
		if(!recu.isEmpty())
			System.out.println("[NETWORK MANAGER] - recevoirUDP : Message recu : [" + recu + "]");
		return recu;
	}
	
	private Message recevoirTCP() {
		Message recu;
		recu = this.tcp_receive_thread.getMessage();
		if(recu != null)
			System.out.println("[NETWORK MANAGER] - recevoirTCP : Message recu : [" + recu + "]");
		return recu;
	}

	private boolean isConnectivite(String reponse) {
		// on ne sélectionne que le type
		int ordinalReponse = Integer.parseInt(reponse.substring(0, 1));
		
		return (ordinalReponse == MessageType.CONNECTIVITE.ordinal());
	}

	private boolean isChangementPseudo(String reponse) {
		// on ne sélectionne que le type
		int ordinalReponse = Integer.parseInt(reponse.substring(0, 1));
		
		return (ordinalReponse == MessageType.CHANGEMENT_PSEUDO.ordinal());
	}
	
	private boolean isDeconnexionMessage(String reponse) {
		boolean res = false;
    	String message = reponse.substring(1);
    	String messagesSepares[] = message.split(";");
    	if(messagesSepares.length == 2) {
    		 if (messagesSepares[0].equals("Deconnexion")) {
				 if(!messagesSepares[1].isEmpty()) {
	    			 res = true;
				 }
		    	else {
		    		System.out.println("[isDeconnexionMessage] - ip vide");
		    	}
	    	}
	    	else {
	    		System.out.println("[isDeconnexionMessage] - mauvais message (pas deco mais " + messagesSepares[0] + ")");
	    	}
    	}
    	else {
    		System.out.println("[isDeconnexionMessage] - mauvaise taille : " + messagesSepares.length);
    	}

    	return res;
	}

	private InetAddress recevoirDeconnexionMessage(String reponse) throws InvalidMessageFormatException, MyOwnIpException, UnknownHostException {
    	String message = reponse.substring(1);
    	String messagesSepares[] = message.split(";");
    	if(messagesSepares.length != 2) {
			System.out.println("[recevoirDeconnexionMessage] - mauvaise taille : " + messagesSepares.length);
    		throw new InvalidMessageFormatException();
    	}
		if (!messagesSepares[0].equals("Deconnexion")) {
			System.out.println("[recevoirDeconnexionMessage] - mauvais format (!=Deconnexion)");
    		throw new InvalidMessageFormatException();
    	}
		if(messagesSepares[1].isEmpty()) {
			System.out.println("[recevoirDeconnexionMessage] - ip vide");
    		throw new InvalidMessageFormatException();
		}
    	if(messagesSepares[1].equals(this.monIp.getHostAddress())) {
    		throw new MyOwnIpException();
    	}
    	return InetAddress.getByName(messagesSepares[1]);
	}
	
	private void traiterDeconnexion(String message) {
		try {
			InetAddress ip = this.recevoirDeconnexionMessage(message);
			JDBC.getInstance().deleteA(ip.getHostAddress());
			Home.updateUsersList();
			Home.refresh();
		} catch (UnknownHostException e) {
			System.out.println("[traiterDeconnexion] - Adresse IP inconnue, deconnexion non prise en compte");
		} catch (InvalidMessageFormatException e) {
			System.out.println("[traiterDeconnexion] - Format du message de déconnexion invalide, deconnexion non prise en compte");
		} catch (MyOwnIpException e) {
			System.out.println("[traiterDeconnexion] - Propre déconnexion reçue, deconnexion non prise en compte");
		}
	}
	
    private String[] getCoordonneesFromReponseConnexion(String reponse) throws InvalidMessageFormatException, InvalidIpException, MyOwnIpException {
    	String[] coordonnees = new String[3];
    	/* Contient :
    	 * [0] IP
    	 * [1] Pseudo
    	 * [2] OK / KO
    	 * */
    	System.out.println("[getCoordonneesFromReponseConnexion] - getting coords from " + reponse);
    	coordonnees[2] = "OK";
    	// on ne sélectionne que le message
    	String message = reponse.substring(1);
    	
    	// Format
		//String message = "OK;" + monIp.getHostAddress() + ";" + pseudo;
    	String messagesSepares[] = message.split(";");
    	if(messagesSepares.length !=3) {
    		throw new InvalidMessageFormatException();
    	}
    	// l'IP reçue est la notre (ex reception de notre propre broadcast)
    	if(messagesSepares[1].equals(this.monIp.getHostAddress())) {
    		throw new MyOwnIpException();
    	}
    	// le pseudo envoyé était déjà pris
    	if(!messagesSepares[0].equals("OK")) {
    		coordonnees[2] = "KO";
    	}
    	// l'IP reçue est erronée
    	if(messagesSepares[1].isEmpty()) {
    		throw new InvalidIpException();
    	}
    	// le pseudo reçu est erroné
    	if(messagesSepares[2].isEmpty()) {
    		coordonnees[2] = "KO";
    	}
    	
    	coordonnees[0] = messagesSepares[1];	// IP
    	coordonnees[1] = messagesSepares[2];	// Pseudo
    	System.out.println("[getCoordonneesFromReponseConnexion] - got " + coordonnees[0] + " ; " + coordonnees[1] + " ; " + coordonnees[2]);
    	return coordonnees;
    }
    
    private String[] getCoordonneesFromDemandeConnexion(String reponse) throws InvalidMessageFormatException, InvalidIpException, InvalidConnexionMessageException, MyOwnIpException {
    	String[] coordonnees = new String[3];
    	/* Contient :
    	 * [0] IP
    	 * [1] Pseudo
    	 * [2] OK / KO
    	 * */
    	coordonnees[2] = "OK";
    	String expectedMessage = "Bonjour";
    	
    	// on ne sélectionne que le message
    	String message = reponse.substring(1);
    	
    	// Format
		//String message = "Bonjour;" + monIp.getHostAddress() + ";" + pseudo;
    	String messagesSepares[] = message.split(";");
    	if(messagesSepares.length !=3) {
    		throw new InvalidMessageFormatException();
    	}
    	// l'IP reçue est la notre (ex reception de notre propre broadcast)
    	if(messagesSepares[1].equals(this.monIp.getHostAddress())) {
    		throw new MyOwnIpException();
    	}
    	// l'IP reçue est erronée
    	if(messagesSepares[1].isEmpty()) {
    		throw new InvalidIpException();
    	}
    	// le pseudo envoyé était déjà pris
    	if(!messagesSepares[0].equals(expectedMessage)) {
    		throw new InvalidConnexionMessageException("Expected " + expectedMessage + " but got " + messagesSepares[0]);
    	}
    	// le pseudo reçu est erroné
    	if(messagesSepares[2].isEmpty()) {
    		System.out.println("Pseudo reçu erroné");
        	coordonnees[2] = "KO";
    	}
    	// le pseudo reçu est le meme que le notre
    	if(messagesSepares[2].equals(this.pseudo)) {
    		System.out.println("Pseudo reçu déjà pris");
        	coordonnees[2] = "KO";
    	}
    	
    	coordonnees[0] = messagesSepares[1];	// IP
    	coordonnees[1] = messagesSepares[2];	// Pseudo
    	
    	return coordonnees;
    }
    

    private String[] getInfosFromDemandeChangementPseudo(String reponse) throws InvalidMessageFormatException, InvalidIpException, MyOwnIpException, InvalidPseudoException {
    	String[] coordonnees = new String[3];
    	/* Contient :
    	 * 	[0] Demande/Reponse
    	 * >>> DEMANDE :
    	 * 		[1] IP
    	 * 		[2] Nouveau Pseudo
    	 * >>> REPONSE
    	 * 		[1] Pseudo
    	 * 		[2] reponse
    	 * */
    	
    	// on ne sélectionne que le message
    	String message = reponse.substring(1);
    	
    	// Format
		//String message = "2;" + monIp.getHostAddress() + ";" + nouveauPseudo;
    	String messagesSepares[] = message.split(";");
    	if(messagesSepares.length != 3) {
    		throw new InvalidMessageFormatException();
    	}
    	
    	// le type du message est valide 
    	if(!(	messagesSepares[0].equals("Demande") || 
    			messagesSepares[0].equals("Reponse") || 
    			messagesSepares[0].equals("DemandeOK"))) {
    		throw new InvalidMessageFormatException();
    	}
    	if(messagesSepares[0].equals("Demande")) {
        	// l'IP reçue est la notre (ex reception de notre propre broadcast)
        	if(messagesSepares[1].equals(this.monIp.getHostAddress())) {
        		throw new MyOwnIpException();
        	}
        	// l'IP reçue est erronée
        	if(messagesSepares[1].isEmpty()) {
        		throw new InvalidIpException();
        	}
        	// le pseudo reçu est erroné
        	if(messagesSepares[2].isEmpty()) {
        		throw new InvalidPseudoException("Empty pseudo received");
        	}
    	}
    	else if (messagesSepares[0].equals("Reponse")) {
        	// le pseudo reçu est erroné
        	if(messagesSepares[1].isEmpty()) {
        		throw new InvalidPseudoException("Empty pseudo received");
        	}
        	// la reponse reçue est erronée
        	if(!(messagesSepares[2].equals("OK") || messagesSepares[2].equals("KO"))) {
        		throw new InvalidMessageFormatException();
        	}
    	}
    	coordonnees[0] = messagesSepares[0];
    	coordonnees[1] = messagesSepares[1];
    	coordonnees[2] = messagesSepares[2];
    	return coordonnees;
    }
    
    private void setIPAddress() throws SocketException {
    	Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
    	if(e.hasMoreElements())
    	{
    	    NetworkInterface n = (NetworkInterface) e.nextElement();
    	    Enumeration<InetAddress> ee = n.getInetAddresses();
    	    if (ee.hasMoreElements())
    	    {
    	        InetAddress i = (InetAddress) ee.nextElement();
    	        i = (InetAddress) ee.nextElement();
    	        this.monIp = i;
    	        System.out.println(i.getHostAddress());
    	    }
    	}
    }
    
    public InetAddress getMyIpAddress() {
    	return this.monIp;
    }
    
    public String getPseudo() {
		return pseudo;
	}
    
    private int generateRandomPort() {
        ServerSocket s = null;
        try {
             // ServerSocket(0) results in availability of a free random port
             s = new ServerSocket(0);
             int port = s.getLocalPort();
             s.close();
             return port;
            } catch (Exception e) {
              throw new RuntimeException(e);
               } finally {
              assert s != null;
           try {
                  s.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
            }
        }

    
}
