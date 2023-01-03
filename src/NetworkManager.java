import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkManager {
	
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
	
	public NetworkManager() throws IOException {
		this.pseudo = "";
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

		
		t_udpsend.start();
		t_udprcv.start();	
	}
	
	public boolean connexion(String pseudo) throws InvalidPseudoException, IOException {
		boolean success = true;
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
					String[] coord;
					try {
						coord = getCoordonneesFromReponseConnexion(recu);
						if(coord[2] == "OK")
						{
							this.coordonneesUtilisateur.put(coord[0], coord[1]);
							// à ajouter dans la bdd aussi !
						}
						else {
							System.out.println("Coordonnees recues erronnees");
							success = false;
						}
					} catch (InvalidMessageFormatException | InvalidIpException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MyOwnIpException e) {
						System.out.println("[NETWORK MANAGER] - connexion : Réception de notre propre paquet ignorée");
					}
				}
			}
			elapsedTime = System.currentTimeMillis() - start;
		}
		
		System.out.println("[NETWORK MANAGER] - connexion : Coordonnees recues : \n" + this.coordonneesUtilisateur);
		
		if(success) {
			// mise a jour de l'annuaire
			System.out.println("[NETWORK MANAGER] - connexion : Mise à jour de l'Annuaire...");
			this.pseudo = pseudo;
			this.initTCPRcvThread();
		}
		else {
			System.out.println("[NETWORK MANAGER] - connexion : Echec de la mise à jour de l'Annuaire, pseudo invalidé par les autres utilisateurs");
		}
		
		
		udp_send_thread.setBroadcastDisabled();
		
		System.out.println("[NETWORK MANAGER] - connexion : Fin connexion");
		return success;
	}
	

	public void update() {
		String recuUdp = this.recevoirUDP();
		if(!recuUdp.isEmpty()) {
			System.out.println("[NETWORK MANAGER] - update : MESSAGE UDP " + recuUdp + " RECU ! Réponse en cours...");
			this.repondreTentativeConnexionUDP(recuUdp);
		}
		
		Message recuTcp = this.recevoirTCP();
		// /!\ vérifier qu'on reçoive bien un paquet du meme ip pendant une co !
		// + vérifier que l'ip est dans le dictionnaire ! = sécu
		if(recuTcp != null) {
			System.out.println("[NETWORK MANAGER] - update : MESSAGE TCP " + recuTcp + " RECU ! Réponse en cours...");
			this.negociationDePorts(recuTcp);
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
	
	
	
	/** Méthodes privées 
	 * @throws IOException **/
	
	
	private void initTCPRcvThread() throws IOException {
		// TCP thread config
		try {
			this.tcp_receive_thread = new TCP_Receiver(NetworkManager.TCP_PORT);
			Thread t_tcprcv = new Thread(tcp_receive_thread);
			t_tcprcv.start();
		} catch (IOException e) {
			System.out.println("Connexion sur le port " + NetworkManager.TCP_PORT + " impossible");
			throw new IOException();
		}
	}
	

	private void initTCPSendThread() throws IOException{
		// TCP thread config
		try {
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
					coord = this.getCoordonneesFromDemandeConnexion(recu);
					
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
						System.out.println("MAJ de la table Annuaire ...");
						// mettre a jour la bdd (avec port null !)
					}
				} catch (InvalidMessageFormatException | InvalidIpException | InvalidConnexionMessageException e) {
					e.printStackTrace();
				} catch (MyOwnIpException e) {
					// si c'est notre IP, on ignore le paquet
					System.out.println("[repondreTentativeConnexionUDP] : Propre paquet reçu");
				}
		}
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
					// ajouter le port dans la BDD
					this.convManager.lancerConversation(this.tcp_ipDistant);
					System.out.println("[negociationDePorts] : La discussion peut conmmencer.");
					this.convManager.sendSomething(this.tcp_ipDistant);
				} catch (ConversationNotFound e) {
					this.annulationNegociationPorts();
					System.out.println("[negociationDePorts] : Erreur d'ajout de port distant, annulation de la négociation de ports");
				}
			}
			// voir pourquoi recu incorrect
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
		this.tcp_send_thread.stop();
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
		this.udp_send_thread.stop();
		this.udp_receive_thread.stop();
		this.tcp_send_thread.stop();
		this.tcp_receive_thread.stop();
		System.out.println("[Network Manager] : Fin network manager");
	}
	
	private String recevoirUDP() {
		String recu = "";
		recu = this.udp_receive_thread.getMessageString();
		//recu = NetworkManager.data(this.udp_receive_thread.getMessage());
		if(!recu.isEmpty())
			System.out.println("[NETWORK MANAGER] - recevoirUDP : Message recu : [" + recu + "]");
		return recu;
	}
	
	private Message recevoirTCP() {
		Message recu;
		recu = this.tcp_receive_thread.getMessage();
		//recu = NetworkManager.data(this.udp_receive_thread.getMessage());
		if(recu != null)
			System.out.println("[NETWORK MANAGER] - recevoirTCP : Message recu : [" + recu + "]");
		return recu;
	}

	private boolean isConnectivite(String reponse) {
		// on ne sélectionne que le type
		int ordinalReponse = Integer.parseInt(reponse.substring(0, 1));
		
		return (ordinalReponse == MessageType.CONNECTIVITE.ordinal());
	}

	
    private String[] getCoordonneesFromReponseConnexion(String reponse) throws InvalidMessageFormatException, InvalidIpException, MyOwnIpException {
    	String[] coordonnees = new String[3];
    	/* Contient :
    	 * [0] IP
    	 * [1] Pseudo
    	 * [2] OK / KO
    	 * */
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
    
    private void setIPAddress() throws SocketException {
    	Enumeration e = NetworkInterface.getNetworkInterfaces();
    	if(e.hasMoreElements())
    	{
    	    NetworkInterface n = (NetworkInterface) e.nextElement();
    	    Enumeration ee = n.getInetAddresses();
    	    if (ee.hasMoreElements())
    	    {
    	        InetAddress i = (InetAddress) ee.nextElement();
    	        i = (InetAddress) ee.nextElement();
    	        this.monIp = i;
    	        System.out.println(i.getHostAddress());
    	    }
    	}
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
