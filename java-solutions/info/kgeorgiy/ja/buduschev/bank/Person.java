package info.kgeorgiy.ja.buduschev.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote {
    String getFirstName() throws RemoteException;

    String getLastName() throws RemoteException;

    String getPassportId() throws RemoteException;

    Account getAccount(String subId) throws RemoteException;

    Account createAccount(String subId) throws RemoteException;

    String getAccountId(String subId) throws RemoteException;

    Map<String, Account> getAccounts() throws RemoteException;
}
