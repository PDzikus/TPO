package zad1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Vector;

import javax.rmi.PortableRemoteObject;

public class Shop extends PortableRemoteObject implements IShop {
	
	private HashMap<String, Product> products = new HashMap<>(); 
	private IBank bank;
	private int accountId;
	private HashMap<Integer, Order> placedOrders;
	private int nextOrderId;
	
	public Shop(String fileName, IBank bank) throws RemoteException {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] info = line.split("::");

				String name = info[0];
				double price = Double.parseDouble(info[1]);
				int amount = Integer.parseInt(info[2]);

				products.put(name, new Product(name, price, amount));
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			System.exit(1);
		}
		this.bank = bank;
		placedOrders = new HashMap<>();
	}
	
	public void openBankAccount() {
		try {
			accountId = this.bank.createShopAccount();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public HashMap<String, Product> getProductList() throws RemoteException {
		return new HashMap<String, Product>(products);
	}
	
	private boolean isOrderValid(Order order) {
		HashMap<String, Product> orderedProducts = order.products;
		for (Entry<String, Product> entry : orderedProducts.entrySet()) {
			if (! products.containsKey(entry.getKey()))
				return false;
			if (entry.getValue().getAmount() > products.get(entry.getKey()).getAmount())
				return false;
		}
		return true;
	}
	
	public Bill placeOrder(Order order) throws RemoteException {
		if (!isOrderValid(order))
			return null;
		placedOrders.put(nextOrderId, order);
		Bill bill = new Bill(order.clientId, accountId, nextOrderId, order.price );
		System.out.println("Otrzymałem zlecenie od klienta " + order.clientId + " z kosztem " + order.price);
		System.out.println("Wystawiłem rachunek");
		return bill;
	}

	@Override
	public void confirmBill(Bill bill) throws RemoteException {
		Order order = placedOrders.get(bill.orderId);
		placedOrders.remove(bill.orderId);
		for (Entry<String, Product> orderEntry : order.products.entrySet()) {
			Product product = products.get(orderEntry.getKey());
			product.adjustAmount( -orderEntry.getValue().getAmount());
		}
	}
}
