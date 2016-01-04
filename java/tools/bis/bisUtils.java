package tools.bis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

import tools.bis.resources.bisQueueItem;
import tools.bis.resources.bisQueue_filter;
import tools.bis.resources.bisQueue_filteredRegion;
import tools.bis.resources.bisQueue_region;
import tools.bis.resources.bisQueue_sink;
import tools.bis.resources.bisQueue_source;
import tools.bis.resources.genomicInterval;
import tools.utils.general;



public class bisUtils {

	final static String sep= "\t";
	private static HashMap<String, String> methodsHelp= new HashMap<String, String>();
	
	
	
	public static void main(String[] args) throws Exception{
		methodsHelp.put("nomePeaksInitialBin", "nomePeaksInitialBin - prints counts for a sliding window of 'size' bp (shifted by 'step' bp) to the 'flank' bp up- and downstream regions. Due to a bug in java (version<1.7) gzip compressed with bgzip doesn't work by default == .gz files are not accepted \n\targs = <GCH output from Bissnp> <size> <step> <flank>\n");
		methodsHelp.put("nomePeaksFilteredBin", "nomePeaksFilteredBin - prints counts for a sliding window of 'size' bp (shifted by 'step' bp) to the 'flank' bp up- and downstream regions. Filters out sites in the filtered regions. Only prints windows were either flank overlap a filtered region. Due to a bug in java (version<1.7) gzip compressed with bgzip doesn't work by default == .gz files are not accepted \n\targs = <GCH output from Bissnp> <filtered regions (chr\\tstart\\tstop)> <size> <step> <flank>\n");
		methodsHelp.put("nomePeaksFilteredRegion", "nomePeaksFilteredRegion - prints counts for the defined windows and the 'flank' bp up- and downstream regions. Filters out sites in the flanking in the filtered regions. Only prints windows were either flank overlap a filtered region. Due to a bug in java (version<1.7) gzip compressed with bgzip doesn't work by default == .gz files are not accepted \n\targs = <GCH output from Bissnp> <target regions (chr\\tstart\\tstop)> <filtered regions (chr\\tstart\\tstop)> <flank>\n");
		methodsHelp.put("nomePeaksFilteredRegionEff", "nomePeaksFilteredRegionEff - prints counts for the defined windows and the 'flank' non-filtered bp up- and downstream regions. Filters out sites in the flanking in the filtered regions. Only prints windows were either flank overlap a filtered region. Due to a bug in java (version<1.7) gzip compressed with bgzip doesn't work by default == .gz files are not accepted \n\targs = <GCH output from Bissnp> <target regions (chr\\tstart\\tstop)> <filtered regions (chr\\tstart\\tstop)> <flank>\n");
		methodsHelp.put("gNOMe_addFDR", "gNOMe_addFDR - Due to a bug in java (version<1.7) gzip compressed with bgzip doesn't work by default == .gz files are not accepted \n\targs = <File with p-values in column 4> <file with random p-values> <total size of random p-value distribution> <q-value>\n");
		methodsHelp.put("gNOMe_getWindows", "gNOMe_getWindows - given windows in bed format, extract read counts in these and flanking regions of size flank bp. The bed file must be sorted in the same order as the Bissnp file. There is no control for this. Due to a bug in java (version<1.7) gzip compressed with bgzip doesn't work by default == .gz files are not accepted \n\targs = <GCH output from Bissnp> <windows bed file> <flank>\n");
		methodsHelp.put("hpSeq_biSplit", "hpSeq_biSplit - Reads a sam alignment without header and for read pairs starting at the same position, the frequencies of pair transitions are printed. Stratifies on the samflag \n\targs \n");
		
		
		if(args.length>0){
			if(args[0].equals("nomePeaksInitialBin")&&args.length==5){
				if(args[1].endsWith("gz")){
					System.err.println(methodsHelp.get(args[0]));
					System.exit(616);
				}
				nomePeaksInitialBin(general.getBufferedReader(args[1]),Integer.parseInt(args[2]),
						Integer.parseInt(args[3]),Integer.parseInt(args[4]));
			}else if(args[0].equals("hpSeq_biSplit")&&args.length==1){
				hpSeq_biSplit(new BufferedReader(new InputStreamReader(System.in)));
//				hpSeq_biSplit(new BufferedReader(new FileReader("/home/karln/Desktop/test.sample")));
			}else if(args[0].equals("nomePeaksFilteredBin")&&args.length==6){
				if(args[1].endsWith("gz")||(args[1].equals("-") && args[2].equals("-"))){
					System.err.println(methodsHelp.get(args[0]));
					System.exit(616);
				}
				nomePeaksFilteredBin(general.getBufferedReader(args[1]),general.getBufferedReader(args[2]),
						Integer.parseInt(args[3]),Integer.parseInt(args[4]),Integer.parseInt(args[5]));
			}else if(args[0].equals("nomePeaksFilteredRegion")&&args.length==5){
				if(args[1].endsWith("gz")||args[2].endsWith("gz")||args[3].endsWith("gz") ||((args[1].equals("-")?1:0)+ (args[2].equals("-")?1:0)+(args[3].equals("-")?1:0))>1){
					System.err.println(methodsHelp.get(args[0]));
					System.exit(616);
				}
				nomePeaksFilterRegion(general.getBufferedReader(args[1]), general.getBufferedReader(args[2]), general.getBufferedReader(args[3]), Integer.parseInt(args[4]),false);
			}else if(args[0].equals("nomePeaksFilteredRegionEff")&&args.length==5){
				if(args[1].endsWith("gz")||args[2].endsWith("gz")||args[3].endsWith("gz") ||((args[1].equals("-")?1:0)+ (args[2].equals("-")?1:0)+(args[3].equals("-")?1:0))>1){
					System.err.println(methodsHelp.get(args[0]));
					System.exit(616);
				}
				nomePeaksFilterRegion(general.getBufferedReader(args[1]), general.getBufferedReader(args[2]), general.getBufferedReader(args[3]), Integer.parseInt(args[4]),true);
			}else if(args[0].equals("gNOMe_addFDR")&&args.length==5){
				if(args[1].endsWith("gz")||(args[1].equals("-") && args[2].equals("-"))){
					System.err.println(methodsHelp.get(args[0]));
					System.exit(616);
				}
				gNOMe_addFDR(general.getBufferedReader(args[1]), general.getBufferedReader(args[2]),Long.parseLong(args[3]), Double.parseDouble(args[4]));
			}else if(args[0].equals("gNOMe_getWindows")&&args.length==4){
				if(args[1].endsWith("gz")||(args[1].equals("-") && args[2].equals("-"))){
					System.err.println(methodsHelp.get(args[0]));
					System.exit(616);
				}
				gNOMe_getWindows(general.getBufferedReader(args[1]), general.getBufferedReader(args[2]), Integer.parseInt(args[3]));
			}else if(args[0].equals("testGZreader")&&args.length==2){
				BufferedReader in= general.getBufferedReader(args[1]);
				for(String s=in.readLine();s!=null;s=in.readLine()){
					System.out.println(s);
				}
			}else if(args[0].equals("nextMethod")&&args.length==5){

			}else{
				System.err.println(printHelp(args[0]));
				System.exit(616);
			}
		}else{
			System.err.println(printHelp());
			System.exit(616);
		}
	}
	
		
	private static String printHelp(String cmd){
		if(methodsHelp.containsKey(cmd)){
			return methodsHelp.get(cmd);
		}else{
			return printHelp();
		}
	}
	
