package tests;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import datahandeling.util.AbstractGeneFileReader;
import datahandeling.util.HumanReadableGeneFileReader;

import static org.junit.Assert.*;

import query.GeneQuery;
import query.VanillaQuery;
import util.ChromosomeSequence;
import util.CorruptGeneReadException;
import util.GenePoint;
import util.GeneRange;
import util.GeneRange.GeneRangeException;
import util.GenomeSearchTree;
import util.GenomeSearchTree.GenomeTreeLoadException;
/**
 * creates test cases which queries the original data (the slow way O(n)) n = number of entries total
 * and also runs the query in our new way (O(logm + n)) m = number of files, n = number of results
 * we print out the total run time for each query for debug purposes,
 * and verify that both are the same to confirm our new data structure is accurate.
 * @author Paul
 *
 */
public class TestInMemoryAccuracy {
	private static File vanillalocation = new File("testdata/original/probes.txt");
	private static File reorglocation = new File("testdata/organized");
//	I had a utility digester that took the entire data file given and separated them into separate files.
//	This test case simply does the digestion, and walks through everything to make sure every gene point is accounted for.
	@Test
	public void testSingleChromosome1() {
		GeneRange range = new GeneRange(2, 63510981, 2, 63530199);
		assert(realtest(range));
	}
	
	@Test
	public void testEngulfNode() {
		GeneRange range = new GeneRange(5, 0, 5, 117188696);
		assert(realtest(range));
	}
	
	@Test
	public void testMultChromosomes() {
		GeneRange range = new GeneRange(19, 36341339, 22, 24936262);
		assert(realtest(range));
	}
	
	@Test
	public void testSinglePointQuery() {
		GeneRange range = new GeneRange(10, 37972971, 10, 37972971);
		assert(realtest(range));
	}
	
	@Test
	public void testSinglePointResult() {
		GeneRange range = new GeneRange(10, 38009280, 10, 38009285);
		assert(realtest(range));
	}
	
	@Test
	public void testSinglePointQueryMultipleResults() {
		GeneRange range = new GeneRange(10, 38029525, 10, 38029525);
		assert(realtest(range));
	}
	
	/**
	 * I went thru the files and manually picked out a few cases. Not as robust,
	 * more reliable than using 'vanilla search' as source of truth.
	 */
	@Test
	public void testManual1() {
		GeneRange range = new GeneRange(10, 38029525, 10, 38029525);
		
		List<GenePoint> expected = new ArrayList<>();
		expected.add(new GenePoint(10, 38029503, 38029528, 0.3598266839981079, 0));
		expected.add(new GenePoint(10, 38029520, 38029545, 0.4669439494609833, 0));
		expected.add(new GenePoint(10, 38029521, 38029545, 0.7733854055404663, 0));
		
		boolean result = manual(range, expected);
		assert(result);
	}
	
	@Test
	public void testManual2() {
		GeneRange range = new GeneRange(20, 26161518, 20, 26161518);
		
		List<GenePoint> expected = new ArrayList<>();
		expected.add(new GenePoint(20, 26161493, 26161518, -0.17485979199409485, 0));
		
		boolean result = manual(range, expected);
		assert(result);
	}
	
	@Test
	public void testManual3() {
		GeneRange range = new GeneRange(20, 26161517, 20, 26161520);
		
		List<GenePoint> expected = new ArrayList<>();
		expected.add(new GenePoint(20, 26161493, 26161518, -0.17485979199409485, 0));
		
		boolean result = manual(range, expected);
		assert(result);
	}
	
	@Test
	public void testManual4() {
		GeneRange range = new GeneRange(ChromosomeSequence.Y, 7688992, ChromosomeSequence.Y, 7689407);
		
		List<GenePoint> expected = new ArrayList<>();
		expected.add(new GenePoint(ChromosomeSequence.Y, 7688968, 7688993, -1.3593016862869263, 0));
		expected.add(new GenePoint(ChromosomeSequence.Y, 7689406, 7689431, -1.1860036849975586, 0));
		
		boolean result = manual(range, expected);
		assert(result);
	}
	
	@Test
	public void testManual5() {
		GeneRange range = new GeneRange(14, 107285058, 15, 20016340);
		
		List<GenePoint> expected = new ArrayList<>();
		expected.add(new GenePoint(14, 107285033, 107285058, -0.030590958893299103, 0));
		expected.add(new GenePoint(14, 107285437, 107285437, -0.3149268329143524, 0));
		expected.add(new GenePoint(15, 20016315, 20016340, -0.26238173246383667, 0));
		
		boolean result = manual(range, expected);
		assert(result);
	}
	
