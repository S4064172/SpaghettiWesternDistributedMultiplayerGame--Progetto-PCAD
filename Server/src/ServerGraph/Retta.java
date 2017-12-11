package ServerGraph;


public class Retta {
	private int asseX_start;	//inizio asse x
	private int asseY_start;	//fine asse x
	private int asseX_end;		//inizio asse y
	private int asseY_end;		//fine asse y
	
	public Retta(int asseX_start, int asseY_start, int asseX_end, int asseY_end) {
		super();
		this.asseX_start = asseX_start;
		this.asseY_start = asseY_start;
		this.asseX_end = asseX_end;
		this.asseY_end = asseY_end;
	}
	
	public int getAsseX_start() {
		return asseX_start;
	}

	public void setAsseX_start(int asseX_start) {
		this.asseX_start = asseX_start;
	}

	public int getAsseY_start() {
		return asseY_start;
	}

	public void setAsseY_start(int asseY_start) {
		this.asseY_start = asseY_start;
	}

	public int getAsseX_end() {
		return asseX_end;
	}

	public void setAsseX_end(int asseX_end) {
		this.asseX_end = asseX_end;
	}

	public int getAsseY_end() {
		return asseY_end;
	}

	public void setAsseY_end(int asseY_end) {
		this.asseY_end = asseY_end;
	}
	public int getMinX()
	{
		return this.getAsseX_start()<this.getAsseX_end()?this.getAsseX_start():this.getAsseX_end();
	}
	public int getMinY()
	{
		return this.getAsseY_start()<this.getAsseY_end()?this.getAsseY_start():this.getAsseY_end();
	}
	public int getMaxX()
	{
		return this.getAsseX_start()>this.getAsseX_end()?this.getAsseX_start():this.getAsseX_end();
	}
	public int getMaxY()
	{
		return this.getAsseY_start()>this.getAsseY_end()?this.getAsseY_start():this.getAsseY_end();
	}
	public boolean Intersec(Retta r)
	{
		boolean aux = xContains(r);
		return aux;
	}
	public boolean IntersecAsseX(Retta r)
	{
		if (this.getMinX()>=r.getMinX() && r.getMaxX()>=this.getMinX())
			return true;
		if (this.getMinX()<=r.getMinX() && r.getMinX()<=this.getMaxX())
			return true;
		
		return false;
	}
	public boolean IntersecAsseY(Retta r)
	{
		if (this.getMinY()>=r.getMinY() && r.getMaxY()>=this.getMinY())
			return true;
		if (this.getMinY()<=r.getMinY() && r.getMinY()<=this.getMaxY())
			return true;
		
		return false;
	}
	public boolean xContains(Retta r) {
		if (IntersecAsseX(r) && IntersecAsseY(r))
			return true;
		return false;
	}
	
	public int getNumPassi(int lunghezza1,int lunghezza2)//ottengo la lunghezza della retta.
	{
		lunghezza1=absoluteInt(lunghezza1);
		lunghezza2=absoluteInt(lunghezza2);
		int lunghezza = lunghezza1>lunghezza2?lunghezza1:lunghezza2;
		int numPassi=10;
		while (lunghezza>numPassi)
		{
			numPassi=numPassi*10;
		}
		return numPassi;
	}
	public int absoluteInt(int num)
	{
		return num>0?num:(num*(-1));
	}
	
	
}
