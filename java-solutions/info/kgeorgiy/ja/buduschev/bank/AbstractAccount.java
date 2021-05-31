package info.kgeorgiy.ja.buduschev.bank;

import java.io.Serializable;

public abstract class AbstractAccount implements Serializable {
    private final String id;
    private int amount;

    public AbstractAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    public AbstractAccount(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }


    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    public synchronized void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }

    public synchronized void addAmount(final int addition) {
        System.out.println("Setting amount of money for account " + id);
        this.amount += addition;
    }
}
