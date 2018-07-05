package zad1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface IBank extends Remote{
	public int createAccount() throws RemoteException;
	public int createShopAccount() throws RemoteException;
	public double getAccountBalance(int accountNumber) throws RemoteException;
	public double setAccountBalance(int accountNumber, double balanceChange) throws RemoteException;
	public Vector<Transaction> getAccountHistory(int accountNumber) throws RemoteException;
	public Bill.Status payBill(Bill bill) throws RemoteException;
}
