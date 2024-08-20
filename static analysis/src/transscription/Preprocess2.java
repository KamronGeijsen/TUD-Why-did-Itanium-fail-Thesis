package transscription;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Stream;

public class Preprocess2 {
	
	
	static PrintWriter out;
	
	static void dis(Path path) {
		try {
			byte[] bs = Files.readAllBytes(path);
			for(byte b : bs) {
				if (b == 0) {
					out.println(path);
					System.out.println(path);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		out = new PrintWriter(new File("invalid_files.txt"));
		try (Stream<Path> stream = Files.walk(Paths.get("C:\\Users\\kgeijsen\\Desktop\\Virtual Machines\\Ubuntu 22.04\\shared\\debian-archive"))) {
			stream.parallel().filter(Files::isRegularFile).forEach(Preprocess2::dis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		out.flush();
		out.close();
	}
}
