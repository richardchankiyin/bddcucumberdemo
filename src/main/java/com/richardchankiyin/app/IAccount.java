package com.richardchankiyin.app;

import java.util.List;

public interface IAccount {
	
	/**
	 * Personalid must be unique and will return 
	 * account no which is unique.
	 * 
	 * Will throw exception if personal id found
	 * @param personalId
	 * @return account no created
	 */
	public String createAccount(String personalId);
	
	/**
	 * deposit amount for a particular account no.
	 * Will reject if amount <= 0
	 * @param accountno
	 * @param amt
	 */
	public void deposit(String accountno, double amt);
	
	/**
	 * withdraw amount for a particular account no, will
	 * reject if balance is insufficient
	 * @param accountno
	 * @param amt
	 */
	public void withdraw(String accountno, double amt);
	
	/**
	 * enquire balance for a particular accountno
	 * @param accountno
	 * @return account balance
	 */
	public double enquirebalance(String accountno);
	
	/**
	 * get last n transactions.
	 * n >= 1, if n <= 0 or n > 10; will reject
	 * @param accountno
	 * @param n
	 * @return list of transactions
	 */
	public List<Transaction> listLastNTransactions(String accountno, int n);
}
