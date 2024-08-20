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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DirectParsingLoops {
	
	
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
//			System.out.println(globalHashes.get("nop.m imm") + "\t" + globalHashes.size());
			
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
	
	static void fun(String line, int addr, String lineN, HashMap<String, Long> localHashes, ArrayList<Long> addrRanges) {
		if(lineN.startsWith("br.", addr+33) && (lineN.charAt(addr+36) == 'c' ? lineN.charAt(addr+37) != 'a' && lineN.charAt(addr+37) != 'o' : lineN.charAt(addr+36) == 'w')) {
			int op = lineN.indexOf(' ', addr+33);
			int comm = lineN.indexOf(' ', op+1);
			comm = comm == -1? lineN.length() : comm;
			if(lineN.charAt(comm-1) == ';') {
				comm -= 2;
				
			}
			if(lineN.charAt(op+2) == 'x')
				op += 2;
//			System.out.println(Arrays.toString(line.substring(0, addr).trim().toCharArray()));
			long iaddr = Long.parseUnsignedLong(line.substring(0, addr).trim(), 16);
			long itgt = Long.parseUnsignedLong(lineN.substring(op+1, comm), 16);
//			if(itgt > iaddr)
//				System.out.println(iaddr + "\t" + path);
//			out.println()
//			StringBuilder sb = new StringBuilder(256);
//			sb.append(itgt-iaddr);
//			String newString = sb.toString();
//			String newString = Long.toString(itgt-iaddr);
//			localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
			long start = Math.min(iaddr, itgt);
			long end = Math.max(iaddr, itgt);
			boolean inside = false;
			int i = 0;
			for(; i < addrRanges.size(); i++) {
				if(addrRanges.get(i) >= start)
					break;
				inside = !inside;
			}
			if(!inside) {
				addrRanges.add(i, start);
				i++;
			}
			for(; i < addrRanges.size();) {
				if(addrRanges.get(i) >= end)
					break;
				addrRanges.remove(i);
				inside = !inside;
			}
			if(!inside) {
				addrRanges.add(i, end);
				i++;
			}
		}
		
	}
	static boolean isInsideRange(long addr, ArrayList<Long> addrRange) {
		boolean inside = false;
		for(Long a : addrRange) {
			if(a == addr) {inside = true; break;}
			if(a >= addr) break;
			inside = !inside;
		}
		return inside;
	}
	static ArrayList<Long> sizes = new ArrayList<Long>();
	static void dis(Path path) {
		HashMap<String, Long> localHashes = new HashMap<String, Long>();
		ArrayList<Long> addrRanges = new ArrayList<>(256);
		try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()), 0x1000)) {
			
			br.readLine();
			String l = br.readLine();
			if(!l.endsWith("file format elf64-ia64-little") && !l.endsWith("file format pei-ia64")) {
				return;
			}
			
			while(br.ready()) {
				String line = br.readLine();
				int len = line.length();
				int addrEnd = line.indexOf(':');
//				if(len > 3000) {
//					System.out.println(len + "\t" + line);
//					System.out.println(path);
//				}
				int instrStart = line.indexOf('\t', addrEnd+2);
//				System.out.println(line);
				if(addrEnd != -1 && addrEnd != len-1 && addrEnd != len-14 && line.charAt(addrEnd+32) == ' ') {
					if(line.charAt(addrEnd+21) == '[' && line.charAt(addrEnd-1) == '0'
							&& line.charAt(addrEnd+22) != '-'
							) {
						String line2 = br.readLine();
						String line3 = br.readLine();
						
						if(line2.length() <= addrEnd+33 || (line.charAt(addrEnd+24) == 'X' ? line3.length() != addrEnd+14 : line3.length() <= addrEnd+33)
								|| line2.charAt(addrEnd) != ':' || line3.charAt(addrEnd) != ':') {
//							System.out.println(line2);
//							System.out.println(line3);
							continue;
						}
						
						if(line.startsWith("data8", addrEnd+33) || line2.startsWith("data8", addrEnd+33) || line3.startsWith("data8", addrEnd+33))
							continue;
					
					
//						if(line.startsWith("br.c", addrEnd+33) && line.charAt(addrEnd+37) != 'a') {
//							System.out.println(line.substring(0, addrEnd) + "\t" +line.substring(addrEnd+33));
							fun(line, addrEnd, line, localHashes, addrRanges);
//						}
//						if(line2.startsWith("br.cond", addrEnd+33)) {
							fun(line, addrEnd, line2, localHashes, addrRanges);
//							System.out.println(line.substring(0, addrEnd) + "\t" +line2.substring(addrEnd+33));
//						}
//						if(line3.startsWith("br.cond", addrEnd+33)) {
							fun(line, addrEnd, line3, localHashes, addrRanges);
//							System.out.println(line.substring(0, addrEnd) + "\t" +line3.substring(addrEnd+33));
//						}
					
//					StringBuilder sb = new StringBuilder(256);
//					
//					int i;
//					for(i = addrEnd+33; i < len; i++) {
//						if(line.charAt(i) == ';') {
//							i = len;
//							break;
//						}
//						sb.append(line.charAt(i));
//						if(line.charAt(i) == ' ')
//							break;
//					}
//					boolean endspace = false;
//					i++;
//					int start;
//					for(start = i; i < len; i++) {
//						if(line.charAt(i) == ',' || line.charAt(i) == '=') {
//							arg(sb, line, start, i);
//							sb.append(line.charAt(i));
//							start = i+1;
//						}
//						if(line.charAt(i) == ' ' || line.charAt(i) == ';') {
//							arg(sb, line, start, i);
//							endspace = line.charAt(i) == ' ';
//							start = i+1;
//							break;
//						}
//					}
//					if(start < i) {
//						if(endspace)
//							sb.append("target");
//						else
//							arg(sb, line, start, i);
//					}
//					
////					System.out.println(sb);
//					String newString = sb.toString();
//					localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
					
					}
				}
			}
			
			
		} catch (IOException e) {
//			e.printStackTrace();
//			long l = path.toFile().length();
//			out.println(l);
//			System.out.println(path);
			e.printStackTrace();
		} catch (NullPointerException e) {
//			System.out.println(path.toFile() + "\t" + l);
//			long l = path.toFile().length();
//			out.println(l);
//			e.printStackTrace();
			System.out.println(path);
		}
