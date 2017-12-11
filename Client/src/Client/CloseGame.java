package Client;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;


/*
 * Questa classe viene utilizzata per 
 * stampare alla fine del gioco il o i
 * vincitori.
 * Questa classe non ha bisogno di particolari 
 * attenzioni alla concorrenza poiche viene 
 * utilizzata una sola volta ad ormai risultati
 * fissi.
 */


@SuppressWarnings("serial")
public class CloseGame extends JFrame {

		private BufferedImage image;
		private Image imageScalata;
		private ImageIcon backgroundImage;
		private JTextArea classifica;
		
		public CloseGame(String text) 
		{
			setResizable(false);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(100, 100, 200, 300);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			image = null;
			try {
				image = ImageIO.read(ClassLoader.getSystemResource("ClientImage/Classifica.jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//--Sistemare la scala in base alla grandezza della finestra
			imageScalata = image.getScaledInstance(200,300,Image.SCALE_SMOOTH);
			backgroundImage = new ImageIcon(imageScalata);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.getViewport().setOpaque(false);
			scrollPane.getVerticalScrollBar().setOpaque(false);
			scrollPane.setOpaque(false);
			setContentPane(new JLabel(backgroundImage));
			
			//settaggi della grafica
			GroupLayout groupLayout = new GroupLayout(getContentPane());
			groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addComponent(scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
			);
			groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.TRAILING)
					.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
						.addGap(53)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(62, Short.MAX_VALUE))
			);
			
			classifica = new JTextArea(text);
			scrollPane.setViewportView(classifica);
			classifica.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 18));
			classifica.setLineWrap(true);
			classifica.setEditable(false);
			classifica.setOpaque(false);
			
			getContentPane().setLayout(groupLayout);
			
			
		}
	
		public void SetVisible()
		{
			setVisible(true);
		}
}
