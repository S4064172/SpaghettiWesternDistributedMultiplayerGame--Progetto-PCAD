package ClientGraph;


import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import ServerClientGraph.DatiGraph;
import ServerClientGraph.Edge;
import ServerClientGraph.Vertex;

/*
 * Questa classe si occupa della gestione della 
 * mappa di gioco
 * */

public class Grafox {


	private Object parent;
	private mxGraph graph;
	private Vector<Vertex > listVertex=null;
	private List<Edge > listEdge=null;
	private List<String> listCitta;
	private int larghezzaX,larghezzaY, nVertex;
	private String fazione;
	private Vector<Object> V_toPrint ;
	

	private Vector<Object> O_Personaggio_odierno= new Vector<>(nVertex);
	private Vector<Object> O_Buoni = new Vector<>(nVertex);
	private Vector<Object> O_Cattivi = new Vector<>(nVertex);
	private int indexPG=0;
	
	private String fazioneBuoni;
	
	private final boolean Debug=false;
	private final String ClassName = this.getClass().getName();
	
	public Grafox(DatiGraph d, mxGraph graph,List<String> listCitta,String fazione,String fazioneBuoni)  {
		this.fazioneBuoni = fazioneBuoni;
		this.listCitta=listCitta;			
		this.listVertex = d.getListVertex();
		this.listEdge = d.getListEdge();
		this.larghezzaX = d.getLarghezzaX();
		this.larghezzaY = d.getLarghezzaY();
		this.graph=graph;
		this.nVertex=d.getnVertex();
		this.fazione=fazione;
		
	}
	

	public int indexOfCity(String name)
	{
		int index=0;
		for (String string : listCitta) {
			if(string.equals(name))
				return index;
			index++;
		}
		throw new IndexOutOfBoundsException("Errore ricerca indice citta");
	}
	
	public mxGraphComponent StampaGrafo(String nameCitta)
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";
		
		final mxGraphComponent graphComponent;
		
