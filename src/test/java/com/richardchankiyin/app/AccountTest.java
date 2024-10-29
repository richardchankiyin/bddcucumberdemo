package com.richardchankiyin.app;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

class AccountTest {
	final Logger logger = LoggerFactory.getLogger(AccountTest.class);
	private IAccount account = null;
	private final String DEFAULT_PERSONAL_ID = "default_personal_id";
	private String default_account_no = null;
	private ExecutorService es = null;
	
	@BeforeEach
	void setup() {
		account = new Account();
		default_account_no = account.createAccount(DEFAULT_PERSONAL_ID);
		es = Executors.newFixedThreadPool(10);
	}
	
	@AfterEach
	void tearDown() {
		if (es != null) {
			es.shutdownNow();
		}
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
	void testConcurrentCreateAccountOneFailed() throws Exception{
		
		Callable<String> callableTask = () -> {
			String accountno = null;
			try {
				account.createAccount("123");
			} catch (Exception e) {
				logger.debug("testConcurrentCreateAccountOneFailed", e);
			}
			return accountno;
		};
		
		List<Callable<String>> callableTasks = new ArrayList<>();
		callableTasks.add(callableTask);
		callableTasks.add(callableTask);
		List<Future<String>> results = es.invokeAll(callableTasks);
		String result1 = results.get(0).get(100, TimeUnit.SECONDS);
		String result2 = results.get(1).get(100, TimeUnit.SECONDS);
		assertTrue(result1 == null | result2 == null);
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
	void testDepositAccountBalancePositiveConcurrentAndThenEnquireBalance() throws Exception{
		Callable<Double> callableTask1 = () -> {
			account.deposit(default_account_no, 100d);
			return account.enquirebalance(default_account_no);
		};
		
		Callable<Double> callableTask2 = () -> {
			account.deposit(default_account_no, 150d);
			return account.enquirebalance(default_account_no);
		};
		
		List<Callable<Double>> callableTasks = new ArrayList<>();
		callableTasks.add(callableTask1);
		callableTasks.add(callableTask2);
		List<Future<Double>> results = es.invokeAll(callableTasks);
		
		double result1 = results.get(0).get(100, TimeUnit.SECONDS).doubleValue();
		double result2 = results.get(1).get(100, TimeUnit.SECONDS).doubleValue();
		logger.debug("result1: {} result2: {}", result1, result2);
		
		assertTrue((result1 == 100d && result2 == 250d) || (result1 == 250d && result2 == 150d), String.format("result1: %s result2: %s", result1, result2));
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
	void testWithdrawAccountBalanceConcurrentEnquireBalance() throws Exception{
		account.deposit(default_account_no, 500d);
		
		Callable<Double> callableTask1 = () -> {
			account.withdraw(default_account_no, 200d);
			return account.enquirebalance(default_account_no);
		};
		
		Callable<Double> callableTask2 = () -> {
			account.withdraw(default_account_no, 150d);
			return account.enquirebalance(default_account_no);
		};
		
		List<Callable<Double>> callableTasks = new ArrayList<>();
		callableTasks.add(callableTask1);
		callableTasks.add(callableTask2);
		List<Future<Double>> results = es.invokeAll(callableTasks);
		
		double result1 = results.get(0).get(100, TimeUnit.SECONDS).doubleValue();
		double result2 = results.get(1).get(100, TimeUnit.SECONDS).doubleValue();
		logger.debug("result1: {} result2: {}", result1, result2);
		
		assertTrue((result1 == 300d && result2 == 150d) || (result1 == 350d && result2 == 150d), String.format("result1: %s result2: %s", result1, result2));

	}
	
	@Test
	void testDepositWithdrawConcurrentSometimesFailed() throws Exception {
		Callable<Double> callableTask1 = () -> {
			account.deposit(default_account_no, 200d);
			return account.enquirebalance(default_account_no);
		};
		
		Callable<Double> callableTask2 = () -> {
			account.withdraw(default_account_no, 150d);
			return account.enquirebalance(default_account_no);
		};
		
		List<Callable<Double>> callableTasks = new ArrayList<>();
		callableTasks.add(callableTask1);
		callableTasks.add(callableTask2);
		List<Future<Double>> results = es.invokeAll(callableTasks);
		
		double result1 = results.get(0).get(100, TimeUnit.SECONDS).doubleValue();
		
		try {
			double result2 = results.get(1).get(100, TimeUnit.SECONDS).doubleValue();
			assertEquals(200d, result1);
			assertEquals(50d, result2);
		} catch (Exception e) {
			logger.debug("testDepositWithdrawConcurrentSometimesFailed", e);
			assertEquals(200d, result1);
		}
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
		assertEquals(3, txns.size());
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
		assertEquals(3, txns.size());
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
