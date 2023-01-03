
public class Message {
	private String contenu;
	private MessageType type;
	private byte[] trame;	// trame à envoyer en bytes
	// TODO : ajouter l'horodatage !
	
	public Message (String contenu, MessageType type) {
		this.contenu = contenu;
		this.type = type;
		this.trame = new byte[65535];
		this.formaterMessageBytes();
	}
	
	public Message (byte[] trame) {
		this.trame = trame;
		this.setStringMessageFromBytes();
	}
	
	
	
	
	
	public byte[] getTrame() {
		return trame;
	}
	
	public String getTrameString() {
		return this.formaterMessageString();
	}
	
	public String getContenu() {
		return this.contenu;
	}
	
	public MessageType getType() {
		return this.type;
	}
	
	

	private void setStringMessageFromBytes()
	{
		String tmp = new String(this.trame);
		tmp = tmp.trim();	//trim pour éliminer les espaces générés dus à la taille du tableau de bytes
		this.setContenuTypeFromTrame(tmp);
	}
	
	private void setContenuTypeFromTrame(String reponse) {
		int ordinalReponse = Integer.parseInt(reponse.substring(0, 1));
		this.type = MessageType.values()[ordinalReponse];
		this.contenu = reponse.substring(1);
	}
	
	
	
	private synchronized String formaterMessageString(){
		return this.type.ordinal() + this.contenu;
	}
	
	private synchronized void formaterMessageBytes(){
		this.trame = (this.formaterMessageString()).getBytes();
	}
	
	
	
	@Override
	public String toString() {
		return this.getTrameString();
	}
	
}
