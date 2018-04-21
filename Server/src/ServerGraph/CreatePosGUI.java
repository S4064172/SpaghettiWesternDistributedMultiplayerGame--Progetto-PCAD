package ServerGraph;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
//classe che crea randomicamente le posizione dei grafi e gli oggetti(solo dati) che saranno neccessarie per creare la vera gui nei vari client

import ServerClientGraph.Edge;
import ServerClientGraph.PosGraph;
import ServerClientGraph.Vertex;

public class CreatePosGUI {
	private int nVertex,distanza, larghezzaX, larghezzaY, width, height;
	private List<Edge > listEdge;
	private Vector<Vertex > listVertex;
	
	private  List<String> ListNomiCitta;
	
	private final boolean Debug = false;
	private final String ClassName = this.getClass().getName();
	
	private void setListNomiCitta()
	{
		ListNomiCitta = new LinkedList<>();
		ListNomiCitta.add("Tornstone");
		ListNomiCitta.add("Proudridge");
		ListNomiCitta.add("Willowbranch");
		ListNomiCitta.add("Wolfbranch");
		ListNomiCitta.add("Mudhallow");
		ListNomiCitta.add("Limbo Cliff");
		ListNomiCitta.add("Crimelanding");
		ListNomiCitta.add("Wildbrook");
		ListNomiCitta.add("Whispercreek");
		ListNomiCitta.add("Skullbluff");
		ListNomiCitta.add("Angelbend");
		ListNomiCitta.add("Serenity Cliff");
		ListNomiCitta.add("Defiant Peaks");
		ListNomiCitta.add("Lost Lake");
		ListNomiCitta.add("Shadowbend");
		ListNomiCitta.add("Snakebrook");
		ListNomiCitta.add("Stagpass");
		ListNomiCitta.add("Dodgedowns");
		ListNomiCitta.add("Idlepass");
		ListNomiCitta.add("Bitterbluff");
		ListNomiCitta.add("Ruby Howl");
		ListNomiCitta.add("Evil's Banks");
		ListNomiCitta.add("Crow's Worth");
		ListNomiCitta.add("New Butte");
		ListNomiCitta.add("Shade Flats");
		ListNomiCitta.add("Smooth Gorge");
		ListNomiCitta.add("Blackwater");
		ListNomiCitta.add("Bull's Snag");
		ListNomiCitta.add("Violence Wood");
		ListNomiCitta.add("Cruelty Stead");
		
		
	}
	
	
	public CreatePosGUI(int nVertex, int distanza, int larghezzaX, int larghezzaY,int width,int height) {
		this.nVertex = nVertex;
		this.distanza=distanza;
		this.larghezzaX=larghezzaX;
		this.larghezzaY=larghezzaY;
		this.width=width;
		this.height=height;
		setListNomiCitta();
	}
	
	public PosGraph get_PosGUI()
	{
		PosGraph infoGraph = new PosGraph(listEdge, listVertex);
		return infoGraph;
	}
	
	
	public List<String> getListNomiCitta()
	{
		return ListNomiCitta;
	}
	public String cityAtIndex(int index)
	{
		return ListNomiCitta.get(index);
	}
	
	public int indexOfCity(String name)
	{
		int index=0;
		for (String string : ListNomiCitta) {
			if(string.equals(name))
				return index;
			index++;
		}
		throw new IndexOutOfBoundsException("Errore ricerca indice citta");
	}
	
	
	
	@SuppressWarnings("boxing")
	public boolean create_PosGraph_aux()//ritornera un bool che indica se é andato a buon fine oppure ci sará un nuovo tentativo di rifarlo z.z
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";
		
