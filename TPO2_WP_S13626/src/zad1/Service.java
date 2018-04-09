/**
 *
 *  @author Wiszniewski Paweł S13626
 *
 */

package zad1;

import java.util.*;
import java.util.regex.*;
import org.json.*;
import java.io.*;
import java.net.*;


public class Service {
    private Locale country;

    public Service(String country) {
        
    	Map <String, Locale> countries = new HashMap<>();

        for (String iso : Locale.getISOCountries()) {
            Locale loc = new Locale("", iso);
            countries.put(loc.getDisplayCountry(), loc);
        }
        this.country = countries.get(country);
    }

    public Locale getCountry() {
    	return country;
    }
    
    public Currency getCurrency() {
    	return Currency.getInstance(country);
    }
    
    public String getWeather(String city) {
        String apiRequest = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "," + country.getDisplayCountry() + "&units=metric&lang=pl&appid=f91ab2fe37cc8b75984d514dd7781217";
        String answer = sendAPIRequest(apiRequest);
        //String answer = "{\"coord\":{\"lon\":21.01,\"lat\":52.23},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"bezchmurnie\",\"icon\":\"01d\"}],\"base\":\"stations\",\"main\":{\"temp\":4,\"pressure\":1011,\"humidity\":60,\"temp_min\":4,\"temp_max\":4},\"visibility\":10000,\"wind\":{\"speed\":1},\"clouds\":{\"all\":0},\"dt\":1521964800,\"sys\":{\"type\":1,\"id\":5374,\"message\":0.0038,\"country\":\"PL\",\"sunrise\":1521951992,\"sunset\":1521997090},\"id\":756135,\"name\":\"Warsaw\",\"cod\":200}";
        return answer;
    }
    
    public double getRateFor(String currencyCode) {
    	Currency currency = Currency.getInstance(country);
    	String apiRequest = "http://data.fixer.io/api/latest?access_key=e6e7f83a4b2faf939444e5eaf0796777&base=EUR&symbols=" + currency.getCurrencyCode() + "," + currencyCode;
        String answer = sendAPIRequest(apiRequest);
        //String answer = "{\"success\":true,\"timestamp\":1521971046,\"base\":\"EUR\",\"date\":\"2018-03-25\",\"rates\":{\"PLN\":4.230914,\"USD\":1.23624}}";
        
        JSONObject obj = new JSONObject(answer);
        boolean success = obj.getBoolean("success");
        if (success && obj.getJSONObject("rates").has(currencyCode)) {
        	double countryCurrency = obj.getJSONObject("rates").getDouble(currency.getCurrencyCode());
        	
        	double requestedCurrency = obj.getJSONObject("rates").getDouble(currencyCode);
        	return countryCurrency/requestedCurrency;
        } else {
        	return 0.0;
        }
    }

    public double getNBPRate() {
    	String apiRequest1 = "http://www.nbp.pl/kursy/kursya.html";
    	String apiRequest2 = "http://www.nbp.pl/kursy/kursyb.html";
    	String answer = "";
    	URL url;
		try {
			url = new URL(apiRequest1);
	        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringWriter sw = new StringWriter(10240);
	        String line;
	 
	        while ((line = in.readLine()) != null) {
	           sw.write(line);
	        }
	        in.close();
	        
	        url = new URL(apiRequest2);
	        in = new BufferedReader(new InputStreamReader(url.openStream()));
	        
	        while ((line = in.readLine()) != null) {
	           sw.write(line);
	        }
	        in.close();
	        answer = sw.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        Matcher matcher = Pattern.compile("<td class=\"bgt2 right\">1 (\\w\\w\\w)</td>\\s*<td class=\"bgt2 right\">(\\d*,?\\d*)</td>",
                 Pattern.CASE_INSENSITIVE).matcher(answer);
        
    
        while (matcher.find()) {
        	if (matcher.group(1).equals(getCurrency().getCurrencyCode()))
        		return Double.parseDouble(matcher.group(2).replace(',', '.'));
        }
        System.out.println();
        return 0.0;
    }
    
    public String sendHTMLRequest(String address) {
    	StringBuilder sb = new StringBuilder();
    	
    	
    	
    	return sb.toString();
    }

    public String sendAPIRequest(String request) {
        String answer = "";
        try {
            URL url = new URL(request);
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                        conn.getInputStream()));
            answer = in.readLine();
            in.close();
        } catch (IOException ex) {
            System.out.println("Problem z połączeniem: "+ ex.getMessage());
        }
        return answer;
    }
    
}  
