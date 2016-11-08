package datahandeling.util;

import java.io.File;
import java.io.IOException;

import util.GenePoint;

//Wanted to be able to write out the genes as "compressed form" and also "human readable form"
//using the same interfaces
public abstract class AbstractGeneFileWriter {
	protected File underlyingfile;
	public AbstractGeneFileWriter(File file) {
		this.underlyingfile = file;
	}
	
	protected File createFileIfNecessary() throws IOException {
		if(underlyingfile.getParentFile() != null) {
			underlyingfile.getParentFile().mkdirs();
		}
		
		if(!underlyingfile.exists()) {
			underlyingfile.createNewFile();
		}
		return underlyingfile;
	}
	
	public abstract void open() throws IOException ;
	public abstract void writeGene(GenePoint point) throws IOException;
	public abstract void close() throws IOException;
	public void renameFileTo(File newfile) throws IOException {
		if(this.underlyingfile != null) {
			this.underlyingfile.renameTo(newfile);
		}
	}
	
	public File getFile() {
		return this.underlyingfile;
	}
}
