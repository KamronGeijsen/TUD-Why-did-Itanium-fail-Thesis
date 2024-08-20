package transscription;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DirectParsingFix {
	
	
	static PrintWriter out;
	
	
	static long timeee = 0;
	
	
	static HashMap<String, Long> globalHashes = new HashMap<String, Long>();
	static HashMap<String, Long> globalHashes2 = new HashMap<String, Long>();
	static void dis(Path path) {
		HashMap<String, Long> localHashes = new HashMap<String, Long>();
		try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()), 0x10000)) {

			br.readLine();
			String l = br.readLine();
			if(!l.endsWith("file format elf64-ia64-little") && !l.endsWith("file format pei-ia64")) {
				return;
			}
			String section = null;
			long counter = 0;
			while(br.ready()) {
				String line = br.readLine();
				int end = line.length();
				int addrEnd = line.indexOf(':');
				if(addrEnd != -1 && addrEnd != end-1)
					counter ++;
//				if(addrEnd != -1 && addrEnd + 27 < end && line.charAt(addrEnd+21) == '[' && line.charAt(addrEnd-1) != '0') {
//					out.println(line.substring(0, addrEnd) + "\t" + path);
//				}
				if(line.startsWith("Disa")) {
					if(section != null) {
						if(localHashes.containsKey(section))
							System.out.println(section +"\t" + path);
						localHashes.put(section, counter);
					}
					section = line.substring(23);
					if(line.contains(".text._ZStplIcSt11char_traitsIcESaIcEESbIT_T0_T1_EPKS3_RKS6_:")) {
						out.println(path);
					}
				}
			}
			
		} catch (IOException e) {
			System.out.println("Huuuh\t" + path);
		} catch (NullPointerException e) {
			
			try {
				System.out.println(Files.readAllBytes(path).length+"\t"+path);
//				System.out.println(new String(Files.readAllBytes(path)));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		synchronized (globalHashes2) {
//			for(String s : localHashes.keySet())
//				globalHashes.put(s, globalHashes.getOrDefault(s, 0l) + 1l);
			
			for (Map.Entry<String, Long> entry : localHashes.entrySet())
				globalHashes2.merge(entry.getKey(), entry.getValue(), Long::sum);
			
		}
		
//		try {
//			byte[] bs = Files.readAllBytes(path);
////			out.println(bs.length);
//			for(byte b : bs) {
//				if(b == 0) {
//					out.println(path);
//					
//					return;
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	
	public static void main(String[] args) throws IOException {
		out = new PrintWriter(new File("stuff.txt"));
		timeee = System.currentTimeMillis() + 10000;
		
		long startTime= System.currentTimeMillis();
		Files.walk(Paths.get("C:\\Users\\kgeijsen\\Desktop\\Virtual Machines\\Ubuntu 22.04\\shared\\debian-archive\\debian"))
			.parallel()
			.filter(Files::isRegularFile)
			.forEach(DirectParsingFix::dis);
		System.out.println("Took " + (System.currentTimeMillis()-startTime)/1000 + "s");
//		for(Map.Entry<String, Long> entry : globalHashes2.entrySet()) {
//			out.println(entry.getValue() +"\t"+ entry.getKey().replace(' ', '\t'));
//		}
		
		out.flush();
		out.close();
		
		
	}
}
