package ServerClient;

import java.io.Serializable;



public class InfoGiocatore implements InformazioniGiocatore,Serializable {

	
	private static final long serialVersionUID = 1L;
	private int punteggio;
	private String pos;
	private String fazione;
	private boolean isUpDate;
	private int miss;
	
	
	public InfoGiocatore(int punteggio, String pos, boolean isUpDate,String fazione) {
		this.punteggio = punteggio;
		this.pos = pos;
		this.isUpDate = isUpDate;
		this.miss=0;
		this.fazione=fazione;
	}

	

	public void setPunteggio(int punteggio) {
		this.punteggio = punteggio;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	
	public void setUpDate(boolean isUpDate) {
		this.isUpDate = isUpDate;
		System.err.println("sei falso!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

	public void setMiss(int miss){
		this.miss=miss;
	}
	
	
	public void upDatePunteggio(int proiettili)
	{
		this.punteggio=punteggio+proiettili;
	}
	
	public void usoProiettile()
	{
		if(punteggio==0)
			throw new IllegalStateException("il punteggio non puo essere negativo!!");
		punteggio--;
	}
	
	public int getMiss(){
		return miss;
	}
	
	@Override
	public int getPunteggio() {
		return punteggio;
	}

	@Override
	public String getPos() {
		return pos;
	}

	
	@Override
	public boolean IsUpdate() {
		return isUpDate;
	}

	@Override
	public String getFazione() {
		return fazione;
	}

}
