package tech.knyaz.cowsandbullstelegram;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class UserStats {

    @Setter
    @Getter
    private GameStats currentGame;

    @Getter
    private ArrayList<GameStats> history;

    @Setter
    @Getter
    private int preferredGuessLength = 3;

    UserStats() {
        history = new ArrayList<GameStats>();
    }

    public void syncHistory()
    {
        if (currentGame.isFinished())
            history.add(new GameStats(currentGame));
        else
            System.out.println("Written nothing as game not finished");
    }
}
