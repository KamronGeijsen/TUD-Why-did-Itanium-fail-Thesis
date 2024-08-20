package emulator;

import java.io.File;

public class CaseTests {
	
	
	static void arrFill() {
		Simulator simulator = new Simulator();
		simulator.loadFile(new File("emulatorData/fill.o3.txt"));
		simulator.init(0);	

		long addr = 0x4000;
		long len = 10;
		
		simulator.executeFunction(0, addr, len);
		
		
		for(int i = 0; i < len+1; i++)
			System.out.println(simulator.ram.getLong(0x4000 + i*8));
	}
	
	static void arrSum() {
		Simulator simulator = new Simulator();
		
		
		
		simulator.loadFile(new File("emulatorData/sum_arr.o3.txt"));
		simulator.init(0);

		long addr = 0x4000;
		long len = 10;
		
		for(int i = 0; i < len; i++)
			simulator.ram.putLong((int) (addr+i*8), (long)i);
		
		
		simulator.executeFunction(0, addr, len);
		
		
		System.out.println(simulator.gr[8]);
		
	}
	
	static void mergeSort() {
		Simulator simulator = new Simulator();
		
		
		
		simulator.loadFile(new File("emulatorData/mergesort.o3.txt"));
		simulator.init(0);

		long addr = 0x4000;
		long start = 0;
		long end = 10;
		
		
		for(int i = 0; i < end; i++)
			simulator.ram.putLong((int) (addr+i*8), new long[] {5,1,7,2,3,8,9,0,4,6}[i]);
		
		
		simulator.executeFunction(0x480, addr, start, end);
		
		
		System.out.println(simulator.gr[8]);
		
	}
	
	static void indexOf() {
		Simulator simulator = new Simulator();
		
		
		
		simulator.loadFile(new File("emulatorData/indexOf.o3.txt"));
		simulator.init(0);

		long addr = 0x4000;
		long len = 10;
		long element = 0;
		
		
		for(int i = 0; i < len; i++)
			simulator.ram.putLong((int) (addr+i*8), new long[] {5,1,7,2,3,8,9,0,4,6}[i]);
		
		
		simulator.executeFunction(0x0, addr, len, element);
		
		
		System.out.println(simulator.gr[8]);
		
	}
	
	static void memcpy() {
		Simulator simulator = new Simulator();
		
		
		
		simulator.loadFile(new File("emulatorData/memcpy.o3.txt"));
		simulator.init(0);

		
		long src = 0x4000;
		long dst = 0x4800;
		long len = 10;
		
		
		for(int i = 0; i < len; i++)
			simulator.ram.putLong((int) (src+i*8), new long[] {5,1,7,2,3,8,9,0,4,6}[i]);
		
		
		simulator.executeFunction(0x0, dst, src, len * 8);
		
		for(int i = 0; i < len; i++)
			System.out.println(simulator.ram.getLong((int)(dst + i*8)));
		
	}
	
	
	public static void main(String[] args) {
//		arrFill();
		arrSum();
//		mergeSort();
//		indexOf();
//		memcpy();
	}
}
