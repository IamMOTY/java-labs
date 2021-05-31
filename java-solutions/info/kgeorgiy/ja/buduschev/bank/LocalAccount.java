package info.kgeorgiy.ja.buduschev.bank;

import java.io.Serializable;

public class LocalAccount extends AbstractAccount implements Serializable {
    public LocalAccount(final String id) {
        super(id);
    }

    public LocalAccount(final String id, final int amount) {
        super(id, amount);
    }
}
