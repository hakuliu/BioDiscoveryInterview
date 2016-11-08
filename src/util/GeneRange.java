package util;

import java.util.ArrayList;
import java.util.List;

public class GeneRange {
	private int startChromosome;
	private long startPoint;
	private int endChromosome;
	private long endPoint;
	private List<GeneRange> trurange = null;
	public GeneRange(int startChr, long start, int endChr, long end) {
		this.startChromosome = startChr;
		this.startPoint = start;
		this.endChromosome = endChr;
		this.endPoint = end;
		this.trurange = GeneRange.toIndividualChromosomes(this);
	}
	public GeneRange(int chr, long start, long end) {
		this(chr, start, chr, end);
	}
	
	public boolean isSingleChromosome() {
		return this.startChromosome == this.endChromosome;
	}
	
	public int getChr() {
		return this.startChromosome;
	}
	
	public long getStartPoint() {
		return this.startPoint;
	}
	
	public long getEndPoint() {
		return this.endPoint;
	}
	
	public List<GeneRange> getTruRange() {
		return this.trurange;
	}
	
	public boolean isInIntersecting(GenePoint g) {
		boolean rv = false;
		
		for(GeneRange single : this.trurange) {
			rv |= isIntersectingSingle(g, single);
		}
		
		return rv;
	}
	
	private static boolean isIntersectingSingle(GenePoint g, GeneRange singlechr) {
		if(g.chromosomeid != singlechr.startChromosome) {
			return false;
		}
		if(g.getStart() < singlechr.getStartPoint() && g.getEnd() < singlechr.getStartPoint()) {
			return false;
		} else if(g.getStart() > singlechr.getEndPoint() && g.getEnd() > singlechr.getEndPoint()) {
			return false;
		}
		return true;
	}
	
	public static List<GeneRange> toIndividualChromosomes(GeneRange r) {
		List<GeneRange> rv = new ArrayList<>();
		
		if(r.isSingleChromosome()) {
			rv.add(r);
		} else {
			int currentchr = r.startChromosome;
			while(currentchr > 0 && currentchr <= r.endChromosome){
				long startpoint = Long.MIN_VALUE;
				long endpoint = Long.MAX_VALUE;
				if(currentchr == r.startChromosome) {
					startpoint = r.startPoint;
				}
				if(currentchr == r.endChromosome) {
					endpoint = r.endPoint;
				}
				rv.add(new GeneRange(currentchr, startpoint, endpoint));
				currentchr = ChromosomeSequence.getNext(currentchr);
			}
		}
		
		return rv;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(ChromosomeSequence.chromosomeIntToStr(startChromosome));
		builder.append(':');
		builder.append(this.startPoint);
		builder.append('-');
		builder.append(ChromosomeSequence.chromosomeIntToStr(endChromosome));
		builder.append(':');
		builder.append(this.endPoint);
		return builder.toString();
	}
	
	public static GeneRange parseQueryTextToRange(String qtext) throws GeneRangeException {
		if(qtext == null || qtext.isEmpty()) {
			throw new GeneRangeException("no query text");
		}
		//remove all white spaces
		qtext = qtext.replaceAll("\\s", "");
		String[] sp = qtext.split("-");
		if(sp.length > 2) {
			throw new GeneRangeException("too many \'-\'.");
		}
		//parse first point
		String[] pt = sp[0].split(":");
		if(sp.length > 2) {
			throw new GeneRangeException("too many \':\'.");
		}
		int startchr = getChrFromPtSplit(pt[0]);
		long startpoint = Long.MIN_VALUE;
		if(pt.length == 2) {
			startpoint = Long.parseLong(pt[1]);//if exception happen it'll just get thrown and get caught by ui
		}
		int endchr = startchr;
		long endpoint = Long.MAX_VALUE;
		if(sp.length == 2) {
			pt = sp[1].split(":");
			try {
				endchr = getChrFromPtSplit(pt[0]);
				if(pt.length == 2) {
					endpoint = Long.parseLong(pt[1]);
				}
			} catch (Exception e) {
				//in this case it's possible it's same chromosome, just number
				endpoint = Long.parseLong(pt[0]);
			}
		}
		return new GeneRange(startchr, startpoint, endchr, endpoint);
	}
	@Override
	public boolean equals(Object obj) {
		try {
			GeneRange r = (GeneRange)obj;
			return r.startChromosome == this.startChromosome &&
					r.endChromosome == this.endChromosome &&
					r.startPoint == this.startPoint &&
					r.endPoint == this.endPoint;
		} catch (Exception e) {
			return false;
		}
	}
	private static int getChrFromPtSplit(String ptchr) throws GeneRangeException {
		try {
			int rv = ChromosomeSequence.chromosomeStrToInt(ptchr);
			return rv;
		} catch (CorruptGeneReadException e) {
			throw new GeneRangeException(e.getMessage());
		}
	}
	
	public static class GeneRangeException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = -4444537906783571184L;
		public GeneRangeException(String msg) {
			super(msg);
		}
	}
}
