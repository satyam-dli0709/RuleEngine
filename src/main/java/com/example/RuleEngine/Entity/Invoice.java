package com.example.RuleEngine.Entity;

import lombok.Data;

@Data
public class Invoice {
    private final Customer customer;
    private final double amount;

    public Invoice(Customer customer, double amount) {
        this.customer = customer;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public Customer getCustomer() {
        return customer;
    }
}
