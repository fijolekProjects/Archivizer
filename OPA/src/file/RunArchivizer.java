package file;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import file.MainGuiClass;

public class RunArchivizer {

    public static void main(String args[]) {

	final JFrame frame = new JFrame("Welcome to Archivizer!");
	Container contentPane = frame.getContentPane();

	SpringLayout layout = new SpringLayout();
	contentPane.setLayout(layout);

	JLabel hostNameLabel = new JLabel("Host name: ");
	final JTextField hostNameTextField = new JTextField("localhost", 15);

	contentPane.add(hostNameLabel);
	contentPane.add(hostNameTextField);

	layout.putConstraint(SpringLayout.WEST, hostNameLabel, 115, SpringLayout.WEST, contentPane);
	layout.putConstraint(SpringLayout.NORTH, hostNameLabel, 145, SpringLayout.NORTH,
		contentPane);
	layout.putConstraint(SpringLayout.NORTH, hostNameTextField, 145, SpringLayout.NORTH,
		contentPane);
	layout.putConstraint(SpringLayout.WEST, hostNameTextField, 30, SpringLayout.EAST,
		hostNameLabel);

	JLabel portNumberLabel = new JLabel("Port number: ");
	final JTextField portNumberTextField = new JTextField("1099", 15);

	contentPane.add(portNumberLabel);
	contentPane.add(portNumberTextField);
	layout.putConstraint(SpringLayout.WEST, portNumberLabel, 115, SpringLayout.WEST,
		contentPane);
	layout.putConstraint(SpringLayout.NORTH, portNumberLabel, 175, SpringLayout.NORTH,
		contentPane);
	layout.putConstraint(SpringLayout.NORTH, portNumberTextField, 175, SpringLayout.NORTH,
		contentPane);
	layout.putConstraint(SpringLayout.WEST, portNumberTextField, 20, SpringLayout.EAST,
		portNumberLabel);

	Icon logoIcon = new ImageIcon("logo.png");
	JLabel logoLabel = new JLabel(logoIcon);
	contentPane.add(logoLabel);
	layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, logoLabel, 0,
		SpringLayout.HORIZONTAL_CENTER, contentPane);
	layout.putConstraint(SpringLayout.NORTH, logoLabel, 20, SpringLayout.NORTH, contentPane);

	final JButton runButton = new JButton("Run!");
	contentPane.add(runButton);

	ActionListener actionListener = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (e.getSource() == runButton) {
		    String hostName = hostNameTextField.getText();
		    int portNumber = Integer.parseInt(portNumberTextField.getText());
		    MainGuiClass.setHostName(hostName);
		    MainGuiClass.setPortNumber(portNumber);
		    try {
			MainGuiClass.start();
		    } catch (Exception e1) {
			e1.printStackTrace();
		    }
		    frame.setVisible(false);
		}
	    }
	};

	layout.putConstraint(SpringLayout.NORTH, runButton, 220, SpringLayout.NORTH, contentPane);
	layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, runButton, 0,
		SpringLayout.HORIZONTAL_CENTER, contentPane);

	runButton.addActionListener(actionListener);
	frame.setSize(500, 300);
	frame.setLocationRelativeTo(null);
	frame.setResizable(false);
	frame.setVisible(true);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}