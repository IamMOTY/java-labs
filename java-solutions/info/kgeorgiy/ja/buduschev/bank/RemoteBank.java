package info.kgeorgiy.ja.buduschev.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        System.out.println("Creating account " + id);
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            System.out.println("Account putting succeed");
            return account;
        } else {
            System.out.println("Account already exist");
            return getAccount(id);
        }
    }

    @Override
    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    @Override
    public Person createPerson(String firstName, String lastName, String passportId) throws RemoteException {
        final Person person = new RemotePerson(firstName, lastName, passportId, this);
        if (persons.putIfAbsent(passportId, person) == null) {
            System.out.printf("Creating person %s%n", passportId);
            UnicastRemoteObject.exportObject(person, port);
            return person;
        }
        return persons.get(passportId);
    }

    @Override
    public Person getRemotePerson(final String passportId) throws RemoteException {
        System.out.printf("Getting remote person %s%n", passportId);
        return persons.get(passportId);
    }


    @Override
    public LocalPerson getLocalPerson(final String passportId) throws RemoteException {
        System.out.printf("Getting local person %s%n", passportId);
        final Person person = getRemotePerson(passportId);
        if (person == null) {
            return null;
        }
        return new LocalPerson(person.getFirstName(), person.getLastName(), passportId, getAccounts(person));
    }

    public Map<String, Account> getAccounts(final Person person) throws RemoteException {
        final Map<String, Account> result = new ConcurrentHashMap<>();
        for (String id : person.getSubIds()) {
            result.put(id, accounts.get(person.getAccountId(id)));
        }
        return result;
    }
}
