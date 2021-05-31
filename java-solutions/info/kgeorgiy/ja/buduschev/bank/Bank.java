package info.kgeorgiy.ja.buduschev.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     *
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    Person createPerson(String firstName, String lastName, String passportId) throws RemoteException;

    Person getRemotePerson(String passportId) throws RemoteException;

    LocalPerson getLocalPerson(String passportId) throws RemoteException;

    Map<String, Account> getAccounts(Person person) throws RemoteException;

}
