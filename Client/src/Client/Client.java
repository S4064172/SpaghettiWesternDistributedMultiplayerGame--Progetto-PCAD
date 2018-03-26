package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import com.mxgraph.view.mxGraph;
import ClientGraph.Grafox;
import ServerClient.InfoGiocatore;
import ServerClientGraph.DatiGraph;
import ServerClientGraph.InfoGraph;
import ServerClientGraph.PosGraph;

import static ServerClient.EnumKeyErrorCode.*;
@SuppressWarnings({ "unchecked" })
public class Client {
	
	//Socket per la gestione della comunicazione
	private Socket socketClient; //socket della fase di registrazione
	private ServerSocket socketServer; //socket della fase di gioco
	private ServerSocket socketChat; //socket della fase di chat
	private ServerSocket socketPingPong; //socket della fase di pingPong
	
	//Gestione della grafica
	private  Game formDiGioco; //Completa gestione della partita di gioco
	private  Grafox grafox; //Creazione del campo da gioco
	private Battle sfida; //gestione della sfida tra i giocatori
	private WaitGame waitGame; //finestra di attesa gioco

	//Strutture per la memorizzazione delle informazioni vitali
	private ConcurrentHashMap<String, InfoGiocatore> mapGiocatoriInfo=null; //Informazioni sui partecipanti del gioco
	private List<String> listNomiCitta; //Nome delle citta della mappa
	private List<String> keyErrorCode; //Lista dei codici di errore
	private ConcurrentHashMap<String, String> errorCode; //Associazione codice errore significato
	private InfoGraph infoGraph;
		
	//Procedure per il controllo dello stato del server
	private TPingPongServerToClinet threadPingPong; //Questo servizioni server per farsi pingare dal server
	private TPingPongClientToServer pingPong; //Questo servizio server per  controllare lo stato del server

	//Dati necessari al collegamento col server
	private String ipServer = "127.0.0.1";
	private int portServer = 9000;
	private int portServerChat=9001;
	private int portServerPingPong = 9002;
	
	//Dati necissari per farsi contattare dal server
	private int port=9000;
	private int portChat=9025;
	private int portPingPong=9050;
	
	//Variabile di appoggio per le risposte del server
	private String response;
	
	//Tempi di gioco
	private int TempoRegistrazione=50000; // tempo disponibile per ricevere una risposta dal server
	private int TempoRispostaGame=5000; //attesa risposta server in fase di gioco/aggiornamento
	private int TempoPingPong=20000; // tempo di attesa connessione col server...(due turni)
	private int TempoPingPongAttesaLettura=1000; //tempo di attesa per la ricezione del messaggio del server
	private int TempoInizializzazionePartita=900; //tempo di attesa conferma dal server
	private int TemporizzazioneChiusuraForm=2000;//tempo che permette di vedere l'esito della registrazione
	private int TempoSfida=6500; //tempo attesa risposta server 
	
	private int  TemporizzatoreMossaAutomatica=5000;
	private int TemporizzatoreAttesaFineAggiornamento =500;
	private int TemporizzatoreThreadPingPong = 500;

	//variabile d'appoggio per memorizzare 
	private String username; //il nick scelto. utilizzato per la chat
	
