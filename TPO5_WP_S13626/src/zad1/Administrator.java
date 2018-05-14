package zad1;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalTime;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

@SuppressWarnings("serial")
public class Administrator extends JFrame{
	private SocketChannel channelToServer;
	private ByteBuffer buffer = ByteBuffer.allocate(1024);
	private Charset charset  = Charset.forName("UTF-8");
	
	public Administrator(InetAddress IP, int port) {
		try {
            channelToServer = SocketChannel.open(new InetSocketAddress(IP, port));
            log("połączenie do serwera otwarte");
        } catch (IOException e) {
            log("Nie mogę połączyć się z serwerem");
            System.exit(1);
        }
        createAndShowGUI();
	}
	
	private String readMessage() {
		if (!channelToServer.isOpen()) return "";
		StringBuffer reqString = new StringBuffer();
		reqString.setLength(0);
		buffer.clear();

		readLoop:
			while (true) {
				int n = 0;
				try {
					n = channelToServer.read(buffer);
				} catch (IOException ex) {
					ex.printStackTrace();
					log("błąd przy odczycie wiadomości");
				}
				if (n > 0) {
					buffer.flip();
					CharBuffer cbuf = charset.decode(buffer);
					while(cbuf.hasRemaining()) {
						char c = cbuf.get();
						if (c == '\r' || c == '\n') break readLoop;
						reqString.append(c);
					}
					buffer.clear();
				}
			}
		log("odczytałem wiadomość: " + reqString.toString().trim());
		return reqString.toString().trim();
	}
	
	private void writeMessage(String message) {
		try {
			StringBuffer messageBuffer = new StringBuffer();
			messageBuffer.append(message);
			messageBuffer.append("\r\n");
			buffer = charset.encode(CharBuffer.wrap(messageBuffer));
			while(buffer.hasRemaining()) 
				channelToServer.write(buffer);
			log("Wysłałem wiadomość: " + message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	  
	public static void log(String message){
		LocalTime time = LocalTime.now();
		System.err.println("[" + time.toString() + "] Administrator: "+ message);
	}
	
	// zmienne dla GUI
	private JTextField topicText;
	private JTextArea messageArea;
	private JList<String> topicList;
	DefaultListModel<String> topicListModel;
	
	private void createAndShowGUI() {

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
		
		JButton addTopicButton = new JButton("Dodaj");
		addTopicButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				manageTopic("ADD_TOPIC");
			}
		});
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 3;
		position.anchor = GridBagConstraints.WEST;
		add(addTopicButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 1;
		position.gridy = 3;
		add(Box.createRigidArea(new Dimension(10,10)), position);	
		
		JButton removeTopicButton = new JButton("Usuń");
		removeTopicButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				manageTopic("DELETE_TOPIC");
			}
		});
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
		refreshTopics();
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
		
		JButton refreshTopicListButton = new JButton("Odśwież");
		refreshTopicListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshTopics();
			}
		});
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
		
		JButton sendMessageButton = new JButton("Wyślij wiadomość");
		sendMessageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				messageForTopics();
			}
		});
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
		
		JButton shutdownServerButton = new JButton("Wyłącz serwer");
		shutdownServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				shutdownServer();
			} 
		});
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 15;
		position.anchor = GridBagConstraints.EAST;
		add(shutdownServerButton, position);
		 
		setVisible(true);
	}
    
	private void shutdownServer () {
		writeMessage("SHUTDOWN");
		try {
			channelToServer.close();
		} catch (IOException e) {
		}
		JOptionPane.showMessageDialog(null, "Serwer został wyłączony");
		System.exit(0);
	}
	
	private void manageTopic(String command) {
		String topic = topicText.getText();
		topic.replaceAll("\\s+","");
		if (topic == null || topic.equals("")) {
			return;
		}
		writeMessage(command+"::" + topic);
		String response = readMessage();
		if (!response.equals("OK")) {
			JOptionPane.showMessageDialog(null, "Nie udało się " + (command.equals("ADD_TOPIC") ? "dodać" : "usunąć") + " tematu " + topic +".");
		} else {
			refreshTopics();
		}
	}
	
	private void messageForTopics() {
		java.util.List<String> topics = topicList.getSelectedValuesList();
		String message = messageArea.getText();
		boolean refresh = false;
		for (String topic : topics) {
			writeMessage("SEND_MESSAGE::" + topic + "::" + message);
			String outcome = readMessage();
			if (outcome.equals("TOPIC_GONE")) {
				refresh = true;
			} else
				log("wiadomość dla " + topic + " : otrzymało " + outcome.split("::")[1] + " klientów.");
		}
		if (refresh)
			refreshTopics();
	}
	
	private void refreshTopics() {
		writeMessage("TOPICS");
		String lista = readMessage();
		topicListModel.clear();
		for (String el : lista.split("::")) {
			topicListModel.addElement(el);
		}
	}

	public static void main(String[] args) throws UnknownHostException {
		InetAddress addressIP = InetAddress.getByName("localhost");
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	new Administrator(addressIP, 9999);
            }
        });
		
	}
}
