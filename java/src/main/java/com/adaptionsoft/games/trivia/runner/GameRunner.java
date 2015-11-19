
package com.adaptionsoft.games.trivia.runner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.adaptionsoft.games.uglytrivia.Game;


public class GameRunner {


    public static void main(String[] args) {
        runWith(111, new Players("Chet", "Pat", "Sue"));
    }

    public static void runWith(int seed, Players players) {
        boolean notAWinner;

        Game aGame = new Game();

        for (String player : players) {
            aGame.add(player);
        }

        System.out.println("isPlayable = " + aGame.isPlayable());

        Random rand = new Random(seed);

        do {

            aGame.roll(rand.nextInt(5) + 1);

            if (rand.nextInt(9) == 7) {
                notAWinner = aGame.wrongAnswer();
            } else {
                notAWinner = aGame.wasCorrectlyAnswered();
            }


        } while (notAWinner);
    }
}
