package zad1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

public interface IShop extends Remote{
	public HashMap<String, Product> getProductList() throws RemoteException;
	public Bill placeOrder(Order order) throws RemoteException;
	public void confirmBill(Bill bill) throws RemoteException;
}
