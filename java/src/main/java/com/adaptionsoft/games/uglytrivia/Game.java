package com.adaptionsoft.games.uglytrivia;

import java.util.ArrayList;
import java.util.LinkedList;

public class Game {
    ArrayList players = new ArrayList();
    int[] places = new int[6];
    int[] purses = new int[6];
    boolean[] inPenaltyBox = new boolean[6];
    PlayerState[] playerStates = new PlayerState[6];
    private int totalPlayers;

    LinkedList popQuestions = new LinkedList();
    LinkedList scienceQuestions = new LinkedList();
    LinkedList sportsQuestions = new LinkedList();
    LinkedList rockQuestions = new LinkedList();

    int currentPlayerIndex = 0;
    boolean isGettingOutOfPenaltyBox;
    private final Questions QUESTIONS;

    public Game() {
        for (int i = 0; i < 50; i++) {
            popQuestions.addLast("Pop Question " + i);
            scienceQuestions.addLast(("Science Question " + i));
            sportsQuestions.addLast(("Sports Question " + i));
            rockQuestions.addLast("Rock Question " + i);
        }
        QUESTIONS = new Questions(popQuestions, scienceQuestions, sportsQuestions, rockQuestions);
    }

    public boolean game_builder_add(String playerName) {


        players.add(playerName);
        int nextPlayer = totalPlayers(players);
        places[nextPlayer] = 0;
        purses[nextPlayer] = 0;
        inPenaltyBox[nextPlayer] = false;
//        playerStates[nextPlayer] = new PlayerState();
        totalPlayers = totalPlayers(players);

        display(playerName + " was added");
        display("They are player number " + nextPlayer);
        return true;
    }

    public boolean game_builder_isPlayable() {
        return (totalPlayers(players) >= 2);
    }


    private static class PlayerState {
        private final boolean currentPlayerIsGettingOutOfPenaltyBox;
        private final boolean currentPlayerIsCurrentlyInThePenaltyBox;
        private final Object currentPlayerName;
        private final int currentPlayerInitialPlace;

        private final int purse;

        private PlayerState(boolean currentPlayerIsGettingOutOfPenaltyBox,
                            boolean currentPlayerIsCurrentlyInThePenaltyBox,
                            Object currentPlayerName,
                            int currentPlayerInitialPlace, int purse) {
            this.currentPlayerIsGettingOutOfPenaltyBox = currentPlayerIsGettingOutOfPenaltyBox;
            this.currentPlayerIsCurrentlyInThePenaltyBox = currentPlayerIsCurrentlyInThePenaltyBox;
            this.currentPlayerName = currentPlayerName;
            this.currentPlayerInitialPlace = currentPlayerInitialPlace;
            this.purse = purse;
        }

        public boolean isCurrentPlayerIsGettingOutOfPenaltyBox() {
            return currentPlayerIsGettingOutOfPenaltyBox;
        }

        public boolean isCurrentPlayerIsCurrentlyInThePenaltyBox() {
            return currentPlayerIsCurrentlyInThePenaltyBox;
        }

        public Object getCurrentPlayerName() {
            return currentPlayerName;
        }

        public int getCurrentPlayerInitialPlace() {
            return currentPlayerInitialPlace;
        }
        public int getPurse() {
            return purse;
        }

    }

