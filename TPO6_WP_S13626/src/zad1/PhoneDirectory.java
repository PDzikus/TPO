package zad1;

import java.util.*;
import javax.rmi.PortableRemoteObject;
import java.io.*;
import java.rmi.RemoteException;

public class PhoneDirectory extends PortableRemoteObject implements IPhoneDirectory {

  private Map<String, String> pbMap = new HashMap<>();

  public PhoneDirectory (String fileName) throws RemoteException {

    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] info = line.split(" +", 2);
        pbMap.put(info[0], info[1]);
      }
    } catch (Exception exc) {
        exc.printStackTrace();
        System.exit(1);
    }
  }

  public String getPhoneNumber(String name) throws RemoteException {
	  return pbMap.get(name);
  }

  public boolean addPhoneNumber(String name, String num) throws RemoteException{
	  if (pbMap.containsKey(name)) 
		  return false;
	  pbMap.put(name, num);
	  return true;
  }

  public boolean replacePhoneNumber(String name, String num) throws RemoteException{
	  if (!pbMap.containsKey(name)) return false;
	  pbMap.put(name, num);
	  return true;
  }

}  