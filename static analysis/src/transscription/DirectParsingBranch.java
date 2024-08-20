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

public class DirectParsingBranch {
	
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
					out = new PrintWriter(new File("stuff3.txt"));
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
	static String[] aaaaa = {
			".sptk.few",
			".sptk.many",
			".spnt.few",
			".spnt.many",
			".dptk.few",
			".dptk.many",
			".dpnt.few",
			".dpnt.many",
			".sptk.few.clr",
			".sptk.many.clr",
			".spnt.few.clr",
			".spnt.many.clr",
			".dptk.few.clr",
			".dptk.many.clr",
			".dpnt.few.clr",
			".dpnt.many.clr",
	};
	static String[] aaaaa2 = {
			".cond",  // op = 0
			".ia",
			".?",
			".?",
			".ret",
			".?",
			".?",
			".?",
			".call",  // op = 1
			".call",
			".call",
			".call",
			".call",
			".call",
			".call",
			".call",
			".?",".?",".?",".?",".?",".?",".?",".?",
			".?",".?",".?",".?",".?",".?",".?",".?",
			".cond", // op = 4
			".?",
			".wexit",
			".wtop",
			".?",
			".cloop",
			".cexit",
			".ctop",
			".call",  // op = 5
			".call",
			".call",
			".call",
			".call",
			".call",
			".call",
			".call",
	};
	static void br(long bytes, String line, int addrEnd) {
		int hints = (int) (((bytes >> 32) & 0xe) | ((bytes >> 12) & 0x1));
		
//		int btype = (int) ((bytes >> 6) & 0x7);
//		
//		int operation = (int) ((bytes >> 37) & 0xf);
		
		int op = (int) (((bytes >> 34) & 0x78) | ((bytes >> 6) & 0x7));
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("br");
		if((op == 0 || op == 32) && (bytes&0x3f) == 0) {
			sb.append(((bytes >> 12) & 0x1) == 0 ? ".few" : ".many");
		} else {
			sb.append(aaaaa2[op]);
			sb.append(aaaaa[hints]);
		}
		
		sb.append(' ');
		if(op >= 32 && op <= 47) {
			long imm = ((bytes >> 13) & 0xfffff) + ((bytes & 0x10_0000_0000l) == 0 ? 0 : -0x100000l);
			if(op >= 40) {
				sb.append('b');
				sb.append((char)('0' + (op & 0x7)));
				sb.append('=');
			}
			long thisAddr;
			try {
				thisAddr = Long.parseLong(line.substring(0,addrEnd-34).trim(), 16);
			} catch(NumberFormatException e) {
				thisAddr = 0;
			}
			sb.append(Long.toHexString((thisAddr+imm)*16));
		} if(op >= 0 && op <= 15) {
			if(op >= 8) {
				sb.append('b');
				sb.append((char)('0' + (op & 0x7)));
				sb.append('=');
			}
			sb.append('b');
			sb.append((char)('0' + ((bytes >> 13) & 0x7)));
		}
		if(!line.substring(addrEnd).replace("0x","").startsWith(sb.toString())) {
			System.out.println(line);
			System.out.println(line.substring(addrEnd).replace("0x",""));
			System.out.println(sb);
			System.out.println();
		}
		
//		if(!line.substring(addrEnd, line.indexOf(' ', addrEnd)).contentEquals(sb)) {
//			System.out.println(Long.toHexString(bytes + 0x10_00000_00000l).substring(1) + "\t" + aaaaa2[op] + aaaaa[hints]);
//			System.out.println(line.substring(addrEnd, line.indexOf(' ', addrEnd)));
//			System.out.println();
//		}
		
//		StringBuilder sb = new StringBuilder(Long.toBinaryString(0x2_00000_00000l + bytes));
//		sb.deleteCharAt(0);
//		sb.insert(4, ' '); // 0
//		sb.insert(6, ' '); // 1
//		sb.insert(8, ' '); // 2
//		sb.insert(11, ' '); // 3
//		sb.insert(32, ' '); // 4
//		sb.insert(34, ' ');
//		sb.insert(38, ' ');
//		sb.insert(42, ' ');
//		System.out.println(sb);
////		System.out.println(hints);
////		System.out.println(line);
//		System.out.println(line.substring(addrEnd-12));
//		System.out.println(Long.toHexString(bytes + 0x10_00000_00000l).substring(1) + "\t" + aaaaa2[op] + aaaaa[hints]);
//		
//		
//		sb.delete(42, 49);
//		sb.delete(35, 39);
//		sb.delete(12, 33);
//		sb.delete(4, 6);
//		sb.append('\t');
//		sb.append(line.substring(addrEnd, line.indexOf(' ', addrEnd)));
////		System.out.println(sb);
//		System.out.println(line.substring(addrEnd, line.indexOf(' ', addrEnd)));
////		String newString = sb.toString();
////		globalHashes.put(newString, globalHashes.getOrDefault(newString, 0l)+1);
//		System.out.println();
	}
	
