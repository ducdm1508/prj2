package com.cyber.server.controller.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerManager {
    public static final int SERVER_PORT = 12345;
    public static final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, String> ipsByUsername = new ConcurrentHashMap<>();

    public static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is running on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                System.out.println("Client connected: " + clientIP);
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientIP);
                clients.put(clientIP, clientHandler);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void lockClient(String clientIP) {
        ClientHandler clientHandler = clients.get(clientIP);
        if (clientHandler != null) {
            clientHandler.sendMessage("LOCK");
        } else {
            System.out.println("⚠️ Client " + clientIP + " không tồn tại hoặc đã ngắt kết nối.");
        }
    }

    public static void unlockClient(String clientIP) {
        ClientHandler clientHandler = clients.get(clientIP);
        if (clientHandler != null) {
            clientHandler.sendMessage("UNLOCK");
        } else {
            System.out.println("⚠️ Client " + clientIP + " không tồn tại hoặc đã ngắt kết nối.");
        }
    }

    public static void sendBalanceUpdate(String username, double newBalance) {
        ClientHandler clientHandler = clients.get(ipsByUsername.get(username));
        if (clientHandler != null) {
            String message = "BALANCE_UPDATE:" + newBalance;
            System.out.println("📤 Sending balance update to client: " + username + " -> " + message);
            System.out.println(clientHandler);
            clientHandler.sendMessage(message);
            System.out.println("✅ Message sent to client: " + username);
        } else {
            System.out.println("⚠️ No active client found for user: " + username + ". Balance update not sent.");
        }
    }

    public static void main(String[] args) {
        startServer();
    }
}
