package com.github.lutzenh.networking;

import org.apache.commons.net.telnet.TelnetClient;
import org.json.*;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import com.github.lutzenh.networking.GameClientExceptions.*;

public class TelnetGameClient implements GameClient {
    private TelnetClient telnet;
    private DataOutputStream out;
    private DataInputStream in;

    boolean isConnected;

    private BlockingQueue<String> returnQueue;
    Runnable returnedInfo;
    Runnable processQueue;

    private volatile Player[] playerList;
    private volatile String[] gameList;
    private volatile boolean isOK;
    private volatile String error;

    public TelnetGameClient() {
        returnQueue = new SynchronousQueue<>();

        returnedInfo = () -> {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                while (isConnected) {
                    byte currentChar = in.readByte();

                    if(currentChar == '\n') {
                        String constructedString = stringBuilder.toString();
                        returnQueue.put(constructedString);
                        stringBuilder.setLength(0);
                    } else {
                        stringBuilder.append((char) currentChar);
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        };

        processQueue  = () -> {
            while (isConnected) {
                try {
                    String string = returnQueue.take();
                    System.out.println(string);

                    String jsonString;
                    JSONArray array;

                    if(string.startsWith("SVR")) {
                        String[] split = string.split(" ");

                        switch (split[1]) {
                            case "PLAYERLIST":
                                jsonString = string.substring("SVR PLAYERLIST ".length());
                                array = new JSONArray(jsonString);

                                Player[] playerList = new Player[array.length()];
                                for (int i = 0; i < array.length(); i++) {
                                    playerList[i] = new Player(array.getString(i));
                                }
                                this.playerList = playerList;

                                break;
                            case "GAMELIST":
                                jsonString = string.substring("SVR GAMELIST ".length());
                                array = new JSONArray(jsonString);

                                String[] gameList = new String[array.length()];
                                for (int i = 0; i < array.length(); i++) {
                                    gameList[i] = array.getString(i);
                                }

                                this.gameList = gameList;
                                break;
                        }
                    }
                    else if (string.startsWith("ERR")) {
                        error = string;
                    }
                    else if (string.startsWith("OK")) {
                        isOK = true;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    //region Game Methods

    /**
     * Use this method to connect to a given telnet-server
     * @param hostname The hostname of this server
     * @param port The port of this server. (usually 7789)
     * @throws ConnectionException Thrown when a connection to the server fails.
     */
    @Override
    public void connect(String hostname, int port) throws ConnectionException {
        try {
            if(!isConnected) {
                telnet = new TelnetClient();
                telnet.connect(hostname, port);

                out = new DataOutputStream(telnet.getOutputStream());
                in = new DataInputStream(telnet.getInputStream());

                isConnected = true;
                new Thread(returnedInfo).start();
                new Thread(processQueue).start();
            }
        } catch (IOException e) {
            disconnect();
            throw new ConnectionException("Failed to connect to server: " + hostname + ":" + port);
        }
    }

    /**
     * Disconnects you from the server.
     * @throws ConnectionException Thrown when disconnecting from the server fails.
     */
    @Override
    public void disconnect() throws ConnectionException {
        try {
            isConnected = false;
            out.close();
            in.close();
        } catch (IOException e) {
            throw new ConnectionException("Failed to disconnect from the server!");
        }
    }

    /**
     * @return True if you are currently connected to the server.
     */
    @Override
    public boolean getConnected() {
        return isConnected;
    }

    /**
     * Use this method if you want to login to the server with a certain username.
     * @param username the unique username you want to use to connect to the server.
     * @throws LoginException Thrown when the username already exists or if the command has failed.
     */
    @Override
    public void login(String username) throws LoginException {
        try {
            sendAndReceiveProtocol("login " + username);
        } catch (Exception e) {
            throw new LoginException(e.getMessage());
        }
    }

    @Override
    public void logout() throws LoginException { }

    /**
     * Retrieves a list of games that the server supports.
     * @return A String[] array of games that the server supports.
     */
    @Override
    public String[] getGameList() {
        try {
            String output = "get gamelist\n";
            out.writeBytes(output);
            out.flush();

            gameList = null;
            while (gameList == null) {
                Thread.onSpinWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gameList.clone();
    }

    /**
     * Retrieves all the players that are currently online.
     * @return A list of players that are online.
     */
    @Override
    public Player[] getPlayerList() {
        try {
            String output = "get playerlist\n";
            out.writeBytes(output);
            out.flush();

            playerList = null;
            while (playerList == null) {
                Thread.onSpinWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return playerList.clone();
    }

    /**
     * Use this method if you want to subscribe to a game inorder to auto-join a match.
     * @param gameName The name of the game you want to subscribe to.
     * @throws SubscribeException Thrown when the response is not OK but an ERR.
     */
    @Override
    public void subscribeToGame(String gameName) throws SubscribeException {
        try {
            sendAndReceiveProtocol("subscribe " + gameName);
        } catch (Exception e) {
            throw new SubscribeException(e.getMessage());
        }
    }

    @Override
    public void challengePlayer(String playerName, String gameName) { }

    @Override
    public void acceptChallenge(int challengeId) { }

    @Override
    public void performMove(int position) { }

    /**
     * Use this method when you want to give up during a match.
     * @throws MoveException Thrown when the response is not OK but an ERR.
     */
    @Override
    public void forfeit() throws MoveException {
        try {
            sendAndReceiveProtocol("forfeit");
        } catch (Exception e) {
            throw new MoveException(e.getMessage());
        }
    }

    //endregion

    /**
     * Universal telnet send and receive used by the different telnet game methods.
     * @param command The command that needs to be sent to the server.
     * @throws Exception Thrown when the response is not OK but an ERR.
     */
    private void sendAndReceiveProtocol(String command) throws Exception {
        try {
            String output = command + "\n";
            out.writeBytes(output);
            out.flush();

            isOK = false;
            error = null;
            while (!isOK && error == null) {
                Thread.onSpinWait();
            }

            if(error != null)
                throw new Exception(error);

        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Failed command:" + command);
        }
    }

    // This entry-point is for testing only!
    // TODO: Remove this entry-point when all game-methods have been implemented.
    public static void main(String[] args) throws ConnectionException, LoginException, SubscribeException, MoveException {
        TelnetGameClient gameClient = new TelnetGameClient();

        try {
            gameClient.connect("localhost", 7789);
        } catch (ConnectionException e) {
            e.printStackTrace();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        while (gameClient.getConnected()) {
            String input = scanner.nextLine();
            var splitInput = input.split(" ");
            switch (splitInput[0]) {
                case "login":
                    gameClient.login(splitInput[1]);
                    break;
                case "sub":
                    gameClient.subscribeToGame(splitInput[1]);
                    break;
                case "exit":
                    gameClient.disconnect();
                    break;
                case "players":
                    var playerList = gameClient.getPlayerList();
                    System.out.println(Arrays.toString(playerList));
                    break;
                case "games":
                    var gameList = gameClient.getGameList();
                    System.out.println(Arrays.toString(gameList));
                    break;
                case "forfeit":
                    gameClient.forfeit();
                    break;
            }
        }
    }
}
