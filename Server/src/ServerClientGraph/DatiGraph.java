package ServerClientGraph;


import java.util.List;
import java.util.Vector;


public class DatiGraph {
	Vector<Vertex > listVertex=null;	//vettori con tutti i vertici(in questo caso N vertex)
	Vector<Vertex > listGiocatori=null;
	List<Edge > listEdge=null;
	int width,height,larghezzaX,larghezzaY,distanza,nVertex;
	public Vector<Vertex> getListVertex() {
		return listVertex;
	}
	public void setListVertex(Vector<Vertex> listVertex) {
		this.listVertex = listVertex;
	}
	public Vector<Vertex> getListGiocatori() {
		return listGiocatori;
	}
	public void setListGiocatori(Vector<Vertex> listGiocatori) {
		this.listGiocatori = listGiocatori;
	}
	public List<Edge> getListEdge() {
		return listEdge;
	}
	public void setListEdge(List<Edge> listEdge) {
		this.listEdge = listEdge;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getLarghezzaX() {
		return larghezzaX;
	}
	public void setLarghezzaX(int larghezzaX) {
		this.larghezzaX = larghezzaX;
	}
	public int getLarghezzaY() {
		return larghezzaY;
	}
	public void setLarghezzaY(int larghezzaY) {
		this.larghezzaY = larghezzaY;
	}
	public int getDistanza() {
		return distanza;
	}
	public void setDistanza(int distanza) {
		this.distanza = distanza;
	}
	public int getnVertex() {
		return nVertex;
	}
	public void setnVertex(int nVertex) {
		this.nVertex = nVertex;
	}
	public void SetDatiGraph(Vector<Vertex> listVertex, Vector<Vertex> listGiocatori,
			List<Edge> listEdge, int width, int height, int larghezzaX,
			int larghezzaY, int distanza, int nVertex) {
		this.listVertex = listVertex;
		this.listGiocatori = listGiocatori;
		this.listEdge = listEdge;
		this.width = width;
		this.height = height;
		this.larghezzaX = larghezzaX;
		this.larghezzaY = larghezzaY;
		this.distanza = distanza;
		this.nVertex = nVertex;
	}
	
}
