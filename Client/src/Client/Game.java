package Client;

import static ServerClient.EnumKeyErrorCode.ComandoAttacco;
import static ServerClient.EnumKeyErrorCode.ComandoFineTurno;
import static ServerClient.EnumKeyErrorCode.ComandoMossa;
import static ServerClient.EnumKeyErrorCode.ErroreStessaFazione;
import static ServerClient.EnumKeyErrorCode.FazioneBuoni;
import static ServerClient.EnumKeyErrorCode.FazioneCattivi;


import java.awt.Color;
import java.awt.Component;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.border.Border;


import com.mxgraph.swing.mxGraphComponent;


import ClientGraph.Grafox;
import ServerClient.InfoGiocatore;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTextArea;

/*
 * Questa classe si occupa della gestione della 
 * grafica del gioco e dell'invio dei comandi al
 * server.
 * Inoltre gestisce acnhe la parte di lettura e scrittuta
 * della chat
 * 
 * */


@SuppressWarnings("serial")
public class Game extends JFrame implements ActionListener,KeyListener{

	private JButton FineTurno;
	private JTextArea ChatInvio;
	private JTextArea Chat;
	private JComboBox<String> Mosse;
	private JComboBox<String> Giocatori;
	private JTextArea Error;
	private JTextArea info;
	
	private ServerSocket socketServerChat;
	private String ip;
	private int port;
	private String username;
	private String fazione;
	private List<String> listNomiCitta;
	private String citta;
	private ReentrantLock lockClassifica;
	private ReentrantLock lockError;
	
	private Grafox grafox;
	
	ConcurrentHashMap<String, String> errorCode;
	List<String> keyErrorCode;
	
	List<String> adiacent;
	ObjectOutputStream outClient;
	private JTextField FazionePlayer;
	private JTextField UserPlayer;
	private JTextField MunizioniPlayer;
	private JTextField PosizionePlayer;
	
	JLabel labelImageGrafox;
	
	
	mxGraphComponent graphComponent;
	JScrollPane GraphPane;
	JScrollPane ChatInvioPanel;
	JScrollPane ChatPanel;
	private JTextField DatiPersonali;
	
	
	
	private void SetView()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		
		setTitle("Spaghetti Western Distributed Multiplayer Game");
//---------------------	
		JScrollPane InfoPanel = new JScrollPane();
		info = new JTextArea();
		info.setOpaque(false);
		info.setEditable(false);
		info.setLineWrap(true);
		info.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		info.setForeground(Color.WHITE);
		info.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		InfoPanel.setViewportView(info);
		InfoPanel.getViewport().setOpaque(false);
		InfoPanel.setOpaque(false);

//-------------------
		DatiPersonali = new JTextField("Dati Personali :");
		DatiPersonali.setEditable(false);
		DatiPersonali.setColumns(10);
		DatiPersonali.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		DatiPersonali.setForeground(Color.WHITE);
		DatiPersonali.setOpaque(false);
		
		UserPlayer = new JTextField();
		UserPlayer.setEditable(false);
		UserPlayer.setColumns(10);
		UserPlayer.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		UserPlayer.setForeground(Color.WHITE);
		UserPlayer.setOpaque(false);
		
		FazionePlayer = new JTextField();
		FazionePlayer.setEditable(false);
		FazionePlayer.setColumns(10);
		FazionePlayer.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		FazionePlayer.setForeground(Color.WHITE);
		FazionePlayer.setOpaque(false);
		
		PosizionePlayer = new JTextField();
		PosizionePlayer.setEditable(false);
		PosizionePlayer.setColumns(10);
		PosizionePlayer.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		PosizionePlayer.setForeground(Color.WHITE);
		PosizionePlayer.setOpaque(false);
		
		MunizioniPlayer = new JTextField();
		MunizioniPlayer.setEditable(false);
		MunizioniPlayer.setColumns(10);
		MunizioniPlayer.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		MunizioniPlayer.setForeground(Color.WHITE);
		MunizioniPlayer.setOpaque(false);
//---------------------		
		Mosse = new JComboBox<>();
		Mosse.setMaximumRowCount(5);
		Mosse.addActionListener(this);
		
