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
import java.util.HashMap;
import java.util.Map;

public class DirectParsingPred {
	
	
	static PrintWriter out;
	
	static void arg(StringBuilder sb, String line, final int start, final int end) {
//		System.out.println("< " + line.substring(start, end));
		sw:switch(line.charAt(start)) {
		case 'r': {
			if((start + 2 == end && line.charAt(start+1) >= '0' && line.charAt(start+1) <= '9') ||
					(start + 3 == end && line.charAt(start+1) >= '1' && line.charAt(start+1) <= '9'
					 && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '9') ||
					(start + 4 == end && line.charAt(start+1) == '1'
					 && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '2'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '9')) {
				sb.append("reg");
				return;
			}
		}break;
		case 'b': {
			if(start + 2 == end && line.charAt(start+1) >= '0' && line.charAt(start+1) <= '7') {
				sb.append("breg");
				return;
			}
		}break;
		case 'p': {
			if((start + 2 == end && line.charAt(start+1) >= '0' && line.charAt(start+1) <= '9') ||
					(start + 3 == end && line.charAt(start+1) >= '1' && line.charAt(start+1) <= '6'
					 && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '9')) {
				sb.append("preg");
				return;
			}
		}break;
		case 'f': {
			if((start + 2 == end && line.charAt(start+1) >= '0' && line.charAt(start+1) <= '9') ||
					(start + 3 == end && line.charAt(start+1) >= '1' && line.charAt(start+1) <= '9'
					 && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '9') ||
					(start + 4 == end && line.charAt(start+1) == '1'
					 && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '2'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '9')) {
				sb.append("freg");
				return;
			}
		}break;
		case '-': {
			sb.append("imm");
			return;
		}
		case '0': {
			if((start + 1 == end) || (start + 2 < end && line.charAt(start+1) == 'x')) {
				sb.append("imm");
				return;
			}
		}
		case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':{
			for(int i = start+1; i < end-1; i++) {
				if(line.charAt(i) < '0' || line.charAt(i) > '9')
					break sw;
			}
			sb.append("imm");
			return;
		}
		case '[': {
			sb.append("[reg]");
			return;
		}
		case 'a': {
			if((start + 3 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '9') ||
					(start + 4 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) >= '1' && line.charAt(start+2) <= '9'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '9') ||
					(start + 5 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) == '1'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '2'
					 && line.charAt(start+4) >= '0' && line.charAt(start+4) <= '9')) {
				sb.append("areg");
				return;
			}
		}break;
		case 'c': {
			if((start + 3 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) >= '0' && line.charAt(start+2) <= '9') ||
					(start + 4 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) >= '1' && line.charAt(start+2) <= '9'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '9') ||
					(start + 5 == end && line.charAt(start+1) == 'r' && line.charAt(start+2) == '1'
					 && line.charAt(start+3) >= '0' && line.charAt(start+3) <= '2'
					 && line.charAt(start+4) >= '0' && line.charAt(start+4) <= '9')) {
				sb.append("creg");
				return;
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
			sb.append("target");
			return;
		}
		
		for(int i = start; i < end-1; i++) {
			char c = line.charAt(i);
			if((c < '0' || c > '9') && (c < 'a' || c > 'f')) {
				nothing = true;
				break;
			}
		}
		sb.append(line.substring(start, end).replaceFirst("\\[.*\\]", "[reg]"));
		
	}
	
	static long timeee = 0;
	
	static HashMap<String, Long> globalHashes = new HashMap<String, Long>();
	static void processSegment(HashMap<String, Long> localHashMap) {
		synchronized (globalHashes) {
			int startSize = globalHashes.size();
			for (Map.Entry<String, Long> entry : localHashMap.entrySet()) {
				globalHashes.merge(entry.getKey(), entry.getValue(), Long::sum);
			}
//			if(globalHashes.size() < startSize) {
//				System.out.println("HUUHH");
//			}
			System.out.println(globalHashes.get("nop.m imm") + "\t" + globalHashes.size());
			
			if(timeee < System.currentTimeMillis()) {
				timeee = System.currentTimeMillis() + 10000;
				
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
				if(len > 3000) {
					System.out.println(len + "\t" + line);
					System.out.println(path);
				}
				int instrStart = line.indexOf('\t', addrEnd+2);
//				System.out.println(line);
				if(addrEnd != -1 && addrEnd != len-1 && addrEnd != len-14 && line.charAt(addrEnd+32) == ' ') {
//					
					StringBuilder sb = new StringBuilder(256);
					
					int i;
					for(i = addrEnd+33; i < len; i++) {
						if(line.charAt(i) == ';') {
							i = len;
							break;
						}
						sb.append(line.charAt(i));
						if(line.charAt(i) == ' ')
							break;
					}
					boolean endspace = false;
					i++;
					int start;
					for(start = i; i < len; i++) {
						if(line.charAt(i) == ',' || line.charAt(i) == '=') {
							arg(sb, line, start, i);
							sb.append(line.charAt(i));
							start = i+1;
						}
						if(line.charAt(i) == ' ' || line.charAt(i) == ';') {
							arg(sb, line, start, i);
							endspace = line.charAt(i) == ' ';
							start = i+1;
							break;
						}
					}
					if(start < i) {
						if(endspace)
							sb.append("target");
						else
							arg(sb, line, start, i);
					}
					
//					System.out.println(sb);
					sb.append("\t");
					sb.append(line.charAt(addrEnd+30)==' ');
					String newString = sb.toString();
					localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
					

				}
			}
			
			
		} catch (IOException e) {
//			e.printStackTrace();
//			long l = path.toFile().length();
//			out.println(l);
//			System.out.println(path);
		} catch (NullPointerException e) {
//			System.out.println(path.toFile() + "\t" + l);
//			long l = path.toFile().length();
//			out.println(l);
//			System.out.println(path);
		}
		processSegment(localHashes);
//		System.out.println(localHashes.size());
//		System.out.println(localHashes.get("nop.m imm"));
		
//		long l = path.toFile().length();
////		System.out.println(l);
//		sizes.add(l);
//		out.println(l);-
	}
	
	
	public static void main(String[] args) throws IOException {
//		out = new PrintWriter(new File("stuff.txt"));
		timeee = System.currentTimeMillis() + 10000;
		
		long startTime= System.currentTimeMillis();
		Files.walk(Paths.get("C:\\Users\\kgeijsen\\Desktop\\Virtual Machines\\Ubuntu 22.04\\shared\\debian-archive\\debian"))
			.parallel()
			.filter(Files::isRegularFile)
			.forEach(DirectParsingPred::dis);
		System.out.println("Took " + (System.currentTimeMillis()-startTime)/1000 + "s");
//		sizes.sort(null);
//		for(long l : sizes)
//			System.out.println(l);
		try {
			out = new PrintWriter(new File("stuff.txt"));
			System.out.println(globalHashes.size());
			for(Map.Entry<String, Long> entry : globalHashes.entrySet()) {
				out.println(entry.getValue() +"\t"+ entry.getKey().replace(' ', '\t'));
			}
			System.out.println(globalHashes.get("nop.m imm") + "\t" + globalHashes.size());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
//		out.flush();
//		out.close();
		
		
	}
}
