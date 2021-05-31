package info.kgeorgiy.ja.buduschev.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalPerson extends AbstractPerson implements Serializable {
    private final Map<String, LocalAccount> accounts;

    public LocalPerson(final String firstName, final String lastName, final String passportId, final Map<String, Account> remoteAccountMap) {
        super(firstName, lastName, passportId);
        this.accounts = new ConcurrentHashMap<>();
        remoteAccountMap.forEach((id, rA) -> {
            try {
                accounts.put(id, new LocalAccount(rA.getId(), rA.getAmount()));
            } catch (RemoteException e) {
                System.err.println("Connection lost.");
            }
        });
    }

    public LocalAccount createAccount(final String subId) {
        return accounts.computeIfAbsent(subId, id -> new LocalAccount(getAccountId(id)));
    }

    public LocalAccount getAccount(final String subId) {
        return accounts.get(getAccountId(subId));
    }

    public Map<String, LocalAccount> getAccounts() {
        return accounts;
    }
}
