import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Clavardeur {

	public static void main(String[] args) throws IOException {
		
		InetAddress ipToReach;
		
		String pseudo = "";
		if(args.length == 1)
		{
			pseudo = args[0];

			NetworkManager nm = new NetworkManager();
			boolean connexionOK = false;
			try {
				connexionOK = nm.connexion(pseudo);
			} catch (InvalidPseudoException e) {
				e.printStackTrace();
			}
			if(!connexionOK) {
				System.exit(0);
			}
			
			
			System.out.println("Ecoute :\n>>");
			while(connexionOK)
			{
				nm.update();
			}
		}
		if(args.length == 2)	// cas d'une machine voulant lancer une conversation
		{
			pseudo = args[0];
			ipToReach = InetAddress.getByName(args[1]);

			NetworkManager nm = new NetworkManager();
			boolean connexionOK = false;
			try {
				connexionOK = nm.connexion(pseudo);
			} catch (InvalidPseudoException e) {
				e.printStackTrace();
			}
			if(!connexionOK) {
				System.exit(0);
			}
			
			nm.newDiscussion(ipToReach);
			System.out.println("Demande de discussion :\n>>");
			while(connexionOK)
			{
				nm.update();
			}
		}
		else {
			System.out.println("argument PSEUDO manquant");
			System.exit(0);
		}
		
	}
}
