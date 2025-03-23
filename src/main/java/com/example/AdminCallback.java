package com.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * This interface is responsible for the callback methods that the admin
 * will use to update the admin data.
 */
public interface AdminCallback extends Remote {
    void updateAdminData(ArrayList<String> adminData) throws RemoteException;
}