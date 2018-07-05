package zad1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JButton;

import zad1.Bill.Status;

public class Client {
	
	private static IShop shop; 
	private static IBank bank;
	private ClientGUI gui;
	private int accountId;
	
	public Client() throws RemoteException {
		this.accountId = bank.createAccount();
	}
	
	public void prepareGUI() {
		gui = new ClientGUI();
		
		gui.accountButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				confirmBalance();
			}
		});
		gui.transferButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				transferMoney();
			}
		});
		gui.historyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayHistory();
			}
		});
		
		gui.refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshProducts();
			}
		});
		
		gui.orderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				placeOrder();
			}
		});
		
		gui.addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshProducts();
				gui.addToOrder();
			}
		});
		
		refreshProducts();
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.start();
			}
		});
	}
	
	public void placeOrder() {
		Order order = new Order(accountId, gui.orderedProducts);
		Bill.Status paymentStatus = null;
		try {
			Bill bill = shop.placeOrder(order);
			if (bill == null) {
				gui.displayMessage("Sklep nie ma na stanie wszystkich zamówionych towarów");
				refreshProducts();
				gui.updateOrderedItems();
				return;
			}
			paymentStatus = bank.payBill(bill);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (paymentStatus == Status.TOO_LOW_BALANCE)
			gui.displayMessage("Nie masz pieniędzy na złożenie tego zamówienia. Koszt zamówienia to " + order.price);
		else {
			refreshProducts();
			confirmBalance();
			gui.confirmOrder(order);
		}
	}
	
	public void refreshProducts() {
		HashMap<String, Product> availableProducts = null;		
		boolean changeFound = false;
		try {
			availableProducts = shop.getProductList();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		gui.refreshAvailable(availableProducts);
	}
	
	public void transferMoney() {
		double balanceChange = gui.getCash();
		double finalBalance = 0;
		try {
			finalBalance = bank.setAccountBalance(accountId, balanceChange);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (finalBalance < 0 )
			gui.displayMessage("Brak środków na koncie");
		confirmBalance();
	}
	
	public void confirmBalance() {
		double balance = 0;
		try {
			balance = bank.getAccountBalance(accountId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		gui.displayBalance(balance);
	}
	
	public void displayHistory() {
		Vector<Transaction> history = null;
		try {
			history = bank.getAccountHistory(accountId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (history == null) {
			gui.displayMessage("Błąd pobierania historii konta");
			return;
		}
		for (Transaction transaction : history)
			gui.displayMessage(transaction.toString());
	}
	
	public static void main (String[] args) {
		try {
			Context ctx = new InitialContext();

			Object shopref = ctx.lookup("Shop");
			Object bankref = ctx.lookup("Bank");
			shop = (IShop) PortableRemoteObject.narrow(shopref, IShop.class);
			bank = (IBank) PortableRemoteObject.narrow(bankref, IBank.class);

		} catch( Exception e ) {
			e.printStackTrace( );
		}

		Client klient = null;
		try {
			klient = new Client();
		} catch (RemoteException e) {
			System.out.println("Nie mogę założyć konta w banku!");
			System.exit(1);
		}
		klient.prepareGUI();
	}
}
