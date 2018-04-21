package Server;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.javafx.print.Units;

import ServerClient.InfoGiocatore;
import ServerClientGraph.InfoGraph;
import ServerClientGraph.PosGraph;
import ServerClientGraph.Vertex;
import ServerGraph.CreatePosGUI;

import static  ServerClient.EnumKeyErrorCode.*;
import static  Server.ErrorCodeEnum.*;

public class Server {
	
	//Gestione registrazione
	private ThreadPoolExecutor poolResgistrazione;
	//Gestione aggiornamento della partita
	private ThreadPoolExecutor poolAggiornamento;
	//Gestione aggiornamento chat
	private ThreadPoolExecutor poolChat;
	
	//Gestione Connessione Registrazione
	private ServerSocket socketConnessione;
	//Gestione Connessine Chat
	private ServerSocket socketChat;
	
	private ServerSocket socketPingPong;
	
	//Blocca le liste dei giocatori
	private ReentrantLock LockListBuoniCattivi = new ReentrantLock();
	//Blocca l'hashMap dei giocatori
	private ReentrantLock LockMapGiocatori = new ReentrantLock();
	//Blocca l'hashMap dei giocatori info
	private ReentrantLock LockMapGiocatoriInfo = new ReentrantLock();
	//Blocca l'inizio della partita, utilizzato come sincronizzatore
	private ReentrantLock lockInizioGame = new ReentrantLock();
	//Condizione utilizzate per la sincronizzazione
//private Condition conInizioGame = lockInizioGame.newCondition();
	//Blocca le modifiche ai pool (forse per essere piu concorrenti meglio un pool un lock)
	private ReentrantLock lockPool = new ReentrantLock();
//--controllare i lock su mapInfo
	//Memorizza tutte le informazioni del giocatore (Della connessione) associando l'username alla propria struttura dati
	private ConcurrentHashMap<String, Giocatore> mapGiocatori;
	//Memorizza tutte le informazioni del giocatore (Del gioco) associando l'username alla propria struttura dati
	private ConcurrentHashMap<String, InfoGiocatore> mapGiocatoriInfo;
	
	private CreatePosGUI grafo;
	private PosGraph posGraph;
	private Munizioni threadMunizioni;
	private TimerRegistrazioene timerRegistrazioene;
	private ThreadPingPong threadPingPong;
	private InfoGraph infoGraph;
	
	//Memorizza l'username dei giocatori che appartengono a questa fazione
	private ConcurrentLinkedQueue<String> buoni = new ConcurrentLinkedQueue<>();
	//Memorizza l'username dei giocatori che appartengono a questa fazione
	private ConcurrentLinkedQueue<String> cattivi= new ConcurrentLinkedQueue<>();
	
	
	//Tempi di gestion delle farie fasi di gioco
	private int TempoIscrizione=120000; //tempo di iscrizione (Quanto tempo il socket resta "aperto") (non funziona)
	private int NControlli=8; //tempo riattivazione thread di controllo inizio partita
	private int TempoRegistrazione=60000; // tempo disponibile per inviare i dati al server durante la fase di iscrizione
	private int TempoDiGioco=120000;// durata di una partita 
	private int TempoChat=500;// tempo di attera dei messaggi del client
	private int TempoDurataTurno=6000;//tempo durata turno
	private int TempoCreazioneMunizioni=5000;
	private int TempoDiInizializzazione=1000; 
	private int TempoMunizioni=200000; //tempo permanenza munizioni sul nodo
	
	private int TemporizzatoreAggiornamentoComando=1000;
	private int TemporizzatoreAggiornamentoChat=500;
	private int TemporizzatoreSfida=600;
	private int TempoDiSfida=6500;
	private int TempoPingPongLettura=2000;
	private int MaxGiocatori = 4;
	private int MinGiocatori = 4;
	
	//definisce se il gioco è iniziato o no, utile per sapere se un client
	//si puo registrare o meno
	private boolean gameIsStart = false;
	//definisce la fine del gioco, utile per chiudere tutte la fasi o controlli del gioco
	private boolean IsStop=false;
	//definisce il turno facendo alternare le due squadre (da fare randomico)
	private boolean turnoEvil;
	//definisce se il server è online, utile per l'avvio dei servizzi
	private boolean isOnLineServer = false;
	//definisce se il client che sto contattando è on o off
	//utile per la gestione dei client durante la partita
	
	//memorizza la posizione del brutto
	private String posizione;
	
	//contiene tutti i codici degli errori
	private List<String> keyErrorCode;
	
	//contiene l'associazione tra codici e significato degli errori
	private ConcurrentHashMap<String,String> errorCode;

	private Random randomGenerator = new Random();
	
	private float perditaMunizioniSfida=0.3f;
	private float perditaMunizioniBrutto= 0.3f;
	List<String> tempListCitta;
	
	ReentrantLock LockGame = new ReentrantLock();
	List<Future> futureTaskRiconnessione = new LinkedList<>();
	
	
	private final boolean Debug = false;
	private final String ClassName = this.getClass().getName();
	
	private void SeKeyErrorCode()
	{
		keyErrorCode = new LinkedList<>();
		keyErrorCode.add("InizioRegistrazione");				
		keyErrorCode.add("ConnessioneServer");					
		keyErrorCode.add("RiconnessioneServer");				
		keyErrorCode.add("FazioneBuoni");						
		keyErrorCode.add("FazioneCattivi");						
		keyErrorCode.add("ServerPieno");			
		
		keyErrorCode.add("ErroreDatiConnessione");				
		keyErrorCode.add("ErroreLetturaDatiDiRegistrazione");	
		keyErrorCode.add("ErroreLetturaDatiDiRiconnesione");
		keyErrorCode.add("ErroreUsernameGiaPresente");	
		keyErrorCode.add("ErroreFaziniSquilibrate");		
		keyErrorCode.add("ErroreRiconnessione");		
		keyErrorCode.add("ErroreCampiRegistrazioneVuoti");		
		keyErrorCode.add("ErroreClientOnline");		
	
		
		keyErrorCode.add("ConfermaAvvenutaRegistrazione");		
		keyErrorCode.add("ConfermaAvvenutaRiconnessione");		
		keyErrorCode.add("ConfermaTempoRegistrazioneScaduto");	
		
		keyErrorCode.add("GiocoIniziato");				
		keyErrorCode.add("FinePartita");						
		keyErrorCode.add("ClientOffline");		

		
		keyErrorCode.add("InizioTurno");						
		keyErrorCode.add("ConfermaInizioTurno");				
		keyErrorCode.add("ConfermaInizioAggironamento");		
		keyErrorCode.add("ConfermaAggiornamentoRicevuto");	
		keyErrorCode.add("ConfermaComandoRicevuto");			
		keyErrorCode.add("ConfermaFineTruno");				
		
		keyErrorCode.add("ComandoMossa");						
		keyErrorCode.add("ComandoAttacco");						
		keyErrorCode.add("ComandoFineTurno");					
		
		keyErrorCode.add("SfidaIniziata");						
		keyErrorCode.add("FineSfida");							
		keyErrorCode.add("SfidaAttacco");						
		keyErrorCode.add("SfidaRicarico");							
		keyErrorCode.add("SfidaDifesa");					
		keyErrorCode.add("SfidaPareggio");							
		keyErrorCode.add("SfidaPunteggiAggiornati");			
		
		
		keyErrorCode.add("HaiPerso");							
		keyErrorCode.add("HaiVinto");							
		keyErrorCode.add("PareggioFineMosse");					
		

		keyErrorCode.add("ServerNonRisponde");
		keyErrorCode.add("ConnessioneAssente");
		keyErrorCode.add("ErroreStessaFazione");
		keyErrorCode.add("Aggiornamento");
		keyErrorCode.add("Spostamento");
		keyErrorCode.add("Attaccamento");
		keyErrorCode.add("Munizioni");
		keyErrorCode.add("NumDifesa");
		keyErrorCode.add("NumRicaricate");
		keyErrorCode.add("ErroreInizializzazione");
		keyErrorCode.add("ErroreSconosciuto");
		
		keyErrorCode.add("ClientOnline");
		keyErrorCode.add("ServerOnline");
	}
	
