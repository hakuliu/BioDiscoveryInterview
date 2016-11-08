package datahandeling.searching;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import datahandeling.util.AbstractGeneFileReader;
import datahandeling.util.AbstractGeneFileWriter;
import datahandeling.util.HumanReadableGeneFileReader;
import util.CorruptGeneReadException;
import util.GenePoint;
import util.GeneRange;

/**
 * given a file, iteratively search thru all entries to 
 * @author Paul
 *
 */
public class IterativeFileSearcher {
	private File tosearch;
	public IterativeFileSearcher(File input) {
		this.tosearch = input;
	}
	/**
	 * search thru entire file and shoves the results into a list in-memory
	 * used for debug and testing...
	 * for output that are exceptionally large (and for the demo,) we use search with a writer.
	 * @return
	 */
	public List<GenePoint> search(GeneRange range) {
		ArrayList<GenePoint> rv = new ArrayList<>();
		AbstractGeneFileReader reader = new HumanReadableGeneFileReader(this.tosearch);
		try {
			reader.open();
		} catch (FileNotFoundException e) {
			//at this point the rv is empty so we say something to the user about how we couldn't find the file just return that.
			System.out.println("Could not find file to search: " + this.tosearch.getPath());
			return rv;
		}
		
		try {
			GenePoint pt = null;
			while((pt = reader.readLine()) != null) {
				if(isGeneInRange(pt, range)) {
					rv.add(pt);
				}
			}
		} catch (IOException | CorruptGeneReadException e) {
			//again, we just return whatever is in rv, and notify the user that something wrong happened.
			e.printStackTrace();
		} finally {
			reader.closeReader();
		}
		return rv;
	}
	
	public boolean search(GeneRange range, AbstractGeneFileWriter writer) throws IOException, CorruptGeneReadException {
		AbstractGeneFileReader reader = new HumanReadableGeneFileReader(this.tosearch);
		try {
			reader.open();
			GenePoint pt = null;
			while((pt = reader.readLine()) != null) {
				if(isGeneInRange(pt, range)) {
					writer.writeGene(pt);;
				}
			}
		} finally {
			reader.closeReader();
		}
		return true;
	}
	
	private boolean isGeneInRange(GenePoint g, GeneRange searchRange) {
		return searchRange.isInIntersecting(g); 
	}
}
