package ServerClient;


/*
 * 
 * Interfaccia che memorizza i dati condivisi 
 * tra clien e server
 */

public interface InformazioniGiocatore {

	public int getPunteggio();
	public String getFazione();
	public String getPos();
	//public void upDatePunteggio(int proiettili);
	public boolean IsUpdate();
}
