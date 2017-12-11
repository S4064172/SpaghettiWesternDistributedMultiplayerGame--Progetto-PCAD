package ServerClientGraph;

import java.io.Serializable;


public class InfoGraph implements InformationGraph,Serializable {

	private static final long serialVersionUID = 3L;
	
	private int nVertex;
	private int distanza=100; 
	private int larghezzaX=90; 
	private int larghezzaY=50; 
	private int width=1000;
	private int height=1000;
	
	
	public void setNVertex(int nVertex) {
		this.nVertex = nVertex;
	}
	
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
