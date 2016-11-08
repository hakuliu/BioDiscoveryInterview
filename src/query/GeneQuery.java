package query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import datahandeling.searching.NodeSearchUnit;
import datahandeling.util.HumanReadableGeneFileWriter;
import util.CorruptGeneReadException;
import util.GenePoint;
import util.GeneRange;
import util.GenomeSearchTree;

public class GeneQuery {
	private GeneRange range;
	private GenomeSearchTree tree;//temporary, should be a full 'db'
	
	public GeneQuery(GenomeSearchTree tree, int startChr, long start, int endChr, long end) {
		this(tree, new GeneRange(startChr, start, endChr, end));
	}
	public GeneQuery(GenomeSearchTree tree, int chr, long start, long end) {
		this(tree, new GeneRange(chr, start, end));
	}
	public GeneQuery(GenomeSearchTree tree, GeneRange range) {
		this.range = range;
		this.tree = tree;
	}
	
	public List<GenePoint> executeInMemory() {
		List<GenePoint> result = new ArrayList<>();
		List<NodeSearchUnit> treeresult = null;
		try {
			treeresult = tree.getSearchUnits(range);
		} catch (CorruptGeneReadException e) {
		}
		if(treeresult != null) {
			for(NodeSearchUnit ns : treeresult) {
				ns.executeSearch(result);
			}
		}
		return result;
	}
	
	public void executeInFile(File outfile) throws IOException, CorruptGeneReadException {
		HumanReadableGeneFileWriter writer = new HumanReadableGeneFileWriter(outfile, true);
		try {
			writer.open();
			List<NodeSearchUnit> treeresult = tree.getSearchUnits(range);
			for(NodeSearchUnit ns : treeresult) {
				ns.executeSearch(writer);
			}
		} finally {
			writer.close();
		}
	}
}