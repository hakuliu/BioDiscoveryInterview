package datahandeling.consumeconstruct;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import datahandeling.util.AbstractGeneFileWriter;
import datahandeling.util.HumanReadableGeneFileWriter;
import util.GenePoint;
import util.GeneRange;
import util.IntervalTreeNode;

/**
 * This splitter is given statistics about a single chromosomes, and tries to figure out how many files to split each chromosome into
 * so that the ending tree would be somewhat balanced.
 * @author Paul
 *
 */
public class ChromosomeSplitter {
	private ChromosomeIngestionStat stat;
	private List<SplitWrapper> splits;
	private File chromosomedirectory;
	public ChromosomeSplitter(ChromosomeIngestionStat stat, File chromosomedirectory) {
		this.stat = stat;
		this.chromosomedirectory = chromosomedirectory;
		splits = new ArrayList<>();
	}
	/**
	 * for this method, we use the histogram built from the previous stage,
	 * and try to fit however many pairs into the particular file before we allocate a new file. 
	 * @param targetsplit amount of entries preferred in a split file.
	 */
	public void partitionFiles(int targetsplit) {
		List<ChromosomeIngestionStat.HistogramBucket> buckets = this.stat.getSortedHistorgram();
		
		int numInSplit = 0;
		long minsofar = buckets.get(0).start;
		int i = 0;
		for(ChromosomeIngestionStat.HistogramBucket bucket : buckets) {
			numInSplit += bucket.count;
			if(numInSplit >= targetsplit) {
				split(i, minsofar, bucket.end);
				minsofar = bucket.end+1;
				numInSplit = 0;
				i++;
			}
		}
		split(i, minsofar, buckets.get(buckets.size() - 1).end);
	}
	private void split(int i, long lo, long hi) {
		SplitWrapper split = new SplitWrapper();
		File outfile = new File(chromosomedirectory.getAbsolutePath() + "/genesplit" + i + ".txt");
		split.writer = new HumanReadableGeneFileWriter(outfile, false);
		split.range = new GeneRange(stat.getChromosomeID(), lo, hi);
		split.node = new IntervalTreeNode(stat.getChromosomeID(), outfile);
		splits.add(split);
	}
	
	public void writeLine(GenePoint line) throws IOException, IngestionException {
		for(int i = 0 ; i < splits.size() ; i++) {
			SplitWrapper split = splits.get(i);
			if(split.range.isInIntersecting(line)) {
				split.writer.writeGene(line);
				split.node.updateStart(line.getStart());
				split.node.updateEnd(line.getEnd());
				return;
			}
		}
		throw new IngestionException("found an unexpected gene point out of range");
	}
	public void open() throws IOException {
		for(SplitWrapper split : splits) {
			if(split.writer != null) {
				split.writer.open();
			}
		}
	}
	
	public void close() throws IOException {
		for(SplitWrapper split : splits) {
			if(split.writer != null) {
				split.writer.close();
			}
		}
	}
	
	public List<IntervalTreeNode> getTreeNodes() {
		List<IntervalTreeNode> rv = new ArrayList<>();
		for(SplitWrapper split : splits) {
			rv.add(split.node);
		}
		return rv;
	}
	
	private static class SplitWrapper {
		AbstractGeneFileWriter writer;
		GeneRange range;
		IntervalTreeNode node;
	}
	
	public File getDirectory() {
		return this.chromosomedirectory;
	}
}
