package org.server;

import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class HelpView {
	private JFrame frame;
	private JPanel mainpanel;
	private JTabbedPane tabbedPane;

	public HelpView() {
		display();
	}

	/*
	 * Display method for Frame
	 */
	private void display() {
		frame = new JFrame("HelpMenu");
		frame.setSize(600, 400);
		MainPanel();
		frame.add(mainpanel);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}

	/*
	 * MainPanel of the Frame
	 */
	private void MainPanel() {
		mainpanel = new JPanel();
		mainpanel.setLayout(new GridLayout(1, 1));

		// JLabel text = new JLabel("Help");
		// text.setBounds(10,10,100,20);
		// mainpanel.add(text);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		// tabbedPane.setBounds(10, 10, 550, 300);
		mainpanel.add(tabbedPane);
		ImageIcon icon = new ImageIcon("ask.PNG");
		// make new tabs with text!
		tabbedPane
				.addTab("Help",
						icon,
						makePanel("Hello and welcome to the Help Menu!"
								+ "\n"
								+ "\n"
								+ "If you need some help then this is the perfect place for you!"));
		tabbedPane
				.addTab("How to get started!",
						icon,
						makePanel("How to get started!"
								+ "\n"
								+ "\n"
								+ "1. Load a XML-File from the File -> Load File option"
								+ "\n"
								+ "2. Dont start the server if the Client is not open! (else it does nothing)"
								+ "\n"
								+ "3. If you want to update the website then Login to ftp!"
								+ "\n" + "4. Start the Server!"));
		tabbedPane.addTab("Contact", icon, makePanel("Contact: \n \n"
				+ "Groep 5 \n" + "Telefoonnummer: no thanks \n"
				+ "Email: projectcontaining@nhl.nl"));
	}

	private JPanel makePanel(String text) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		// JLabel Textlabel = new JLabel(text);
		// Textlabel.setBounds(5,0,400,400);
		JTextArea textArea = new JTextArea(text);
		textArea.setEditable(false);
		textArea.setBounds(0, 0, 600, 400);
		panel.add(textArea);
		// panel.setLayout(new GridLayout(1, 1));
		return panel;
	}

}
