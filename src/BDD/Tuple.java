package BDD;

import java.sql.Timestamp;

public class Tuple {
	public String ip;
	public String message;
	public Timestamp date;
	
	public Tuple(String ip, String message, Timestamp date) {
		super();
		this.ip = ip;
		this.message = message;
		this.date = date ;
	}

	public Timestamp getDate() {
		return date;
	}

	public String getIp() {
		return ip;
	}

	public String getMessage() {
		return message;
	}
	
	

}