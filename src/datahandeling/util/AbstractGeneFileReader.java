package datahandeling.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import util.CorruptGeneReadException;
import util.GenePoint;

public abstract class AbstractGeneFileReader {
	protected File file;
	
	public AbstractGeneFileReader(File f) {
		this.file = f;
	}
	
	public abstract void open() throws FileNotFoundException;
	public abstract GenePoint readLine() throws IOException, CorruptGeneReadException;
	public abstract void closeReader();
}