	private void SetErrorCode()
	{
		errorCode = new ConcurrentHashMap<>();
		
		errorCode.put(keyErrorCode.get(ErroreDatiConnessione.ordinal()), "ErroreLetturaDatiConnessione");
		errorCode.put(keyErrorCode.get(ErroreLetturaDatiDiRegistrazione.ordinal()), "ErroreLetturaDatiRegistrazione");
		errorCode.put(keyErrorCode.get(ErroreLetturaDatiDiRiconnesione.ordinal()), "ErroreLetturaDatiRiconnessione");
		errorCode.put(keyErrorCode.get(ErroreUsernameGiaPresente.ordinal()), "Username gia presente");
		errorCode.put(keyErrorCode.get(ErroreFaziniSquilibrate.ordinal()), "Le fazioni non sono equilibrate!! cambia fazione!!");
		errorCode.put(keyErrorCode.get(InizioRegistrazione.ordinal()), "Start");
		errorCode.put(keyErrorCode.get(ConnessioneServer.ordinal()), "Connessione");
		errorCode.put(keyErrorCode.get(RiconnessioneServer.ordinal()), "Riconnessione");
		errorCode.put(keyErrorCode.get(ConfermaAvvenutaRegistrazione.ordinal()), "Ti sei registrato");
		errorCode.put(keyErrorCode.get(ConfermaAvvenutaRiconnessione.ordinal()), "Ti sei riconnesso");
		errorCode.put(keyErrorCode.get(ErroreRiconnessione.ordinal()), "Non ti sei riconnesso!! Campi inesistenti o sbalgiati");
		errorCode.put(keyErrorCode.get(ConfermaTempoRegistrazioneScaduto.ordinal()), "Tempo di registrazione scaduto");
		errorCode.put(keyErrorCode.get(ConfermaInizioTurno.ordinal()), "Gioca");
		errorCode.put(keyErrorCode.get(ConfermaInizioAggironamento.ordinal()), "Aggiornamento");
		errorCode.put(keyErrorCode.get(ConfermaAggiornamentoRicevuto.ordinal()), "AggiornamentoRicevuto");
		errorCode.put(keyErrorCode.get(ConfermaComandoRicevuto.ordinal()), "Comando Ricevuto");
		errorCode.put(keyErrorCode.get(ConfermaComandoRicevuto.ordinal()), "FineTurno");
		errorCode.put(keyErrorCode.get(ComandoMossa.ordinal()), "Mossa");
		errorCode.put(keyErrorCode.get(ComandoAttacco.ordinal()), "Attacco");
		errorCode.put(keyErrorCode.get(ComandoFineTurno.ordinal()), "FineTurno");
		errorCode.put(keyErrorCode.get(ErroreCampiRegistrazioneVuoti.ordinal()), "Campi vuoti!! L'username e la passoword sono sobligatori!!");
		errorCode.put(keyErrorCode.get(FazioneBuoni.ordinal()), "CappelliBianchi");
		errorCode.put(keyErrorCode.get(FazioneCattivi.ordinal()), "CappelliNeri");
		errorCode.put(keyErrorCode.get(ServerPieno.ordinal()), "ServerPieno");
		errorCode.put(keyErrorCode.get(GiocoIniziato.ordinal()), "GiocoIniziato");
		errorCode.put(keyErrorCode.get(InizioTurno.ordinal()), "Tocca a te: ");
		errorCode.put(keyErrorCode.get(SfidaIniziata.ordinal()), "SfidaIniziata");
		errorCode.put(keyErrorCode.get(SfidaAttacco.ordinal()), "SfidaAttacco");
		errorCode.put(keyErrorCode.get(SfidaRicarico.ordinal()), "Ricarico");
		errorCode.put(keyErrorCode.get(SfidaDifesa.ordinal()), "Difesa");
		errorCode.put(keyErrorCode.get(SfidaPareggio.ordinal()), "Pareggio ");
		errorCode.put(keyErrorCode.get(FineSfida.ordinal()), "FineSfida");
		errorCode.put(keyErrorCode.get(HaiPerso.ordinal()), "Hai Perso la sfida");
		errorCode.put(keyErrorCode.get(HaiVinto.ordinal()), "Hai Vinto la sfida");
		errorCode.put(keyErrorCode.get(PareggioFineMosse.ordinal()), "Hai Pareggiato la sfida");
		errorCode.put(keyErrorCode.get(ClientOffline.ordinal()), "Giocatore offline");
		errorCode.put(keyErrorCode.get(SfidaPunteggiAggiornati.ordinal()), "PuntrggiAggiornati");
		errorCode.put(keyErrorCode.get(FinePartita.ordinal()), "FinePartita");
		errorCode.put(keyErrorCode.get(ServerNonRisponde.ordinal()), "Il Server Non Risponde");
		errorCode.put(keyErrorCode.get(ConnessioneAssente.ordinal()), "Non Sei Connesso Ad Alcun Server");
		errorCode.put(keyErrorCode.get(ErroreFaziniSquilibrate.ordinal()),"Fazioni Squilibrate!! Cambia Fazione Per Registrarti");
		errorCode.put(keyErrorCode.get(Aggiornamento.ordinal()), "Inizio Fase Aggiornamento");
		errorCode.put(keyErrorCode.get(Spostamento.ordinal()), " si sposta ");
		errorCode.put(keyErrorCode.get(Attaccamento.ordinal()), " attacca ");
		errorCode.put(keyErrorCode.get(ErroreStessaFazione.ordinal()), "Giocatore selezionato appartiene alla tua stessa squadra");
		errorCode.put(keyErrorCode.get(Munizioni.ordinal()), "Munizioni Disponibili");
		errorCode.put(keyErrorCode.get(NumDifesa.ordinal()),"Divese a disposizioni");
		errorCode.put(keyErrorCode.get(NumRicaricate.ordinal()), "Munizioni nel caricatore");
		errorCode.put(keyErrorCode.get(ErroreClientOnline.ordinal()), "Il Client è Online; Non puoi effettuare la riconnessione");
		errorCode.put(keyErrorCode.get(ErroreInizializzazione.ordinal()), "Errore caricamento dati, riavviare il gioco e riconnettersi");
		errorCode.put(keyErrorCode.get(ErroreSconosciuto.ordinal()), "Errore sconoscito, riavviare il gioco");
		errorCode.put(keyErrorCode.get(ClientOnline.ordinal()), "Il client pingato è online");
		errorCode.put(keyErrorCode.get(ServerOnline.ordinal()), "Il server pingato è online");
	}
	
	
	public Server (int count)
	{
		final String PrintId=ClassName+","+ClassName+" ";
 		try {
 			//Apertura servizzi
			socketConnessione = new ServerSocket(9000);
			socketChat = new ServerSocket(9001);
			socketPingPong = new ServerSocket(9002);
			System.err.println(PrintId+"******Server on********");
			isOnLineServer=true;
		} catch (IOException e) {
			System.err.println(PrintId+"Server gia online!!!O c'e un servizio attivo sulla porta 9000 o 9001 0 9002 " + e);
			return;
		}
 		
 		//definizione delle strutture dati
		mapGiocatori = new  ConcurrentHashMap<>();
		mapGiocatoriInfo = new ConcurrentHashMap<>();
		timerRegistrazioene = new TimerRegistrazioene();
		
		//definizione dei pool (magari a senso dimensioni diverse)
		poolResgistrazione = (ThreadPoolExecutor) Executors.newFixedThreadPool(count);
		poolAggiornamento = (ThreadPoolExecutor) Executors.newFixedThreadPool(count);
		poolChat = (ThreadPoolExecutor) Executors.newFixedThreadPool(count);
				
		turnoEvil=(1==randomGenerator.nextInt(1));//turni casuale fra buoni e cattivi
		
		threadPingPong = new ThreadPingPong();
		threadPingPong.start();
		SeKeyErrorCode();
		SetErrorCode();
		infoGraph = new InfoGraph();
	}
	
