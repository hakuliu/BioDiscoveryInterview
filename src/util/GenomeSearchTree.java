package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import datahandeling.consumeconstruct.IntervalTreeConstructor;
import datahandeling.searching.NodeSearchUnit;

public class GenomeSearchTree {
	File fileloc;
	List<ChromosomeIntervalTree> chrtrees = null;
	/**
	 * representation of the entire genomic search tree
	 * @param fileloc the directory where the organized data is located.
	 */
	public GenomeSearchTree(File fileloc) {
		this.fileloc = fileloc;
	}
	/**
	 * attempts to load the database from the specified location.
	 * 
	 * @return returs true if it's been loaded, false if an error happened. (when error happen, we will syso what happened. (in real app we'd log it))
	 * @throws IOException 
	 */
	public void load() throws GenomeTreeLoadException, IOException{
		if(!fileloc.exists() || !fileloc.isDirectory()) {
			throw new GenomeTreeLoadException(fileloc.getName() + " is not a directory.");
		}
		
		File[] chrfiles = fileloc.listFiles();
		
		if(chrfiles.length == 0) {
			throw new GenomeTreeLoadException("Empty Directory.");
		}
		
		ArrayList<ChromosomeIntervalTree> loadedtree = new ArrayList<>();
		
		for(File chrfile : chrfiles) {
			//what chromosome am i?
			int chrnum = 0;
			String chrname = chrfile.getName();
			try {
				chrnum = ChromosomeSequence.chromosomeStrToInt(chrname);
				String graphpath = chrfile.getPath()+"/graph.txt";
				IntervalTreeConstructor reload = new IntervalTreeConstructor();
				IntervalTreeNode newroot = reload.loadFromFile(chrnum, new File(graphpath));
				ChromosomeIntervalTree tree = new ChromosomeIntervalTree(newroot, chrnum);
				loadedtree.add(tree);
			} catch (CorruptGeneReadException e) {
				throw new GenomeTreeLoadException("Unexpected file organization for chromosome: " + chrname);
			}
		}
		this.chrtrees = loadedtree;
	}
	
	public List<NodeSearchUnit> getSearchUnits(GeneRange range) throws CorruptGeneReadException {
		
		List<NodeSearchUnit> rv = new ArrayList<>();
		
		//find the tree you need
		List<GeneRange> chrseparated = range.getTruRange();
		for(GeneRange chrrange : chrseparated) {
			int chr = chrrange.getChr();
			ChromosomeIntervalTree tree = getTreeForChromosome(chr);
			if(tree != null) {
				rv.addAll(tree.getFilesToSearch(chrrange));
			}
		}
		Collections.sort(rv);
		return rv;
	}
	
	private ChromosomeIntervalTree getTreeForChromosome(int chrnum) {
		//probably better to have a hash here or something so we don't ahve to search...
		for(ChromosomeIntervalTree t : this.chrtrees) {
			if(t.getChrNumber() == chrnum) {
				return t;
			}
		}
		return null;
	}
	
	public static class GenomeTreeLoadException extends Exception  {

		/**
		 * 
		 */
		private static final long serialVersionUID = -956065629575249146L;
		
		public GenomeTreeLoadException(String msg) {
			super(msg);
		}
	}
}
