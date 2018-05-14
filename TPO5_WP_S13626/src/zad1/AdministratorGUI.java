package zad1;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class AdministratorGUI extends JFrame {

	// zmienne dla GUI
	private JTextField topicText;
	private JTextArea messageArea;
	private JList<String> topicList;
	DefaultListModel<String> topicListModel;
	
	public JButton addTopicButton;
	public JButton removeTopicButton;
	public JButton refreshTopicListButton;
	public JButton sendMessageButton;
	public JButton shutdownServerButton;
	
	public AdministratorGUI() {

		setTitle("Administrator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(280,480);
		
		setLayout(new GridBagLayout());
		GridBagConstraints position;
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 0;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Temat:"), position);		
		
		topicText = new JTextField(20);
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 1;
		position.gridwidth = 3;
		position.anchor = GridBagConstraints.WEST;
		add(topicText, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 2;
		add(Box.createRigidArea(new Dimension(10,5)), position);	
		
		addTopicButton = new JButton("Dodaj");
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 3;
		position.anchor = GridBagConstraints.WEST;
		add(addTopicButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 1;
		position.gridy = 3;
		add(Box.createRigidArea(new Dimension(10,10)), position);	
		
		removeTopicButton = new JButton("Usuń");
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 3;
		position.anchor = GridBagConstraints.EAST;
		add(removeTopicButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 4;
		add(Box.createRigidArea(new Dimension(10,15)), position);	
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 5;
		position.gridwidth = 3;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Lista zarejestrowanych tematów:"), position);	
		
		topicListModel = new DefaultListModel<String>();
		topicList = new JList<String>(topicListModel);
		topicList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane pane = new JScrollPane(topicList);
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 6;
		position.gridwidth = 3;
		position.anchor = GridBagConstraints.WEST;
		pane.setPreferredSize(new Dimension (225,100));
		add(pane,position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 7;
		add(Box.createRigidArea(new Dimension(10,5)), position);	
		
		refreshTopicListButton = new JButton("Odśwież");
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 8;
		position.anchor = GridBagConstraints.WEST;
		add(refreshTopicListButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 9;
		add(Box.createRigidArea(new Dimension(10,20)), position);	
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 10;
		position.gridwidth = 3;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Wiadomość dla wybranych kanałów:"), position);
		
		
		messageArea = new JTextArea(5,20);
		messageArea.setLineWrap(true);
		JScrollPane messagePane = new JScrollPane(messageArea);
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 11;
		position.gridwidth = 3;
		add(messagePane, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 12;
		add(Box.createRigidArea(new Dimension(10,5)), position);	
		
		sendMessageButton = new JButton("Wyślij wiadomość");
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 13;
		position.gridwidth = 3;
		position.anchor = GridBagConstraints.WEST;
		add(sendMessageButton, position);
			
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 14;
		add(Box.createRigidArea(new Dimension(10,20)), position);	
		
		shutdownServerButton = new JButton("Wyłącz serwer");
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 15;
		position.anchor = GridBagConstraints.EAST;
		add(shutdownServerButton, position);

	}
	
	public String getTopicText() {
		return topicText.getText();
	}
	
	public String getMessageText() {
		return messageArea.getText();
	}
	
	public void displayAvailableTopics(List<String> topics) {
		topicListModel.clear();
		for (String topic : topics) {
			topicListModel.addElement(topic);
		}	
	}
	
	public List<String> getSelectedTopics() {
		return topicList.getSelectedValuesList();
	}

	public void start() {
		setVisible(true);
	}
}
