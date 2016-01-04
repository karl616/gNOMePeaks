package tools.bis.resources;

import java.io.BufferedReader;
import java.util.LinkedList;
import java.util.Queue;

public class bisQueue_source implements bisQueue {
	
	private bisQueueItem nextItem;
	private BufferedReader source;
	private bisQueue downstream;
	private boolean isEmpty;
	private String chr;
	private int readCount;
	
	public bisQueue_source(BufferedReader source) throws Exception{
		this.source= source;
		isEmpty=false;
		chr="";
		readCount=0;
		this.readNext();
	}
	
	public void setDownstream(bisQueue downstream){
		this.downstream=downstream;
	}
	
	public void fastForwardToChr(String targetChr)throws Exception{
		while(!getChr().equals(targetChr) && !isEmpty){
			readNext();
		}
		if(getChr().equals(targetChr)){
			propagateSourceChr();
		}
	}
	

	private void readNext()throws Exception{
		String s= source.readLine();
		while(s!=null && (s.trim().startsWith("#") || s.trim().startsWith("track"))){
			s=source.readLine();
		}
		if(s==null){
			isEmpty=true;
		}else{
			String[] l= s.split("\t");
			if(!this.getChr().equals(l[0])){
				this.setChr(l[0]);
			}
			int pos=Integer.parseInt(l[1]);
			double part=Double.parseDouble(l[3]);
			int cov= Integer.parseInt(l[4]);
			int meth=(int)Math.round(part*cov/100);
			++readCount;
			nextItem=new bisQueueItem(pos,meth,cov-meth);
			isEmpty=false;
		}
	}

	@Override
	public void shift(int start) throws Exception{
		LinkedList<bisQueueItem> toOffer= new LinkedList<bisQueueItem>();
		final String curChr=getChr();
		if(curChr.equals(downstream.getChr())){
			while(!isEmpty && nextItem.getPos()<start && curChr.equals(getChr())){
				toOffer.add(nextItem);
				readNext();
			}
		}
		downstream.offer(toOffer, start);
	}


	@Override
	public void offer(Queue<bisQueueItem> toAdd, int end) {
		// Nothing can be offered
	}

	@Override
	public bisQueueItem getState() {
		return nextItem;
	}

	@Override
	public boolean isEmpty() {
		return isEmpty;
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
		nextItem= new bisQueueItem();
	}
	
	public void propagateSourceChr(){
		downstream.flush();
	}

	@Override
	public int nextPos() {
		return nextItem.getPos();
	}


	@Override
	public void setUpstream(bisQueue upstream) {
		// there is nothing upstream
	}


	public int size(){
		return readCount;
	}
	

}
