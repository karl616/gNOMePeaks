package tools.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class general {

	
	public static BufferedReader getBufferedReader(String fileName)throws IOException{
		if(fileName.equals("-")){
			return new BufferedReader(new InputStreamReader(System.in));
		}
		if(fileName.endsWith("gz")){
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
		}
		return new BufferedReader(new FileReader(fileName));
	}
}
