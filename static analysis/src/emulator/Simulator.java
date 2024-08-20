package emulator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;
import java.util.Stack;

public class Simulator {
	
	static final String[] formats = {
			"MII",	"MII;",
			"MI;I",	"MI;I;",
			"MLX",	"MLX;",
			null,	null,
			"MMI",	"MMI;",
			"M;MI",	"M;MI;",
			"MFI",	"MFI;",
			"MMF",	"MMF;",
			"MIB",	"MIB;",
			"MBB",	"MBB;",
			null,	null,
			"BBB",	"BBB;",
			"MMB",	"MMB;",
			null,	null,
			"MFB",	"MFB;",
			null,	null,
		};
	static final String[] cmpStrings = {
			"cmp.lt",			"cmp.lt.unc",
			"cmp.eq.and",		"cmp.ne.and",
			"cmp.gt.and",		"cmp.le.and",
			"cmp.ge.and",		"cmp.lt.and",
			"cmp4.lt",			"cmp4.lt.unc",
			"cmp4.eq.and",		"cmp4.ne.and",
			"cmp4.gt.and",		"cmp4.le.and",
			"cmp4.ge.and",		"cmp4.lt.and",
			"cmp.ltu",			"cmp.ltu.unc",
			"cmp.eq.or",		"cmp.ne.or",
			"cmp.gt.or",		"cmp.le.or",
			"cmp.ge.or",		"cmp.lt.or",
			"cmp4.Itu",			"cmp4.ltu.unc",
			"cmp4.eq.or",		"cmp4.ne.or",
			"cmp4.gt.or",		"cmp4.le.or",
			"cmp4.ge.or",		"cmp4.lt.or",
			"cmp.eq",			"cmp.eq.unc",
			"cmp.eq.or.andcm",	"cmp.ne.or.andcm",
			"cmp.gt.or.andcm",	"cmp.le.or.andcm",
			"cmp.ge.or.andcm",	"cmp.lt.or.andcm",
			"cmp4.eq",			"cmp4.eq.unc",
			"cmp4.eq.or.andcm",	"cmp4.ne.or.andcm",
			"cmp4.gt.or.andcm",	"cmp4.le.or.andcm",
			"cmp4.ge.or.andcm",	"cmp4.lt.or.andcm",
	};
	
	
//	public static void main(String[] args) {
////		File file = new File("emulatorData\\template");
////		File file = new File("emulatorData\\test2 - fill");
//		Simulator simulator = new Simulator();
//
//		File file = new File("emulatorData\\mergesort.o3.txt");
//		
//		simulator.loadFile(file);
//		simulator.init(0x480);
//		
//		simulator.executeCycle();
//		
//		
//	}

