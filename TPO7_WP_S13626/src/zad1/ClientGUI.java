package zad1;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


public class ClientGUI extends JFrame{
	
	JTextField cash;
	JTextArea messageArea;
	JLabel accBalance;
	JTable available;
	JTable ordered;
	Vector<String> columnNames;
	JScrollPane availableScrollPane;
	HashMap <String, Product> availableProducts = new HashMap<>();
	
	public HashMap <String, Product> orderedProducts;
	public JButton transferButton;
	public JButton accountButton;
	public JButton historyButton;
	public JButton addButton;
	public JButton delButton;
	public JButton orderButton;
	public JButton refreshButton;
	
	public ClientGUI () {
		setTitle("Klient");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800,600);
		setResizable(true);
		
		setLayout(new GridBagLayout());
		GridBagConstraints position;
		
		columnNames = new Vector<>();
		columnNames.add("Towar");
		columnNames.add("Cena");
		columnNames.add("Ilość");
		Vector<Vector<String>> availableData = new Vector<>();
		Vector<Vector<String>> orderData = new Vector<>();
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 0;
		position.gridwidth = 2;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Dostępne produkty: "), position);
		
		position = new GridBagConstraints();
		position.gridx = 5;
		position.gridy = 0;
		position.gridwidth = 2;
		position.anchor = GridBagConstraints.WEST;
		add(new JLabel("Twoje zamówienie: "), position);
		
		available = new JTable(availableData, columnNames);
		availableScrollPane = new JScrollPane(available);
		availableScrollPane.setPreferredSize(new Dimension (250, 150));
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 1;
		position.gridwidth = 2;
		position.gridheight = 3;
		add(availableScrollPane, position);
		
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 2;
		add(Box.createRigidArea(new Dimension(10,5)), position);	
		
