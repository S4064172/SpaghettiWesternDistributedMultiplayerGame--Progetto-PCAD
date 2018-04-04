package Client; 

import java.awt.Image;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.LayoutStyle.ComponentPlacement;
import static ServerClient.EnumKeyErrorCode.*;
import java.awt.Color;

/*
 * Questa classe gestisce la parte grafica e 
 * la parte di invio comandi al server.
 * Il giocatore non è obblicato a selezionare 
 * una mossa; infatti in questo 
 * caso dopo tot secondi
 * viene fatta scleta in maniera automatica.
 * Per evita del loop infiniti le scelte sono
 * semivincolate
 * 
 */
@SuppressWarnings("serial")
public class Battle extends JFrame implements ActionListener{


	private JButton Attacca ;
	private JButton Difendi ;
	private JButton Ricarica;
	//riporta le principali informazioni
	private JTextArea textArea; 
	
	//contatori che memorizzano le scelte fatte
	private int nDifesa; //qunate volte posso difendermi
	private int nMunizioni; //munizioni che possi usare
	private int nRicaricate; //munizioni nel caricatore
	
	//memorizza lo stato dei bottoni per poter
	//attivare i bottoni corretti dopo la disattivazine
	private boolean statoPrec[];
	
	//indica se attendo una risposta dal server,
	//se true implica che ho fatto una mossa
	private boolean attesaRisposta=false;
	
	//procedura che forza la scelta
	private Timer timeOutScelta;
	
	private ObjectOutputStream outStream;
	private ConcurrentHashMap<String, String> errorCode;
	private List<String> keyErrorCode;
	private boolean IsStop;
	private Random randomGenerate = new Random();
	
	private final boolean Debug = false;
	private final String ClassName = this.getClass().getName();
	
	public int getNMunizioni()
	{
		return nMunizioni;
	}
	