	/*
	 * In questa fase, un client invia le informazioni al server; le controlla
	 * e stabilisce se vanno bene o meno. Con un opportuno protocollo scambia 
	 * messaggi con client e gli segnala gli errori. La registrazione termina 
	 * in caso di errori non "gestibili", di fine tempo o di iscrizione avvenuta
	 */
	private ErrorCodeEnum Registrazione(Socket clientSocket,ObjectInputStream inStreamRegistrazione, ObjectOutputStream outStreamRegistrazione )
	{

		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";
		//variabili d'appoggio per la memorizzazione dei dati
		String inputStreamUserName = null;
		String inputStreamPassword = null;
		String inputStreamFazione = null;
		String inputStreamPorta = null; 
		String inputStreamPortaChat = null; 
		String inputSteamPortaPingPong = null;
		System.err.println(PrintId+"-----------------InizioFaseRegistrazione-------------------");
		
		//dichiarazione strutture
		Giocatore giocatore = new Giocatore();
		
		try{
			
			System.err.println(PrintId+"-----------------LetturaDati-------------------");
			inputStreamUserName=(String)inStreamRegistrazione.readObject();
			inputStreamPassword=(String)inStreamRegistrazione.readObject();			
			inputStreamFazione = (String)inStreamRegistrazione.readObject();			
			inputStreamPorta = (String)inStreamRegistrazione.readObject();		
			inputStreamPortaChat = (String)inStreamRegistrazione.readObject();
			inputSteamPortaPingPong=(String)inStreamRegistrazione.readObject();
			System.err.println(PrintId+"-----------------FineLetturaDati-------------------");
			
		} catch (ClassNotFoundException | IOException e){
			System.err.println(PrintId+"Errore Lettura Registrazione!!! Registrazione Finita!! "+e);
			try {
				outStreamRegistrazione.writeObject(keyErrorCode.get(ErroreLetturaDatiDiRegistrazione.ordinal()));
			} catch (IOException e1) {
				//se non riesco a notificare al client l'errore, che il time out 
				//del client e del server che fa resettare la comunicazione
				System.err.println(PrintId+"Errore notifica errore lettura!! "+e1);
			}
			return ErroreRegistrazioneRestart;
		}
		//Controllo fazioni corrette
		if(!inputStreamFazione.equals(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal()))) && !inputStreamFazione.equals(errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal()))) )
			throw new IllegalArgumentException("La fazione indicata è inesistente!! Questo non doveva succedere!!");
		
		//Questa sezione di codice, è gestita in muta esclusione
		//poiche devo garantire l'unicita dell'username
		//il primo che arriva qua si aggiudica username scelto
		LockMapGiocatori.lock();
		//Controllo la disponibilita dell'username
		if (mapGiocatori.containsKey(inputStreamUserName))
		{	
			//se non disponibile....
			LockMapGiocatori.unlock();
			try {
				outStreamRegistrazione.writeObject(keyErrorCode.get(ErroreUsernameGiaPresente.ordinal()));
				System.err.println(PrintId+"NickName già presente");
			} catch (IOException e1) {
				//se non riesco a notificare al client l'errore, che il time out 
				//del client e del server che fa resettare la comunicazione
				System.err.println(PrintId+errorCode.get(keyErrorCode.get(ErroreUsernameGiaPresente.ordinal())) +" "+ e1);
			}
			return  ErroreRegistrazioneRestart;
		}	
		//Se disponibile
		if(mapGiocatori.size()>=MaxGiocatori)
		{
			LockMapGiocatori.unlock();
			try {
				System.err.println(PrintId+keyErrorCode.get(ServerPieno.ordinal()));
				outStreamRegistrazione.writeObject(keyErrorCode.get(ServerPieno.ordinal()));		
			} catch (IOException e) {
				System.err.println(PrintId+"Errore segnalazione server pieno "+e);
			}
			return  ErroreFineRegistrazione;
		}
		
		if (mapGiocatori.putIfAbsent(inputStreamUserName, giocatore)!=null) 
		{//se entro in questo if gestisco male la concorrenza
			LockMapGiocatori.unlock();
			throw new IllegalArgumentException("la concorrenza non è gestita bene, in nick name è presente e non doveva succedere");
		}
		
		
		//Questa sezione di codice è gestita in muta escusione 
		//poiche devo garantire l'equilibio tra le due fazioni
		//"l'equilibrio" puo essere regolato a piacimento 
		//per ora la differenza non puo essere maggiore di uno
		LockListBuoniCattivi.lock();	
		if ( (inputStreamFazione.equals(errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal()))) &&  cattivi.size() > buoni.size() ) || (inputStreamFazione.equals(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal()))) && cattivi.size() < buoni.size() ) )
		{
			//se fazioni squilibrate
			mapGiocatori.remove(inputStreamUserName);
			LockMapGiocatori.unlock();
			LockListBuoniCattivi.unlock();
			try {
				outStreamRegistrazione.writeObject(keyErrorCode.get(ErroreFaziniSquilibrate.ordinal()));
				System.err.println(PrintId+errorCode.get(keyErrorCode.get(ErroreFaziniSquilibrate.ordinal())));
			} catch (IOException e) {
				System.err.println(PrintId+"Errore segnalazione errore fazione squilibrate "+e);
			}
			return  ErroreRegistrazioneRestart;
		}	
		
		if(inputStreamFazione.equals(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal()))))
			buoni.add(inputStreamUserName);
		else
			cattivi.add(inputStreamUserName);
		LockMapGiocatori.unlock(); 
		LockListBuoniCattivi.unlock();
		//se fazioni equilibrate
		
		InfoGiocatore infoGiocatore = new InfoGiocatore(0,null,false,inputStreamFazione);	
		//inserisco i dati del giocatore
		giocatore.setIp(String.valueOf(clientSocket.getInetAddress()).substring(1));
		giocatore.setPorta(Integer.parseInt(inputStreamPorta));
		giocatore.setUsername(inputStreamUserName);
		giocatore.setPassword(inputStreamPassword);
		giocatore.setFazione(inputStreamFazione);
		giocatore.setPortaChat(Integer.parseInt(inputStreamPortaChat));
		giocatore.setPortaPingPong(Integer.parseInt(inputSteamPortaPingPong));
		if(Debug)
		{
			System.err.println(PrintId+"server reciver user***"+inputStreamUserName);
			System.err.println(PrintId+"server reciver pass ***"+inputStreamPassword);
			System.err.println(PrintId+"server reciver fazine***"+inputStreamFazione);
			System.err.println(PrintId+"server reciver porta***"+inputStreamPorta);
			System.err.println(PrintId+"server reciver porta chat***"+inputStreamPortaChat);
			System.err.println(PrintId+"server reciver porta pingPogn***"+inputSteamPortaPingPong);
			System.err.println(PrintId+"server reciver ip***"+String.valueOf(clientSocket.getInetAddress()).substring(1));
		}
		//inserisco nell'hash map le informazioni ricevute
		//posso utilizzare una funzione non thread safe poichè
		//sono sicuro che tutti i nomi saranno diversi e gia presenti nell'hash
		mapGiocatori.put(inputStreamUserName, giocatore);
		if (mapGiocatoriInfo.putIfAbsent(inputStreamUserName, infoGiocatore)!=null)
			throw new IllegalArgumentException("la concorrenza non è gestita bene, in nick name è presente e non doveva succedere");
		
		if(Debug)
		{
			System.err.println(PrintId+"Port: "+mapGiocatori.get(inputStreamUserName).getPorta());
			System.err.println(PrintId+"Port chat: "+mapGiocatori.get(inputStreamUserName).getPortaChat());
			System.err.println(PrintId+"Port pingPong: "+mapGiocatori.get(inputStreamUserName).getPortaPingPong());
			System.err.println(PrintId+"indirizzo ip: "+mapGiocatori.get(inputStreamUserName).getIp());
			System.err.println(PrintId+"user: "+mapGiocatori.get(inputStreamUserName).getUsername());
			System.err.println(PrintId+"fazione: "+mapGiocatori.get(inputStreamUserName).getFazione());
			System.err.println(PrintId+"pw: "+mapGiocatori.get(inputStreamUserName).getPassword());
		}
		try{
			outStreamRegistrazione.writeObject(keyErrorCode.get(ConfermaAvvenutaRegistrazione.ordinal()));
		} catch (IOException e1){
			//se mi salta anche questa notifica, c'è il timeOut nel client che fa resettare tutto
			System.err.println(PrintId+"Errore notifica avvenuta registrazione!!! "+e1);
		}
		System.err.println(PrintId+"---------------FineFaseRegistrazione---------------------");
		return RegistrazioneAvvenutaConSuccesso;
	}

	
	/*
	 * In questa fase un client che si era gia connesso prova a riconnetterisi
	 * se durante la partita e "morto". L'obbiettivo di questa fase è ricollegare 
	 * il giocatore e ritrasmettergli le informazioni necessarie per il gioco.
	 * per le dinameche del gioco, potrebbe essere che al primo turno
	 * qualcosa resti sballato ma non dovrebbe mai succedere.
	 * questa funzione aggionra l'ip e le porte per contattarlo. Se la riconnessione riesce 
	 * allora vengono inviate al client le informazioni per poter giocare di nuovo
	 */
	private ErrorCodeEnum Riconnessione(Socket clientSocket,ObjectInputStream inRiconnessione, ObjectOutputStream outRiconnessione )
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";
		
		System.err.println(PrintId+"---------------InizioFaseRiconnessione---------------------");
		String inputStreamUserName = null;
		String inputStreamPassWord = null;
		String inputStreamPorta = null;
		String inputStreamPortaChat = null;
		String inputSteamPortaPingPong = null;
		
		try{
			System.err.println(PrintId+"-----------------LetturaDati-------------------");
			inputStreamUserName=(String)inRiconnessione.readObject();
			inputStreamPassWord=(String)inRiconnessione.readObject();
			inputStreamPorta=(String)inRiconnessione.readObject();
			inputStreamPortaChat=(String)inRiconnessione.readObject();
			inputSteamPortaPingPong=(String)inRiconnessione.readObject();
			System.err.println(PrintId+"-----------------FineLetturaDati-------------------");
		}	
		catch (ClassNotFoundException | IOException e){
			System.err.println(PrintId+errorCode.get(keyErrorCode.get(ErroreLetturaDatiDiRegistrazione.ordinal()))+" "+e);
			try {
				outRiconnessione.writeObject(keyErrorCode.get(ErroreLetturaDatiDiRegistrazione.ordinal()));
			} catch (IOException e1) {
				//se mi salta anche questa notifica, c'è il timeOut nel client che fa resettare tutto
				System.err.println(PrintId+"Errore notifica errore Riconnessione!! "+e1);
			}
			return ErroreRegistrazioneRestart;
		}
		/*
		 * Questa parte di codice viene gestita in 
		 * muta esclusione a causa della possibile
		 * rimozione del client.
		 * Ambo le strutture sono a rischio
		 */
		LockMapGiocatori.lock();	
		LockMapGiocatoriInfo.lock();
		//controllo presenza giocatore e offline
		if(Debug)
		{
			System.err.println(PrintId+"U----->"+inputStreamUserName);
			System.err.println(PrintId+"PWR----->"+ mapGiocatori.get(inputStreamUserName).getPassword());
			System.err.println(PrintId+"PW----->"+inputStreamPassWord);
			System.err.println(PrintId+"----->"+mapGiocatori.containsKey(inputStreamUserName));
		}
		if ( mapGiocatori.containsKey(inputStreamUserName)	&& 
			 mapGiocatori.get(inputStreamUserName).getPassword().equals(inputStreamPassWord))											  
		{
			
			 /* con questo pingPong controllo che il client a cui mi volgio
			 * riconnettere sia effettivamente offLine. In caso contrario
			 * segnalo con un messaggio di errore e chiudo la connessione
			 */
			System.err.println(PrintId+"----------ControlloOffLineGiocatore---------------");
			System.err.println(PrintId+mapGiocatori.get(inputStreamUserName).getIp()+"----"+ mapGiocatori.get(inputStreamUserName).getPortaPingPong());
			try(Socket temp = new Socket(mapGiocatori.get(inputStreamUserName).getIp(), mapGiocatori.get(inputStreamUserName).getPortaPingPong())) 
			{
				temp.setSoTimeout(500);
				System.err.println(PrintId+"----------AttesaRisposta---------------");
				try(ObjectInputStream inStreamRicconnesione = new ObjectInputStream(temp.getInputStream()))
				{
					LockMapGiocatori.unlock();	
					LockMapGiocatoriInfo.unlock();
					System.err.println(PrintId+"----------AttesaRisposta---------------");
					String cod = (String)inStreamRicconnesione.readObject();
					if(cod.equals(keyErrorCode.get(ClientOnline.ordinal())))
					{
						
						System.err.println(PrintId+"----------FineControlloOffLineGiocatore---------------");
						try {
							outRiconnessione.writeObject(keyErrorCode.get(ErroreClientOnline.ordinal()));
							return ErroreFineRegistrazione;
						} catch (IOException e) {
							System.err.println(PrintId+"Errore segnalazione errore per ClientOnline "+ e);
							return ErroreFineRegistrazione;
						}
					}
					return ErroreFineRegistrazione;
				}
			} catch (ClassNotFoundException | IOException e2) {
				System.err.println(PrintId+"Client scelto offline " + e2);
			}
			System.err.println(PrintId+"----------FineControlloOffLineGiocatore---------------");
			LockMapGiocatoriInfo.unlock();
			LockMapGiocatori.unlock();	
			try {
				outRiconnessione.writeObject(keyErrorCode.get(ConfermaAvvenutaRiconnessione.ordinal()));
			} catch (IOException e1) {
				//se mi salta anche questa notifica, c'è il timeOut nel client che fa resettare tutto
				System.err.println(PrintId+"errore notifica avvenuta riconnessione "+e1);
			}
			System.err.println(PrintId+errorCode.get(keyErrorCode.get(ConfermaAvvenutaRiconnessione.ordinal())));
			/*
			 * So che non è il pool "corretto, ma l'inizializzazione "è piu urgente"
			 * di un aggiornamento normale e quindi carico il task in un pool che presumibilmente
			 * è vuovo o comunque con poco carico di lavoro
			 */
			
System.err.println(PrintId+"--->Attesa permesso riconnessione...");
LockGame.lock();
System.err.println(PrintId+"--->Permesso riconnessione ricevuto...");
LockMapGiocatoriInfo.lock();
LockMapGiocatori.lock();	
mapGiocatoriInfo.get(inputStreamUserName).setUpDate(false);
mapGiocatoriInfo.get(inputStreamUserName).setMiss(0);
mapGiocatori.get(inputStreamUserName).setIp(String.valueOf(clientSocket.getInetAddress()).substring(1));
mapGiocatori.get(inputStreamUserName).setPorta(Integer.parseInt(inputStreamPorta));
mapGiocatori.get(inputStreamUserName).setPortaChat(Integer.parseInt(inputStreamPortaChat));
mapGiocatori.get(inputStreamUserName).setPortaPingPong(Integer.parseInt(inputSteamPortaPingPong));
LockMapGiocatoriInfo.unlock();
LockMapGiocatori.unlock();	
futureTaskRiconnessione.add(poolResgistrazione.submit(InizializzazionePartita(inputStreamUserName)));

System.err.println(PrintId+"--->Fine riconnessine....");
LockGame.unlock();
System.err.println(PrintId+"--->Sgancio della risorza....");
			
			
		}
		else
		{
			LockMapGiocatoriInfo.unlock();
			LockMapGiocatori.unlock();	
			try {
				outRiconnessione.writeObject(keyErrorCode.get(ErroreRiconnessione.ordinal()));
				System.err.println(PrintId+errorCode.get(keyErrorCode.get(ErroreRiconnessione.ordinal())));
			} catch (IOException e1) {
				//se mi salta anche questa notifica, c'è il timeOut nel client che fa resettare tutto
				System.err.println(PrintId+"Errore segnalazione errore riconnessione "+e1);
			}
			return ErroreRegistrazioneRestart;
		}	
		System.err.println(PrintId+"---------------Fine Riconnessione---------------------");
		return RiconnessioneAvvenutaConSuccesso; 
	} 
	

	/*
	 * In questa fase il server riceve una richiesta da parte di un client
	 * e dopo una prima analisi decide dove indirizzare la richiesta ed
	 * effettuare gli opporutni controlli. In particolare le richieste potranno 
	 * essere o per una connessione o per una riconnessione. La comunicazione in questa
	 * fase è gestita tramite un apposito proticollo che si basa su uno specifico scambio di messaggi.
	 */
	private  Callable<ErrorCodeEnum> InizioFasePreliminare(final Socket clientSocket) 
	{

		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";
		Callable<ErrorCodeEnum> task = new Callable<ErrorCodeEnum>() 
		{
			@Override
			public  ErrorCodeEnum call()
			{
				//Settaggio codice di errore
				//se la connessione non cambia il valore, qualcosa non quadra ed intterrompo la fase
				ErrorCodeEnum messError = ErroreFineRegistrazione;
				//Stringa di appoggio per i messaggi
				String frtMessage = null;
							
				try (ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream()))
				{
					try 
					{
						outStream.writeObject(keyErrorCode);
						outStream.writeObject(errorCode);
						outStream.writeObject(keyErrorCode.get(InizioRegistrazione.ordinal()));
					} catch (IOException e1) {
						//se non riesco a notificare al client l'errore, che il time out 
						//del client e del server che fa resettare la comunicazione
						System.err.println(PrintId+"Errore scittura primo messaggio!! "+e1);
						//e1.printStackTrace();
						return ErroreFineRegistrazione;
					}
					
					try (ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream()))
					{
						do
						{
							try {
								clientSocket.setSoTimeout(TempoRegistrazione);
							} catch (IOException e) {
								System.err.println(PrintId+"errore set time connessione!!! "+e);
								messError=ErroreRegistrazioneRestart;					
							}
							
							System.err.println(PrintId+"------Primo messaggio Connessione o Riconnessione?------");
							try	{
								frtMessage = (String)inputStream.readObject();
							}catch (SocketTimeoutException e){
								System.err.println(PrintId+"Tempo scaduto "+e);
								try {
									if(Debug)
										System.err.println(PrintId+errorCode.get(keyErrorCode.get(ConfermaTempoRegistrazioneScaduto.ordinal())));
									outStream.writeObject(keyErrorCode.get(ConfermaTempoRegistrazioneScaduto.ordinal()));
								} catch (IOException e1) {
									//se non riesco a notificare al client l'errore, che il time out 
									//del client e del server che fa resettare la comunicazione
									System.err.println(PrintId+"Errore segnalazione Tempo scaduto!! " + e1);
									//e1.printStackTrace();
								}
								messError=ErroreFineRegistrazione;
							} catch (ClassNotFoundException | IOException e) {
								throw new IllegalArgumentException(errorCode.get(keyErrorCode.get(0)));
							}
							//Prima analisi della connessione...
							if(Debug)
								System.err.println(PrintId+"frtMessage: "+frtMessage);
							if ( frtMessage!=null && frtMessage.equals(keyErrorCode.get(ConnessioneServer.ordinal()) )  )
							{
								lockInizioGame.lock();
								//Controllo inzio gioco in muta esclusione per garantire
								//che i dati non vengano cambiati di colpo (potrebbe non avere senso)
								if (gameIsStart)
								{
									lockInizioGame.unlock();
									outStream.writeObject(keyErrorCode.get(GiocoIniziato.ordinal()));
									return ErroreFineRegistrazione;
								}
								lockInizioGame.unlock();
								//se il gioco non è iniziato allora ....
								messError=  Registrazione(clientSocket,inputStream,outStream);
							}
							//Seconda analisi della connessione...
							if ( frtMessage!=null && frtMessage.equals(keyErrorCode.get(RiconnessioneServer.ordinal())) )	
								messError= Riconnessione(clientSocket,inputStream,outStream);
						}while(messError == ErroreRegistrazioneRestart);	
						
					} catch (IOException e1) {
						System.err.println(PrintId+"Errore apertuta stream in!! " +e1);
						e1.printStackTrace();
						messError = ErroreFineRegistrazione;
					}						
				} catch (IOException e1) {
					System.err.println(PrintId+"Errore apertuta stream out!! "+e1);
					messError=ErroreFineRegistrazione;
				}
				try {
					clientSocket.close();
				} catch (IOException e) {
					System.err.println(PrintId+"Errore chiusura socket e buffer!!!" + e);
				}
				return messError;	
			}
		};
		return task;
	}
	
	/*
	 * Questa fase gestisce le connessioni, il ruolo principale è quello di 
	 * aspettare una nuova connessione e di delegarla subito al poll rispettivo
	 * per rimettersi in attesa dei un'atra connessione.
	 * Inoltre si occupa di gestire il time out della partita che è temporizzato; 
	 * nel caso il server si riempa allora viene interrotto prima.
	 * Una volta "scattato" il tempo sara possibile comunque poter accedere a
	 * questa partita per un giocatore che ha "perso" la connessione.
	 */
	
	private void FasePreliminare() 
	{

		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";
		timerRegistrazioene.start();
		
		
		while(!IsStop)
		{
			System.out.println("attesa Connessione.....");
			Socket clientSocket = null;
			try
			{
				//Attesa connessione..
				clientSocket = socketConnessione.accept();
				System.out.println("-->Connessione");
				lockPool.lock();
				//si potrebbe mettere un controllo per evitare 
				//l'eccezione se il pool è spento ed arriva un task
				@SuppressWarnings("unused")
				Future<ErrorCodeEnum> response = poolResgistrazione.submit(InizioFasePreliminare(clientSocket));
				lockPool.unlock();
			}catch (SocketTimeoutException e) {
				//Tempo scaduto la prima volta che finisco qua, se ci sono abbastanza giocatori
				//faccio partire la partita e "chiudo" la connessione lasciando aperta la riconnessione
				System.err.println(PrintId+"Tempo di connessione scaduto; possibilile solo la riconnessione!! " + e);
			} catch (IOException e) {
				System.err.println(PrintId+"chiusura socket connessione!!" + e);
				//e.printStackTrace();
			}
		}
	}
		
	/*
	 * Questo task si occupa di proparage i dati iniziali della patita ia vari client.
	 * Vengono inviati i giocatori e la mappa creata su misura 
	 * della partita.
	 */
	private  Callable<ErrorCodeEnum> InizializzazionePartita(final String username) 
	{

		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";	
		Callable<ErrorCodeEnum> task = new Callable<ErrorCodeEnum>() {
			@Override
			public  ErrorCodeEnum call()
			{
				Giocatore player = mapGiocatori.get(username);
				try(Socket socketAggiornamento = new Socket(player.getIp(), player.getPorta()))
				{
					try(ObjectOutputStream outStream = new ObjectOutputStream(socketAggiornamento.getOutputStream()))
					{						
						//invio la lista dei giocatori con le relative informazioni
						outStream.writeObject(mapGiocatoriInfo);
						mapGiocatoriInfo.get(username).setUpDate(true);
						System.err.println(PrintId+"SEI TRUE !!!!!!");
						if(Debug)
						{
							for (String string : mapGiocatoriInfo.keySet()) 
							{
								System.err.println(PrintId+"-->"+mapGiocatoriInfo.get(string).getPos());
								System.err.println(PrintId+"-->"+mapGiocatoriInfo.get(string).getPunteggio());
							}
							for(int index=0; index < infoGraph.getNVertex(); index++)
							{
								System.err.println(PrintId+"--->"+grafo.getListNomiCitta().get(index)+posGraph.getListVertex().get(index).getCountBuoni());
								System.err.println(PrintId+"--->"+grafo.getListNomiCitta().get(index)+posGraph.getListVertex().get(index).getCountCattivi());
							}
						}
						//invio la mappa del gioco
						outStream.writeObject(posGraph);
						outStream.writeObject(tempListCitta);
						outStream.writeObject(infoGraph);
						try(ObjectInputStream inStream = new ObjectInputStream(socketAggiornamento.getInputStream()))
						{
							socketAggiornamento.setSoTimeout(TempoDiInizializzazione);
							System.err.println(PrintId+"nome giocatore : "+player.getUsername() );
							try{
								if( !((String)inStream.readObject()).equals(keyErrorCode.get(ConfermaAggiornamentoRicevuto.ordinal())) )
								{
									mapGiocatoriInfo.get(username).setUpDate(false);
									System.err.println(PrintId+"SEI false !!!!!!");
								}
							}catch(SocketTimeoutException | ClassNotFoundException e){
								System.err.println(PrintId+"fine tempo di attesa conferma aggiornamento " + e);
							}
							outStream.writeObject(mapGiocatoriInfo.get(username).IsUpdate());
						}catch(IOException  e){
							System.err.println(PrintId+"Errore apertura buffer in " + e);
						}
					}catch(IOException e){
						System.err.println(PrintId+"Errore apertura buffer out " + e);
					}
				} catch (IOException e) {
					System.err.println(PrintId+"Errore Fase aggiornamento client " + e);
				} 
				return null;
			}
		};
		return task;	
	}
	
	/*
	 * In questa fase, mi occupo di creare i task per 
	 * propagare le informazioni base al corretto svolgimento della partita
	 */
	private void Inizializzazione()
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";	
		//non serve il lock perchè non faccio operazioni
		//pericolose
		for (String username : buoni) {
			poolAggiornamento.submit(InizializzazionePartita(username));
		}
		for (String username : cattivi) {
			poolAggiornamento.submit(InizializzazionePartita(username));
		}
		System.err.println(PrintId+"tutti i task sono stati caricati");
		
		
		try {
			poolAggiornamento.shutdown();
			poolAggiornamento.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			System.err.println(PrintId+"Inizializzazione fallita per qualche client "+e);
			System.err.println(PrintId+"Chiusura forzata del pool di aggiornamento");
			poolAggiornamento.shutdownNow();	
		}
		System.err.println(PrintId+"tuttu i task sono stati eseguiti");
	}

	/*
	 * Questo task mi consente di propagare ai vairi client le mosse degli altri giocatori o informazioni utili
	 * che tutti i client devono leggere.
	 * E' possivile escludere un client dalla propagazione dei messaggi o meno.
	 * Principalmete è usato per la propagazione dei comandi dei vari giocatori e per lo stato della partita.
	 */
	private  Callable<ErrorCodeEnum> AggiornamentoComando(final String username,final String comando, final String usernamePlayer) 
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";	
		Callable<ErrorCodeEnum> task = new Callable<ErrorCodeEnum>() {
			@Override
			public  ErrorCodeEnum call()
			{
				System.err.println(PrintId+"Inizio fase aggirnamento comando");
				Giocatore palyerBackGround = mapGiocatori.get(username);
				//per essere sicuri, basterebbe semplicemente username == usernamePlayer
				if ( !palyerBackGround.getUsername().equals(usernamePlayer))
				{
					try (Socket aggiornamentiPartira = new Socket(palyerBackGround.getIp(), palyerBackGround.getPorta());
						ObjectOutputStream	outStreamBackGroud = new ObjectOutputStream(aggiornamentiPartira.getOutputStream()))
					{
						outStreamBackGroud.writeObject(keyErrorCode.get(ConfermaInizioAggironamento.ordinal()));

						if (usernamePlayer == null)
							//propagazione ha tutti i client messaggio del server
							outStreamBackGroud.writeObject(comando);
						else
							//propagazione a tutti -1 client messaggio di un altro client (mossa o attacco)
							outStreamBackGroud.writeObject(comando +","+usernamePlayer);
						//temporizzazione, vedere se si puo togliere
						try {
							Thread.sleep(TemporizzatoreAggiornamentoComando);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (IOException e) {
						System.err.println(PrintId+"Errore comunicazine client per aggiornamento " +e);
					}
					System.err.println(PrintId+"Fine fase aggirnamento comando");
				}
				return null;
			}
		};
		return task;
	}
	
	
	
	private void concessaRiconnessione(int timeWait, int timeWaitTask)
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";	
		System.err.println(PrintId+"--->Permesso Riconnessione");
		LockGame.unlock();
		
		try {
			Thread.sleep(timeWait); 
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.err.println(PrintId+"--->Fine Permesso Riconnessione");
		LockGame.lock();
		boolean flag=true;
			for (Future result : futureTaskRiconnessione) {
				try {
					result.get((timeWaitTask),TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				flag=false;
			}
		}
		if (flag)
			futureTaskRiconnessione.clear();
		System.err.println(PrintId+"--->Ritorno al gioco");
	}
	
	private void propagazioneInfo(ConcurrentLinkedQueue<String> listGiocatori, String comando,String user)
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";	
		LockListBuoniCattivi.lock();
		for (String username : listGiocatori) {
			LockListBuoniCattivi.unlock();
			if(mapGiocatoriInfo.get(username).IsUpdate())
			{
				System.err.println(PrintId+"--->"+username+mapGiocatori.get(username).getPorta());
				poolAggiornamento.submit(AggiornamentoComando(username, comando,user)); 
			}else
				System.err.println(PrintId+"-->user"+username+"false");
			LockListBuoniCattivi.lock();
		}								
		LockListBuoniCattivi.unlock();
	}
	/*
	 * Questa fase mi occupo del gioco vero e proprio.
	 * Questa funzione gestisce i vari timeout della connessione e di fine partita;
	 * nonchè gestisce la gestione dei turni e controlla che i client non si disconnettono
	 * durante la partita (dopo un certo limite li tolgo dalla mia lista).
	 */
	private void gioco()
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";	

		//Dichiarazione di variabili d'appoggio
		Giocatore player;
		//creazione del task di fine partita
		TimeOut timeOut = new TimeOut();
		Timer timer = new Timer();
		timer.schedule(timeOut, TempoDiGioco);
		poolAggiornamento = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		//fase e durata di gioco
		
		threadMunizioni = new Munizioni(posGraph.getListVertex().size(), posGraph.getListVertex(),(mapGiocatori.size()*3)/2 );
		threadMunizioni.start();
LockGame.lock();

		while(mapGiocatori.size() > 1 && !IsStop)
		{
System.err.println(PrintId+"------------------------------------------------------------------------------------------");
System.err.println(PrintId+"--->Permesso Riconnessione");
LockGame.unlock();

			player=null;
			
			LockListBuoniCattivi.lock();
			if( turnoEvil )
			{//turno cattivi...
				if (cattivi.size()>0)
				{//se c'e un cativo...
					System.err.println(PrintId+"Turno fazione"+ errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal())));
					player = mapGiocatori.get(cattivi.remove());
					cattivi.add(player.getUsername());
				}
				else
					//se non c'è un cattivo..
					turnoEvil=!turnoEvil;
			}
			
			if( !turnoEvil)
			{//turno buoni...
				if (buoni.size()>0)
				{//se c'è un buono...
					System.err.println(PrintId+"Turno fazione"+ errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal())));
					player = mapGiocatori.get(buoni.remove());
					buoni.add(player.getUsername());
				}	
			}
			LockListBuoniCattivi.unlock();
			//cambio turno
			turnoEvil = !(turnoEvil);
			//controllo se ho scelto un giocatore
			//se il turno e buoni e non ci sono player rimane a null 
			//e cambio dubito il turno
