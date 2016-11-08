package util;
/**
 * Needed a way to keep the sequence of chromosomes while accounting for X and Y.
 * Also, this way, we can have a different sequence for nonhumans etc. 
 * @author Paul
 *
 */
public class ChromosomeSequence {
	public static int X = Integer.MAX_VALUE - 1;
	public static int Y = Integer.MAX_VALUE;
	public static int maxNumberedChromosomes = 22;
	public static int chromosomeStrToInt(String chrstr) throws CorruptGeneReadException {
		if(!"chr".equalsIgnoreCase(chrstr.substring(0, 3))) {
			throw new CorruptGeneReadException("unexpected chromosome id: " + chrstr);
		} else {
			String id = chrstr.substring(3);
			if("X".equalsIgnoreCase(id)) {
				return X;
			} else if("Y".equalsIgnoreCase(id)) {
				return Y;
			} else {
				try {
					return Integer.parseInt(id);
				} catch (Exception e) {
					throw new CorruptGeneReadException("unexpected chromosome id: " + chrstr);
				}
			}
		}
	}
	public static String chromosomeIntToStr(int n) {
		if(n==X) {
			return "chrX";
		} else if(n==Y) {
			return "chrY";
		} else {
			return "chr"+n;
		}
	}
	public static int getNext(int current) {
		if(current == X) {
			return Y;
		} else if(current == Y) {
			return -1;
		}
		int rv = current + 1;
		if(current > maxNumberedChromosomes) {
			rv = X;
		}
		return rv;
	}
}
