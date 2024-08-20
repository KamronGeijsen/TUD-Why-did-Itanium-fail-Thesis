package parsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Parsing {
	
	
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
	
	static HashMap<String, Integer> formInd = new HashMap<>() {{
		for(int i = 0; i < 32; i++)
			put(formats[i], i);
	}};
	
	static class Bundle {
		
		public Bundle(String[] line) {
			this.addr = Long.parseLong(line[0].substring(0,line[0].length()-1),16);
			try {
				this.template = formInd.getOrDefault(line[2], -1);
			} catch(NullPointerException e) {
//				System.out.println(e);
//				System.out.println(Arrays.toString(line));
			}
			
			if(".text".contentEquals(line[3]))
				this.section = ".text";
			else this.section = null;
		}
		long addr;
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
		
		public Instr(String[] line) {
			this.bytes = Long.parseLong(line[0],16);
			if(line[1].isEmpty())
				this.pred = 0;
			else
				this.pred = Integer.parseInt(line[1]);
			if("nop.m 0x0".contentEquals(line[2]))
				this.asm = "nop.m 0x0";
			else if("nop.i 0x0".contentEquals(line[2]))
				this.asm = "nop.m 0x0";
			else if("nop.b 0x0".contentEquals(line[2]))
				this.asm = "nop.b 0x0";
			else if("-".contentEquals(line[2]))
				this.asm = "-";
			else
				this.asm = line[2];
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
	
	static class Parser extends Thread{
		String[] in;
		Bundle[] out;
		@Override
		public void run() {
			int i = 0;
			try {
			out = new Bundle[in.length/4];
			for(i = 0; i < out.length; i++) {
//				System.out.println(Arrays.toString(in[i].getBytes()));
//				System.out.println(in[i*4]);
				out[i] = new Bundle(in[i*4].split("\t"));
				out[i].is[0] = new Instr(in[i*4+1].split("\t"));
				out[i].is[1] = new Instr(in[i*4+2].split("\t"));
				out[i].is[2] = new Instr(in[i*4+3].split("\t"));
//				in[i*4] = in[i*4+1] = in[i*4+2] = in[i*4+3] = null;
			}
			
			in = null;
			} catch (Exception e) {
				System.out.println(in[i*4-4]);
				System.out.println(in[i*4-3]);
				System.out.println(in[i*4-2]);
				System.out.println(in[i*4-1]);
				
				System.out.println(in[i*4]);
				System.out.println(in[i*4+1]);
				System.out.println(in[i*4+2]);
				System.out.println(in[i*4+3]);
				e.printStackTrace();
				
				System.exit(0);
			}
		}
	}
	
	static Bundle[] bundles() throws FileNotFoundException, IOException {
		ArrayList<Bundle> bs = new ArrayList<>();
		ArrayList<Parser> ps = new ArrayList<>();
		
		long start = System.currentTimeMillis();
		
		try (BufferedReader br = new BufferedReader(new FileReader("transscripted_dirty.txt"), 0x100000)) {
			while(br.ready()) {
				Parser p = new Parser();
				p.in = new String[4232*4];
				for(int i = 0; i < 4232*4;) {
					p.in[i++] = br.readLine();
					p.in[i++] = br.readLine();
					p.in[i++] = br.readLine();
					p.in[i++] = br.readLine();
					br.readLine();
//					for(int l = i-4; l < i; l++)
//						System.out.println(p.in[l]);
				}
				p.start();
				ps.add(p);
			}

		}
		
		for(Parser p : ps) {
			try {
				p.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(Bundle b : p.out) {
//				if(b.section.equals(".text"))
					bs.add(b);
			}
			p.out = null;
		}
//		43505304
		System.out.println(bs.size() + "\t" + (System.currentTimeMillis()-start)/1000.0);
		
		return bs.toArray(n -> new Bundle[n]);
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Bundle[] bs = bundles();
		
		
		HashMap<String, Integer> instrFreq = new HashMap<>();
		for(Bundle b : bs) {
			String s;
			StringBuilder sb = new StringBuilder(formats[b.template].replace(";", ""));
			int l = 0;
			for(Instr i : b.is) {
//				String s = i.asm.replaceFirst(";;", "");
//				s = s.split(" ")[0];
				String[] ps = i.asm.replaceFirst(";;", "").split(" ");
				if(ps.length >= 2) {
					boolean eq = ps[1].contains("=");
					String[] pps = ps[1].split("=|,");
					for(int j = 0; j < pps.length; j++) {
						pps[j] = pps[j]
						.replaceFirst("^r[0-9]+$", "reg")
						.replaceFirst("^\\[r[0-9]+\\]$", "[reg]")
						.replaceFirst("^p[0-9]+$", "pred")
						.replaceFirst("^f[0-9]+$", "freg")
						.replaceFirst("^b[0-9]+$", "breg")
						.replaceFirst("^(-)?[0-9]+$", "imm")
						.replaceFirst("^0x[0-9a-f]+$", "imm")
						.replaceFirst("^[0-9a-f]+$", "hex");
//						s = pps[j];
//						instrFreq.put(s, instrFreq.getOrDefault(s, 0) + 1);
					}
					s = String.join(",", pps);
					if(eq)s = s.replaceFirst(",", "=");
					s = ps[0]+"\t"+s;
					instrFreq.put(s, instrFreq.getOrDefault(s, 0) + 1);
				}
					
				
			}
//			instrFreq.put(s, instrFreq.getOrDefault(s, 0) + 1);
		}
		
		try {
			PrintWriter out = new PrintWriter(new File("instr freqs.txt"));
			
			ArrayList<Entry<String, Integer>> es = new ArrayList<>();
			es.addAll(instrFreq.entrySet());
			
			es.sort(new Comparator<Entry<String, Integer>>() {

				@Override
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					return Integer.compare(o2.getValue(), o1.getValue());
				}
			});
			for(Entry<String, Integer> e : es) {
				out.println(e.getValue() + "\t" + e.getKey());
			}
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		
//		long[] ts = new long[32], ps = new long[64];
//		
//		for(Bundle b : bs) {
//			ts[b.template]++;
//			for(Instr i : b.is)
//				ps[i.pred]++;
//		}
		
//		for(int i = 0; i < 32; i++) {
//			System.out.println(i + "\t" + ts[i]);
//		}
//		System.out.println();
//		for(int i = 0; i < 64; i++) {
//			System.out.println(i + "\t" + ps[i]);
//		}
	}
}
