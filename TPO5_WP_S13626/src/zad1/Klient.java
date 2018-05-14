package zad1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.text.DefaultCaret;


public class Klient extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SocketChannel channelToServer;
	private SocketChannel channelFromServer;
	private Selector selector;
	private InetAddress serverIP;
	private int serverPort;
	private int id;
    private HashSet<String> subscribedTopics = new HashSet<String>();
    private HashSet<String> availableTopics = new HashSet<String>();
    private boolean clientIsRunning = true;
	
	public Klient(InetAddress IP, int port) {
		serverIP = IP;
		serverPort = port;
		Random rand = new Random();
		id = rand.nextInt();
        try {
        	selector = Selector.open();
        	channelFromServer = SocketChannel.open(new InetSocketAddress(serverIP, serverPort));
            channelFromServer.configureBlocking(false);
            channelFromServer.register(selector, channelFromServer.validOps());
			channelToServer = SocketChannel.open(new InetSocketAddress(serverIP, serverPort));
			channelToServer.configureBlocking(false);
			//channelToServer.register(selector, channelToServer.validOps());
            writeMessage("REGISTER::"+id, channelFromServer);
            if (readMessage(channelFromServer).equals("OK"))
            	log("połączenie do serwera otwarte");
        } catch (IOException e) {
            log("Nie mogę połączyć się z serwerem");
            System.exit(1);
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               	createAndShowGUI();
            }
        });
		serviceConnection();
	}
	
	private void serviceConnection() {
		while(clientIsRunning) {
			try {
				int readyCount = selector.select();
				if (readyCount == 0) {
					continue;
				}
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> iter = keys.iterator();
				while(iter.hasNext()) { 
					SelectionKey key = iter.next();
					iter.remove();

					if (key.isReadable()) {
						log("rozpoczynam odczyt z gniazda");
						serviceRequest(); 
					}
				}
			} catch(Exception exc) {
				log("straciłem połączenie z serwerem");
				clientIsRunning = false;
				displayMessage("informacja od serwera", "Straciłem połączenie z serwerem");
			}
		}
		log("zamykam nasłuch");
	}
	
	private void serviceRequest() {
		SocketChannel channel = channelFromServer;
		String request = readMessage(channel);
		String[] command = request.split("::");

		if (command[0].equals("TOPIC_MESSAGE")) {
			displayMessage(command[1], command[2]);
			writeMessage("OK", channel);          
		}
		else if (command[0].equals("TOPIC_CHANGE")) {
			writeMessage("OK", channel);
			refreshTopics();
		}
		else if (command[0].equals("SHUTDOWN")) {
			displayMessage("informacja od serwera", "Serwer zakończył działanie");
		}
	}

	private Charset charset  = Charset.forName("UTF-8");
		
	private String readMessage(SocketChannel channel) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		if (!channel.isOpen()) return "";
		StringBuffer reqString = new StringBuffer();
		reqString.setLength(0);
		buffer.clear();
		try {
			readLoop:
				while (true) {
					int n = 0;
						n = channel.read(buffer);
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
		} catch (Exception ex) {
			log("problem przy próbie odczytu");
		}
		log("otrzymałem wiadomość: " + reqString.toString().trim());
		return reqString.toString().trim();
	}
	
	private void writeMessage(String message, SocketChannel channel) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			StringBuffer messageBuffer = new StringBuffer();
			messageBuffer.append(message);
			messageBuffer.append("\r\n");
			buffer = charset.encode(CharBuffer.wrap(messageBuffer));
			while(buffer.hasRemaining()) 
				channel.write(buffer);
			log("Wysłałem wiadomość: " + message);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}
	
	private String sendRequest(String message) {
		String response = "";
		writeMessage(message, channelToServer);
		response = readMessage(channelToServer);
        return response;
	}
	
	private void displayMessage(String topic, String message) {
		String toDisplay = "[ "+LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))+" ] kanał: " + topic;
		messageArea.append(toDisplay + "\r\n");
		messageArea.append(message + "\r\n");
		messageArea.append("\r\n");
	}
	
	public static void log(String message){
		LocalTime time = LocalTime.now();
		System.err.println("[" + time.toString() + "] Klient: "+ message);
	}
	
	// zmienne dla GUI
	private JTextArea messageArea;
	private JList<String> subscribedTopicList;
	private JList<String> availableTopicList;
	
	private void createAndShowGUI() {
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
		
		refreshTopics();
		
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 3;
		add(Box.createRigidArea(new Dimension(10,15)), position);	
		
		position = new GridBagConstraints();
		position.gridx = 1;
		position.gridy = 4;
		add(Box.createRigidArea(new Dimension(10,5)), position);	
		
		JButton removeTopicButton = new JButton("Zapisz >>");
		removeTopicButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				subscribeTopic();
			}
		});
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 4;
		position.anchor = GridBagConstraints.CENTER;
		add(removeTopicButton, position);
		
		JButton refreshTopicListButton = new JButton("<< Usuń");
		refreshTopicListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unsubscribeTopic();
			}
		});
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 5;
		position.anchor = GridBagConstraints.CENTER;
		add(refreshTopicListButton, position);
		
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
		
		JButton sendMessageButton = new JButton("Zamknij połączenie");
		sendMessageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				//messageForTopics();
			}
		});
		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 10;
		position.gridwidth = 1;
		position.anchor = GridBagConstraints.EAST;
		add(sendMessageButton, position);
		
		setVisible(true);
	}
	
	private void subscribeTopic() {
		java.util.List<String> topics = availableTopicList.getSelectedValuesList();
		for (String topic : topics) {
			writeMessage("SUBSCRIBE::" + id + "::" + topic, channelToServer);
			String answer = readMessage(channelToServer);
			if(answer.equals("OK")) {
				subscribedTopics.add(topic);
			}
			availableTopics.remove(topic);
		}
		showTopics();
	}
	
	private void unsubscribeTopic() {
		java.util.List<String> topics = subscribedTopicList.getSelectedValuesList();
		for (String topic : topics) {
			writeMessage("UNSUBSCRIBE::" + id + "::" + topic, channelToServer);
			String answer = readMessage(channelToServer);
			if(answer.equals("OK")) {
				availableTopics.add(topic);
			}
			subscribedTopics.remove(topic);
		}
		showTopics();
	}
	
	private void showTopics() {
		DefaultListModel<String> listModel = (DefaultListModel<String>) availableTopicList.getModel();
		listModel.clear();
		for (String topic : availableTopics) {
			listModel.addElement(topic);
		}
		listModel = (DefaultListModel<String>) subscribedTopicList.getModel();
		listModel.clear();
		for (String topic : subscribedTopics) {
			listModel.addElement(topic);
		}	
	}
	
	private void refreshTopics() {
		String[] topics = sendRequest("TOPICS").split("::");	
		
		for (String topic : topics) 
			availableTopics.add(topic);
		showTopics();
	}

	public static void main(String[] args) throws UnknownHostException {
		InetAddress addressIP = InetAddress.getByName("localhost");
		new Klient(addressIP, 9999);
				
	}


}