		Random randomGenerator = new Random();
		Vector<Vector<Integer>> OrigenVetex=new Vector<>(nVertex);//crea i n vertex
		listVertex = new Vector<>(nVertex);
		int value;
		int limit=10000;
		int counter_limit;
		int incrementatore_area;
		boolean check;
		for(int i=0;i<nVertex;i++)
		{
			Vector<Integer> aux = new Vector<>(2);//vettore di 2 per salvare x e y
			aux.addElement(0);
			aux.addElement(0);//setto a 0
			check=false;
	    	incrementatore_area=0;//ogni volta che in una certa area non é stato possibile generare gli origini ingrandisco l'area.
	    	do{
	    		counter_limit=0;//contatore di quante volte in una determinata area é stato tentato di generare le origini.
		    	do
		    	{
		    		//
		    		value=(i+incrementatore_area)*larghezzaX<this.width-larghezzaX?value=(i+incrementatore_area)*larghezzaX:(this.width-larghezzaX);
		    		if(Debug)
		    			System.err.println(PrintId+"---> value:"+value+" -->i:"+i+" larghezzaX:"+larghezzaX);
		    		if(value==0)
		    			aux.set(0, larghezzaX+randomGenerator.nextInt((larghezzaX+(1))));
		    		else
		    		aux.set(0, larghezzaX+randomGenerator.nextInt(value));
		    		aux.set(1, larghezzaY+randomGenerator.nextInt((height-(larghezzaY*3 ))));
		    		counter_limit++;
		    		check=checkAroundVertex(OrigenVetex, aux.get(0), aux.get(1));
		    	}while(!check && counter_limit<limit);//controllo che le posizioni non siano già state prese o si vadano a sovrapporre agli altri vertex
		    	incrementatore_area++;
	    	}while(counter_limit>=limit && ((i+incrementatore_area)*larghezzaX)<(this.width-larghezzaX));
	    	System.err.println(PrintId+"---> n:"+counter_limit+" incrementatore_area:"+incrementatore_area+" i:"+i+" value:"+value);
	    	if(counter_limit >=limit)
	    	{
	    		System.err.println(PrintId+"--->n maggiore di limit");
	    		return false;
	    	}
	    	listVertex.addElement(new Vertex(ListNomiCitta.get(i), aux.get(0), aux.get(1)));// faccio il check degli edge dopo (anche se segnala unused server!!)
	    	OrigenVetex.add(aux);
		}
		create_Edge();
		return true;
	}
	
	
	public void create_PosGraph()//genera casualmente le posizioni dei vertex(che saranno i nodi del grafo)
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";
		for(int i=0;i<100;i++)
			if(create_PosGraph_aux())
				return;
		System.err.println(PrintId+"--->impossibile creare il grafo con questi settaggi!! Diminuire i nodi o cambiare le dimensioni!!");
		System.exit(0);
		
	}
	private void create_Edge()
	{
		final String MethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		final String PrintId=ClassName+","+MethodName+": ";
		Random randomGenerator = new Random();
		Vector<Vector<Vertex> > intersectVertex=new Vector<>(nVertex);
		//@SuppressWarnings("unused")
		Retta retta;//per salvare i dati in formato 'retta' (teoricamente era per evitare le intersezioni tra le rette.)
		int nGenerator;
		int indexGenerator;
		listEdge=new LinkedList<>();
		for (int i = 0; i < nVertex; i++) 
		{
			nGenerator =randomGenerator.nextInt(3)+1;//da 1 a 3
			for (int j = 0; j < nGenerator; j++)
			{
				indexGenerator=randomGenerator.nextInt(nVertex);//index-> 0 to nVertex
				if (i==indexGenerator)
				{
					j--;
					continue;
				}
				retta = set_Retta(listVertex.get(i), listVertex.get(indexGenerator));
				Vector<Vertex> aux_intersect = new Vector<>(2);
				aux_intersect.addElement(listVertex.get(i));
				aux_intersect.addElement(listVertex.get(indexGenerator));
				Edge a = new Edge(listVertex.get(i), listVertex.get(indexGenerator));
				if( !(listVertex.get(i).containsNeighbor(listVertex.get(indexGenerator).getName())) )
					listVertex.get(i).addNeighbor(listVertex.get(indexGenerator).getName());
				if( !(listVertex.get(indexGenerator).containsNeighbor(listVertex.get(i).getName())) )
					listVertex.get(indexGenerator).addNeighbor(listVertex.get(i).getName());
				System.err.println(PrintId+listVertex.get(i).getName()+"<------->"+listVertex.get(indexGenerator).getName());
				listEdge.add(a);
				intersectVertex.addElement(aux_intersect);
			}
			
		}
	}
	
	private Retta set_Retta(Vertex v1, Vertex v2)
	{
		Retta retta = new Retta(v1.getDistanceX(), v1.getDistanceY(), v2.getDistanceX(), v2.getDistanceY());
		return retta;
	}
	
	private boolean checkAroundVertex(Vector<Vector<Integer> > OrigenVetex, int distanceX, int distanceY)
	{
		for (int i = 0; i < OrigenVetex.size(); i++) {
				if(!check_distance(distanceX, distanceY, OrigenVetex.get(i).get(0), OrigenVetex.get(i).get(1)))
					return false;
		}
		return true;
	}
	
	private boolean check_distance(int distanceX, int distanceY, int distance_check_X, int distance_check_Y)
	{
		if( (distanceX >= distance_check_X-distanza && distanceX <= distance_check_X+distanza) )//a destra X
			if( (distanceY >= distance_check_Y-distanza && distanceY <= distance_check_Y+distanza) )
				return false;
			return true;
	}

	private Vector<String> GetName(int nName)
	{
		Vector<String> vertexName = new Vector<String>();
		for (int i = 0; i < nName; i++) {
			vertexName.add("V"+Integer.toString(i+1));
		}
		return vertexName;
	}
}
