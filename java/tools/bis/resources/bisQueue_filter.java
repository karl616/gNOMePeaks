package tools.bis.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class bisQueue_filter implements bisQueue {

	private BufferedReader filterRegion;
//	private String activeChr,filterChr, nextChr;
	private String activeChr,filterChr;
//	private int filterStart,filterEnd,nextStart,nextEnd, bufferPos;
	private int filterStart,filterEnd, bufferPos;
	private HashMap<String, HashSet<Integer>> filter;
	private bisQueue upstream, downstream;
	private LinkedList<genomicInterval> regions;
	
	//TODO: add something that empties the filter... filter cleaner... is this needed... 
	//could be added directly before the sink
	
	public bisQueue_filter(BufferedReader filterRegion,bisQueue upstream){
		this.filterRegion=filterRegion;
		this.upstream=upstream;
		filter= new HashMap<String, HashSet<Integer>>();
		regions= new LinkedList<genomicInterval>();
		bufferPos=-1;
		setChr(upstream.getChr());
		filterChr=getChr();
		filterStart=-1;
		filterEnd=-1;
		
		advanceRegion();
	}
	
	private void addToBuffer(){
		String s;
		try{
			s=filterRegion.readLine();
		}catch(IOException ioe){
			s=null;
			System.err.println("Failed to read filter due to IOException:");
			System.err.println(ioe.toString());
		}
		if(s==null){
			regions.add(new genomicInterval(null, -1, -1));
		}else{
			regions.add(new genomicInterval(s));
//			String l[]=s.split("\t");
//			nextChr=l[0];
//			nextStart=Integer.parseInt(l[1]);
//			nextEnd=Integer.parseInt(l[2]);
		}
	}
	
	private void removeFromBuffer(){
		if(regions.size()>0){
			regions.removeFirst();
//			--bufferPos;
		}
	}
	
	private genomicInterval getBuffer(int n){
		if(n==0 && regions.size()>0){
			return regions.peek();
		}
		//this could go wrong... there is no break if n is huge
		while(n>=regions.size()){
			addToBuffer();
		}
		return regions.get(n);
	}
	
	private int getNextStartBuffer(int pos){
		int i=0;
		genomicInterval gi;
		while(true){
			gi=getBuffer(i);
			if(gi.getStart()>pos || gi.getStart()==-1){
				return i;
			}
			++i;
		}
	}
	
	
	private void goToChr(String chr){
		while(filterChr!=null && !filterChr.equals(chr)){
			advanceRegion();
		}
	}
	
	private void advanceRegion(){
		genomicInterval gi=getBuffer(getNextStartBuffer(filterEnd));
		
		filterChr=gi.getChr();
		filterStart=gi.getStart();
		filterEnd=gi.getStop();
	}
	
	
	private void advanceRegion(int pos){
		while(filterChr!=null &&filterChr.equals(activeChr) && pos<filterEnd){
			advanceRegion();
		}
//		regions.add(new genomicInterval(filterChr, filterStart, filterEnd));
	}
	
	public int getFilteredSizeFromStart(int start, int effectiveSize){
		long st=System.currentTimeMillis();
		int curSize=0, lastEnd=0,effectiveEnd=start,gap, i=getNextStartBuffer(start);
		genomicInterval gi;
		if(i>5){
			//take one step back
			i-=5;
		}else{
			i=0;
		}
		
		for(;curSize<effectiveSize;i++){
			gi=getBuffer(i);
			if(gi.getChr()==null){
				break;
			}
			if(start<gi.getStart()){
				if(lastEnd<start){
					lastEnd=start;
				}
				gap=gi.getStart()-lastEnd;
				if(gap>effectiveSize-curSize){
					gap=effectiveSize-curSize;
				}
				curSize+=gap;
				effectiveEnd=lastEnd+gap;
			}
			lastEnd=gi.getStop();
		}
		//System.err.println(System.currentTimeMillis()-st + "\tstart");
		if(curSize<effectiveSize){
			return lastEnd+effectiveSize-curSize-start;
		}else{
			return effectiveEnd-start;
		}
	}
	
	public int getFilteredSizeFromEnd(int end, int effectiveSize){
		long st=System.currentTimeMillis();
		int curSize=0, lastStart=Integer.MAX_VALUE, effectiveStart=end, gap, i=getNextStartBuffer(end);
		genomicInterval gi;
		
		for(;curSize<effectiveSize && i>-1;--i){
			gi=getBuffer(i);
			if(end>gi.getStop()){
				if(lastStart>end){
					lastStart=end;
				}
				gap=lastStart-gi.getStop();
				if(gap>effectiveSize-curSize){
					gap=effectiveSize-curSize;
				}
				curSize+=gap;
				effectiveStart=lastStart-gap;
			}
			lastStart=gi.getStart();
		}
		if(curSize<effectiveSize){
			effectiveStart=lastStart-effectiveSize+curSize;
		}

//		System.err.println(System.currentTimeMillis()-st + "\t" + regions.size() + "\tend");
		if(effectiveStart<0){
			return end;
		}else{
			return end-effectiveStart;
		}
	}
	
	public void cleanBuffer(String chr, int start){
		genomicInterval gi=getBuffer(0);
		while(gi.getChr()!=null && chr.equals(gi.getChr()) && gi.getStop()<start){
			removeFromBuffer();
			gi=getBuffer(0);
		}
	}
	
	public genomicInterval getNextInterval(){
		return regions.poll(); // not activated in advanceRegion()
	}
	
	@Override
	public void shift(int start) throws Exception {
		upstream.shift(start);
	}

	@Override
	public void offer(Queue<bisQueueItem> toAdd, int end) {
		// TODO chromosome breaks are not handled well?
		HashSet<Integer> chrFilter;
		String curChr=getChr();
		if(filter.containsKey(curChr)){
			chrFilter=filter.get(curChr);
		}else{
			chrFilter=new HashSet<Integer>();
			filter.put(curChr, chrFilter);
		}
		for(bisQueueItem b : toAdd){
			while(filterChr!=null &&b.getPos()>=filterEnd && curChr.equals(filterChr)){
				advanceRegion();
			} // asserts that b.getPos<filterEnd if the chromosome isn't switched
			if(filterChr==null || !curChr.equals(filterChr)){
				//if the filter chr changes, assume that it went to the next one. Not allowed to lag behind
				break;
			}
			if(b.getPos()>filterStart){
				chrFilter.add(b.getPos());
			}
		}
		downstream.offer(toAdd, end);
	}
	
	public HashSet<Integer> getChrFilter(String chr){
		if(filter.containsKey(chr)){
			return filter.get(chr);
		}else{
			return new HashSet<Integer>();
		}
	}

	@Override
	public bisQueueItem getState() {
		//should this be transparant?
		return upstream.getState();
	}

	@Override
	public boolean isEmpty() {
		return upstream.isEmpty();
	}

	@Override
	public String getChr() {
		return activeChr;
	}

	@Override
	public void setChr(String chr) {
		activeChr=chr;
	}

	@Override
	public void flush() {
		setChr(upstream.getChr());
		goToChr(getChr());
		downstream.flush();
	}

	@Override
	public void propagateSourceChr() {
		upstream.propagateSourceChr();
	}

	@Override
	public int nextPos() {
		if(this.getChr().equals(upstream.getChr())){
			return upstream.nextPos();
		}else{
			return -1;
		}
	}

	@Override
	public void setDownstream(bisQueue downstream){
		this.downstream=downstream;
		upstream.setDownstream(this);
	}
	@Override
	public void setUpstream(bisQueue upstream) {
		this.upstream=upstream;
	}

	@Override
	public int size() {
		return 0;
	}

}
