package my.anlights.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ParserHelper {

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static String removeBrackets(String input){
		if(input.indexOf("[")==0 && input.lastIndexOf("]")==(input.length()-1)){
			return input.substring(1, input.length()-1);
		} else return input;
	}
	
	public static String readInputStream(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null)
			{
			    sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
}
