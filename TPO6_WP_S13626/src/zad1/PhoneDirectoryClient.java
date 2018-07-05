package zad1;

import javax.rmi.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.naming.*;

public class PhoneDirectoryClient {
	
	public static PhoneDirectoryClientGUI gui;
	
	public static void prepareGUI() {
		gui = new PhoneDirectoryClientGUI();
		gui.getButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				getNumber();
			}
		});
		gui.addButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				addNumber();
			}
		});
		gui.replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceNumber();
			}
		});
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.start();
			}
		});

	}
	
	public static void getNumber() {
		String name = gui.getName();
		String number = "";
		try {
			number = aif.getPhoneNumber(name);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		gui.displayInfo(name + " : " + number);
	}
	
	public static void addNumber() {
		String name = gui.getName();
		String number = gui.getNumber();
		if ((name.length() > 0) && (number.length() > 0))
			try {
				if (aif.addPhoneNumber(name, number))
					gui.displayInfo("Numer " + number + " dla " + name + " dodany.");
				else
					gui.displayInfo("Numer dla tej osoby jest już w książce numerów.");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		else 
			gui.displayInfo("Wpisz imię osoby i jej numer.");
	}
	
	public static void replaceNumber() {
		String name = gui.getName();
		String number = gui.getNumber();
		if ((name.length() > 0) && (number.length() > 0))
			try {
				if (aif.replacePhoneNumber(name, number))
					gui.displayInfo("Zmieniono numer " + name + " na " + number + ".");
				else
					gui.displayInfo("Nie mam tej osoby w książce numerów.");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		else 
			gui.displayInfo("Wpisz imię osoby i jej numer.");		
	}
	
	public static IPhoneDirectory aif; 
	
	public static void  main( String args[] ) {
		prepareGUI();
		try {
			Context ctx = new InitialContext();

			Object objref = ctx.lookup("PhoneDirectory");

			aif = (IPhoneDirectory) PortableRemoteObject.narrow(
					objref, IPhoneDirectory.class);

		} catch( Exception e ) {
			e.printStackTrace( );
		}
	}

}
