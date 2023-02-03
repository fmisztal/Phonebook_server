package Lab5.Zad_2;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class PhonebookClient {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (true) {
            String commandLine = in.nextLine().toLowerCase();
            String[] commandParameters = commandLine.split(" ");

            try {
                switch (commandParameters[0]) {
                    case "put" -> {
                        if (commandParameters.length == 3)
                            Integer.parseInt(commandParameters[2]);
                        else
                            throw new Exception("Invalid number of parameters for put method!");
                    }
                    case "get" -> {
                        if (commandParameters.length != 2)
                            throw new Exception("Invalid number of parameters for get method!");
                    }
                    default -> throw new Exception("Incorrect request!");
                }

                Socket socket = new Socket("localhost", 12129);
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os, true);
                InputStream is = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                pw.println(commandLine);

                String line;
                while ((line = br.readLine()) != null)
                    System.out.println(line);

                br.close();
                socket.close();
            } catch (NumberFormatException e) {
                System.err.println("[number] must be Integer!");
            } catch (Exception e) {
                System.err.println("Client exception: " + e);
            }
        }
    }
}
