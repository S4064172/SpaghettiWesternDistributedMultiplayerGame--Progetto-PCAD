package Client;


import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.border.Border;
import static ServerClient.EnumKeyErrorCode.*;
import java.awt.Font;

/*
 * Questa classe costruisce la grafica e la gestione
 * della fase di registrazione. 
 * I dati prima sono soggetti ad alcuni controlli
 * e poi sono spediti al server.
 * Gestisce solo la parte di invio dati.
 */

@SuppressWarnings("serial")
public class StartGame extends JFrame implements  ActionListener {
	
	
	private JTextArea Username;
	private JTextArea UsernameEditable;
	private JTextArea Password;
	private JTextArea PasswordEditable;
	private JTextArea Fazione;
	private JToggleButton FazioneEditable;
	private JTextArea Error;
	private JButton Connessione;
	private JButton Riconnessione;
	
	private ConcurrentHashMap<String, String> errorCode;
	private List<String> keyErrorCode;
	private ObjectOutputStream outClient;
	
	
	//porte dei servizi attivati
	private int port;
	private int portChat;
	private int portPingPong;
	private boolean isConnect;
	
	
	//stringe di memozizzazione dati temporanei
	private String username;
	private String password;
	private String fazione;
	
	//variabili per il caricamento dello sfondos
	private BufferedImage image;
	private Image dImgame;
	private ImageIcon backgroundImage;
	
