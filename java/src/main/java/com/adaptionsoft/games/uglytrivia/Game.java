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
        Questions questions = new Questions(popQuestions, scienceQuestions, sportsQuestions, rockQuestions);
        RollResult rollResult = game_do_roll(roll, players, currentPlayerIndex, inPenaltyBox, places, isGettingOutOfPenaltyBox,
                questions);

        places[currentPlayerIndex] = rollResult.getNewPlayerPlace();
        isGettingOutOfPenaltyBox = rollResult.isGettingOutOfPenaltyBox();
        LinkedList categoryUsedForQuestion = rollResult.getCategoryUsedForQuestion();
        if (!categoryUsedForQuestion.isEmpty()) {
            categoryUsedForQuestion.removeFirst();
        }
    }

    public void p_answer(int value) {
        boolean currentlyInPenaltyBox = inPenaltyBox[currentPlayerIndex];
        AnswerResult answerResult = game_do_answer(value, purses, currentPlayerIndex, players, currentlyInPenaltyBox);

        purses[currentPlayerIndex] = answerResult.getNewPurseValue();
        inPenaltyBox[currentPlayerIndex] = answerResult.getPlayerPenaltyBoxStatus();
    }

    public boolean p_isCurrentPlayerAllowedToAnswer() {
        return game_isPlayerAllowedToAnswer(currentPlayerIndex, inPenaltyBox, isGettingOutOfPenaltyBox);
    }

    public boolean p_shouldContinueGame() {
        return !didPlayerWin(currentPlayerIndex, purses);
    }


    public void game_moveToNextPlayer() {
        game_incrementPlayerIndex();
        game_resetPlayerIfLast(currentPlayerIndex, players);
    }

    private void game_incrementPlayerIndex() {
        currentPlayerIndex++;
    }

    private void game_resetPlayerIfLast(int currentPlayer, ArrayList players) {
        if (currentPlayer == totalPlayers(players))
            this.currentPlayerIndex = 0;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //~~~~ PURE game related

    private static boolean game_isPlayerAllowedToAnswer(int currentPlayerIndex, boolean[] inPenaltyBox, boolean isGettingOutOfPenaltyBox) {
        return !isPlayerInPenaltyBox(currentPlayerIndex, inPenaltyBox) || isGettingOutOfPenaltyBox;
    }

    private static boolean game_isCorrectAnswer(int value) {
        return value != 7;
    }


    private static RollResult game_do_roll(int roll, ArrayList players, int currentPlayerIndex, boolean[] inPenaltyBox, int[] places, boolean isGettingOutOfPenaltyBox, Questions questions) {
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
                categoryToUse = questions.getPopQuestions();
            if (category == "Science")
                categoryToUse = questions.getScienceQuestions();
            if (category == "Sports")
                categoryToUse = questions.getSportsQuestions();
            if (category == "Rock")
                categoryToUse = questions.getRockQuestions();

            System.out.println(categoryToUse.peekFirst());
        }

        return new RollResult(playerPlace, newValueForIsGettingOutOfPenaltyBox, categoryToUse);
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

    private  AnswerResult game_do_answer(int value, int[] purses, int currentPlayerIndex, ArrayList players, boolean currentlyInPenaltyBox) {
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


    private static int place_changePlaceForPlayer(int roll, int[] places, int currentPlayerIndex, ArrayList players) {
        int value = places[currentPlayerIndex] + roll;
        if (value > 11) value = value - 12;

        display(getCurrentPlayerName(players, currentPlayerIndex)
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

    private static boolean game_checkIfPlayerCanGetOutOfPenaltyBox(int roll, int currentPlayerIndex, ArrayList players) {
        if (roll % 2 != 0) {
            display(getCurrentPlayerName(players, currentPlayerIndex) + " is getting out of the penalty box");
            return true;
        } else {
            display(getCurrentPlayerName(players, currentPlayerIndex) + " is not getting out of the penalty box");
            return false;
        }
    }

    private static boolean isPlayerInPenaltyBox(int playerIndex, boolean[] penaltyBox) {
        return penaltyBox[playerIndex];
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
