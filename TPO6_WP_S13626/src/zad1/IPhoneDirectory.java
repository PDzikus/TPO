package zad1;

import java.rmi.*;

public interface IPhoneDirectory extends Remote {
	public String getPhoneNumber(String name) throws RemoteException;
	public boolean addPhoneNumber(String name, String num) throws RemoteException;
	public boolean replacePhoneNumber(String name, String num) throws RemoteException;
}
