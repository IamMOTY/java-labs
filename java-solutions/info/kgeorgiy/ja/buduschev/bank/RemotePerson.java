package info.kgeorgiy.ja.buduschev.bank;

import java.rmi.RemoteException;
import java.util.Map;

public class RemotePerson extends AbstractPerson implements Person {
    private final Bank bank;

    public RemotePerson(final String firstName, final String lastName, final String passportId, final Bank bank) {
        super(firstName, lastName, passportId);
        this.bank = bank;
    }


    @Override
    public Account getAccount(final String subId) throws RemoteException {
        System.out.printf("Getting account %s for person %s%n", subId, getPassportId());
        return bank.getAccount(subId);
    }

    public String getAccountId(final String subId) throws RemoteException {
        return getPassportId() + ":" + subId;
    }

    @Override
    public Map<String, Account> getAccounts() throws RemoteException {
        System.out.printf("Getting accounts for person: %s%n", getPassportId());
        return bank.getAccounts(this);
    }

    @Override
    public Account createAccount(final String subId) throws RemoteException {
        System.out.printf("Account %s for person %s created%n", subId, getPassportId());
        bank.putSubId(getPassportId(), subId);
        return bank.createAccount(getAccountId(subId));
    }

}
