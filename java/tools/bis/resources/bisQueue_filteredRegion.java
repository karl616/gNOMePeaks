package tools.bis.resources;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class bisQueue_filteredRegion extends bisQueue_region {

	private bisQueue_filter filterQueue;
	private int nrFiltered, effectiveSize;
	private boolean useEffectiveSize;
	
	public bisQueue_filteredRegion(int size, bisQueue upstream) {
		super(size, upstream);
		nrFiltered=0;
		effectiveSize=size;
		// TODO Auto-generated constructor stub
	}
	
//	public bisQueue_filteredRegion(int size, bisQueue upstream, bisQueue_filter filterQueue){
//		this(size, upstream, filterQueue, true);
//	}
	
	public bisQueue_filteredRegion(int size, bisQueue upstream, bisQueue_filter filterQueue, boolean sizeUnfiltered){
		this(size,upstream);
		this.filterQueue=filterQueue;
		this.useEffectiveSize=sizeUnfiltered;
	}
	
	@Override
	public void shift(int start) throws Exception{
		if(this.getChr().equals(upstream.getChr())){
			if(useEffectiveSize){
				this.setWindowSize(filterQueue.getFilteredSizeFromStart(start, effectiveSize));
			}
			upstream.shift(start+this.getWindowSize());
		}else{
			downstream.offer(this.getOffer(start), start);
			curState.setPos(start);
		}	
	}
	
	private LinkedList<bisQueueItem> getOffer(int start){
		LinkedList<bisQueueItem> toOffer= new LinkedList<bisQueueItem>();
		HashSet<Integer> chrFilter= filterQueue.getChrFilter(getChr());
		while(!items.isEmpty() && items.peek().getPos()<start){
			bisQueueItem b= items.poll();
			if(chrFilter.contains(b.getPos())){
				--nrFiltered;
			}else{
				curState.subtract(b);
			}
			toOffer.add(b);
		}
		return toOffer;
	}
	
	@Override
	public void flush(){
		super.flush();
		nrFiltered=0;
	}
	
	public boolean isFiltered(){
		return nrFiltered>0;
	}
	
	@Override
	public void offer(Queue<bisQueueItem> toAdd, int end) {
		HashSet<Integer> chrFilter= filterQueue.getChrFilter(getChr());
		for(bisQueueItem b : toAdd){
			if(chrFilter.contains(b.getPos())){
				++nrFiltered;
			}else{
				curState.add(b);
			}
			items.add(b);
		}
		if(useEffectiveSize){
			this.setWindowSize(filterQueue.getFilteredSizeFromEnd(end, effectiveSize));
		}
		int start=end-super.getWindowSize();
		curState.setPos(start);
		downstream.offer(getOffer(start), start);		
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return items.size()-nrFiltered;
	}
}
