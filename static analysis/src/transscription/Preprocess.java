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

public class Preprocess {
	
	
	static PrintWriter out;
	
	static ArrayList<File> remove = new ArrayList<File>();
	static void dis(Path path) throws NullPointerException{
//		if(path.getFileName().toString().contentEquals("libcrypto.so.3.txt"))
//			return;
		Scanner sc = null;
		try {
			sc = new Scanner(path.toFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String l;
		try {
		sc.nextLine();
		if(!sc.hasNext())
			remove.add(path.toFile());
		l = sc.nextLine();
		} catch (NoSuchElementException e) {
				e.printStackTrace();
				System.out.println(path);
				throw e;
			}
		if(!l.endsWith("file format elf64-ia64-little") && !l.endsWith("file format pei-ia64")) {
//			out.println(path + "\t" + l);
			System.out.println(path.toFile().getName() + "\t" + l);
			remove.add(path.toFile());
		}
		sc.close();
	}
	
	
	public static void main(String[] args) throws IOException {
		out = new PrintWriter(new File("stuff.txt"));
		try (Stream<Path> stream = Files.walk(Paths.get("C:\\Users\\kgeijsen\\Desktop\\Virtual Machines\\Ubuntu 22.04\\shared\\debian-archive"))) {
			stream.filter(Files::isRegularFile).forEach(Preprocess::dis);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		}
		int i = System.in.read();
		
		for(File f : remove) {
			System.out.println(f);
			f.delete();
		}
		
		out.flush();
		out.close();
	}
}
