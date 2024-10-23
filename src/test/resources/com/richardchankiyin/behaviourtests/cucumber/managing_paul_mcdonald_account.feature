Feature: Managing Paul McDonald Account
  This is to illustrate how Paul McDonald managing his account

  Scenario: Paul wants to withdraw 10 from his account and then deposit 30 using his account no
    Given Paul has account balance 500
    When Paul withdraws 10 and then deposits 20
    Then the account balance becomes 510
    
  Scenario: Paul wants to withdraw 40.5 from his account and then deposit 20.2 using his account no
    Given Paul has account balance 1000.0
    When Paul withdraws 40.5 and then deposits 20.2
    Then the account balance becomes 979.7

  Scenario: Paul wants to deposit 30.2 from his account and then deposit 10.1 using his account no
    Given Paul has account balance 1000.0
    When Paul deposits 30.2
    And Paul withdraws 10.1
    Then the account balance becomes 1020.1
    
  Scenario: Paul wants to check his last 3 transactions
    Given Paul has account balance 200
    When Paul withdraws 40.5, 50.0 and then deposit 100.2
    Then the transaction records will show 2 withdraws and 1 deposit records with amount 40.5, 50.0 and 100.2 respectively

  Scenario: Paul fails to withdraw 100 from his account due to insufficient balance
    Given Paul has account balance 50
    When Paul attempts to withdraw 100 due to insufficient balance
    Then the account balance becomes 50      
