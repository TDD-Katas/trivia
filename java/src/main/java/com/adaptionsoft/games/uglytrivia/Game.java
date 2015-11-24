package com.adaptionsoft.games.uglytrivia;

import java.util.LinkedList;
import java.util.Random;

public class Game {
    PlayerState[] playerStates = new PlayerState[6];


    LinkedList<PlayerState> aCircular = new LinkedList<PlayerState>();
    private int totalPlayers;

    LinkedList popQuestions = new LinkedList();
    LinkedList scienceQuestions = new LinkedList();
    LinkedList sportsQuestions = new LinkedList();
    LinkedList rockQuestions = new LinkedList();

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
        int currentIndex = totalPlayers;

        PlayerState newPlayerState = new PlayerState(false, false, playerName, 0, 0);
        playerStates[currentIndex]= newPlayerState;
        aCircular.add(newPlayerState);
        totalPlayers = currentIndex + 1;

        if (totalPlayers > 5) throw new ArrayIndexOutOfBoundsException(6);

        display(playerName + " was added");
        display("They are player number " + totalPlayers);
        return true;
    }

    public boolean game_builder_isPlayable() {
        return (totalPlayers >= 2);
    }


    private static class PlayerState {
        private final boolean currentPlayerIsGettingOutOfPenaltyBox;
        private final boolean currentPlayerIsCurrentlyInThePenaltyBox;
        private final String currentPlayerName;
        private final int currentPlayerInitialPlace;

        private final int purse;

        private PlayerState(boolean currentPlayerIsGettingOutOfPenaltyBox,
                            boolean currentPlayerIsCurrentlyInThePenaltyBox,
                            String currentPlayerName,
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

        public String getCurrentPlayerName() {
            return currentPlayerName;
        }

        public int getCurrentPlayerPlace() {
            return currentPlayerInitialPlace;
        }
        public int getPurse() {
            return purse;
        }

    }

    public void p_theGameLoop(Random rand) {
        boolean notAWinner;
        do {
            PlayerState playerStateBefore = aCircular.pollFirst();

            PlayerState playerStateAfterRoll = p_roll(rand.nextInt(5) + 1, playerStateBefore);
            PlayerState playerStateAfterAnswer = p_answer(rand.nextInt(9), playerStateAfterRoll);

            //Writes !
            notAWinner = !didPlayerWin(playerStateAfterAnswer.getPurse());

            //Writes !
            aCircular.add(playerStateAfterAnswer);
        } while (notAWinner);
    }


    private PlayerState p_roll(int rollValue, PlayerState currentPlayerState) {
        RollResult rollResult = game_do_roll(rollValue, currentPlayerState, QUESTIONS);

        //Writes !
        LinkedList categoryUsedForQuestion = rollResult.getCategoryUsedForQuestion();
        if (!categoryUsedForQuestion.isEmpty()) {
            categoryUsedForQuestion.removeFirst();
        }


        return new PlayerState(rollResult.isGettingOutOfPenaltyBox(),
                currentPlayerState.isCurrentPlayerIsCurrentlyInThePenaltyBox(),
                currentPlayerState.getCurrentPlayerName(),
                rollResult.getNewPlayerPlace(),
                currentPlayerState.getPurse());
    }

    private PlayerState p_answer(int answer, PlayerState playerStateAfterRoll) {
        PlayerState playerStateAfterAnswer;
        if (game_isPlayerAllowedToAnswer(playerStateAfterRoll.isCurrentPlayerIsGettingOutOfPenaltyBox(),
                playerStateAfterRoll.isCurrentPlayerIsCurrentlyInThePenaltyBox())) {

            AnswerResult answerResult = game_do_answer(answer, playerStateAfterRoll);

            playerStateAfterAnswer =
                    new PlayerState(playerStateAfterRoll.isCurrentPlayerIsGettingOutOfPenaltyBox(),
                            answerResult.getPlayerPenaltyBoxStatus(),
                            playerStateAfterRoll.getCurrentPlayerName(),
                            playerStateAfterRoll.getCurrentPlayerPlace(),
                            answerResult.getNewPurseValue());
        } else {
            playerStateAfterAnswer = playerStateAfterRoll;
        }
        return playerStateAfterAnswer;
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //~~~~ PURE game related

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

        int newPlayerPlace = playerState.getCurrentPlayerPlace();
        LinkedList categoryToUse = new LinkedList();
        if (!playerState.isCurrentPlayerIsCurrentlyInThePenaltyBox() || newValueForIsGettingOutOfPenaltyBox) {
            newPlayerPlace = place_newPlaceForPlayer(roll, playerState.getCurrentPlayerPlace(), playerState.getCurrentPlayerName());

            String category = getCategoryForPlayerPlace(newPlayerPlace);
            display("The category is " + category);

            if (category.equals("Pop"))
                categoryToUse = questions.getPopQuestions();
            if (category.equals("Science"))
                categoryToUse = questions.getScienceQuestions();
            if (category.equals("Sports"))
                categoryToUse = questions.getSportsQuestions();
            if (category.equals("Rock"))
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


    private static int place_newPlaceForPlayer(int roll, int currentPlayerPlace, Object currentPlayerName) {
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
