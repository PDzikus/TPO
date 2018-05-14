package zad1;

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
import javax.swing.*;


public class Klient extends JFrame{

	private static final long serialVersionUID = 1L;
	private SocketChannel requestsChannel;
	private SocketChannel messagesChannel;
	private Selector selector;
	private InetAddress serverIP;
	private int serverPort;
	private int id;
	private boolean clientIsRunning = true;
	private Charset charset  = Charset.forName("UTF-8");
	
    private LinkedList<String> subscribedTopics;
    private LinkedList<String> availableTopics;
    private KlientGUI gui;
	
	public Klient(InetAddress IP, int port, KlientGUI gui) {
		serverIP = IP;
		serverPort = port;
		subscribedTopics = new LinkedList<>();
		availableTopics = new LinkedList<>();
		this.gui = gui;
		
        try {
        	selector = Selector.open();
        	messagesChannel = SocketChannel.open(new InetSocketAddress(serverIP, serverPort));
            messagesChannel.configureBlocking(false);
            messagesChannel.register(selector, messagesChannel.validOps());
			requestsChannel = SocketChannel.open(new InetSocketAddress(serverIP, serverPort));
			requestsChannel.configureBlocking(false);
            writeMessage("REGISTER", messagesChannel);
            id = Integer.parseInt(readMessage(messagesChannel));
           	log("połączenie do serwera otwarte");
        } catch (IOException e) {
            log("Nie mogę połączyć się z serwerem");
            System.exit(1);
        }
	}
	
	public void listen() {
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
						serviceRequest(messagesChannel); 

					}
				}
			} catch(Exception exc) {
				log("straciłem połączenie z serwerem");
				clientIsRunning = false;
				gui.displayMessage("informacja od klienta", "Straciłem połączenie z serwerem");
			}
		}
		log("zamykam nasłuch");
	}
	
	private void serviceRequest(SocketChannel channel) {
		String request = readMessage(channel);
		String[] command = request.split("::");
		if (command[0].equals("TOPIC_MESSAGE")) {
			gui.displayMessage(command[1], command[2]);
			writeMessage("OK", channel);          
		}
		else if (command[0].equals("TOPIC_CHANGE")) {
			writeMessage("OK", channel);
			refreshTopics();
		}
		else if (command[0].equals("SHUTDOWN")) {
			gui.displayMessage("informacja od serwera", "Serwer zakończył działanie");
		}
	}
	
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
			log("otrzymałem wiadomość: " + reqString.toString().trim());
		} catch (Exception ex) {
			log("problem przy próbie odczytu - straciłem połączenie");
			clientIsRunning = false;
			gui.displayMessage("informacja od klienta", "Straciłem połączenie z serwerem");
		}
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
			log("problem z wysłaniem wiadomości - straciłem połączenie z serwerem");
			clientIsRunning = false;
			gui.displayMessage("informacja od klienta", "Straciłem połączenie z serwerem");
		}
	}
	
	private String sendRequest(String message) {
		String response = "";
		writeMessage(message, requestsChannel);
		response = readMessage(requestsChannel);
        return response;
	}
	
	private static void log(String message){
		LocalTime time = LocalTime.now();
		System.err.println("[" + time.toString() + "] Klient: "+ message);
	}
	
	public void subscribeTopic() {
		for (String topic : gui.getSelectedAvailableTopics()) {
			writeMessage("SUBSCRIBE::" + id + "::" + topic, requestsChannel);
			String answer = readMessage(requestsChannel);
			if(answer.equals("OK")) {
				subscribedTopics.add(topic);
			}
			availableTopics.remove(topic);
		}
		updateTopicsDisplay();
	}
	
	public void unsubscribeTopic() {
		for (String topic : gui.getSelectedSubscribedTopics()) {
			writeMessage("UNSUBSCRIBE::" + id + "::" + topic, requestsChannel);
			String answer = readMessage(requestsChannel);
			if(answer.equals("OK")) {
				availableTopics.add(topic);
			}
			subscribedTopics.remove(topic);
		}
		updateTopicsDisplay();
	}
		
	public void refreshTopics() {
		String[] topics = sendRequest("TOPICS").split("::");	
		LinkedList<String> subscribed = new LinkedList<>();
		LinkedList<String> available = new LinkedList<>();
		for (String topic : topics) 
			if (!subscribedTopics.contains(topic))
				available.add(topic);
			else
				subscribed.add(topic);
		subscribedTopics = subscribed;
		availableTopics = available;
		updateTopicsDisplay();
	}
	
	private void prepareGUI() {
		gui.unsubTopicButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unsubscribeTopic();
			}
		});	
		gui.subscribeTopicButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				subscribeTopic();
			}
		});
//		gui.closeClientButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				//shutdownClient();
//			}
//		});
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               	gui.start();
            }
        });
		refreshTopics();
	}
	
	private void updateTopicsDisplay() {
		gui.displayAvailableTopics(availableTopics);
		gui.displaySubscribedTopics(subscribedTopics);
	}

	public static void main(String[] args) throws UnknownHostException {
		InetAddress addressIP = InetAddress.getByName("localhost");
		KlientGUI gui = new KlientGUI();
		Klient klient = new Klient(addressIP, 9999, gui);
		klient.prepareGUI();
		klient.listen();
	}


}
