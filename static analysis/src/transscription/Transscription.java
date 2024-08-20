package transscription;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;

public class Transscription {
	
	
	static PrintWriter out;
	
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
	
	
	static class Bundle {
		
		public Bundle(long addr, byte[] bytes, String template, String section) {
			this.addr = addr;
			this.bytes = bytes;
			
			templateStr = template;
			this.template = bytes[0]&0x1f;
			this.section = section;
		}
		long addr;
		byte[] bytes;
		String templateStr;
		int template;
		Instr[] is = new Instr[3];
		String section;
		
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(256);
			sb.append("%016x:\t".formatted(this.addr));
//			for(byte b : bytes)
//				sb.append("%02x ".formatted(b));
//			sb.append("\t");
			sb.append(this.templateStr);
			sb.append("\t");
			sb.append(formats[this.template]);
			sb.append("\t");
			sb.append(this.section);
			
			for(Instr i : is) {
				sb.append("\n");
				sb.append(i);
			}
			
			
			return sb.toString();
		}
	}
	
	static class Instr {
		
		public Instr(long bytes, String asm, int pred) {
			this.bytes = bytes;
			this.asm = asm;
			this.pred = pred;
		}
		
		long bytes;
		String asm;
		int pred;
		
		@Override
		public String toString() {
//			StringBuilder
			if(pred == 0) return "%s\t\t%s".formatted(Long.toHexString(bytes+0x100000000000l).substring(1), asm);
			return "%s\t%d\t%s".formatted(Long.toHexString(bytes+0x100000000000l).substring(1), pred, asm);
		}
	}
	
	static long convertToLong(byte[] bytes, int start) {
		long value = 0l;
		
		for (int i = start; i < Math.min(bytes.length, start+8); i++) {
			value |= (bytes[i] & 255l) << (8l*(i-start));
		}

		return value;
	}
	
	static void dis(Path path) {
//		if(path.getFileName().toString().contentEquals("libcrypto.so.3.txt"))
//			return;
		Scanner sc = null;
		try {
			sc = new Scanner(path.toFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
//		System.out.println(path);
		
//		sc.nextLine();
//		System.out.println(sc.nextLine().split("     ")[1]);
		String section = null;
		for (String l; sc.hasNext();) {
			l = sc.nextLine();
//			System.out.println(l);
			if(l.matches(" *[0-9A-Fa-f]+:\t.*")) {
				String s1 = sc.hasNext()?sc.nextLine():"	...";
				String s2 = sc.hasNext()?sc.nextLine():"	...";
				
				if(l.contains(" is out of bounds.") || s1.contains(" is out of bounds.") || s2.contains(" is out of bounds.")
						|| s1.equals("	...") || s2.equals("	...")
						|| s1.trim().isEmpty()|| s2.trim().isEmpty()) {
					continue;
				}
//				System.out.println(l);
//				System.out.println(s1);
//				System.out.println(s2);
				
				
				String[] l1 = l.split("\t");
				String[] l2 = s1.split("\t");
				String[] l3 = s2.split("\t");
				
				if(l3.length == 2) {
					continue;
				}
				
				Long addr = Long.parseLong(l1[0].substring(0, l1[0].length()-1).trim(), 16);
				if((addr & 0xf) != 0)
					continue;
				
				if(l3.length == 2) {
					String[] l3s = new String[3];
					l3s[0] = l3[0];
					l3s[1] = l3[1];
					l3s[2] = "            -";
					l3 = l3s;
				}
				
				
				
//				out.println();
//				System.out.println(l1[1]);
//				System.out.println(l2[1]);
//				System.out.println(l3[1]);
				
//				System.out.println(l1[0]);
//				System.out.println((l1[1]+l2[1]+l3[1]));
//				System.out.println(l1[2].substring(12));
//				System.out.println(l2[2].substring(12));
//				System.out.println(l3[2].substring(12));
				
//				System.out.println(l1[2].substring(0, 12));
//				System.out.println(l2[2].substring(0, 12));
//				System.out.println(l3[2].substring(0, 12));
				
				String[] bss = (l1[1]+l2[1]+l3[1]).split(" ");
				byte[] bs = new byte[16];
				if(bss.length != 16)
					continue;
				for(int i = 0; i < 16; i++)
					bs[i] = (byte) (Integer.parseInt(bss[i], 16) & 0xff);
				
				Bundle bundle = new Bundle(
						Long.parseLong(l1[0].substring(0, l1[0].length()-1).trim(), 16), 
						bs,
						l1[2].substring(0,5),
						section
						);
//				System.out.println(Long.toHexString(convertToLong(bs, 10)));
				bundle.is[0] = new Instr((convertToLong(bs, 0) >> 5) & 0x1ffffffffffl, l1[2].substring(12), l1[2].substring(8, 10).contentEquals("  ")?0:Integer.parseInt(l1[2].substring(8, 10)));
				bundle.is[1] = new Instr((convertToLong(bs, 5) >> 6) & 0x1ffffffffffl, l2[2].substring(12), l2[2].substring(8, 10).contentEquals("  ")?0:Integer.parseInt(l2[2].substring(8, 10)));
				bundle.is[2] = new Instr((convertToLong(bs, 10) >> 7) & 0x1ffffffffffl, l3[2].substring(12), l3[2].substring(8, 10).contentEquals("  ")?0:Integer.parseInt(l3[2].substring(8, 10)));
				
//				if(bundle.templateStr.contains("-")) {
//					System.out.println(path + "\t" + bundle.addr);
//				}
				out.println(bundle);
				out.println();
				
				
			}
			if(l.startsWith("Disassembly of section ")) {
				section = l.substring(23, l.length()-1);
			}
		}
	}
	
	
	public static void main(String[] args) throws FileNotFoundException {
		out = new PrintWriter(new File("transscripted_dirty.txt"));
		try (Stream<Path> stream = Files.walk(Paths.get("C:\\Users\\kgeijsen\\Desktop\\Virtual Machines\\Ubuntu 22.04\\shared\\trash"))) {
			stream.filter(Files::isRegularFile).forEach(Transscription::dis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.flush();
		out.close();
	}
}
