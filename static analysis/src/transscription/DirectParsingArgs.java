package transscription;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DirectParsingArgs {
	

	static PrintWriter out;
	static long globalTimer = System.currentTimeMillis();
	static HashMap<String, Long> globalHashes = new HashMap<String, Long>();
	
	
	static String arg(String line, final int start, final int end) {
		sw:switch(line.charAt(start)) {
		case 'r': {
			if((start + 2 == end && line.charAt(start+1) >= '0' && line.charAt(start+1) <= '9') ||
					(start + 3 == end && line.charAt(start+1) >= '1' && line.charAt(start+1) <= '9'
					 && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '9') ||
					(start + 4 == end && line.charAt(start+1) == '1'
					 && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '2'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '9')) {
				return line.substring(start, end);
			}
		}break;
		case 'b': {
			if(start + 2 == end && line.charAt(start+1) >= '0' && line.charAt(start+1) <= '7') {
				return line.substring(start, end);
			}
		}break;
		case 'p': {
			if((start + 2 == end && line.charAt(start+1) >= '0' && line.charAt(start+1) <= '9') ||
					(start + 3 == end && line.charAt(start+1) >= '1' && line.charAt(start+1) <= '6'
					 && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '9')) {
				return line.substring(start, end);
			}
		}break;
		case 'f': {
			if((start + 2 == end && line.charAt(start+1) >= '0' && line.charAt(start+1) <= '9') ||
					(start + 3 == end && line.charAt(start+1) >= '1' && line.charAt(start+1) <= '9'
					 && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '9') ||
					(start + 4 == end && line.charAt(start+1) == '1'
					 && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '2'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '9')) {
				return line.substring(start, end);
			}
		}break;
		case '-': {
			return "imm";
		}
		case '0': {
			if((start + 1 == end) || (start + 2 < end && line.charAt(start+1) == 'x')) {
				return "imm";
			}
		}
		case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':{
			for(int i = start+1; i < end-1; i++) {
				if(line.charAt(i) < '0' || line.charAt(i) > '9')
					break sw;
			}
			return "imm";
		}
		case '[': {
			return line.substring(start, end);
		}
		case 'a': {
			if((start + 3 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '9') ||
					(start + 4 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) >= '1' && line.charAt(start+2) <= '9'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '9') ||
					(start + 5 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) == '1'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '2'
					 && line.charAt(start+4) >= '0' && line.charAt(start+4) <= '9')) {
				return line.substring(start, end);
			}
		}break;
		case 'c': {
			if((start + 3 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '9') ||
					(start + 4 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) >= '1' && line.charAt(start+2) <= '9'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '9') ||
					(start + 5 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) == '1'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '2'
					 && line.charAt(start+4) >= '0' && line.charAt(start+4) <= '9')) {
				return line.substring(start, end);
			}
		}break;
		}
		
		boolean nothing = false;
		for(int i = start; i < end-1; i++) {
			char c = line.charAt(i);
			if((c < '0' || c > '9') && (c < 'a' || c > 'f')) {
				nothing = true;
				break;
			}
		}
		if(!nothing) {
			return "target";
		}
		
		for(int i = start; i < end-1; i++) {
			char c = line.charAt(i);
			if((c < '0' || c > '9') && (c < 'a' || c > 'f')) {
				nothing = true;
				break;
			}
		}
		return line.substring(start, end);
		
	}
	
	static void processSegment(HashMap<String, Long> localHashMap) {
		synchronized (globalHashes) {
			for (Map.Entry<String, Long> entry : localHashMap.entrySet()) {
				globalHashes.merge(entry.getKey(), entry.getValue(), Long::sum);
			}
			
			
			if(globalTimer < System.currentTimeMillis()) {
				globalTimer = System.currentTimeMillis() + 100000;
				
				try {
					out = new PrintWriter(new File("stuff.txt"));
					for(Map.Entry<String, Long> entry : globalHashes.entrySet()) {
						out.println(entry.getValue() +"\t"+ entry.getKey().replace(' ', '\t'));
					}
					out.flush();
					out.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			if(globalHashes.size() > 0x100_0000) {
				ArrayList<Entry<String, Long>> bla = new ArrayList<>();
				bla.addAll(globalHashes.entrySet());
				bla.sort(new Comparator<Entry<String, Long>>() {

					@Override
					public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
						return Long.compare(o2.getValue(),o1.getValue());
					}
				});
				int size = globalHashes.size();
				for(int i = 0x100000; i < size; i++)
					globalHashes.remove(bla.get(i).getKey());
				
			}
		}
	}
	static ArrayList<Long> sizes = new ArrayList<Long>();
	static void dis(Path path) {
		HashMap<String, Long> localHashes = new HashMap<String, Long>();
		try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()), 0x10000)) {
			
			br.readLine();
			String l = br.readLine();
			if(!l.endsWith("file format elf64-ia64-little") && !l.endsWith("file format pei-ia64")) {
				return;
			}
			
			while(br.ready()) {
				String line = br.readLine();
				int len = line.length();
				int addrEnd = line.indexOf(':');
				if(addrEnd != -1 && addrEnd != len-1 && addrEnd != len-14 && line.charAt(addrEnd+32) == ' ') {
					
					int i;
					for(i = addrEnd+33; i < len; i++) {
						if(line.charAt(i) == ';') {
							i = len;
							break;
						}
						if(line.charAt(i) == ' ')
							break;
					}
					boolean endspace = false;
					i++;
					int start;
					for(start = i; i < len; i++) {
						if(line.charAt(i) == ',' || line.charAt(i) == '=') {
							
							String newString = arg(line, start, i);
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
							
							start = i+1;
						}
						if(line.charAt(i) == ' ' || line.charAt(i) == ';') {
							String newString = arg(line, start, i);
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
							
							start = i+1;
							endspace = line.charAt(i) == ' ';
							break;
						}
					}
					if(start < i) {
						if(endspace)
						{
							String newString = "target";
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);}
						else {
							String newString = arg(line, start, i);
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
						}
					}

				}
			}
			
			
		} catch (IOException e) {
		} catch (NullPointerException e) {
		}
		processSegment(localHashes);
	}
	
	
	public static void main(String[] args) throws IOException {
		globalTimer = System.currentTimeMillis() + 10000;
		long startTime= System.currentTimeMillis();
		Files.walk(Paths.get("path/to/files"))
			.parallel()
			.filter(Files::isRegularFile)
			.forEach(DirectParsingArgs::dis);
		
		
		System.out.println("Took " + (System.currentTimeMillis()-startTime)/1000 + "s");

		out = new PrintWriter(new File("outputFile.txt"));
		ArrayList<Entry<String, Long>> bla = new ArrayList<>();
		bla.addAll(globalHashes.entrySet());
		bla.sort(new Comparator<Entry<String, Long>>() {
			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
				return -Long.compare(o1.getValue(),o2.getValue());
			}
		});
		for(Map.Entry<String, Long> entry : bla) {
			out.println(entry.getValue() +"\t"+ entry.getKey().replace(' ', '\t'));
		}
		out.flush();
		out.close();
		
	}
}
