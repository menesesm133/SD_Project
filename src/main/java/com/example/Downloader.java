package com.example;

import org.json.JSONObject;
import org.json.JSONArray;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class is responsible for creating the downloader threads,
 * extract information from the web pages and send it to the barrels
 * through RMI and get urls from the queue through RMI.
 */
public class Downloader extends Thread{

    /**
     * Constructs a Downloader thread.
     */
    public Downloader() {
        super("Server: " + (long) (Math.random() * 1000));
    }

    public void run() {
        String RMI_ADDRESS;
        int RMI_PORT;

        //Read information regarding the RMI from "properties.txt"
        try(BufferedReader br = new BufferedReader(new FileReader(new File("urls.txt")))){
            String line;

            while((line = br.readLine()) != null) {
                String[] token = line.split(":");

                if (token[0].trim().equals("RMI Address")) {
                    RMI_ADDRESS = token[1].trim();
                }

                if (token[0].trim().equals("RMI Port")) {
                    RMI_PORT = Integer.parseInt(token[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * This function serches a web page using the Jsoup library
     * and extracts the information.
     * @param url The url to be searched.
     * @return The JSONObject with the information of the url.
     */
    private JSONObject searchURL(String url) {
        System.out.println("Searching for: " + url);
        JSONObject json = new JSONObject();

        //Get the url information(title, description, associated urls)
        try {
            //Url
            json.put("url", url);

            //Title
            Document doc = Jsoup.connect(url).get();
            json.put("title", doc.title());

            //Description
            Elements paragraphs = doc.select("p");
            StringBuilder textBuilder = new StringBuilder();

            for (Element paragraph : paragraphs) {
                textBuilder.append(paragraph.text()).append("\n");
            }

            String text = textBuilder.toString();
            json.put("text", text);

            //Associated Urls
            Elements links = doc.select("a[href]");
            JSONArray linksArray = new JSONArray();

            for (Element link : links) {
                String absoluteUrl = link.attr("abs:href");
                linksArray.put(absoluteUrl);
            }

            json.put("links", linksArray);

        } catch (IOException e) {
            e.printStackTrace();
            json.put("url", "Not available");

        }
        
        return json;
    }
    
    public static void main(String[] args) {
        try {
            Downloader downloader = new Downloader();
            System.out.println("Starting downloader " + downloader.getName());
            downloader.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}