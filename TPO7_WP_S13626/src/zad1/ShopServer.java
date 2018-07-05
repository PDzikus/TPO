package zad1;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

public class ShopServer {
	
	public static void main(String[] args) {
		try {
			Context ctx = new InitialContext();
			
			Object bankref = ctx.lookup("Bank");
			IBank bank = (IBank) PortableRemoteObject.narrow(bankref, IBank.class);
			
			Shop shopref =  new Shop("products.txt", bank);
			ctx.rebind("Shop", shopref );
			System.out.println("Sklep jest otwarty.");
			shopref.openBankAccount();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