	static ArrayList<Long> sizes = new ArrayList<Long>();
	static void dis(Path path) {
		final byte[] lut = {0,1,2,3,4,5,6,7,8,9,0,0,0,0,0,0,
							0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
							0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
							0,10,11,12,13,14,15};
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
//							System.out.println(line2);
//							System.out.println(line3);
							continue;
						}
						
						if(line.startsWith("data8", addrEnd+33) || line2.startsWith("data8", addrEnd+33) || line3.startsWith("data8", addrEnd+33))
							continue;
						
						if(line.startsWith("br.", addrEnd+33)) {
							long instr = (lut[line.charAt(addrEnd + 2) - '0'] << 4l) | (long)lut[line.charAt(addrEnd + 3) - '0'];
							instr |= ((lut[line.charAt(addrEnd + 5) - '0'] << 4l) | (long)lut[line.charAt(addrEnd + 6) - '0']) << 8l;
							instr |= ((lut[line.charAt(addrEnd + 8) - '0'] << 4l) | (long)lut[line.charAt(addrEnd + 9) - '0']) << 16l;
							instr |= ((lut[line.charAt(addrEnd + 11) - '0'] << 4l) | (long)lut[line.charAt(addrEnd + 12) - '0']) << 24l;
							instr |= ((lut[line.charAt(addrEnd + 14) - '0'] << 4l) | (long)lut[line.charAt(addrEnd + 15) - '0']) << 32l;
							instr |= ((lut[line.charAt(addrEnd + 17) - '0'] << 4l) | (long)lut[line.charAt(addrEnd + 18) - '0']) << 40l;
							instr = (instr >> 5) & 0x1ffffffffffl;
							
							br(instr, line, addrEnd+33);
						}
						if(line2.startsWith("br.", addrEnd+33)) {
							long instr = (lut[line.charAt(addrEnd + 17) - '0'] << 4l) | (long)lut[line.charAt(addrEnd + 18) - '0'];
							instr |= ((lut[line2.charAt(addrEnd + 2) - '0'] << 4l) | (long)lut[line2.charAt(addrEnd + 3) - '0']) << 8l;
							instr |= ((lut[line2.charAt(addrEnd + 5) - '0'] << 4l) | (long)lut[line2.charAt(addrEnd + 6) - '0']) << 16l;
							instr |= ((lut[line2.charAt(addrEnd + 8) - '0'] << 4l) | (long)lut[line2.charAt(addrEnd + 9) - '0']) << 24l;
							instr |= ((lut[line2.charAt(addrEnd + 11) - '0'] << 4l) | (long)lut[line2.charAt(addrEnd + 12) - '0']) << 32l;
							instr |= ((lut[line2.charAt(addrEnd + 14) - '0'] << 4l) | (long)lut[line2.charAt(addrEnd + 15) - '0']) << 40l;
							instr = (instr >> 6) & 0x1ffffffffffl;
							
							br(instr, line2, addrEnd+33);
						}
						if(line3.startsWith("br.", addrEnd+33)) {
							long instr = (lut[line2.charAt(addrEnd + 14) - '0'] << 4l) | (long)lut[line2.charAt(addrEnd + 15) - '0'];
							instr |= ((lut[line2.charAt(addrEnd + 17) - '0'] << 4l) | (long)lut[line2.charAt(addrEnd + 18) - '0']) << 8l;
							instr |= ((lut[line3.charAt(addrEnd + 2) - '0'] << 4l) | (long)lut[line3.charAt(addrEnd + 3) - '0']) << 16l;
							instr |= ((lut[line3.charAt(addrEnd + 5) - '0'] << 4l) | (long)lut[line3.charAt(addrEnd + 6) - '0']) << 24l;
							instr |= ((lut[line3.charAt(addrEnd + 8) - '0'] << 4l) | (long)lut[line3.charAt(addrEnd + 9) - '0']) << 32l;
							instr |= ((lut[line3.charAt(addrEnd + 11) - '0'] << 4l) | (long)lut[line3.charAt(addrEnd + 12) - '0']) << 40l;
							instr = (instr >> 7) & 0x1ffffffffffl;
							
							br(instr, line3, addrEnd+33);
						}
						
//						if(line2.contains(";;") != (line2.charAt(line2.length()-1) == ';'))
//							System.out.println(line2);
//						if(line3.contains(";;") != (line3.charAt(line3.length()-1) == ';'))
//							System.out.println(line3);
						
//						if(line.startsWith("nop.", addrEnd+33)) {}//sb.append('-');
//						else if(line.startsWith("br.", addrEnd+33)) sb.append(line.charAt(addrEnd+30) == ' '?'b':'B');
//						else sb.append(line.charAt(addrEnd+22));
//						if(line.charAt(line.length()-1) == ';') {
//							sb.append(';');
//							sb.append('\t');
//							sb.append(textSection);
//							String newString = sb.toString();
//							if(newString.length() > 200) System.out.println(path + "\t" + line.substring(0, addrEnd));
//							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
//							sb.setLength(0);
//						}
//						
//						if(line2.startsWith("nop.", addrEnd+33)) {}//sb.append('-');
//						else if(line2.startsWith("br.", addrEnd+33)) sb.append(line2.charAt(addrEnd+30) == ' '?'b':'B');
//						else sb.append(line.charAt(addrEnd+23));
//						if(line2.charAt(line2.length()-1) == ';' && line.charAt(addrEnd+23)!='L') {
//							sb.append(';');
//							sb.append('\t');
//							sb.append(textSection);
//							String newString = sb.toString();
//							if(newString.length() > 200) System.out.println(path + "\t" + line.substring(0, addrEnd));
//							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
//							sb.setLength(0);
//						}
//						
//						if(line3.startsWith("nop.", addrEnd+33)) {}//sb.append('-');
//						else if(line3.startsWith("br.", addrEnd+33)) sb.append(line3.charAt(addrEnd+30) == ' '?'b':'B');
//						else sb.append(line.charAt(addrEnd+24));
//						if(line3.charAt(line3.length()-1) == ';' || (line2.charAt(line2.length()-1) == ';' && line.charAt(addrEnd+23)=='L')) {
//							
//							sb.append(';');
//							sb.append('\t');
//							sb.append(textSection);
//							String newString = sb.toString();
//							if(newString.length() > 200) System.out.println(path + "\t" + line.substring(0, addrEnd));
//							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
//							sb.setLength(0);
//						}
//						
//						if(sb.indexOf("b") != -1) {
//							sb.append('\t');
//							sb.append(textSection);
//							String newString = sb.toString();
//							if(newString.length() > 200) System.out.println(path + "\t" + line.substring(0, addrEnd));
//							localHashes.put(newString, localHashes.getOrDefault(newString, 0l)+1);
//							sb.setLength(0);
//						}
						
						
						
					}
				} else if(addrEnd == len-1 && len > 16) {
					sb.append('\t');
					sb.append(textSection);
					String newString = sb.toString();
					if(newString.length() > 200) System.out.println(path + "\t" + line.substring(0, addrEnd));
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
//			.parallel()
			.filter(Files::isRegularFile)
			.forEach(DirectParsingBranch::dis);
		System.out.println("Took " + (System.currentTimeMillis()-startTime)/1000 + "s");
//		sizes.sort(null);
//		for(long l : sizes)
//			System.out.println(l);
//		try {
//			out = new PrintWriter(new File("stuff.txt"));
//			System.out.println(globalHashes.size());
//			for(Map.Entry<String, Long> entry : globalHashes.entrySet()) {
//				out.println(entry.getValue() +"\t"+ entry.getKey().replace(' ', '\t'));
//			}
//			out.flush();
//			out.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		
//		out.flush();
//		out.close();
		
		
	}
}
