import java.net.SocketException;
import java.net.UnknownHostException;

public class Clavardeur {

	public static void main(String[] args) throws UnknownHostException, SocketException {
		String pseudo = "";
		if(args.length == 1)
		{
			pseudo = args[0];
		}
		else {
			System.out.println("argument PSEUDO manquant");
			System.exit(0);
		}
		
		NetworkManager nm = new NetworkManager();
		boolean connexionOK = false;
		try {
			nm.connexion(pseudo);
			connexionOK = true;
		} catch (InvalidPseudoException e) {
			e.printStackTrace();
		}
		System.out.println("Ecoute :\n>>");
		while(connexionOK)
		{
			nm.update();
		}
	}
}
