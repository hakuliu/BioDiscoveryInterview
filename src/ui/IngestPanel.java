package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import datahandeling.consumeconstruct.DataIngestor;

public class IngestPanel extends JPanel {

	private static final long serialVersionUID = -3890173419927065492L;
	private static int MAX_FILELABEL_LEN = 300;
	private JLabel chosendatalabel = null;
	private JLabel outputlabel = null;
	private JLabel errormsg = null;
	private JButton ingestbutton = null;
	
	File outdir = null;
	File datafile = null;
	public IngestPanel() {
		
	}
	public void build() {
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.d;
		gbc.weighty = 1.d;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.ipadx = 36;
		gbc.ipadx = 36;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = GridBagConstraints.BOTH;
		JLabel dblabel = new JLabel("Data File: ");
		this.add(dblabel, gbc);
		
		gbc.gridx++;
		chosendatalabel = new JLabel("None Chosen!");
		chosendatalabel.setMaximumSize(new Dimension(MAX_FILELABEL_LEN, Integer.MAX_VALUE));
		this.add(chosendatalabel, gbc);
		
		gbc.gridx++;
		JButton choosedb = new JButton("Choose File");
		this.add(choosedb, gbc);
		choosedb.addActionListener(getSelectInputButtonAction());
		
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.gridy++;
		JLabel outlabel = new JLabel("Output Directory:");
		this.add(outlabel, gbc);
		
		gbc.gridx++;
		outputlabel = new JLabel("None Chosen!");
		outputlabel.setMaximumSize(new Dimension(MAX_FILELABEL_LEN, Integer.MAX_VALUE));
		this.add(outputlabel, gbc);
		
		gbc.gridx++;
		JButton outbut = new JButton("Choose Directory");
		this.add(outbut, gbc);
		outbut.addActionListener(getSelectOutputButtonAction());
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		errormsg = new JLabel("");
		this.add(errormsg, gbc);
		
		gbc.gridx += 2;
		gbc.gridwidth = 1;
		ingestbutton = new JButton("Ingest");
		ingestbutton.setEnabled(false);
		this.add(ingestbutton, gbc);
		ingestbutton.addActionListener(getIngestButtonAction());
	}
	private ActionListener getIngestButtonAction() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				Thread ingestthread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						executeIngest();
					}
				});
				ingestthread.start();
			}
		};
	}
	private ActionListener getSelectOutputButtonAction() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int chooserval = chooser.showSaveDialog(IngestPanel.this);
				
				if(chooserval == JFileChooser.APPROVE_OPTION) {
					IngestPanel.this.outdir = chooser.getSelectedFile();
					respondToFileSelection();
				}
			}
		};
	}
	private ActionListener getSelectInputButtonAction(){
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				
				int chooserval = chooser.showOpenDialog(IngestPanel.this);
				
				if(chooserval == JFileChooser.APPROVE_OPTION) {
					IngestPanel.this.datafile = chooser.getSelectedFile();
					respondToFileSelection();
				}
			}
		};
	}
	private void executeIngest() {
		if(this.datafile != null && this.outdir != null) {
			toggleEnable(false);
			DataIngestor ingestor = new DataIngestor(datafile, outdir);
			try {
				updateMsg("Ingesting data...");
				long startt = System.currentTimeMillis();
				ingestor.executeIngest();
				long endt = System.currentTimeMillis() - startt;
				updateMsg("Data ingested in " + endt + "ms.");
			} catch (Exception e) {
				e.printStackTrace();
				respondToError(e);
			} finally {
				toggleEnable(true);
			}
		} else {
			updateMsg("Files not selected", Color.red);
		}
	}
	private void respondToFileSelection() {
		if(this.datafile != null) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					JLabel toupdate = chosendatalabel;
					if(toupdate != null) {
						toupdate.setText(datafile.getName());
					}
				}
			});
		}
		if(this.outdir != null) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					JLabel toupdate = outputlabel;
					if(toupdate != null) {
						toupdate.setText(outdir.getName());
					}
				}
			});
		}
		if(this.datafile != null && this.outdir != null) {
			toggleEnable(true);
		}
	}
	private void respondToError(Exception e) {
		e.printStackTrace();
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JLabel toupdate = errormsg;
				if(toupdate != null) {
					toupdate.setForeground(Color.red);
					toupdate.setText(e.getMessage());
				}
			}
		});
	}
	
	private void updateMsg(String msg) {
		updateMsg(msg, Color.black);
	}
	private void updateMsg(String msg, Color c) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JLabel toupdate = errormsg;
				if(toupdate != null) {
					toupdate.setForeground(Color.black);
					toupdate.setText(msg);
				}
			}
		});
	}
	private void toggleEnable(boolean toggle) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JButton toupdate = ingestbutton;
				if(toupdate != null) {
					toupdate.setEnabled(toggle);
				}
			}
		});
	}
}
