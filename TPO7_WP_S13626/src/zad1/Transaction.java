package zad1;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable{
	private static final long serialVersionUID = 233029164619037596L;
	int accountNumber;
	LocalDateTime transactionTime;
	double balanceChange;
	double finalBalance;
	Type type;
	int destinationAccountNumber;
	
	public enum Type {
		CREATION,
		DEPOSIT,
		WITHDRAW,
		TRANSFER_TO,
		TRANSFER_FROM
	}
	
	private Transaction (int accNumber, int destAccount, double balanceChange, double finalBalance, Type type) {
		this.type = type;
		this.accountNumber = accNumber;
		this.destinationAccountNumber = destAccount;
		transactionTime = LocalDateTime.now();
		this.balanceChange = balanceChange;
		this.finalBalance = finalBalance;
	}
	
	public static Transaction CreationTransaction(int accNumber) {
		return new Transaction(accNumber, 0, 0, 0, Type.CREATION);
	}
	
	public static Transaction DepositTransaction(int accNumber, double balanceChange, double finalBalance) {
		return new Transaction(accNumber, 0, balanceChange, finalBalance, Type.DEPOSIT);
	}
	
	public static Transaction WithdrawTransaction(int accNumber, double balanceChange, double finalBalance) {
		return new Transaction(accNumber, 0, balanceChange, finalBalance, Type.WITHDRAW);
	}
	
	public static Transaction TransferToTransaction(int accNumber, int destAccount, double balanceChange, double finalBalance) {
		return new Transaction(accNumber, destAccount, balanceChange, finalBalance, Type.TRANSFER_TO);
	}
	
	public static Transaction TransferFromTransaction(int accNumber, int fromAccount, double balanceChange, double finalBalance) {
		return new Transaction(accNumber, fromAccount, balanceChange, finalBalance, Type.TRANSFER_FROM);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + transactionTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))+ "] ");
		switch (type) {
			case CREATION : sb.append ("Utworzono konto.");
							break;
			case DEPOSIT : sb.append ("Zdeponowano "+ balanceChange);
							break;
			case WITHDRAW : sb.append ("Wypłacono " + balanceChange);
							break;
			case TRANSFER_TO: sb.append("Wykonano transfer " + balanceChange + " na konto " + destinationAccountNumber);
							break;
			case TRANSFER_FROM: sb.append("Otrzymano transfer " + balanceChange + "z konta " + destinationAccountNumber);
		}
		return sb.toString();
	}
	
}
