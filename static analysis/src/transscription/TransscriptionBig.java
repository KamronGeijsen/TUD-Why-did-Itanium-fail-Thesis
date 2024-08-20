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

public class TransscriptionBig {
	
	static String[] formats = {
			"MII",
			"MII;",
			"MI;I",
			"MI;I;",
			"MLX",
			"MLX;",
			null,
			null,
			"MMI",
			"MMI;",
			"M;MI",
			"M;MI;",
			"MFI",
			"MFI;",
			"MMF",
			"MMF;",
			"MIB",
			"MIB;",
			"MBB",
			"MBB;",
			null,
			null,
			"BBB",
			"BBB;",
			"MMB",
			"MMB;",
			null,
			null,
			"MFB",
			"MFB;",
			null,
			null,
		};
	static PrintWriter out;
	
	

	static long timeee = 0;
	static HashMap<String, Long> globalHashes = new HashMap<String, Long>();
	static void processSegment(HashMap<String, Long> localHashMap) {
		synchronized (globalHashes) {
			int startSize = globalHashes.size();
			for (Map.Entry<String, Long> entry : localHashMap.entrySet()) {
				globalHashes.merge(entry.getKey(), entry.getValue(), Long::sum);
			}
//			System.out.println(globalHashes.size());
//			System.out.println(globalHashes);
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
			StringBuilder sb = new StringBuilder(64);
			boolean textSection = false;
			while(br.ready()) {
				String line = br.readLine();
				int len = line.length();
				int addrEnd = line.indexOf(':');
				
				int instrStart = line.indexOf('\t', addrEnd+2);
				
				if(addrEnd != -1 && addrEnd != len-1 && addrEnd != len-14 && line.charAt(addrEnd+32) == ' ') {
					if(line.charAt(addrEnd+21) == '[' && line.charAt(addrEnd-1) == '0'
							&& line.charAt(addrEnd+22) != '-'
							) {
						String line2 = br.readLine();
						String line3 = br.readLine();
						
						if(line2.length() <= addrEnd+33 || (line.charAt(addrEnd+24) == 'X' ? line3.length() != addrEnd+14 : line3.length() <= addrEnd+33)
								|| line2.charAt(addrEnd) != ':' || line3.charAt(addrEnd) != ':') {
							System.out.println(line2);
							System.out.println(line3);
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
							sb.append(textSection);
							String newString = sb.toString();
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
							sb.setLength(0);
						}
						
						if(line2.startsWith("nop.", addrEnd+33)) {}//sb.append('-');
						else if(line2.startsWith("br.", addrEnd+33)) sb.append(line2.charAt(addrEnd+30) == ' '?'b':'B');
						else sb.append(line.charAt(addrEnd+23));
						if(line2.charAt(line2.length()-1) == ';' && line.charAt(addrEnd+23)!='L') {
							sb.append(';');
							sb.append('\t');
							sb.append(textSection);
							String newString = sb.toString();
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
							sb.setLength(0);
						}
						
						if(line3.startsWith("nop.", addrEnd+33)) {}//sb.append('-');
						else if(line3.startsWith("br.", addrEnd+33)) sb.append(line3.charAt(addrEnd+30) == ' '?'b':'B');
						else sb.append(line.charAt(addrEnd+24));
						if(line3.charAt(line3.length()-1) == ';' || (line2.charAt(line2.length()-1) == ';' && line.charAt(addrEnd+23)=='L')) {
							
							sb.append(';');
							sb.append('\t');
							sb.append(textSection);
							String newString = sb.toString();
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
							sb.setLength(0);
						}
						
						if(sb.indexOf("b") != -1) {
							sb.append('\t');
							sb.append(textSection);
							String newString = sb.toString();
							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
							sb.setLength(0);
						}
						
						
						
					}
				} else if(addrEnd == len-1 && len > 16) {
					sb.append('\t');
					sb.append(textSection);
					String newString = sb.toString();
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
		}
		processSegment(localHashes);
//		System.out.println(localHashes.size());
//		System.out.println(localHashes.get("nop.m imm"));
		
//		long l = path.toFile().length();
////		System.out.println(l);
//		sizes.add(l);
//		out.println(l);
	}
	
	
	public static void main(String[] args) throws IOException {
		
		long startTime= System.currentTimeMillis();
		Files.walk(Paths.get("C:\\Users\\kgeijsen\\Desktop\\Virtual Machines\\Ubuntu 22.04\\shared\\debian-archive\\debian"))
			.parallel()
			.filter(Files::isRegularFile)
			.forEach(TransscriptionBig::dis);
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
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
//		out.flush();
//		out.close();
		
		
	}
}
