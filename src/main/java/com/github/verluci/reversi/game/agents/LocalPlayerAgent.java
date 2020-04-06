package com.github.verluci.reversi.game.agents;

import java.util.Scanner;

/**
 * Use this agent if you want to play as a local-player using the cli-scanner.
 */
public class LocalPlayerAgent extends Agent {
    /**
     * The constructor for LocalPlayerAgent
     */
    public LocalPlayerAgent() {
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
