package datahandeling.consumeconstruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import util.ChromosomeSequence;
import util.GenePoint;

/**
 * internal struct used to keep the relevant stat on a chromosome.
 * @author Paul
 *
 */
public class ChromosomeIngestionStat {
	private int chrid;
	private long minrange;
	private long maxrange;
	private HashMap<Integer, HistogramBucket> hist = new HashMap<>();
	public ChromosomeIngestionStat(int chrid) {
		this.chrid = chrid;
		this.minrange = Long.MAX_VALUE;
		this.maxrange = Long.MIN_VALUE;
	}
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(ChromosomeSequence.chromosomeIntToStr(chrid));
		b.append(':');
		b.append(minrange);
		b.append('-');
		b.append(maxrange);
		b.append("...");
		b.append(maxrange-minrange);
		
		return b.toString();
	}
	public long getMin() {
		return minrange;
	}
	public long getMax() {
		return maxrange;
	}
	public int getChromosomeID() {
		return chrid;
	}
	public void updateVal(GenePoint pt) {
		if(pt.getStart() < minrange) {
			minrange = pt.getStart();
		}
		if(pt.getEnd() > maxrange) {
			maxrange = pt.getEnd();
		}
		getHistogramBucket(pt.getStart()).count++;
	}
	public List<HistogramBucket> getSortedHistorgram() {
		List<HistogramBucket>rv = new ArrayList<>(this.hist.values());
		Collections.sort(rv);
		return rv;
	}
	
	private HistogramBucket getHistogramBucket(long start) {
		long bucketstart = HistogramBucket.getHistStart(start);
		HistogramBucket bucket = hist.get((int)bucketstart);
		if(bucket == null) {
			bucket = HistogramBucket.getBestFor(start);
			hist.put((int)bucketstart, bucket);
		}
		return bucket;
	}
	
	public void printHist() {
		for(HistogramBucket bucket : this.hist.values()) {
			System.out.println(bucket.start + "-" + bucket.end + ":" + bucket.count);
		}
	}
	
	public static class HistogramBucket  implements Comparable<HistogramBucket> {
		static long BUCKETSIZE = 500000;
		long start;
		long end;
		
		int count;
		public HistogramBucket (long start, long end) {
			this.start = start;
			this.end = end;
			this.count = 0;
		}
		static long getHistStart(long start) {
			long st = start / BUCKETSIZE;
			st *= BUCKETSIZE;
			return st;
		}
		static HistogramBucket getBestFor(long start) {
			long st = getHistStart(start);
			return new HistogramBucket(st, st + BUCKETSIZE - 1);
		}
		@Override
		public int compareTo(HistogramBucket o) {
			return (int)(this.start - o.start);
		}
		
	}
}