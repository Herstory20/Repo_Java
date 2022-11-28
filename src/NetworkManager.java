import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkManager {
	
	/*
	public NetworkManager() {
	}*/
	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {
		int portUDPs, portUDPr;
		String mode = "LISTEN";
		String message = "Bonjour a tous, j'existe";
		
		// si on met java NetworkManager BROADCAST => on veut brodcats
		// LISTEN => on ne veut que listen
		if(args.length==1)
		{
			if (args[0].equals("BROADCAST"))
			{
				mode = args[0];
			}
		}
		System.out.println("mode : " + mode);
		
		portUDPs = 1235;
		portUDPr = 1235;

		if(mode.equals("LISTEN"))
		{
			String recu = "";
			int i=0;
			UDP_Receiver udp_receive_thread;
			udp_receive_thread = UDP_Receiver.getInstance(portUDPr);
			Thread tudprcv = new Thread(udp_receive_thread);
			tudprcv.start();
			while(i<15)
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				recu = data(udp_receive_thread.getMessage());
				if(!recu.equals(""))
					System.out.println("Message recu : [" + recu + "]");
				i++;

			}
			
			udp_receive_thread.stop();
		}
		else if (mode.equals("BROADCAST"))
		{
			UDP_Sender udp_send_thread;
			
			InetAddress ip = InetAddress.getLocalHost();
			
			udp_send_thread = UDP_Sender.getInstance(ip, portUDPs);
			
			Thread tudpsend = new Thread(udp_send_thread);
			tudpsend.start();
			
			udp_send_thread.setMessage(message, MessageType.CONNECTIVITE);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			udp_send_thread.stop();
		}
			
		
		System.out.println("Fin network manager");
		
		
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
