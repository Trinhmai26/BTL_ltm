package com.loginSystem.server;

import com.google.gson.Gson;
import com.loginSystem.common.*;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private DatabaseManager dbManager;
    private ExecutorService executorService;
    private ConcurrentHashMap<String, ClientHandler> activeClients;
    
    public void broadcast(String message) {
        activeClients.values().forEach(h -> h.pushNotification(message));
    }
    
    public void broadcast(String message, String excludeUsername) {
        activeClients.values().forEach(h -> {
            if (!h.getCurrentUser().equals(excludeUsername)) {
                h.pushNotification(message);
            }
        });
    }

    public Server() {
        this.dbManager = new DatabaseManager();
        this.executorService = Executors.newFixedThreadPool(50);
        this.activeClients = new ConcurrentHashMap<>();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for client connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                executorService.submit(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            if (serverSocket != null) serverSocket.close();
            executorService.shutdown();
            dbManager.close();
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    public DatabaseManager getDbManager() { return dbManager; }

    public void addActiveClient(String username, ClientHandler handler) { activeClients.put(username, handler); }
    public void removeActiveClient(String username) { activeClients.remove(username); }
    public int getActiveClientCount() { return activeClients.size(); }
    public java.util.Set<String> getActiveClients() { return activeClients.keySet(); }

    public static void main(String[] args) {
        Server server = new Server();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}
