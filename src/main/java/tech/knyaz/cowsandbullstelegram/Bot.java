package tech.knyaz.cowsandbullstelegram;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Hashtable;
import java.util.Random;

@BotController
@SpringBootApplication
public class Bot implements TelegramMvcController {
	@Getter
	@Value("${bot.token}")
	private String token;

	private Hashtable<Long, UserStats> data;

	Bot() {
		data = new Hashtable<Long, UserStats>();
	}

	public void setup(User user, Chat chat) {
		if (!data.containsKey(chat.id()))
			data.put(chat.id(), new UserStats());
	}

	@MessageRequest(value = "/setLength")
	public String setLengthButEmpty(User user, Chat chat) {
		return "Please, don't forget to provide a number after /setLength ! For example, /setLength 4";
	}

	@MessageRequest(value = "/setLength {length:[3-8]}")
	public String setLength(@BotPathVariable("length") String length, User user, Chat chat) {
		setup(user, chat);

		if (data.get(chat.id()).getCurrentGame() != null && !data.get(chat.id()).getCurrentGame().isFinished())
			return "You can't set new length in game. To do so you need to /quit first, and only that /setLength.";

		if (length.isEmpty())
			return "";

		data.get(chat.id()).setPreferredGuessLength(Integer.parseInt(length));
		return "New secret length set.";
	}

	@MessageRequest(value = "/quit")
	public String quitGame(User user, Chat chat) {
		setup(user, chat);

		if (data.get(chat.id()).getCurrentGame() == null || data.get(chat.id()).getCurrentGame().isFinished())
			return "You can't quit while not playing!";

		data.get(chat.id()).getCurrentGame().quit();
		data.get(chat.id()).syncHistory();

		return "Well, ok. Game has finished. You've lost. Loser... " +
				"The secret was " + data.get(chat.id()).getCurrentGame().getSecret() + ".";
	}

	@MessageRequest(value = "/stats")
	public String getStats(User user, Chat chat) {
		setup(user, chat);

		UserStats userStats = data.get(chat.id());

		int gamesWon = 0, gamesLost = 0, N = userStats.getHistory().size();
		double sumScore = 0;
		for (int i = 0; i < N; i++) {
			GameStats game = userStats.getHistory().get(i);
			gamesWon += (game.isWon() ? 1 : 0);
			gamesLost += (game.isWon() ? 0 : 1);
			if (game.isWon())
				sumScore += game.getAttempts();
		}

		if (N == 0)
			return "You have played no games. What stats do you want lol?";

		return "You have played " + N + " games with an average attempts number " +
				"of " + (gamesWon == 0 ? "?" : Math.floor(sumScore * 100.0 / (double)(gamesWon)) / 100.0) +
				". You have won " + gamesWon + " and lost " + gamesLost + " games.";
	}

	@MessageRequest(value = "/play")
	public String startGame(User user, Chat chat) {
		setup(user, chat);

		UserStats userStats = data.get(chat.id());

		if (userStats.getCurrentGame() != null)
			if (!userStats.getCurrentGame().isFinished())
				return "You can't start a new game while in game! To restart use /quit and then /play.";

		Random rand = new Random();
		StringBuilder secret = new StringBuilder();
		for (int i = 0; i < userStats.getPreferredGuessLength(); i++)
			secret.append((char)((i == 0 ? '1' : '0') + rand.nextInt(i == 0 ? 9 : 10)));

		if (userStats.getCurrentGame() == null)
			userStats.setCurrentGame(new GameStats());
		userStats.getCurrentGame().init(secret.toString());

		System.out.println(secret);

		return "The Game is starting with secret length of " + secret.length() + ". Good luck. \n\n" +
				"You can make an attempt with just plain message, like " +
				"1".repeat(userStats.getPreferredGuessLength());
	}

	@MessageRequest(value = "{guess:[0-9]+}")
	public String makeGuess(@BotPathVariable("guess") String guess, User user, Chat chat) {
		setup(user, chat);

		GameStats state = data.get(chat.id()).getCurrentGame();

		if (state == null)
			return "Please run the /play first, to enter the game state. " +
					"After that you will be able to automatically start games.";

		boolean automaticallyStarted = false;
		if (state.isFinished()) {
			startGame(user, chat);

			automaticallyStarted = true;
		}

		String message = "";

		if (guess.length() != state.getGuessLength())
			message =	"You've provided a valid guess, but the wrong number of numbers (haha). \n" +
						"Reattempt with " + state.getGuessLength() + " numbers instead. \n" +
						"Also you could quit the game with /quit, but it will affect your stats :)";

		else if (guess.equals(state.getSecret())) {
			state.attemptSucceeded();

			message =	"Wow! You've won actually. It has taken you " + state.getAttempts() +
								" attempts. Good job (maybe). \n\n You can start a new game with /play" +
								"\n or you can set different secret length (3-8) with /setLength , " +
								"e.g. /setLength 4";

			data.get(chat.id()).syncHistory();
		}
		else {
			state.attemptFailed();

			Checker checker = new Checker(guess, state.getSecret());
			message = checker.asString() + " Make next guess.";
		}

		return (automaticallyStarted ? "The game has started automatically wth secret length " +
				"of " + state.getGuessLength() + ".\n\n" : "") + message;
	}

	// Cheeky way to set default response
	@MessageRequest(value = "{x:[^0-9]+}")
	public String defaultBehaviour() {
		return "You wrote unknown command. Here what you are able to do with this bot: \n\n" + help();
	}

	@MessageRequest(value = "/help")
	public String help() {
		return """
				A game about Cows and Bulls! A bunch of commands available:\s
				/play - start the game\s
				/setLength (number) - set the secret length, from 3 to 8 including\s
				/help - display this message\s
				/quit - exit the game\s
				/stats - some stats about your played games
				""";
	}


	public static void main(String[] args) {
		SpringApplication.run(Bot.class);
	}
}