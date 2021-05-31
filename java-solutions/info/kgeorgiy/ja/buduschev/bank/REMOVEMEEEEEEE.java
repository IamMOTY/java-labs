package info.kgeorgiy.ja.buduschev.bank;//package info.kgeorgiy.ja.kriushenkov.rmi.test;
//
//import java.net.MalformedURLException;
//import java.rmi.Naming;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.IntConsumer;
//import java.util.stream.IntStream;
//
//import info.kgeorgiy.ja.kriushenkov.rmi.Account;
//import info.kgeorgiy.ja.kriushenkov.rmi.Bank;
//import info.kgeorgiy.ja.kriushenkov.rmi.Person;
//import info.kgeorgiy.ja.kriushenkov.rmi.Server;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.fail;
//
//public class REMOVEMEEEEE {
//    Bank bank;
//
//    private final static String DEFAULT_NAME = "1";
//    private final static String DEFAULT_SURNAME = "2";
//    private final static String DEFAULT_PASSPORT = "123";
//    private final static String DEFAULT_SUB_ID = "121";
//    private final static int DEFAULT_NEW_AMOUNT = 42;
//
//    @BeforeClass
//    public static void before()  {
//        try {
//            LocateRegistry.createRegistry(1099);
//        } catch (final RemoteException e) {
//            System.out.println("Failed create rmiregistry: " + e.getMessage());
//        }
//    }
//
//    @Before
//    public void start() {
//        Server.main();
//        try {
//            bank = (Bank) Naming.lookup("//localhost/bank");
//        } catch (final NotBoundException e) {
//            System.out.println("Bank is not bound");
//        } catch (final MalformedURLException e) {
//            System.out.println("Bank URL is invalid");
//        } catch (final RemoteException e) {
//            System.out.println("Remote exception while getting bank: " + e.getMessage());
//        }
//    }
//
//    @Test
//    public void createRemotePerson() {
//        try {
//            createDefaultPerson();
//            final Person remotePerson = getDefaultRemotePerson();
//
//            assertDefaultPerson(remotePerson);
//        } catch (final RemoteException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void createLocalPerson() {
//        try {
//            createDefaultPerson();
//            final Person localPerson = getDefaultLocalPerson();
//
//            assertDefaultPerson(localPerson);
//        } catch (final RemoteException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void changeMoneyRemote() {
//        try {
//            createDefaultPerson();
//            final Person remotePerson = getDefaultRemotePerson();
//
//            createDefaultAccount(remotePerson);
//
//            final Account account = getDefaultAccount();
//
//            assertAccountDefaultPerson(remotePerson);
//
//            setNewDefaultAmount(account);
//
//            assertAccountDefaultPersonWithAmount(remotePerson);
//        } catch (final RemoteException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void changeMoneyLocal() {
//        try {
//            createDefaultPerson();
//            final Person localPerson = getDefaultLocalPerson();
//
//            createDefaultAccount(localPerson);
//
//            final Account account = getDefaultAccount();
//            assertNull(account);
//
//            assertAccountDefaultPerson(localPerson);
//
//            final var accounts = List.copyOf(localPerson.getAccounts());
//            assertEquals(1, accounts.size());
//            setNewDefaultAmount(accounts.get(0));
//            assertAccountDefaultPersonWithAmount(localPerson);
//        } catch (final RemoteException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void localHavePreviousRemoteChanges() {
//        try {
//            createDefaultPerson();
//            final Person remotePerson = getDefaultRemotePerson();
//
//            createDefaultAccount(remotePerson);
//
//            final Account account = getDefaultAccount();
//
//            setNewDefaultAmount(account);
//
//            assertAccountDefaultPersonWithAmount(remotePerson);
//
//            final Person localPerson = getDefaultLocalPerson();
//
//            assertAccountDefaultPersonWithAmount(localPerson);
//        } catch (final RemoteException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void createManyAccountsRemote() {
//        try {
//            createDefaultPerson();
//            final Person remotePerson = getDefaultRemotePerson();
//
//            final int size = 10;
//
//            for (int i = 0; i < size; i++) {
//                remotePerson.createAccount(Integer.toString(i));
//                final Account account = bank.getAccount(DEFAULT_PASSPORT + ":" + i);
//                account.setAmount(i);
//            }
//
//            final var accounts = remotePerson.getAccounts();
//
//            assertEquals(size, accounts.size());
//
//            for (Account i : accounts) {
//                final String subId = getGeneratingAccountId(i.getId());
//                assertEquals(Integer.parseInt(subId), i.getAmount());
//            }
//        } catch (final RemoteException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void createManyAccountsLocal() {
//        try {
//            createDefaultPerson();
//            final Person localPerson = getDefaultLocalPerson();
//
//            final int size = 10;
//
//            for (int i = 0; i < size; i++) {
//                localPerson.createAccount(Integer.toString(i));
//            }
//
//            final var accounts = localPerson.getAccounts();
//
//            for (Account i : accounts) {
//                i.setAmount(Integer.parseInt(getGeneratingAccountId(i.getId())));
//            }
//
//            assertEquals(size, accounts.size());
//
//            for (Account i : accounts) {
//                final String subId = i.getId().substring(localPerson.getPassport().length() + 1);
//                assertEquals(Integer.parseInt(subId), i.getAmount());
//            }
//        } catch (final RemoteException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void createManyPersons() {
//        try {
//            for (int i = 0; i < 10; i++) {
//                createDefaultPerson(Integer.toString(i));
//            }
//            for (int i = 0; i < 6; i++) {
//                final Person remotePerson = bank.getRemotePerson(Integer.toString(i));
//                final Account account = remotePerson.createAccount(Integer.toString(i));
//                account.setAmount(i);
//                assertAccountPerson(remotePerson, i);
//            }
//            for (int i = 4; i < 10; i++) {
//                final Person localPerson = bank.getLocalPerson(Integer.toString(i));
//                final Account account = localPerson.createAccount(Integer.toString(i + 1));
//                account.setAmount(i + 1);
//                final var accounts = List.copyOf(localPerson.getAccounts());
//                if (i < 6) {
//                    assertEquals(2, accounts.size());
//                    assertEquals(i, accounts.get(0).getAmount());
//                    assertEquals(i + 1, accounts.get(1).getAmount());
//                } else {
//                    assertEquals(1, accounts.size());
//                    assertEquals(i + 1, accounts.get(0).getAmount());
//                }
//            }
//        } catch (final RemoteException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void parallelCreate() throws InterruptedException {
//        final int threads = 10;
//        final ExecutorService service = Executors.newFixedThreadPool(threads);
//        final CountDownLatch count = new CountDownLatch(threads);
//        for (int i = 0; i < threads; i++) {
//            final int finalI = i;
//            final Runnable task = () -> {
//                for (int j = 0; j < threads; j++) {
//                    try {
//                        createDefaultPerson(Integer.toString(finalI * threads + j));
//                    } catch (final RemoteException e) {
//                        System.err.println("Remote exception while test: " + e.getMessage());
//                        fail();
//                    }
//                }
//                count.countDown();
//            };
//            service.submit(task);
//        }
//        count.await();
//        try {
//            for (int i = 0; i < threads * threads; i++) {
//                Person person = bank.getRemotePerson(Integer.toString(i));
//                assertNotNull(person);
//                person = bank.getLocalPerson(Integer.toString(i));
//                assertNotNull(person);
//            }
//        } catch (final RemoteException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void getParallelOneAccount() {
//        final int threads = 100;
//        final ExecutorService service = Executors.newFixedThreadPool(threads);
//        final CountDownLatch count = new CountDownLatch(threads);
//
//        try {
//            createDefaultPerson();
//
//            final Person person = getDefaultRemotePerson();
//
//            createDefaultAccount(person);
//
//            final Account account = getDefaultAccount();
//
//            final String accountId = account.getId();
//
//            final int accountAmount = account.getAmount();
//
//            final Runnable task = () -> {
//                try {
//                    for (int j = 0; j < threads; j++) {
//                        final Account remoteAccount = bank.getAccount(accountId);
//                        assertEquals(accountAmount, remoteAccount.getAmount());
//                    }
//                    count.countDown();
//                } catch (final RemoteException e) {
//                    System.err.println("Remote exception while test: " + e.getMessage());
//                    fail();
//                }
//            };
//
//            IntStream.range(0, threads).forEach(i -> service.submit(task));
//            count.await();
//
//        } catch (RemoteException | InterruptedException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void incrementAccount() {
//        try {
//            final int threads = 10;
//            final ExecutorService service = Executors.newFixedThreadPool(threads);
//            final CountDownLatch count = new CountDownLatch(threads);
//
//            createDefaultPerson();
//
//            final Person person = getDefaultRemotePerson();
//
//            createDefaultAccount(person);
//
//            final Account account = getDefaultAccount();
//
//            final Runnable task = () -> {
//                for (int i = 0; i < threads; i++) {
//                    try {
//                        account.addAmount(1);
//                    } catch (final RemoteException e) {
//                        System.err.println("Remote exception while test: " + e.getMessage());
//                        fail();
//                    }
//                }
//                count.countDown();
//            };
//
//            IntStream.range(0, threads).forEach(i -> service.submit(task));
//
//            count.await();
//
//            assertEquals(threads * threads, account.getAmount());
//
//        } catch (final RemoteException | InterruptedException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    @Test
//    public void createParallelAccounts() {
//        try {
//            final int threads = 10;
//            final ExecutorService service = Executors.newFixedThreadPool(threads);
//            final CountDownLatch count = new CountDownLatch(threads);
//
//            createDefaultPerson();
//
//            final Person person = getDefaultRemotePerson();
//
//            final IntConsumer consumer = i -> {
//                final Runnable task = () -> {
//                    for (int j = 0; j < threads; j++) {
//                        try {
//                            person.createAccount(Integer.toString(i * 10 + j));
//                        } catch (final RemoteException e) {
//                            System.err.println("Remote exception while test: " + e.getMessage());
//                            fail();
//                        }
//                    }
//                    count.countDown();
//                };
//                service.submit(task);
//            };
//
//            IntStream.range(0, threads).forEach(consumer);
//
//            count.await();
//
//            assertEquals(threads * threads, person.getAccounts().size());
//
//        } catch (final RemoteException | InterruptedException e) {
//            System.err.println("Remote exception while test: " + e.getMessage());
//            fail();
//        }
//    }
//
//    private void createDefaultPerson() throws RemoteException {
//        createDefaultPerson(DEFAULT_PASSPORT);
//    }
//
//    private void createDefaultPerson(final String passport) throws RemoteException {
//        bank.createPerson(DEFAULT_NAME, DEFAULT_SURNAME, passport);
//    }
//
//    private Account getDefaultAccount() throws RemoteException {
//        return bank.getAccount(DEFAULT_PASSPORT + ":" + DEFAULT_SUB_ID);
//    }
//
//    private Person getDefaultRemotePerson() throws RemoteException {
//        return bank.getRemotePerson(DEFAULT_PASSPORT);
//    }
//
//    private Person getDefaultLocalPerson() throws RemoteException {
//        return bank.getLocalPerson(DEFAULT_PASSPORT);
//    }
//
//    private void createDefaultAccount(final Person person) throws RemoteException {
//        person.createAccount(DEFAULT_SUB_ID);
//    }
//
//    private void setNewDefaultAmount(final Account account) throws RemoteException {
//        account.setAmount(DEFAULT_NEW_AMOUNT);
//    }
//
//    private String getGeneratingAccountId(String id) {
//        return id.substring(DEFAULT_PASSPORT.length() + 1);
//    }
//
//    private void assertDefaultPerson(final Person person) throws RemoteException {
//        assertEquals(DEFAULT_NAME, person.getName());
//        assertEquals(DEFAULT_SURNAME, person.getSurname());
//        assertEquals(DEFAULT_PASSPORT, person.getPassport());
//    }
//
//    private void assertAccountDefaultPerson(final Person person) throws RemoteException {
//        final var accounts = List.copyOf(person.getAccounts());
//        assertEquals(1, accounts.size());
//        assertDefaultAccount(accounts.get(0));
//    }
//
//    private void assertAccountPerson(final Person person, final int amount) throws RemoteException {
//        final var accounts = List.copyOf(person.getAccounts());
//        assertEquals(1, accounts.size());
//        assertEquals(amount, accounts.get(0).getAmount());
//    }
//
//    private void assertAccountDefaultPersonWithAmount(final Person person) throws RemoteException {
//        final var accounts = List.copyOf(person.getAccounts());
//        assertEquals(1, accounts.size());
//        assertDefaultAccountWithAmount(accounts.get(0));
//    }
//
//    private void assertDefaultAccount(final Account account) throws RemoteException {
//        assertEquals(DEFAULT_PASSPORT + ":" + DEFAULT_SUB_ID, account.getId());
//        assertEquals(0, account.getAmount());
//    }
//
//    private void assertDefaultAccountWithAmount(final Account account) throws RemoteException {
//        assertEquals(DEFAULT_PASSPORT + ":" + DEFAULT_SUB_ID, account.getId());
//        assertEquals(DEFAULT_NEW_AMOUNT, account.getAmount());
//    }
//}
