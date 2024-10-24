package com.richardchankiyin.app;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountTest {

	private IAccount account = null;
	private final String DEFAULT_PERSONAL_ID = "default_personal_id";
	private String default_account_no = null;
	
	@BeforeEach
	void setup() {
		account = new Account();
		default_account_no = account.createAccount(DEFAULT_PERSONAL_ID);
	}
	
	@Test
	void testCreateAccountSuccessful() {
		String accountno = account.createAccount("123");
		assertNotNull(accountno);
		assertFalse(accountno.isBlank());
	}
	
	@Test
	void testCreateDuplicateAccountFailed() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.createAccount(DEFAULT_PERSONAL_ID),
		           "Expected account.createAccount(default_personal_id) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("Personal Id duplicated"));
	}

	
	@Test
	void testDepositAccountDoesNotExist() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.deposit("123", 10),
		           "Expected account.deposit(\"123\", 10) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("Invalid Account"));
	}
	
	@Test
	void testDepositAccountZeroFailed() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.deposit(default_account_no, 0),
		           "Expected account.deposit(default_account_no, 0) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("Invalid Amount"));
	}
	
	@Test
	void testDepositAccountNegativeFailed() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.deposit(default_account_no, -10),
		           "Expected account.deposit(default_account_no, -10) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("Invalid Amount"));
	}
	
	@Test
	void testDepositAccountBalancePositiveAndThenEnquireBalance() {
		account.deposit(default_account_no, 100d);
		assertEquals(100d, account.enquirebalance(default_account_no));
		account.deposit(default_account_no, 150d);
		assertEquals(250d, account.enquirebalance(default_account_no));
	}
	
	@Test
	void testWithdrawAccountDoesNotExist() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.withdraw("123", 10),
		           "Expected account.withdraw(\"123\", 10) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("Invalid Account"));
	}
	
	@Test
	void testWithdrawAccountZeroFailed() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.withdraw(default_account_no, 0),
		           "Expected account.withdraw(default_account_no, 0) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("Invalid Amount"));
	}
	
	@Test
	void testWithdrawAccountNegativeFailed() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.withdraw(default_account_no, -10),
		           "Expected account.withdraw(default_account_no, -10) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("Invalid Amount"));
	}
	
	@Test
	void testWithdrawAccountBalanceInsufficient() {
		account.deposit(default_account_no, 100d);
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.withdraw(default_account_no, 200d),
		           "Expected account.withdraw(default_account_no, 200) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("Insufficient Balance"));
	}
	
	@Test
	void testWithdrawAccountBalanceSuccessful() {
		account.deposit(default_account_no, 100d);
		account.withdraw(default_account_no, 50d);
		assertEquals(50d, account.enquirebalance(default_account_no));
	}
	
	@Test
	void testListTransactionAccountDoesNotExist() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.listLastNTransactions("123", 5),
		           "Expected account.listLastNTransactions(\"123\", 5) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("Invalid Account"));
	}
	
	@Test
	void testListTransactionAccountProvidingZerosNotAccepted() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.listLastNTransactions(default_account_no, 0),
		           "Expected account.listLastNTransactions(default_account_no, 0) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("No of Transactions Arg not permitted"));
	}
	
	@Test
	void testListTransactionAccountProvidingNegativeNotAccepted() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.listLastNTransactions(default_account_no, -1),
		           "Expected account.listLastNTransactions(default_account_no, -1) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("No of Transactions Arg not permitted"));
	}
	
	@Test
	void testListTransactionAccountProvidingElevenNotAccepted() {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.listLastNTransactions(default_account_no, 11),
		           "Expected account.listLastNTransactions(default_account_no, 11) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("No of Transactions Arg not permitted"));
	}
	
	@Test
	void testListAllTransactionsGivenOnly3FoundWhenAskingFor5() {
		account.deposit(default_account_no, 100d);
		account.withdraw(default_account_no, 50d);
		account.deposit(default_account_no, 140d);
		
		List<Transaction> txns = account.listLastNTransactions(default_account_no, 5);
		assertNotNull(txns);
		assertEquals(txns.size(), 3);
		assertEquals(default_account_no, txns.get(0).accountno());
		assertEquals(default_account_no, txns.get(1).accountno());
		assertEquals(default_account_no, txns.get(2).accountno());
		
		assertTrue(txns.get(0).isDeposit());
		assertFalse(txns.get(1).isDeposit());
		assertTrue(txns.get(2).isDeposit());
		
		assertEquals(100d, txns.get(0).amount());
		assertEquals(50d, txns.get(1).amount());
		assertEquals(140d, txns.get(2).amount());
		
	}
	
	@Test
	void testListAllTransactionsWhenAskingFor5() {
		account.deposit(default_account_no, 100d);
		account.deposit(default_account_no, 100d);
		account.deposit(default_account_no, 100d);		
		account.deposit(default_account_no, 100d);
		account.withdraw(default_account_no, 50d);
		account.deposit(default_account_no, 140d);
		
		List<Transaction> txns = account.listLastNTransactions(default_account_no, 3);
		assertNotNull(txns);
		assertEquals(txns.size(), 3);
		assertEquals(default_account_no, txns.get(0).accountno());
		assertEquals(default_account_no, txns.get(1).accountno());
		assertEquals(default_account_no, txns.get(2).accountno());
		
		assertTrue(txns.get(0).isDeposit());
		assertFalse(txns.get(1).isDeposit());
		assertTrue(txns.get(2).isDeposit());
		
		assertEquals(100d, txns.get(0).amount());
		assertEquals(50d, txns.get(1).amount());
		assertEquals(140d, txns.get(2).amount());
		
	}
}