		addButton = new JButton("Dodaj >>");
		position = new GridBagConstraints();
		position.gridx = 3;
		position.gridy = 2;
		position.anchor = GridBagConstraints.CENTER;
		add(addButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 2;
		add(Box.createRigidArea(new Dimension(10,5)), position);
		
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 3;
		add(Box.createRigidArea(new Dimension(10,5)), position);
		
		delButton = new JButton("<< Usuń");
		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeFromOrder();
			}
		});
		position = new GridBagConstraints();
		position.gridx = 3;
		position.gridy = 3;
		position.anchor = GridBagConstraints.CENTER;
		add(delButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 3;
		add(Box.createRigidArea(new Dimension(10,5)), position);
		
		ordered = new JTable(orderData, columnNames);
		JScrollPane orderScrollPane = new JScrollPane(ordered);
		orderScrollPane.setPreferredSize(new Dimension (250, 150));
		position = new GridBagConstraints();
		position.gridx = 5;
		position.gridy = 1;
		position.gridwidth = 2;
		position.gridheight = 3;
		add(orderScrollPane, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 5;
		add(Box.createRigidArea(new Dimension(10,15)), position);
		
		orderButton = new JButton("Zamów");
		position = new GridBagConstraints();
		position.gridx = 6;
		position.gridy = 4;
		position.anchor = GridBagConstraints.WEST;
		add(orderButton, position);
		
		refreshButton = new JButton("Odśwież");
		position = new GridBagConstraints();
		position.gridx = 1;
		position.gridy = 4;
		position.anchor = GridBagConstraints.WEST;
		add(refreshButton, position);
		
		JPanel kontoPanel = new JPanel();
		kontoPanel.setLayout(new GridBagLayout());
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 8;
		position.gridheight = 5;
		position.gridwidth = 7;
		position.anchor = GridBagConstraints.WEST;
		add(kontoPanel, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 0;
		kontoPanel.add(Box.createRigidArea(new Dimension(10,15)), position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 1;
		position.anchor = GridBagConstraints.WEST;
		kontoPanel.add(new JLabel("Stan konta: "), position);
		
		accBalance = new JLabel("0.0");
		position = new GridBagConstraints();
		position.gridx = 1;
		position.gridy = 1;
		position.anchor = GridBagConstraints.WEST;
		accBalance.setPreferredSize(new Dimension(60,15));
		kontoPanel.add(accBalance, position);
		
		position = new GridBagConstraints();
		position.gridx = 2;
		position.gridy = 1;
		kontoPanel.add(Box.createRigidArea(new Dimension(30,15)), position);
		
		position = new GridBagConstraints();
		position.gridx = 3;
		position.gridy = 1;
		kontoPanel.add(Box.createRigidArea(new Dimension(10,15)), position);
		
		accountButton = new JButton("Potwierdź");
		position = new GridBagConstraints();
		position.gridx = 4;
		position.gridy = 1;
		position.anchor = GridBagConstraints.WEST;
		kontoPanel.add(accountButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 5;
		position.gridy = 1;
		kontoPanel.add(Box.createRigidArea(new Dimension(30,15)), position);
		
		cash = new JTextField(10);
		position = new GridBagConstraints();
		position.gridx = 6;
		position.gridy = 1;
		position.gridwidth = 2;
		position.anchor = GridBagConstraints.WEST;
		kontoPanel.add(cash, position);
		
		position = new GridBagConstraints();
		position.gridx = 8;
		position.gridy = 1;
		kontoPanel.add(Box.createRigidArea(new Dimension(10,15)), position);
		
		transferButton = new JButton("Przelej");
		position = new GridBagConstraints();
		position.gridx = 9;
		position.gridy = 1;
		position.anchor = GridBagConstraints.EAST;
		kontoPanel.add(transferButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 10;
		position.gridy = 1;
		kontoPanel.add(Box.createRigidArea(new Dimension(63,10)), position);
		
		historyButton = new JButton ("Historia konta");
		position = new GridBagConstraints();
		position.gridx = 11;
		position.gridy = 1;
		position.anchor = GridBagConstraints.EAST;
		kontoPanel.add(historyButton, position);
		
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 13;
		kontoPanel.add(Box.createRigidArea(new Dimension(10,20)), position);
		
		messageArea = new JTextArea(10, 60);
		messageArea.setLineWrap(true);
		JScrollPane messagePane = new JScrollPane(messageArea);
		position = new GridBagConstraints();
		position.gridx = 0;
		position.gridy = 14;
		position.gridwidth = 7;
		add(messagePane, position);
		
		orderedProducts = new HashMap<>();
	}
	
	public void start() {
		setVisible(true);
	}
	
	public double getCash () {
		return Double.parseDouble(cash.getText());
	}
	
	public void displayMessage(String message) {
		messageArea.append(message + "\r\n");
	}
	
	public void displayBalance(double balance) {
		accBalance.setText("" + balance);
	}
	
	public void refreshAvailable(HashMap<String, Product> data) {
		boolean changeFound = false;
		for (Entry<String, Product> entry : data.entrySet()) {
			if (!availableProducts.containsKey(entry.getKey()) ||
					(entry.getValue().getAmount() != availableProducts.get(entry.getKey()).getAmount())) {
				changeFound = true;
				availableProducts = data;
				break;
			}

		}
		if (changeFound) {
			Vector<Vector<String>> dataForJTable = new Vector<>();
			for (Product product : availableProducts.values()) {
				dataForJTable.add(product.toVectorString());
			}
			DefaultTableModel model = new DefaultTableModel(dataForJTable,columnNames);
			available.setModel(model);
			updateOrderedItems();
		}
	}
	
	public void addToOrder() {
		int[] wybrane = available.getSelectedRows();
		for (int index : wybrane) {
			if (Integer.parseInt((String) available.getValueAt(index, 2)) <= 0) {
				displayMessage("Na magazynie nie ma już towaru: " + available.getValueAt(index, 0));
				return;
			}
		}
		for (int index: wybrane) {
			String name = (String)available.getValueAt(index, 0);
			double price = Double.parseDouble((String)available.getValueAt(index, 1));
			int amount = Integer.parseInt((String)available.getValueAt(index,2));
			
			if (orderedProducts.containsKey(name)) {
				if (orderedProducts.get(name).getAmount() < amount)
					orderedProducts.get(name).adjustAmount(1);
				else
					displayMessage("Zamówiłeś wszystkie dostępne produkty: " + name);
			} else {
				orderedProducts.put(name, new Product(name, price, 1));
			}
		}
		refreshOrdered();
	}
	
	public void removeFromOrder() {
		int[] wybrane = ordered.getSelectedRows();
		Vector<Integer> doWybrania = new Vector<Integer>();
		int deleted = 0;
		for (int index : wybrane) {
			String name = (String)ordered.getValueAt(index, 0);
			int amount = Integer.parseInt((String)ordered.getValueAt(index,2));
			if (amount > 1) {
				orderedProducts.get(name).adjustAmount(-1);
				doWybrania.add(index - deleted);
				
			} else {
				orderedProducts.remove(name);
				deleted++;
			}	
		}
		refreshOrdered();
		for (int row : doWybrania) 
			ordered.getSelectionModel().addSelectionInterval(row,row);
	}
	
	public void refreshOrdered() {
		Vector<Vector<String>> data = new Vector<>(); 
		if (orderedProducts.size() > 0) {
			for (Entry<String, Product> entry : orderedProducts.entrySet()) {
				if (entry.getValue().getAmount() > 0) {
					data.add(entry.getValue().toVectorString());
				}
			}
		} 
		DefaultTableModel model = new DefaultTableModel(data,columnNames);
		ordered.setModel(model);
	}
	
	public void updateOrderedItems() {
		boolean refreshRequired = false;
		for (Entry<String, Product> entry : orderedProducts.entrySet()) {
			Product ordered = entry.getValue();
			Product available = availableProducts.get(entry.getKey());
			if (ordered.getAmount() > available.getAmount()) {
				refreshRequired = true;
				if (available.getAmount() == 0) {
					orderedProducts.remove(entry.getKey());
				} else {
					ordered.setAmount(available.getAmount());
				}
			}
		}
		if (refreshRequired)
			refreshOrdered();
	}
	
	public void confirmOrder(Order order) {
		orderedProducts = new HashMap<>();
		refreshOrdered();
		displayMessage("Zapłaciłeś za zamówienie: ");
		for (Product product : order.products.values()) {
			displayMessage(" - " + product.getName() + " : " + product.getAmount() + " szt.");
		}
		displayMessage("Łącznie zapłaciłeś " + order.price);
	}
	
}