	private static String printHelp(){
		String help="";
		ArrayList<String> cmds= new ArrayList<String>(methodsHelp.keySet());
		Collections.sort(cmds);
		for (String s : cmds) {
			help+=methodsHelp.get(s);
		}
		
		return help;
	}
	
	private static void hpSeq_biSplit(BufferedReader samFile) throws Exception{
//		HashMap<sam flags, HashMap<transition, count>>
		int firstStrand=64;
		HashMap<String, HashMap<String, Integer>> transitionCounts= new HashMap<String, HashMap<String,Integer>>();
		HashMap<String, Integer> localCount;
		ArrayList<String[]> lines= new ArrayList<String[]>();
		HashMap<String, String[]> first= new HashMap<String, String[]>(), 
				second= new HashMap<String, String[]>();
		String pos="",curPos, samFlags, seq1, seq2;
		StringBuffer subSeq1,subSeq2,key;
		String[] l,l1,l2;
		int count=0,count2=0;
		System.err.println("Starting analysis");
		for(String s=samFile.readLine();s!=null;s=samFile.readLine()){
			++count;
			if(count%10000==0){
				System.err.print(count+"    "+count2+"\r");
			}
			l=s.split("\t");
			curPos=l[2]+"_"+l[3];
			if(!pos.equals(curPos)){
				if(lines.size()>1){
//					do the counting dance...
					first.clear();
					second.clear();
					for (String[] line : lines) {
//						split into first and second read
//						int a=Integer.parseInt(line[1]);
//						int b= a & firstStrand;
						

						if((((int) Integer.parseInt(line[1])) & firstStrand) == firstStrand){
							first.put(l[0], line);
						}else{
							second.put(l[0], line);
						}
					}
					for(String id : first.keySet()){
						if(second.containsKey(id)){
							++count2;
							l1= first.get(id);
							l2= second.get(id);
							samFlags= l1[1] + "_" + l2[1];
							if(transitionCounts.containsKey(samFlags)){
								localCount= transitionCounts.get(samFlags);
							}else{
								localCount= new HashMap<String, Integer>();
								transitionCounts.put(samFlags, localCount);
							}
							seq1=l1[9];
							seq2=l2[9];
							subSeq1= new StringBuffer("N" + seq1.charAt(0));
							subSeq2= new StringBuffer("N" + seq2.charAt(0));
							for(int i=1; i<seq1.length() && i< seq2.length();++i){
								subSeq1.deleteCharAt(0);
								subSeq1.append(seq1.charAt(i));
								subSeq2.deleteCharAt(0);
								subSeq2.append(seq2.charAt(i));
								key= new StringBuffer(subSeq1);
								key.append('/');
								key.append(subSeq2);
								if(localCount.containsKey(key.toString())){
									localCount.put(key.toString(), localCount.get(key.toString())+1);
								}else{
									localCount.put(key.toString(), 1);
								}
							}
						}
					}
				}
				lines= new ArrayList<String[]>();
				pos=curPos;
			}
			if(l[6].equals("=") && l[3].equals(l[7])){
				lines.add(l);
			}
		}
		System.err.println(count+"    "+count2);
		
		System.err.println("Size of db: "+ transitionCounts.size());
//		print the counts
		for(String sf : transitionCounts.keySet()){
			localCount= transitionCounts.get(sf);
			for(String t : localCount.keySet()){
				System.out.println(sf + sep + t + sep + localCount.get(t));
			}
		}
	}
	
