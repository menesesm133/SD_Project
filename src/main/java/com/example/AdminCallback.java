package com.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface AdminCallback extends Remote {
    void updateAdminData(ArrayList<String> adminData) throws RemoteException;
}