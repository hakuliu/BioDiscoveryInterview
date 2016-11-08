package util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import datahandeling.searching.NodeSearchType;
import datahandeling.searching.NodeSearchUnit;

/**
 * handles the representation of the tree for the purposes of a query
 * right now the prompt does not have any writing or adding to the data once it's been established,
 * so I'll just assume that it just needs to do the appropriate searching.
 * @author Paul
 *
 */
public class ChromosomeIntervalTree {
	int chromosomeid;
	IntervalTreeNode root;
	public ChromosomeIntervalTree(IntervalTreeNode root, int chr) {
		this.root = root;
		this.chromosomeid = chr;
	}


	/**
	 * we perform a breadth-first-search to see which files we should search thru for our query.
	 * @param start
	 * @param end
	 * @return
	 * @throws CorruptGeneReadException 
	 */
	public List<NodeSearchUnit> getFilesToSearch(GeneRange range) throws CorruptGeneReadException {
		List<NodeSearchUnit> rv = new ArrayList<>();
		if(!range.isSingleChromosome()) {
			throw new CorruptGeneReadException("Error: A chromosome tree was given a search that spans multiple chromosomes");
		}
		
		
		Deque<IntervalTreeNode> checkq = new LinkedList<>(); 
		if(this.root != null) {
			checkq.add(this.root);
		}
		
		while(!checkq.isEmpty()) {
			IntervalTreeNode currentnode = checkq.pollFirst();
			NodeSearchType stype = shouldSearchNode(currentnode, range);
			if(stype != NodeSearchType.NOSEARCH) {
				rv.add(new NodeSearchUnit(currentnode, stype, range));
			}
			IntervalTreeNode next = currentnode.getLeft();
			if(next != null) {
				stype = shouldCheckSubtree(next, range);
				if(stype == NodeSearchType.ENGULFSUBTREE) {
					rv.add(new NodeSearchUnit(next, stype, range));
				} else if(stype == NodeSearchType.SEARCHNODE) {
					checkq.add(next);
				}
			}
			next = currentnode.getRight();
			if(next != null) {
				stype = shouldCheckSubtree(next, range);
				if(stype == NodeSearchType.ENGULFSUBTREE) {
					rv.add(new NodeSearchUnit(next, stype, range));
				} else if(stype == NodeSearchType.SEARCHNODE) {
					checkq.add(next);
				}
			}
		}
		
		return rv;
	}
	
	private NodeSearchType shouldSearchNode(IntervalTreeNode n, GeneRange searchrange) {
		if(searchrange.getStartPoint() <= n.getStart() && searchrange.getEndPoint() >= n.getEnd()) {
			return NodeSearchType.ENGULFNODE;
		} else if((n.getStart() < searchrange.getStartPoint() && n.getEnd() < searchrange.getStartPoint()) || 
				(n.getStart() > searchrange.getEndPoint() && n.getEnd() > searchrange.getEndPoint())) {
			return NodeSearchType.NOSEARCH;
		}
		return NodeSearchType.SEARCHNODE;
	}
	/**
	 * basically the same check, but using the childmax flag 
	 * @param n
	 * @param start
	 * @param end
	 * @return
	 */
	private NodeSearchType shouldCheckSubtree(IntervalTreeNode n, GeneRange searchrange) {
		if(searchrange.getStartPoint() <= n.getMin() && searchrange.getEndPoint() >= n.getMax()) {
			return NodeSearchType.ENGULFSUBTREE;
		} else if((n.getMin() < searchrange.getStartPoint() && n.getMax() < searchrange.getStartPoint()) || 
				(n.getMin() > searchrange.getEndPoint() && n.getMax() > searchrange.getEndPoint())) {
			return NodeSearchType.NOSEARCH;
		}
		return NodeSearchType.SEARCHNODE;
	}
	
	public int getChrNumber() {
		return this.chromosomeid;
	}
}
