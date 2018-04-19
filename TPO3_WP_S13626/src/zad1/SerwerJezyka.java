package zad1;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SerwerJezyka implements Runnable {
	private static String kodJezyka = "";
	private HashMap<String, String> slownik;
	private InetAddress listeningIP;
	private int listeningPort;
	private static ServerSocket incomingSocket = null;
	
	private static final int iloscWatkow = 100;
	private static final ExecutorService pulaWatkow = Executors.newFixedThreadPool(iloscWatkow);
	
	private SerwerJezyka(String plik, InetAddress serverIP, InetAddress serviceServerIP, int serviceServerPort ) {
		slownik = new HashMap<String, String>();		
		try ( BufferedReader file = new BufferedReader(new FileReader("./dict/" + plik)) ) {
			kodJezyka = file.readLine();
			log("wczytuję jezyk: " + kodJezyka);
			String line;
			while ( (line = file.readLine()) != null) {
				String[] definicja = line.split(":");
				slownik.put(definicja[0], definicja[1]);
				log("Definicja: " + definicja[0] + " = " + definicja[1]);
			}
		} catch (IOException ex) {
			log("błąd uruchomienia: nie mogę odczytać pliku słownikowego " + plik);
			ex.printStackTrace();
			System.exit(1);
		}
		
		try {
			incomingSocket = new ServerSocket(0);
			listeningPort = incomingSocket.getLocalPort();
			log("usługa uruchomiona, nasłuchuję na porcie " + listeningPort );
		} catch (IOException e) {
			log("nie można utworzyć gniazda serwera");
			e.printStackTrace();
			System.exit(1);
		}
		listeningIP = serverIP;
		rejestrujSerwer(serviceServerIP, serviceServerPort);
	}
	
	public void run () {
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
	
	private void rejestrujSerwer(InetAddress IP, int port) {
		try (	Socket connectionSocket = new Socket(IP,port);
				OutputStream os = connectionSocket.getOutputStream();
				PrintWriter out = new PrintWriter(os, true);
				InputStream is = connectionSocket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is)); )
		{ 
			log("rejestracja na serwerze usługi: REGISTER");

			out.println("REGISTER");
			out.println(listeningIP.getHostAddress());
			out.println(listeningPort);
			out.println(kodJezyka);
			out.flush();
			
			String response = br.readLine();
			log("otrzymałem potwierdzenie: " + response);

			if (!response.equals("REGISTER OK")) {
				log("operacja REGISTER nie udana");
				closeServer();
				System.exit(1);
			}
			
		} catch (Exception ex) {
			log("problem z połączeniem");
			closeServer();
			System.exit(1);
		} 
	}
	
	private void obsluzPolaczenie(Socket connection) {
		String wiadomosc;
		String word = "";
		String hostAdr = "";
		int hostPort = 0;
		
		try (	BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				PrintWriter out = new PrintWriter(connection.getOutputStream(), true) )
		{	
			wiadomosc = in.readLine();
			log("otrzymałem wiadomość: " + wiadomosc);

			if (wiadomosc.equals("SEND_TRANSLATION")) {
				log("przetwarzam SEND_TRANSLATION");
				word = in.readLine();
				log("otrzymałem wiadomość: " + word);
				hostAdr = in.readLine();
				log("otrzymałem wiadomość: " + hostAdr);
				hostPort = Integer.parseInt(in.readLine());
				log("otrzymałem wiadomość: " + hostPort);

				String odpowiedz = "SEND_TRANSLATION OK";
				out.println(odpowiedz);
				out.flush();

				log("wiadomość wysłana: " + odpowiedz);
			}
			log("zakończono przetwarzanie żądania klienta");
		} catch (Exception ex) {
			log("Nie udało się odpowiedzieć na żądanie klienta" + ex.getMessage());		
		}	
		
		try {
			connection.close();
		} catch (IOException e) {
			log("problem przy zamykaniu przychodzącego połączenia");
			e.printStackTrace();
		}
		
		if (word != "") {
			sendTranslationToClient(word, hostAdr, hostPort);
		}
	}
		
	private void sendTranslationToClient(String word, String hostAdr, int port) {
		InetAddress clientIP;
		try {
			clientIP = InetAddress.getByName(hostAdr);
		} catch (UnknownHostException e) {
			log("Nie mogę rozszyfrować adres IP klienta");
			return;
		}
		
		try (	Socket connectionSocket = new Socket(clientIP,port);
				OutputStream os = connectionSocket.getOutputStream();
				PrintWriter out = new PrintWriter(os, true);
				InputStream is = connectionSocket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is)); )
		{ 
			String translation = slownik.get(word);
			
			log("wysyłam wiadomość: TRANSLATION");

			out.println("TRANSLATION");
			if (translation != null) {
				out.println(translation);
				log("wysyłam wiadomość: " + translation);
			} else {
				log("wysyłam wiadomość: Słowa nie ma w słowniku");
				out.println("Słowa nie ma w słowniku");
			}
			out.flush();
			
			String response = br.readLine();
			log("otrzymałem potwierdzenie: " + response);

			if (!response.equals("TRANSLATION OK")) {
				log("operacja TRANSLATION nieudana");
			}
			
		} catch (Exception ex) {
			log("problem z połączeniem z klientem (sendTranslationToClient)");
		} 	
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
	
	public static void log(String message){
		LocalTime time = LocalTime.now();
		System.err.println("[" + time.toString() + "] Language[" + kodJezyka + "]: "+ message);
	}
	
	public static void main (String[] args) {
		InetAddress localServiceIP = null;
		InetAddress remoteServiceIP = null;
		try {
			localServiceIP = InetAddress.getByName("127.0.0.1");
			remoteServiceIP = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		if (args.length < 1) {
			System.out.println("Brak argumentu: nazwa pliku słownika!");
			System.out.println("Poprawne wywołanie: SerwerJezyka plikSlownika");
			System.exit(1);
		}
		String languageFile = args[0];
		File f =  new File("./dict/" + languageFile);
		if ( ! f.isFile() ) {
			System.out.println("Pierwszy argument: nazwa pliku słownika. Podany plik nie istnieje");
			System.exit(2);
		}
		 
		SerwerJezyka server = new SerwerJezyka(languageFile, localServiceIP, remoteServiceIP, 9999 );
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
