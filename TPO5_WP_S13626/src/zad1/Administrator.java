package zad1;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalTime;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

public class Administrator {
	
	private SocketChannel channelToServer;
	private Charset charset  = Charset.forName("UTF-8");
	private AdministratorGUI gui;
	
	public Administrator(InetAddress IP, int port, AdministratorGUI gui) {
		this.gui = gui;
		try {
            channelToServer = SocketChannel.open(new InetSocketAddress(IP, port));
            channelToServer.configureBlocking(false);
            log("połączenie do serwera otwarte");
        } catch (IOException e) {
            log("Nie mogę połączyć się z serwerem");
            System.exit(1);
        }	
	}
	
	private String readMessage() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		if (!channelToServer.isOpen()) return "";
		StringBuffer reqString = new StringBuffer();
		reqString.setLength(0);
		buffer.clear();
		try {
			readLoop:
				while (true) {
					int n = 0;
					n = channelToServer.read(buffer);
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
		} catch (IOException ex) {
			ex.printStackTrace();
			log("błąd przy odczycie wiadomości");
		}
		return reqString.toString().trim();
	}
	
	private void writeMessage(String message) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
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
		String topic = gui.getTopicText();
		topic.trim();
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
		String message = gui.getMessageText();
		boolean refresh = false;
		for (String topic : gui.getSelectedTopics()) {
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
	
	public void startGUI() {
		gui.addTopicButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				manageTopic("ADD_TOPIC");
			}
		});
		gui.removeTopicButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				manageTopic("DELETE_TOPIC");
			}
		});
		gui.refreshTopicListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshTopics();
			}
		});
		gui.sendMessageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				messageForTopics();
			}
		});
		gui.shutdownServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				shutdownServer();
			} 
		});
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	gui.start();
            }
        });
		
	}
	
	private void refreshTopics() {
		ArrayList<String> topics = new ArrayList<>();
		writeMessage("TOPICS");
		String lista = readMessage();
		for (String el : lista.split("::")) {
			topics.add(el);
		}
		gui.displayAvailableTopics(topics);
	}

	public static void main(String[] args) throws UnknownHostException {
		InetAddress addressIP = InetAddress.getByName("localhost");
		AdministratorGUI gui = new AdministratorGUI();
		Administrator admin = new Administrator(addressIP, 9999, gui);
		admin.startGUI();	
	}
}
