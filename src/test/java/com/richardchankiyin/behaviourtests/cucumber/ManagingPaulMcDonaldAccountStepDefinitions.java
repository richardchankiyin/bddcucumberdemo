package com.richardchankiyin.behaviourtests.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.richardchankiyin.app.Account;
import com.richardchankiyin.app.AccountException;
import com.richardchankiyin.app.IAccount;
import com.richardchankiyin.app.Transaction;

import io.cucumber.java.en.*;

public class ManagingPaulMcDonaldAccountStepDefinitions {
	
	private IAccount account;
	private final String pid = "paulmcdonald_pid";
	private String accountno = null;
	
	public ManagingPaulMcDonaldAccountStepDefinitions() {
		account = new Account();
		accountno = account.createAccount(pid);
	}

	
    @Given("Paul has account balance {double}")
    public void paul_has_account_balance(double d) {
        account.deposit(accountno, d);
    }
    
    @When("Paul withdraws {double} and then deposits {double}")
    public void paul_withdraws_and_then_deposits(double d1, double d2) {
    	account.withdraw(accountno, d1);
    	account.deposit(accountno, d2);
    }

    
    @Then("the account balance becomes {double}")
    public void the_account_balance_becomes(double d) {
        assertEquals(d, account.enquirebalance(accountno));
    }

    @When("Paul withdraws {double}, {double} and then deposit {double}")
    public void paul_withdraws_and_then_deposit(double d1, double d2, double d3) {
        account.withdraw(accountno, d1);
        account.withdraw(accountno, d2);
        account.deposit(accountno, d3);
    }
    
    @Then("the transaction records will show {long} withdraws and {long} deposit records with amount {double}, {double} and {double} respectively")
    public void the_transaction_records_will_show_withdraws_and_deposit_records_with_amount_and_respectively(long l1, long l2, double d1, double d2, double d3) {
        int n = (int)(l1 + l2);
    	List<Transaction> txns = account.listLastNTransactions(accountno, n);
    	long noofwithdraw = txns.stream().filter(x->!x.isDeposit()).count();
    	long noofdeposit = txns.stream().filter(x->x.isDeposit()).count();
    	assertEquals(noofwithdraw, l1);
    	assertEquals(noofdeposit, l2);
    	assertEquals(d1, txns.get(0).amount());
    	assertEquals(d2, txns.get(1).amount());
    	assertEquals(d3, txns.get(2).amount());
    }

    @When("Paul attempts to withdraw {double} due to insufficient balance")
    public void paul_attempts_to_withdraw_due_to_insufficient_balance(double d1) {
		AccountException thrown = assertThrows(
				AccountException.class,
		           () -> account.withdraw(accountno, d1),
		           "Expected account.withdraw(default_account_no, d1) to throw, but it didn't"
		    );

		assertTrue(thrown.getMessage().contains("Insufficient Balance"));
    }
	
}