	private void SetView()
	{
		image = null;
		try {
			image = ImageIO.read(ClassLoader.getSystemResource("ClientImage/Registrazione.jpg"));
		} catch (IOException e) {
			System.err.println("errore lettura immagine "+e);
		}
		dImgame = image.getScaledInstance(400,275,Image.SCALE_AREA_AVERAGING);
		backgroundImage = new ImageIcon(dImgame);
		
	
		setContentPane(new JLabel(backgroundImage));
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		Username = new JTextArea("Username") ;
		Username.setForeground(Color.WHITE);
		Username.setFont(new Font("Wide Latin", Font.PLAIN, 14));
		Username.setOpaque(false);
		Username.setEditable(false);
		
		Username.setBorder(BorderFactory.createCompoundBorder(border, 
		            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		UsernameEditable = new JTextArea("") ;
		UsernameEditable.setForeground(Color.WHITE);
		UsernameEditable.setFont(new Font("Wide Latin", Font.BOLD | Font.ITALIC, 13));
		UsernameEditable.setOpaque(false);
		UsernameEditable.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		Password = new JTextArea("PassWord") ;
		Password.setForeground(Color.WHITE);
		Password.setFont(new Font("Wide Latin", Font.PLAIN, 14));
		Password.setOpaque(false);
		Password.setEditable(false);
		Password.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		PasswordEditable = new JTextArea("") ;
		PasswordEditable.setForeground(Color.WHITE);
		PasswordEditable.setFont(new Font("Wide Latin", Font.BOLD | Font.ITALIC, 13));
		PasswordEditable.setOpaque(false);
		PasswordEditable.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		Fazione = new JTextArea("Fazione") ;
		Fazione.setForeground(Color.WHITE);
		Fazione.setFont(new Font("Wide Latin", Font.PLAIN, 13));
		Fazione.setOpaque(false);
		Fazione.setEditable(false);
		Fazione.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		FazioneEditable = new JToggleButton(errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal())));
		FazioneEditable.setForeground(Color.WHITE);
		FazioneEditable.setFont(new Font("Wide Latin", Font.BOLD | Font.ITALIC, 11));
		FazioneEditable.setContentAreaFilled(false);
		FazioneEditable.setVisible(true);
		FazioneEditable.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		Connessione = new JButton("Connettiti");
		Connessione.setFont(new Font("Wide Latin", Font.PLAIN, 11));
		Connessione.setForeground(Color.WHITE);
		Connessione.setContentAreaFilled(false);
		Connessione.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		Riconnessione = new JButton("Riconnettiti");
		Riconnessione.setForeground(Color.WHITE);
		Riconnessione.setFont(new Font("Wide Latin", Font.PLAIN, 11));
		Riconnessione.setContentAreaFilled(false);
		Riconnessione.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		Error = new JTextArea();
		Error.setForeground(Color.WHITE);
		Error.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
		Error.setOpaque(false);
		Error.setLineWrap(true);
		Error.setEditable(false);
		Error.setBorder(BorderFactory.createCompoundBorder(border, 
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		Connessione.addActionListener(this);
		Riconnessione.addActionListener(this);
		FazioneEditable.addActionListener(this);
		
		
		setTitle("FasePreliminare");
		setSize(400,300);
		setResizable(false);
		GroupLayout gl_p = new GroupLayout(getContentPane());
		gl_p.setHorizontalGroup(
			gl_p.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_p.createSequentialGroup()
					.addComponent(Username, GroupLayout.PREFERRED_SIZE, 196, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(UsernameEditable, GroupLayout.PREFERRED_SIZE, 196, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_p.createSequentialGroup()
					.addComponent(Password, GroupLayout.PREFERRED_SIZE, 196, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(PasswordEditable, GroupLayout.PREFERRED_SIZE, 196, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_p.createSequentialGroup()
					.addComponent(Fazione, GroupLayout.PREFERRED_SIZE, 196, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(FazioneEditable, GroupLayout.PREFERRED_SIZE, 196, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_p.createSequentialGroup()
					.addComponent(Connessione, GroupLayout.PREFERRED_SIZE, 196, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(Riconnessione, GroupLayout.PREFERRED_SIZE, 196, GroupLayout.PREFERRED_SIZE))
				.addComponent(Error, GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
		);
		gl_p.setVerticalGroup(
			gl_p.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_p.createSequentialGroup()
					.addGap(1)
					.addGroup(gl_p.createParallelGroup(Alignment.LEADING)
						.addComponent(Username, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
						.addComponent(UsernameEditable, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
					.addGap(2)
					.addGroup(gl_p.createParallelGroup(Alignment.LEADING)
						.addComponent(Password, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
						.addComponent(PasswordEditable, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
					.addGap(2)
					.addGroup(gl_p.createParallelGroup(Alignment.LEADING)
						.addComponent(Fazione, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
						.addComponent(FazioneEditable, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
					.addGap(2)
					.addGroup(gl_p.createParallelGroup(Alignment.LEADING)
						.addComponent(Connessione, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
						.addComponent(Riconnessione, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
					.addGap(2)
					.addComponent(Error, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
					.addGap(2))
		);
		getContentPane().setLayout(gl_p);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public StartGame( ObjectOutputStream out, int port,int portChat,int portPingPong,ConcurrentHashMap<String, String> errorCode,List<String> keyErrorCode)
	{
		this.errorCode=errorCode;
		this.keyErrorCode=keyErrorCode;
		this.isConnect=true;
		this.outClient = out;
		this.port=port;
		this.portChat=portChat;
		this.portPingPong=portPingPong;
		SetView();
			
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		
		if (arg0.getSource()==FazioneEditable)
		{
			
			if(FazioneEditable.getText().equals(errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal()))))
				FazioneEditable.setText(errorCode.get(keyErrorCode.get(FazioneBuoni.ordinal())));
			else
				FazioneEditable.setText(errorCode.get(keyErrorCode.get(FazioneCattivi.ordinal())));
			return;
		}
		
		username=UsernameEditable.getText();
		password=PasswordEditable.getText();
		fazione=FazioneEditable.getText();
		
		//Regione per il controllo dell'username e della password
		Pattern regEx = Pattern.compile("[A-Za-z0-9]*");
		Matcher matcher = regEx.matcher(username);
		
		if(!isConnect)
		{
			Error.setText(errorCode.get(keyErrorCode.get(ConnessioneAssente.ordinal())));
			return;
		}
		if (username.equals("") || password.equals(""))
		{
			Error.setText(errorCode.get(keyErrorCode.get(ErroreCampiRegistrazioneVuoti.ordinal())));
			return;
		}
		
		if(!matcher.matches())
		{
			Error.setText("l'username puo essere composto solo da lettere o numeri");
			return;
		}
		
		matcher.reset(password);
		if(!matcher.matches())
		{
			Error.setText("la password puo essere composta solo da lettere o numeri");
			return;
		}
		if(username.length()>7)
		{
			Error.setText("Username troppo lungo");
			return;
		}
		if(password.length()>7)
		{
			Error.setText("Password troppo lungo");
			return;
		}
		
		if (arg0.getSource()==Connessione)
		{
			System.err.println("-->InizioFaseConnessione");
//			System.err.println(errorCode.get(keyErrorCode.get(ConnessioneServer.ordinal())));
//			System.err.println("UserName "+username);
//			System.err.println("PassWord "+password);
//			System.err.println("Fazione "+fazione);
			new SendConnessione().execute();
			System.err.println("-->FineFaseConnsessione");
			return;
		}
		if (arg0.getSource()==Riconnessione)
		{
			System.err.println("-->InizioFaseRiconnessione");
//			System.err.println(errorCode.get(keyErrorCode.get(RiconnessioneServer.ordinal())));
//			System.err.println("UserName"+username);
//			System.err.println("PassWord"+password);
			new SendRiconnessione().execute();
			System.err.println("-->FineFaseRionnsessione");
			return;
		}
		
			
		
	}
	
	
	/*
	 * procedura che si occupa dell'invio dei dati nella fase di connessione
	 * */
	class SendConnessione extends SwingWorker<String, String>
	{
		@Override
		protected String doInBackground()
		{
			try 
			{
				outClient.writeObject(keyErrorCode.get(ConnessioneServer.ordinal()));
				outClient.writeObject(username);
				outClient.writeObject(password);
				outClient.writeObject(FazioneEditable.getText());
				
				outClient.writeObject(String.valueOf(port));
				outClient.writeObject(String.valueOf(portChat));
				outClient.writeObject(String.valueOf(portPingPong));
			} catch (IOException e) {
				System.err.println("ErroreInvioDati");
			}
			return null;
		}
	}
	
	/*
	 * procedura che si occupa dell'invio dei dati nella fase di riconnessione
	 * */
	class SendRiconnessione extends SwingWorker<String, String>
	{

		@Override
		protected String doInBackground()
		{
			try {
				outClient.writeObject(keyErrorCode.get(RiconnessioneServer.ordinal()));
				outClient.writeObject(username);
				outClient.writeObject(password);
				outClient.writeObject(String.valueOf(port));
				outClient.writeObject(String.valueOf(portChat));
				outClient.writeObject(String.valueOf(portPingPong));
			} catch (IOException e) {
				System.err.println("ErroreInvioDati");
			}	
			return null;
		}
		
		
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getFazione() {
		return fazione;
	}
	
	public void SetIsConnect(boolean isConnect)
	{
		this.isConnect=isConnect;
	}
	public void SetError(String text){
		Error.setText(text);
	}
	public void SetVisible(){
		setVisible(true);
	}
	
	public void Close()	{
		dispose();
	}
	
	public void SetDisable(){
		setVisible(false);
	}
}
