
public class Message {
	private String contenu;
	private MessageType type;
	private byte[] trame;	// trame à envoyer en bytes
	
	public Message (String contenu, MessageType type) {
		this.contenu = contenu;
		this.trame = new byte[65535];
		this.formaterMessage();
	}
	
	public byte[] getTrame() {
		return trame;
	}

	private String setStringMessageFromBytes()
	{
		String tmp = new String(this.contenu);	//trim pour éliminer les espaces générés dus à la taille du tableau de bytes
		tmp = tmp.trim();
		return tmp;
	}
	
	private synchronized void formaterMessage(){
		this.trame = (this.type.ordinal() + this.contenu).getBytes();
	}
	
}
