import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkManager {
	
	/*
	public NetworkManager() {
	}*/
	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {
		int portUDPs, portUDPr;
		String message = "Super message a envoyer";
		if(args.length==3)
		{
			portUDPs = Integer.parseInt(args[0]); 
			portUDPr = Integer.parseInt(args[1]); 
			message = args[2];
		}
		else
		{
			portUDPs = 1234; 
			portUDPr = 1235; 
		}

		UDP_Sender udp_send_thread;
		UDP_Receiver udp_receive_thread;
		
		InetAddress ip = InetAddress.getLocalHost();
		
		udp_send_thread = UDP_Sender.getInstance(ip, portUDPs);
		udp_receive_thread = UDP_Receiver.getInstance(portUDPr);
		
		Thread tudpsend = new Thread(udp_send_thread);
		Thread tudprcv = new Thread(udp_receive_thread);
		tudpsend.start();
		tudprcv.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		udp_send_thread.setMessage(message, MessageType.CONNECTIVITE);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Message recu : [" + data(udp_receive_thread.getMessage()) + "]");
		
		System.out.println("Fin network manager");
		
		udp_send_thread.endThread();
		udp_receive_thread.endThread();
		
		tudpsend.join();
		tudprcv.join();
		
	}

    // A utility method to convert the byte array
    // data into a string representation.
    private static StringBuilder data(byte[] a)
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
        return ret;
    }
}
