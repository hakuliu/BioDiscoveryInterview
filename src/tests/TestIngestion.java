package tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

import datahandeling.consumeconstruct.DataIngestor;
import datahandeling.consumeconstruct.IngestionException;
import util.CorruptGeneReadException;
import util.GenomeSearchTree;
import util.GenomeSearchTree.GenomeTreeLoadException;

public class TestIngestion {
	private static File inputfile = new File("testdata/ingestdata/smallcase.txt");
	private static File brokefile1 = new File("testdata/ingestdata/broken1.txt");
	
	private static File outputdir = new File("testdata/ingestdata/testresult");
	@Test
	public void testCorrupt() {
		boolean failed = false;
		DataIngestor ingestor = new DataIngestor(brokefile1, outputdir);
		try {
			ingestor.executeIngest();
		} catch (IngestionException | IOException | CorruptGeneReadException e) {
			failed = true;
		}
		assert(failed);
	}
	
	@Test
	public void testSmallTree() {
		DataIngestor ingestor = new DataIngestor(inputfile, outputdir);
		try {
			ingestor.executeIngest();
		} catch (IngestionException | IOException | CorruptGeneReadException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		File[] chrs = outputdir.listFiles();
		assertEquals(chrs.length, 5);
		for(File chr : chrs) {
			String[] names = chr.list();
			boolean found = false;
			for(String s : names) {
				if("graph.txt".equals(s)) found = true;
			}
			assert(found);
		}
		//load the tree, if there's exceptions or returns null, then we have a problem.
		GenomeSearchTree tree = new GenomeSearchTree(outputdir);
		try {
			tree.load();
		} catch (GenomeTreeLoadException | IOException e) {
			fail("genome load error");
		}
	}
	@AfterClass
	public static void cleanup() {
		deletefile(outputdir);
	}
	private static void deletefile(File f) {
		if(f.exists()) {
			File[] files =f.listFiles();
			if(files != null) {
				for(File ff : files) {
					if(ff.isDirectory()) deletefile(ff);
					else ff.delete();
				}
			}
			f.delete();
		}
	}
}