	private static void gNOMe_getWindows(BufferedReader GCHfile, BufferedReader windows, final int flank) throws Exception{
		String s= windows.readLine();
		genomicInterval nextTarget= new genomicInterval(s);
		
		bisQueue_source source = new bisQueue_source(GCHfile);
		bisQueue_region upstream = new bisQueue_region(flank, source);
		bisQueue_region target = new bisQueue_region(nextTarget.getStop()-nextTarget.getStart(),upstream);
		bisQueue_region downstream = new bisQueue_region(flank,target);
		bisQueue_sink sink = new bisQueue_sink(downstream);
		
		sink.connectQueue();
		
		while(s!=null){
			nextTarget= new genomicInterval(s);
			target.setWindowSize(nextTarget.getStop()-nextTarget.getStart());
			if(!target.getChr().equals(nextTarget.getChr())){
				source.fastForwardToChr(nextTarget.getChr());
			}
			target.shiftThis(nextTarget.getStart());
			bisQueueItem t=target.getState(),
					u=upstream.getState(),
					d=downstream.getState();
			int am=t.getMeth(),
					au=t.getUnmeth(),
					bm=u.getMeth()+d.getMeth(),
					bu=u.getUnmeth() + d.getUnmeth();
			
			System.out.println(target.getChr() + sep + t.getPos() + sep+ (t.getPos() + target.getWindowSize()) + 
					sep + am + sep + au + sep + bm + sep + bu);
			
			s= windows.readLine();
		}
	}
	
