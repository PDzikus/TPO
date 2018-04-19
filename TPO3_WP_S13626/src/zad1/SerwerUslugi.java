package zad1;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SerwerUslugi implements Runnable{
	private int listeningPort;
	private static ServerSocket incomingSocket = null;
	private HashMap<String, LanguageServer> languageServers = new HashMap<String, LanguageServer>();
	private static final int iloscWatkow = 100;
	private static final ExecutorService pulaWatkow = Executors.newFixedThreadPool(iloscWatkow);
	
	class LanguageServer {
		public InetAddress address;
		public int port;
		public String kodJezyka;
		
		public LanguageServer(InetAddress address, int port, String kod) {
			this.address = address;
			this.port = port;
			this.kodJezyka = kod;
		}
	}
	
	class TranslationRequest {
		public InetAddress address;
		public int port;
		public String language;
		public String word;
		
		public TranslationRequest(InetAddress adres, int port, String lang, String word) {
			this.address = adres;
			this.port = port;
			this.language = lang;
			this.word = word;
		}
		
	}
	
	private SerwerUslugi (int port) {
		listeningPort = port;	
	}
	
	public void run () {
		try {
			incomingSocket = new ServerSocket(listeningPort);	
			log("usługa uruchomiona, nasłuchuję");
		} catch (Exception ex) {
			log("nie można utworzyć gniazda serwera");
			return;
		}
		
		while (!incomingSocket.isClosed()) {
			try {
				Socket connection = incomingSocket.accept();
				Runnable task = new Runnable()
				{
					@Override
					public void run()
					{
						log("nowe połączenie przychodzące");
						obsluzPolaczenie(connection);
					}	
				};
				pulaWatkow.execute(task);
			} catch (SocketException ex) {
				log("gniazdo nasłuch zamknięte");
			} catch (IOException ex) {
				log("problem z przychodzącym połączeniem");
			}
		}
	}
	
	private void obsluzPolaczenie(Socket connection) {
		String wiadomosc;
		TranslationRequest request = null;
		try (	BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				PrintWriter out = new PrintWriter(connection.getOutputStream(), true);)
		{
			wiadomosc = in.readLine();
			log("otrzymałem wiadomość: " + wiadomosc);
			
			if (wiadomosc.equals("TRANSLATE")) {
				request = request_TRANSLATE(in, out);
			}				
			else
				if (wiadomosc.equals("REGISTER"))
					request_REGISTER(in, out);	
		} catch (Exception ex) {
			log("Failed to respond to client request" + ex.getMessage());		
		} 
		
		try {
			connection.close();
		} catch (IOException e) {
			log("problem przy zamykaniu przychodzącego połączenia");
		}
		
		if (request != null) {
			sendToLangServer(request);
		}
		log("zakończyłem obsługę żądania klienta");
	}
	
	private TranslationRequest request_TRANSLATE(BufferedReader in, PrintWriter out) {
		TranslationRequest request = null;
		try {
			log("przetwarzam TRANSLATE");
			String language = in.readLine();
			log("otrzymana wiadomość: " + language);
			String word = in.readLine();
			log("otrzymana wiadomość: " + word);
			String clientIP = in.readLine();
			log("otrzymana wiadomość: " + clientIP);
			int clientPort = Integer.parseInt(in.readLine());
			log("otrzymana wiadomość: " + clientPort);
			
			String odpowiedz = "";
			if (languageServers.containsKey(language)) {
				odpowiedz = "TRANSLATE OK";
				request = new TranslationRequest(InetAddress.getByName(clientIP), clientPort, language, word);
			} else {
				odpowiedz = "TRANSLATE LANGUAGE_UNKNOWN";
				request = null;
			}
			out.println(odpowiedz);
			out.flush();

			log("wysłana wiadomość: " + odpowiedz);
			
		} catch (Exception ex) {
			log("Nieudane obsługa komunikatu TRANSLATE: " + ex.getMessage());
		}
		return request;
	}
	
	private void sendToLangServer(TranslationRequest request) {
		LanguageServer ls = languageServers.get(request.language);
		
		try (Socket connectionSocket = new Socket(ls.address,ls.port);
			OutputStream os = connectionSocket.getOutputStream();
			PrintWriter out = new PrintWriter(os, true);
			InputStream is = connectionSocket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));)
		{
			log("przesyłanie zapytania : SEND_TRANSLATION");
			
			out.println("SEND_TRANSLATION");
			out.println(request.word);
			out.println(request.address.getHostAddress());
			out.println(request.port);
			out.flush();
			
			String response = br.readLine();
			log("otrzymałem potwierdzenie: " + response);
			
			if (!response.equals("SEND_TRANSLATION OK")) {
				log("operacja SEND_TRANSLATION nie udana");
			}
		} catch (Exception ex) {
			log("problem z połączeniem z serwerem języka " + ls.kodJezyka);
		} 
	}
	
	private void request_REGISTER(BufferedReader in, PrintWriter out) {
		try {
			log("przetwarzam REGISTER");
			String lsAddress = in.readLine();
			log("otrzymana wiadomość: " + lsAddress);
			int lsPort = Integer.parseInt(in.readLine());
			log("otrzymana wiadomość: " + lsPort);
			String languageCode = in.readLine();
			log("otrzymana wiadomość: " + languageCode);
			
			InetAddress lsIP = null;
			try {
				lsIP = InetAddress.getByName(lsAddress);
			} catch (Exception ex) {
				log("nie mogę przetworzyć otrzymanego adresu IP");
			}
			
			String answer = "";
			if (lsIP != null) {
				answer = "REGISTER OK";
				LanguageServer ls = new LanguageServer(lsIP, lsPort, languageCode);
				languageServers.put(languageCode, ls );
			} else {
				answer = "REGISTER ERROR: IncorrectIP";
			}
			out.println(answer);
			out.flush();

			log("wysłana wiadomość: " + answer);
		} catch (Exception ex) {
			log("Nieudane obsługa komunikatu REGISTER: " + ex.getMessage());	
		}	
	}
	
	public static void log(String message){
		LocalTime time = LocalTime.now();
		System.err.println("[" + time.toString() + "] ServiceServer: "+ message);
	}
	
	public static void closeServer () {
		try {
			log("zamykam gniazdo nasłuchu");
			if (incomingSocket != null) {
				incomingSocket.close();
			}
			pulaWatkow.shutdownNow();
			pulaWatkow.awaitTermination(100, TimeUnit.MICROSECONDS);
		} catch (IOException | InterruptedException e) {
			log("problem z zamknięciem gniazda nasłuchu");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		SerwerUslugi server = new SerwerUslugi(9999);
		Thread serverDaemon = new Thread(server);
		serverDaemon.start();	
		
		System.out.println("Naciśnij ENTER w celu zamknięcia usługi");
		try
        {
            System.in.read();
        }  
        catch(Exception e)
        {}  
		closeServer();
		log("usługa zamknięta");
	}
	
}
