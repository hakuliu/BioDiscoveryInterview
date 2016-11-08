package datahandeling.consumeconstruct;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import datahandeling.util.AbstractGeneFileReader;
import datahandeling.util.HumanReadableGeneFileReader;
import util.ChromosomeSequence;
import util.CorruptGeneReadException;
import util.GenePoint;
import util.IntervalTreeNode;

public class DataIngestor {
	private File datafile;
	private File outdir;
	private HashMap<Integer,ChromosomeIngestionStat> stats = new HashMap<>();
	private HashMap<Integer,ChromosomeSplitter> splitters = new HashMap<>();
	private static int PREFERREDENTRIESPERSPLIT = 10000;
	public DataIngestor(File in, File out) {
		this.datafile = in;
		this.outdir = out;
	}
	
	public void executeIngest() throws IngestionException, IOException, CorruptGeneReadException {
			this.getStats();
			this.prepSeparation(outdir, PREFERREDENTRIESPERSPLIT);
			this.doSplit();
			this.constructSaveTrees();
	}
	
	/**
	 * This step tries to make the tree constructed later to be balanced
	 * pass 1 is to figure out some stats on each chromosome such as:
	 * *how many there are
	 * *min and max points of each chromosome
	 * I do this because I'm working under the assumption that the data I'm given might not be sorted, and random
	 * even tho the one I'm given for the project is for the most part sorted...
	 * There is also the assumption that the distribution of gene points are about 'uniform' which I don't exactly know...
	 * @param reader FRESH reader
	 * @throws CorruptGeneReadException 
	 * @throws IOException 
	 */
	private void getStats() throws IOException, CorruptGeneReadException {
		stats.clear();
		GenePoint line = null;
		AbstractGeneFileReader reader = new HumanReadableGeneFileReader(this.datafile);
		try {
			reader.open();
			while((line = reader.readLine()) != null) {
				int chrnum = line.getChromID();
				ChromosomeIngestionStat stat = stats.get(chrnum);
				if(stat == null) {
					stat = new ChromosomeIngestionStat(chrnum);
					stats.put(chrnum, stat);
				}
				stat.updateVal(line);
			}
		} finally {
			reader.closeReader();
		}
	}
	/**
	 * uses {@code this.stats} to create the chromosome directories and prepare it to begin writing the splits.
	 * @throws IngestionException 
	 */
	private void prepSeparation(File outputdirectory, int targetbalancefactor) throws IngestionException {
		if(this.stats == null) {
			throw new IngestionException("preparation requires first pass to finish");
		}
		//we must first create all the chromosome directories
		if(!outputdirectory.exists()) {
			outputdirectory.mkdirs();
		}
		splitters.clear();
		
		for(int chrnum : this.stats.keySet()) {
			File prepdir = new File(outputdirectory.getAbsolutePath()+"/"+ChromosomeSequence.chromosomeIntToStr(chrnum));
			prepdir.mkdir();
			ChromosomeIngestionStat stat = this.stats.get(chrnum);
			ChromosomeSplitter sp = new ChromosomeSplitter(stat, prepdir);
			sp.partitionFiles(targetbalancefactor);
			splitters.put(chrnum, sp);
		}
	}
	
	private void doSplit() throws IOException, CorruptGeneReadException, IngestionException {
		GenePoint line = null;
		AbstractGeneFileReader reader = new HumanReadableGeneFileReader(this.datafile);
		try {
			for(ChromosomeSplitter splitter : splitters.values()) {
				splitter.open();
			}
			reader.open();
			while((line = reader.readLine()) != null) {
				int chrnum = line.getChromID();
				ChromosomeSplitter splitter = splitters.get(chrnum);
				splitter.writeLine(line);
			}
		} finally {
			reader.closeReader();
			for(ChromosomeSplitter splitter : splitters.values()) {
				splitter.close();
			}
		}
	}
	
	private void constructSaveTrees() throws IOException, IngestionException {
		for(ChromosomeSplitter splitter : this.splitters.values()) {
			List<IntervalTreeNode> nodes = splitter.getTreeNodes();
			IntervalTreeConstructor constructor = new IntervalTreeConstructor(nodes);
			constructor.constructTree();
			constructor.writeToFile(splitter.getDirectory());
		}
	}
}
