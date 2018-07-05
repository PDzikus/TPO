package zad1;

import java.io.Serializable;
import java.util.Vector;

public class Product implements Serializable{
	
	private static final long serialVersionUID = 3263396895170944958L;
	private double price;
	private int amount;
	private String name;
	
	public Product(String name, double price, int amount) {
		this.name = name;
		this.price = price;
		this.amount = amount;
	}
	
	/**
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}
	/**
	 * @param price the price to set
	 */
	public void setPrice(int price) {
		this.price = price;
	}
	
	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}
	/**
	 * @param amount the amount to set
	 */
	
	public void setAmount(int amount) {
		if (amount >= 0) {
			this.amount = amount;
		}
	}
	
	public void adjustAmount(int amountChange) {
		if (amount + amountChange < 0)
			return;
		amount += amountChange;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public Vector<String> toVectorString()  {
		Vector<String> vector = new Vector<>();
		vector.add(name);
		vector.add(""+price);
		vector.add("" + amount);
		return vector;
	}
	
}
