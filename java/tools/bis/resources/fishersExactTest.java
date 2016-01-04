package tools.bis.resources;


//adopted from http://www.users.zetnet.co.uk/hopwood/tools/StatTests.java

public class fishersExactTest {

	public static final int PHI_COEFFICIENT = 1;
	public static final int FISHER_1TAILED = 2;
	public static final int FISHER_2TAILED = 4;
	
//	private static final int WIDTH= 7;
//	private static final int DECIMALS= 3;
//	
//    /** Number of categories. */
//    private int n;
//
//    /** Number of records. */
//    private int m;
//
//    /** A label for each category. */
//    private String[] label;
//
//    /** The raw data (a boolean matrix of n rows by m columns). */
//    private boolean[][] raw;
//
//    /**
//     * count1[i][j] is the number of records in category i but not in category j.
//     */
//    private int[][] count1;
//
//    /**
//     * count2[i][j] is the number of records in both category i and category j.
//     * count2[i][i] is the sample size for category i.
//     */
//    private int[][] count2;
//
//    /** The calculated results (a real lower triangular matrix of n rows x n columns). */
//    private double[][] result;
    
    private double[] logFactorial;
    
    public fishersExactTest(){
    	logFactorial= new double[]{0.0d};
    	extendLogFactorial(100000);
    }
    
    private void extendLogFactorial(int m){
    	double[] newVersion= new double[m+1];
    	for(int i=0;i<logFactorial.length;++i){
    		newVersion[i]= logFactorial[i];
    	}
    	for(int i=logFactorial.length;i<newVersion.length;++i){
    		newVersion[i]= newVersion[i-1] + Math.log(i);
    	}
    	logFactorial= newVersion;
    }
    
    public double fisherOneTailGreater(bisQueueItem target,bisQueueItem downstream, bisQueueItem upstream){
    	int a=target.getMeth(),
    			b=target.getUnmeth(),
    			c=downstream.getMeth()+upstream.getMeth(),
    			d=downstream.getUnmeth()+upstream.getUnmeth();
    	if(a*(c+d)>c*(a+b)){
    		return fisherOneTail(a, b, c, d);
    	}else{
//    		return 1.0;
    		return -fisherOneTail(a, b, c, d);
    	}
    }
    
    
    public double fisherOneTail(int a, int b, int c, int d){
    	if(a * d > b * c){
    		a= a+b;
    		b= a-b;
    		a= a-b;
    		c= c+d;
    		d= c-d;
    		c= c-d;
    	}
    	if(a>d){
    		a= a+d;
    		d= a-d;
    		a= a-d;
    	}
    	if(b>c){
    		b= b+c;
    		c= b-c;
    		b= b-c;
    	}
    	int m= a+b+c+d;
    	
    	if(m>=logFactorial.length){
    		extendLogFactorial((((logFactorial.length-m)/10000)+1)*10000);
    	}
    	
    	int a_org= a;
    	double p_sum= 0.0d;
    	
    	double p= fisherSub(a,b,c,d);
    	p=0;
//    	double p_1= p;
    	
    	while (a>=0) {
    		p_sum+= p;
    		if(a==0){
    			break;
    		}
    		--a; ++b; ++c; --d;
    		p= fisherSub(a_org, b, c, d);
    	}
    	
    	return p_sum;
    }
    
    private double fisherSub(int a, int b, int c, int d){
    	return Math.exp(logFactorial[a+b] + 
    			logFactorial[c+d] +
    			logFactorial[a+c] +
    			logFactorial[b+d] -
    			logFactorial[a+b+c+d] -
    			logFactorial[a] -
    			logFactorial[b] -
    			logFactorial[c] -
    			logFactorial[d]);
    }
    
}
