package info.kgeorgiy.ja.buduschev.bank;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {

    private static final int PORT = 8080;
    private static final int REGISTRY_PORT = 1099;

    public static void main(String[] args) {
        try {
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(REGISTRY_PORT);
                registry.list();
            } catch (ConnectException e1) {
                registry = LocateRegistry.createRegistry(REGISTRY_PORT);
            }
            Bank bank = new RemoteBank(PORT);
            try {
                UnicastRemoteObject.exportObject(bank, PORT);
                Naming.rebind("//localhost/bank", bank);
            } catch (final RemoteException e) {
                System.out.println("Unable to export object: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (final RemoteException e) {
            System.out.println("Error while creating RMI registry: " + e.getMessage());
            e.printStackTrace();
        } catch (final MalformedURLException e) {
            System.out.println("Invalid URL");
        }
        System.out.println("Server started");
    }

}
