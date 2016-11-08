package datahandeling.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import util.CorruptGeneReadException;
import util.GenePoint;

public class HumanReadableGeneFileReader extends AbstractGeneFileReader {
	
	private BufferedReader reader;
	private boolean fresh = true;//this boolean is used to discard the header (if necessary)
	public HumanReadableGeneFileReader(String filepath) {
		this(new File(filepath));
	}
	
	public HumanReadableGeneFileReader(File f) {
		super(f);
	}
	@Override
	public void open() throws FileNotFoundException{
			InputStream stream = new FileInputStream(this.file);
			InputStreamReader isr = new InputStreamReader(stream);
			BufferedReader br = new BufferedReader(isr);
			this.reader = br;
	}
	@Override
	public GenePoint readLine() throws IOException, CorruptGeneReadException {
		String line = this.reader.readLine();
		if(line != null) {
			GenePoint current = GenePoint.parseFromLine(line);
			if(current == null && fresh) {
				//this means we encountered a header, so we'll just skip to the next
				line = this.reader.readLine();
				if(line != null) {
					current = GenePoint.parseFromLine(line);
				}
				fresh = false;
			}
			if(current == null) {
				//if this current is null at this point, we might have a corrupt data...
				throw new CorruptGeneReadException("Tried to parse following line into gene point but failed: " + line);
			}
			return current;
		}

		return null;
	}
	@Override
	public void closeReader() {
		if(this.reader != null) {
			try {
				this.reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
