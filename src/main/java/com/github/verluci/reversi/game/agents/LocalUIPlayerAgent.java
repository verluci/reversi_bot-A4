package com.github.verluci.reversi.game.agents;

public class LocalUIPlayerAgent extends Agent {
    boolean doneWithTurn = false;
    int x;
    int y;

    public LocalUIPlayerAgent() {
        super();

    }

    public synchronized void doMove(int x, int y) {
        this.x = x;
        this.y = y;
        this.doneWithTurn = true;
        notify();
    }

    @Override
    public synchronized void performNextMove() {
        try {
            while (!doneWithTurn) {
                wait();
            }
            move(this.x, this.y);
            doneWithTurn = false;

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
