package info.kgeorgiy.ja.buduschev.bank;

public class RemoteAccount extends AbstractAccount implements Account {
    public RemoteAccount(String id) {
        super(id);
    }

    public RemoteAccount(String id, int amount) {
        super(id, amount);
    }
}
