package zad1;

import java.awt.*;
import javax.swing.*;


@SuppressWarnings("serial")
public class PhoneDirectoryClientGUI extends JFrame {
	
	public JButton getButton;
	public JButton addButton;
	public JButton replaceButton;
	private JTextField name;
	private JTextField number;
	private JLabel info;
	
	public PhoneDirectoryClientGUI () {
		setTitle("Klient");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(450,200);
		
		setLayout(new GridBagLayout());
		GridBagConstraints position;
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 0;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Imię"), position);
		
		name = new JTextField(20);
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 1;
		position.anchor = GridBagConstraints.WEST;
		add(name, position);
		
		position = new GridBagConstraints();
		position.gridx = 1;
		position.gridy = 1;
		add(Box.createRigidArea(new Dimension(10,10)), position);	
		
		getButton = new JButton("Pobierz");
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 1;
		position.anchor = GridBagConstraints.WEST;
		add(getButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 2;
		add(Box.createRigidArea(new Dimension(10,20)), position);	
		
		info = new JLabel (" ");
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 3;
		position.gridwidth = 4;
		position.anchor = GridBagConstraints.WEST;
		add(info, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 4;
		add(Box.createRigidArea(new Dimension(10,20)), position);	
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 5;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Numer"), position);
		
		number = new JTextField(20);
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 6;
		position.anchor = GridBagConstraints.WEST;
		add(number, position);
		
		addButton = new JButton("Dodaj");
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 6;
		position.anchor = GridBagConstraints.WEST;
		add(addButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 3;
		position.gridy = 6;
		add(Box.createRigidArea(new Dimension(10,10)), position);	
		
		replaceButton = new JButton("Zamień");
		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 6;
		position.anchor = GridBagConstraints.WEST;
		add(replaceButton, position);	
	}
	
	public String getName() {
		return name.getText().trim();
	}
	
	public void displayInfo(String text) {
		info.setText(text);
	}
	
	public void setNumber(String newNumber) {
		number.setText(newNumber);
	}
	
	public String getNumber() {
		return number.getText().replaceAll("\\D", "");
	}

	public void start() {
		setVisible(true);
	}
}