	private static void gNOMe_addFDR(BufferedReader peaks, BufferedReader pDist, long pDistSize, double qValue)throws Exception{
		ArrayList<Double> tn= new ArrayList<Double>(750000),
				tpP= new ArrayList<Double>(7500000);
		ArrayList<String> tpS= new ArrayList<String>(7500000);
		HashMap<Double, Double> fdr= new HashMap<Double, Double>(7500000);
		for(String s=pDist.readLine();s!=null;s=pDist.readLine()){
			tn.add(Double.parseDouble(s));
		}
		for(String s=peaks.readLine();s!=null;s=peaks.readLine()){
			Double key= Double.parseDouble(s.split("\t")[3]);
			tpP.add(key);
			if(fdr.containsKey(key)){
				fdr.put(key, fdr.get(key)+1.0);
			}else{
				fdr.put(key, 1.0);
			}
			tpS.add(s);
		}
		Collections.sort(tn);
		TreeSet<Double> keys= new TreeSet<Double>();
		keys.addAll(fdr.keySet());
		Double tp_count=0.0; // has to be a double to keep the precision
		int tn_pos=0;
		for(Double key : keys){
			tp_count+= fdr.get(key);
			while(tn_pos<tn.size() && tn.get(tn_pos)<=key){
				tn_pos++;
			}
			if(tn_pos==tn.size()){
				System.err.println("Maximum treated p-value: "+key);
				break;
			}
			fdr.put(key,  (((double)tpS.size())/pDistSize)*(((double)tn_pos)/tp_count));
			if( (((double)tpS.size())/pDistSize)*(((double)tn_pos)/tp_count)>1){
				System.err.println((((double)tpS.size())/pDistSize)*((double)tn_pos/tp_count)+sep+tn_pos+sep+tpS.size()+sep+tp_count+sep+pDistSize);
			}
		}
		for(int i=0;i<tpS.size();++i){
			if(fdr.containsKey(tpP.get(i))){
				Double fdr_value=fdr.get(tpP.get(i));
				if(fdr_value<=qValue ){
					System.out.println(tpS.get(i)+sep+fdr_value);
				}
			}
		}
	}

	private static void nomePeaksFilterRegion(BufferedReader GCHfile, BufferedReader peakReader,BufferedReader filterReader, final int flank, boolean effectiveSize)throws Exception{
		genomicInterval nextInterval= new genomicInterval(peakReader.readLine());
		
		bisQueue_source source= new bisQueue_source(GCHfile);
		bisQueue_filter filter= new bisQueue_filter(filterReader, source);
		bisQueue_filteredRegion upstream= new bisQueue_filteredRegion(flank, filter, filter, effectiveSize);
		bisQueue_region target= new bisQueue_region(nextInterval.getSize(),upstream);
		bisQueue_filteredRegion downstream= new bisQueue_filteredRegion(flank, target, filter, effectiveSize);
		bisQueue_sink sink= new bisQueue_sink(downstream);
		
		sink.addFilterToClean(filter);
		
		sink.connectQueue();
		

		
		target.shiftThis(0);
		
		while(!target.isEmpty()){
			if(!target.getChr().equals(nextInterval.getChr())){
				source.fastForwardToChr(nextInterval.getChr());
			}
//			
//			while(!target.isEmpty() && !target.getChr().equals(nextInterval.getChr())){
//				System.err.println("Skipped " + target.getChr()+". All files have to be sorted the same way.");
//				target.propagateSourceChr();
//			}
			if(target.isEmpty()){
				break;
			}
			target.setWindowSize(nextInterval.getSize());
			target.shiftThis(nextInterval.getStart());
			
			bisQueueItem t=target.getState(),
					u=upstream.getState(),
					d=downstream.getState();
			int am=t.getMeth(),
					au=t.getUnmeth(),
					an=target.size(),
					bm=u.getMeth()+d.getMeth(),
					bu=u.getUnmeth() + d.getUnmeth(),
					bn=upstream.size() + downstream.size();
			

			if(am*(bm+bu)>bm*(am+au)){
				//only print when the frequency is greater
				System.out.println(target.getChr() + sep + t.getPos() + sep+ (t.getPos() + target.getWindowSize()) + 
						sep + am + sep + au + sep + an + sep + bm + sep + bu + sep + bn);
				//+ sep + upstream.getWindowSize() + sep + downstream.getWindowSize());
			}
			String s= peakReader.readLine();
			if(s==null){
				break;
			}else{
				nextInterval= new genomicInterval(s);
			}
		}
	}
	
