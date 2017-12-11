package Client;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JProgressBar;


/*
 * Questa classe si occupa di caricare 
 * un immagine nell'attesa che la partita 
 * inizi
 * */

@SuppressWarnings("serial")
public class WaitGame extends JFrame {

	private BufferedImage image;
	private Image dimage;
	private ImageIcon backgroundImage;
	
	public WaitGame() 
	{
		setResizable(false);
		
		image= null;
		try {
			image = ImageIO.read(ClassLoader.getSystemResource("ClientImage/LoadGame.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dimage = image.getScaledInstance(450,300,Image.SCALE_SMOOTH);
		backgroundImage = new ImageIcon(dimage);
		
		setContentPane(new JLabel(backgroundImage));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap(278, Short.MAX_VALUE)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(117)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(130, Short.MAX_VALUE))
		);
		getContentPane().setLayout(groupLayout);
	}

	public void setVisibe()
	{
		setVisible(true);
	}
	
	public void close()
	{
		dispose();
	}
}
