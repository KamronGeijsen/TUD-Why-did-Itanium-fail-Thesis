package bundles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BestBundles {
	
	
	private static String toStr(int[] bundle) {

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 6; i++)
			for(int l = 0; l < bundle[i]; l++)
				sb.append("ABFILM".charAt(i));
		return sb.toString();
	}
	
	class Bundle {
		String text;
		int[][] parsed;
		boolean[] ends;
		
		public Bundle(String text) {
			this.text = text;
			int i = 0, c = 0;
			for(; i < text.length(); i++)
				if(text.charAt(i) == ';')
					c++;
			if(text.charAt(text.length()-1) != ';') {
				c++;
			}
			
			parsed = new int[c][6];
			ends = new boolean[c];
			
			for(c = 0, i = 0; i < text.length(); i++) {
				switch(text.charAt(i)) {
				case 'A': parsed[c][0]++; break;
				case 'B': parsed[c][1]++; break;
				case 'F': parsed[c][2]++; break;
				case 'I': parsed[c][3]++; break;
				case 'L': parsed[c][4]++; break;
				case 'M': parsed[c][5]++; break;
				case ';': ends[c++] = true; break;
				default:
				}
			}
			
//			System.out.println("\n"+text);
//			for(i = 0; i < ends.length; i++) {
//				System.out.println(Arrays.toString(parsed[i]) + "\t" + ends[i]);
//			}
//				
		}
		
	}
	
	class Schedule implements Comparable<Schedule> {
		Bundle[] schedule;
		int[] nextHead;
		int index;
		int nops;
		
		
		
		public Schedule(Bundle[] schedule, int[] nextHead, int index, int nops) {
			this.schedule = schedule;
			this.nextHead = nextHead;
			this.index = index;
			this.nops = nops;
			
			
		}



		@Override
		public int compareTo(Schedule o) {
			int c = Integer.compare(nops, o.nops);
			if(c == 0)
				return -Integer.compare(schedule.length, o.schedule.length);
			return c;
		}
		
		@Override
		public String toString() {
			return Arrays.stream(schedule).map(s -> s.text).collect(Collectors.joining(" ")) + " -> " + toStr(nextHead) + " :: " + index;
		}
		
	}
	
	public BestBundles() throws IOException {
		
//		BufferedReader f = new BufferedReader(new FileReader("big file.txt"));
		
		
		ArrayList<int[]> parsed = new ArrayList<>();
		Scanner sc = new Scanner(new File("big file.txt"));
		sc.useDelimiter(";");
		for(int i = 0; i < 1767; i++) {
			sc.next();
		}
		
		int chars = 2;
		StringBuilder compiledSchedule = new StringBuilder();
		for(int i = 0; i < 1000; i++) {
//			System.out.println();
			String line = sc.next();
			
			int[] bundle = new int[6];
			for(char c : line.toCharArray()) {
				switch(c) {
				case 'A': bundle[0]++; break;
				case 'B': bundle[1]++; break;
				case 'F': bundle[2]++; break;
				case 'I': bundle[3]++; break;
				case 'L': bundle[4]++; break;
				case 'M': bundle[5]++; break;
				default:
				}
				
				
				
				if(c != ';') {
					chars++;
					if(chars == 3) {
						chars = 0;
						compiledSchedule.append(' ');
					}
				}
				compiledSchedule.append(c);
				
			}
			compiledSchedule.append(';');
			
			parsed.add(bundle);
			
			if(bundle[0] > 0) {
				for(int l = 0; l < bundle[0]; l++) {
					if(bundle[5] < bundle[3]) bundle[5]++;
					else bundle[3]++;
				}
				bundle[0] = 0;
			}
			
//			System.out.println(Arrays.toString(bundle));
		}
		
		
		System.out.println(compiledSchedule.toString().chars().filter(Character::isWhitespace).count() + " bundles total (with nops)");
		compiledSchedule = new StringBuilder(compiledSchedule.toString().replaceAll("[a-z]{3} ", ""));
		System.out.println(compiledSchedule);
		System.out.println(compiledSchedule.toString().chars().filter(Character::isLowerCase).count() + "\t" + compiledSchedule);
//		System.out.println(compiledSchedule.substring(0, 1000));
		System.out.println(compiledSchedule.toString().chars().filter(Character::isWhitespace).count() + " bundles total");
		System.out.println(compiledSchedule.toString().chars().filter(Character::isLowerCase).count() + " nops total");
		
		
//		String[] bst = "MMI; MMI MIB; MMB; ML; MI;I; M;MI; ML MIB MI;I MII; M;MI MII MMB MBB; MBB BBB; BBB MFI; MFI MMF MMF; MFB; MFB".split(" ");
		
		
		
		String[] bst = "BL B;M;M BM;F I;II; M;L FIM F;BF FM;F; BM;F; F;B;F; B;F;B BI;I M;MM B;BI MM;M M;I;I IMM I;I;F II;I I;FM; M;IM BMM; IM;I; IM;I".split(" ");
		
		Bundle[] bs = new Bundle[bst.length];
		for(int i = 0; i < bs.length; i++) {
			bs[i] = new Bundle(bst[i]);
		}
		
//		System.out.println(parsed.stream().map(BestBundles::toStr).collect(Collectors.joining(" ")));
//		PriorityQueue<Schedule> pq = new PriorityQueue<>();
		ArrayList<Schedule> finishedSchedules = new ArrayList<>();
		ArrayList<Schedule> schedules = new ArrayList<>();
		
		schedules.add(new Schedule(new Bundle[0], parsed.get(0), 1, 0));
		
		int iterations;
		for(iterations = 0; iterations < 1000000; iterations++) {
			
			
			if(schedules.size() == 0)
				break;
			
			int maxLen = 0;
			for(Schedule s : schedules)
				if(s.schedule.length > maxLen)
					maxLen = s.schedule.length;
			final int fml = maxLen;
			schedules.removeIf(s -> s.schedule.length < fml - 3);
			
			Collections.sort(schedules);
			Schedule s = schedules.remove(0);
			
			if(!finishedSchedules.isEmpty() && s.nops > finishedSchedules.get(0).nops) {
				break;
			}
//			System.out.println(s);
//			System.out.println(fml);
//			System.out.println(maxLen + "\t" + s.nops + "\t" + s);
			
			nextBundle: for(Bundle b : bs) {
//				System.out.println(b.text);
				int[] currentHead = s.nextHead;
				int nextNops = s.nops;
				int nextIndex = s.index;
				
				
				for(int l = 0; l < b.parsed.length; l++) {
//					System.out.println(b.text);
					int[] subBundle = b.parsed[l];
					int nops = 0;
					int ops = 0;
					int[] nextHead = new int[6];
					for(int j = 0; j < 6; j++)
						nextHead[j] = currentHead[j] - subBundle[j];
					
					if(nextHead[3] < 0) {nextHead[0] += nextHead[3]; nextHead[3] = 0;}
					if(nextHead[5] < 0) {nextHead[0] += nextHead[5]; nextHead[5] = 0;}
					
					if(nextHead[4] < 0)
						nops -= nextHead[4];
					for(int j = 0; j < 6; j++)
						if(nextHead[j] < 0) {
							nops -= nextHead[j];
							nextHead[j] = 0;
						} else {
							ops += nextHead[j];
						};
					nextNops += nops;
					
					if(b.ends[l]) {
						if(ops > 0)
							continue nextBundle;
						
						
						if(nextIndex == parsed.size()) {
							if(l == b.parsed.length-1) {
								Bundle[] nextSchedule = new Bundle[s.schedule.length + 1];
								System.arraycopy(s.schedule, 0, nextSchedule, 0, s.schedule.length);
								nextSchedule[s.schedule.length] = b;
								Schedule ns = new Schedule(nextSchedule, new int[6], nextIndex, nextNops);
								
								finishedSchedules.add(ns);
								Collections.sort(finishedSchedules);
								break nextBundle;
							} else {
								break nextBundle;
							}
							
						}
						currentHead = parsed.get(nextIndex);
						nextIndex++;
					} else {
						currentHead = nextHead;
					}
					
				}
				
				
				
				Bundle[] nextSchedule = new Bundle[s.schedule.length + 1];
				System.arraycopy(s.schedule, 0, nextSchedule, 0, s.schedule.length);
				nextSchedule[s.schedule.length] = b;
				Schedule ns = new Schedule(nextSchedule, currentHead, nextIndex, nextNops);
				
				int j;
				for(j = 0; j < schedules.size(); j++) {
					Schedule ss = schedules.get(j);
					if(Arrays.equals(ns.nextHead, ss.nextHead) && ns.index == ss.index) {
						if(ns.nops < ss.nops) {
							schedules.set(j, ns);
						}
						break;
					}
				}
				if(j >= schedules.size()) {
					schedules.add(ns);
				}
				
			}
		}
		if(finishedSchedules.isEmpty())
			return;
		Schedule s = finishedSchedules.get(0);
		System.out.println(s.nops + "\t" + iterations);
		System.out.println(s);
//		for(Schedule s : finishedSchedules) {
//			System.out.println(s.nops + "\t" + iterations + "\t" + s);
//		}
	}
	
	
	
	
	
	public static void main(String[] args) {
		try {
			new BestBundles();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
