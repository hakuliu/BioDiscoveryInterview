package util;

import java.io.File;

public class IntervalTreeNode implements Comparable<IntervalTreeNode>{
	private int chrId;
	private long start;
	private long end;
	private long minChild;
	private long maxChild;
	private File associatedfile;
	
	private IntervalTreeNode left = null;
	private IntervalTreeNode right = null;
	
	
	public IntervalTreeNode(int chr, File file) {
		//start and end to be updated thru reads
		this(chr, Long.MAX_VALUE, Long.MIN_VALUE, file);
	}
	public IntervalTreeNode(int chr, long start, long end, File file) {
		this.start = start;
		this.end = end;
		this.associatedfile = file;
		this.maxChild = end;
		this.minChild = start;
		this.chrId = chr;
	}
	public IntervalTreeNode(int chr, long start, long end, File file, long min, long max) {
		this(chr, start, end, file);
		this.minChild = min;
		this.maxChild = max;
	}

	//need to be able to compare to sort / make BST 
	@Override
	public int compareTo(IntervalTreeNode o) {
		if(this.chrId == o.chrId) {
			//straight minus gives long and we have chance of overflow...
			//we only need -1, 0, and 1 anyway.
			long diff = this.start - o.start;
			if(diff < 0) {
				return -1;
			} else if (diff > 0) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return this.chrId - o.chrId;
		}
	}
	
	public long getStart() {
		return start;
	}
	
	public long getEnd() {
		return end;
	}
	
	public long getMin() {
		return minChild;
	}
	
	public long getMax() {
		return maxChild;
	}
	
	public void setMin(long min) {
		this.minChild = min;
	}
	public void setMax(long max) {
		this.maxChild = max;
	}
	
	public File getFile() {
		return this.associatedfile;
	}
	
	public IntervalTreeNode getLeft() {
		return this.left;
	}
	public IntervalTreeNode getRight() {
		return this.right;
	}
	public void setLeft(IntervalTreeNode l) {
		this.left = l;
	}
	public void setRight(IntervalTreeNode r) {
		this.right = r;
	}
	/**
	 * for the purpose of debug, right now i'm printing out the start side only
	 * (because that's what the sort and BST is constructed on)
	 */
	public String toString() {
		return "IntervalNode:"+this.start;
	}
	
	public String getSerializeString() {
		StringBuilder builder =  new StringBuilder();
		builder.append(this.associatedfile.getPath());
		builder.append('\t');
		builder.append(this.start);
		builder.append('\t');
		builder.append(this.end);
		builder.append('\t');
		builder.append(this.minChild);
		builder.append('\t');
		builder.append(this.maxChild);
		return builder.toString();
	}
	
	public void updateStart(long m) {
		if(this.start > m) {
			this.start = m;
		}
		if(this.minChild > this.start) {
			this.minChild = this.start;
		}
	}
	public void updateEnd(long m) {
		if(this.end < m) {
			this.end = m;
		}
		if(this.maxChild < this.end) {
			this.maxChild = this.end;
		}
	}
}