System.err.println(PrintId+"--->Fine Permesso Riconnessione");
LockGame.lock();
boolean flag=true;
for (Future result : futureTaskRiconnessione) {
	try {
		result.get((1100),TimeUnit.MILLISECONDS);
} catch (InterruptedException | ExecutionException | TimeoutException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
	flag=false;
}
}
if (flag)
futureTaskRiconnessione.clear();
System.err.println(PrintId+"--->Ritorno al gioco");


			if (player!=null)
			{
				System.err.println(PrintId+"---->"+player.getUsername()+"--"+mapGiocatoriInfo.get(player.getUsername()).IsUpdate());
				
				LockMapGiocatori.lock();
				System.err.println(PrintId+"------------TENTATIVO--------player.getUsername(): "+player.getUsername());
				try (Socket socketPartita = new Socket(player.getIp(), player.getPorta()))
				{
					System.err.println(PrintId+"client online");
					LockMapGiocatori.unlock();
						
					try (ObjectOutputStream outStream = new ObjectOutputStream(socketPartita.getOutputStream()))
					{
						try {
							System.err.println(PrintId+"------------INIZIOTURNO---------player.getUsername(): "+player.getUsername());
							outStream.flush();
							outStream.writeObject(keyErrorCode.get(ConfermaInizioTurno.ordinal()));
							outStream.writeObject(keyErrorCode.get(InizioTurno.ordinal())+player.getUsername());
							outStream.flush();
						} catch (IOException e1) {
							System.err.println(PrintId+"Errore segnalazione turno " +e1);
							e1.printStackTrace();
						}
						
						
						socketPartita.setSoTimeout(TempoDurataTurno);				
						try (ObjectInputStream inStream =  new ObjectInputStream(socketPartita.getInputStream()))
						{
							String comando = null;
							try{
								//attesa dei comandi
								//termina se il client mi manda il codice fine o se scade il tempo
								while ( !(comando = (String)inStream.readObject()).equals(keyErrorCode.get(ComandoFineTurno.ordinal())))
								{

concessaRiconnessione(100, 1100);


									System.err.println(PrintId+"ComandoRicevuto: "+comando);
									String temp[] = comando.split(",");
									
									//creazione dei task di propagazione delle mosse del client
									propagazioneInfo(buoni, comando,player.getUsername());
									propagazioneInfo(cattivi, comando,player.getUsername());
									try {
										outStream.writeObject(keyErrorCode.get(ConfermaComandoRicevuto.ordinal()));
									} catch (IOException e) {
										System.err.println(PrintId+"Errore notifica mossa " +e);
									}
									
									if (temp[0].equals((keyErrorCode.get(ComandoMossa.ordinal()))))
									{
										System.err.println(PrintId+"Il Giocatore Si è Mosso, ed ha :"+mapGiocatoriInfo.get(player.getUsername()).getPunteggio());
										
										//indica il prossimo giocatore, se vero vuol dire che sta giocando un buono
										if(!turnoEvil)
											posGraph.getListVertex().get(grafo.indexOfCity(mapGiocatoriInfo.get(player.getUsername()).getPos())).minuscountCattivi();
										else
											posGraph.getListVertex().get(grafo.indexOfCity(mapGiocatoriInfo.get(player.getUsername()).getPos())).minuscountBuoni();
										
										mapGiocatoriInfo.get(player.getUsername()).setPos(temp[1]);
										
										if(!turnoEvil)
											posGraph.getListVertex().get(grafo.indexOfCity(mapGiocatoriInfo.get(player.getUsername()).getPos())).addcountCattivi();
										else
											posGraph.getListVertex().get(grafo.indexOfCity(mapGiocatoriInfo.get(player.getUsername()).getPos())).addcountBuoni();
										
										int ricarica =threadMunizioni.getMunizioni(temp[1]);
										int munizioni=mapGiocatoriInfo.get(player.getUsername()).getPunteggio();
										boolean IsBrutto=false;
										
										if(temp[1].equals(posizione))
											IsBrutto=true; 
										if (ricarica != 0 || IsBrutto)
										{
											System.err.println(PrintId+"Munizioni prese :"+ricarica);
											if(IsBrutto)
											{
												mapGiocatoriInfo.get(player.getUsername()).upDatePunteggio(ricarica-(int)(munizioni*perditaMunizioniBrutto));
												propagazioneInfo(buoni, keyErrorCode.get(SfidaRicarico.ordinal())+","+ricarica+","+player.getUsername()+","+(int)(munizioni*perditaMunizioniBrutto),null);
												propagazioneInfo(cattivi, keyErrorCode.get(SfidaRicarico.ordinal())+","+ricarica+","+player.getUsername()+","+(int)(munizioni*perditaMunizioniBrutto),null);
											
											}else
											{
												mapGiocatoriInfo.get(player.getUsername()).upDatePunteggio(ricarica);
												propagazioneInfo(buoni, keyErrorCode.get(SfidaRicarico.ordinal())+","+ricarica+","+player.getUsername(),null);
												propagazioneInfo(cattivi, keyErrorCode.get(SfidaRicarico.ordinal())+","+ricarica+","+player.getUsername(),null);							
											}
										
										}
										
									}
									if (temp[0].equals((keyErrorCode.get(ComandoAttacco.ordinal()))))
									{
concessaRiconnessione(100, 1100);

										if(mapGiocatoriInfo.get(player.getUsername()).getPos().equals(mapGiocatoriInfo.get(temp[1]).getPos()))
										{
											System.err.println(PrintId+"Il Giocatore Attacca: "+player.getUsername()+" attacca "+temp[1]);
											try(Socket socketSfidante = new Socket(mapGiocatori.get(temp[1]).getIp(), mapGiocatori.get(temp[1]).getPorta());
												ObjectOutputStream outStreamSfidante = new ObjectOutputStream(socketSfidante.getOutputStream()))
											{
												
												outStreamSfidante.writeObject(keyErrorCode.get(SfidaIniziata.ordinal()));
												outStream.writeObject(keyErrorCode.get(SfidaIniziata.ordinal()));
												
												try(ObjectInputStream inStreamSfidante = new ObjectInputStream(socketSfidante.getInputStream()))
												{
													boolean IsSfida=false;
													String comandoAttaccante = null;
													String comandoSfidante = null;
													int munizioniAttaccante=0;
													int munizioniSfidante=0;
													while(!IsSfida)
													{
														socketSfidante.setSoTimeout(TempoDiSfida);
														socketPartita.setSoTimeout(TempoDiSfida);
														try{
															comandoAttaccante=(String)inStream.readObject();
															try{
																comandoSfidante= (String) inStreamSfidante.readObject();
																if(comandoSfidante.equals(""))
																	throw new IllegalStateException();
																System.err.println(PrintId+"ComandoSfidante ---------------> "+comandoSfidante);
																System.err.println(PrintId+"ComandoAttaccante -------------> "+comandoAttaccante);
																Thread.sleep(TemporizzatoreSfida);
																if (comandoAttaccante.equals(keyErrorCode.get(SfidaAttacco.ordinal())))
																	mapGiocatoriInfo.get(player.getUsername()).usoProiettile();
																if (comandoSfidante.equals(keyErrorCode.get(SfidaAttacco.ordinal())))
																	mapGiocatoriInfo.get(temp[1]).usoProiettile();
															
																if (comandoSfidante.equals(keyErrorCode.get(HaiPerso.ordinal())) && comandoAttaccante.equals(keyErrorCode.get(HaiPerso.ordinal())))
																{																
																	System.err.println(PrintId+"(PrimoIf)"+keyErrorCode.get(FineSfida.ordinal()));
																	outStream.writeObject(keyErrorCode.get(PareggioFineMosse.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(PareggioFineMosse.ordinal()));
																	System.err.println(PrintId+"---->Risultato Sfida ---->>"+munizioniAttaccante+"|----|"+munizioniSfidante+"<<----");
																	outStream.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																	
																	IsSfida=true;	
																}
																
																if (comandoSfidante.equals(keyErrorCode.get(HaiPerso.ordinal())) && !comandoAttaccante.equals(keyErrorCode.get(HaiPerso.ordinal())))
																{														
																	System.err.println(PrintId+"(SecondoIf)"+keyErrorCode.get(FineSfida.ordinal()));
																	outStream.writeObject(keyErrorCode.get(HaiVinto.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(HaiPerso.ordinal()));
																	
																	int munizioni = mapGiocatoriInfo.get(temp[1]).getPunteggio();
																	munizioniAttaccante=+(int)(munizioni*perditaMunizioniSfida);
																	if(munizioniAttaccante>=1)
																		munizioniSfidante=-(int)(munizioni*perditaMunizioniSfida);
																	else
																	{
																		munizioniAttaccante=munizioni;
																		munizioniSfidante=-munizioni;
																	}
																	System.err.println(PrintId+"---->Risultato Sfida ---->>"+munizioniAttaccante+"|----|"+munizioniSfidante+"<<----");
																	mapGiocatoriInfo.get(player.getUsername()).upDatePunteggio(munizioniAttaccante);
																	mapGiocatoriInfo.get(temp[1]).upDatePunteggio(munizioniSfidante);
																
																	outStream.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																	
																	IsSfida=true;	
																}
																
																if (!comandoSfidante.equals(keyErrorCode.get(HaiPerso.ordinal())) && comandoAttaccante.equals(keyErrorCode.get(HaiPerso.ordinal())))
																{
																	System.err.println(PrintId+"(TerzoIf)"+keyErrorCode.get(FineSfida.ordinal()));
																	outStream.writeObject(keyErrorCode.get(HaiPerso.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(HaiVinto.ordinal()));
																	
																	int munizioni = mapGiocatoriInfo.get(player.getUsername()).getPunteggio();
																	munizioniSfidante=+(int)(munizioni*perditaMunizioniSfida);
																	if(munizioniSfidante>=1)
																		munizioniAttaccante=-(int)(munizioni*perditaMunizioniSfida);
																	else
																	{
																		munizioniSfidante=munizioni;
																		munizioniAttaccante=-munizioni;
																	}
																	
																	System.err.println(PrintId+"---->Risultato Sfida ---->>"+munizioniAttaccante+"|----|"+munizioniSfidante+"<<----");
																	mapGiocatoriInfo.get(player.getUsername()).upDatePunteggio(munizioniAttaccante);
																	mapGiocatoriInfo.get(temp[1]).upDatePunteggio(munizioniSfidante);
																	
																	outStream.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																	
																	IsSfida=true;	
																}
																
																if (comandoAttaccante.equals(keyErrorCode.get(SfidaAttacco.ordinal())) && comandoSfidante.equals(keyErrorCode.get(SfidaRicarico.ordinal())))
																{
																	System.err.println(PrintId+"(QuartoIf)"+keyErrorCode.get(FineSfida.ordinal()));
																	
																	outStream.writeObject(keyErrorCode.get(HaiVinto.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(HaiPerso.ordinal()));
																												
																	int munizioni = mapGiocatoriInfo.get(temp[1]).getPunteggio();
																	munizioniAttaccante=+(int)(munizioni*perditaMunizioniSfida);
																	if(munizioniAttaccante>=1)
																		munizioniSfidante=-(int)(munizioni*perditaMunizioniSfida);
																	else
																	{
																		munizioniAttaccante=munizioni;
																		munizioniSfidante=-munizioni;
																	}
																	System.err.println(PrintId+"---->Risultato Sfida ---->>"+munizioniAttaccante+"|----|"+munizioniSfidante+"<<----");
																	mapGiocatoriInfo.get(player.getUsername()).upDatePunteggio(munizioniAttaccante);
																	mapGiocatoriInfo.get(temp[1]).upDatePunteggio(munizioniSfidante);
																	
																	outStream.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(FineSfida.ordinal()));
													
																	IsSfida=true;
																}
																
																if (comandoSfidante.equals(keyErrorCode.get(SfidaAttacco.ordinal())) && comandoAttaccante.equals(keyErrorCode.get(SfidaRicarico.ordinal())))
																{
																	System.err.println(PrintId+"(QuintoIf)"+keyErrorCode.get(FineSfida.ordinal()));
																	
																	outStream.writeObject(keyErrorCode.get(HaiPerso.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(HaiVinto.ordinal()));
																	
																	int munizioni = mapGiocatoriInfo.get(player.getUsername()).getPunteggio();
																	munizioniSfidante=+(int)(munizioni*perditaMunizioniSfida);
																	if(munizioniSfidante>=1)
																		munizioniAttaccante=-(int)(munizioni*perditaMunizioniSfida);
																	else
																	{
																		munizioniSfidante=munizioni;
																		munizioniAttaccante=-munizioni;
																	}
																	System.err.println(PrintId+"---->Risultato Sfida ---->>"+munizioniAttaccante+"|----|"+munizioniSfidante+"<<----");
																	mapGiocatoriInfo.get(player.getUsername()).upDatePunteggio(munizioniAttaccante);
																	mapGiocatoriInfo.get(temp[1]).upDatePunteggio(munizioniSfidante);
																	
																	outStream.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																	
																	IsSfida=true;
																}
																if (!IsSfida)
																{
																	outStream.writeObject(keyErrorCode.get(SfidaPareggio.ordinal()));
																	outStreamSfidante.writeObject(keyErrorCode.get(SfidaPareggio.ordinal()));
																}
																comando=comandoAttaccante=null;
																
															} catch (ClassNotFoundException | IOException e2)
															{
																System.err.println(PrintId+"(Cathc1)"+keyErrorCode.get(FineSfida.ordinal())+" "+e2);
																outStream.writeObject(keyErrorCode.get(HaiVinto.ordinal()));
																outStreamSfidante.writeObject(keyErrorCode.get(HaiPerso.ordinal()));
																
																int munizioni = mapGiocatoriInfo.get(temp[1]).getPunteggio();
																munizioniAttaccante=+(int)(munizioni*perditaMunizioniSfida);
																if(munizioniAttaccante>=1)
																	munizioniSfidante=-(int)(munizioni*perditaMunizioniSfida);
																else
																{
																	munizioniAttaccante=munizioni;
																	munizioniSfidante=-munizioni;
																}
																System.err.println(PrintId+"---->Risultato Sfida ---->>"+munizioniAttaccante+"|----|"+munizioniSfidante+"<<----");
																mapGiocatoriInfo.get(player.getUsername()).upDatePunteggio(munizioniAttaccante);
																mapGiocatoriInfo.get(temp[1]).upDatePunteggio(munizioniSfidante);
															
																outStream.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																outStreamSfidante.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																
																IsSfida=true;	
																
															} 
																									
														} catch (ClassNotFoundException | IOException e2)
														{
															try
															{
																comandoSfidante=(String) inStreamSfidante.readObject();
																System.err.println(PrintId+"(Cathc2)"+keyErrorCode.get(FineSfida.ordinal())+" "+e2);
																outStream.writeObject(keyErrorCode.get(HaiPerso.ordinal()));
																outStreamSfidante.writeObject(keyErrorCode.get(HaiVinto.ordinal()));
																
																int munizioni = mapGiocatoriInfo.get(player.getUsername()).getPunteggio();
																munizioniSfidante=+(int)(munizioni*perditaMunizioniSfida);
																if(munizioniSfidante>=1)
																	munizioniAttaccante=-(int)(munizioni*perditaMunizioniSfida);
																else
																{
																	munizioniSfidante=munizioni;
																	munizioniAttaccante=-munizioni;
																}
																System.err.println(PrintId+"---->Risultato Sfida ---->>"+munizioniAttaccante+"|----|"+munizioniSfidante+"<<----");
																mapGiocatoriInfo.get(player.getUsername()).upDatePunteggio(munizioniAttaccante);
																mapGiocatoriInfo.get(temp[1]).upDatePunteggio(munizioniSfidante);
																
																outStream.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																outStreamSfidante.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																
																IsSfida=true;	
															
															} catch (ClassNotFoundException | IOException e3)
															{
																System.err.println(PrintId+"(Catch3)"+keyErrorCode.get(FineSfida.ordinal()));
																outStream.writeObject(keyErrorCode.get(PareggioFineMosse.ordinal()));
																outStreamSfidante.writeObject(keyErrorCode.get(PareggioFineMosse.ordinal()));
																System.err.println(PrintId+"---->Risultato Sfida ---->>"+munizioniAttaccante+"|----|"+munizioniSfidante+"<<----");
																outStream.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																outStreamSfidante.writeObject(keyErrorCode.get(FineSfida.ordinal()));
																
																IsSfida=true;	
															}
															
															
															
														} 
														
													}
													
													String upDateSfida =keyErrorCode.get(SfidaPunteggiAggiornati.ordinal())+","
																		+player.getUsername()+","
																		+mapGiocatoriInfo.get(player.getUsername()).getPunteggio()+","
																		+temp[1]+","
																		+mapGiocatoriInfo.get(temp[1]).getPunteggio();
													
													
													propagazioneInfo(buoni,upDateSfida,null);
													propagazioneInfo(cattivi,upDateSfida,null);
											
													
												}catch (Exception e) {
													outStream.writeObject(keyErrorCode.get(ClientOffline.ordinal()));
													outStream.writeObject(keyErrorCode.get(FineSfida.ordinal()));
												}
												
											}catch (Exception e) {
												outStream.writeObject(keyErrorCode.get(ClientOffline.ordinal()));
												outStream.writeObject(keyErrorCode.get(FineSfida.ordinal()));
											}
																				
										}
										else
											outStream.writeObject("Giocatore non è nella tua stessa citta\n");
									}
									socketPartita.setSoTimeout(TempoDurataTurno);
							}
concessaRiconnessione(100, 1100);

							System.out.println(comando);
	//sbagli a fare i conti delle munizioni							
							
							} catch (SocketTimeoutException e1){
								System.err.println(PrintId+"Errore time out lettura "+e1);
							} catch (ClassNotFoundException | IOException e2) {
								System.err.println(PrintId+"errore lettura "+e2);
							} 
							try {
								
								outStream.writeObject(keyErrorCode.get(ConfermaFineTruno.ordinal()));
							} catch (IOException e1) {
								System.err.println(PrintId+"Errore notifica fine turno "+e1);
							}
concessaRiconnessione(100, 1100);

						} catch (IOException e1) {
							System.err.println(PrintId+"Errore "+e1);
						}	
					}catch (IOException e1) {
						System.err.println(PrintId+"Errore apetura stream "+e1);
					}					
					
				} catch (IOException e) 
				{
					//se finisco qua è perche non riesco a contattare il client
					/*
					 * Questo pezzo è gestito in muta esclusione perche
					 * potrei accedere e modificare questo campo in fase di riconnessione
					 */
					System.err.println(PrintId+"--->Client "+player.getUsername()+" risulta offLine");
					LockMapGiocatoriInfo.lock();
					int miss = mapGiocatoriInfo.get(player.getUsername()).getMiss();
					//mapGiocatoriInfo.get(player.getUsername()).setUpDate(false);
					//se per tre volte non contatto il client allora lo tolgo dalla partita
					//0 1 2 3 esco
					if ( miss > 2)
					{	
						mapGiocatori.remove(player.getUsername());
						mapGiocatoriInfo.remove(player.getUsername());
						if (player.getFazione().equals(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal()))))
							buoni.remove(player.getUsername());
						else
							cattivi.remove(player.getUsername());
					
						LockMapGiocatori.unlock();
						LockMapGiocatoriInfo.unlock();
						propagazioneInfo(buoni, keyErrorCode.get(ClientOffline.ordinal()) ,player.getUsername());
						propagazioneInfo(cattivi, keyErrorCode.get(ClientOffline.ordinal()) ,player.getUsername());
						
					}	
					else
					{	
						mapGiocatoriInfo.get(player.getUsername()).setMiss(miss+1);
						LockMapGiocatori.unlock();
						LockMapGiocatoriInfo.unlock();
concessaRiconnessione(100, 1100);

					}

					System.err.println(PrintId+"Client offline :"+player.getUsername());
					
				}finally 
				{
					System.err.println(PrintId+"---------------SpostamentoBrutto------------------");
					System.err.println(PrintId+"VecchiaPosizioneBrutto"+posizione);
					List<String> temp =posGraph.getListVertex().get(grafo.indexOfCity(posizione)).getNeighbors();
					if (temp.size()==1)
						posizione=temp.get(0);
					else
						posizione=temp.get(randomGenerator.nextInt(temp.size()-1));
					System.err.println(PrintId+"NuovaPosizioneBrutto"+posizione);				
					System.err.println(PrintId+"----------------FineSpostamentoBrutto-----------------");
				}
			}
		}
//concessaRiconnessione(500, 1000);
LockGame.unlock();
		//se esco dalla partita ma IsStop=false
		if(!IsStop)
		{
			//cancello il tasck programmato all'inizio
			timer.cancel();
			//e lo lancio manualmente
			timeOut.run();
		}else 
		//cancello il tasck programmato all'inizio
			timer.cancel();
		System.err.println(PrintId+"fine gioco**********");	

	}
	
	/*
	 * Questo task si occupa della propagazione hai client designati dei messaggi della chat.
	 */
	private  Callable<ErrorCodeEnum> AggiornamentoChat(String username, String text) 
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";	
		Callable<ErrorCodeEnum> task = new Callable<ErrorCodeEnum>() {
			@Override
			public  ErrorCodeEnum call()
			{
				System.err.println(PrintId+"-------------InizioFaseAggirnamentoChat-------------");
				
				LockMapGiocatori.lock();
				Giocatore palyerBackGround = mapGiocatori.get(username);
				LockMapGiocatori.unlock();
					if(Debug)
						System.err.println(PrintId+"palyerBackGround.getIp(): "+palyerBackGround.getIp()+"palyerBackGround.getPorta()"+palyerBackGround.getPorta());
					try (Socket aggiornamentiChat = new Socket(palyerBackGround.getIp(), palyerBackGround.getPortaChat());
						ObjectOutputStream	outStreamBackGroud = new ObjectOutputStream(aggiornamentiChat.getOutputStream()))
					{
						outStreamBackGroud.flush();
						outStreamBackGroud.writeObject(text);
						try {
							Thread.sleep(TemporizzatoreAggiornamentoChat);
						} catch (InterruptedException e) {
							System.err.println(PrintId+e);
							//e.printStackTrace();
						}
					} catch (IOException e) {
						System.err.println(PrintId+"Errore comunicazine client per aggiornamento "+e);
						//e.printStackTrace();
					}
					System.err.println(PrintId+"------------FineFaseAggirnamentoComando------------");			
				return null;
			}
		};
		return task;

	}

	
	/*
	 * In questa fase, il server rimane in attesa per la connessione di qualche client 
	 * che vuole scrivere un messaggio agli altri giocatori.
	 * la chat potra essere globale o di team ; con un apposito codice determinera
	 * il tipo di chat. In quensta fase è gestito anche il tempo di ivio del messaggio; se 
	 * il messaggio non arriva entro tot tempo la comunicazione viene abbandonata.
	 * (Un possibile miglioramento potrebbe essere quallo di assochiara ad ogni client un
	 * thread per la chat che serta attivo tot secondi. se i messaggi contunuano ad arrivare allora non
	 * chiudo il thread (ovv la connessione); altrimenti si. Questo solo il lettura pero!)
	 */
	private void Chat()
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";	
		while(!IsStop)
		{
			System.out.println("-->attesa Connessione chat");
			Socket chatSocket = null;
			try
			{
				String text;
				String user;
				chatSocket = socketChat.accept();
				chatSocket.setSoTimeout(TempoChat);
				System.out.println("-->Connessione chat avvenuta");
				try (ObjectInputStream inSocketChat = new ObjectInputStream(chatSocket.getInputStream()))
				{
					user = (String) inSocketChat.readObject();
					text = (String) inSocketChat.readObject();
					if(Debug)
						System.out.println(PrintId+"*****************testo ricevuto..... "+text);
					
					//se mi sta scrivendo, teoricamente dovrebbe risultare on e non dovrebbe essere tolto
					//quindi posso evitare i lock
					if ( text.startsWith("\\a") || mapGiocatori.get(user).getFazione().equals(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal()))) )
					{
						//Questa parte viene gestita in muta esclusione
						//poiche è posibile toglio dei giocatori
						//durante la partita
						LockListBuoniCattivi.lock();
						//propagazione del messaggio
						for (String username : buoni) {
							LockListBuoniCattivi.unlock();
							if(Debug)
								System.err.println(PrintId+"--------username: "+username);
							poolChat.submit(AggiornamentoChat(username,user+" : "+text.replace("\\a", "")+"\n"));
							if(Debug)
								System.err.println(PrintId+"--------username: "+username);
							LockListBuoniCattivi.lock();
						}								
						LockListBuoniCattivi.unlock();
					}
					//se mi sta scrivendo, teoricamente dovrebbe risultare on e non dovrebbe essere tolto
					//quindi posso evitare i lock

					if ( text.startsWith("\\a") || mapGiocatori.get(user).getFazione().equals(errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal()))) )
					{
						//Questa parte viene gestita in muta esclusione
						//poiche è posibile toglio dei giocatori
						//durante la partita
						LockListBuoniCattivi.lock();
						//propagazione del messaggio
						for (String username : cattivi) {
							LockListBuoniCattivi.unlock();
							if(Debug)
								System.err.println(PrintId+"--------username: "+username);
							poolChat.submit(AggiornamentoChat(username,user+" : "+text.replace("\\a", "")+"\n"));
							System.err.println(PrintId+"--------username: "+username);
							LockListBuoniCattivi.lock();
						}
						LockListBuoniCattivi.unlock();
					}

				} catch (ClassNotFoundException e) {
					System.err.println(PrintId+"Errore messaggi chat " + e);
				}
			}catch (SocketTimeoutException e) {
				System.err.println(PrintId+"Errore messaggi chat timeOut " + e);
			} catch (IOException e) {
				System.err.println(PrintId+"Errore messaggi chat " + e);
			}
		}
	}
	
	public void start() 
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";	
		
		if(!isOnLineServer)
			return;
	
		ThreadFasePreliminare TfarePreliminare = new ThreadFasePreliminare();
		TfarePreliminare.start();
				
		ThreadChat Tchat = new ThreadChat();
		Tchat.start();

		System.err.println(PrintId+"Attesa Inzio Gioco");
		try {
			timerRegistrazioene.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		poolResgistrazione.shutdown();
		try {
			poolResgistrazione.awaitTermination(60, TimeUnit.SECONDS);
			poolResgistrazione = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		} catch (InterruptedException e) {
		
			e.printStackTrace();
		}
		
		TempoDiGioco=TempoDiGioco*mapGiocatori.size();
		infoGraph.setNVertex(mapGiocatori.size()*3);
		grafo = new CreatePosGUI(infoGraph.getNVertex(), 
				infoGraph.getDistanza(),
				infoGraph.getLarghezzaX(),
				infoGraph.getLarghezzaY(),
				infoGraph.getWidth(),
				infoGraph.getHeight());

 		posizione=grafo.cityAtIndex(randomGenerator.nextInt(mapGiocatori.size()*3 -1));
		grafo.create_PosGraph();
		posGraph = grafo.get_PosGUI();
		tempListCitta= new LinkedList<>();
		List<String> ListCitta = grafo.getListNomiCitta();
		for(int index=0; index<infoGraph.getNVertex(); index++)
			tempListCitta.add(ListCitta.get(index));
		
		LockMapGiocatoriInfo.lock();
	
		for (String string : buoni) {
			mapGiocatoriInfo.get(string).setPos(posGraph.getListVertex().get(1).getName());
			posGraph.getListVertex().get(1).addcountBuoni();
			mapGiocatoriInfo.get(string).setPunteggio(10);
		}
		
		for (String string : cattivi) {
			mapGiocatoriInfo.get(string).setPos(posGraph.getListVertex().get(1).getName());
			posGraph.getListVertex().get(1).addcountCattivi();
			mapGiocatoriInfo.get(string).setPunteggio(10);
		}
		LockMapGiocatoriInfo.unlock();
		System.err.println(PrintId+"Inizializzazione gioco");
		Inizializzazione();
		System.err.println(PrintId+"Fine Inizializzazione gioco");
		System.err.println(PrintId+"Inizio gioco");
		gioco();
		
		
	}
	
	//Questa classe mi permette 
	//di creare un task che viene schedulato dopo 
	//topo tot tempo dalla sua dichiarazione
	//utile per gestite il timeout di fine partita
	class TimeOut extends TimerTask
	{
		private final String SubClassName=this.getClass().getName();
					
		TimeOut(){}
	
		
		@Override
		//il metoto e' synchronized per "sicurezza" ma ne esitera sempre solo uno
		public synchronized void run() 
		{
			final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
			final String PrintId=ClassName+"-"+SubClassName+","+MethodName+": ";
			//se vero allora il task è gia stato lanciato
			if (IsStop)
				return;
			
			int max=-1;
			int punteggioBuoni=0;
			int punteggioCattivi=0;
			String giocatoriVincenti = null;
			String fazioneVincente = null;
			for (String string : mapGiocatoriInfo.keySet()) {
				
				if(mapGiocatoriInfo.get(string).getFazione().equals(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal()))))
					punteggioBuoni+=mapGiocatoriInfo.get(string).getPunteggio();
				else
					punteggioCattivi+=mapGiocatoriInfo.get(string).getPunteggio();
				
				if(mapGiocatoriInfo.get(string).getPunteggio()==max)
				{
					giocatoriVincenti+="\n"+string;
				}
				
				if(mapGiocatoriInfo.get(string).getPunteggio()>max)
				{
					giocatoriVincenti = string;
					max=mapGiocatoriInfo.get(string).getPunteggio();
				}
			}
			
			if (punteggioBuoni > punteggioCattivi)
				fazioneVincente="Fazione vincitrice \n"+(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal()))+": "+punteggioBuoni
						+"\n\nFazione perdente \n"+(errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal()))+" :"+punteggioCattivi));
			else
				if(punteggioBuoni < punteggioCattivi)
					fazioneVincente="Fazione vincitrice \n"+(errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal()))+" :"+punteggioCattivi
							+"\n\nFazione perdente \n"+(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal()))+" :"+punteggioBuoni));
				else
					fazioneVincente="";
			propagazioneInfo(buoni, keyErrorCode.get(FinePartita.ordinal())+","+fazioneVincente+"\n\nGiocatori Migliori\n,"+giocatoriVincenti ,null);
			propagazioneInfo(cattivi, keyErrorCode.get(FinePartita.ordinal())+","+fazioneVincente+"\n\nGiocatori Migliori\n,"+giocatoriVincenti ,null);
			System.err.println(PrintId+"server off");
			IsStop=true;
			try {
				socketConnessione.close();
				socketChat.close();
			} catch (IOException e) {
				System.err.println(PrintId+e);
			}
			
			lockPool.lock();
			//provo a chiudere i pool
			poolAggiornamento.shutdown();
			poolResgistrazione.shutdown();
			poolChat.shutdown();
			lockPool.unlock();
			
			//se i task non finiscono nel tempo stabilito
			//li tronco
			try {
				poolAggiornamento.awaitTermination(1, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.err.println(PrintId+"Forzatura chiusura poolAggiornamento "+e);
				poolAggiornamento.shutdownNow();
				//e.printStackTrace();
			}
			try {
				poolResgistrazione.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.err.println(PrintId+"Forzatura chiusura poolResgistrazione "+e);
				poolResgistrazione.shutdownNow();
				//e.printStackTrace();
			}
			
			try {
				poolChat.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.err.println(PrintId+"Forzatura chiusura poolChat " +e);
				poolChat.shutdownNow();
				//e.printStackTrace();
			}
			
			if(threadPingPong.isAlive())
				try (Socket a = new Socket("127.0.0.1", 9002 ))
				{
					System.err.println(PrintId+"-->Chiusura ThreadPingPong");
				} catch (IOException e) {
					System.err.println(PrintId+"-->Chiusura ThreadPingPong " +e);
					//e1.printStackTrace();
				}
			
			if(threadMunizioni.isAlive())
				threadMunizioni.Stop();
			
		}
	}
	
		//Questo thread scandisce il tempo della registrazione
		//e definisce l'inizio del gioco.
		//effettua n controllo per vedere se per caso
		//prima della scadenza del server si siano registrati tutti i giocatori
		class TimerRegistrazioene extends Thread
		{
			PingPong pingPong;
			private final String SubClassName=this.getClass().getName();
			public TimerRegistrazioene() {
				pingPong = new PingPong();
				pingPong.start();
			}
			@Override
			public void run() {
				final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
				final String PrintId=ClassName+"-"+SubClassName+","+MethodName+": ";
				int i=0;
				System.err.println(PrintId+"TimeOutIscrizione*******************************");
			while (i < NControlli)
			{			try {
					Thread.sleep(TempoIscrizione/NControlli);
				} catch (InterruptedException e) {
				System.err.println(PrintId+"errore sleep timerRegistrazione");
				}
				pingPong.signal();
				System.err.println(PrintId+"-->controllo" + i);
				LockMapGiocatori.lock();
				System.err.println(PrintId+"-->Giocatori presenti -- Giocatori necessari --->"+mapGiocatori.size()+" -- "+MaxGiocatori);
				if (mapGiocatori.size()>=MaxGiocatori)
				{
					LockMapGiocatori.unlock();
					break;
				}
			
				i++;
				//se il tempo è scaduto ma non ci sono abbastanza giocatori 
				//il thread non fa partire il gioco e si mette in attesa di un giocatore
				//si possono adottare due politiche : o riaspettare il tempo stabilito (implementato)
				//o appena si iscive uno si parte col gioco
				
				if (i >= NControlli && mapGiocatori.size()<MinGiocatori)
				{
					try {
						//per essere sicuri che tutte le informazioni 
						//siano salvate nelle strutture
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.err.println(PrintId+"Giocatori insufficenti");
					i=0;
				}
				LockMapGiocatori.unlock();
			}
			lockInizioGame.lock();
			gameIsStart=true;
			pingPong.signal();
			lockInizioGame.unlock();
			System.err.println(PrintId+"Fine TimeOut iscrizione*******************************");
			}	
		}
		
		//Questo thread contolla che tutti i giocatori che si connettono
		//restano online per tutto la fase di attesa
		//togliento quelli che vanno off line
		class PingPong extends Thread
		{
			ReentrantLock lockPingPong;
			Condition condPingPong;
			private final String SubClassName=this.getClass().getName();
			public PingPong() {
				lockPingPong= new ReentrantLock();
				condPingPong = lockPingPong.newCondition();
			}
			
			public void signal()
			{
				lockPingPong.lock();
				condPingPong.signalAll();
				lockPingPong.unlock();
			}
			
			@Override
			public void run() 
			{
				final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
				final String PrintId=ClassName+"-"+SubClassName+","+MethodName+": ";
				lockInizioGame.lock();
				while(!gameIsStart)
				{
					lockInizioGame.unlock();
					System.err.println(PrintId+"TimeOut pingPong*******************************");
					lockPingPong.lock();
					try {
						condPingPong.await();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					lockPingPong.unlock();
					lockInizioGame.lock();
					if(gameIsStart)
					{
						lockInizioGame.unlock();
						return;
					}
					lockInizioGame.unlock();
					LockListBuoniCattivi.lock();
					for (String username : buoni)
					{
						//lascio la lista tra i lock poiche 
						//questo thread lavora in contemporanea con la registrazione
						//la quale aggiunge dei campi. forse non è necessario ma 
						//precauzione
						LockListBuoniCattivi.unlock();
						//non serve mettere i lock poiche 
						//non posso modificare questi campi.
						//in questa fase di pingpong solo questo thread puo modificare questa struttuta
						if(Debug)
							System.err.println(PrintId+mapGiocatori.get(username).getIp() +" --- "+ mapGiocatori.get(username).getPortaPingPong());
						try(Socket pingPong = new Socket(mapGiocatori.get(username).getIp(), mapGiocatori.get(username).getPortaPingPong());
							ObjectInputStream inStreamPingPong = new ObjectInputStream(pingPong.getInputStream())	)
						{
							pingPong.setSoTimeout(TempoPingPongLettura);
							String cod =(String) inStreamPingPong.readObject();
						
							if(cod.equals(keyErrorCode.get(ClientOnline.ordinal())))
								System.err.println(PrintId+"-->client online "+username);
							else
							{
								System.err.println(PrintId+"-->client offline "+username);
								mapGiocatori.remove(username);
								mapGiocatoriInfo.remove(username);
								buoni.remove(username);
							}
						}catch (Exception e) {
							e.printStackTrace();
							System.err.println(PrintId+"client offline "+username+" -- "+e);
							mapGiocatori.remove(username);
							mapGiocatoriInfo.remove(username);
							buoni.remove(username);
						}
						LockListBuoniCattivi.lock();
					}								
					LockListBuoniCattivi.unlock();
					
					lockInizioGame.lock();
					if(gameIsStart)
					{
						lockInizioGame.unlock();
						return;
					}
					lockInizioGame.unlock();
					//lascio la lista tra i lock poiche 
					//questo thread lavora in contemporanea con la registrazione
					//la quale aggiunge dei campi. forse non è necessario ma 
					//precauzione
					LockListBuoniCattivi.lock();
					for (String username : cattivi) 
					{
						LockListBuoniCattivi.unlock();
						//non serve mettere i lock poiche 
						//non posso modificare questi campi.
						//in questa fase di pingpong solo questo thread puo modificare questa struttuta
						if(Debug)
							System.err.println(PrintId+mapGiocatori.get(username).getIp() +" --- "+ mapGiocatori.get(username).getPortaPingPong());
						try(Socket pingPong = new Socket(mapGiocatori.get(username).getIp(), mapGiocatori.get(username).getPortaPingPong());
							ObjectInputStream inStreamPingPong = new ObjectInputStream(pingPong.getInputStream())	)
						{
							pingPong.setSoTimeout(TempoPingPongLettura);
							String cod =(String) inStreamPingPong.readObject();
							if(cod.equals(keyErrorCode.get(ClientOnline.ordinal())))
								System.err.println(PrintId+"client online "+username);
							else
							{
								System.err.println(PrintId+"client offline "+username);
								mapGiocatori.remove(username);
								mapGiocatoriInfo.remove(username);
								buoni.remove(username);
								cattivi.remove(username);
							}
							
						}catch (Exception e) {
							e.printStackTrace();
							System.err.println(PrintId+"client offline "+username+" -- "+e);
							mapGiocatori.remove(username);
							mapGiocatoriInfo.remove(username);
							buoni.remove(username);
							cattivi.remove(username);
						}
						LockListBuoniCattivi.lock();	
						
					}
					LockListBuoniCattivi.unlock();
					System.err.println(PrintId+"FineTimeOutPingPong*******************************");
					lockInizioGame.lock();
				}
				lockInizioGame.unlock();
			}	
		}
		
		
		class Munizioni extends Thread
		{
			private List<ThreadMunizioni> listMunizioni;
			private List<Vertex> listVertex;
			private int count;
			private int max;
			private final String SubClassName=this.getClass().getName();
			public Munizioni(int nVertex, List<Vertex> listVertex, int max) {
				this.listMunizioni = new Vector<>(nVertex);
				for(int i=0; i<nVertex; i++)
					this.listMunizioni.add(new ThreadMunizioni());
				this.listVertex = listVertex; 
				this.max=max;
				
				
			}
			
			@Override
			public  void  run()
			{
				final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
				final String PrintId=ClassName+"-"+SubClassName+","+MethodName+": ";
				synchronized(this)
				{
					while (!IsStop)
					{
						
							if(count < max)
							{
								int num =randomGenerator.nextInt(listVertex.size()-1);
								if(!listMunizioni.get(num).isAlive() )
								{
									System.err.println(PrintId+"--->munizioni sull citta "+grafo.cityAtIndex(num));
									listMunizioni.set(num, new ThreadMunizioni());
									listMunizioni.get(num).start();
									count++;
								}
							}
						
						try {
							wait(TempoCreazioneMunizioni);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				for (ThreadMunizioni threadMuni : listMunizioni)
					try {
						threadMuni.join();
					} catch (InterruptedException e) {
						System.err.println(PrintId+"Errore attesa join Munizioni");
					}
						
			}
			
			public synchronized void Stop()
			{
				
				for (ThreadMunizioni threadMuni : listMunizioni) {
					if(threadMuni.isAlive())
						threadMuni.Stop();
					notify();
			}
				
				
			}
			
			public int getMunizioni(String citta)
			{
				int muni=0;
				if(listMunizioni.get(grafo.indexOfCity(citta)).isAlive())
				{
					muni=listMunizioni.get(grafo.indexOfCity(citta)).getNmunizioni();
					listMunizioni.get(grafo.indexOfCity(citta)).Stop();
				}
				return muni;
			}
			
			class ThreadMunizioni extends Thread
			{
				
				private int numMunizioni;
				private ReentrantLock lockMun;
				private Condition condMun;	
				private final String SubClassName=this.getClass().getName();
				public ThreadMunizioni() {
					lockMun= new ReentrantLock();
					condMun= lockMun.newCondition();
					
				}
				
				@Override
				public  void run() {
					final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
					final String PrintId=ClassName+"-"+SubClassName+","+MethodName+": ";
					numMunizioni =randomGenerator.nextInt(99)+1;
					try {
						System.err.println(PrintId+"/*/*/*/*/*TIMEOUT MUNIZIONI/*/*/*/*/*/*/*/"+numMunizioni);
						lockMun.lock();
						int temp =(numMunizioni/4);
						if (temp == 0)
							condMun.await(TempoMunizioni, TimeUnit.MILLISECONDS);
						else
							condMun.await(TempoMunizioni/temp, TimeUnit.MILLISECONDS);
						lockMun.unlock();
						System.err.println(PrintId+"/*/*/*/*/*FINE TIMEOUT MUNIZIONI/*/*/*/*/*/*/*/");
						count--;
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				public int getNmunizioni(){
					return numMunizioni;
				}
				
				public void Stop() 
				{
					lockMun.lock();
					condMun.signal();
					this.numMunizioni=0;
					lockMun.unlock();
					
				}
			}
			
		}
		
		class ThreadPingPong extends Thread 
		{
			private final String SubClassName=this.getClass().getName();
			@Override
			public void run() 
			{
				final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
				final String PrintId=ClassName+"-"+SubClassName+","+MethodName+": ";
				while(!IsStop)
				{
					
					try (Socket temp =socketPingPong.accept())
					{		
						System.err.println(PrintId+"------------------------------------------------------");
						try(ObjectOutputStream outStreamPingPong = new ObjectOutputStream(temp.getOutputStream()))
						{
							outStreamPingPong.writeObject(keyErrorCode.get(ServerOnline.ordinal()));
						}catch (IOException  e){
							System.err.println(PrintId+"errore outStreamPingPong " + e);
						}
						
					} catch (IOException e) {
					System.err.println(PrintId+"errore socketPingPong " + e);
					}
				}
			}
		}
		
		class ThreadFasePreliminare extends Thread
		{
			@Override
			public void run() {
				FasePreliminare();
			}
		}
		
		class ThreadChat extends Thread{
			
			@Override
			public void run() {
				Chat();
			}
		}
	
/*
 * miglioramento da fare piu avanti
	class Chat extends Thread{
		
		Socket chatSocket;
		
		public Chat(Socket chat) {
			this.chatSocket = chat;
		}
		
		//la mia idea è quella di far partire il thread non appena 
		//arriva una connessione e poi lasciarlo attico qualche minuto 
		//in questo modo, se il client scrive di continuo non devo aprire e 
		//chiudere ogni volta il socket
		
		@Override
		public void run() {
			String text;
			try(ObjectInputStream inStreamChat = new ObjectInputStream(chatSocket.getInputStream())) 
			{
				text = (String) inStreamChat.readObject();
				
				LockListBuoniCattivi.lock();
				for (String username : buoni) {
					
					LockListBuoniCattivi.unlock();
					lockPool.lock();
					poolChat.submit(AggiornamentoChat(username,text));
					lockPool.unlock();
					LockListBuoniCattivi.lock();
				}								
				LockListBuoniCattivi.unlock();
				
				LockListBuoniCattivi.lock();
				for (String username : cattivi) {
					LockListBuoniCattivi.unlock();
					lockPool.lock();
					poolChat.submit(AggiornamentoChat(username,text));
					lockPool.unlock();
					LockListBuoniCattivi.lock();	
				}
				LockListBuoniCattivi.unlock();
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		
	}
	*/
	


}
