package ca.utoronto.utm.mcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Utils {
    public static String convert(InputStream inputStream) throws IOException {
 
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
    public static String[] parseRecord(String record) {
    	record = record.replace("[", "");
    	record = record.replace("]", "");
    	String a[] = record.split(",");
    	return a;
	}
    public static String removequotations(String record) {
    	record = record.replace("\"", "");
    
    	return record;
	}
}