		Giocatori = new JComboBox<>();
		Giocatori.setMaximumRowCount(5);
		Giocatori.addActionListener(this);
		
		
		FineTurno = new JButton("FineTurno");
		FineTurno.addActionListener(this);
//-------------------------		
		
		Chat = new JTextArea();
		Chat.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		Chat.setForeground(Color.BLUE);
		Chat.setEditable(false);
		Chat.setLineWrap(true);
		Chat.setOpaque(false);
		Chat.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		
		
		ChatPanel = new JScrollPane(Chat);
		ChatPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		ChatPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		ChatPanel.getViewport().setOpaque(false);
		ChatPanel.setOpaque(false);
		
		
//------------------		
		ChatInvio = new JTextArea();
		ChatInvio.setTabSize(4);
		ChatInvio.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		ChatInvio.setForeground(Color.BLUE);
		ChatInvio.setLineWrap(true);
		ChatInvio.setOpaque(false);
		ChatInvio.addKeyListener(this);
		ChatInvio.setVisible(true);
		ChatInvio.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		
		ChatInvioPanel = new JScrollPane(ChatInvio);
		ChatInvioPanel.setOpaque(false);
		ChatInvioPanel.getViewport().setOpaque(false);
		
		 
		
		
//---------------------------------		
		JScrollPane ErrorPanel = new JScrollPane();
		Error = new JTextArea();
		Error.setEditable(false);
		Error.setLineWrap(true);
		Error.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		Error.setForeground(new Color(255, 0, 0));
		Error.setOpaque(false);
		ErrorPanel.setViewportView(Error);
		ErrorPanel.getViewport().setOpaque(false);
		ErrorPanel.setOpaque(false);
		
		
//---------------------------------		
		
		graphComponent = grafox.StampaGrafo(citta);	
		
