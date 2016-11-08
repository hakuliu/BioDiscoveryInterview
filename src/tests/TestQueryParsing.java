package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import util.GeneRange;
import util.GeneRange.GeneRangeException;
/**
 * uses the examples seen in explanations document as tests.
 * @author Paul
 *
 */
public class TestQueryParsing {

	@Test
	public void test1() {
		try {
			GeneRange expected = new GeneRange(18, 0, 18, 60000000);
			GeneRange observed = GeneRange.parseQueryTextToRange("chr18:0-60000000");
			assertEquals(expected, observed);
		} catch (GeneRangeException e) {
			fail("syntax error");
		}
	}
	@Test
	public void test2() {
		try {
			GeneRange expected = new GeneRange(3, 5000, 5, 8000);
			GeneRange observed = GeneRange.parseQueryTextToRange("chr3:5000-chr5:8000");
			assertEquals(expected, observed);
		} catch (GeneRangeException e) {
			fail("syntax error");
		}
	}
	
	@Test
	public void test3() {
		try {
			GeneRange expected = new GeneRange(2, Long.MIN_VALUE, 2, Long.MAX_VALUE);
			GeneRange observed = GeneRange.parseQueryTextToRange("chr2");
			assertEquals(expected, observed);
		} catch (GeneRangeException e) {
			fail("syntax error");
		}
	}
	@Test
	public void test4() {
		try {
			GeneRange expected = new GeneRange(2, Long.MIN_VALUE, 3, Long.MAX_VALUE);
			GeneRange observed = GeneRange.parseQueryTextToRange("chr2-chr3");
			assertEquals(expected, observed);
		} catch (GeneRangeException e) {
			fail("syntax error");
		}
	}
	@Test
	public void test5() {
		try {
			GeneRange expected = new GeneRange(5, 5000, 5, Long.MAX_VALUE);
			GeneRange observed = GeneRange.parseQueryTextToRange("chr5:5000");
			assertEquals(expected, observed);
		} catch (GeneRangeException e) {
			fail("syntax error");
		}
	}
}
