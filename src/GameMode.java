import java.io.Serializable;


/**
 * Creates, handles and regulates the actions and events of a game.
 * @author Wentao
 *
 */
public interface GameMode extends Serializable {
	
	/**
	 * Gets the name of the game mode.
	 * @return the name of the game mode
	 */
	public String getName();
	
	/**
	 * Called when the game mode is selected by a player. This method should prepare and prompt for special conditions for a new game of the game mode.
	 * The new game builder is passed as the argument in the action.
	 * @param action the action to be executed after the game mode is set up
	 */
	public void setUpGameMode(ProjectionPlane.DialogDisposedAction<Game.Builder> action);

	/**
	 * Checks if the current round of the game is completed.
	 * @param game the game to be checked
	 * @return the time delay in milliseconds until the next round begins or {@code null} if the game is not completed
	 */
	public Long isRoundCompleted(Game game);
	
	/**
	 * Checks if the game is completed.
	 * @param game the game to be checked
	 * @return {@code true} if the game is completed; otherwise, {@code false}
	 */
	public boolean isGameCompleted(Game game);
	
	/**
	 * Called when a new game is started.
	 * @param game the new game to be started
	 */
	public void startNewGame(Game game);
	
	/**
	 * Called when a new round is started.
	 * @param game the new game in which the round is to be started
	 */
	public void startNewRound(Game game);
	
	/**
	 * Called when the health of a player reaches 0 or less.
	 * @param player the player with 0 or less health
	 */
	public void playerTerminated(Player player);
	
	/**
	 * Called when a player in the game has moved its location. This does not include vertical movements such as jumping or crouching.
	 * @param player the player that has recently moved
	 */
	public void playerMoved(Player player);
	
	/**
	 * Invoked a the completion of a game.
	 * @param game the game that has ended
	 */
	public void gameCompleted(Game game);

	/**
	 * Invoked at the completion of a round.
	 * @param game the game that has ended
	 * @return {@code true} if the game should be ended; otherwise {@code false} to begin a new round
	 */
	public boolean roundCompleted(Game game);
	
	/**
	 * Assigns a team to a player. The player's team is set by this method.
	 * @param player the player to be assigned a new team
	 */
	public void assignTeam(Player player);
	
	/**
	 * Invoked when a player request a team change.
	 * @param player the player requesting the team change
	 */
	public void requestTeamChange(Player player);
	
	/**
	 * Assigns a bomb to a player.
	 * @param game the game with the players and the bomb to be assigned
	 */
	public void assignBomb(Game game);
	
	/**
	 * Creates a default player for the game.
	 * @param game the game that the player will enter
	 * @return the default player
	 */
	public Player createDefaultPlayer(Game game);
}
