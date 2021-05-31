package info.kgeorgiy.ja.buduschev.bank;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class BankTests {
    Bank bank;

    private final static String FIRSTNAME = "Lucifer";
    private final static String LASTNAME = "Morningstar";
    private final static String PASSPORT = "1528";
    private final static String SUB_ID = "1331";
    private final static String KEY = PASSPORT + ":" + SUB_ID;
    private final static int AMOUNT = 777;

    @BeforeClass
    public static void before() {
        try {
            LocateRegistry.createRegistry(1099);
        } catch (final RemoteException e) {
            System.out.println("Failed create registry: " + e.getMessage());
        }
    }

    @Before
    public void start() {
        Server.main(new String[0]);
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
        } catch (final RemoteException e) {
            System.out.println("Remote exception while getting bank: " + e.getMessage());
        }
    }

    @After
    public void end() {
        System.out.println("Testing end.");
    }


    private void createPerson() throws RemoteException {
        createPerson(PASSPORT);
    }

    private void createPerson(final String passport) throws RemoteException {
        bank.createPerson(FIRSTNAME, LASTNAME, passport);
    }

    private Person getRemotePerson() throws RemoteException {
        return bank.getRemotePerson(PASSPORT);
    }

    private LocalPerson getLocalPerson() throws RemoteException {
        return bank.getLocalPerson(PASSPORT);
    }

    private void createAccount(final Person person) throws RemoteException {
        person.createAccount(SUB_ID);
    }

    private void createAccount(final LocalPerson person) throws RemoteException {
        person.createAccount(SUB_ID);
    }


    private Account getAccount() throws RemoteException {
        return bank.getAccount(KEY);
    }


    private void setAmount(final Account account) throws RemoteException {
        account.setAmount(AMOUNT);
    }

    private void setAmount(final LocalAccount account) throws RemoteException {
        account.setAmount(AMOUNT);
    }


    private void assertPerson(final Person person) throws RemoteException {
        assertEquals(FIRSTNAME, person.getFirstName());
        assertEquals(LASTNAME, person.getLastName());
        assertEquals(PASSPORT, person.getPassportId());
    }

    private void assertPerson(final LocalPerson person) {
        assertEquals(FIRSTNAME, person.getFirstName());
        assertEquals(LASTNAME, person.getLastName());
        assertEquals(PASSPORT, person.getPassportId());
    }

    private void assertAccount(final Person person, final int amount) throws RemoteException {
        final Map<String, Account> accounts = Map.copyOf(person.getAccounts());
        assertEquals(1, accounts.size());
        assertEquals(KEY, accounts.get(SUB_ID).getId());
        assertEquals(amount, accounts.get(SUB_ID).getAmount());
    }

    private void assertAccount(final LocalPerson person, final int amount) throws RemoteException {
        final Map<String, LocalAccount> accounts = Map.copyOf(person.getAccounts());
        assertEquals(1, accounts.size());
        assertEquals(KEY, accounts.get(SUB_ID).getId());
        assertEquals(amount, accounts.get(SUB_ID).getAmount());
    }


    @Test
    public void createRP() {
        try {
            createPerson();
            final Person person = getRemotePerson();
            assertPerson(person);
        } catch (RemoteException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void createLP() {
        try {
            createPerson();
            final LocalPerson person = getLocalPerson();
            assertPerson(person);
        } catch (RemoteException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void remoteChange() {
        try {
            createPerson();
            final Person person = getRemotePerson();
            createAccount(person);
            final Account account = getAccount();
            assertNotNull(account);
            assertAccount(person, 0);
            setAmount(account);
            assertAccount(person, AMOUNT);
        } catch (RemoteException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void localChange() {
        try {
            createPerson();
            final LocalPerson localPerson = getLocalPerson();
            createAccount(localPerson);
            final Account account = getAccount();
            assertNull(account);
            assertAccount(localPerson, 0);
            final Map<String, LocalAccount> accounts = Map.copyOf(localPerson.getAccounts());
            assertEquals(1, accounts.size());
            final LocalAccount localAccount = accounts.get(SUB_ID);
            setAmount(localAccount);
            assertAccount(localPerson, AMOUNT);
        } catch (RemoteException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void updatedLocale() {
        try {
            createPerson();
            final Person person = getRemotePerson();
            createAccount(person);
            final Account account = getAccount();
            setAmount(account);
            assertAccount(person, AMOUNT);
            final LocalPerson localPerson = getLocalPerson();
            assertAccount(localPerson, AMOUNT);
        } catch (RemoteException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void concurrentCreate() {
        final int threads = 8;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch count = new CountDownLatch(threads);
        IntStream.range(0, threads).forEach(i -> pool.submit(() -> {
            IntStream.range(0, threads).forEach(j -> {
                try {
                    createPerson(Integer.toString(i * threads + j));
                } catch (RemoteException e) {
                    e.printStackTrace();
                    fail();
                }
            });
            count.countDown();
        }));
        try {
            count.await();
            for (int i = 0; i < threads * threads; i++) {
                Person person = bank.getRemotePerson(Integer.toString(i));
                assertNotNull(person);
                LocalPerson localPerson = bank.getLocalPerson(Integer.toString(i));
                assertNotNull(localPerson);
            }
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void concurrentGet() {
        final int threads = 8;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch count = new CountDownLatch(threads);
        try {
            createPerson();
            final Person person = getRemotePerson();
            createAccount(person);
            final Account account = getAccount();
            final String id = account.getId();
            final int amount = account.getAmount();
            IntStream.range(0, threads).forEach(i -> pool.submit(() -> {
                try {
                    for (int j = 0; j < threads; j++) {
                        final Account test = bank.getAccount(id);
                        assertEquals(amount, test.getAmount());
                    }
                    count.countDown();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    fail();
                }
            }));
            count.await();
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void concurrentAdd() {
        try {
            final int threads = 8;
            final ExecutorService pool = Executors.newFixedThreadPool(threads);
            final CountDownLatch count = new CountDownLatch(threads);
            createPerson();
            final Person person = getRemotePerson();
            createAccount(person);
            final Account account = getAccount();
            IntStream.range(0, threads).forEach(i -> pool.submit(() -> {
                for (int j = 0; j < threads; j++) {
                    try {
                        account.addAmount(AMOUNT);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        fail();
                    }
                }
                count.countDown();
            }));
            count.await();
            assertEquals(AMOUNT * threads * threads, account.getAmount());
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        Result result = junit.run(BankTests.class);
        System.out.println(result);
        System.exit(0);
    }
}
