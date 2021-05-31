package info.kgeorgiy.ja.buduschev.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Objects;

public class Client {
    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Invalid count of arguments, must be 5");
            return;
        }
        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Null value in arguments found");
        }
        String firstName = args[0];
        String lastName = args[1];
        String passportId = args[2];
        String accountId = args[3];
        try {

            int amount = Integer.parseInt(args[4]);

            final Bank bank;
            try {
                bank = (Bank) Naming.lookup("//localhost/bank");
            } catch (final NotBoundException e) {
                System.out.println("Bank hasn't been bounded");
                return;
            } catch (final MalformedURLException e) {
                System.out.println("Invalid URL of bank");
                return;
            } catch (RemoteException e) {
                System.out.println("Could not find remote bank");
                return;
            }

            try {
                Person person = bank.getRemotePerson(passportId);
                if (person == null) {
                    person = bank.createPerson(firstName, lastName, passportId);
                } else {
                    if (!person.getFirstName().equals(firstName) || !person.getLastName()
                            .equals(lastName)) {
                        System.out.println("Invalid credentials for user with id " + passportId);
                        return;
                    }
                }
                Account account = person.createAccount(accountId);
                System.out.println("Account id: " + account.getId());
                System.out.println("Money: " + account.getAmount());
                System.out.println("Set Money");
                account.setAmount(amount);
                System.out.println("Money: " + account.getAmount());
            } catch (RemoteException e) {
                System.out.println("Connection problem");
            }
        } catch (NumberFormatException e) {
            System.err.println("Amount - integer value required!");
        }
    }

}
