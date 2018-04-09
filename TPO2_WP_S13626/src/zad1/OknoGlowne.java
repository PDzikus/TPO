package zad1;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import org.json.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.*;

@SuppressWarnings("serial")
public class OknoGlowne extends JFrame {
	private Service target;
	private String [] countries;
	private JLabel opisPogody;
	private JLabel opisWaluty;
	private JLabel kursNBP;
	private JTextField walutaDocelowa;
	private JTextField miasto;
	private JLabel statusLabel;
	private WebEngine webEngine;
	
	public OknoGlowne() {
		this.setSize(800,600);
		target = new Service("Poland");
		populateCountries();
		
		setLayout(new BorderLayout());
		
		JComboBox<String> countryChoice = setCountriesForChoice();
		
		opisPogody = new JLabel("Jeszcze nie wiem.");
		opisWaluty = new JLabel("Podaj walutę do przeliczenia");
		kursNBP = new JLabel("kurs NBP: ");
				
		JButton przyciskPogody = setWeatherButton();
		JButton przyciskWaluty = setCurrencyButton();
		JButton przyciskBrowserLoad = setBrowserLoadButton();
		
		walutaDocelowa = new JTextField(3);
		miasto = new JTextField(20);
		
		JPanel statusBar = setStatusBar("OK");
		//JFXPanel webPanel = new JFXPanel();
		final JFXPanel webPanel = new JFXPanel();
		Platform.runLater( () -> setupBrowser(webPanel));
		JPanel navigation = new JPanel(new GridBagLayout());
		GridBagConstraints position;
		
		// ustawienie głównego okienka
		add(statusBar, BorderLayout.SOUTH);
		add(webPanel, BorderLayout.CENTER);
		add(navigation, BorderLayout.NORTH);
		
		// ustawienie panelu navigation:
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 0;
		navigation.add(Box.createRigidArea(new Dimension(10,10)), position);
		
		// Państwo - labelMiasto - Miasto
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 1;
		position.gridwidth = 2;
		navigation.add(countryChoice, position);
		
		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 1;
		navigation.add(new JLabel("Miasto: "), position);
		
		position = new GridBagConstraints();
		position.gridx = 5;
		position.gridy = 1;
		navigation.add(miasto, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 2;
		navigation.add(Box.createRigidArea(new Dimension(10,10)), position);
		
		// przycisk Pogody i opis pogody
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 3;
		position.anchor = GridBagConstraints.WEST;
		navigation.add(przyciskPogody, position);
		
		position = new GridBagConstraints();
		position.gridx = 1;
		position.gridy = 3;
		position.gridwidth = 5;
		navigation.add(opisPogody, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 4;
		navigation.add(Box.createRigidArea(new Dimension(10,10)), position);
		
		// wybór waluty to przeliczenia
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 5;
		position.gridwidth = 3;
		position.anchor = GridBagConstraints.WEST;
		navigation.add(new JLabel("Symbol waluty do przeliczenia: "),position);
		
		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 5;
		position.anchor = GridBagConstraints.WEST;
		navigation.add(walutaDocelowa, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 6;
		navigation.add(Box.createRigidArea(new Dimension(10,10)), position);
		
		// przycisk przeliczania waluty i informacje o walucie
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 7;
		position.anchor = GridBagConstraints.WEST;
		navigation.add(przyciskWaluty, position);
		
		position = new GridBagConstraints();
		position.gridx = 1;
		position.gridy = 7;
		position.gridwidth = 4;
		navigation.add(opisWaluty, position);
		
		position = new GridBagConstraints();
		position.gridx = 5;
		position.gridy = 7;
		position.gridwidth = 2;
		position.anchor = GridBagConstraints.EAST;
		navigation.add(kursNBP, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 8;
		navigation.add(Box.createRigidArea(new Dimension(10,10)), position);
		
		// przycisk wikipedii
		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 9;
		position.gridwidth = 1;
		navigation.add(przyciskBrowserLoad, position);
		
		this.setVisible(true);
	}

	public static void start() {
		@SuppressWarnings("unused")
		OknoGlowne okno = new OknoGlowne();	
	}
	
	private void populateCountries() {
		countries = Locale.getISOCountries();
		Locale loc;
		for (int idx = 0; idx < countries.length; idx++) {
            loc = new Locale("", countries[idx]);
            countries[idx] = loc.getDisplayCountry();
        }
	}
	
	private void resetValuesToDefault() {
		SwingUtilities.invokeLater(() -> opisPogody.setText("Nieznana lokacja, podaj miasto"));
		
	}
	
	private JComboBox<String> setCountriesForChoice() {
		JComboBox<String> countriesCombo = new JComboBox<String>();
		for (String s : countries) 
			countriesCombo.addItem(s);
		countriesCombo.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				target = new Service((String)countriesCombo.getSelectedItem());
				resetValuesToDefault();
			}
		});
		countriesCombo.setSelectedItem("Poland");
		countriesCombo.setPreferredSize(new Dimension(200, 20));
		return countriesCombo;
	}

	private JButton setWeatherButton() {
		JButton b1 = new JButton ("Pogoda");
		b1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
		        JSONObject obj = new JSONObject(target.getWeather(miasto.getText()));
		        String opis = obj.getJSONArray("weather").getJSONObject(0).getString("description");
		        Integer temp = obj.getJSONObject("main").getInt("temp");
		        Double pressure = obj.getJSONObject("main").getDouble("pressure");
		        Double humidity = obj.getJSONObject("main").getDouble("humidity");
		        SwingUtilities.invokeLater(() -> {	opisPogody.setText("Pogoda: " + opis + ". Temp: "+temp+" C, ciśnienie: " + pressure + ", wilgotność: " + humidity);
		        									statusLabel.setText("OK");
		        });
			}
		});
		return b1;
	}
	
	private JButton setCurrencyButton() {
		JButton b1 = new JButton ("Waluta");
		b1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String wd = walutaDocelowa.getText();
				DecimalFormat df = new DecimalFormat();
				df.setMaximumFractionDigits(3);
				
				if (wd.length() != 3) 
					SwingUtilities.invokeLater(() -> statusLabel.setText("Nieprawidłowa waluta, musi mieć 3 znaki"));
				else {
					Double rate = target.getRateFor(wd);
					if (rate > 0) 
						SwingUtilities.invokeLater(() -> { 	opisWaluty.setText("" + df.format(target.getRateFor(wd)) + " " + target.getCurrency().getCurrencyCode() + "/" +wd);
															statusLabel.setText("OK");
						});
					else
						SwingUtilities.invokeLater(() -> { 	opisWaluty.setText("Nie znaleziono waluty o kodzie " + wd);
															statusLabel.setText("Nieznany kod waluty " + wd);
						});
				}
				
				if (target.getCurrency().getCurrencyCode().equals("PLN")) 
					SwingUtilities.invokeLater(() -> { 	kursNBP.setText("Kurs NBP: 1 PLN");
														statusLabel.setText("OK");	
					});
				else {
					double kursWalutyNBP = target.getNBPRate();
					if (kursWalutyNBP > 0) 
						SwingUtilities.invokeLater(() -> { 	kursNBP.setText("Kurs NBP: " + df.format(kursWalutyNBP) + " PLN/" + target.getCurrency().getCurrencyCode());
															statusLabel.setText("OK");
						});
					else
						SwingUtilities.invokeLater(() -> { 	kursNBP.setText("NBP nie obsługuje tej waluty");
															statusLabel.setText("NBP nie obsługuje tej waluty dla kraju " + target.getCountry().getDisplayCountry());
						});
				}				
			}
		});
		return b1;
	}
	
	private JButton setBrowserLoadButton() {
		JButton b1 = new JButton("Wikipedia");
		b1.addActionListener((e) -> requestWikiPage());
		return b1;
	}
	
	
	private JPanel setStatusBar(String status) {
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setPreferredSize(new Dimension(this.getWidth(), 16));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel(status);
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);
		return statusPanel;
	}
	
	
	private JFXPanel setupBrowser(JFXPanel panel) {
		WebView browser = new WebView();
		Scene scena = new Scene(browser);
		panel.setScene(scena);
		webEngine = browser.getEngine();
		return panel;
	}
	
	private void requestWikiPage() {
		if (miasto.getText() != null) {
			String request = "https://en.wikipedia.org/wiki/" + miasto.getText();
			Platform.runLater(() -> webEngine.load(request));
		} else {
			Platform.runLater(() -> statusLabel.setText("Wpisz nazwę miasta"));
		}
	}
}
