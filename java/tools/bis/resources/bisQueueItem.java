package tools.bis.resources;

public class bisQueueItem {

	private int pos,meth,unmeth;

	public bisQueueItem(){
		this(0,0,0);
	}
	
	public bisQueueItem(int pos, int meth, int unmeth) {
		super();
		this.pos = pos;
		this.meth = meth;
		this.unmeth = unmeth;
	}
	
	protected void add(bisQueueItem b){
		this.meth+=b.meth;
		this.unmeth+=b.unmeth;
	}
	
	protected void subtract(bisQueueItem b){
		this.meth-=b.meth;
		this.unmeth-=b.unmeth;
	}

	public int getPos() {
		return pos;
	}

	protected void setPos(int pos) {
		this.pos = pos;
	}

	public int getMeth() {
		return meth;
	}

	protected void setMeth(int meth) {
		this.meth = meth;
	}

	public int getUnmeth() {
		return unmeth;
	}

	protected void setUnmeth(int unmeth) {
		this.unmeth = unmeth;
	}
	
}
