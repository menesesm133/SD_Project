package com.example;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StorageBarrelInterface extends Remote{
    public String teste() throws RemoteException;
}