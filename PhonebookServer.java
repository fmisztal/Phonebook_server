package Lab5.Zad_2;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhonebookServer {
    /**
     * Klasa servera; zawiera wszystkie rozszerzenia, czyli wielowątkowość, protokół rozmyty oraz zapis do pliku
     */
    public static Map<String, String> phonebook = new HashMap<>();
    public static final String database = "..\\OPA22Z_Misztal_Filip\\src\\Lab5\\Zad_2\\database.txt";
    private static class ClientHandler implements Runnable {
        private Socket socket;
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os, true);

                String request = br.readLine();
                if (request == null)
                    throw new Exception("Empty request!");
                String[] reqArgs = request.split(" ");
                String command = reqArgs[0];
                String name = reqArgs[1];

                switch (command) {
                    case "put" -> {
                        String number = reqArgs[2];
                        synchronized (phonebook) {
                            if (phonebook.get(name) != number) {
                                if (!phonebook.containsKey(name)) {
                                    phonebook.put(name, number);
                                    BufferedWriter writer = new BufferedWriter(new FileWriter(database, true));
                                    writer.write(name + " " + number + "\n");
                                    writer.close();
                                }
                                else {
                                    phonebook.put(name, number);
                                    BufferedWriter writer = new BufferedWriter(new FileWriter(database, false));
                                    for (Map.Entry<String, String> mapElement : phonebook.entrySet())
                                        writer.write(mapElement.getKey() + " " + mapElement.getValue() + "\n");
                                    writer.close();
                                }
                            }
                            pw.println("[" + name + "] [" + number + "]");
                        }
                    }
                    case "get" -> {
                        boolean isNameFound = false;
                        synchronized (phonebook) {
                            for (Map.Entry<String, String> element : phonebook.entrySet()) {
                                if (element.getKey().startsWith(name)) {
                                    pw.println("[" + element.getKey() + "] [" + element.getValue() + "]");
                                    isNameFound = true;
                                }
                            }
                        }
                        if (!isNameFound)
                            pw.println("--- no name: [" + name + "] ---");
                    }
                }
                br.close();
                socket.close();
            } catch (Exception e) {
                System.err.println("Server exception: " + e);
            }
        }
    }

    public static void main(String... args) throws Exception {
        InetAddress localHost = InetAddress.getLocalHost();
        System.out.println("localHost.getHostAddress() = " + localHost.getHostAddress());
        System.out.println("localHost.getHostName() = " + localHost.getHostName());

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(12129);
        } catch (Exception ex) {
            System.err.println("Create server socket: " + ex);
            return;
        }

        try {
            List<String> databaseLines = Files.readAllLines(Paths.get(database));
            for (String line : databaseLines) {
                String[] mapElement = line.split(" ");
                phonebook.put(mapElement[0], mapElement[1]);
            }
            System.out.println("Database loaded.");
        } catch (NoSuchFileException ex) {
            System.out.println("Starting without database.");
        }

        int l = 0;
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
            System.out.println("Client: " + l++);
        }
    }
}
