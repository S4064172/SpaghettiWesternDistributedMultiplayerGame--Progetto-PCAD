package Server;

/*
 * Questa classe memorizza le informazioni del client
 * utili per la connessione della partita
 * e per la connessione della chat dato 
 * che in queste fasi si comporta come client
 */

public class Giocatore 
{
	private String ip;
	private String Username;
	private String Password;
	private int porta;
	private int portaChat;
	private int portaPingPong;
	private String fazione;
	
	
	public Giocatore(){}

	
	public Giocatore(String ip, String username, int porta,String Password, String fazione,int portaChat, int portaPingPong)
	{
		this.ip = ip;
		this.Password = Password;
		Username = username;
		this.porta = porta;
		this.fazione=fazione;
		this.portaChat = portaChat;
		this.portaPingPong=portaPingPong;
	}

	
	
	public int getPorta() {
		return porta;
	}

	public void setPorta(int porta) {
		this.porta = porta;
	}

	public int getPortaPingPong() {
		return portaPingPong;
	}

	public void setPortaPingPong(int portaPingPong) {
		this.portaPingPong = portaPingPong;
	}


	public String getFazione() {
		return fazione;
	}

	public void setFazione(String fazione) {
		this.fazione = fazione;
	}
	
	public String getPassword(){
		return Password;
	}

	public void setPassword(String password){
		Password = password;
	}


	public int getPortaChat() {
		return portaChat;
	}
	
	public void setPortaChat(int portaChat) {
		this.portaChat=portaChat;
	}
	

	public String getIp(){
		return ip;
	}
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public String getUsername(){
		return Username;
	}
	
	public void setUsername(String username){
		Username = username;
	}
	

	

}
