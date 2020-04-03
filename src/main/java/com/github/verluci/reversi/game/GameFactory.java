package com.github.verluci.reversi.game;

import java.lang.reflect.InvocationTargetException;

/**
 * This class contains a function for generating a Game based on a class-name-definition.
 */
public class GameFactory {
    /**
     * This function creates a new game based on the specified Game class-type
     * @param gameType The type of game you want to create.
     * @return A new Game object as the specified implementation.
     */
    public static Game createGame(Class<Game> gameType) {
        try {
            return gameType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }
}