//		System.out.println(addrRanges);
//		if(addrRanges.isEmpty())
//			return;
		
		try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()), 0x10000)) {
			
			br.readLine();
			String l = br.readLine();
			if(!l.endsWith("file format elf64-ia64-little") && !l.endsWith("file format pei-ia64")) {
				return;
			}
			StringBuilder sb = new StringBuilder(64);
			boolean textSection = false;
			Long startAddr=null;
			while(br.ready()) {
				String line = br.readLine();
				int len = line.length();
				int addrEnd = line.indexOf(':');
				
//				int instrStart = line.indexOf('\t', addrEnd+2);
				
				if(addrEnd != -1 && addrEnd != len-1 && addrEnd != len-14 && line.charAt(addrEnd+32) == ' ') {
					if(line.charAt(addrEnd+21) == '[' && line.charAt(addrEnd-1) == '0'
							&& line.charAt(addrEnd+22) != '-'
							) {
						if(startAddr == null) {
							startAddr = Long.parseLong(line.substring(0, addrEnd).trim(), 16);
						}
						
						String line2 = br.readLine();
						String line3 = br.readLine();
						
						if(line2.length() <= addrEnd+33 || 
								(line.charAt(addrEnd+24) == 'X' ? line3.length() != addrEnd+14 : line3.length() <= addrEnd+33)
								|| line2.charAt(addrEnd) != ':' 
								|| line3.charAt(addrEnd) != ':') {
//							System.out.println(line2);
//							System.out.println(line3);
							continue;
						}
						
						if(line.startsWith("data8", addrEnd+33) || line2.startsWith("data8", addrEnd+33) || line3.startsWith("data8", addrEnd+33))
							continue;
						
						
//						if(line2.contains(";;") != (line2.charAt(line2.length()-1) == ';'))
//							System.out.println(line2);
//						if(line3.contains(";;") != (line3.charAt(line3.length()-1) == ';'))
//							System.out.println(line3);
						
						if(line.startsWith("nop.", addrEnd+33)) {}//sb.append('-');
						else if(line.startsWith("br.", addrEnd+33)) sb.append(line.charAt(addrEnd+30) == ' '?'b':'B');
						else sb.append(line.charAt(addrEnd+22));
						if(line.charAt(line.length()-1) == ';') {
							sb.append(';');
							sb.append('\t');
							sb.append(isInsideRange(Long.parseLong(line.substring(0, addrEnd).trim(), 16), addrRanges) || 
									isInsideRange(startAddr, addrRanges));
							String newString = sb.toString();
//							if(newString.length() > 200) System.out.println(path + "\t" + line.substring(0, addrEnd));
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
							sb.setLength(0);
						}
						
						if(line2.startsWith("nop.", addrEnd+33)) {}//sb.append('-');
						else if(line2.startsWith("br.", addrEnd+33)) sb.append(line2.charAt(addrEnd+30) == ' '?'b':'B');
						else sb.append(line.charAt(addrEnd+23));
						if(line2.charAt(line2.length()-1) == ';' && line.charAt(addrEnd+23)!='L') {
							sb.append(';');
							sb.append('\t');
							sb.append(isInsideRange(Long.parseLong(line.substring(0, addrEnd).trim(), 16), addrRanges) || 
									isInsideRange(startAddr, addrRanges));
							String newString = sb.toString();
							if(newString.length() > 200) System.out.println(path + "\t" + line.substring(0, addrEnd));
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
							sb.setLength(0);
						}
						
						if(line3.startsWith("nop.", addrEnd+33)) {}//sb.append('-');
						else if(line3.startsWith("br.", addrEnd+33)) sb.append(line3.charAt(addrEnd+30) == ' '?'b':'B');
						else sb.append(line.charAt(addrEnd+24));
						if(line3.charAt(line3.length()-1) == ';' || (line2.charAt(line2.length()-1) == ';' && line.charAt(addrEnd+23)=='L')) {
							
							sb.append(';');
							sb.append('\t');
							sb.append(isInsideRange(Long.parseLong(line.substring(0, addrEnd).trim(), 16), addrRanges) || 
									isInsideRange(startAddr, addrRanges));
							String newString = sb.toString();
							if(newString.length() > 200) System.out.println(path + "\t" + line.substring(0, addrEnd));
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
							sb.setLength(0);
						}
						
						if(sb.indexOf("b") != -1) {
							sb.append('\t');
							sb.append(isInsideRange(Long.parseLong(line.substring(0, addrEnd).trim(), 16), addrRanges) || 
									isInsideRange(startAddr, addrRanges));
							
							String newString = sb.toString();
							if(newString.length() > 200) System.out.println(path + "\t" + line.substring(0, addrEnd));
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
							sb.setLength(0);
						}
						
						
						if(sb.length() == 0)
							startAddr = null;
					}
				} else if(addrEnd == len-1 && len > 16) {
					sb.append('\t');
					if(startAddr == null) startAddr = -1l;
					sb.append(isInsideRange(startAddr, addrRanges));
					startAddr = null;
					String newString = sb.toString();
//					if(newString.length() > 200) System.out.println(path + "\t" + line.substring(0, addrEnd));
					localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
					sb.setLength(0);
					
					if(line.startsWith("Disassembly of section "))
						textSection = line.endsWith(" .text:"); 
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
//			System.out.println(e);
			e.printStackTrace();
			System.out.println(path);
		}
		processSegment(localHashes);
//		processSegment(localHashes);
//		System.out.println(localHashes.size());
//		System.out.println(localHashes.get("nop.m imm"));
		
//		long l = path.toFile().length();
////		System.out.println(l);
//		sizes.add(l);
//		out.println(l);
	}
	
	
	public static void main(String[] args) throws IOException {
//		out = new PrintWriter(new File("stuff.txt"));
		timeee = System.currentTimeMillis() + 10000;
		
		long startTime= System.currentTimeMillis();
		Files.walk(Paths.get("C:\\Users\\kgeijsen\\Desktop\\Virtual Machines\\Ubuntu 22.04\\shared\\debian-archive\\debian"))
			.parallel()
			.filter(Files::isRegularFile)
			.forEach(DirectParsingLoops::dis);
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