	private void setView()
	{
		BufferedImage image;
		Image dimage;
		ImageIcon backgroundImage;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		image= null;
		
		try {
			image = ImageIO.read(ClassLoader.getSystemResource("ClientImage/Battle.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dimage = image.getScaledInstance(450,350,Image.SCALE_SMOOTH);
		backgroundImage = new ImageIcon(dimage);
		
		setBounds(100, 100, 450, 300);
		setContentPane(new JLabel(backgroundImage));
		Attacca = new JButton("Attacca");
		Attacca.addActionListener(this);
		
		Difendi = new JButton("Difendi");
		Difendi.addActionListener(this);
		
		Ricarica = new JButton("Ricarica");
		Ricarica.addActionListener(this);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setForeground(Color.BLACK);
		textArea.setOpaque(false);
	
		textArea.setText( errorCode.get(keyErrorCode.get(Munizioni.ordinal()))+": "+nMunizioni
						+"\n"+errorCode.get(keyErrorCode.get(NumDifesa.ordinal()))+" : "+nDifesa
						+"\n"+errorCode.get(keyErrorCode.get(NumRicaricate.ordinal()))+" : "+nRicaricate);
		GroupLayout gl_contentPane = new GroupLayout(getContentPane());
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(Attacca, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
					.addComponent(Difendi, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
					.addGap(32)
					.addComponent(Ricarica, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap(291, Short.MAX_VALUE)
					.addComponent(textArea, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(Ricarica, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
							.addComponent(Difendi, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
						.addComponent(Attacca, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 127, Short.MAX_VALUE)
					.addComponent(textArea, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))
		);
		getContentPane().setLayout(gl_contentPane);
	}
	
	
	public boolean isClose()
	{
		return IsStop;
	}
	public void close()
	{
		IsStop=true;	
		timeOutScelta.NotifyTimerClose();
		this.dispose();
	}
	
	public Battle( int nMunizioni, ObjectOutputStream outStream,ConcurrentHashMap<String, String> errorCode, List<String> keyErrorCode,String username,int TempoMossaAutomatica) 
	{
		setResizable(false);
		this.nDifesa=3;
		this.nMunizioni=nMunizioni;
		if(this.nMunizioni==0)
			this.nRicaricate=0;
		else
		{
			this.nMunizioni--;
			this.nRicaricate=1;
		}
		this.outStream=outStream;
		timeOutScelta=new Timer(TempoMossaAutomatica);
		timeOutScelta.start();
		this.IsStop=false;
		this.errorCode=errorCode;
		this.keyErrorCode=keyErrorCode;
		this.setTitle(username);
		setView();
		if(nMunizioni==0)
			Attacca.setEnabled(false);
		if(nRicaricate==0)
			Ricarica.setEnabled(false);
		
	}

	private void DisableAll()
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String printId=ClassName+","+MethodName+": ";
		if (attesaRisposta)
			return;
		if(Debug)
			System.err.println(printId+"-----------------------Disattivo--------------------------------");
		attesaRisposta=true;
		statoPrec = new boolean[3];
		statoPrec[0]=Difendi.isEnabled();
		statoPrec[1]=Ricarica.isEnabled();
		statoPrec[2]=Attacca.isEnabled();
		if(Debug)
			System.err.println(printId+"StatiPrec: "+statoPrec[0]+statoPrec[1]+statoPrec[2]);
		Difendi.setEnabled(false);
		Ricarica.setEnabled(false);
		Attacca.setEnabled(false);
	}
	
	public void EnableAll()
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String printId=ClassName+","+MethodName+": ";
		if(Debug)
			System.err.println(printId+"--------------------------Attivo-----------------------------");
		if(statoPrec==null)
			return;
		if(Debug)
			System.err.println(printId+"StatiPrec: "+statoPrec[0]+statoPrec[1]+statoPrec[2]);
		Difendi.setEnabled(statoPrec[0]);
		Ricarica.setEnabled(statoPrec[1]);
		Attacca.setEnabled(statoPrec[2]);
		if(Debug)
			System.err.println(printId+"Stati.isEmable: "+Difendi.isEnabled()+Ricarica.isEnabled()+Attacca.isEnabled());
		if (Difendi.isEnabled()!=statoPrec[0])
			throw new IllegalAccessError();
		if (Ricarica.isEnabled()!=statoPrec[1])
			throw new IllegalAccessError();
		if (Attacca.isEnabled()!=statoPrec[2])
			throw new IllegalAccessError();
		statoPrec = null;
		attesaRisposta=false;
		timeOutScelta.NotifyTimer();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		timeOutScelta.NotifyTimer();

		if(arg0.getSource()==Difendi)
		{
			nDifesa--;
			if(nDifesa==0)
				Difendi.setEnabled(false);
			DisableAll();
			new SendComando(keyErrorCode.get(SfidaDifesa.ordinal())).execute();
		}
		
		if(arg0.getSource()==Ricarica)
		{
			if(nDifesa<3)
				nDifesa++;
			if(nRicaricate==5)
				Ricarica.setEnabled(false);
			nRicaricate++;
			if (!Attacca.isEnabled())
				Attacca.setEnabled(true);
			if (!Difendi.isEnabled())
				Difendi.setEnabled(true);
			nMunizioni--;
			if(nMunizioni==0)
				Ricarica.setEnabled(false);
			DisableAll();
			new SendComando(keyErrorCode.get(SfidaRicarico.ordinal())).execute();
		}
		
		if(arg0.getSource()==Attacca)
		{
			if(nDifesa<3)
				nDifesa++;
			if (!Difendi.isEnabled())
				Difendi.setEnabled(true);
			if (!Ricarica.isEnabled() && nMunizioni!=0)
				Ricarica.setEnabled(true);
			nRicaricate--;
			if(nRicaricate==0)
				Attacca.setEnabled(false);
			DisableAll();
			new SendComando(keyErrorCode.get(SfidaAttacco.ordinal())).execute();
			
		}
	
		textArea.setText( " "+errorCode.get(keyErrorCode.get(Munizioni.ordinal()))+": "+nMunizioni
				+"\n"+	errorCode.get(keyErrorCode.get(NumDifesa.ordinal()))+" : "+nDifesa
				+"\n"+errorCode.get(keyErrorCode.get(NumRicaricate.ordinal()))+" : "+nRicaricate);
		timeOutScelta.NotifyTimer();
		
	}
	
	
	 /*
	    * durante la sfida questa procedura serve per generare
	    * una "mossa casuale"
	    * */
	class Timer extends Thread
	{
		private boolean timeOut;
		private int timer;
		private final String SubClassName= this.getClass().getName();
		
		Timer(int timer)
		{
			timeOut=true;
			this.timer = timer;
		}

		public boolean  isTimeOut() {
			return timeOut;
		}

		public synchronized void setConnect(boolean timeOut) {
			this.timeOut = timeOut; 
		}
		
		public synchronized void NotifyTimerClose()
		{
			final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
			final String printId=ClassName+"-"+SubClassName+","+MethodName+": ";
			System.err.println(printId+"-->SfidaInterrotto");
			this.notify();
			this.interrupt();
		}
		
		public synchronized void NotifyTimer()
		{
			timeOut=true;
			this.notify();
		}
		
		
		@Override
		public synchronized void run()
		{
			final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
			final String printId=ClassName+"-"+SubClassName+","+MethodName+": ";
			while(!IsStop)
			{	
				if(Debug)
					System.err.println(printId+"---->TimeOut :"+timeOut);
				while(timeOut)
				{
					timeOut=false;
					try {
						this.wait(timer);
					} catch (InterruptedException e) {
						System.err.println(printId+"-->errore wait mossa forzata");
					}
				}	
				if (!IsStop)
				{					
					if (!attesaRisposta)
					{
						List<String> tempScelte =  new LinkedList<>();
						if(Ricarica.isEnabled())
							tempScelte.add(keyErrorCode.get(SfidaRicarico.ordinal()));
						if(Attacca.isEnabled())
							tempScelte.add(keyErrorCode.get(SfidaAttacco.ordinal()));
						if(Difendi.isEnabled())
							tempScelte.add(keyErrorCode.get(SfidaDifesa.ordinal()));			
						int scelta;
						if (tempScelte.size()>1)
						{
							scelta =randomGenerate.nextInt(tempScelte.size()-1);
												
							if(tempScelte.get(scelta).equals(keyErrorCode.get(SfidaAttacco.ordinal())))
							{
								if(nDifesa<3)
									nDifesa++;
								if (!Difendi.isEnabled())
									Difendi.setEnabled(true);
								if (!Ricarica.isEnabled() && nMunizioni!=0)
									Ricarica.setEnabled(true);
								nRicaricate--;
								if(nRicaricate==0)
									Attacca.setEnabled(false);
							}
							
							if( tempScelte.get(scelta).equals(keyErrorCode.get(SfidaRicarico.ordinal())))
							{
								if(nDifesa<3)
									nDifesa++;
								if(nRicaricate==5)
									Ricarica.setEnabled(false);
								nRicaricate++;
								if (!Attacca.isEnabled())
									Attacca.setEnabled(true);
								if (!Difendi.isEnabled())
									Difendi.setEnabled(true);
								nMunizioni--;
								if(nMunizioni==0)
									Ricarica.setEnabled(false);
							}
							
							if(tempScelte.get(scelta).equals(keyErrorCode.get(SfidaDifesa.ordinal())))
							{
								nDifesa--;
								if(nDifesa==0)
									Difendi.setEnabled(false);
							}
							
							DisableAll();
							textArea.setText( " "+errorCode.get(keyErrorCode.get(Munizioni.ordinal()))+": "+nMunizioni
									+"\n"+	errorCode.get(keyErrorCode.get(NumDifesa.ordinal()))+" : "+nDifesa
									+"\n"+errorCode.get(keyErrorCode.get(NumRicaricate.ordinal()))+" : "+nRicaricate);
							timeOutScelta.NotifyTimer();
							System.err.println(printId+"-->scelata automatica"+tempScelte.get(scelta));
							new SendComando(tempScelte.get(scelta)).execute();
						}
						else
							new SendComando(keyErrorCode.get(HaiPerso.ordinal())).execute();
					}
				}
				timeOut=true;
			}
			
		}
	}
	
	class SendComando extends SwingWorker<String, String>
	{
		private String comando;
		private final String SubClassName = this.getClass().getName();
		public SendComando(String comando) {
			this.comando=comando;
		}
		@Override
		protected String doInBackground()
		{
			final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
			final String printId=ClassName+"-"+SubClassName+","+MethodName+": ";
			try {
				outStream.writeObject(comando);
			} catch (IOException e) {
				System.err.println(printId+"Errore invio comando");
			}
			return null;
		}
	}
	
}


