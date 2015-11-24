
package com.adaptionsoft.games.trivia.runner;

import com.adaptionsoft.games.uglytrivia.Game;

import java.util.Random;


public class GameRunner {


    public static void main(String[] args) {
        runWith(111, new String[]{"Chet", "Pat", "Sue"});
    }

    public static void runWith(int seed, String[] players) {
        Game aGame = new Game();

        for (String player : players) {
            aGame.game_builder_add(player);
        }

        System.out.println("isPlayable = " + aGame.game_builder_isPlayable());

        Random rand = new Random(seed);

        aGame.p_theGameLoop(rand);
    }
}