    public boolean p_round(int rollValue, int answer) {
        boolean notAWinner;


        Object currentPlayerName = currentPlayerName(currentPlayerIndex, players);
        boolean currentPlayerIsCurrentlyInThePenaltyBox = inPenaltyBox[currentPlayerIndex];

        PlayerState currentPlayerState =
                new PlayerState(isGettingOutOfPenaltyBox,
                        currentPlayerIsCurrentlyInThePenaltyBox,
                        currentPlayerName,
                        places[currentPlayerIndex],
                        purses[currentPlayerIndex]);
        RollResult rollResult = game_do_roll(rollValue, currentPlayerState, QUESTIONS);

        //Writes
        places[currentPlayerIndex] = rollResult.getNewPlayerPlace();
        isGettingOutOfPenaltyBox = rollResult.isGettingOutOfPenaltyBox();
        LinkedList categoryUsedForQuestion = rollResult.getCategoryUsedForQuestion();
        if (!categoryUsedForQuestion.isEmpty()) {
            categoryUsedForQuestion.removeFirst();
        }
        PlayerState playerStateAfterRoll =
                new PlayerState(rollResult.isGettingOutOfPenaltyBox(),
                        inPenaltyBox[currentPlayerIndex],
                        currentPlayerName,
                        rollResult.getNewPlayerPlace(),
                        purses[currentPlayerIndex]);

        PlayerState playerStateAfterAnswer;
        if (game_isPlayerAllowedToAnswer(playerStateAfterRoll.isCurrentPlayerIsGettingOutOfPenaltyBox(),
                playerStateAfterRoll.isCurrentPlayerIsCurrentlyInThePenaltyBox())) {

            AnswerResult answerResult = game_do_answer(answer, playerStateAfterRoll);

            //Writes
            purses[currentPlayerIndex] = answerResult.getNewPurseValue();
            inPenaltyBox[currentPlayerIndex] = answerResult.getPlayerPenaltyBoxStatus();

            playerStateAfterAnswer =
                    new PlayerState(playerStateAfterRoll.isCurrentPlayerIsGettingOutOfPenaltyBox(),
                            answerResult.getPlayerPenaltyBoxStatus(),
                            playerStateAfterRoll.getCurrentPlayerName(),
                            playerStateAfterRoll.getCurrentPlayerInitialPlace(),
                            answerResult.getNewPurseValue());
        } else {
            playerStateAfterAnswer = playerStateAfterRoll;
        }



        notAWinner = !didPlayerWin(playerStateAfterAnswer.getPurse());

        currentPlayerIndex = game_getNextPlayer(currentPlayerIndex, totalPlayers);
        return notAWinner;
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

    private static RollResult game_do_roll(int roll,
                                           PlayerState playerState, Questions questions) {
        display(playerState.getCurrentPlayerName() + " is the current player");
        display("They have rolled a " + roll);

        boolean newValueForIsGettingOutOfPenaltyBox = playerState.isCurrentPlayerIsGettingOutOfPenaltyBox();
        if (playerState.isCurrentPlayerIsCurrentlyInThePenaltyBox()) {
            newValueForIsGettingOutOfPenaltyBox = game_checkIfPlayerRollCanGetOutOfPenaltyBox(roll);

            if (newValueForIsGettingOutOfPenaltyBox) {
                displayPlayerIsGettingOutOfPenaltyBox(playerState.getCurrentPlayerName());
            } else {
                displayPlayerIsStayingInThePenaltyBox(playerState.getCurrentPlayerName());
            }
        }

        int newPlayerPlace = playerState.getCurrentPlayerInitialPlace();
        LinkedList categoryToUse = new LinkedList();
        if (!playerState.isCurrentPlayerIsCurrentlyInThePenaltyBox() || newValueForIsGettingOutOfPenaltyBox) {
            newPlayerPlace = place_changePlaceForPlayer(roll, playerState.getCurrentPlayerInitialPlace(), playerState.getCurrentPlayerName());

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

    private static AnswerResult game_do_answer(int value,
                                               PlayerState playerState) {
        int currentPurseValue;
        boolean sendPlayerToPenaltyBox;

        if (game_isCorrectAnswer(value)) {
            displayCorrectAnswer();
            currentPurseValue = playerState.getPurse() + 1;
            displayPlayerCoins(playerState.getCurrentPlayerName(), currentPurseValue);
            sendPlayerToPenaltyBox = playerState.isCurrentPlayerIsCurrentlyInThePenaltyBox();
        } else {
            displayQuestionIncorrect();
            currentPurseValue = playerState.getPurse();
            displayPlayerSentToPenaltyBox(playerState.getCurrentPlayerName());
            sendPlayerToPenaltyBox = true;
        }

        return new AnswerResult(currentPurseValue, sendPlayerToPenaltyBox);
    }

    private static Object currentPlayerName(int currentPlayerIndex, ArrayList players) {
        return getCurrentPlayerName(players, currentPlayerIndex);
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

    private static boolean didPlayerWin(int purse) {
        return purse == 6;
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
