package com.github.verluci.reversi.game.entities;

import java.util.Scanner;

/**
 * Use this entity if you want to play as a local-player using the cli-scanner.
 */
public class LocalPlayerEntity extends Entity{
    /**
     * The constructor for LocalPlayerEntity
     */
    public LocalPlayerEntity() {
        super();
    }

    @Override
    public void performNextMove() {
        Scanner scanner = new Scanner(System.in);
        while (game.getCurrentPlayer() == player) {
            String input = scanner.nextLine();
            var splitInput = input.split(" ");

            int x = Integer.parseInt(splitInput[0]);
            int y = Integer.parseInt(splitInput[1]);

            move(x, y);
        }
    }
}
