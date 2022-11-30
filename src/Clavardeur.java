import java.net.SocketException;
import java.net.UnknownHostException;

public class Clavardeur {

	public static void main(String[] args) throws UnknownHostException, SocketException {
		NetworkManager nm = new NetworkManager();
		boolean connexionOK = false;
		try {
			nm.connexion("Provençal le Gaulois");
			connexionOK = true;
		} catch (InvalidPseudoException e) {
			e.printStackTrace();
		}
		
		while(connexionOK)
		{
			nm.update();
		}
	}
}
