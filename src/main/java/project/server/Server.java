package project.server;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static Set<String> userNames = Collections.synchronizedSet(new HashSet<>());
    private static int count = 1;
    private static Room curRoom;

    public static void main(String[] args) {
        try {
            File configFile = new File("src/main/resources/config.xml");
            XmlExtractor xmlExtractor = new XmlExtractor(configFile);

            final int NUM_OF_PLAYERS_IN_ROOM = Integer.parseInt(xmlExtractor.getRootChildText("playersInRoom"));
            final int TIME_BANK_PORT_NUMBER = Integer.parseInt(xmlExtractor.getRootChildText("portNumber"));
            final int TIMEOUT = Integer.parseInt(xmlExtractor.getRootChildText("timeout"));
            curRoom = new Room(TIMEOUT, NUM_OF_PLAYERS_IN_ROOM);

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(TIME_BANK_PORT_NUMBER);
            } catch (IOException e) {
                System.out.println("IOException caught when trying to listen on port " + TIME_BANK_PORT_NUMBER);
                System.out.println(e.getMessage());
                System.exit(1);
            }

            while (true) {
                Socket clientSocket;
                try {
                    clientSocket = serverSocket.accept();
                    new Thread(new PlayerHandlerTask(clientSocket, curRoom, userNames)).start();
                    if (++count > NUM_OF_PLAYERS_IN_ROOM) {
                        curRoom = new Room(TIMEOUT, NUM_OF_PLAYERS_IN_ROOM);
                        count = 1;
                    }
                } catch (IOException e) {
                    System.out.println("IOException caught while listening for a connection");
                    e.getMessage();
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            System.err.println("An error occurred while parsing xml file");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
