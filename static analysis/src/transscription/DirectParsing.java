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

public class DirectParsing {
	
	
	static PrintWriter out;
	
	static void dis(Path path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()), 0x10000)) {
			PrintWriter out; 
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
				if(addrEnd != -1 && addrEnd != len-1 && addrEnd != len-14 && line.charAt(addrEnd+32) == ' ') {
					
//					if(line.startsWith("break", addrEnd+33) || line.startsWith("data", addrEnd+33))
					
					boolean b = line.startsWith("break", addrEnd+33) || line.startsWith("data", addrEnd+33);
					String line2 = br.readLine();
					b |= line2.startsWith("break", addrEnd+33) || line2.startsWith("data", addrEnd+33);
					String line3 =  br.readLine();
					b |= line3.startsWith("break", addrEnd+33) || line3.startsWith("data", addrEnd+33);
					
					System.out.println((b?"> ":"  ") + line);
					System.out.println((b?"> ":"  ") + line2);
					System.out.println((b?"> ":"  ") + line3);
				}
			}
			
			
		} catch (IOException e) {
		} catch (NullPointerException e) {
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		long startTime= System.currentTimeMillis();
		Files.walk(Paths.get("C:\\Users\\kgeijsen\\Desktop\\Virtual Machines\\Ubuntu 22.04\\shared\\winXP_IA64"))
//			.parallel()
			.filter(Files::isRegularFile)
			.forEach(DirectParsing::dis);
		System.out.println("Took " + (System.currentTimeMillis()-startTime)/1000 + "s");
//		
		
		
	}
}
