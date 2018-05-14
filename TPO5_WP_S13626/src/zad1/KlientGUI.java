package zad1;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

@SuppressWarnings("serial")
public class KlientGUI extends JFrame {
	// zmienne dla GUI
	private JTextArea messageArea;
	private JList<String> subscribedTopicList;
	private JList<String> availableTopicList;
	public JButton subscribeTopicButton;
	public JButton unsubTopicButton;
	public JButton closeClientButton;
	
	public KlientGUI (){
		setTitle("Klient");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600,480);

		setLayout(new GridBagLayout());
		GridBagConstraints position;

		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 0;
		position.gridwidth = 4;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Zarządzanie kanałami:"), position);

		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 1;
		add(Box.createRigidArea(new Dimension(10,5)), position);	

		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 2;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Kanały dostępne:"), position);

		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 2;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Kanały subskrybowane:"), position);

		availableTopicList = new JList<String>(new DefaultListModel<String>());
		availableTopicList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane availablePane = new JScrollPane(availableTopicList);
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 3;
		position.gridheight = 3;
		position.anchor = GridBagConstraints.WEST;
		availablePane.setPreferredSize(new Dimension (225,100));
		add(availablePane,position);

		subscribedTopicList = new JList<String>(new DefaultListModel<String>());
		subscribedTopicList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane subscribedPane = new JScrollPane(subscribedTopicList);
		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 3;
		position.gridheight = 3;
		position.anchor = GridBagConstraints.EAST;
		subscribedPane.setPreferredSize(new Dimension (225,100));
		add(subscribedPane,position);
		
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 3;
		add(Box.createRigidArea(new Dimension(10,15)), position);	

		position = new GridBagConstraints();
		position.gridx = 1;
		position.gridy = 4;
		add(Box.createRigidArea(new Dimension(10,5)), position);	

		JButton subscribeTopicButton = new JButton("Zapisz >>");
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 4;
		position.anchor = GridBagConstraints.CENTER;
		add(subscribeTopicButton, position);

		JButton unsubTopicButton = new JButton("<< Usuń");

		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 5;
		position.anchor = GridBagConstraints.CENTER;
		add(unsubTopicButton, position);

		position = new GridBagConstraints();
		position.gridx = 3;
		position.gridy = 4;
		add(Box.createRigidArea(new Dimension(10,5)), position);	

		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 6;
		add(Box.createRigidArea(new Dimension(10,20)), position);	

		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 7;
		position.gridwidth = 3;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Wiadomość z wybranych kanałów:"), position);

		messageArea = new JTextArea(10,50);
		messageArea.setLineWrap(true);
		DefaultCaret caret = (DefaultCaret)messageArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane messagePane = new JScrollPane(messageArea);
		messagePane.setViewportView(messageArea);
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 8;
		position.gridwidth = 5;
		add(messagePane, position);

		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 9;
		add(Box.createRigidArea(new Dimension(10,30)), position);	

		JButton closeClientButton = new JButton("Zamknij połączenie");
		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 10;
		position.gridwidth = 1;
		position.anchor = GridBagConstraints.EAST;
		add(closeClientButton, position);	
	}
	
	public void displayMessage(String topic, String message) {
		String toDisplay = "[ "+LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))+" ] kanał: " + topic;
		messageArea.append(toDisplay + "\r\n");
		messageArea.append(message + "\r\n");
		messageArea.append("\r\n");
	}
	
	public void displaySubscribedTopics (List<String> topics) {
		DefaultListModel<String> listModel = (DefaultListModel<String>) subscribedTopicList.getModel();
		listModel.clear();
		for (String topic : topics) {
			listModel.addElement(topic);
		}	
	}
	
	public void displayAvailableTopics (List<String> topics) {
		DefaultListModel<String> listModel = (DefaultListModel<String>) availableTopicList.getModel();
		listModel.clear();
		for (String topic : topics) {
			listModel.addElement(topic);
		}
	}
	
	public List<String> getSelectedAvailableTopics () {
		return subscribedTopicList.getSelectedValuesList();
	}
	
	public List<String> getSelectedSubscribedTopics() {
		return availableTopicList.getSelectedValuesList();
	}
	
	public void start() {
		setVisible(true);
	}
}
