package tools.bis.resources;

import java.util.Queue;

public interface bisQueue {
	
	void shift(int start) throws Exception;
	
	void offer(Queue<bisQueueItem> toAdd,int end);
	
	bisQueueItem getState();
	
	boolean isEmpty();
	
	String getChr();
	
	void setChr(String chr);
	
	void flush();
	
	void propagateSourceChr();
	
	int nextPos();
	
	void setDownstream(bisQueue downstream);
	
	void setUpstream(bisQueue upstream);
	
	public int size();
}
