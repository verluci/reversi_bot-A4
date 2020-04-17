package com.github.verluci.reversi.game.agents;

/**
 * Use this agent if you want to use a local agent for in a UI.
 */
public class LocalUIPlayerAgent extends Agent {
    private boolean doneWithTurn = false;
    private int x;
    private int y;

    /**
     * Use this method in your GUI to perform a move.
     * @param x The horizontal position of this move.
     * @param y The vertical position of this move.
     */
    public synchronized void doMove(int x, int y) {
        this.x = x;
        this.y = y;
        this.doneWithTurn = true;
        notify();
    }

    /**
     * This method waits until doMove is performed.
     */
    @Override
    public synchronized void performNextMove() {
        try {
            while (!doneWithTurn) {
                wait();
            }
            move(this.x, this.y);
            doneWithTurn = false;

        } catch (InterruptedException e) {
         System.err.println("Player unexpectedly closed the application!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
