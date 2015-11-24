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
    private final Questions questions;

    public Game() {
        for (int i = 0; i < 50; i++) {
            popQuestions.addLast("Pop Question " + i);
            scienceQuestions.addLast(("Science Question " + i));
            sportsQuestions.addLast(("Sports Question " + i));
            rockQuestions.addLast("Rock Question " + i);
        }
        questions = new Questions(popQuestions, scienceQuestions, sportsQuestions, rockQuestions);
    }

    public boolean p_round(int rollValue, int answer) {
        boolean notAWinner;


        RollResult rollResult = game_do_roll(rollValue, isGettingOutOfPenaltyBox,
                questions, inPenaltyBox[currentPlayerIndex],
                getCurrentPlayerName(players, currentPlayerIndex),
                places[currentPlayerIndex]);

        places[currentPlayerIndex] = rollResult.getNewPlayerPlace();
        isGettingOutOfPenaltyBox = rollResult.isGettingOutOfPenaltyBox();
        LinkedList categoryUsedForQuestion = rollResult.getCategoryUsedForQuestion();
        if (!categoryUsedForQuestion.isEmpty()) {
            categoryUsedForQuestion.removeFirst();
        }


        if (game_isPlayerAllowedToAnswer(isGettingOutOfPenaltyBox, inPenaltyBox[currentPlayerIndex])) {
            boolean currentlyInPenaltyBox = inPenaltyBox[currentPlayerIndex];
            AnswerResult answerResult = game_do_answer(answer, purses, currentPlayerIndex, players, currentlyInPenaltyBox);

            purses[currentPlayerIndex] = answerResult.getNewPurseValue();
            inPenaltyBox[currentPlayerIndex] = answerResult.getPlayerPenaltyBoxStatus();
        }


        notAWinner = !didPlayerWin(currentPlayerIndex, purses);

        currentPlayerIndex = game_getNextPlayer(currentPlayerIndex, totalPlayers(players));
        return notAWinner;
    }

    public boolean game_builder_isPlayable() {
        return (totalPlayers(players) >= 2);
    }

    public boolean game_builder_add(String playerName) {
        players.add(playerName);
        places[totalPlayers(players)] = 0;
        purses[totalPlayers(players)] = 0;
        inPenaltyBox[totalPlayers(players)] = false;

        display(playerName + " was added");
        display("They are player number " + totalPlayers(players));
        return true;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //~~~~ PURE game related


    private static int game_getNextPlayer(int currentPlayerIndex, int totalPlayers) {
        int currentPlayer = currentPlayerIndex;
        int nextPlayer = currentPlayer + 1;
        if (nextPlayer == totalPlayers)
            nextPlayer = 0;

        return nextPlayer;
    }

    private static boolean game_isPlayerAllowedToAnswer(boolean isGettingOutOfPenaltyBox, boolean isPlayerCurrentlyInThePenaltyBox) {
        return !isPlayerCurrentlyInThePenaltyBox || isGettingOutOfPenaltyBox;
    }

    private static boolean game_isCorrectAnswer(int value) {
        return value != 7;
    }


    private static RollResult game_do_roll(int roll, boolean currentPlayerIsGettingOutOfPenaltyBox,
                                           Questions questions, boolean currentPlayerIsCurrentlyInThePenaltyBox,
                                           Object currentPlayerName, int currentPlayerInitialPlace) {
        display(currentPlayerName + " is the current player");
        display("They have rolled a " + roll);

        boolean newValueForIsGettingOutOfPenaltyBox = currentPlayerIsGettingOutOfPenaltyBox;
        if (currentPlayerIsCurrentlyInThePenaltyBox) {
            newValueForIsGettingOutOfPenaltyBox = game_checkIfPlayerRollCanGetOutOfPenaltyBox(roll);

            if (newValueForIsGettingOutOfPenaltyBox) {
                displayPlayerIsGettingOutOfPenaltyBox(currentPlayerName);
            } else {
                displayPlayerIsStayingInThePenaltyBox(currentPlayerName);
            }
        }

        int newPlayerPlace = currentPlayerInitialPlace;
        LinkedList categoryToUse = new LinkedList();
        if (!currentPlayerIsCurrentlyInThePenaltyBox || newValueForIsGettingOutOfPenaltyBox) {
            newPlayerPlace = place_changePlaceForPlayer(roll, currentPlayerInitialPlace, currentPlayerName);

            String category = getCategoryForPlayerPlace(newPlayerPlace);
            display("The category is " + category);

            if (category == "Pop")
                categoryToUse = questions.getPopQuestions();
            if (category == "Science")
                categoryToUse = questions.getScienceQuestions();
            if (category == "Sports")
                categoryToUse = questions.getSportsQuestions();
            if (category == "Rock")
                categoryToUse = questions.getRockQuestions();

            System.out.println(categoryToUse.peekFirst());
        }

        return new RollResult(newPlayerPlace, newValueForIsGettingOutOfPenaltyBox, categoryToUse);
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

    //~~~~ PURE answer related

    private static AnswerResult game_do_answer(int value, int[] purses, int currentPlayerIndex, ArrayList players, boolean currentlyInPenaltyBox) {
        int currentPurseValue;
        boolean sendPlayerToPenaltyBox;

        if (game_isCorrectAnswer(value)) {
            displayCorrectAnswer();
            currentPurseValue = getCoinsForPlayer(currentPlayerIndex, purses) + 1;
            displayPlayerCoins(getCurrentPlayerName(players, currentPlayerIndex), currentPurseValue);
            sendPlayerToPenaltyBox = currentlyInPenaltyBox;
        } else {
            displayQuestionIncorrect();
            currentPurseValue = getCoinsForPlayer(currentPlayerIndex, purses);
            displayPlayerSentToPenaltyBox(getCurrentPlayerName(players, currentPlayerIndex));
            sendPlayerToPenaltyBox = true;
        }

        return new AnswerResult(currentPurseValue, sendPlayerToPenaltyBox);
    }

    private static class AnswerResult {
        private final int newPurseValue;
        private final boolean sendPlayerToPenaltyBox;

        public AnswerResult(int newPurseValue, boolean sendPlayerToPenaltyBox) {
            this.newPurseValue = newPurseValue;
            this.sendPlayerToPenaltyBox = sendPlayerToPenaltyBox;
        }

        public int getNewPurseValue() {
            return newPurseValue;
        }

        public boolean getPlayerPenaltyBoxStatus() {
            return sendPlayerToPenaltyBox;
        }
    }

    //~~~~ PURE place related


    private static int place_changePlaceForPlayer(int roll, int currentPlayerPlace, Object currentPlayerName) {
        int value = currentPlayerPlace + roll;
        if (value > 11) value = value - 12;

        display(currentPlayerName
                + "'s new location is "
                + value);

        return value;
    }

    //~~~~ PURE purse related

    private static boolean didPlayerWin(int currentPlayerIndex, int[] purses) {
        return getCoinsForPlayer(currentPlayerIndex, purses) == 6;
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

    //~~~~ PURE penalty box related

    private static boolean game_checkIfPlayerRollCanGetOutOfPenaltyBox(int rollForPlayer) {
        return rollForPlayer % 2 != 0;
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

    private static void displayPlayerIsStayingInThePenaltyBox(Object playerName) {
        display(playerName + " is not getting out of the penalty box");
    }

    private static void displayPlayerIsGettingOutOfPenaltyBox(Object playerName) {
        display(playerName + " is getting out of the penalty box");
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
    private static class Questions {
        private final LinkedList popQuestions;
        private final LinkedList scienceQuestions;
        private final LinkedList sportsQuestions;

        private final LinkedList rockQuestions;

        private Questions(LinkedList popQuestions, LinkedList scienceQuestions, LinkedList sportsQuestions, LinkedList rockQuestions) {
            this.popQuestions = popQuestions;
            this.scienceQuestions = scienceQuestions;
            this.sportsQuestions = sportsQuestions;
            this.rockQuestions = rockQuestions;
        }

        public LinkedList getPopQuestions() {
            return popQuestions;
        }

        public LinkedList getScienceQuestions() {
            return scienceQuestions;
        }

        public LinkedList getSportsQuestions() {
            return sportsQuestions;
        }
        public LinkedList getRockQuestions() {
            return rockQuestions;
        }

    }
}
