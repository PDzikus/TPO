package zad1;

import java.rmi.RemoteException;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

public class Bank extends PortableRemoteObject implements IBank {
	
	class Account {
		int id;
		double balance;
		Vector<Transaction> history;
		
		public Account(int id) {
			this.id = id;
			balance = 0;
			history = new Vector<>();
			history.add(Transaction.CreationTransaction(id));
		}
	}
	
	public Vector<Account> accounts = new Vector<>();
	public static int lastAccount = 0;
	private static IShop shop;
	
	
	public Bank() throws RemoteException {
		// creation of a bank account
		accounts.add(new Account(lastAccount));
	}
	
	@Override
	public int createAccount() throws RemoteException {
		lastAccount++;
		accounts.add(new Account(lastAccount));
		System.out.println("Account created: " + lastAccount);
		return lastAccount;
	}

	@Override
	public double getAccountBalance(int accountNumber) throws RemoteException {
		return accounts.get(accountNumber).balance;
	}

	@Override
	public double setAccountBalance(int accountNumber, double balanceChange) throws RemoteException {
		Account account = accounts.get(accountNumber);
		double currentBalance = account.balance;
		if (currentBalance + balanceChange < 0) 
			return -1;
		
		account.balance += balanceChange;
		if (balanceChange >= 0) {
			account.history.add(Transaction.DepositTransaction(account.id, balanceChange, account.balance));
			return account.balance;
		} else {
			account.history.add(Transaction.WithdrawTransaction(account.id, balanceChange, account.balance));
		}
		return account.balance;
	}

	@Override
	public Vector<Transaction> getAccountHistory(int accountNumber) throws RemoteException {
		return new Vector<Transaction>(accounts.get(accountNumber).history);
	}

	@Override
	public Bill.Status payBill(Bill bill) throws RemoteException {
		Account clientAccount = accounts.get(bill.clientAccount);
		if (clientAccount.balance < bill.amount) {
			return Bill.Status.TOO_LOW_BALANCE;
		}
		Account shopAccount = accounts.get(bill.shopAccount);
		clientAccount.balance -= bill.amount;
		clientAccount.history.add(Transaction.TransferToTransaction(clientAccount.id, shopAccount.id, bill.amount, clientAccount.balance));
		shopAccount.balance += bill.amount;
		shopAccount.history.add(Transaction.TransferFromTransaction(shopAccount.id, clientAccount.id, bill.amount, shopAccount.balance));
		shop.confirmBill(bill);
		return Bill.Status.PAYED;
	}

	@Override
	public int createShopAccount() throws RemoteException {
		int shopAccountId = createAccount();
		try {
			Context ctx = new InitialContext();
			Object shopref = ctx.lookup("Shop");
			shop = (IShop) PortableRemoteObject.narrow(shopref, IShop.class);
		} catch( Exception e ) {
			e.printStackTrace( );
		}
		return shopAccountId;
	}

}
