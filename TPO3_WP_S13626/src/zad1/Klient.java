package zad1;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.time.LocalTime;

import javax.swing.*;

@SuppressWarnings("serial")
public class Klient extends JFrame {
	private JComboBox<String> languageChoice;
	private JTextField wordField;
	private JLabel translationField;
	private InetAddress serverIP = null;
	private int serverPort;
	private String myIP = "127.0.0.1";
	
	public static void main (String[] args) {
		SwingUtilities.invokeLater( () -> Klient.start());
	}

	private Klient () {
		setTitle("Tłumacz");
		setSize(420,170);
		setLayout(new GridBagLayout());
		GridBagConstraints position;
		
		try {
			serverIP = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {}
		serverPort = 9999;
		
		languageChoice = setupLanguageChoice();
		JLabel langDescription = new JLabel ("Wybór języka tłumaczenia ");
		JLabel wordDescription = new JLabel ("Słowo do tłumaczenia ");
		JLabel translationDescription = new JLabel ("Tłumaczenie ");
		wordField = new JTextField(20);
		translationField = new JLabel("");
		JButton translateButton = setupTranslateButton();
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 0;
		position.anchor = GridBagConstraints.WEST;
		add(langDescription, position);
		
		position = new GridBagConstraints();
		position.gridx = 1;
		position.gridy = 0;
		add(Box.createRigidArea(new Dimension(10,10)), position);
		
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 0;
		position.anchor = GridBagConstraints.WEST;
		add(languageChoice, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 1;
		add(Box.createRigidArea(new Dimension(10,10)), position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 2;
		position.anchor = GridBagConstraints.WEST;
		add(wordDescription, position);
		
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 2;
		add(wordField, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 3;
		add(Box.createRigidArea(new Dimension(10,10)), position);
	
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 4;
		position.anchor = GridBagConstraints.WEST;
		add(translationDescription, position);

		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 4;
		add(translationField, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 5;
		add(Box.createRigidArea(new Dimension(10,10)), position);

		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 6;
		position.gridwidth = 3;
		add(translateButton, position);
		
		wordField.requestFocusInWindow();
		this.setVisible(true);
	
	}
	
	private JComboBox<String> setupLanguageChoice() {
		JComboBox<String> combo = new JComboBox<String>();
		combo.addItem("EN");
		combo.addItem("DE");
		combo.addItem("IT");
		combo.setSelectedIndex(0);
		combo.setPreferredSize(new Dimension(50, 20));
		return combo;
	}
	
	private JButton setupTranslateButton() {
		JButton b = new JButton ("Przetłumacz");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try (	Socket connectionSocket = new Socket(serverIP,serverPort);				
						OutputStream os = connectionSocket.getOutputStream();
						PrintWriter out = new PrintWriter(os, true);
						InputStream is = connectionSocket.getInputStream();
						BufferedReader br = new BufferedReader(new InputStreamReader(is));
						ServerSocket incomingSocket = new ServerSocket(0) ) 
				{
					log("wysyłam wiadomość: TRANSLATE");
					out.println("TRANSLATE");
					log("wysyłam wiadomość: " + getLanguageChoice());
					out.println(getLanguageChoice());
					log("wysyłam wiadomość: " + getWordField());
					out.println(getWordField());
					log("wysyłam wiadomość: " + myIP);
					out.println(myIP);
					out.println(incomingSocket.getLocalPort());
					out.flush();
					
					String response = br.readLine();
					log("otrzymałem wiadomość: " + response);
					if (response.equals("TRANSLATE LANGUAGE_UNKNOWN")) {
						translationField.setText("Brak słownika!");
						return;
					} else if (response.equals("TRANSLATE OK")) {
						String translation = getTranslationFromServer(incomingSocket);
						translationField.setText(translation);
					} else {
						translationField.setText("Błąd połączenia z serwerem usługi");
					}
				} catch (Exception ex) {
					translationField.setText("Błąd połączenia");
				}
			}
			
		});
		
		return b;
	}
	
	@SuppressWarnings("unused")
	protected static void start() {
		Klient okno = new Klient();
	}
	
	private String getWordField() {
		return wordField.getText();
	}
	
	private String getLanguageChoice() {
		return languageChoice.getSelectedItem().toString();
	}
	
	private String getTranslationFromServer(ServerSocket socket) {
		String answer = "Nieznane słowo";
		try {
			Socket connection = socket.accept();
			try (	BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					PrintWriter out = new PrintWriter(connection.getOutputStream(), true) )
			{
				String wiadomosc = in.readLine();
				log("otrzymałem wiadmość: " + wiadomosc);
				if (wiadomosc.equals("TRANSLATION")) {
					answer = in.readLine();
					log("otrzymałem wiadomość: " + answer);
					String odpowiedz = "TRANSLATION OK";
					log("wysyłam wiadomość: " + odpowiedz);
					out.println(odpowiedz);
					out.flush();
				}
				
			} catch (Exception ex) {
				answer = "Nieudane połączenie z serwerem języka";
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return "Błąd komunikacji z serwerem";
		}		
		return answer;
	}	
	
	public static void log(String message){
		LocalTime time = LocalTime.now();
		System.err.println("[" + time.toString() + "] Klient: "+ message);
	}
}