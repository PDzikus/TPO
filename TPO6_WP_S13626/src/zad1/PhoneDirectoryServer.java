package zad1;

import javax.naming.*;

public class PhoneDirectoryServer {

    public static void main(String[] args) {
        try {
            PhoneDirectory ref =  new PhoneDirectory("phoneDirectory.txt");
            System.out.println("Serwer wystartował");
            Context ctx = new InitialContext();
            ctx.rebind("PhoneDirectory", ref );

         } catch (Exception ex) {
            ex.printStackTrace();
         }
     }
}
