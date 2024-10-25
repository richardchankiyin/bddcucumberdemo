package com.richardchankiyin.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Account implements IAccount {

	private Map<String,String> personalIdAccountMap;
	private Map<String,BigDecimal> accountBalance;
	private Map<String,List<Transaction>> accountTransactions;
	private static final int MAX_LAST_LIST_N_VAL = 10;
	
	public Account() {
		personalIdAccountMap = new ConcurrentHashMap<>();
		accountBalance = new ConcurrentHashMap<>();
		accountTransactions = new ConcurrentHashMap<>();
	}

	private boolean isPersonalIdFound(String personalId) {
		return personalIdAccountMap.containsKey(personalId);
	}
	
	private String generateAccountId(String personalId) {
		return UUID.randomUUID().toString();
	}
	
	private void validateAccount(String accountno) {
		if (!accountBalance.containsKey(accountno) || !accountTransactions.containsKey(accountno)) {
			throw new AccountException("Invalid Account");
		}
	}
	
	private void validateAmount(double amt ) {
		if (amt <= 0) {
			throw new AccountException("Invalid Amount");
		}
	}
	
	@Override
	public String createAccount(String personalId) {
		String personalIdIntern = personalId.intern();
		String accountno = "";
		synchronized(personalIdIntern) {
			if (isPersonalIdFound(personalIdIntern)) {
				throw new AccountException("Personal Id duplicated");
			}
			accountno = generateAccountId(personalIdIntern);
			personalIdAccountMap.put(personalIdIntern, accountno);
			accountBalance.put(accountno, BigDecimal.ZERO);
			accountTransactions.put(accountno, new ArrayList<>());
		}
		
		return accountno;
	}

	@Override
	public void deposit(String accountno, double amt) {
		validateAccount(accountno);
		validateAmount(amt);
		String accountnoIntern = accountno.intern();
		synchronized(accountnoIntern) {
			BigDecimal balance = accountBalance.get(accountnoIntern);
			balance = balance.add(BigDecimal.valueOf(amt));
			Transaction txn = new Transaction(accountnoIntern, true, amt);
			accountBalance.put(accountno,balance);
			accountTransactions.get(accountnoIntern).add(txn);
		}
		
	}

	@Override
	public void withdraw(String accountno, double amt) {
		validateAccount(accountno);
		validateAmount(amt);
		String accountnoIntern = accountno.intern();
		synchronized(accountnoIntern) {
			BigDecimal balance = accountBalance.get(accountnoIntern);
			BigDecimal amtBD = BigDecimal.valueOf(amt);
			if (balance.compareTo(amtBD) < 0) {
				throw new AccountException("Insufficient Balance");
			}
			balance = balance.subtract(amtBD);
			Transaction txn = new Transaction(accountnoIntern, false, amt);
			accountBalance.put(accountno,balance);
			accountTransactions.get(accountnoIntern).add(txn);
		}
	}

	@Override
	public double enquirebalance(String accountno) {
		validateAccount(accountno);
		return accountBalance.get(accountno).doubleValue();
	}

	@Override
	public List<Transaction> listLastNTransactions(String accountno, int n) {
		validateAccount(accountno);
		if (n <= 0 || n > MAX_LAST_LIST_N_VAL) {
			throw new AccountException("No of Transactions Arg not permitted");
		}
		String accountnoIntern = accountno.intern();
		List<Transaction> txns = accountTransactions.get(accountnoIntern);
		List<Transaction> result = null;
		synchronized(accountnoIntern) {
			if (txns.size() > n) {
				result = txns.subList(txns.size() - n, txns.size());
			} else {
				result = txns;
			}
		}
		return result;
	}

}