	public void init(long ip) {
		this.ip = ip;
		ram.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	public void loadFile(File file) {
		ByteArrayOutputStream bb = new ByteArrayOutputStream(0x10000);
		Scanner sc;
		try {
			sc = new Scanner(file);
			for(String line; sc.hasNext(); ) {
				line = sc.nextLine();
//				System.out.println(line);
				int len = line.length();
				int addr = line.indexOf(":");
				for(int i = addr + 2; i < len; i+=3) {
					int n1 = Character.digit(line.charAt(i), 16);
					int n2 = Character.digit(line.charAt(i+1), 16);
					if(n1 != -1) {
//						System.out.println(line.charAt(i) + "" + line.charAt(i+1) + "\t" + bb.toByteArray().length);
						bb.write(n1 * 16 + n2);
					} else
						break;
				}
				
			}
			ram.put(bb.toByteArray());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	
	long[] gr = new long[128]; // General registers
	boolean[] nat = new boolean[128]; // NAT registers
	long[] fr = new long[128]; // Floating point registers
	long[] br = new long[8]; // Branch registers
	boolean[] pr = new boolean[64]; // Predicate registers
	long[] ar = new long[128]; // Application registers
	long ip = 0; // Instruction pointer
	
	Stack<Long> shadowStack = new Stack<>();
	Stack<long[]> registerStack = new Stack<>();
	
	ByteBuffer ram = ByteBuffer.allocate(1024*1024*1024);
	
	class BundleBuffer {
		final long[] instructionBuffer = new long[8*3];
		final long[] addrBuffer = new long[8*3];
		int end;
		int len;
		
		
		boolean bufferReady() {
			return len < 8*3 - 2*3;
		}
		boolean popReady() {
			return len != 0;
		}
		long pop() {
			assert len != 0;
			
			int modEnd = end-len;
			modEnd = modEnd < 0 ? modEnd + 8*3 : modEnd;
			
			long instr = instructionBuffer[modEnd];
			len--;
			
			return instr;
		}
		long peekAddr() {
			int modStart = end-len;
			modStart = modStart < 0 ? modStart + 8*3 : modStart;
			return addrBuffer[modStart];
		}
		
		final static byte[] stopAndFormatBits = {
				4,2,2,
				4,2,10,
				4,10,2,
				4,10,10,
				4,3,5,
				4,3,13,
				0,0,0,
				0,0,0,
				4,4,2,
				4,4,10,
				12,4,2,
				12,4,10,
				4,1,2,
				4,1,10,
				4,4,1,
				4,4,9,
				4,2,0,
				4,2,8,
				4,0,0,
				4,0,8,
				0,0,0,
				0,0,0,
				0,0,0,
				0,0,8,
				4,4,0,
				4,4,8,
				0,0,0,
				0,0,8,
				4,1,0,
				4,1,8,
				0,0,0,
				0,0,0,
		};
		
		void bufferInstrs() {
			 
			
			for(int i = 0; i < 2; i++) {
				
				final int format = ram.get((int)ip) & 0x1f;
				
				instructionBuffer[end+0] = ((ram.getLong((int)ip+ 0)>>5) & 0x1ff_ffff_ffffl) | ((long)stopAndFormatBits[format*3] << 41);
				instructionBuffer[end+1] = ((ram.getLong((int)ip+ 5)>>6) & 0x1ff_ffff_ffffl) | ((long)stopAndFormatBits[format*3+1] << 41);
				instructionBuffer[end+2] = ((ram.getLong((int)ip+10)>>7) & 0x1ff_ffff_ffffl) | ((long)stopAndFormatBits[format*3+2] << 41);
				addrBuffer[end+0] = ip;
				addrBuffer[end+1] = ip;
				addrBuffer[end+2] = ip;
//				System.out.println(stopAndFormatBits[format*3] + "\t" + stopAndFormatBits[format*3+1] + "\t" + stopAndFormatBits[format*3+2]);
//				
//				System.out.println(format + "\t" + formats[format]);
//				System.out.println(String.format("%012x", instructionBuffer[end]));
//				System.out.println(String.format("%012x", instructionBuffer[end+1]));
//				System.out.println(String.format("%012x", instructionBuffer[end+2]));
				
				
				end = end + 3 == 8*3 ? 0 : end + 3;
				len += 3;
				ip += 16;
				
			}
		}
	}
	
	BundleBuffer bundleBuffer = new BundleBuffer();
	
	void executeFunction(long ip, long ... args) {
		this.ip = ip;
		System.out.println("Start " + ip);
		
		for(int i = 0; i < Math.min(args.length, 32); i++)
			gr[i+32] = args[i];
			
		
		shadowStack.push(0l);
		while(!shadowStack.isEmpty()) {
			executeCycle();
		}
	}
	
	void executeCycle() {
		String a = String.format("%016x", ram.getLong((int)ip+8)) + String.format("%016x", ram.getLong((int)ip));
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(a);
		
		if(bundleBuffer.bufferReady())
			bundleBuffer.bufferInstrs();
		
		System.out.println();
		
		System.out.println("\n===\n");
		
		int instrs = 0;
		for(int i = 0; i < 6 && bundleBuffer.popReady(); i++) {
			long addr = bundleBuffer.peekAddr();
			long instr = bundleBuffer.pop();
			
			
			executeInstr(instr & 0xfffffffffffl, addr);
			instrs ++;
			
			if((instr & 0x100000000000l) != 0)
				break;
			
		}
		System.out.println("IPC = " + instrs);
		System.out.println("NOPs = " + nopsThisCycle);
		System.out.println("#\t" + instrs + "\t" + nopsThisCycle);
		nopsThisCycle = 0;
		
		System.out.println("\n===\n");
	}
	
	int nopsThisCycle = 0;
	void executeInstr(long instr, long addr) {
		
		byte unit = (byte) ((instr >> 41) & 0x7);
		byte op = (byte) ((instr >> 37) & 0xf);
//		System.out.println(String.format("%012x", instr));
//		System.out.println(String.format("%x", (instr >> 37) & 0xf));
//		System.out.println(unit);
//		System.out.println(op);
		System.out.println(String.format("%04x", addr) + "\t" + String.format("%012x", instr) + "\top=" + op + "\tunit=" + unit);
		if (instr == 0x040008000000l) {
			System.out.println("nop.i 0x0");
			nopsThisCycle ++;
		} else if (instr == 0x080008000000l) {
			System.out.println("nop.m 0x0");
			nopsThisCycle ++;
		} else if (instr == 0x004000000000l) {
			System.out.println("nop.b 0x0");
			nopsThisCycle ++;
		} else if ((unit == 2 || unit == 4) && op == 9) {
			byte qp = (byte) ((instr >> 0) & 0x3F);
			byte r1 = (byte) ((instr >> 6) & 0x7F);
			long imm7b = (instr >> 13) & 0x7F;
			byte r3 = (byte) ((instr >> 20) & 0x03);
			long imm5c = (instr >> 22) & 0x1F;
			long imm9d = (instr >> 27) & 0x1FF;
			long s = (instr >> 36) & 0x01;
			
			long imm22 = -s << 21 | imm5c << 16 | imm9d << 7 | imm7b;
			
			System.out.println("addl" + "\tr" + r1 + "=" + imm22 + ",r" + r3);
			gr[r1] = imm22 + gr[r3];
			System.out.println("addl\tr%d=%d,%d  # %d".formatted(r1, imm22, gr[r3], gr[r1]));
			
		} else if((unit == 2 || unit == 4) && (op == 12 || op == 13 || op == 14)) {
			byte qp = (byte) ((instr >> 0) & 0x3F);
			byte p1 = (byte) ((instr >> 6) & 0x3F);
			byte c = (byte) ((instr >> 12) & 0x1);
			byte r2 = (byte) ((instr >> 13) & 0x7F);
			byte r3 = (byte) ((instr >> 20) & 0x7f);
			byte p2 = (byte) ((instr >> 27) & 0x3f);
			byte ta = (byte) ((instr >> 33) & 0x1);
			byte x2 = (byte) ((instr >> 34) & 0x3);
			byte tb = (byte) ((instr >> 36) & 0x1); //test bit
			
			if(x2 == 2 || x2 == 3)
				tb = 0;
			int fullOp = (op & 0x3) << 4 | (x2&1) << 3 | tb << 2 | ta << 1 | c;
//			System.out.println(x2);
//			if(tb == 0) { // compare
//				System.out.println("????UNKNOWN ALU");
//			}
			
			System.out.println("%s\tp%d,p%d=r%d,r%d".formatted(cmpStrings[fullOp], p1, p2, r2, r3));
			System.out.println(fullOp);
			switch(fullOp) {
			case 0: //cmp.lt
				pr[p2] = !(pr[p1] = gr[r2] < gr[r3]);
				System.out.println("%s\tp%d,p%d=%d,%d  # %b".formatted(cmpStrings[fullOp], p1, p2, gr[r2], gr[r3], pr[p1]));
				break;
			case 8: //cmp4.lt
				pr[p2] = !(pr[p1] = (int)gr[r2] < (int)gr[r3]);
				System.out.println("%s\tp%d,p%d=%d,%d  # %b".formatted(cmpStrings[fullOp], p1, p2, gr[r2], gr[r3], pr[p1]));
				break;
			case 16: //cmp.ltu 
				pr[p2] = !(pr[p1] = Long.compareUnsigned(gr[r2], gr[r3]) < 0);
				System.out.println("%s\tp%d,p%d=%d,%d  # %b".formatted(cmpStrings[fullOp], p1, p2, gr[r2], gr[r3], pr[p1]));
				break;
			case 24: //cmp4.Itu
				pr[p2] = !(pr[p1] = Integer.compareUnsigned((int)gr[r2], (int)gr[r3]) < 0);
				System.out.println("%s\tp%d,p%d=%d,%d  # %b".formatted(cmpStrings[fullOp], p1, p2, gr[r2], gr[r3], pr[p1]));
				break;
			case 32: //cmp.eq 
				pr[p2] = !(pr[p1] = gr[r2] == gr[r3]);
				System.out.println("%s\tp%d,p%d=%d,%d  # %b".formatted(cmpStrings[fullOp], p1, p2, gr[r2], gr[r3], pr[p1]));
				break;
			case 40: //cmp4.eq
				pr[p2] = !(pr[p1] = (int)gr[r2] == (int)gr[r3]);
				System.out.println("%s\tp%d,p%d=%d,%d  # %b".formatted(cmpStrings[fullOp], p1, p2, gr[r2], gr[r3], pr[p1]));
				break;
			default: 
				System.out.println("COMPARE %8s".formatted(Integer.toBinaryString(fullOp)));
				throw new RuntimeException("Unimplemented");
			}
			
		} else if((unit == 2 || unit == 4) && op == 8) {
			final byte qp = (byte) ((instr >> 0) & 0x3F);
			final byte r1 = (byte) ((instr >> 6) & 0x7F);
			final byte imm7b = (byte) ((instr >> 13) & 0x7F);
			final byte r2 = imm7b;
			final byte r3 = (byte) ((instr >> 20) & 0x7f);
			final byte x2b = (byte) ((instr >> 27) & 0x3);
			final byte x4 = (byte) ((instr >> 29) & 0xF);
			final byte ve = (byte) ((instr >> 33) & 0x1);
			final byte x2a = (byte) ((instr >> 34) & 0x3);
			final long s = (instr >> 36) & 0x01;
			
			if(x2a == 0) { //alu
				final int x6 = x4 << 2 | x2b;
				final long imm8 = -s << 7 | imm7b;

				switch(x6) {
				case 0:
					System.out.println("add\tr%d=r%d,r%d".formatted(r1, r2, r3));
					gr[r1] = gr[r2] + gr[r3];
					System.out.println("add\tr%d=%d,%d  # %d".formatted(r1, gr[r2], gr[r3], gr[r1]));
					break;
				case 1:
					System.out.println("add\tr%d=r%d,r%d,1".formatted(r1, r2, r3));
					gr[r1] = gr[r2] + gr[r3] + 1;
					System.out.println("add\tr%d=%d,%d,1  # %d".formatted(r1, gr[r2], gr[r3], gr[r1]));
					break;
				case 4:
					System.out.println("sub\tr%d=r%d,r%d,1".formatted(r1, r2, r3));
					gr[r1] = gr[r2] - gr[r3] - 1;
					System.out.println("sub\tr%d=%d,%d,1  # %d".formatted(r1, gr[r2], gr[r3], gr[r1]));
					break;
				case 5:
					System.out.println("sub\tr%d=r%d,r%d".formatted(r1, r2, r3));
					gr[r1] = gr[r2] - gr[r3];
					System.out.println("sub\tr%d=%d,%d  # %d".formatted(r1, gr[r2], gr[r3], gr[r1]));
					break;
				case 8:
					System.out.println("addp4\tr%d=r%d,r%d".formatted(r1, r2, r3));
					gr[r1] = Integer.toUnsignedLong((int)gr[r2] + (int)gr[r3]) | ((gr[r3] & 0xc0000000l) << 31);
					System.out.println("addp4\tr%d=%d,%d  # %d".formatted(r1, gr[r2], gr[r3], gr[r1]));
					break;
				case 12:
					System.out.println("and\tr%d=r%d,r%d".formatted(r1, r2, r3));
					gr[r1] = gr[r2] & gr[r3];
					System.out.println("and\tr%d=%d,%d  # %d".formatted(r1, gr[r2], gr[r3], gr[r1]));
					break;
				case 13:
					System.out.println("andcm\tr%d=r%d,r%d".formatted(r1, r2, r3));
					gr[r1] = gr[r2] & ~gr[r3];
					System.out.println("andcm\tr%d=%d,%d  # %d".formatted(r1, gr[r2], gr[r3], gr[r1]));
					break;
				case 14:
					System.out.println("or\tr%d=r%d,r%d".formatted(r1, r2, r3));
					gr[r1] = gr[r2] | gr[r3];
					System.out.println("or\tr%d=%d,%d  # %d".formatted(r1, gr[r2], gr[r3], gr[r1]));
					break;
				case 15:
					System.out.println("xor\tr%d=r%d,r%d".formatted(r1, r2, r3));
					gr[r1] = gr[r2] ^ gr[r3];
					System.out.println("xor\tr%d=%d,%d  # %d".formatted(r1, gr[r2], gr[r3], gr[r1]));
					break;
				case 16: case 17: case 18: case 19:
					System.out.println("shladd\tr%d=r%d,%d,r%d".formatted(r1, r2, x2b, r3));
					long res = (gr[r2] << (x2b+1)) + gr[r3];
					System.out.println("shladd\tr%d=%d,%d,%d  # %d".formatted(r1, gr[r2], x2b, gr[r3], res));
					gr[r1] = res;
					break;
				case 24: case 25: case 26: case 27:
					System.out.println("shladdp\tr%d=r%d,%d,r%d".formatted(r1, r2, x2b, r3));
					throw new RuntimeException("Emulator ran into unimplemented operation: ");
//					break;
				case 37:
					System.out.println("sub\tr%d=r%d,%d".formatted(r1, r3, imm8));
					gr[r1] = gr[r2] - gr[r3];
					System.out.println("sub\tr%d=%d,%d  # %d".formatted(r1, gr[r3], imm8, gr[r1]));
					break;
				case 44:
					System.out.println("and\tr%d=r%d,%d".formatted(r1, r3, imm8));
					gr[r1] = gr[r2] & gr[r3];
					System.out.println("and\tr%d=%d,%d  # %d".formatted(r1, gr[r3], imm8, gr[r1]));
					break;
				case 45:
					System.out.println("andcm\tr%d=r%d,%d".formatted(r1, r3, imm8));
					gr[r1] = gr[r2] & ~gr[r3];
					System.out.println("andcm\tr%d=%d,%d  # %d".formatted(r1, gr[r3], imm8, gr[r1]));
					break;
				case 46:
					System.out.println("or\tr%d=r%d,%d".formatted(r1, r3, imm8));
					gr[r1] = gr[r2] | gr[r3];
					System.out.println("or\tr%d=%d,%d  # %d".formatted(r1, gr[r3], imm8, gr[r1]));
					break;
				case 47:
					System.out.println("xor\tr%d=r%d,%d".formatted(r1, r3, imm8));
					gr[r1] = gr[r2] ^ gr[r3];
					System.out.println("xor\tr%d=%d,%d  # %d".formatted(r1, gr[r3], imm8, gr[r1]));
					break;
				default:
					System.out.println("????UNKNOWN ALU reg/imm\tr1=%d,imm7b=%d,r3=%d,x2b=%d,x4=%d,ve=%d,x2a=%d,s=%d".formatted(
							r1, imm7b, r3, x2b, x4, ve, x2a, s));
					throw new RuntimeException("Emulator ran into invalid operation: ");
				}
			} else if(x2a == 2) { //adds imm
				long imm14 = -s << 13 | x4 << 9 | x2b << 7 | imm7b;
				System.out.println("adds" + "\tr" + r1 + "=" + imm14 + ",r" + r3);
				long res = imm14 + gr[r3];
				System.out.println("adds\tr%d=%d,%d  # %d".formatted(r1, imm14, gr[r3], res));
				gr[r1] = res;
			} else if(x2a == 3) { //addp4 imm
				long imm14 = -s << 13 | x4 << 9 | x2b << 7 | imm7b;
				System.out.println("addp4" + "\tr" + r1 + "=" + imm14 + ",r" + r3);
				long res = Integer.toUnsignedLong((int)imm14 + (int)gr[r3]) | ((gr[r3] & 0xc0000000l) << 31);
				System.out.println("addp4\tr%d=%d,%d  # %d".formatted(r1, imm14, gr[r3], gr[r1]));
				gr[r1] = res;
			} else {
				System.out.println("????UNKNOWN multimedia");
				throw new RuntimeException("Unimplemented");
			}
		} else if(unit == 0 && (op == 4 || op == 5)) {
			
			final byte qp = (byte) ((instr >> 0) & 0x3F);
			final byte b1 = (byte) ((instr >> 6) & 0x7);
			final byte btype = b1;
//			byte p = (byte) ((instr >> 12) & 0x1);
			final boolean p = (instr & (1 << 11)) != 0;
			final long imm20b = (byte) ((instr >> 13) & 0xfffff);
//			byte wh = (byte) ((instr >> 33) & 0x3);
			final boolean whtk_nt = (instr & (1l << 32)) != 0;
			final boolean whsp_dk = (instr & (1l << 33)) != 0;
//			byte d = (byte) ((instr >> 35) & 0x1);
			final boolean d = (instr & (1l << 34)) != 0;
//			byte s = (byte) ((instr >> 36) & 0x1);
			final long s = instr & (1l << 35);
			
			
			final long offs23 = (-s << 20 | imm20b) << 4;
			if(op == 4) {
				if(btype == 0) { // cond
					System.out.print("(p%02d) ".formatted(qp));
					System.out.println("br.cond" + (whsp_dk?".dp":".sp")+(whtk_nt?"nt":"tk")+(p?".many":".few")+(d?".clr":"") + "\t" + Long.toHexString(addr + offs23));
//					System.out.println();
					System.out.print("(%b) ".formatted(pr[qp]));
					System.out.println("br.cond" + (whsp_dk?".dp":".sp")+(whtk_nt?"nt":"tk")+(p?".many":".few")+(d?".clr":"") + "\t" + Long.toHexString(addr + offs23));
					if(pr[qp]) {
						this.ip = addr + offs23;
						this.bundleBuffer.len = 0;
					}
				} else if(btype == 5) { // cloop
					if(qp != 0) {
						throw new RuntimeException("Illegal operation. CLOOP does not allow predication");
					}
					System.out.println("br.cloop " + (whsp_dk?".dp":".sp")+(whtk_nt?"nt":"tk")+(p?".many":".few")+(d?".clr":"") + "\t" + Long.toHexString(addr + offs23));
					System.out.println("br.cloop\tar.lc=%d %04x".formatted(ar[65], addr + offs23) );
					if(ar[65] != 0) {
						ar[65]--;
						this.ip = addr + offs23;
						this.bundleBuffer.len = 0;
					}
				} else {
					throw new RuntimeException("Unimplemented");
				}
				
				
			} else if (op == 5) {
				if(pr[qp]) {
					this.ip = addr + offs23;
					br[b1] = addr + 16;
					this.shadowStack.push(addr + 16);
					this.bundleBuffer.len = 0;
				}
			} else {
				System.out.println("UNKNOWN BR/COND ????");
				throw new RuntimeException("Unimplemented");
			}
		} else if(unit == 4 && op == 1) {
			byte qp = (byte) ((instr >> 0) & 0x3F);
			byte r1 = (byte) ((instr >> 6) & 0x7F);
			byte sof = (byte) ((instr >> 13) & 0x7F);
			byte sol = (byte) ((instr >> 20) & 0x7F);
			byte sor = (byte) ((instr >> 27) & 0xf);
			byte x3 = (byte) ((instr >> 33) & 0x7);
			
//			
			if(x3 == 6) {
				System.out.println("alloc\tr%d=ar.pfs,%d,%d,%d".formatted(r1, sof, sol, sor));
			} else {
				System.out.println("UNKNOWN MEM SP ????");
			}
		} else if(unit == 2 && op == 0) { // special INT operations
			final byte qp = (byte) ((instr >> 0) & 0x3F);
			final byte r1 = (byte) ((instr >> 6) & 0x7F);
			final byte imm7b = (byte) ((instr >> 13) & 0x7F);
			final byte r2 = imm7b; // field alias
			final byte r3 = (byte) ((instr >> 20) & 0x7f);
			final byte ar3 = r3; // field alias
			final byte x6 = (byte) ((instr >> 27) & 0x3f);
			final byte x3 = (byte) ((instr >> 33) & 0x7);
			final long s = (instr >> 36) & 0x01;
			
			if(x3 == 0) {
				final long imm8 = -s << 7 | imm7b;
				
				final long imm20a = (instr >> 6) & 0xFFFFF;
				final long imm21 = s << 20 | imm20a;
				
				switch(x6) {
				case 0:
					System.out.println("break.i\t%d".formatted(imm21));
					break;
				case 1:
					final boolean y = ((instr >> 26) & 1) != 0;
					if(y)
						System.out.println("hint.i\t%d".formatted(imm21));
					else
						System.out.println("nop.i\t%d".formatted(imm21));
				case 10: { // mov.i to ar - imm8
					System.out.println("mov.i\tar%d=%d".formatted(r1,imm8));
					this.ar[r1] = imm8;
					System.out.println("mov.i\tar%d=%d  # %d".formatted(r1,imm8,imm8));
					break;
				}
				case 16: {
					System.out.println("zxt1\tr%d=r%d".formatted(r1,r3));
					long res = gr[r3] & 0xff;
					System.out.println("zxt1\tr%d=%d  # %d".formatted(r1,gr[r3],res));
					gr[r3] = res;
					break;
				}
				case 17: {
					System.out.println("zxt2\tr%d=r%d".formatted(r1,r3));
					long res = gr[r3] & 0xffff;
					System.out.println("zxt2\tr%d=%d  # %d".formatted(r1,gr[r3],res));
					gr[r3] = res;
					break;
				}
				case 18: {
					System.out.println("zxt4\tr%d=r%d".formatted(r1,r3));
					long res = gr[r3] & 0xffffffff;
					System.out.println("zxt4\tr%d=%d  # %d".formatted(r1,gr[r3],res));
					gr[r3] = res;
					break;
				}
				case 20: {
					System.out.println("sxt1\tr%d=r%d".formatted(r1,r3));
					long res = (long)(byte)gr[r3];
					System.out.println("sxt1\tr%d=%d  # %d".formatted(r1,gr[r3],res));
					gr[r3] = res;
					break;
				}
				case 21: {
					System.out.println("sxt2\tr%d=r%d".formatted(r1,r3));
					long res = (long)(short)gr[r3];
					System.out.println("sxt2\tr%d=%d  # %d".formatted(r1,gr[r3],res));
					gr[r3] = res;
					break;
				}
				case 22: {
					System.out.println("sxt4\tr%d=r%d".formatted(r1,r3));
					long res = (long)(int)gr[r3];
					System.out.println("sxt4\tr%d=%d  # %d".formatted(r1,gr[r3],res));
					gr[r3] = res;
					break;
				}
				case 24:
					System.out.println("czx1.l\tr%d=r%d".formatted(r1,r3));
					throw new RuntimeException("Unimplemented");
//					break;
				case 25:
					System.out.println("czx2.l\tr%d=r%d".formatted(r1,r3));
					throw new RuntimeException("Unimplemented");
//					break;
				case 26:
					System.out.println("czx1.r\tr%d=r%d".formatted(r1,r3));
					throw new RuntimeException("Unimplemented");
//					break;
				case 27:
					System.out.println("czx2.r\tr%d=r%d".formatted(r1,r3));
					throw new RuntimeException("Unimplemented");
//					break;
				case 42: { // mov.i to ar
					System.out.println("mov.i\tar%d=r%d".formatted(r3,r2));
					ar[r3] = gr[r2];
					System.out.println("mov.i\tar%d=%d  # %d".formatted(r3,gr[r2],gr[r2]));
					break;
				}
				case 48: {
					System.out.println("mov\tr%d=ip".formatted(r1));
					long res = addr;
					System.out.println("mov\tr%d=ip  # %d".formatted(r1,res));
					gr[r3] = res;
					break;
				}
				case 49: {
					final int b2 = r2 & 0x7;
					System.out.println("mov\tr%d=b%d".formatted(r1,b2));
					long res = br[b2];
					System.out.println("mov\tr%d=b%d  # %d".formatted(r1,b2,res));
					gr[r3] = res;
					break;
				}
				case 50:
					System.out.println("mov\tr%d=ar%d".formatted(r1,ar3));
					gr[r1] = ar[ar3];
					System.out.println("mov\tr%d=%d  # %d".formatted(r1,ar[ar3],ar[ar3]));
					break;
				case 51:
					System.out.println("mov\tr%d=pr".formatted(r1));
					throw new RuntimeException("Unimplemented");
//					break;
//				case 18:
//					System.out.println("zxt4\tr%d,%d".formatted(r1,r3));
//					break;
					
				default:
					System.out.println("????UNKNOWN INT special\tr1=%d,r2=%d,r3=%d,x6=%d,x3=%d,s=%d".formatted(
							r1, r2, r3, x6, x3, s));
					throw new RuntimeException("Emulator ran into invalid operation: ");
				}
			} else if (x3 == 1){ // chk.s.i
				System.out.println("????UNKNOWN INT special\tr1=%d,r2=%d,r3=%d,x6=%d,x3=%d,s=%d".formatted(
						r1, r2, r3, x6, x3, s));
				throw new RuntimeException("Emulator ran into invalid operation: ");
				
			} else if (x3 == 2){ // mov to pr.rot - imm44
				System.out.println("????UNKNOWN INT special\tr1=%d,r2=%d,r3=%d,x6=%d,x3=%d,s=%d".formatted(
						r1, r2, r3, x6, x3, s));
				throw new RuntimeException("Emulator ran into invalid operation: ");
				
			} else if (x3 == 3){ // mov to pr
				System.out.println("????UNKNOWN INT special\tr1=%d,r2=%d,r3=%d,x6=%d,x3=%d,s=%d".formatted(
						r1, r2, r3, x6, x3, s));
				throw new RuntimeException("Emulator ran into invalid operation: ");
				
			} else if (x3 == 7){ // mov to b
				final byte wh = (byte) ((instr >> 20)&0x3);
				final byte x = (byte) ((instr >> 22)&0x1);
				final byte ih = (byte) ((instr >> 23)&0x1);
				final long timm9c = (instr >> 24)&0x1ff;
				final byte b1 = (byte) (r1 & 0x7);
				
				final long tag = addr + (timm9c << 16);
				System.out.println("mov\tb%d=r%d,%d".formatted(b1, r2, tag));
				br[b1] = gr[r2];
				System.out.println("mov\tb%d=%d,%d  # %d".formatted(b1, gr[r2], tag, gr[r2]));
			} else {
				
				System.out.println("????UNKNOWN INT special\tr1=%d,r2=%d,r3=%d,x6=%d,x3=%d,s=%d".formatted(
						r1, r2, r3, x6, x3, s));
				throw new RuntimeException("Emulator ran into invalid operation: ");
			}
			
		} else if(instr  == 0x000108001100l) {
			System.out.println("br.ret.sptk.many b0");
			this.bundleBuffer.len = 0;
			this.ip = shadowStack.pop();
		} else if(unit == 2 && op == 5) { // shift/bitfield
			final byte qp = (byte) ((instr >> 0) & 0x3F);
			final byte r1 = (byte) ((instr >> 6) & 0x7F);
			final byte r2 = (byte) ((instr >> 13) & 0x7F);
			final byte r3 = (byte) ((instr >> 20) & 0x7F);
			final byte len6d = (byte) ((instr >> 27) & 0x3F);
			final byte x = (byte) ((instr >> 33) & 0x1);
			final byte x2 = (byte) ((instr >> 34) & 0x3);
			final byte y = (byte) ((x != 0 ? instr >> 26 : instr >> 13) & 0x1);
			final int x4 = x << 3 | x2 << 1 | y;
			final byte len4d = (byte) ((instr >> 27) & 0xF);
			final byte cpos6d = (byte) ((instr >> 31) & 0x3F);
			final byte cpos6c = (byte) ((instr >> 20) & 0x3F);
			final byte cpos6b = (byte) ((instr >> 15) & 0x3F);
			final byte s = (byte) ((instr >> 35) & 0x3F);
			
			switch (x4) {
			case 0: // test Bit
				throw new UnsupportedOperationException("Unsupported operation");
			case 1: // test special
				throw new UnsupportedOperationException("Unsupported operation");
			case 2: { // extr.u
				System.out.println("extr.u\tr%d=r%d,%d,%d".formatted(r1, r2, cpos6b, len6d));
				final byte len = (byte) (len6d + cpos6b > 64 ? 64-cpos6b : len6d);
				long res = gr[r2] >> cpos6c & ((1l << len) - 1) | (-1l << len);
				System.out.println("extr.u\\tr%d=%d,%d,%d  # %d".formatted(r1, gr[r2], cpos6b, len6d, res));
				gr[r1] = res;
				break;
			}
			case 3: {// extr
				System.out.println("extr\tr%d=r%d,%d,%d".formatted(r1, r2, cpos6b, len6d));
				final byte len = (byte) (len6d + cpos6b > 64 ? 64-cpos6b : len6d);
				long res = gr[r2] >> cpos6c & ((1l << len) - 1);
				System.out.println("extr\\tr%d=%d,%d,%d  # %d".formatted(r1, gr[r2], cpos6b, len6d, res));
				gr[r1] = res;
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + x4);
			}
		} else if((unit == 4) && (op == 4 || op == 5 || op == 6 || op == 7)) {
			byte qp = (byte) ((instr >> 0) & 0x3F);
			byte r1 = (byte) ((instr >> 6) & 0x7F);
			byte r2 = (byte) ((instr >> 13) & 0x7F);
			byte r3 = (byte) ((instr >> 20) & 0x7F);
			byte x = (byte) ((instr >> 27) & 0x1);
			byte hint = (byte) ((instr >> 28) & 0x3);
			byte x6 = (byte) ((instr >> 30) & 0x3F);
			byte m = (byte) ((instr >> 36) & 0x1);
//			
			if(x6 == 0) {
				System.out.println("ld1\tr%d=[r%d]".formatted(r1, r3));
				long res = this.ram.get((int)gr[r3]);
				System.out.println("ld1\tr%d=[%d]  # %d".formatted(r1, gr[r3], res));
				gr[r1] = res;
			} else if(x6 == 1) {
				System.out.println("ld2\tr%d=[r%d]".formatted(r1, r3));
				long res = this.ram.getShort((int)gr[r3]);
				System.out.println("ld2\tr%d=[%d]  # %d".formatted(r1, gr[r3], res));
				gr[r1] = res;
			} else if(x6 == 2) {
				System.out.println("ld4\tr%d=[r%d]".formatted(r1, r3));
				long res = this.ram.getInt((int)gr[r3]);
				System.out.println("ld4\tr%d=[%d]  # %d".formatted(r1, gr[r3], res));
				gr[r1] = res;
			} else if(x6 == 3) {
				System.out.println("ld8\tr%d=[r%d]".formatted(r1, r3));
				long res = this.ram.getLong((int)gr[r3]);
				System.out.println("ld8\tr%d=[%d]  # %d".formatted(r1, gr[r3], res));
				gr[r1] = res;
			} else if(x6 == 48){
				System.out.println("st1\t[r%d]=r%d".formatted(r3, r2));
				this.ram.put((int)gr[r3], (byte)gr[r2]);
				System.out.println("st1\t[%d]=%d".formatted(gr[r3], (byte)gr[r2]));
			} else if(x6 == 49){
				System.out.println("st2\t[r%d]=r%d".formatted(r3, r2));
				this.ram.putShort((int)gr[r3], (short)gr[r2]);
				System.out.println("st2\t[%d]=%d".formatted(gr[r3], (short)gr[r2]));
			} else if(x6 == 50){
				System.out.println("st4\t[r%d]=r%d".formatted(r3, r2));
				this.ram.putInt((int)gr[r3], (int)gr[r2]);
				System.out.println("st4\t[%d]=%d".formatted(gr[r3], (int)gr[r2]));
			} else if(x6 == 51){
				System.out.println("st8\t[r%d]=r%d".formatted(r3, r2));
				this.ram.putLong((int)gr[r3], gr[r2]);
				System.out.println("st8\t[%d]=%d".formatted(gr[r3], gr[r2]));
			
			} else {
				System.out.println("Unknown Load/store???\t" + x6);
			}
		} else {
			System.out.println("????UNKNOWN???");
		}
		System.out.println();
	}
	
	// Load is 2+ cycles
	// Two writes to the same reg is undefined
	// WAW, RAW
	// Instruction latency = Exec latency and instruction bypass
	
	// tbit can only be executed in I0
	// instruction group over cache lines
	// stalls are not affected by predicates (even if it is NOT executed, it WILL stall)
	
}
