import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;


import java.net.NetworkInterface;
public class NetworkManager {

	private UDP_Sender udp_send_thread;
	private UDP_Receiver udp_receive_thread;
	private int udp_port;
	private InetAddress monIp;
	private static final int CONNEXION_DELAI_ATTENTE_REPONSE_MS = 1000;
	private String pseudo;
	
	public NetworkManager() throws UnknownHostException, SocketException {
		this.pseudo = "";
		this.monIp = InetAddress.getLocalHost();
		
		// send thread config
		this.udp_send_thread = UDP_Sender.getInstance(this.monIp, this.udp_port);
		Thread t_udpsend = new Thread(this.udp_send_thread);

		// receive thread config
		this.udp_receive_thread = UDP_Receiver.getInstance(this.udp_port);
		Thread tudprcv = new Thread(this.udp_receive_thread);
		
		t_udpsend.start();
		tudprcv.start();
		
	}
	
	public void connexion(String pseudo) throws InvalidPseudoException {
		
		// envoi des informations de connexion
		String message = "Bonjour;" + monIp.getHostAddress() + ";" + pseudo;
		this.udp_send_thread.setBroadcastEnabled();
		this.udp_send_thread.setMessage(message, MessageType.CONNECTIVITE);
		udp_send_thread.setBroadcastDisabled();
		
		// attente des réponses des autres utilisateurs
		String recu = "";
		Hashtable<String, String> coordonneesUtilisateur = new Hashtable<String, String>();
		
		long start = System.currentTimeMillis();
		long elapsedTime = System.currentTimeMillis() - start;
		
		while(elapsedTime < NetworkManager.CONNEXION_DELAI_ATTENTE_REPONSE_MS) {
			recu = this.recevoirUDP();
			if (this.isConnectivite(recu)) {
				String[] coord;
				try {
					coord = getCoordonneesFromReponseConnexion(recu);
					coordonneesUtilisateur.put(coord[0], coord[1]);
				} catch(InvalidPseudoException e) {
					throw e;
				} catch (InvalidMessageFormatException | InvalidIpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			elapsedTime = System.currentTimeMillis() - start;
		}
		
		// mise a jour de l'annuaire
		System.out.println("[Network Manager] : Coordonnees recues : \n" + coordonneesUtilisateur);
		
		this.pseudo = pseudo;
		
		System.out.println("[Network Manager] : Fin connexion");
	}
	
	private void repondreTentativeConnexionUDP(String recu) {
		if (this.isConnectivite(recu)) {
				try {
					this.getCoordonneesFromDemandeConnexion(recu);
					
					// on envoit OK si tout est bon avec nos coordonnées
					String message = "OK;" + monIp.getHostAddress() + ";" + pseudo;
					this.udp_send_thread.setMessage(message, MessageType.CONNECTIVITE);
					
				} catch (InvalidPseudoException e) {
					// on envoit KO car pseudo invalide
					String message = "KO;" + monIp.getHostAddress() + ";" + pseudo;
					this.udp_send_thread.setMessage(message, MessageType.CONNECTIVITE);
					e.printStackTrace();
					
				} catch (InvalidMessageFormatException | InvalidIpException | InvalidConnexionMessageException e) {
					e.printStackTrace();
				}
		}
	}
	
	public void update() {
		String recu = this.recevoirUDP();
		if(!recu.equals("")) {
			repondreTentativeConnexionUDP(recu);
		}
	}
	
	@Override
	public void finalize()
	{
		this.udp_send_thread.stop();
		this.udp_receive_thread.stop();
		System.out.println("[Network Manager] : Fin network manager");
	}
	
	private String recevoirUDP() {
		String recu = "";
		recu = data(udp_receive_thread.getMessage());
		if(!recu.equals(""))
			System.out.println("Message recu : [" + recu + "]");
		return recu;
	}

	private boolean isConnectivite(String reponse) {
		// on ne sélectionne que le type
		int ordinalReponse = Integer.parseInt(reponse.substring(0, 1));
		
		return (ordinalReponse == MessageType.CONNECTIVITE.ordinal());
	}

	
    private String[] getCoordonneesFromReponseConnexion(String reponse) throws InvalidMessageFormatException, InvalidPseudoException, InvalidIpException {
    	String[] coordonnees = new String[2];
    	
    	// on ne sélectionne que le message
    	String message = reponse.substring(1);
    	
    	// Format
		//String message = "OK;" + monIp.getHostAddress() + ";" + pseudo;
    	String messagesSepares[] = message.split(";");
    	if(messagesSepares.length !=3) {
    		throw new InvalidMessageFormatException();
    	}
    	// le pseudo envoyé était déjà pris
    	if(!messagesSepares[0].equals("OK")) {
    		throw new InvalidPseudoException("Pseudo déjà pris");
    	}
    	// l'IP reçue est erronée
    	if(messagesSepares[1].equals("")) {
    		throw new InvalidIpException();
    	}
    	// le pseudo reçu est erroné
    	if(messagesSepares[2].equals("")) {
    		throw new InvalidPseudoException("Pseudo reçu erroné");
    	}
    	
    	coordonnees[0] = messagesSepares[1];	// IP
    	coordonnees[1] = messagesSepares[2];	// Pseudo
    	
    	return coordonnees;
    }
    
    private String[] getCoordonneesFromDemandeConnexion(String reponse) throws InvalidMessageFormatException, InvalidPseudoException, InvalidIpException, InvalidConnexionMessageException {
    	String[] coordonnees = new String[2];
    	String expectedMessage = "Bonjour";
    	
    	// on ne sélectionne que le message
    	String message = reponse.substring(1);
    	
    	// Format
		//String message = "Bonjour;" + monIp.getHostAddress() + ";" + pseudo;
    	String messagesSepares[] = message.split(";");
    	if(messagesSepares.length !=3) {
    		throw new InvalidMessageFormatException();
    	}
    	// le pseudo envoyé était déjà pris
    	if(!messagesSepares[0].equals(expectedMessage)) {
    		throw new InvalidConnexionMessageException("Expected " + expectedMessage + " but got " + messagesSepares[0]);
    	}
    	// l'IP reçue est erronée
    	if(messagesSepares[1].equals("")) {
    		throw new InvalidIpException();
    	}
    	// le pseudo reçu est erroné
    	if(messagesSepares[2].equals("")) {
    		throw new InvalidPseudoException("Pseudo reçu erroné");
    	}
    	// le pseudo reçu est le meme que le notre
    	if(messagesSepares[2].equals(this.pseudo)) {
    		throw new InvalidPseudoException("Pseudo reçu déjà pris");
    	}
    	
    	coordonnees[0] = messagesSepares[1];	// IP
    	coordonnees[1] = messagesSepares[2];	// Pseudo
    	
    	return coordonnees;
    }
    
    // A utility method to convert the byte array
    // data into a string representation.
    private static String data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret.toString();
    }
    
}
