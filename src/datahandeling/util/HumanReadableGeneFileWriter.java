package datahandeling.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import util.ChromosomeSequence;
import util.GenePoint;

//writes out each gene point as the same format as what was provided by BioDiscovery
//tab-separated values, new line per point.
public class HumanReadableGeneFileWriter extends AbstractGeneFileWriter {
	private boolean header;
	private BufferedWriter underlyingwriter = null;
	public HumanReadableGeneFileWriter(File f, boolean header) {
		super(f);
		this.header = header;
	}
	@Override
	public void open() throws IOException {
		File f = this.createFileIfNecessary();
		FileOutputStream fop = new FileOutputStream(f);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fop));
		this.underlyingwriter = bw;
		if(header) {
			writeHeader();
		}
	}
	
	private void writeHeader() throws IOException {
		if(this.underlyingwriter != null) {
			StringBuilder head = new StringBuilder();
			head.append("Chromesome");
			head.append('\t');
			head.append("Start");
			head.append('\t');
			head.append("End");
			head.append('\t');
			head.append("Value");
			head.append('\t');
			head.append("Array");
			underlyingwriter.write(head.toString());
			underlyingwriter.newLine();
		}
	}
	
	@Override
	public void writeGene(GenePoint point) throws IOException {
		if(this.underlyingwriter != null) {
			StringBuilder line = new StringBuilder();
			line.append(ChromosomeSequence.chromosomeIntToStr(point.getChromID()));
			line.append('\t');
			line.append(point.getStart());
			line.append('\t');
			line.append(point.getEnd());
			line.append('\t');
			line.append(point.getVal());
			line.append('\t');
			line.append(point.getArr());
			this.underlyingwriter.write(line.toString());
			this.underlyingwriter.newLine();
		}
	}
	@Override
	public void close() {
		try {
			if(this.underlyingwriter != null) {
				this.underlyingwriter.close();
			}
		} catch (IOException e) {
			//in a real software we'd log exceptions...
			e.printStackTrace();
		}
	}
	
}
