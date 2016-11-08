package datahandeling.searching;

import java.io.IOException;
import java.util.List;

import datahandeling.util.AbstractGeneFileReader;
import datahandeling.util.AbstractGeneFileWriter;
import datahandeling.util.HumanReadableGeneFileReader;
import util.CorruptGeneReadException;
import util.GenePoint;
import util.GeneRange;
import util.IntervalTreeNode;

public class NodeSearchUnit implements Comparable<NodeSearchUnit> {
	private NodeSearchType searchtype;
	private IntervalTreeNode node;
	private GeneRange searchrange;
	
	public NodeSearchUnit(IntervalTreeNode node, NodeSearchType type, GeneRange searchrange) {
		this.node = node;
		this.searchtype = type;
		this.searchrange = searchrange;
	}
	
	public String toString() {
		return "" + this.node.getFile().getPath() + ", " + this.searchtype;
	}
	
	public boolean executeSearch(List<GenePoint> out) {
		if(this.searchtype == NodeSearchType.SEARCHNODE) {
			return doSingleFileSearch(out);
		} else if(this.searchtype == NodeSearchType.ENGULFNODE) {
			return engulfNode(node, out);
		} else if(this.searchtype == NodeSearchType.ENGULFSUBTREE) {
			return engulfSubtree(node, out);
		}
		return false;
	}
	
	public void executeSearch(AbstractGeneFileWriter writer) throws IOException, CorruptGeneReadException {
		if(this.searchtype == NodeSearchType.SEARCHNODE) {
			doSingleFileSearch(writer);
		} else if(this.searchtype == NodeSearchType.ENGULFNODE) {
			engulfNode(node, writer);
		} else if(this.searchtype == NodeSearchType.ENGULFSUBTREE) {
			engulfSubtree(node, writer);
		}
	}
	
	private boolean doSingleFileSearch(List<GenePoint> out) {
		IterativeFileSearcher fileSearcher = new IterativeFileSearcher(node.getFile());
		List<GenePoint> result = fileSearcher.search(this.searchrange);
		out.addAll(result);
		return true;
	}
	
	private void doSingleFileSearch(AbstractGeneFileWriter writer) throws IOException, CorruptGeneReadException {
		IterativeFileSearcher fileSearcher = new IterativeFileSearcher(node.getFile());
		fileSearcher.search(this.searchrange, writer);
	}
	
	private boolean engulfSubtree(IntervalTreeNode n, List<GenePoint> out) {
		if(n == null) {
			return true;
		}
		boolean b = true;
		
		b &= engulfSubtree(n.getLeft(), out);
		b &= engulfNode(n, out);
		b &= engulfSubtree(n.getRight(), out);
		
		return b;
	}
	
	private void engulfSubtree(IntervalTreeNode n, AbstractGeneFileWriter out) throws IOException, CorruptGeneReadException {
		if(n == null) {
			return;
		}
		
		engulfSubtree(n.getLeft(), out);
		engulfNode(n, out);
		engulfSubtree(n.getRight(), out);
	}
	
	private void engulfNode(IntervalTreeNode n, AbstractGeneFileWriter out) throws IOException, CorruptGeneReadException {
		AbstractGeneFileReader reader = new HumanReadableGeneFileReader(n.getFile());
		reader.open();
		GenePoint pt = null;
		while((pt = reader.readLine()) != null) {
			out.writeGene(pt);
		}
	}
	
	/**
	 * reads the entire file and puts it in output without checking
	 * this should be slightly faster because branching takes a log of time in the processor...
	 * @param out
	 * @return
	 */
	private boolean engulfNode(IntervalTreeNode n, List<GenePoint> out) {
		AbstractGeneFileReader reader = new HumanReadableGeneFileReader(n.getFile());
		try {
			reader.open();
			GenePoint pt = null;
			while((pt = reader.readLine()) != null) {
				out.add(pt);
			}
		} catch (IOException | CorruptGeneReadException e) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(NodeSearchUnit o) {
		return this.node.compareTo(o.node);
	}
}
