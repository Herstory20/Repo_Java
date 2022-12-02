import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Tests_udp_client {

	public static void main(String[] args) throws SocketException, UnknownHostException {
		DatagramSocket ds = new DatagramSocket(1236);
		
		//InetAddress ip = InetAddress.getLocalHost();
		
        byte[] receive = new byte[65535];
        String toGet = "";
		
        DatagramPacket DpReceive = null;
        System.out.println("Waiting for messages... ");
        
        while (true)
        {
  
            DpReceive = new DatagramPacket(receive, receive.length);
  
            try {
				ds.receive(DpReceive);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  
            System.out.println("Client:-" + data(receive));
  
            if (data(receive).toString().equals("bye"))
            {
                System.out.println("Client sent bye.....EXITING");
                break;
            }
  
            // Clear the buffer after every message.
            receive = new byte[65535];
        }
        
        
	}
	  
    // A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a)
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
