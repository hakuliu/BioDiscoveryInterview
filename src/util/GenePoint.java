package util;


public class GenePoint {
	int chromosomeid;
	//I wasn't sure how big these numbers would get...
	long start;
	long end;
	//(assumption) looked like double was enough precision?
	double value;
	
	int array;
	
	public GenePoint() {
		//nothing for now
	}
	
	public GenePoint(int chr, long start, long end, double val, int array) {
		this.chromosomeid = chr;
		this.start = start;
		this.end = end;
		this.value = val;
		this.array = array;
	}
	
	public int getChromID() {
		return chromosomeid;
	}
	
	public long getStart() {
		return start;
	}
	
	public long getEnd() {
		return end;
	}
	
	public double getVal() {
		return value;
	}
	
	public int getArr() {
		return this.array;
	}
	
	public static GenePoint parseFromLine(String line) {
		try {
			String[] split = line.split("\t");
			int chrom = ChromosomeSequence.chromosomeStrToInt(split[0]);
			long start = Long.parseLong(split[1]);
			long end = Long.parseLong(split[2]);
			float val = Float.parseFloat(split[3]);
			int array = Integer.parseInt(split[4]);
			
			GenePoint candidate = new GenePoint();
			candidate.chromosomeid = chrom;
			candidate.start = start;
			candidate.end = end;
			candidate.value = val;
			candidate.array = array;
			return candidate;
		} catch (Exception e) {
			//if any exception happened (outofbounds or parsing) then that means there was something wrong with the line.
			//for the time being, that just means we throw it out and return null (depends on how the org wants to handle it tho)
			//for example this might mean corrupt data and we need to do something about it....
			return null;
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		String chr = ChromosomeSequence.chromosomeIntToStr(chromosomeid);
		builder.append(chr);
		builder.append('\t');
		builder.append(start);
		builder.append('-');
		builder.append(end);
		builder.append('\t');
		builder.append(value);
		return builder.toString();
	}
	@Override
	public boolean equals(Object obj) {
		GenePoint pt = null;
		try {
			pt = (GenePoint)obj;
		} catch (Exception e) {
			return false;
		}
		if(pt != null) {
			return pt.chromosomeid == this.chromosomeid &&
					pt.start == this.start && 
					pt.end == this.end &&
					pt.value == this.value &&
					pt.array == this.array;
		} else {
			return false;
		}
	}
	@Override
	public int hashCode() {
		//I think for the purpose of this study we can just use the start point...
		//this is sufficient in differenciating from one another...we may have a few collisions in hash but that's ok.
		//we have the .equals function to resolve that.
		return (int)this.start;
	}
}
