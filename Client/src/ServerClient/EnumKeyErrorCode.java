package ServerClient;



public enum EnumKeyErrorCode 
{
	//INIZIO FASE DI REGISTRAZIONE
	InizioRegistrazione,
	ConnessioneServer,
	RiconnessioneServer,
	FazioneBuoni,
	FazioneCattivi,
	ServerPieno,
		//Errori
		ErroreDatiConnessione,
		ErroreLetturaDatiDiRegistrazione,
		ErroreLetturaDatiDiRiconnesione,
		ErroreUsernameGiaPresente,
		ErroreFaziniSquilibrate,
		ErroreRiconnessione,
		ErroreCampiRegistrazioneVuoti,
		ErroreClientOnline,
		
		//Conferme
		ConfermaAvvenutaRegistrazione,
		ConfermaAvvenutaRiconnessione,
		ConfermaTempoRegistrazioneScaduto,
	//FINE FASE DI REGISTRAZIONE
	
	//INIZIO GESTIONE PARTITA
	GiocoIniziato,
	FinePartita,
	ClientOffline,
	//FINE GESTIONE PARTITA
		
	//INIZIO GESTIONE TURNO
	InizioTurno,
		//CONFERME
		ConfermaInizioTurno,
		ConfermaInizioAggironamento,
		ConfermaAggiornamentoRicevuto,
		ConfermaComandoRicevuto,
		ConfermaFineTruno,
		
		//TIPOLOGIE COMANDI
		ComandoMossa,
		ComandoAttacco,
		ComandoFineTurno,
		
		//SFIDA
		SfidaIniziata,
		FineSfida,
			//TIPOLOGIE DI COMANDI
			SfidaAttacco,
			SfidaRicarico,
			SfidaDifesa,
			SfidaPareggio,
			SfidaPunteggiAggiornati,
			
			//TIPOLOGIE RISULTATI
			HaiPerso,
			HaiVinto,
			PareggioFineMosse,
	//FINE GESTIONE TURNO
			
	//STAMPE A VIDEO
	ServerNonRisponde,
	ConnessioneAssente,
	ErroreStessaFazione,
	Aggiornamento,
	Spostamento,
	Attaccamento, //XD
	Munizioni,
	NumDifesa,
	NumRicaricate,
	ErroreInizializzazione,
	ErroreSconosciuto,	
	
	//FASE PING PONG
	ClientOnline,
	ServerOnline
}
