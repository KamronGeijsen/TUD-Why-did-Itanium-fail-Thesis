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

public class DirectParsingTests {
	
	
	static PrintWriter out;
	
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
	
	static Long total_faulty = 0l;
	static Long total = 0l;
	
	static void dis(Path path) {
		HashMap<String, Long> localHashes = new HashMap<String, Long>();
		long faultyLines = 0;
		long goodLines = 0;
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
							faultyLines+=3;
							continue;
						}
						
						if(line.startsWith("data8", addrEnd+33) || 
								line2.startsWith("data8", addrEnd+33) || 
								line3.startsWith("data8", addrEnd+33)) {
//							System.out.println(line);
//							System.out.println(line2);
//							System.out.println(line3);
							faultyLines+=3;
							continue;
						}
						
						goodLines += 3;
						
					} else {
						
						System.out.println(line);
					}
				} else {
					if(!line.startsWith("Disassembly of section ") && line.length() != 0 && addrEnd != len-1 && !line.startsWith("     ", 1+16*3+addrEnd) && !line.contentEquals("	..."))
						System.out.println(line);
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
		
		synchronized (globalHashes) {
			total_faulty += faultyLines;
			total += goodLines;
			if(faultyLines > 1000)
				System.out.println(total_faulty+"\t"+total);
		}
	}
	
	
	public static void main(String[] args) throws IOException {
//		out = new PrintWriter(new File("stuff.txt"));
		timeee = System.currentTimeMillis() + 10000;
		
		long startTime= System.currentTimeMillis();
		Files.walk(Paths.get("C:\\Users\\kgeijsen\\Desktop\\Virtual Machines\\Ubuntu 22.04\\shared\\debian-archive\\debian"))
			.parallel()
			.filter(Files::isRegularFile)
			.forEach(DirectParsingTests::dis);
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
