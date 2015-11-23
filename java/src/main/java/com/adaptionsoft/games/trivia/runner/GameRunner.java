
package com.adaptionsoft.games.trivia.runner;

import com.adaptionsoft.games.uglytrivia.Game;

import java.util.Random;


public class GameRunner {


    public static void main(String[] args) {
        runWith(111, new Players("Chet", "Pat", "Sue"));
    }

    public static void runWith(int seed, Players players) {
        boolean notAWinner;

        Game aGame = new Game();

        for (String player : players) {
            aGame.p_add(player);
        }

        System.out.println("isPlayable = " + aGame.p_isPlayable());

        Random rand = new Random(seed);

        do {

            aGame.p_roll(rand.nextInt(5) + 1);
            int answer = rand.nextInt(9);

            if (aGame.p_isCurrentPlayerAllowedToAnswer()) {
                aGame.p_answer(answer);
            }
            notAWinner = aGame.p_shouldContinueGame();

            aGame.game_moveToNextPlayer();
        } while (notAWinner);
    }
}
