package tools.bis.resources;

import java.util.LinkedList;
import java.util.Queue;

public class bisQueue_region implements bisQueue {

	protected LinkedList<bisQueueItem> items;
	protected bisQueue upstream;
	protected bisQueue downstream;
	protected bisQueueItem curState;
	private int size;
	private String chr;
	
	
	public bisQueue_region(int size,bisQueue upstream){
		this.size=size;
		this.setUpstream(upstream);
		setChr(upstream.getChr());
		items= new LinkedList<bisQueueItem>();
		curState= new bisQueueItem();
	}
	
	public void setDownstream(bisQueue downstream){
		this.downstream=downstream;
		upstream.setDownstream(this);
	}
	
	public void shiftThisNext(int shiftSize)throws Exception{
		if(items.isEmpty()){
			int nextPos=upstream.nextPos();
			if(nextPos>0){
				if(nextPos<curState.getPos()){
					throw new Exception("Trying to go backwards: "+nextPos + "\t"+curState.getPos());
				}
				//find the first window that overlap the next item in the queue
				int nextStart= (nextPos/shiftSize)*shiftSize;
				while(nextStart+size-shiftSize>nextPos){
					nextStart-=shiftSize;
				}
				shiftThis(nextStart);
			}else{
				//time to shift chromosomes
				
				propagateSourceChr();
				shiftThisNext(shiftSize);
			}
		}else{
			shiftThis(curState.getPos()+shiftSize);
		}
		//if we enter an empty state, continue
		if(!this.isEmpty() && items.isEmpty()){
			shiftThisNext(shiftSize);
		}
	}
	
	public void shiftThis(int start)throws Exception{
		if(!downstream.getChr().equals(this.getChr())){
			downstream.flush();
		}
		shift(start);
	}

	@Override
	public void shift(int start) throws Exception{
		if(this.getChr().equals(upstream.getChr())){
			upstream.shift(start+size);
		}else{
			downstream.offer(this.getOffer(start), start);
			curState.setPos(start);
		}	
	}
	
	private LinkedList<bisQueueItem> getOffer(int start){
		LinkedList<bisQueueItem> toOffer= new LinkedList<bisQueueItem>();
		while(!items.isEmpty() && items.peek().getPos()<start){
			bisQueueItem b= items.poll();
			curState.subtract(b);
			toOffer.add(b);
		}
		return toOffer;
	}
	
	public void flush(){
		items.clear();
		setChr(upstream.getChr());
		curState=new bisQueueItem();
		downstream.flush();
	}
	
	public void propagateSourceChr(){
		upstream.propagateSourceChr();
	}

	@Override
	public void offer(Queue<bisQueueItem> toAdd, int end) {
		for(bisQueueItem b : toAdd){
			curState.add(b);
			items.add(b);
		}
		int start=end-size;
		curState.setPos(start);
		downstream.offer(getOffer(start), start);		
	}

	@Override
	public bisQueueItem getState() {
		return curState;
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty() && upstream.isEmpty();
	}

	@Override
	public String getChr() {
		return chr;
	}

	@Override
	public void setChr(String chr) {
		this.chr=chr;
	}
	
	public int getWindowSize(){
		return size;
	}
	
	public void setWindowSize(int size){
		this.size=size;
	}
	
	@Override
	public int nextPos(){
		if(items.isEmpty()){
			if(this.getChr().equals(upstream.getChr())){
				return upstream.nextPos();
			}else{
				return -1;
			}
		}else{
			return items.peek().getPos();
		}
	}

	@Override
	public void setUpstream(bisQueue upstream) {
		this.upstream=upstream;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return items.size();
	}

}