		Map<String, Object> edgeStyle = graph.getStylesheet().getDefaultEdgeStyle();
        edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.NONE);
       
        //settaggio grafica vertici
        V_toPrint = new Vector<>(nVertex);
		
		parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
	
		try
		{	
			graph.setConnectableEdges(false);
			graph.setCellsEditable(false);
			graph.setCellsDeletable(false);
			graph.setCellsResizable(false);
			graph.setCellsCloneable(false);
			graph.setCellsDisconnectable(false);
			graph.setCellsLocked(true);
			graph.setCellsBendable(false);
			graph.setDropEnabled(false);
			graph.setSplitEnabled(false);
			graph.setAllowDanglingEdges(true);
			graph.setAllowLoops(false);
			graph.setVertexLabelsMovable(false);
			graph.setCellsSelectable(false);
			
			mxStylesheet styleSheet = graph.getStylesheet();
			Hashtable<String, Object> style;
			String myStleName ="";
			System.err.println(PrintId+"-----larghezzaX: "+larghezzaX+"--larghezzaY: "+larghezzaY);
			for (int i = 0; i < listVertex.size(); i++) 
			{
				myStleName = Integer.toString(i);

				style = new Hashtable<String,Object>();
				style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
				style.put(mxConstants.STYLE_IMAGE, "/ClientGraphCityImage/city"+(1+i)+".jpg");
				style.put(mxConstants.STYLE_FONTCOLOR, "red");
				style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_BOTTOM);
				style.put(mxConstants.STYLE_VERTICAL_ALIGN, 0);
				style.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "black");
				style.put(mxConstants.STYLE_LABEL_BORDERCOLOR, "green");
				styleSheet.putCellStyle(myStleName, style);
			
				V_toPrint.add(graph.insertVertex(parent, null,listVertex.get(i).getName(),listVertex.get(i).getDistanceX(), listVertex.get(i).getDistanceY(), larghezzaX,//20=posx,10posy,50grandezzaX,10altezza
					larghezzaY,myStleName));
		}
			
			String immaginePersonaggio = fazione.equals(fazioneBuoni)?"Buono":"Cattivo";
			if(Debug)
				System.out.println(PrintId+"immaginePersonaggio: "+immaginePersonaggio+" fazione: "+fazione+"");
			int larghezzaAltriOggetti=50;
			int larghezzaAltriOggettiY=larghezzaAltriOggetti/2;
			for (int i = 0; i < listVertex.size(); i++)
			{
				O_Personaggio_odierno.add(graph.insertVertex(parent, null,"",listVertex.get(i).getDistanceX()+(larghezzaX/2)-(larghezzaAltriOggettiY),listVertex.get(i).getDistanceY()+larghezzaAltriOggetti+10, larghezzaAltriOggetti,
							larghezzaAltriOggetti,"shape=image;image=/ClientGraphImage/Personaggio"+immaginePersonaggio + ".png"));
				graph.getModel().setVisible(O_Personaggio_odierno.get(i),false);
			
				O_Buoni.add(graph.insertVertex(parent, null,"Buoni",listVertex.get(i).getDistanceX(), listVertex.get(i).getDistanceY()-larghezzaAltriOggettiY-10, larghezzaAltriOggetti,
						larghezzaAltriOggetti,"shape=image;image=/ClientGraphImage/Buoni.png"));
				graph.getModel().setVisible(O_Buoni.get(i),false);
				
				O_Cattivi.add(graph.insertVertex(parent, null,"Cattivi",listVertex.get(i).getDistanceX()+larghezzaAltriOggetti, listVertex.get(i).getDistanceY()-larghezzaAltriOggettiY-10, larghezzaAltriOggetti,
						larghezzaAltriOggetti,"shape=image;image=/ClientGraphImage/Cattivi.png"));
				graph.getModel().setVisible(O_Cattivi.get(i),false);
			
			}
			for (int i = 0; i < listEdge.size(); i++)
					graph.insertEdge(parent, null, "", V_toPrint.get(getIndexV_toPrint(listEdge.get(i).getStart())), V_toPrint.get(getIndexV_toPrint(listEdge.get(i).getEnd())));
			
			graph.getModel().setVisible(O_Personaggio_odierno.get(indexOfCity(nameCitta)),true);
			if(Debug)
				System.err.println(PrintId+"---> size nomeCitta"+ listCitta.size());
			for (String city : listCitta) 
			{
				if(Debug)
					System.err.println(PrintId+"-->"+city+listVertex.get(indexOfCity(city)).getCountBuoni());
				if( listVertex.get(indexOfCity(city)).getCountBuoni() > 0 )
					graph.getModel().setVisible(O_Buoni.get(indexOfCity(city)),true);
				
				if(Debug)
					System.err.println(PrintId+"-->"+city+listVertex.get(indexOfCity(city)).getCountCattivi());
				if( listVertex.get(indexOfCity(city)).getCountCattivi() > 0 )
					graph.getModel().setVisible(O_Cattivi.get(indexOfCity(city)),true);
			}
			
			
		}
		finally
		{
			graph.setKeepEdgesInBackground(true);
			graph.getModel().endUpdate();
		}
		graphComponent = new mxGraphComponent(graph);
		Map<String, Object> style = graph.getStylesheet().getDefaultEdgeStyle();
		style.put(mxConstants.STYLE_ROUNDED, true);
	    style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ENTITY_RELATION);
	   
		
		graphComponent.getViewport().setOpaque(false);
		graphComponent.setOpaque(false);
		graphComponent.setDragEnabled(false);
		graphComponent.setConnectable(false);
		return graphComponent;
		
	}

	
	public Vector<Vertex> getListVertex()
	{
		return this.listVertex;
	}
	
	public void upDateGraphBuoni(String name,boolean bool)
	{
		if (name.equals(""))
			return;
		indexPG=indexOfCity(name);
		graph.getModel().setVisible(O_Buoni.get(indexPG),bool);
	}
	
	public void upDateGraphCattivi(String name,boolean bool)
	{
		if (name.equals(""))
			return;
		indexPG=indexOfCity(name);
		graph.getModel().setVisible(O_Cattivi.get(indexPG),bool);
	}
	
	public void upDateGraph(String name,boolean bool)
	{
		if (name.equals(""))
			return;
		indexPG=indexOfCity(name);
		graph.getModel().setVisible(O_Personaggio_odierno.get(indexPG),bool);
	}
	
	
	public List<String> adiecent(String name)
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";
		
		for (Vertex nameTemp : listVertex) {
			
			if(Debug)
				System.err.println(PrintId+"nameTemp.getName(): "+nameTemp.getName()+" name:"+name);
			if(nameTemp.getName().equals(name))
				return nameTemp.getNeighbors();
		}
		throw new IllegalStateException("Nome della citta non trovato");
		
	}

	private int getIndexV_toPrint(Vertex find)
	{
		for (int i = 0; i < listVertex.size(); i++) {
			if(find==listVertex.get(i))
				return i;
		}
		return -1;
	}
}