	//variabili di controllo
	private boolean GameIsStop=false; //definisce la fine del gioco
	private boolean IsConnet=false; //definisce la fine della connessione con un servizio
	private boolean isPortCorrect=false; //definisce la disponibilita della porta scelta
	private boolean errorUpdate = false;
	
	
	private final String logPort = "logPorte.txt";
	private final boolean Debug = false;
	
	
	public Client()
	{
		final String idStampa="Client: ";
		int lockPort = 0;
		int lockPortChat = 0;
		int lockPortPingPong = 0;
		
		//per effettuare una riconnessione serve uno store delle porte utilizzate
		//in modo da evitare di riprendere le stesse usate nella connessione precedente
		try (BufferedReader readFile = new BufferedReader(new FileReader(logPort)))
		{
			lockPort = Integer.parseInt(readFile.readLine());
			lockPortChat = Integer.parseInt(readFile.readLine());
			lockPortPingPong = Integer.parseInt(readFile.readLine());
			
		} catch (IOException  e) {
			System.err.println(idStampa+"-->il file non esiste "+e);
		}
		
		/*Connessione col server*/
		try {
			socketClient = new Socket(ipServer, portServer);
			IsConnet = true;
			System.err.println(idStampa+"Server online!!");
		} catch (IOException e1) {
			System.err.println(idStampa+"Server offline!!");
		}
		
		if (!IsConnet)
			return;
		
		/*Inizializzazione servizio: comunicazione server-client */
		while (!isPortCorrect)
		{
			try {
				// todo: sincronizzare i client!! potrebbe nascere dei problemi sulla porta!!
				if(port==lockPort)
					port++;
				socketServer = new ServerSocket(port);
				isPortCorrect=true;
				System.err.println(idStampa+"---> Servizio Attesa Turno Online sulla porta " +port);
			} catch (IOException e) {
				if(Debug)
					System.err.println(idStampa+"Porta Occupata "+ port +"!!Provane un'altra");
				if(port==9500)
					return;
				port++;
				
			}
		}
		
		/*Inizializzazione servizio: servizio di chat*/
		isPortCorrect=false;
		while (!isPortCorrect)
		{
			try {
				if(portChat==lockPortChat)
					portChat++;
				socketChat = new ServerSocket(portChat);
				isPortCorrect=true;
				System.err.println(idStampa+"---> Servizio Chat Online sulla porta " +portChat);
			} catch (IOException e) {
				if(Debug)
					System.err.println(idStampa+"Porta Occupata "+ portChat +"!!Provane un'altra");
				if(portChat==9500)
					return;
				portChat++;
				
			}
		}
	
		/*Inizializzazione servizio: servizio di pingPong*/
		isPortCorrect=false;
		while (!isPortCorrect)
		{
			try {
				if(portPingPong==lockPortPingPong)
					portPingPong++;
				socketPingPong = new ServerSocket(portPingPong);
				isPortCorrect=true;
				System.err.println(idStampa+"---> Servizio PingPong Online sulla porta " +portPingPong);
			} catch (IOException e) {
				if(Debug)
					System.err.println(idStampa+"Porta Occupata "+ portPingPong +"!!Provane un'altra");
				if(portPingPong==9500)
					return;
				portPingPong++;
				
			}
		}
		threadPingPong = new TPingPongServerToClinet();
		pingPong = new TPingPongClientToServer(TempoPingPong);	
	}
	
	
	/*
	* Questa fasce gestisce la registrazione del client al
	* server di gioco e tramite un apposito protocollo 
	* vengono scambiate delle stringere per far "funzionare"
	* tutto a dovere. Nel caso in cui il client non ricevesse alcun messaggio
	* dal server è stato inserito un timeOut per poter far "saltare" la fase 
	* di registrazione.
	*/
	private int Registrazione() 
	{
		final String idStampa="Registrazione: ";
		System.err.println(idStampa+"----------InizioFaseRegistrazione----------------");
		
		try {
			socketClient.setSoTimeout(TempoRegistrazione);
		} catch (SocketException e) {
			System.err.println(idStampa+"Errore settaggio attesa client!!! " + e);
		}
		
		try (ObjectOutputStream outStream = new ObjectOutputStream(socketClient.getOutputStream());
			 ObjectInputStream	in = new ObjectInputStream(socketClient.getInputStream()) ) 
		{
			try {
				keyErrorCode=(List<String>)in.readObject();
				errorCode=(ConcurrentHashMap<String, String>)in.readObject();
				
				//Se non riconosco il primo messaggio del protocollp interrompo tutto (controllo)
				if ( !((String) in.readObject()).equals(keyErrorCode.get(InizioRegistrazione.ordinal()) ))
					throw new IllegalStateException("PrimoMessaggioInRegistrazioneErrato");
				
			}catch (ClassNotFoundException | IOException e) {
				System.err.println(idStampa+"Errore messaggio apertura!! Riavviare il client!!" + e);
				socketClient.close();
				return -1;
			}
			
			System.err.println(idStampa+"----InizioFaseIvioDati");
			StartGame form = new StartGame(outStream,port,portChat,portPingPong,errorCode,keyErrorCode);
			form.SetVisible();			
			try {
				//risposte della fase di registrazione
				while( 	!((response = (String)in.readObject()).equals(keyErrorCode.get(ConfermaAvvenutaRegistrazione.ordinal()))) && 
						!(response.equals(keyErrorCode.get(ConfermaAvvenutaRiconnessione.ordinal()))) )
				{
					form.SetError(errorCode.get(response));
					System.err.println(idStampa+"codiceRisposta: "+errorCode.get(response));
					if(	response.equals(keyErrorCode.get(GiocoIniziato.ordinal())) ||
						response.equals(keyErrorCode.get(ServerPieno.ordinal()))  ||
						response.equals(keyErrorCode.get(ErroreClientOnline.ordinal())))
					{
						form.SetIsConnect(false);
						System.err.println(idStampa+"----FineFaseIvioDati");
						return -1;
					}
				}
				
				form.SetError(errorCode.get(response));
				System.err.println(idStampa+"codiceRisposta: "+errorCode.get(response));
				
			} catch (SocketTimeoutException e)
			{
				System.err.println(idStampa+"Tempo Risposta Scaduto!! Riavviare il client!! "+ e);
				form.SetError(errorCode.get(keyErrorCode.get(ServerNonRisponde.ordinal())));
				form.SetIsConnect(false);
				socketClient.close();
				System.err.println(idStampa+"----FineFaseIvioDati");
				return -1;
			} 
			catch (ClassNotFoundException | IOException e1)
			{
				System.err.println(idStampa+"Errore messaggio conferma campi iscrizione!! Riavviare il client!! " + e1);
				socketClient.close();
				form.SetError(errorCode.get(keyErrorCode.get(ConnessioneAssente.ordinal())));
				form.SetIsConnect(false);
				System.err.println(idStampa+"----FineFaseIvioDati");
				return -1;
			}
			System.err.println(idStampa+"----FineFaseIvioDati");
			username=form.getUsername();
		
			try {
				Thread.sleep(TemporizzazioneChiusuraForm);
			} catch (InterruptedException e) {
				System.err.println(idStampa+"Errore settaggio TemporizzazioneChiusuraForm" + e);
			}
			form.Close();
			socketClient.close();

		} catch (Exception e) {
			System.err.println(idStampa+"Errore apertura buffer O chiusura socket!! Riavviare il client!! " + e);
			return -1;
		}
	
		try {
			socketClient.close();
		} catch (Exception e) {
			System.err.println(idStampa+"Errore chiusura socket!! Riavviare il client!!" + e);
			return -1;
		}	
		System.err.println(idStampa+"----------FineFaseRegistrazione----------------");
		return 0;
	}
	

