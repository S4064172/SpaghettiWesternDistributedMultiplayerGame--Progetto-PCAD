package ServerClientGraph;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("serial")
public class PosGraph implements Serializable {
	
	private List<Edge > listEdge;
	private Vector<Vertex > listVertex;
	public PosGraph(List<Edge> listEdge, Vector<Vertex> listVertex) {
		super();
		this.listEdge = listEdge;
		this.listVertex = listVertex;
	}
	public List<Edge> getListEdge() {
		return listEdge;
	}
	public void setListEdge(List<Edge> listEdge) {
		this.listEdge = listEdge;
	}
	public Vector<Vertex> getListVertex() {
		return listVertex;
	}
	public void setListVertex(Vector<Vertex> listVertex) {
		this.listVertex = listVertex;
	}
	
}