		GraphPane = new JScrollPane(graphComponent);
		GraphPane.setOpaque(false);
		GraphPane.getViewport().setOpaque(false);
	//	GraphPane.setBorder(null);
		
		
		labelImageGrafox = new JLabel();
		setContentPane(labelImageGrafox);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(GraphPane, GroupLayout.DEFAULT_SIZE, 956, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(InfoPanel, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(MunizioniPlayer, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
								.addComponent(PosizionePlayer, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
								.addComponent(FazionePlayer, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
								.addComponent(DatiPersonali, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
								.addComponent(UserPlayer)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(Mosse, Alignment.LEADING, 0, 299, Short.MAX_VALUE)
								.addComponent(Giocatori, Alignment.LEADING, 0, 299, Short.MAX_VALUE)
								.addComponent(ChatPanel, 0, 0, Short.MAX_VALUE)
								.addComponent(ChatInvioPanel, Alignment.LEADING)
								.addComponent(ErrorPanel, Alignment.LEADING))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(FineTurno)))
					.addGap(4))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(13)
							.addComponent(GraphPane, GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(DatiPersonali, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(UserPlayer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(FazionePlayer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(PosizionePlayer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(MunizioniPlayer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addComponent(InfoPanel, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(Mosse, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(Giocatori, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addComponent(FineTurno, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(ChatPanel, GroupLayout.PREFERRED_SIZE, 358, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(ChatInvioPanel, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(ErrorPanel, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)))
					.addGap(1))
		);
		
		getContentPane().setLayout(groupLayout);
	}
	
	public Game(String ip, int port, ServerSocket socketServerChat,String username, Grafox grafox, String citta,String fazione,List<String>listNomiCitta,
			ConcurrentHashMap<String, String> errorCode, List<String> keyErrorCode )
	{
		setFocusTraversalPolicyProvider(true);
		getContentPane().setName("contentPane");
		this.ip=ip;
		this.port=port;
		this.socketServerChat = socketServerChat;
		this.username = username;
		new Read().execute();
		lockClassifica = new ReentrantLock();
		lockError = new ReentrantLock();
		this.listNomiCitta=listNomiCitta;
		this.grafox=grafox;
		this.citta=citta;
		this.fazione=fazione;
		this.errorCode=errorCode;
		this.keyErrorCode=keyErrorCode;
		setTitle("Spaghetti Western Distributed Multiplayer Game");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SetView();
		UserPlayer.setText("username --->"+username);
		FazionePlayer.setText("fazione --->"+fazione);
		PosizionePlayer.setText("posizione--->"+citta);
		
	}
	
	public void upDateDatiPersonaliMunizioni(int munizioni)
	{
		MunizioniPlayer.setText("munizioni--->"+munizioni);
	}
	
	public void upDateDatiPersonaliPosizione()
	{
		MunizioniPlayer.setText("posizione--->"+citta);
	}
	
	public void SetOutClient(ObjectOutputStream outClient) {
		this.outClient = outClient;
	}
	
	public void setFineTurno(boolean temp)
	{
		FineTurno.setEnabled(temp);
	}
	
	public void SetEnable()
	{
		Mosse.setEnabled(true);
		Giocatori.setEnabled(true);
		FineTurno.setEnabled(true);
	}
	
	public void SetDisable()
	{
		Mosse.setEnabled(false);
		Giocatori.setEnabled(false);
		FineTurno.setEnabled(false);
	}

	
	public void upDataCitta(List<String> adiacent)
	{
		Mosse.removeAllItems();
		if (adiacent.isEmpty())
			throw new IllegalAccessError();
		Mosse.addItem("-->CambiaCitta<--");
		for (String nomeCitta : adiacent) {
//			System.err.println(nomeCitta+"******************");
			Mosse.addItem(nomeCitta);
		}
	
	}
	
	public  void upDataGiocatoriTogli(String giocatore)
	{
//		System.err.println("/*/*/*/*/*/*/*/*/*/*/*/  togli" + giocatore);
		Giocatori.removeItem(giocatore);
	}
	
	public  void upDataGiocatoriAggiungi(String giocatore)
	{
//		System.err.println("/*/*/*/*/*/*/*/*/*/*/*/  metti");
		Giocatori.addItem(giocatore);
	}
	
	public  void upDataGiocatori(List<String> giocatori)
	{
//		System.err.println("/*/*/*/*/*/*/*/*/*/*/*/  togliMetti");
		Giocatori.removeAllItems();
		Giocatori.addItem("-->Attacca<--");
		for (String nomeGiocatore : giocatori) {
//			System.err.println(nomeGiocatore+"******************");
			Giocatori.addItem(nomeGiocatore);
		}		
	}
	
	public void LockClassifica()
	{
		 lockClassifica.lock();
	}
	
	public void UnLockClassifica()
	{
		 lockClassifica.unlock();
	}
	
	public void setClassifica(ConcurrentHashMap<String, InfoGiocatore> temp)
    {
        lockClassifica.lock();
        List<String> classifica= new LinkedList<>();
        info.setText("");
        for(String user : temp.keySet())
            classifica.add(user);
        bubbleSort(classifica, temp);
        info.append("Giocatore \t Punteggio\n");
        for(int i=classifica.size()-1 ; i>=0; i--)
        {
//            System.err.println("--->"+classifica.size());
//            System.err.println("--->"+i);
//            System.err.println("--->"+classifica.get(i));
//            System.err.println("--->"+temp.get(classifica.get(i)).getPunteggio());
            info.append(classifica.get(i)+"	: "+temp.get(classifica.get(i)).getPunteggio()+"\n");
        }
        lockClassifica.unlock();
    }

    public void bubbleSort(List<String> temp, ConcurrentHashMap<String, InfoGiocatore> hash)
    {
       // boolean swapped = true;
        for (int i = 0; i < temp.size() /*&& swapped*/; i++) {
          //  swapped = false;
            System.out.println("number of iteration" + i);

            for (int j = 0; j < temp.size()-1; j++) {

//                System.err.println("-||->"+(hash.get(temp.get(j)).getPunteggio() > hash.get(temp.get(j+1)).getPunteggio()));
//                System.err.println("-->"+hash.get(temp.get(j)).getPunteggio());
//                System.err.println("-->"+hash.get(temp.get(j+1)).getPunteggio());
                if (hash.get(temp.get(j)).getPunteggio() > hash.get(temp.get(j+1)).getPunteggio()) {
                    String aux = temp.get(j);
                    temp.set(j, temp.get(j+1));
                    temp.set(j+1, aux);
                    //swapped = true;
                }
            }
        }
    }

	public void resetError()
	{
		lockError.lock();
		Error.setText("");
		lockError.unlock();
	}
	

	public void upDateError1(String upDate)
	{
		lockError.lock();
		Error.append(upDate+"\n");
		lockError.unlock();
	}
	
	public void SetVisible()
	{
		setVisible(true);
		SwingUtilities.invokeLater(new Runnable()
		{
			
			@Override
			public void run() {
				

				BufferedImage imgGrafo = null;
				try {
					imgGrafo = ImageIO.read(ClassLoader.getSystemResource("ClientImage/GraphBackGroung.jpg"));
				} catch (IOException e) {
					System.err.println("errore lettura immagine "+e);
				}
								
				System.err.println("---->"+Integer.valueOf((int) getWidth()) + Integer.valueOf((int) getHeight()));
				Image dimgGrafo = imgGrafo.getScaledInstance(Integer.valueOf((int) getWidth()),Integer.valueOf((int) getHeight()),Image.SCALE_SMOOTH);
				ImageIcon backgroundImageGrafo = new ImageIcon(dimgGrafo);
				labelImageGrafox.setIcon(backgroundImageGrafo);
				
				
			}
		});
	}
	
	public void stopGame()
	{
		FineTurno.setEnabled(false);
		Mosse.setEnabled(false);
		Giocatori.setEnabled(false);
		ChatInvio.setEnabled(false);
	}

	public String NewPosCitta(){
		return citta;
	}
	
	
	private int indexOfCity(String citta)
	{
		int index=0;
		for (String nomeCitta : listNomiCitta) {
			if(nomeCitta.equals(citta))
				return index;
			index++;
		}
		throw new IndexOutOfBoundsException("Errore ricerca indice citta");
	}
	
	public void spegniIcona(String fazione, String citta)
	{
		System.err.println("Fazione -->"+fazione);
//		System.err.println("B-*-/-/-/-/-/-/*/*/*/*/*/"+grafox.getListVertex().get(indexOfCity(citta)).getCountBuoni());
//		System.err.println("c-*-/-/-/-/-/-/*/*/*/*/*/"+grafox.getListVertex().get(indexOfCity(citta)).getCountCattivi());
//		
		if(fazione.equals(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal()))))
		{
			grafox.getListVertex().get(indexOfCity(citta)).minuscountBuoni();
			if(grafox.getListVertex().get(indexOfCity(citta)).getCountBuoni()==0)
			{
				grafox.upDateGraphBuoni(citta, false);
			}
		}
		
		if(fazione.equals(errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal()))))
		{
			grafox.getListVertex().get(indexOfCity(citta)).minuscountCattivi();
			if(grafox.getListVertex().get(indexOfCity(citta)).getCountCattivi()==0)
			{
				grafox.upDateGraphCattivi(citta, false);
			}
		}
	}
	
	public void accendiIncona(String fazione, String newCity)
	{
//		System.err.println("Fazione -->"+fazione);
		if(fazione.equals(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal()))))
		{
			grafox.getListVertex().get(indexOfCity(newCity)).addcountBuoni();
//			System.err.println("----------"+getListVertex().get(indexOfCity(newCity)).getCountBuoni());
			if(grafox.getListVertex().get(indexOfCity(newCity)).getCountBuoni()>0)
			{
				grafox.upDateGraphBuoni(newCity, true);
			}
		}
		
		if(fazione.equals(errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal()))))
		{
			grafox.getListVertex().get(indexOfCity(newCity)).addcountCattivi();
//			System.err.println("----------"+grafox.getListVertex().get(indexOfCity(newCity)).getCountCattivi());
			if(grafox.getListVertex().get(indexOfCity(newCity)).getCountCattivi()>0)
			{
				grafox.upDateGraphCattivi(newCity, true);
			}
		}
	}
	
	
	@Override
    public void keyPressed(KeyEvent e) 
	{
		
        if (e.getKeyCode()==KeyEvent.VK_ENTER)
        {
        	new WriteChat().execute();
        }
    }

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}
	
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent arg0)
	{

		if (arg0.getSource()==FineTurno)
		{
			new SendComando(keyErrorCode.get(ComandoFineTurno.ordinal())).execute();
			return;
		}
		
		if (arg0.getSource()==Mosse)
		{				
			
			String newCity= ((JComboBox<String>)arg0.getSource()).getSelectedItem().toString();
			if(newCity.equals("-->CambiaCitta<--"))
				return;
			System.err.println("-->"+newCity);
			spegniIcona(fazione,citta);
			grafox.upDateGraph(citta, false);
			accendiIncona( fazione,  newCity);
			grafox.upDateGraph(newCity,true);
			citta=newCity;
			PosizionePlayer.setText("posizione-->"+citta);
			new SendComando(keyErrorCode.get(ComandoMossa.ordinal())+","+citta).execute();
			upDataCitta(grafox.adiecent(citta));
			Mosse.setEnabled(false);
			return;
		}
		
		if (arg0.getSource()==Giocatori)
		{		
			if (((JComboBox<String>)arg0.getSource()).getSelectedItem() == null)
				return;
			
			String giocatore= ((JComboBox<String>)arg0.getSource()).getSelectedItem().toString();
			
			if(giocatore.equals("-->Attacca<--"))
				return;
			if(giocatore.contains("["+fazione.charAt(0)+fazione.charAt(8)+"]"))
			{
				Error.setText(errorCode.get(keyErrorCode.get(ErroreStessaFazione.ordinal())));
				return;
			}
			new SendComando(keyErrorCode.get(ComandoAttacco.ordinal())+","+giocatore.substring(0, giocatore.length()-5)).execute();
			if(Mosse.isEnabled())
				Mosse.setEnabled(false);
			Giocatori.setEnabled(false);
		}
		
		
	}
	

	
	class Read extends SwingWorker<String, String>
	{

		@Override
		protected String doInBackground()  
		{
			String textRicevuto;
			while (true)
			{
				System.err.println("--->attesaconnsessione chat");
				try(Socket socketPartitaChat = socketServerChat.accept())
				{
					System.err.println("--->connessione chat");
					try (ObjectInputStream inStream = new ObjectInputStream(socketPartitaChat.getInputStream()))
					{
						textRicevuto = (String)inStream.readObject();
						System.err.println("---->ricevuto"+textRicevuto);
					} catch (ClassNotFoundException | IOException e) {
						System.err.println("Errore lettura messaggio chat " + e);
						break;
					}
					publish(textRicevuto);
					
				}catch (Exception e) {
					System.err.println("errore connessione chat "+e);
				}	
				System.err.println("--->Fine elaborazione richiseta chat");
			}
			return null;
		}
		
		@Override
		protected void process(List <String> arg0) {
			Chat.append(arg0.get(0));
			
		}
	}
	
	class WriteChat extends SwingWorker<String, String>
	{

		@Override
		protected String doInBackground()  {
			
			
			try (	Socket chat = new Socket(ip, port);
					ObjectOutputStream outStream = new ObjectOutputStream(chat.getOutputStream()))
			{
				String textApp = ChatInvio.getText().toString();
				if(!textApp.equals(""))
				{
					outStream.writeObject(username);
					outStream.writeObject(ChatInvio.getText().toString());
					ChatInvio.setText("");
				}
			} catch (IOException e) {
				System.err.println("Errore scrittura chat " + e );
			}
			
			return null;
		}
	}
	
	class SendComando extends SwingWorker<String, String>
	{
		private String comando;
		
		public SendComando(String comando) {
			this.comando=comando;
		}
		@Override
		protected String doInBackground()  {
			try {
				outClient.writeObject(comando);
			} catch (IOException e) {
				System.err.println("Errore scrittura chat " + e );
			}
			return null;
		}
	}
}
