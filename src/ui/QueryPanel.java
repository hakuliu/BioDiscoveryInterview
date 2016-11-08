package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import query.GeneQuery;
import util.CorruptGeneReadException;
import util.GeneRange;
import util.GeneRange.GeneRangeException;
import util.GenomeSearchTree;
import util.GenomeSearchTree.GenomeTreeLoadException;

public class QueryPanel extends JPanel {
	private static final long serialVersionUID = 9087414392561802576L;
	private JLabel chosendblabel;
	private File dbfile;
	private JLabel outputlabel;
	private File outfile;
	private JLabel errormsg;
	private JButton executebutton;
	private GenomeSearchTree loadedtree;
	private JTextField queryfield;
	
	private static int MAX_FILELABEL_LEN = 300;
	public QueryPanel() {
		
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
		JLabel dblabel = new JLabel("Data Directory:");
		this.add(dblabel, gbc);
		
		gbc.gridx++;
		chosendblabel = new JLabel("None Chosen!");
		chosendblabel.setMaximumSize(new Dimension(MAX_FILELABEL_LEN, Integer.MAX_VALUE));
		this.add(chosendblabel, gbc);
		
		gbc.gridx++;
		JButton choosedb = new JButton("Choose Directory");
		this.add(choosedb, gbc);
		choosedb.addActionListener(getSelectInputButtonAction());
		
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel qlabel = new JLabel("Query: ");
		this.add(qlabel, gbc);
		
		gbc.gridx++;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		queryfield = new JTextField("chr1:0-chr1:999999");
		this.add(queryfield, gbc);
		
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.gridy++;
		JLabel outlabel = new JLabel("Output File:");
		this.add(outlabel, gbc);
		
		gbc.gridx++;
		this.outputlabel = new JLabel("None Chosen!");
		outputlabel.setMaximumSize(new Dimension(MAX_FILELABEL_LEN, Integer.MAX_VALUE));
		this.add(outputlabel, gbc);
		
		gbc.gridx++;
		JButton outbut = new JButton("Choose File");
		this.add(outbut, gbc);
		outbut.addActionListener(getSelectOutputButtonAction());
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		
		this.errormsg = new JLabel("ready.");
		this.add(errormsg, gbc);
		
		gbc.gridx += 2;
		gbc.gridwidth = 1;
		executebutton = new JButton("Execute Query");
		executebutton.setEnabled(false);
		this.add(executebutton, gbc);
		executebutton.addActionListener(getExecuteButtonAction());
	}
	
	private ActionListener getExecuteButtonAction() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateMsg("running query...");
				Thread async = new Thread(new Runnable() {
					
					@Override
					public void run() {
						attemptQuery(queryfield.getText());
					}
				});
				async.start();
			}
		};
	}
	
	private ActionListener getSelectOutputButtonAction() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int chooserval = chooser.showSaveDialog(QueryPanel.this);
				
				if(chooserval == JFileChooser.APPROVE_OPTION) {
					//this should be quick so no threading...
					QueryPanel.this.outfile = chooser.getSelectedFile();
					final File out = chooser.getSelectedFile();
					Thread async = new Thread(new Runnable() {
						
						@Override
						public void run() {
							setOutFile(out);;
							respondToFileSelection();
						}
					});
					async.start();
				}
			}
		};
	}
	
	private ActionListener getSelectInputButtonAction(){
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int chooserval = chooser.showOpenDialog(QueryPanel.this);
				
				if(chooserval == JFileChooser.APPROVE_OPTION) {
					final File toload = chooser.getSelectedFile();
					Thread async = new Thread(new Runnable() {
						
						@Override
						public void run() {
							attemptLoadTree(toload);
							respondToFileSelection();
						}
					});
					async.start();
				}
			}
		};
	}
	
	private void attemptQuery(String qtext) {
		GeneRange range = null;
		try {
			range = GeneRange.parseQueryTextToRange(qtext);
		} catch (GeneRangeException e) {
			respondToError(e);
			return;
		}
		if(range == null) {
			updateMsg("Query text syntax error.", Color.red);
		}
		System.out.println("executing query for range " + range);
		if(this.loadedtree != null && this.outfile != null) {
			toggleEnable(false);
			try {
				long starttime = System.currentTimeMillis();
				GeneQuery gq = new GeneQuery(this.loadedtree, range);
				gq.executeInFile(outfile);
				long qtime = System.currentTimeMillis() - starttime;
				updateMsg("Query ran in " + qtime + "ms.");
				System.out.println("query ran in " + qtime + "ms.");
			} catch (CorruptGeneReadException | IOException e) {
				respondToError(e);
			} finally {
				toggleEnable(true);
			}
			
		} else {
			//with how the enable/disable works this shouldn't be possible tho...
			updateMsg("not everything loaded for execution", Color.red);
		}
	}
	
	private void setOutFile(File out) {
		this.outfile = out;
		updateMsg("output file set to " + out.getName());
	}
	
	private void attemptLoadTree(File treeloc) {
		toggleEnable(false);
		GenomeSearchTree tree = new GenomeSearchTree(treeloc);
		try {
			tree.load();
			this.loadedtree = tree;
			this.dbfile = treeloc;
			System.out.println("tree loaded successfully.");
			updateMsg("Successfully loaded tree.");
		} catch (GenomeTreeLoadException | IOException e) {
			System.out.println("error in loading tree " + treeloc.getName());
			e.printStackTrace();
			respondToError(e);
		} finally {
			toggleEnable(true);
		}
		
	}
	
	private void respondToError(Exception e) {
		e.printStackTrace();
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JLabel toupdate = QueryPanel.this.errormsg;
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
				JLabel toupdate = QueryPanel.this.errormsg;
				if(toupdate != null) {
					toupdate.setForeground(Color.black);
					toupdate.setText(msg);
				}
			}
		});
	}
	
	private void respondToFileSelection() {
		if(this.dbfile != null) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					JLabel toupdate = QueryPanel.this.chosendblabel;
					if(toupdate != null) {
						toupdate.setText(dbfile.getName());
					}
				}
			});
		}
		if(this.outfile != null) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					JLabel toupdate = QueryPanel.this.outputlabel;
					if(toupdate != null) {
						toupdate.setText(outfile.getName());
					}
				}
			});
		}
		if(this.outfile != null && this.loadedtree != null) {
			toggleEnable(true);
		}
	}
	private void toggleEnable(boolean toggle) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JButton toupdate = QueryPanel.this.executebutton;
				if(toupdate != null) {
					toupdate.setEnabled(toggle);
				}
			}
		});
	}
}
