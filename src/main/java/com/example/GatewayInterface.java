package com.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface GatewayInterface extends Remote {
    // listar os metodos que chamamos por fora
    public void addToQueue(String url) throws RemoteException;

    public String popFromQueue() throws RemoteException;

    public ArrayList<String> sendMessage(String message, int option) throws RemoteException;

    public ArrayList<StorageBarrelInterface> getBarrels(long myId) throws RemoteException;

    public StorageBarrelInterface getRandomBarrel(long myId) throws RemoteException;

    public void addBarrel(StorageBarrelInterface barrel, long id) throws RemoteException;

    public void removeBarrel(long barrelId) throws RemoteException;

}