	private static void nomePeaksFilteredBin(BufferedReader GCHfile, BufferedReader peakReader,final int size, final int step, final int flank)throws Exception{
		bisQueue_source source= new bisQueue_source(GCHfile);
		bisQueue_filter filter= new bisQueue_filter(peakReader, source);
		bisQueue_filteredRegion upstream= new bisQueue_filteredRegion(flank, filter, filter, false);
		bisQueue_region target= new bisQueue_region(size,upstream);
		bisQueue_filteredRegion downstream= new bisQueue_filteredRegion(flank, target, filter, false);
		bisQueue_sink sink= new bisQueue_sink(downstream);
		
		sink.addFilterToClean(filter);
		
		sink.connectQueue();
		
		target.shiftThis(0);
//		genomicInterval curInterval= filter.getNextInterval();
		
		while(!target.isEmpty()){
			target.shiftThisNext(step);
			if(upstream.isFiltered() || downstream.isFiltered()){
				bisQueueItem t=target.getState(),
						u=upstream.getState(),
						d=downstream.getState();
				int am=t.getMeth(),
						au=t.getUnmeth(),
						bm=u.getMeth()+d.getMeth(),
						bu=u.getUnmeth() + d.getUnmeth();
				
				if(am*(bm+bu)>bm*(am+au)){
					//only print when the frequency is greater
					System.out.println(target.getChr() + sep + t.getPos() + sep+ (t.getPos() + target.getWindowSize()) + 
							sep + am + sep + au + sep + bm + sep + bu);
				}
			}
		}
	}
	
	private static void nomePeaksInitialBin(BufferedReader GCHfile,final int size,final int step,final int flank)throws Exception{
		bisQueue_source source= new bisQueue_source(GCHfile);
		bisQueue_region upstream= new bisQueue_region(flank,source);
		bisQueue_region target= new bisQueue_region(size,upstream);
		bisQueue_region downstream= new bisQueue_region(flank,target);
		bisQueue_sink sink= new bisQueue_sink(downstream);
		
		sink.connectQueue();
		
		
		
		
		while(!target.isEmpty()){
			target.shiftThisNext(step);
			bisQueueItem t=target.getState(),
					u=upstream.getState(),
					d=downstream.getState();
			int am=t.getMeth(),
					au=t.getUnmeth(),
					bm=u.getMeth()+d.getMeth(),
					bu=u.getUnmeth() + d.getUnmeth();
			
			if(am*(bm+bu)>bm*(am+au)){
				//only print when the frequency is greater
				System.out.println(target.getChr() + sep + t.getPos() + sep+ (t.getPos() + target.getWindowSize()) + 
						sep + am + sep + au + sep + bm + sep + bu);
			}
			
			
//			FishersExactTest test= new FishersExactTest(new ContingencyTable2x2(new int[][] {{1,1},{1,1}}));
//			test= new FishersExactTest(new ContingencyTable2x2(new int[][] {{1,1},{1,0}}));
//			test= new FishersExactTest(new ContingencyTable2x2(new int[][] {{1,1},{0,1}}));
//			test= new FishersExactTest(new ContingencyTable2x2(new int[][] {{1,0},{1,1}}));
//			test= new FishersExactTest(new ContingencyTable2x2(new int[][] {{0,1},{1,1}}));
//			
//			if(t.getMeth()>0){
//			FishersExactTest fet_jsc= new FishersExactTest(new ContingencyTable2x2(new int[][]{{t.getMeth(),t.getUnmeth()},{u.getMeth()+d.getMeth(),u.getUnmeth()+d.getUnmeth()}}),H1.GREATER_THAN );
//			
//			
//			System.out.println(sep + fet_jsc.getSP());
//			}else{
//				System.out.println(sep + 1);
//			}
			
//			System.out.println(t.getPos() + sep + t.getMeth() + sep + t.getUnmeth() + sep +
//					(u.getMeth()+d.getMeth()) + sep + (u.getUnmeth() + d.getUnmeth()));
		}
		
	}
}
