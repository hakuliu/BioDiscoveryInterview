package ui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class MainUIFrame extends JFrame {
	
	private static final long serialVersionUID = 1596967989403914799L;

	public MainUIFrame() {
		this.setTitle("BioDiscovery Interview Project");
		this.setSize(new Dimension(600, 200));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private void buildself() {
		JTabbedPane tabs = new JTabbedPane();
		
		QueryPanel querypanel = new QueryPanel();
		querypanel.build();
		IngestPanel ingestpanel = new IngestPanel();
		ingestpanel.build();
		
		tabs.addTab("Querying", querypanel);
		tabs.addTab("Injest Data", ingestpanel);
		
		this.add(tabs);
		
		this.setVisible(true);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				MainUIFrame frame = new MainUIFrame();
				frame.buildself();
			}
		});
	}
}
