package ServerClientGraph;

import java.io.Serializable;


public class InfoGraph implements InformationGraph,Serializable {

	private static final long serialVersionUID = 3L;
	
	private int nVertex;
	private int distanza; 
	private int larghezzaX; 
	private int larghezzaY; 
	private int width;
	private int height;
	
	
	@Override
	public int getNVertex() {
		return nVertex;
	}

	@Override
	public int getDistanza() {
		return distanza;
	}

	@Override
	public int getLarghezzaX() {
		return larghezzaX;
	}

	@Override
	public int getLarghezzaY() {
		return larghezzaY;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

}
