package ServerClient;

import java.io.Serializable;



public class InfoGiocatore implements InformazioniGiocatore,Serializable {

		private static final long serialVersionUID = 1L;
		private int punteggio;
		private String pos;
		private String fazione;
		private boolean isUpDate;
		
		public void setPos(String newPos)
		{
			this.pos=newPos;
		}
		
		@Override
		public String getFazione()
		{
			return fazione;
		}
		
		public void setPunteggio(int puntegggio)
		{
			this.punteggio=puntegggio;
		}
		
		/*@Override
		public void upDatePunteggio(int upDate)
		{
			this.punteggio=this.punteggio+upDate;
		}*/
		
		@Override
		public int getPunteggio() {
			return punteggio;
		}

		@Override
		public String getPos(){
			return pos;
		}
		
		@Override
		public boolean IsUpdate() {
			return isUpDate;
		}

}
