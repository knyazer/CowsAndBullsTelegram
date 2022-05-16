package tech.knyaz.cowsandbullstelegram;

import com.pengrad.telegrambot.model.Game;
import lombok.Getter;

import java.time.LocalDate;

public class GameStats {

    @Getter
    private LocalDate startTime;

    @Getter
    private LocalDate endTime;

    @Getter
    private int attempts;

    @Getter
    private boolean finished;

    @Getter
    private boolean won;

    @Getter
    private int guessLength;

    @Getter
    private String secret;

    GameStats() {

    }

    GameStats(GameStats other) {
        startTime = other.getStartTime();
        endTime = other.getEndTime();
        attempts = other.getAttempts();
        finished = other.isFinished();
        won = other.isWon();
        guessLength = other.getGuessLength();
        secret = other.getSecret();
    }

    public void init(String secret) {
        this.secret = secret;
        guessLength = secret.length();

        if (guessLength <= 1)
            throw new IllegalArgumentException("Guess length passed to game stats is strictly less than 2");

        // Set the starting of the game
        startTime = LocalDate.now();

        // Game has not finished yet
        finished = false;

        // And none attempts were made so far
        attempts = 0;

        // Do not initialize other 'undefined' variables
    }

    public void attemptFailed() {
        this.attempts++;
    }

    public void attemptSucceeded() {
        this.attempts++;

        // Player won
        won = true;

        // Finalize
        exit();
    }

    public void quit() {
        // Player lost
        won = false;

        // Finalize
        exit();
    }

    private void exit() {
        // Set the finish time
        endTime = LocalDate.now();

        // Game has finished
        finished = true;
    }
}
