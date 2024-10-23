package com.richardchankiyin.app;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        IAccount account = new Account();
        String accountno = account.createAccount("richardchan");
        account.deposit(accountno, 100);
        account.withdraw(accountno, 20);
    }
}
