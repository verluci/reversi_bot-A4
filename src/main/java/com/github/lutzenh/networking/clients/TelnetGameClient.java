package com.github.lutzenh.networking.clients;

import com.github.lutzenh.networking.types.*;
import org.apache.commons.net.telnet.TelnetClient;
import org.json.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import com.github.lutzenh.networking.GameClientExceptions.*;

public class TelnetGameClient extends GameClient {
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

    private ConcurrentHashMap<Integer, Challenge> activeChallenges;

    /**
     * Constructor for TelnetGameClient.
     * Use the method .connect() in this class if you want to initiate a connection.
     */
    public TelnetGameClient() {
        returnQueue = new SynchronousQueue<>();
        activeChallenges = new ConcurrentHashMap<>();

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

        // TODO: Remove code-duplication and case-tree.
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
                            case "GAME":
                                switch (split[2]) {
                                    case "CHALLENGE":
                                        if(split[3].equals("CANCELLED")) {
                                            jsonString = string.substring("SVR GAME CHALLENGE CANCELLED ".length());
                                            JSONObject object = new JSONObject(jsonString);
                                            int challengeId = Integer.parseInt(object.getString("CHALLENGENUMBER"));
                                            Challenge challenge = activeChallenges.remove(challengeId);
                                            notifyOnCancelChallenge(challenge);
                                        } else {
                                            jsonString = string.substring("SVR GAME CHALLENGE ".length());
                                            JSONObject object = new JSONObject(jsonString);

                                            int challengeId = Integer.parseInt(object.getString("CHALLENGENUMBER"));
                                            String playerName = object.getString("CHALLENGER");
                                            String gameType = object.getString("GAMETYPE");

                                            Challenge challenge = new Challenge(challengeId, new Player(playerName), gameType);
                                            activeChallenges.put(challengeId, challenge);
                                            notifyOnReceiveChallenge(challenge);
                                        }
                                        break;
                                    case "MATCH":
                                        GameStart gameStart;

                                        jsonString = string.substring("SVR GAME MATCH ".length());
                                        JSONObject object = new JSONObject(jsonString);

                                        String playerToMove = object.getString("PLAYERTOMOVE");
                                        String opponent = object.getString("OPPONENT");
                                        String gameType = object.getString("GAMETYPE");

                                        if(playerToMove.equals(opponent)) {
                                            Player enemy = new Player(opponent);
                                            gameStart = new GameStart(enemy, enemy, gameType);
                                        } else {
                                            Player starter = new Player(playerToMove);
                                            Player oppponent = new Player(opponent);
                                            gameStart = new GameStart(starter, oppponent, gameType);
                                        }
                                        notifyOnGameStart(gameStart);
                                        break;
                                    case "YOURTURN":
                                        jsonString = string.substring("SVR GAME YOURTURN ".length());
                                        JSONObject turnObject = new JSONObject(jsonString);
                                        String message = turnObject.getString("TURNMESSAGE");
                                        notifyOnTurn(message);
                                        break;
                                    case "MOVE":
                                        jsonString = string.substring("SVR GAME MOVE ".length());
                                        JSONObject moveObject = new JSONObject(jsonString);

                                        Player player = new Player(moveObject.getString("PLAYER"));
                                        int setMove = Integer.parseInt(moveObject.getString("MOVE"));
                                        String details = moveObject.getString("DETAILS");

                                        Move move = new Move(player, setMove, details);
                                        notifyOnMove(move);
                                        break;
                                    case "WIN":
                                    case "DRAW":
                                    case "LOSS":
                                        String entry = "SVR GAME " + split[2] + " ";
                                        jsonString = string.substring(entry.length());
                                        JSONObject endGameObject = new JSONObject(jsonString);

                                        int playerOneScore = Integer.parseInt(endGameObject.getString("PLAYERONESCORE"));
                                        int playerTwoScore = Integer.parseInt(endGameObject.getString("PLAYERTWOSCORE"));
                                        String comment = endGameObject.getString("COMMENT");

                                        GameResult result = GameResult.LOSS;
                                        switch (split[2]) {
                                            case "WIN":
                                                result = GameResult.WIN;
                                                break;
                                            case "DRAW":
                                                result = GameResult.DRAW;
                                                break;
                                            case "LOSS":
                                                result = GameResult.LOSS;
                                                break;
                                        }
                                        GameEnd gameEnd = new GameEnd(result, playerOneScore, playerTwoScore, comment);
                                        notifyOnGameEnd(gameEnd);
                                        break;
                                }
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
     * Use this method to connect to a given telnet-server.
     * This method creates two running threads that are used to process information received from the server.
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

    /**
     * Use this command if you want to logout, logging out closes the connection
     * (this is how the telnet-server has been implemented sadly).
     * @throws LoginException Thrown when logging out somehow fails.
     */
    @Override
    public void logout() throws LoginException {
        try {
            String output = "logout\n";
            out.writeBytes(output);
            out.flush();
        } catch (Exception e) {
            throw new LoginException(e.getMessage());
        }
    }

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
     * @return A list of challenges against you.
     */
    @Override
    public Challenge[] getChallenges() {
        var iterator = activeChallenges.values().iterator();
        List<Challenge> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);

        return list.toArray(new Challenge[0]);
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

    /**
     * Use this method if you want to challenge a player to a match.
     * @param player The player you want to challenge.
     * @param gameName The name of the game you want to play with the challenged player.
     * @throws ChallengePlayerException Thrown when challenging a player fails because of invalid Player-name or Game-name.
     */
    @Override
    public void challengePlayer(Player player, String gameName) throws ChallengePlayerException {
        try {
            sendAndReceiveProtocol("challenge \"" + player.getName() + "\" \"" + gameName + "\"");
        } catch (Exception e) {
            throw new ChallengePlayerException(e.getMessage());
        }
    }

    /**
     * Use this method when you want to accept a given challenge.
     * @param challenge The challenge that should be accepted.
     * @throws ChallengePlayerException Thrown when accepting a challenge fails.
     */
    @Override
    public void acceptChallenge(Challenge challenge) throws ChallengePlayerException{
        try {
            sendAndReceiveProtocol("challenge accept " + challenge.getNumber());
        } catch (Exception e) {
            throw new ChallengePlayerException(e.getMessage());
        }
    }

    /**
     * Performs a move on the given position. beware! will return OK on illegal moves!
     * @param position The position the move should be performed on.
     * @throws MoveException Thrown when a move fails because of invalid syntax (not because of illegal moves).
     */
    @Override
    public void performMove(int position) throws MoveException {
        try {
            sendAndReceiveProtocol("move " + position);
        } catch (Exception e) {
            throw new MoveException(e.getMessage());
        }
    }

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
}
