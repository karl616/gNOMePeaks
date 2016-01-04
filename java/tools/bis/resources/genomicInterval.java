package tools.bis.resources;

public class genomicInterval {

	private String chr;
	private int start, stop;
	
	public genomicInterval(String s){
		String[] l=s.split("\t");
		this.chr= l[0];
		this.start= Integer.parseInt(l[1]);
		this.stop= Integer.parseInt(l[2]);
	}
	
	public genomicInterval(String chr, int start, int stop) {
		super();
		this.chr = chr;
		this.start = start;
		this.stop = stop;
	}

	public String getChr() {
		return chr;
	}

	public int getStart() {
		return start;
	}

	public int getStop() {
		return stop;
	}
	
	public int getSize() {
		return stop - start;
	}
}
