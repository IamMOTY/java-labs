package info.kgeorgiy.ja.buduschev.bank;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RemotePerson implements Person {
    private final String firstName;
    private final String lastName;
    private final String passportId;
    private final Bank bank;
    private final Set<String> subIds;

    public RemotePerson(final String firstName, final String lastName, final String passportId, final Bank bank) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportId = passportId;
        this.bank = bank;
        this.subIds = ConcurrentHashMap.newKeySet();
    }

    @Override
    public String getFirstName() throws RemoteException {
        return firstName;
    }

    @Override
    public String getLastName() throws RemoteException {
        return lastName;
    }

    @Override
    public String getPassportId() throws RemoteException {
        return passportId;
    }

    @Override
    public Account getAccount(final String subId) throws RemoteException {
        System.out.printf("Getting account %s for person %s%n", subId, passportId);
        return bank.getAccount(subId);
    }

    public String getAccountId(final String subId) throws RemoteException {
        return getPassportId() + ":" + subId;
    }

    @Override
    public Map<String, Account> getAccounts() throws RemoteException {
        System.out.printf("Getting accounts for person: %s%n", passportId);
        return bank.getAccounts(this);
    }

    @Override
    public Account createAccount(final String subId) throws RemoteException {
        System.out.printf("Account %s for person %s created%n", subId, passportId);
        subIds.add(subId);
        return bank.createAccount(getAccountId(subId));
    }

    public Set<String> getSubIds() {
        return subIds;
    }
}
