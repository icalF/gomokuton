package actor;

import adt.Board;
import adt.Point;

import java.io.*;

/**
 * Class actor.Player
 * actor.Player resource controller
 *
 * @author Afrizal
 */
public class Player implements Runnable {
    Board board;
    PrintStream os;
    BufferedReader is;
    byte id;

    public Player(PrintStream os, BufferedReader is, Board board) {
        this.os = os;
        this.is = is;
        this.board = board;
        id = board.addPlayer();
    }

    /**
     * Thread for player action-response
     */
    public void run() {
        String line;
        try {
            os.println("Gomoku o youkoso ^^");
            while (true) {
                os.println("Are you ready? [ready]");
                while (true) {
                    do {
                        line = is.readLine();
                    } while (line.length() < 1);

                    if (line.equals("ready")) {
                        synchronized (board.playerLock) {
                            board.readyAll |= (1 << (id - (byte) 'A'));
                        }

                        break;
                    }
                }

                os.println(board.players);
                while (board.readyAll != board.players) {       // wait till all ready
                    System.out.println((char) board.turn);
                }

                os.println((char) id);
                // os.flush();

                while (true) {
                    int r = 0;

                    printBoard(os);

                    if (board.turn == id)                           // check turn
                    {
                        line = is.readLine();
                        if (line.length() > 0) {
                            int x = (byte) line.charAt(0) - (byte) 'a';
                            int y = (byte) line.charAt(1) - (byte) 'a';

                            if ((x >= 0 && x < Board.WIDTH) && (y >= 0 && y < Board.WIDTH)) {
                                r = board.move(new Point(x, y));

                                if (r == -1) {
                                    os.println("Invalid move");
                                    continue;
                                } else {
                                    synchronized (board.playerLock) {
                                        board.changed = board.players;
                                        board.changed &= ~(1 << (id - (byte) 'A'));
                                    }

                                    if (r != 0)
                                        board.reset();
                                }
                            } else {
                                continue;
                            }
                        }

                    } else {
                        while (board.changed == 0) {                    // wait until adt changed
                            System.out.println((char) board.turn);
                        }

                        synchronized (board.playerLock) {               // 'read' flag
                            board.changed &= ~(1 << (id - (byte) 'A'));
                        }
                    }

                    while (board.changed != 0) {                      // wait until all player 'read' the changes
                        System.out.println((char) board.turn);
                    }

                    if (board.readyAll == 0)          // if someone win
                    {
                        os.println(r == id ? "win" : "lose");
                        break;
                    }
                }

            }
        } catch (NullPointerException e) {
            board.removePlayer(id);      // delete player from adt
            e.printStackTrace();
        } catch (IOException e) {
        } catch (RuntimeException e) {
            System.out.println("Error occurred : " + e);
            System.exit(1);
        }
    }

    /**
     * Prints game adt
     *
     * @param s Stream to print to
     */
    public void printBoard(PrintStream s) {
        s.println();
        board.print(s);
        s.println();

        if (id == board.turn)
            s.println("It's your turn");
        else
            s.println("It's your opponent's turn");
    }
}