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
            rockQuestions.addLast(createRockQuestion(i));
        }
    }

    public String createRockQuestion(int index) {
        return "Rock Question " + index;
    }

    public boolean isPlayable() {
        return (howManyPlayers() >= 2);
    }

    public boolean add(String playerName) {


        players.add(playerName);
        places[howManyPlayers()] = 0;
        purses[howManyPlayers()] = 0;
        inPenaltyBox[howManyPlayers()] = false;

        display(playerName + " was added");
        display("They are player number " + totalPlayers(players));
        return true;
    }

    public int howManyPlayers() {
        return totalPlayers(players);
    }

    public void roll(int roll) {
        display(getCurrentPlayerName() + " is the current player");
        display("They have rolled a " + roll);

        if (isPlayerInPenaltyBox(currentPlayerIndex)) {
            checkIfPlayerCanGetOutOfPenaltyBox(roll);
        }

        if (!isPlayerInPenaltyBox(currentPlayerIndex) || isGettingOutOfPenaltyBox) {
            changePlaces_and_askQuestion(roll);
        }


    }

    private void checkIfPlayerCanGetOutOfPenaltyBox(int roll) {
        if (roll % 2 != 0) {
            display(getCurrentPlayerName() + " is getting out of the penalty box");
            isGettingOutOfPenaltyBox = true;
        } else {
            display(getCurrentPlayerName() + " is not getting out of the penalty box");
            isGettingOutOfPenaltyBox = false;
        }
    }

    private void changePlaces_and_askQuestion(int roll) {
        places[currentPlayerIndex] = places[currentPlayerIndex] + roll;
        if (places[currentPlayerIndex] > 11) places[currentPlayerIndex] = places[currentPlayerIndex] - 12;

        display(getCurrentPlayerName()
                + "'s new location is "
                + places[currentPlayerIndex]);
        display("The category is " + getCategoryForPlaces(places[currentPlayerIndex]));
        askQuestion_and_removeQuestionFromList(places[currentPlayerIndex]);
    }

    private void askQuestion_and_removeQuestionFromList(int placesValue) {
        String currentCategory = getCategoryForPlaces(placesValue);
        if (currentCategory == "Pop")
            System.out.println(popQuestions.removeFirst());
        if (currentCategory == "Science")
            System.out.println(scienceQuestions.removeFirst());
        if (currentCategory == "Sports")
            System.out.println(sportsQuestions.removeFirst());
        if (currentCategory == "Rock")
            System.out.println(rockQuestions.removeFirst());
    }

    boolean lastQuestionWasAnswered;
    public boolean wasCorrectlyAnswered() {
        if (isPlayerInPenaltyBox(currentPlayerIndex) && !isGettingOutOfPenaltyBox) {
            lastQuestionWasAnswered = false;
            return true;
        } else {
            displayCorrectAnswer();
            awardCoinToCurrentPlayer();
            displayPlayerCoins(getCurrentPlayerName(), getCoinsForPlayer(currentPlayerIndex));
        }


        return shouldContinueGame();
    }


    public boolean wrongAnswer() {
        displayQuestionIncorrect();

        displayPlayerSentToPenaltyBox(getCurrentPlayerName());
        putPlayerInPenaltyBox(currentPlayerIndex);

        return true;
    }

    private boolean shouldContinueGame() {


        return !didPlayerWin(currentPlayerIndex);
    }

    public void moveToNextPlayer() {
        incrementPlayerIndex();
        resetPlayerIfLast(currentPlayerIndex, players);
    }

    //~~~~ More complex methods

    private boolean didPlayerWin(int currentPlayerIndex) {
        return getCoinsForPlayer(currentPlayerIndex) == 6;
    }

    //~~~~ Basic Class methods

    private void awardCoinToCurrentPlayer() {
        purses[currentPlayerIndex]++;
    }

    private void incrementPlayerIndex() {
        currentPlayerIndex++;
    }

    private Object getCurrentPlayerName() {
        return players.get(currentPlayerIndex);
    }

    private boolean isPlayerInPenaltyBox(int playerIndex) {
        return inPenaltyBox[playerIndex];
    }

    private void putPlayerInPenaltyBox(int playerIndex) {
        inPenaltyBox[playerIndex] = true;
    }

    private void resetPlayerIfLast(int currentPlayer, ArrayList players) {
        if (currentPlayer == totalPlayers(players))
            resetPlayer();
    }

    private void resetPlayer() {
        this.currentPlayerIndex = 0;
    }

    private int getCoinsForPlayer(int currentPlayer) {
        return purses[currentPlayer];
    }

    //~~~~ Extracted pure functions

    private static String getCategoryForPlaces(int placesValue) {
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

    private static int totalPlayers(ArrayList players) {
        return players.size();
    }

    private static void display(String x) {
        System.out.println(x);
    }
}
