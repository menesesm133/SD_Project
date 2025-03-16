package com.example;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GatewayInterface extends Remote{
    // listar os metodos que chamamos por fora
    public void addToQueue(String url) throws RemoteException;
}
