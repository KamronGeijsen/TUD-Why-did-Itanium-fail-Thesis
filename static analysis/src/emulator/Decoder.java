package emulator;

public class Decoder {
	
	byte[] bytes;
	
	public Decoder(byte[] bytes) {
		this.bytes = bytes;
	}
	
	
	static class Arg {
		
	}
	static class Reg extends Arg {
		byte reg;
	}
	static class Addr extends Arg {
		byte base;
		byte inc;
	}
	static class FReg extends Arg {
		
	}
	
	static class Instruction {
		long bits;
		int addr;
		byte pred;
		byte unit;
	}
	
	static class Mov extends Instruction {
		Reg dst;
		Reg src;
	}
}
