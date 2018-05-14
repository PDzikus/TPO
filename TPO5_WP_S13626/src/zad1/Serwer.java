package zad1;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import java.time.LocalTime;

public class Serwer {
	private ServerSocketChannel serverSocket = null;
	private Selector selector = null;
	private HashMap<String, ArrayList<Integer>> subscriptions = new HashMap<String, ArrayList<Integer>>();
	private HashMap<Integer, SocketChannel> clients = new HashMap<Integer, SocketChannel>();
	private Set<String> topics = new HashSet<String>();
	boolean serverIsRunning = true;
	private int clientCounter;
	
	private Serwer (String host, int port) {
		clientCounter = 0;
		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
			serverSocket.socket().bind(new InetSocketAddress(host, port));
			selector = Selector.open();
			serverSocket.register(selector,SelectionKey.OP_ACCEPT);
		} catch(Exception exc) {
			exc.printStackTrace();
			System.exit(1);
		}
		log("gniazdo otwarte");
		System.out.println("Serwer uruchomiony i gotowy na połączenia.");
		serviceConnections();
	}

	private void serviceConnections() {
		while(serverIsRunning) {
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
					if (key.isAcceptable()) {
						SocketChannel client = serverSocket.accept();
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);
						log("otwarto nowe połączenie");
						continue;
					}

					if (key.isReadable()) {
						SocketChannel client = (SocketChannel) key.channel();
						log("rozpoczynam odczyt z gniazda");
						serviceRequest(client); 
					}
				}
			} catch(Exception exc) {
				exc.printStackTrace();
				continue;
			}
		}
		log("zamykam serwer");
	}

	private Charset charset  = Charset.forName("UTF-8");
	
	private void serviceRequest(SocketChannel client) {
		try {
			String request = readMessage(client);
			String[] command = request.split("::");

			log("Otrzymałem wiadomość: " + request);
			if (command[0].equals("SHUTDOWN")) {
				log("rozpoznałem SHUTDOWN");
				writeMessage(client, "OK");          
				client.close();          
				client.socket().close();
				serverIsRunning = false;
			}
			else if (command[0].equals("TOPICS")) {
				writeMessage(client, topicsString());
			}
			else if (command[0].equals("ADD_TOPIC")) {
				if (addTopic(command[1])) 
					writeMessage(client, "OK");
				else
					writeMessage(client, "NOPE");
			}
			else if (command[0].equals("DELETE_TOPIC")) {
				writeMessage(client, "OK");
				removeTopic(command[1]);
			}
			else if (command[0].equals("SEND_MESSAGE")) {
				if (topics.contains(command[1])) {
					int clientsOnTopic = sendToSubscriptions(command[1], command[2]);
					writeMessage(client, "OK::" + clientsOnTopic);
				} else
					writeMessage(client, "TOPIC_GONE");
			}
			else if (command[0].equals("REGISTER")) {
				clientCounter++;
				addClient(clientCounter, client);
				writeMessage(client, Integer.toString(clientCounter));
				
			}
			else if (command[0].equals("SUBSCRIBE")) {
				if (subscribe(Integer.parseInt(command[1]), command[2]))
					writeMessage(client,"OK");
				else
					writeMessage(client,"NOPE");
			}
			else if (command[0].equals("UNSUBSCRIBE")) {
				if (unsubscribe(Integer.parseInt(command[1]), command[2]))
					writeMessage(client,"OK");
				else
					writeMessage(client,"NOPE");
			}
			else
				writeMessage(client, "UNKNOWN COMMAND");

		} catch (Exception exc) {
			exc.printStackTrace();
			try { 
				client.close();
				client.socket().close();
			} catch (Exception e) {}
		}
	}
	
	private String readMessage(SocketChannel remote) {
		if (!remote.isOpen()) return "";
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		StringBuffer reqString = new StringBuffer();
		reqString.setLength(0);
		buffer.clear();
		try {
		readLoop:
			while (true) {
				int n = remote.read(buffer);
				if (n > 0) {
					buffer.flip();
					CharBuffer cbuf = charset.decode(buffer);
					while(cbuf.hasRemaining()) {
						char c = cbuf.get();
						if (c == '\r' || c == '\n') break readLoop;
						reqString.append(c);
					}
				}
				buffer.clear();
			}
		} catch (IOException ex) {
			log("Problem w komunikacji z klientem");
			try {
				remote.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			removeClient(remote);
		}
		return reqString.toString().trim();
	}
	
	
	private void writeMessage(SocketChannel remote, String message) {
		try {
			StringBuffer messageBuffer = new StringBuffer();
			messageBuffer.append(message);
			messageBuffer.append("\r\n");
			ByteBuffer buffer = charset.encode(CharBuffer.wrap(messageBuffer));
			remote.setOption(StandardSocketOptions.TCP_NODELAY, false );
			while (buffer.hasRemaining())
				remote.write(buffer);
			log("Wysłałem wiadomość: " + message);
		} catch (IOException ex) {
			log("Problem z połączeniem na podany kanał");
			removeClient(remote);
		}
	}
	  
	public static void log(String message){
		LocalTime time = LocalTime.now();
		System.err.println("[" + time.toString() + "] Server: "+ message);
	}
	
	public boolean addTopic(String name) {
		if (topics.contains(name)) {
			return false;
		}
		topics.add(name);
		subscriptions.put(name, new ArrayList<Integer>());
		for (Entry<Integer, SocketChannel> client : clients.entrySet()) {
			writeMessage(client.getValue(), "TOPIC_CHANGE");
			if (!readMessage(client.getValue()).equals("OK")) {
				log ("Problem z informacją dla klienta");
				return false;
			}
		}
		return true;
	}
	
	public boolean removeTopic(String name) throws IOException {
		if (!topics.contains(name)) {
			return false;
		}
		subscriptions.remove(name);
		topics.remove(name);
		for (SocketChannel client : clients.values()) {
			writeMessage(client,"TOPIC_CHANGE");
			if (!readMessage(client).equals("OK")) {
				log ("Problem z informacją dla klienta");
				return false;
			}
		}
		return true;
	}
	
	public String topicsString() {
		if (topics.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		for (String topic : topics) {
			sb.append(topic);
			sb.append("::");
		}
		sb.setLength(sb.length() - 2);
		return sb.toString();
	}

	
	public boolean subscribe(int client, String topic) {
		if (!topics.contains(topic)) {
			log("Subscribe: nie moge znaleźć tematu: " + topic);
			return false;
		}
		if (subscriptions.get(topic).contains(client)) {
			log("Subscribe: klient jest już zapisany do tematu " + topic);
			return true;
		}
		subscriptions.get(topic).add(client);
		log("Subscribe: zapisałem nowego klienta do tematu: " + topic );
		return true;
	}
	
	public boolean unsubscribe(int client, String topic) {
		if (!topics.contains(topic)) {
			log("Unsubscribe: nie moge znaleźć tematu: " + topic);
			return false;
		}
		if (subscriptions.get(topic).remove(Integer.valueOf(client))) {
			log("unubscribe: klient został usunięty z tematu " + topic);
			return true;
		} else {
			log("Unsubscribe: klient nie był zapisany do tematu: " + topic );
			return true;
		}
	}
	
	public int sendToSubscriptions(String topic, String message) {
		int licznik = 0;
		message = topic + "::" + message;
		for (int id : subscriptions.get(topic)) {
			SocketChannel client = clients.get(id);
			writeMessage(client, "TOPIC_MESSAGE::" + message);
			String answer = readMessage(client);
			if (answer.equals("OK"))
				licznik++;
			else if (answer.equals("NO_CLIENT"))
				removeClient(client);
		}
		return licznik;
	}
	
	private void removeClient(SocketChannel client) {
		if (!clients.containsValue(client))
			return;
		int id = 0;
		for (Entry<Integer, SocketChannel> entry : clients.entrySet()) {
			if (entry.getValue() == client) {
				id = entry.getKey();
				break;
			}
		}
		for (Entry<String, ArrayList<Integer>> subs: subscriptions.entrySet()) {
			if (subs.getValue().remove(Integer.valueOf(id)));
				log("Usuwam klienta " + id + " z listy " + subs.getKey());
		}
		clients.remove(id);
	}
	
	private void addClient (int id, SocketChannel client) {
		if (!clients.containsKey(id)) {
			clients.put(id, client);
			log("Nowy klient " + client.toString());
		}
	}
	
	
	public static void main(String[] args) throws UnknownHostException {
		new Serwer("localhost", 9999);
	}
}