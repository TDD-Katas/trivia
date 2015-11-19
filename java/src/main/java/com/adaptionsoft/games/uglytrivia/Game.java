package com.adaptionsoft.games.uglytrivia;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

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
        RollResult rollResult = do_roll(roll, players, currentPlayerIndex, inPenaltyBox, places, isGettingOutOfPenaltyBox, popQuestions, scienceQuestions, sportsQuestions, rockQuestions);

        places[currentPlayerIndex] = rollResult.getNewPlayerPlace();
        isGettingOutOfPenaltyBox = rollResult.isGettingOutOfPenaltyBox();


        LinkedList categoryUsedForQuestion = rollResult.getCategoryUsedForQuestion();
        if (!categoryUsedForQuestion.isEmpty()) {
            categoryUsedForQuestion.removeFirst();
        }
    }

    static class RollResult {
        private final int newPlayerPlace;
        private final boolean isGettingOutOfPenaltyBox;
        private final LinkedList categoryUsedForQuestion;

        public RollResult(int newPlayerPlace, boolean isGettingOutOfPenaltyBox, LinkedList categoryUsedForQuestion) {
            this.newPlayerPlace = newPlayerPlace;
            this.isGettingOutOfPenaltyBox = isGettingOutOfPenaltyBox;
            this.categoryUsedForQuestion = categoryUsedForQuestion;
        }

        public int getNewPlayerPlace() {
            return newPlayerPlace;
        }

        public boolean isGettingOutOfPenaltyBox() {
            return isGettingOutOfPenaltyBox;
        }

        public LinkedList getCategoryUsedForQuestion() {
            return categoryUsedForQuestion;
        }
    }

    private static RollResult do_roll(int roll, ArrayList players, int currentPlayerIndex, boolean[] inPenaltyBox, int[] places, boolean isGettingOutOfPenaltyBox, LinkedList popQuestions, LinkedList scienceQuestions, LinkedList sportsQuestions, LinkedList rockQuestions) {
        display(getCurrentPlayerName(players, currentPlayerIndex) + " is the current player");
        display("They have rolled a " + roll);

        boolean newValueForIsGettingOutOfPenaltyBox = isGettingOutOfPenaltyBox;
        if (isPlayerInPenaltyBox(currentPlayerIndex, inPenaltyBox)) {
            newValueForIsGettingOutOfPenaltyBox = game_checkIfPlayerCanGetOutOfPenaltyBox(roll, currentPlayerIndex, players);
        }


        int playerPlace = places[currentPlayerIndex];
        LinkedList categoryToUse = new LinkedList();
        if (!isPlayerInPenaltyBox(currentPlayerIndex, inPenaltyBox) || newValueForIsGettingOutOfPenaltyBox) {
            playerPlace = place_changePlaceForPlayer(roll, places, currentPlayerIndex, players);

            String category = getCategoryForPlayerPlace(playerPlace);
            display("The category is " + category);

            if (category == "Pop")
                categoryToUse = popQuestions;
            if (category == "Science")
                categoryToUse = scienceQuestions;
            if (category == "Sports")
                categoryToUse = sportsQuestions;
            if (category == "Rock")
                categoryToUse = rockQuestions;

            System.out.println(categoryToUse.peekFirst());
        }

        return new RollResult(playerPlace, newValueForIsGettingOutOfPenaltyBox, categoryToUse);
    }


    private static boolean game_checkIfPlayerCanGetOutOfPenaltyBox(int roll, int currentPlayerIndex, ArrayList players) {
        if (roll % 2 != 0) {
            display(getCurrentPlayerName(players, currentPlayerIndex) + " is getting out of the penalty box");
            return true;
        } else {
            display(getCurrentPlayerName(players, currentPlayerIndex) + " is not getting out of the penalty box");
            return false;
        }
    }

    private static int place_changePlaceForPlayer(int roll, int[] places, int currentPlayerIndex, ArrayList players) {
        int value = places[currentPlayerIndex] + roll;
        if (value > 11) value = value - 12;

        display(getCurrentPlayerName(players, currentPlayerIndex)
                + "'s new location is "
                + value);

        return value;
    }

    public void p_answer(int value) {
        if (value == 7) {
            game_playerAnswerIsWrong();
        } else {
            game_playerAnswerIsCorrect();
        }
    }

    boolean lastQuestionWasAnswered;
    private void game_playerAnswerIsCorrect() {
        if (isPlayerInPenaltyBox(currentPlayerIndex, inPenaltyBox) && !isGettingOutOfPenaltyBox) {
            lastQuestionWasAnswered = false;
        } else {
            displayCorrectAnswer();
            awardCoinToCurrentPlayer(currentPlayerIndex, purses);
            displayPlayerCoins(getCurrentPlayerName(players, currentPlayerIndex), getCoinsForPlayer(currentPlayerIndex, purses));
            lastQuestionWasAnswered = true;
        }
    }


    private void game_playerAnswerIsWrong() {
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
