package zad1;

import java.io.Serializable;

public class Bill implements Serializable{
	private static final long serialVersionUID = -850215551076389115L;
	public int orderId;
	public double amount;
	public int shopAccount;
	public int clientAccount;
	public Status status;
	
	public enum Status {
		NEW,
		PAYED,
		TOO_LOW_BALANCE
	}
	
	public Bill(int buyerId, int sellerId, int orderId, double amount) {
		this.status = Status.NEW;
		clientAccount = buyerId;
		shopAccount = sellerId;
		this.amount = amount;
		this.orderId = orderId;
	}
}
