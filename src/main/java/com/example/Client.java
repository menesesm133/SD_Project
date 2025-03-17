package com.example;

import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * This class is responsible for the client functionality.
 */
public class Client {
    public Client() throws RemoteException {
        super();
    }

    private void menu(){
        System.out.println(
        "1. Search\n" + //Asks for URL and goes to a menu with the search results
        "2. Index URL to Queue\n" + //Adds a URL to the queue
        "3. Administrator Page\n" + //The admin page gets the status of the system
        "4. Exit\n"); //Ends the client
    Scanner sc = new Scanner(System.in);
    String linha = sc.nextLine();
    if(linha.equals("4")){
        System.out.println("Exiting...");
    }else if(linha.equals("3")){
    }
    else if(linha.equals("2")){
        System.out.println("URL a indexar: ");
        String url = sc.nextLine();
        try{
            // adicionar a queue 
        }catch (Exception e) {
            System.out.println("Exception indexing: " + e);
        }
        System.out.println("URL indexado");

    }else if(linha.equals("1")){
        
    }
    }

    /**
     * Main method of the client.
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.out.println("Client started");
            Client c = new Client();
            c.menu();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
