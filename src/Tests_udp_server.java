import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Tests_udp_server {

	public static void main(String[] args) throws SocketException, UnknownHostException {
		DatagramSocket ds = new DatagramSocket();
		
		InetAddress ip = InetAddress.getLocalHost();
		
        byte buf[] = null;
        String toSend = "coucou";

        
        for (int i = 0; i < 5; i++) {
        	if (i==4)
        	{
        		toSend = "bye";
        	}
            buf = toSend.getBytes();
    		
    		DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 1234);
    		
    		try {
    			ds.send(DpSend);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		System.out.println("Message [" + toSend + "] envoyé en UDP à l'adresse " + ip + " !");
    		ds.close();
        }
	}

}
