package query;

import java.io.File;
import java.util.List;

import datahandeling.searching.IterativeFileSearcher;
import util.GenePoint;
import util.GeneRange;

public class VanillaQuery {
	private File dataloc;
	GeneRange range;
	public VanillaQuery(File dataloc, GeneRange range) {
		this.dataloc = dataloc;
		this.range = range;
	}
	
	public List<GenePoint> execute() {
		IterativeFileSearcher searcher = new IterativeFileSearcher(dataloc);
		List<GenePoint> result1 = searcher.search(range);
		return result1; 
	}
}
