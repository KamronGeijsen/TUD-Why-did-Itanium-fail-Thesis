package transscription;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;

public class DirectParsingGcc {
	
	
	static PrintWriter out;
	
	static HashMap<String, Long> hm  = new HashMap<>();
	
	static void dis(Path path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
			while(br.ready()) {
				String line = br.readLine();
				if(line.startsWith("String dump of section ") || line.length() < 12)
					continue;
				line = line.substring(12);
//				System.out.println(line);
				hm.put(line, hm.getOrDefault(line, 0L) + 1);
			}
			
			
		} catch (IOException e) {
		} catch (NullPointerException e) {
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		out = new PrintWriter(new File("bwaaaah.txt"));
		long startTime= System.currentTimeMillis();
		Files.walk(Paths.get("C:\\Users\\kgeijsen\\Desktop\\Virtual Machines\\Ubuntu 22.04\\shared\\all_comps"))
//			.parallel()
			.filter(Files::isRegularFile)
			.forEach(DirectParsingGcc::dis);
		
		System.out.println("Took " + (System.currentTimeMillis()-startTime)/1000 + "s");
//		System.out.println(hm);
		for(Entry<String, Long> e: hm.entrySet()) {
			out.println(e.getKey() + "\t" + e.getValue());
		}
		out.flush();
		
	}
}