	/*
	 * Questa fase è la fase piu cruciale del gioco, il client 
	 * riceve tutte le informazioni per poter partecipare al gioco.
	 * Qualsiasi errore comporta l'interruzuine del gioco.
	 * Nel caso in cui il client venga disconnesso sarà possibile 
	 * comunque riconnettersi alla partita.
	 */
	private int inizializzazionePartita()
	{
		final String idStampa="InizializzazionePartita: ";
		System.err.println(idStampa+"-----------------InizioFaseInizializzazionePartita---------------");
		PosGraph posGraph;
		try(Socket aggirnamentoSocket = socketServer.accept())
		{
			//durante la partita vengono scambiati dei pingPong 
			//tra server e client; se il server mi sta partlando non ha 
			//senso mandare un ping.
			pingPong.NotifyPingPong();
			
			try(ObjectOutputStream outStream =  new ObjectOutputStream(aggirnamentoSocket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(aggirnamentoSocket.getInputStream()) )
			{
				aggirnamentoSocket.setSoTimeout(TempoInizializzazionePartita);
				Object frtMessage = inStream.readObject();
				if (frtMessage instanceof String)
					throw new IllegalAccessError((String) frtMessage+"--"+(String) inStream.readObject());
				
				mapGiocatoriInfo=(ConcurrentHashMap<String,InfoGiocatore>)frtMessage;
				if(Debug) 
				{
					System.err.println(idStampa+"mapGiocatoriInfo.get(username).getPos(): "+mapGiocatoriInfo.get(username).getPos());
					System.err.println(idStampa+"mapGiocatoriInfo.get(username).getPunteggio()"+mapGiocatoriInfo.get(username).getPunteggio());
					System.err.println(idStampa+"mapGiocatoriInfo.get(username).getFazione()"+mapGiocatoriInfo.get(username).getFazione());
				}				
				posGraph = (PosGraph)inStream.readObject();
				listNomiCitta=(List<String>)inStream.readObject();
				infoGraph=(InfoGraph)inStream.readObject();
				outStream.writeObject(keyErrorCode.get(ConfermaAggiornamentoRicevuto.ordinal()));
				if(!(boolean)inStream.readObject())
				{
					System.err.println(idStampa+"--->per il server non sei aggiornato");
					return ErroreInizializzazione.ordinal();
				}
				
			}catch (SocketTimeoutException e) {
				System.err.println(idStampa+"tempo aggiornamento finito, client non pronto a giocare " + e);
				return ErroreInizializzazione.ordinal();
			}catch (IOException e) {
				System.err.println(idStampa+"Errore apertura buffer in / out " + e);
				e.printStackTrace();
				return -1;
			}
		}catch (Exception e) {
			System.err.println(idStampa+"Errore apertura socket " + e);
			e.printStackTrace();
			return -1;
		}
		
		//inizializzazione campo di gioco
		DatiGraph datiGraph = new DatiGraph();
		System.err.println(idStampa+"-->Creazione campo di gioco");
		datiGraph.SetDatiGraph( posGraph.getListVertex(), null, posGraph.getListEdge(), infoGraph.getWidth(),
								infoGraph.getHeight(), infoGraph.getLarghezzaX(), infoGraph.getLarghezzaY(), 
								infoGraph.getDistanza(), infoGraph.getNVertex());
		mxGraph mxGraph = new mxGraph();
	    grafox  = new Grafox(datiGraph, mxGraph,listNomiCitta,mapGiocatoriInfo.get(username).getFazione(), errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal())));
	    System.err.println(idStampa+"-->Fine creazione campo di gioco");
		System.err.println(idStampa+"-----------------FineFaseInizializzazionePartita---------------");
		return 0;
		
	}
	/*
	 * Questa fase corrisponde alla vera e propria partita
	 * una volta qua, vuol dire che la partita è iniziata
	 * In questa fase viene gestito sia il turno del giocatore che 
	 * gli aggiornamenti creati dalle azioni degli altri giocatori.
	 * 
	 */
	private void Game()
	{
		final String idStampa="Game: ";
		System.err.println(idStampa+"---> InizioGioco");
		
		//Settaggio campo di battaglia
		formDiGioco = new Game(ipServer,portServerChat,socketChat,username,grafox,mapGiocatoriInfo.get(username).getPos(),mapGiocatoriInfo.get(username).getFazione(),listNomiCitta,errorCode,keyErrorCode);
		formDiGioco.upDateDatiPersonaliMunizioni(mapGiocatoriInfo.get(username).getPunteggio());
		formDiGioco.upDataCitta(grafox.adiecent(mapGiocatoriInfo.get(username).getPos()));
		
		//Questa lista serve come aiuto per il settaggio
		//dei giocatori presenti sulla medesima città 
		//del giocatore. Per diversificare la fazione 
		//verranno riportate le iniziali delle due fazioni
		List<String> lisGiocatori = new LinkedList<>();
		for (String user : mapGiocatoriInfo.keySet()) 
		{
			if(!user.equals(username) && mapGiocatoriInfo.get(user).getPos().equals(mapGiocatoriInfo.get(username).getPos()))
				lisGiocatori.add(user+" ["+mapGiocatoriInfo.get(user).getFazione().charAt(0)+mapGiocatoriInfo.get(user).getFazione().charAt(8)+"]");
		}
		formDiGioco.upDataGiocatori(lisGiocatori);
		formDiGioco.SetVisible();
		formDiGioco.SetDisable();
		
		//Per consentire un aggirnamento rapido e "cosatante" delle 
		//informazioni viene utilizzata un lista di thread i quali prendo
		//il task e lo eseguono insieme ad altre fasi del gioco.
		List<Thread> aggiornamnetiInCoda = new LinkedList<>();
		TGioco tGioco = null;
		TSfida tSfida = null;
		TAggiornamento tAggiornamento;
		
		while(!GameIsStop)
		{
			formDiGioco.setClassifica(mapGiocatoriInfo);
			System.err.println(idStampa+"attesa Connessione.....");
			try 
			{
				Socket  partitaSocket = socketServer.accept();
				//se sto giocando non ha senso pingare il server
				pingPong.NotifyPingPong();
				partitaSocket.setSoTimeout(TempoRispostaGame);	
				System.err.println(idStampa+"Connessione.....");
				try{
						ObjectOutputStream outStream =  new ObjectOutputStream(partitaSocket.getOutputStream());
						ObjectInputStream inStream = new ObjectInputStream(partitaSocket.getInputStream());
						System.err.println(idStampa+"Scambio di messaggi.....");
						String comando =(String)inStream.readObject();						
						
						if(comando.equals(keyErrorCode.get(SfidaIniziata.ordinal())))
						{
							tSfida = new TSfida(inStream, outStream,partitaSocket);
							formDiGioco.setFineTurno(false);
							tSfida.execute();
						}else
							if (comando.equals(keyErrorCode.get(ConfermaInizioAggironamento.ordinal())))
							{
								tAggiornamento = new TAggiornamento(inStream, outStream, partitaSocket);
								aggiornamnetiInCoda.add(tAggiornamento);
								aggiornamnetiInCoda.get(aggiornamnetiInCoda.size()-1).start();	
							}else
								if(comando.equals(keyErrorCode.get(ConfermaInizioTurno.ordinal())))
								{
									//prima di incominciare il turno si aspetta la terminazione 
									//degli aggiornamenti dei turni precedenti. Nel caso ci volesse 
									//troppo il turno inizia lo stesso notificando pero' l'errore								
									
									System.err.println(idStampa+"-->attesa fine aggiornamento");
									
									//variabile d'appoggio usata per 
									//la segnalazione di un errore in 
									//fase di aggiornamento
									errorUpdate = false;
									
									for (Thread thread : aggiornamnetiInCoda) 
									{
										try {
											thread.join(TemporizzatoreAttesaFineAggiornamento);
										} catch (InterruptedException e) {
											System.err.println(idStampa+"-->Errore durante l'aggiornamento!!Task cancellato "+e);
											errorUpdate=true;
										}
									}
									
									if(errorUpdate)
										JOptionPane.showMessageDialog(null,"Errore aggiornamento");
									else
										aggiornamnetiInCoda.clear();
									System.err.println(idStampa+"-->fine aggiornamento");
									//in alcuni casi viene interro il gioco... 
									//se inizia il turno e la sfida non è conclusa
									
									if (tSfida !=null && !tSfida.isDone() && !tSfida.isStop())
										throw new IllegalStateException("Il client vuole giocare durante una sfida");
									
									//o se il tuo turno è gia in corso
									if (tGioco !=null &&  !tGioco.isDone())
										throw new IllegalStateException("Il client vuole giocare due turni contemporaneamente");
									else
									{
										tGioco = new TGioco(inStream, outStream, partitaSocket);	
										tGioco.execute();						
									}
								}	
					} catch (IOException | ClassNotFoundException e ) {
						System.err.println(idStampa+"Errore aperturn in/out!!! " + e);
						e.printStackTrace();
					}			
			} catch (IOException e) {
				System.err.println(idStampa+"Errore accetta connessione!!!Chiusra Client!!!IDK (Possibile fine turno): "+e);
			}	
		}
		System.err.println(idStampa+"--->Fine Gioco");
		try {
			socketServer.close();
		} catch (IOException e) {
			System.err.println(idStampa+"Chiusura socket!!!! Errore");
		}
		
	}
		
	/*
	 * Questa finzione racchiude il cuore del programma.
	 * Richiama tutte le funzioni vitali del gioco ed 
	 * effettua alcuni controlli.
	*/
	public void Start()
	{
		final String idStampa="Start: ";
		//Il gioco puo partire solo se sono connesso
		//ed ho inizializzato tutti i servizi
		if (IsConnet && isPortCorrect)
		{
			//Errore di registrazione
			if(Registrazione()==-1)
			{
				StopGame();
				return;
			}
		
			try (BufferedWriter writeFile = new BufferedWriter(new FileWriter("logPorte.txt")))
			{

				writeFile.write(String.valueOf(port)+"\n");
				writeFile.write(String.valueOf(portChat)+"\n");
				writeFile.write(String.valueOf(portPingPong)+"\n");
				
			} catch (IOException e) {
				
				System.err.println(idStampa+"-->il file non esiste "+e);
			}
			
			pingPong.start();
			threadPingPong.start();
			
			waitGame = new WaitGame();
			waitGame.setVisibe();
			
			int result=inizializzazionePartita();
			if (result==ErroreInizializzazione.ordinal())
			{
				JOptionPane.showMessageDialog(null,errorCode.get(keyErrorCode.get(ErroreInizializzazione.ordinal())));
				waitGame.close();
				StopGame();
				return;
			}
			
			if(result==-1)
			{
				JOptionPane.showMessageDialog(null,errorCode.get(keyErrorCode.get(ErroreSconosciuto.ordinal())));
				waitGame.close();
				StopGame();
				return;
			}
			waitGame.close();
			Game();
			StopGame();
			formDiGioco.resetError();
			formDiGioco.upDateError1(errorCode.get(keyErrorCode.get(FinePartita.ordinal())));
		}
		else
		{
			System.err.println(idStampa+"-->ErroreCollegamentoColServer O ImpossibileAprireIServiziNecessari");
			JOptionPane.showMessageDialog(null,"ErroreCollegamentoColServer O ImpossibileAprireIServiziNecessari");
		}
		
		
	}
	
	
	//Questa funzione permette di chiudere tutti.
	//I servizi aperti dal client durante il gioco.
	//Viene chiama in caso di fine gioco (sia normale che non)
	private void StopGame()
	{
		final String idStampa="StopGame: ";
		System.err.println(idStampa+"--->STOP---GAME<---");		
		if (GameIsStop)
			return;
		
		if(formDiGioco!=null && formDiGioco.isActive())
			formDiGioco.stopGame();
		
		if(sfida!=null && sfida.isActive())
			sfida.close();
		
		try {
			GameIsStop=true;
			socketServer.close();
		} catch (IOException e) {
			System.err.println(idStampa+"errore chiusura socketServer");
		}


		if(threadPingPong.isAlive())
			try(Socket socketChiusura = new Socket("127.0.0.1", portPingPong)) {
				System.err.println(idStampa+"-->threadPingPong terminato");
			} catch (IOException e) {
				System.err.println(idStampa+"errore terminazione threadPingPong");
			}
	}
	
	
	//Questo servizio di pingPong server 
	//per capire se il server è online nelle 
	//fasi "morte" del gioco.
	class TPingPongClientToServer extends Thread
	{
		private boolean isConnect;
		private int timer;
		
		
		
		TPingPongClientToServer(int timer)
		{
			isConnect=true;
			this.timer = timer;
		}

		public boolean  isConnect() {
			return isConnect;
		}

		public synchronized void setConnect(boolean isConnect) {
			this.isConnect = isConnect;
		}
		
		//Questa funzione serve per interrompere il servizio
		public synchronized void NotifyPingPongEnd()
		{
			this.notify();
		}
		
		//Questa funzione serve per resettare il "contatore"
		//nel caso il server contatti per qualche motivo il client
		public synchronized void NotifyPingPong()
		{
			final String idStampa="PingPong,NotifyPingPong: ";
			isConnect=true;
			System.err.println(idStampa+"server online");
			this.notify();
		}
		
		@Override
		public synchronized void run()
		{
			final String idStampa="PingPong,run: ";
			System.err.println(idStampa+"--->INIZIO SERVIZIO PINGPONG<---");
			while(!GameIsStop)
			{
				while(isConnect)
				{
					isConnect=false;
					try {
						this.wait(timer);
					} catch (InterruptedException e) {
						System.err.println(idStampa+"Errore attera pingPong client");
					}
				}
				
				if(GameIsStop)
					return;
				
				try(Socket pingPong = new Socket(ipServer, portServerPingPong);
					ObjectInputStream inStreamPingPong = new ObjectInputStream(pingPong.getInputStream())	) 
				{
					pingPong.setSoTimeout(TempoPingPongAttesaLettura);
					String cod = (String)inStreamPingPong.readObject();
					if (cod.equals(keyErrorCode.get(ServerOnline.ordinal())))
					{
						System.err.println(idStampa+"ErrorCode "+errorCode.get(cod));
						isConnect=true;
					}
					else
					{
						System.err.println(idStampa+"server offline");
						StopGame();
					}
				} catch (Exception e) {
					System.err.println(idStampa+"server offline");
					StopGame();
				}

			}
			System.err.println(idStampa+"--->FINE SERVIZIO PINGPONG<---");
		}
	}
	
	
	//Questo servizio serve per eseguire un corretto pingPong col server.
	//In questo caso è il server che mi conttatta e tramite un analisi dei 
	//dati ricevuti capisco se è lui o meno.
	//in caso affermatico segnalo al server che sono online
	class TPingPongServerToClinet extends Thread 
	{
		private final String idStampa="ThreadPingPong,run: ";
		@Override
		public void run() 
		{
			System.err.println(idStampa+"--->INIZIO SERVIZIO THREADPINGPONG<---");
			while(!GameIsStop)
			{
				try (Socket temp = socketPingPong.accept())
				{		
					if(Debug)
					{
						System.err.println(idStampa+"-----> ipConnessione :	 "+temp.getInetAddress());
						System.err.println(idStampa+"-----> ipServer      :	 "+ipServer);
					}
					if (String.valueOf(temp.getInetAddress()).substring(1).equals(ipServer))
					{
						try(ObjectOutputStream outStreamPingPong = new ObjectOutputStream(temp.getOutputStream()))
						{
							outStreamPingPong.writeObject(keyErrorCode.get(ClientOnline.ordinal()));
							Thread.sleep(TemporizzatoreThreadPingPong);
						}catch (IOException | InterruptedException e){
							System.err.println(idStampa+"errore outStreamPingPong  " + e);
						}
					}
				} catch (IOException e) {
				System.err.println(idStampa+"errore socketPingPong " + e);
				}
			}
			System.err.println(idStampa+"--->FINE SERVIZIO THREADPINGPONG<---");
		}
	}
	
	
	
	//Le fasi del gioco sono divise in tre diversi thread per permette una 
	//maggiore velocita nella fase del gioco
	
	//Questo thread si occupa della gestione del turno del giocatore;
	//si occupa di inizializzare e controllare le oppurtune finestre di gioco
	//e dell'opportuna comunicazione col server.
	//Durante questa fase solo il proprietario del turno puo parlare con server in
	//modo attivo a meno che non scrivi in chat.
	class TGioco extends SwingWorker<String, String>
	{
		private ObjectInputStream inStreamTGioco;
		private ObjectOutputStream outStreamTGioco;
		private Socket partitaSocketTGioco;
		private final String idStampa = "TGioco,doInBackground: ";
		
		public TGioco(ObjectInputStream inStream,ObjectOutputStream outStream,Socket partitaSocket)
		{
			this.inStreamTGioco=inStream;
			this.outStreamTGioco=outStream;
			this.partitaSocketTGioco=partitaSocket;
		}
		
		@Override
		protected String doInBackground() throws Exception 
		{
			try
			{
				formDiGioco.resetError();
				System.err.println(idStampa+"--->"+errorCode.get(keyErrorCode.get(InizioTurno.ordinal()))+" : "+username);
				formDiGioco.upDateError1(errorCode.get(keyErrorCode.get(InizioTurno.ordinal()))+username);
				formDiGioco.SetEnable();
				System.out.println((String)inStreamTGioco.readObject());
				formDiGioco.SetOutClient(outStreamTGioco);
				String comando;
				
				//Esecuzione del turno
				while ( !( comando =(String)inStreamTGioco.readObject()).equals((keyErrorCode.get(ConfermaFineTruno.ordinal())) ))
				{	
					if(comando.equals("Giocatore non è nella tua stessa citta"))
					{
						formDiGioco.upDateError1("Giocatore non è nella tua stessa citta\n");
						formDiGioco.setFineTurno(true);
						sfida.close();
					}
					else
						if(comando.equals(keyErrorCode.get(SfidaIniziata.ordinal())))
						{
							sfida = new Battle(mapGiocatoriInfo.get(username).getPunteggio(), outStreamTGioco,errorCode,keyErrorCode,username,TemporizzatoreMossaAutomatica);
							formDiGioco.setFineTurno(false);
							sfida.setVisible(true);
							try
							{
								partitaSocketTGioco.setSoTimeout(TempoSfida);
								while (!( comando =(String)inStreamTGioco.readObject()).equals((keyErrorCode.get(FineSfida.ordinal())) ))
								{
									System.err.println(idStampa+"ComandoRicevto -->"+comando);
									formDiGioco.upDateError1(errorCode.get(comando));
									if( !comando.equals(keyErrorCode.get(HaiVinto.ordinal())) && !comando.equals(keyErrorCode.get(HaiPerso.ordinal())) && !comando.equals(keyErrorCode.get(PareggioFineMosse.ordinal())))
										sfida.EnableAll();
									else
										publish(comando);
									
								}
								System.err.println(idStampa+"ComandoRicevto -->"+comando);
							}catch (IOException | ClassNotFoundException e){
								System.err.println(idStampa+"sfida attaccante" + e);
							}finally {
								formDiGioco.upDateError1(errorCode.get(keyErrorCode.get(FineSfida.ordinal())));
								formDiGioco.setFineTurno(true);
								sfida.close();
							}		
						}
					
					//gestione altri comandi
					if (comando.equals(keyErrorCode.get(ConfermaComandoRicevuto.ordinal())) )
					{
						if(Debug)
							System.err.println(idStampa+mapGiocatoriInfo.get(username).getPos());
						if(!mapGiocatoriInfo.get(username).getPos().equals(formDiGioco.NewPosCitta()))
						{
							mapGiocatoriInfo.get(username).setPos(formDiGioco.NewPosCitta());
							if(Debug)
								System.err.println(idStampa+mapGiocatoriInfo.get(username).getPos());
							System.err.println(idStampa+"comando: "+comando);
							List<String> app = new LinkedList<>();
							for (String user : mapGiocatoriInfo.keySet()) 
							{
								if(!user.equals(username) && 
									mapGiocatoriInfo.get(user).getPos().equals(mapGiocatoriInfo.get(username).getPos()))
										app.add(user+" ["+mapGiocatoriInfo.get(user).getFazione().charAt(0)+mapGiocatoriInfo.get(user).getFazione().charAt(8)+"]");
							}
							formDiGioco.upDataGiocatori(app);
							if(Debug)
								System.err.println(idStampa+mapGiocatoriInfo.get(username).getPos());
						}
					}
				}
				
				
				System.err.println(idStampa+"ComandoRicevto -->"+errorCode.get(comando));
				inStreamTGioco.close();
				outStreamTGioco.close();
				partitaSocketTGioco.close();
				formDiGioco.SetDisable();
				
			}catch(IOException | ClassNotFoundException e){
				System.err.println(idStampa+"Errore Lettura Comando Mossa/Aggiornamento!!!" + e);		
			}
			finally 
			{
				if(!GameIsStop)
				{
					formDiGioco.SetDisable();
					formDiGioco.upDateError1(errorCode.get(keyErrorCode.get(ComandoFineTurno.ordinal())));
				}
			}
			return null;
		}
		
		@Override
		protected void process(List<String> chunks) {
				JOptionPane.showMessageDialog(null,errorCode.get(chunks.get(0)));	
		}
		
	}
		
		
	//Questo thread viene utilizzato per la gestione della sfida da parte di "sfidato"
	//in questo modo durante la sfida posso comunque ricere aggiornamenti da parte del server
	class TSfida extends SwingWorker<String, String>
	{
		
		ObjectInputStream inStreamTSfida;
		ObjectOutputStream outStreamTSfida;
		Socket partitaSocketTSfida;
		private final String idStampa="TSfida,doInBackground: ";
		
		public TSfida(ObjectInputStream inStream,ObjectOutputStream outStream,Socket partitaSocket)
		{
			this.inStreamTSfida=inStream;
			this.outStreamTSfida=outStream;
			this.partitaSocketTSfida=partitaSocket;
		}
		
		public boolean isStop() {
			return sfida.isClose();
		}
		
		@Override
		protected String doInBackground() throws Exception {
			String comando;
			sfida = new Battle(mapGiocatoriInfo.get(username).getPunteggio(), outStreamTSfida,errorCode,keyErrorCode,username,TemporizzatoreMossaAutomatica);
			sfida.setVisible(true);
			try{
				partitaSocketTSfida.setSoTimeout(TempoSfida);
				while (!( comando =(String)inStreamTSfida.readObject()).equals((keyErrorCode.get(FineSfida.ordinal())) ))
				{
					System.err.println(idStampa+"ComandoRicevto -->"+comando);
					formDiGioco.upDateError1(errorCode.get(comando));
					if( !comando.equals(keyErrorCode.get(HaiVinto.ordinal())) && !comando.equals(keyErrorCode.get(HaiPerso.ordinal())) && !comando.equals(keyErrorCode.get(PareggioFineMosse.ordinal())) )
						sfida.EnableAll();
					else
						publish(comando);
				}
				System.err.println(idStampa+"ComandoRicevto -->"+comando);
				formDiGioco.upDateError1(errorCode.get(comando));
				inStreamTSfida.close();
				outStreamTSfida.close();
				partitaSocketTSfida.close();
			}catch(IOException | ClassNotFoundException e){
				System.err.println(idStampa+"Sfida sfidante" + e);
			}finally {
				sfida.close();
				formDiGioco.upDateError1(errorCode.get(keyErrorCode.get(FineSfida.ordinal())));
			}
			
			return null;
		}
		
		@Override
		protected void process(List<String> chunks) {
			JOptionPane.showMessageDialog(null,errorCode.get(chunks.get(0)));		
		}
	}
	
	
	//Questo thread gestisce tutti gli aggiornamenti durante la partita
	//In questo modo posso aggiornare piu cose contemporaneamente ed 
	//avere una migliore concorrenza.
	//Anche se piu aggiornamenti accedono alle stessa strutture dati
	//lavorano su dati diversi e quindi possiamo sfruttare al piento 
	//la concorrenza.
	
	class TAggiornamento extends Thread
	{
		ObjectInputStream inStreamTAggiornamento;
		ObjectOutputStream outStreamTAggiornamento;
		Socket partitaSocketTAggiornamento;
		private final String idStampa="TAggiornamento,run: ";
		public TAggiornamento(ObjectInputStream inStream,ObjectOutputStream outStream,Socket partitaSocket)
		{
			this.inStreamTAggiornamento=inStream;
			this.outStreamTAggiornamento=outStream;
			this.partitaSocketTAggiornamento=partitaSocket;
		}
		
		@Override
		public void run() 
		{
			formDiGioco.upDateError1(errorCode.get(keyErrorCode.get(Aggiornamento.ordinal())));
			String aggiornamento;
			try {
				aggiornamento = (String)inStreamTAggiornamento.readObject();
				System.err.println(idStampa+"Aggiornamento -->" + aggiornamento);
				
				//variabile di appoggio per analizzare il contenuto dell'aggiornamento
				String[] temp = aggiornamento.split(",");
				
				if(temp[0].equals(keyErrorCode.get(ComandoMossa.ordinal())))
				{
					formDiGioco.spegniIcona(mapGiocatoriInfo.get(temp[2]).getFazione(), mapGiocatoriInfo.get(temp[2]).getPos());
					
					if(mapGiocatoriInfo.get(temp[2]).getPos().equals(mapGiocatoriInfo.get(username).getPos()))
						formDiGioco.upDataGiocatoriTogli(temp[2]+" ["+mapGiocatoriInfo.get(temp[2]).getFazione().charAt(0)+mapGiocatoriInfo.get(temp[2]).getFazione().charAt(8)+"]");
					
					mapGiocatoriInfo.get(temp[2]).setPos(temp[1]);
					
					formDiGioco.accendiIncona(mapGiocatoriInfo.get(temp[2]).getFazione(),  mapGiocatoriInfo.get(temp[2]).getPos());
					
					if(mapGiocatoriInfo.get(temp[2]).getPos().equals(mapGiocatoriInfo.get(username).getPos()))
						formDiGioco.upDataGiocatoriAggiungi(temp[2]+" ["+mapGiocatoriInfo.get(temp[2]).getFazione().charAt(0)+mapGiocatoriInfo.get(temp[2]).getFazione().charAt(8)+"]");
					
					formDiGioco.upDateError1(temp[2]+errorCode.get(keyErrorCode.get(Spostamento.ordinal()))+temp[1]);
				}else
					if(temp[0].equals(keyErrorCode.get(ComandoAttacco.ordinal())))	
						formDiGioco.upDateError1(temp[2]+errorCode.get(keyErrorCode.get(Attaccamento.ordinal()))+temp[1]);
					else
						if(temp[0].equals(keyErrorCode.get(SfidaRicarico.ordinal())))
						{				
							if(temp[2].equals(username))
							{
								if(temp.length == 4)
								{
									if(Integer.parseInt(temp[1])==0)
										JOptionPane.showMessageDialog(null,"Il brutto ti ha rubato "+temp[3]+" munizioni");	
									else
										JOptionPane.showMessageDialog(null,"Il brutto ti ha rubato "+temp[3]+" munizioni\n ma ne hai trovate "+temp[1]);	
									mapGiocatoriInfo.get(temp[2]).upDatePunteggio(Integer.parseInt(temp[1])-Integer.parseInt(temp[3]));
								}
								else
								{
									JOptionPane.showMessageDialog(null," Hai trovato "+temp[1]+" munizioni");	
									mapGiocatoriInfo.get(temp[2]).upDatePunteggio(Integer.parseInt(temp[1]));
								}
								formDiGioco.upDateDatiPersonaliMunizioni(mapGiocatoriInfo.get(username).getPunteggio());
							}
							else
							{
								if(temp.length == 4)
									mapGiocatoriInfo.get(temp[2]).upDatePunteggio(Integer.parseInt(temp[1])-Integer.parseInt(temp[3]));
								else
									mapGiocatoriInfo.get(temp[2]).upDatePunteggio(Integer.parseInt(temp[1]));	
							}
							formDiGioco.setClassifica(mapGiocatoriInfo);
						}else
							if(temp[0].equals(keyErrorCode.get(SfidaPunteggiAggiornati.ordinal())))
							{								
								mapGiocatoriInfo.get(temp[1]).setPunteggio(Integer.parseInt(temp[2]));
								mapGiocatoriInfo.get(temp[3]).setPunteggio(Integer.parseInt(temp[4]));
								formDiGioco.upDateDatiPersonaliMunizioni(mapGiocatoriInfo.get(username).getPunteggio());
								formDiGioco.setClassifica(mapGiocatoriInfo);
							}else
								if(temp[0].equals(keyErrorCode.get(ClientOffline.ordinal())))
								{
									formDiGioco.LockClassifica();
									mapGiocatoriInfo.remove(temp[1]);
									formDiGioco.UnLockClassifica();
									formDiGioco.setClassifica(mapGiocatoriInfo);
								}else
									if( temp[0].contains(keyErrorCode.get(FinePartita.ordinal())) )
									{
										CloseGame chiusuraGioco = new CloseGame("\n"+temp[1]+temp[2]);
										chiusuraGioco.SetVisible();						
										StopGame();
										pingPong.NotifyPingPongEnd();
									}
								
				inStreamTAggiornamento.close();
				outStreamTAggiornamento.close();
				partitaSocketTAggiornamento.close();
			} catch (ClassNotFoundException | IOException e) {
				System.err.println(idStampa+"Errore fase di aggiornamento " +e);
			}
			
		}
	}
	
}




