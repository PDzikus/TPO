package zad1;

import javax.naming.Context;
import javax.naming.InitialContext;

public class BankServer {
	
    public static void main(String[] args) {
        try {
            Bank bank =  new Bank();
            Context ctx = new InitialContext();
            ctx.rebind("Bank", bank );
            System.out.println("Bank rozpoczął obsługę klientów");
        } catch (Exception ex) {
            ex.printStackTrace();
         }
     }
}
