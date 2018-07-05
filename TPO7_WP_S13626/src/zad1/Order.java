package zad1;

import java.io.Serializable;
import java.util.HashMap;

public class Order implements Serializable{
	private static final long serialVersionUID = -5455567629706066101L;
	int clientId;
	int price;
	HashMap<String, Product> products = new HashMap<>();
	
	public Order (int clientId, HashMap<String, Product> products) {
		this.clientId = clientId;
		this.price = 0;
		for (Product product : products.values()) {
			this.price += product.getAmount() * product.getPrice();
		}
		this.products = new HashMap<>(products);
	}
}
