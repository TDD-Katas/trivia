package com.adaptionsoft.games.uglytrivia;

import java.util.ArrayList;
import java.util.LinkedList;

public class Game {
    ArrayList players = new ArrayList();
    int[] places = new int[6];
    int[] purses = new int[6];
    boolean[] inPenaltyBox = new boolean[6];

    LinkedList popQuestions = new LinkedList();
    LinkedList scienceQuestions = new LinkedList();
    LinkedList sportsQuestions = new LinkedList();
    LinkedList rockQuestions = new LinkedList();

    int currentPlayerIndex = 0;
    boolean isGettingOutOfPenaltyBox;

    public Game() {
        for (int i = 0; i < 50; i++) {
            popQuestions.addLast("Pop Question " + i);
            scienceQuestions.addLast(("Science Question " + i));
            sportsQuestions.addLast(("Sports Question " + i));
            rockQuestions.addLast("Rock Question " + i);
        }
    }

    public boolean p_isPlayable() {
        return (totalPlayers(players) >= 2);
    }

    public boolean p_add(String playerName) {
        players.add(playerName);
        places[totalPlayers(players)] = 0;
        purses[totalPlayers(players)] = 0;
        inPenaltyBox[totalPlayers(players)] = false;

        display(playerName + " was added");
        display("They are player number " + totalPlayers(players));
        return true;
    }

    public void p_roll(int roll) {
        display(getCurrentPlayerName(players, currentPlayerIndex) + " is the current player");
        display("They have rolled a " + roll);

        if (isPlayerInPenaltyBox(currentPlayerIndex, inPenaltyBox)) {
            game_checkIfPlayerCanGetOutOfPenaltyBox(roll);
        }

        if (!isPlayerInPenaltyBox(currentPlayerIndex, inPenaltyBox) || isGettingOutOfPenaltyBox) {
            place_changePlaceForPlayer(currentPlayerIndex, roll);
            String category = getCategoryForPlayerPlace(places[currentPlayerIndex]);
            display("The category is " + category);

            cat_askQuestion_and_removeQuestionFromCategory(category);
        }
    }

    private void game_checkIfPlayerCanGetOutOfPenaltyBox(int roll) {
        if (roll % 2 != 0) {
            display(getCurrentPlayerName(players, currentPlayerIndex) + " is getting out of the penalty box");
            isGettingOutOfPenaltyBox = true;
        } else {
            display(getCurrentPlayerName(players, currentPlayerIndex) + " is not getting out of the penalty box");
            isGettingOutOfPenaltyBox = false;
        }
    }

    private void place_changePlaceForPlayer(int player, int roll) {
        places[this.currentPlayerIndex] = places[player] + roll;
        if (places[this.currentPlayerIndex] > 11) places[this.currentPlayerIndex] = places[this.currentPlayerIndex] - 12;

        display(getCurrentPlayerName(players, currentPlayerIndex)
                + "'s new location is "
                + places[currentPlayerIndex]);
    }

    private void cat_askQuestion_and_removeQuestionFromCategory(String currentCategory) {
        LinkedList categoryQuestions = new LinkedList();

        if (currentCategory == "Pop")
            categoryQuestions = popQuestions;
        if (currentCategory == "Science")
            categoryQuestions = scienceQuestions;
        if (currentCategory == "Sports")
            categoryQuestions = sportsQuestions;
        if (currentCategory == "Rock")
            categoryQuestions = rockQuestions;

        System.out.println(categoryQuestions.peekFirst());
        categoryQuestions.removeFirst();
    }

    boolean lastQuestionWasAnswered;
    public void p_wasCorrectlyAnswered() {
        if (isPlayerInPenaltyBox(currentPlayerIndex, inPenaltyBox) && !isGettingOutOfPenaltyBox) {
            lastQuestionWasAnswered = false;
        } else {
            displayCorrectAnswer();
            awardCoinToCurrentPlayer(currentPlayerIndex, purses);
            displayPlayerCoins(getCurrentPlayerName(players, currentPlayerIndex), getCoinsForPlayer(currentPlayerIndex, purses));
            lastQuestionWasAnswered = true;
        }
    }


    public void p_wrongAnswer() {
        displayQuestionIncorrect();

        displayPlayerSentToPenaltyBox(getCurrentPlayerName(players, currentPlayerIndex));
        putPlayerInPenaltyBox(currentPlayerIndex, inPenaltyBox);

        lastQuestionWasAnswered = false;
    }

    public boolean p_shouldContinueGame() {
        if (lastQuestionWasAnswered) {
            return !didPlayerWin(currentPlayerIndex, purses);
        } else {
            return true;
        }

    }

    public void game_moveToNextPlayer() {
        game_incrementPlayerIndex();
        game_resetPlayerIfLast(currentPlayerIndex, players);
    }

    //~~~~ Basic Class methods

    private void game_incrementPlayerIndex() {
        currentPlayerIndex++;
    }

    private void game_resetPlayerIfLast(int currentPlayer, ArrayList players) {
        if (currentPlayer == totalPlayers(players))
            this.currentPlayerIndex = 0;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //~~~~ PURE purse related

    private static boolean didPlayerWin(int currentPlayerIndex, int[] purses) {
        return getCoinsForPlayer(currentPlayerIndex, purses) == 6;
    }

    private static void awardCoinToCurrentPlayer(int currentPlayerIndex, int[] purses) {
        purses[currentPlayerIndex]++;
    }

    private static int getCoinsForPlayer(int currentPlayer, int[] purses) {
        return purses[currentPlayer];
    }

    //~~~~ PURE players related

    private static Object getCurrentPlayerName(ArrayList players, int currentPlayerIndex) {
        return players.get(currentPlayerIndex);
    }

    private static int totalPlayers(ArrayList players) {
        return players.size();
    }

    //~~~~ PURE penalty box

    private static boolean isPlayerInPenaltyBox(int playerIndex, boolean[] penaltyBox) {
        return penaltyBox[playerIndex];
    }

    private static void putPlayerInPenaltyBox(int playerIndex, boolean[] penaltyBox) {
        penaltyBox[playerIndex] = true;
    }

    //~~~~ PURE category related

    private static String getCategoryForPlayerPlace(int placesValue) {
        if (placesValue == 0) return "Pop";
        if (placesValue == 4) return "Pop";
        if (placesValue == 8) return "Pop";
        if (placesValue == 1) return "Science";
        if (placesValue == 5) return "Science";
        if (placesValue == 9) return "Science";
        if (placesValue == 2) return "Sports";
        if (placesValue == 6) return "Sports";
        if (placesValue == 10) return "Sports";
        return "Rock";
    }

    //~~~~ PURE display related

    private static void displayPlayerSentToPenaltyBox(Object playerName) {
        display(playerName + " was sent to the penalty box");
    }

    private static void displayPlayerCoins(Object player, int coins) {
        display(player
                + " now has "
                + coins
                + " Gold Coins.");
    }

    private static void displayQuestionIncorrect() {
        display("Question was incorrectly answered");
    }

    private static void displayCorrectAnswer() {
        display("Answer was correct!!!!");
    }

    private static void display(String x) {
        System.out.println(x);
    }
}
