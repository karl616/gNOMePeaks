package tools.bis.resources;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class bisQueue_sink implements bisQueue {

	private bisQueueItem current;
	private bisQueue upstream;
	private String chr;
	private ArrayList<bisQueue_filter> filtersToClean;
	private int processCount;
	
	public bisQueue_sink(bisQueue upstream){
		this.setUpstream(upstream);
		this.setChr(upstream.getChr());
		this.current= new bisQueueItem();
		filtersToClean= new ArrayList<bisQueue_filter>();
		processCount=0;
	}
	
	public void addFilterToClean(bisQueue_filter f){
		filtersToClean.add(f);
	}
	
	@Override
	public void shift(int start) throws Exception{
		upstream.shift(start);
	}

	
	@Override
	public void offer(Queue<bisQueueItem> toAdd, int end) {
		processCount+=toAdd.size();
		if(toAdd.size()>0){
			if(toAdd instanceof LinkedList<?>){
				current=((LinkedList<bisQueueItem>) toAdd).getLast();
			}else{
				while(toAdd.size()>0){
					current=toAdd.poll();
				}
			}
			for (bisQueue_filter filter : filtersToClean) {
				filter.cleanBuffer(chr,current.getPos());
			}
		}
	}

	@Override
	public bisQueueItem getState() {
		return current;
	}

	@Override
	public boolean isEmpty() {
		return upstream.isEmpty();
	}

	@Override
	public String getChr() {
		return chr;
	}

	@Override
	public void setChr(String chr) {
		this.chr=chr;
	}

	@Override
	public void flush() {
		this.setChr(upstream.getChr());
	}
	
	public void propagateSourceChr(){
		upstream.propagateSourceChr();
	}

	@Override
	public int nextPos() {
		return current.getPos();
	}

	@Override
	public void setDownstream(bisQueue downstream) {
		upstream.setUpstream(this);		
	}
	
	public void connectQueue(){
		upstream.setDownstream(this);
	}

	@Override
	public void setUpstream(bisQueue upstream) {
		this.upstream=upstream;
		
	}

	@Override
	public int size() {
		return processCount;
	}

}
