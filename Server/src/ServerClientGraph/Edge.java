package ServerClientGraph;

import java.io.Serializable;

public class Edge implements Comparable<Edge> , Serializable {

    private Vertex start, end;//vertici inizio e fine
    private Object oggetto;	//oggetto che contiene(generico)
    
    public Edge(Vertex start, Vertex end){
        this(start, end, null);
    }
    
    public Edge(Vertex start, Vertex end, Object oggetto){
        this.start = start;
        this.end = end;
        this.oggetto = oggetto;
    }
    
    public Vertex getNeighbor(Vertex current){
        if(!(current.equals(start) || current.equals(end))){
            return null;
        }
        
        return (current.equals(start)) ? end : start;
    }
    
    public Vertex getStart(){
        return this.start;
    }
    
    public Vertex getEnd(){
        return this.end;
    }
    
    
    public Object oggetto(){
        return this.oggetto;
    }
    
    public void setOggetto(Object oggetto){
        this.oggetto = oggetto;
    }
    
    public int compareTo(Edge other){
        return 0;//da completare
    }
    
    public String toString(){
        return "({" + start + ", " + end + "}, " + oggetto + ")";
    }
    
    public int hashCode(){
        return (start.getName() + end.getName()).hashCode(); 
    }
    
    public boolean equals(Object other){
        if(!(other instanceof Edge)){
            return false;
        }
        
        Edge e = (Edge)other;
        
        return e.start.equals(this.start) && e.end.equals(this.end);
    }   
}