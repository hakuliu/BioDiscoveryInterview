package tests;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import datahandeling.util.AbstractGeneFileReader;
import datahandeling.util.HumanReadableGeneFileReader;
import util.GenePoint;

public class ResultValidator {
	public static boolean validate(List<GenePoint> expected, List<GenePoint> observed) {
		if(expected.size() != observed.size()) {
			return false;
		}
		HashSet<GenePoint> hash = new HashSet<>();
		for(GenePoint pt : expected) {
			hash.add(pt);
		}
		//using hashset makes search o(1) instead of searching thru list of o(n)
		//this reduces our actual validation to o(2n) instead of o(n^2).
		
		for(GenePoint pt : observed) {
			if(!hash.contains(pt)) {
				return false;
			}
		}
		return true;
	}
	public static boolean validate(List<GenePoint> expected, File observed) {
		HashSet<GenePoint> hash = new HashSet<>();
		for(GenePoint pt : expected) {
			hash.add(pt);
		}
		
		AbstractGeneFileReader reader = new HumanReadableGeneFileReader(observed);
		try {
			reader.open();
			GenePoint pt = null;
			
			while((pt = reader.readLine()) != null) {
				if(!hash.contains(pt)) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			reader.closeReader();
		}
		
		
	}
}
