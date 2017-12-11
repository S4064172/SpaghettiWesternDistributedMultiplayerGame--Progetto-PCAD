package ServerClientGraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Questa classe si occupa della 
 * gestione dei vertici della mappa
 * 
 */


public class Vertex implements Serializable{

	
	private static final long serialVersionUID = 2L;
	private List<String> neighborhood;
    private String name;	//nome del vertice
    private int distanceX,distanceY;	//posizione x,y nel frame esterno.
    private ReentrantLock lockCountBuoni;
    private ReentrantLock lockCountCattivi;
    
    private int countBuoni;
    private int countCattivi;
    
    public Vertex(String name, int distanceX, int distanceY)
    {	
    	this.distanceX=distanceX;
    	this.distanceY=distanceY;
        this.name = name;
        this.neighborhood = new LinkedList<String>();
        this.countBuoni=0;
        lockCountBuoni = new ReentrantLock();
        this.countCattivi=0;
        lockCountCattivi = new ReentrantLock();
    }
    
    public void addcountBuoni()
    {
    	lockCountBuoni.lock();
    	countBuoni++;
    	lockCountBuoni.unlock();
    }
    

    public void minuscountBuoni()
    {
    	lockCountBuoni.lock();
    	countBuoni--;
    	lockCountBuoni.unlock();
    	if(countBuoni<0)
    		throw new IllegalStateException("numero negativo di giocatori su un vertice");
    }
    
    public int getCountBuoni() 
    {
    	int app;
    	lockCountBuoni.lock();
    	app=countBuoni;
    	lockCountBuoni.unlock();
    	return app;
	}
    
    public void addcountCattivi()
    {
    	lockCountCattivi.lock();
   		countCattivi++;
    	lockCountCattivi.unlock();
    }
    

    public void minuscountCattivi()
    {
    	lockCountCattivi.lock();
    		countCattivi--;
    	lockCountCattivi.unlock();
    	if(countCattivi<0)
    		throw new IllegalStateException("numero negativo di giocatori su un vertice");
    }
    
    public int getCountCattivi() 
    {
    	int app;
    	lockCountCattivi.lock();
    	app=countCattivi;
    	lockCountCattivi.unlock();
    	return app;
	}
    
    public int getDistanceX() {
		return distanceX;
	}

	public void setDistanceX(int distanceX) {
		this.distanceX = distanceX;
	}

	public int getDistanceY() {
		return distanceY;
	}

	public void setDistanceY(int distanceY) {	
		this.distanceY = distanceY;
	}

    public void addNeighbor(String edge){	
        if(this.neighborhood.contains(edge)){
            return;
        }
        this.neighborhood.add(edge);
    }
    
    
    public boolean containsNeighbor(String other){	
        return this.neighborhood.contains(other);
    }
    
    public String getNeighbor(int index){	
        return this.neighborhood.get(index);
    }
    
 
    
    String removeNeighbor(int index){	
        return this.neighborhood.remove(index);
    }
    
    public void removeNeighbor(Edge e){
        this.neighborhood.remove(e);
    }
    
    public int getNeighborCount(){
        return this.neighborhood.size();
    }
    
    public String getName(){
        return this.name;
    }
    
    public String toString(){
        return "Vertex " + name;
    }
    
    public int hashCode(){
        return this.name.hashCode();
    }
    
    public boolean equals(Object other){
        if(!(other instanceof Vertex)){
            return false;
        }
        
        Vertex v = (Vertex)other;
        return this.name.equals(v.name);
    }
    
    public LinkedList<String> getNeighbors(){
        return new LinkedList<String>(this.neighborhood);
    }
    
}
