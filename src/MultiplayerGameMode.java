import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MultiplayerGameMode implements GameMode {
	private static final long serialVersionUID = -7057506426879669926L;
	
	public static final long RESPAWN_TIME = 2000;
	public static final int DEFAULT_MONEY = 16000;
	
	private Set<Player> RECIEVED_BOMB = new HashSet<Player>();
	
	@Override
	public String getName() {
		return "Multiplayer";
	}

	@Override
	public void setUpGameMode(ProjectionPlane.DialogDisposedAction<Game.Builder> action) {
		action.dialogDisposed(new Game.Builder(this));
	}

	@Override
	public Long isRoundCompleted(Game game) {
		if (game.isBombDetonated() || game.isBombDefused()) {
			return 3000l;
		}
		int hasPlayerTeams = 0;
		int teamsAlive = 0;
		for (Team t : game.getMap().getTeams()) {
			if (game.getNumberOfPlayersAlive(t) >= 1) {
				teamsAlive++;
			}
			if (game.getNumberOfPlayers(t) >= 1) {
				hasPlayerTeams++;
			}
		}
		return (teamsAlive <= 1 && (hasPlayerTeams > 1 || game.getNumberOfPlayersAlive() == 0)) ? 3000l : null; // TODO
	}

	@Override
	public boolean isGameCompleted(Game game) {
		return false;
	}

	@Override
	public void startNewGame(Game game) {
		synchronized (this) {
			RECIEVED_BOMB.clear();
		}
		for (Player p : game.getPlayers()) {
			p.setMoney(DEFAULT_MONEY);
		}
	}

	@Override
	public void startNewRound(Game game) {
	}

	@Override
	public void playerTerminated(final Player PLAYER) {
		/*Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
			@Override
			public void run() {
				if (PLAYER.getGame().isRunning()) {
					PLAYER.setHealth(PLAYER.getMaxHealth());
					PLAYER.setMoney(DEFAULT_MONEY);
					PLAYER.getGame().setToSpawnLocation(PLAYER);
				}
			}
		}, RESPAWN_TIME, TimeUnit.MILLISECONDS);*/
	}

	@Override
	public void playerMoved(Player player) {
	}

	@Override
	public void gameCompleted(Game game) {
	}

	@Override
	public boolean roundCompleted(Game game) {
		if (game.isBombDetonated()) {
			game.setTeamScore(Team.DefaultTeams.TERRORISTS.getTeam(), game.getTeamScore(Team.DefaultTeams.TERRORISTS.getTeam()) + 1);
		} else if (game.isBombDefused()) {
			game.setTeamScore(Team.DefaultTeams.COUNTER_TERRORISTS.getTeam(), game.getTeamScore(Team.DefaultTeams.COUNTER_TERRORISTS.getTeam()) + 1);
		} else {
			for (Team t : game.getMap().getTeams()) {
				if (game.getNumberOfPlayersAlive(t) >= 1) {
					game.setTeamScore(t, game.getTeamScore(t) + 1);
				}
			}
		}
		return false;
	}

	@Override
	public void assignTeam(final Player PLAYER) {
		PLAYER.getMap().showTeamSelectionInternalDialog(false, new ProjectionPlane.DialogDisposedAction<Team>() {
			@Override
			public void dialogDisposed(Team t) {
				PLAYER.setTeam(t, true);
				PLAYER.setHealth(0);
			}
		});
	}

	@Override
	public void requestTeamChange(final Player PLAYER) {
		if (PLAYER.equals(ProjectionPlane.getSingleton().getPlayer())) {
			ProjectionPlane.getSingleton().setInputEnabled(false);
		}
		if (!PLAYER.isSpectator()) {
			PLAYER.playTerminationSound();
		}
		PLAYER.setTeam(null, true);
		PLAYER.getGame().getMap().showTeamSelectionInternalDialog(false, new ProjectionPlane.DialogDisposedAction<Team>() {
			@Override
			public void dialogDisposed(Team t) {
				PLAYER.setTeam(t, true);
				PLAYER.setHealth(PLAYER.getMaxHealth());
				PLAYER.addCarryItems(Melee.createDefaultKnife());
				PLAYER.getGame().setToSpawnLocation(PLAYER);
				if (PLAYER.equals(ProjectionPlane.getSingleton().getPlayer())) {
					ProjectionPlane.getSingleton().setInputEnabled(true);
				}
			}
		});
	}

	@Override
	public void assignBomb(Game game) {
		if (RECIEVED_BOMB.size() >= game.getNumberOfPlayers()) {
			synchronized (RECIEVED_BOMB) {
				RECIEVED_BOMB.clear();
			}
		}
		ArrayList<Player> TERRORISTS = new ArrayList<Player>();
		boolean atLeastOneTerrorist = false;
		for (Player p : game.getPlayers()) {
			if (p.getTeam() != null && p.getTeam().isBombTeam()) {
				atLeastOneTerrorist = true;
				if (!RECIEVED_BOMB.contains(p)) {
					TERRORISTS.add(p);
				}
			}
		}
		if (TERRORISTS.size() == 0) {
			if (atLeastOneTerrorist) {
				synchronized (RECIEVED_BOMB) {
					RECIEVED_BOMB.clear();
				}
				for (Player p : game.getPlayers()) {
					if (p.getTeam() != null && p.getTeam().isBombTeam()) {
						TERRORISTS.add(p);
					}
				}
			} else {
				return;
			}
		}
		if (TERRORISTS.size() > 0) {
			Player receiver = TERRORISTS.get((int) Math.floor(Math.random() * TERRORISTS.size()));
			synchronized (RECIEVED_BOMB) {
				RECIEVED_BOMB.add(receiver);
			}
			receiver.setBomb(Bomb.createDefaultBomb(), false);
		}
	}
	
	@Override
	public Player createDefaultPlayer(Game game) {
		Player player = new Player.Builder(ProjectionPlane.getSingleton().getPlaneName(), game, 2, 0.7).height(1).stepHeight(0.2).horizontalDirection(Math.random() * Math.PI * 2).build();
		game.assignTeam(player);
		return player;
	}
}