	/**
	 * better hope you got good memory lol.
	 * (we're testing with 120mb of data here, not gigs, so it's okay...just don't run it with *real* data... 
	 */
	@Test
	public void testEVERYTHING() {
		GeneRange range;
		try {
			range = GeneRange.parseQueryTextToRange("chr1-chrY");
			assert(realtest(range));
		} catch (GeneRangeException e) {
			fail(e.getMessage());
		}
		
	}
	
	/**
	 * verify this time with straight read rather than search
	 */
	@Test
	public void testEVERYTHING2() {
		GeneRange range;
		try {
			range = GeneRange.parseQueryTextToRange("chr1-chrY");
			
			long starttime = System.currentTimeMillis();
			System.out.println("begin straight read...");
			
			List<GenePoint> result1 = new ArrayList<>();
			AbstractGeneFileReader reader = new HumanReadableGeneFileReader(vanillalocation);
			reader.open();
			GenePoint chk = null;
			while((chk = reader.readLine()) != null) {
				result1.add(chk);
			}
			
			long qtime = System.currentTimeMillis() - starttime;
			System.out.println("result had " + result1.size() + " lines");
			System.out.println("query took " + qtime + "ms.");
			
			System.out.println("begin tree search...");
			GenomeSearchTree searchtree = new GenomeSearchTree(reorglocation);
			try {
				searchtree.load();
			} catch (GenomeTreeLoadException | IOException e) {
				fail(e.getMessage());
			}
			starttime = System.currentTimeMillis();
			GeneQuery gq = new GeneQuery(searchtree, range);
			List<GenePoint> result2 = gq.executeInMemory();
			qtime = System.currentTimeMillis() - starttime;
			//System.out.println(result2);
			System.out.println("result had " + result2.size() + " lines");
			System.out.println("query took " + qtime + "ms.");
			
			System.out.println("begin results validation...");
			
			boolean same = ResultValidator.validate(result1, result2);
			
			System.out.println("validation complete. results same? " + same);
			
			assert(same);
			
		} catch (GeneRangeException | IOException | CorruptGeneReadException e) {
			fail(e.getMessage());
		}
		
	}
	
	private boolean manual(GeneRange range, List<GenePoint> expected) {
		GenomeSearchTree searchtree = new GenomeSearchTree(reorglocation);
		try {
			searchtree.load();
		} catch (GenomeTreeLoadException | IOException e) {
			fail(e.getMessage());
		}
		
		long starttime = System.currentTimeMillis();
		GeneQuery gq = new GeneQuery(searchtree, range);
		List<GenePoint> result2 = gq.executeInMemory();
		long qtime = System.currentTimeMillis() - starttime;
		System.out.println("result had " + result2.size() + " lines");
		System.out.println("query took " + qtime + "ms.");
		
		System.out.println("begin results validation...");
		
		boolean same = ResultValidator.validate(expected, result2);
		
		System.out.println("validation complete. results same? " + same);
		return same;
	}
	
	private boolean realtest(GeneRange range) {
		System.out.println("Running test case with " + range);
		
		System.out.println("begin vanila search...");
		long starttime = System.currentTimeMillis();
		VanillaQuery vq = new VanillaQuery(vanillalocation, range);
		List<GenePoint> result1 = vq.execute();
		long qtime = System.currentTimeMillis() - starttime;
		System.out.println("result had " + result1.size() + " lines");
		System.out.println("query took " + qtime + "ms.");
		
		System.out.println("begin tree search...");
		GenomeSearchTree searchtree = new GenomeSearchTree(reorglocation);
		try {
			searchtree.load();
		} catch (GenomeTreeLoadException | IOException e) {
			fail(e.getMessage());
		}
		starttime = System.currentTimeMillis();
		GeneQuery gq = new GeneQuery(searchtree, range);
		List<GenePoint> result2 = gq.executeInMemory();
		qtime = System.currentTimeMillis() - starttime;
		System.out.println("result had " + result2.size() + " lines");
		System.out.println("query took " + qtime + "ms.");
		
		System.out.println("begin results validation...");
		
		boolean same = ResultValidator.validate(result1, result2);
		
		System.out.println("validation complete. results same? " + same);
		return same;
	}
}
