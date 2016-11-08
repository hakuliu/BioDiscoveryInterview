package datahandeling.consumeconstruct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;

import util.CorruptGeneReadException;
import util.IntervalTreeNode;

/**
 * @author Paul
 * handles the building, writing, and loading of the interval tree. The result of said operations should be passed onto {@link IntervalTree}
 */
public class IntervalTreeConstructor {
	private List<IntervalTreeNode> listonodes=null;
	private IntervalTreeNode root = null;
	public IntervalTreeConstructor() {
		
	}
	
	public IntervalTreeConstructor(List<IntervalTreeNode> list) {
		this.listonodes = list;
		//I believe java uses quicksort for primitives, mergesort for objects from this method...
		Collections.sort(this.listonodes);
	}
	
	/**
	 * construct the tree from the list this object was constructed with
	 * @return root of the tree, null if unsuccessful.
	 */
	public IntervalTreeNode constructTree() {
		IntervalTreeNode rv = recursiveTreeConstructor(this.listonodes, 0, this.listonodes.size() - 1);
		this.root = rv;
		return rv;
	}
	/**
	 * I had mentioned in the interview that I prefer iterative over recursive...trees are an exception i guess.
	 * This will create stacks of the end tree's height, so Log(n) where n is number of nodes.
	 * @param list
	 * @param left
	 * @param right
	 * @return root of the subtree null if terminated
	 */
	private IntervalTreeNode recursiveTreeConstructor(List<IntervalTreeNode> list, int left, int right) {
		if(left > right) {
			return null;
		}
		
		int mid = (left+right) / 2;
		IntervalTreeNode n = list.get(mid);
		
		IntervalTreeNode l = recursiveTreeConstructor(list, left, mid - 1);
		IntervalTreeNode r = recursiveTreeConstructor(list, mid + 1, right);
		
		n.setLeft(l);
		n.setRight(r);
		
		if(l != null) {
			n.setMin(l.getMin());
		}
		if(r != null) {
			n.setMax(r.getMax());
		}
		
		return n;
	}
	
	/**
	 * stores the tree constructed earlier from this object into file in pre-order
	 * since it's a BST, just pre-order will be sufficient to reconstruct the same tree from file later.
	 * @param directory file you want to save at
	 * @throws IOException
	 * @throws IngestionException 
	 */
	public void writeToFile(File directory) throws IOException, IngestionException {
		if(this.root == null) {
			throw new IngestionException("Trying to save a tree when there is no tree constructed yet.");
		}
		if(!directory.exists()) {
			directory.mkdirs();
		}
		
		File f = new File(directory.getAbsolutePath() + "/graph.txt");
		
		FileOutputStream fop = new FileOutputStream(f);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(fop));
			
			preorderwrite(root, bw);
		} finally {
			bw.close();
		}
		
	}
	
	private void preorderwrite(IntervalTreeNode node, BufferedWriter writer) throws IOException{
		if(node == null) {
			return;
		}
		//write the node first,
		String line = node.getSerializeString();
		writer.write(line);
		writer.newLine();
		
		//then traverse left and right
		preorderwrite(node.getLeft(), writer);
		preorderwrite(node.getRight(), writer);
	}
	
	public IntervalTreeNode loadFromFile(int chrnum, File f) throws IOException, CorruptGeneReadException {
		InputStream file = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(file);
		BufferedReader br = new BufferedReader(isr);
		try {
			String line = null;
			while((line = br.readLine()) != null) {
				String[] split = line.split("\t");
				if(split.length != 5) {
					throw new CorruptGeneReadException("Unrecognizable chromosome tree file format");
				}
				
				File nodefile = new File(split[0]);
				if(!nodefile.exists()) {
					throw new CorruptGeneReadException("Expecting data in location " + nodefile.getPath() + " but none found");
				}
				try {
					long start = Long.parseLong(split[1]);
					long end = Long.parseLong(split[2]);
					long min = Long.parseLong(split[3]);
					long max = Long.parseLong(split[4]);
					IntervalTreeNode node = new IntervalTreeNode(chrnum, start, end, nodefile, min, max);
					this.addNode(node);
					
				} catch (NumberFormatException e) {
					throw new CorruptGeneReadException("Unrecognizable tree file format: parsing numbers");
				}
				
			}
		} finally {
			br.close();
		}
		
		
		if(!verifyTree()) {
			throw new CorruptGeneReadException("loaded data does not contain a properly formed interval tree");
		}
		
		return this.root;
	}
	
	private boolean verifyTree() {
		return true;
	}
	private void addNode(IntervalTreeNode node) {
		if(this.root == null) {
			this.root = node;
		} else {
			//traverse thru to find place to put
			IntervalTreeNode current = this.root;
			while(current != null) {
				//left or right
				if(node.compareTo(current) < 0) {
					//go left
					if(current.getLeft() == null) {
						//add in and we're done
						current.setLeft(node);
						break;
					} else {
						current = current.getLeft();
					}
				} else {
					//go right
					if(current.getRight() == null) {
						current.setRight(node);
						break;
					} else {
						current = current.getRight();
					}
				}
			}
		}
	}
}
