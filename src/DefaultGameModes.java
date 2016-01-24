import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;


public enum DefaultGameModes implements GameMode {
	FREEPLAY(new FreePlayGameMode()),
	ZOMBIE(new ZombieGameMode()),
	MAZE(new MazeGameMode());
	
	private final GameMode DELAGATE_GAME_MODE;
	
	DefaultGameModes(GameMode delagateGameMode) {
		if (delagateGameMode == null) throw new IllegalArgumentException("Delagator cannot be null");
		DELAGATE_GAME_MODE = delagateGameMode;
	}
	
	@Override
	public String getName() {
		return DELAGATE_GAME_MODE.getName();
	}

	@Override
	public void setUpGameMode(ProjectionPlane.DialogDisposedAction<Game.Builder> action) {
		DELAGATE_GAME_MODE.setUpGameMode(action);
	}

	@Override
	public boolean isGameCompleted(Game game) {
		return DELAGATE_GAME_MODE.isGameCompleted(game);
	}

	@Override
	public Long isRoundCompleted(Game game) {
		return DELAGATE_GAME_MODE.isRoundCompleted(game);
	}

	@Override
	public void startNewGame(Game game) {
		DELAGATE_GAME_MODE.startNewGame(game);
	}

	@Override
	public void startNewRound(Game game) {
		DELAGATE_GAME_MODE.startNewRound(game);
	}

	@Override
	public void playerTerminated(Player player) {
		DELAGATE_GAME_MODE.playerTerminated(player);
	}

	@Override
	public void playerMoved(Player player) {
		DELAGATE_GAME_MODE.playerMoved(player);
	}

	@Override
	public void gameCompleted(Game game) {
		DELAGATE_GAME_MODE.gameCompleted(game);
	}

	@Override
	public boolean roundCompleted(Game game) {
		return DELAGATE_GAME_MODE.roundCompleted(game);
	}

	@Override
	public void assignTeam(Player player) {
		DELAGATE_GAME_MODE.assignTeam(player);
	}

	@Override
	public void requestTeamChange(Player player) {
		DELAGATE_GAME_MODE.requestTeamChange(player);
	}

	@Override
	public void assignBomb(Game game) {
		DELAGATE_GAME_MODE.assignBomb(game);
	}

	@Override
	public Player createDefaultPlayer(Game game) {
		return DELAGATE_GAME_MODE.createDefaultPlayer(game);
	}
	
	private static class FreePlayGameMode implements GameMode {
		private static final long serialVersionUID = -2840388915459071740L;

		@Override
		public String getName() {
			return "FREEPLAY";
		}

		@Override
		public void setUpGameMode(ProjectionPlane.DialogDisposedAction<Game.Builder> action) {
			action.dialogDisposed(new Game.Builder(FREEPLAY).map(DefaultMap.ICE_WORLD.generateMap(DefaultMap.DefaultTeamSet.TERRORISTS_TEAM_SET)));
		}

		@Override
		public Long isRoundCompleted(Game game) {
			if (game.isBombDetonated()) {
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
			return (teamsAlive <= 1 && hasPlayerTeams > 1) ? 3000l : null;
		}

		@Override
		public boolean isGameCompleted(Game game) {
			return false;
		}

		@Override
		public void startNewGame(Game game) {
			for (Player p : game.getPlayers()) {
				p.resetBasicTermination(true, true, 16000, true);
			}
		}

		@Override
		public void startNewRound(Game game) {
		}

		@Override
		public void playerTerminated(final Player PLAYER) {
			PLAYER.resetBasicTermination(true, true, 16000, false);
			Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
				@Override
				public void run() {
					if (isRoundCompleted(PLAYER.getGame()) == null) {
						PLAYER.setHealth(PLAYER.getMaxHealth());
						PLAYER.getGame().setToSpawnLocation(PLAYER);
					}
				}
			}, 2000, TimeUnit.MILLISECONDS);
		}


		@Override
		public void playerMoved(Player player) {
		}

		@Override
		public void gameCompleted(Game game) {
		}

		@Override
		public boolean roundCompleted(Game game) {
			return false;
		}

		@Override
		public void assignTeam(Player player) {
			player.setTeam(player.getMap().getTeam(0), true);
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
			// TODO Auto-generated method stub
			ArrayList<Player> TERRORISTS = new ArrayList<Player>();
			for (Player p : game.getPlayers()) {
				if (p.getTeam() != null && p.getTeam().isBombTeam()) {
					TERRORISTS.add(p);
				}
			}
			if (TERRORISTS.size() > 0) {
				Player reciever = TERRORISTS.get((int) Math.floor(Math.random() * TERRORISTS.size()));
				reciever.setBomb(Bomb.createDefaultBomb(), false);
			}
		}

		@Override
		public Player createDefaultPlayer(Game game) {
			Player player = new Player.Builder(ProjectionPlane.getSingleton().getPlaneName(), game, 2, 0.7).height(1).stepHeight(0.2).horizontalDirection(Math.random() * Math.PI * 2).build();
			game.assignTeam(player);
			game.setToSpawnLocation(player);
			player.setTacticalShield(TacticalShield.createDefaultTacticalShield());
			return player;
		}
	}
	
	private static class ZombieGameMode implements GameMode {
		private static final long serialVersionUID = 1032784023121906816L;
		
		private static final int INITIAL_MONEY = 4000;
		
		private boolean cancelSetUp = true;
		private Map tempMap = null;
		
		private Integer zombieWave = null;
		private boolean jumpingZombies = false;
		private double zombieSpeed = 1;
		private int nOfZombies = 0;
		private int nOfZombiesAwaitingSpawn = 0;
		private int botsGenerated = 0;
		private int zombiesKilled = 0;
		private final ArrayList<BotAI.ZombieAI> ZOMBIE_AI = new ArrayList<BotAI.ZombieAI>();
		
		private synchronized void resetVariables() {
			this.cancelSetUp = false;
			this.tempMap = null;
			
			this.jumpingZombies = false;
			this.zombieSpeed = 1;
			this.nOfZombies = 0;
			this.zombiesKilled = 0;
		}
		
		@Override
		public String getName() {
			return "ZOMBIE";
		}

		@Override
		public void setUpGameMode(final ProjectionPlane.DialogDisposedAction<Game.Builder> ACTION) {
			resetVariables();
			final JCheckBox JUMPING_ZOMBIES_CHECKBOX = new JCheckBox("Jumping Zombies?");
			final SpinnerNumberModel ZOMBIES_SPEED_SPINNER_MODEL = new SpinnerNumberModel(Math.min(Math.max(this.zombieSpeed, 0.1), 10), 0.1, 10, 1);
			final SpinnerNumberModel NUMBER_OF_ZOMBIES_SPINNER_MODEL = new SpinnerNumberModel(3, 1, 10, 1);
			final JCheckBox ZOMBIE_WAVES_CHECKBOX = new JCheckBox("Waves");
			final JPanel ZOMBIES_PANEL = new JPanel(new GridLayout(1, 0));
			final ProjectionPlane.ModalInternalFrame DIALOG = ProjectionPlane.getSingleton().addInternalFrame("New Zombie Game", true, true, true, true, true, true, true, new ProjectionPlane.DialogDisposedSimpleAction() {
				@Override
				public void dialogDisposed() {
					if (!ZombieGameMode.this.cancelSetUp && ZombieGameMode.this.tempMap != null) {
						synchronized (ZombieGameMode.this) {
							if (ZOMBIE_WAVES_CHECKBOX.isSelected()) {
								ZombieGameMode.this.zombieWave = 0;
							} else {
								ZombieGameMode.this.zombieWave = null;
								ZombieGameMode.this.jumpingZombies = JUMPING_ZOMBIES_CHECKBOX.isSelected();
								ZombieGameMode.this.zombieSpeed = ZOMBIES_SPEED_SPINNER_MODEL.getNumber().doubleValue();
								ZombieGameMode.this.nOfZombies = NUMBER_OF_ZOMBIES_SPINNER_MODEL.getNumber().intValue();
							}
						}
						ACTION.dialogDisposed(new Game.Builder(ZOMBIE).map(ZombieGameMode.this.tempMap));
					} else {
						ACTION.dialogDisposed(null);
					}
				}
			});
			DIALOG.getContentPane().setLayout(new BorderLayout());
			JPanel mainPanel = new JPanel(new GridLayout(0, 1));
			JPanel createMapPanel = new JPanel(new BorderLayout());
			createMapPanel.setBorder(BorderFactory.createTitledBorder("Map:"));
			final JPanel MAP_PREVIEW = new JPanel() {
				private static final long serialVersionUID = 6759945718888413929L;
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if (ZombieGameMode.this.tempMap != null) {
						ZombieGameMode.this.tempMap.drawMapPreview(g, 0, 0, getWidth(), getHeight());
					}
				}
			};
			JButton createMapButton = new JButton("Create Map");
			createMapButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MapCreatorDialog.showMapCreatorDialog(ZombieGameMode.this.tempMap, new ProjectionPlane.DialogDisposedAction<Map>() {
						@Override
						public void dialogDisposed(Map t) {
							synchronized (ZombieGameMode.this) {
								ZombieGameMode.this.tempMap = t;
							}
							MAP_PREVIEW.repaint();
							Main.enableComponents(ZOMBIES_PANEL, !ZOMBIE_WAVES_CHECKBOX.isSelected());
						}
					});
				}
			});
			createMapPanel.add(MAP_PREVIEW, BorderLayout.CENTER);
			createMapPanel.add(createMapButton, BorderLayout.SOUTH);
			JPanel zombiesOuterPanel = new JPanel(new BorderLayout());
			ZOMBIES_PANEL.setBorder(BorderFactory.createTitledBorder("Zombies:"));
			JSpinner nOfZombiesSpinner = new JSpinner(NUMBER_OF_ZOMBIES_SPINNER_MODEL);
			nOfZombiesSpinner.setBorder(BorderFactory.createTitledBorder("Number of zombies:"));
			final JSpinner ZOMBIES_SPEED_SPINNER = new JSpinner(ZOMBIES_SPEED_SPINNER_MODEL);
			ZOMBIES_SPEED_SPINNER.setBorder(BorderFactory.createTitledBorder("Zombie Speed:"));
			ZOMBIES_PANEL.add(nOfZombiesSpinner);
			ZOMBIES_PANEL.add(ZOMBIES_SPEED_SPINNER);
			JPanel zombiesPanel2 = new JPanel(new BorderLayout());
			ZOMBIE_WAVES_CHECKBOX.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Main.enableComponents(ZOMBIES_PANEL, !ZOMBIE_WAVES_CHECKBOX.isSelected());
				}
			});
			zombiesPanel2.add(ZOMBIE_WAVES_CHECKBOX, BorderLayout.NORTH);
			zombiesPanel2.add(ZOMBIES_PANEL, BorderLayout.CENTER);
			zombiesOuterPanel.add(zombiesPanel2, BorderLayout.CENTER);
			zombiesOuterPanel.add(JUMPING_ZOMBIES_CHECKBOX, BorderLayout.SOUTH);
			JButton startButton = new JButton("Start Game");
			startButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (ZombieGameMode.this.tempMap == null) {
						JOptionPane.showInternalMessageDialog(DIALOG, "You must create a map before starting the game!", "Warning!", JOptionPane.WARNING_MESSAGE);
					} else {
						ZombieGameMode.this.cancelSetUp = false;
						DIALOG.dispose();
					}
				}
			});
			mainPanel.add(createMapPanel);
			mainPanel.add(zombiesOuterPanel);
			DIALOG.getContentPane().add(mainPanel, BorderLayout.CENTER);
			DIALOG.getContentPane().add(startButton, BorderLayout.SOUTH);
			DIALOG.setSize(400, 300);
		}

		@Override
		public Long isRoundCompleted(Game game) {
			if (this.zombieWave != null) {
				if (game.getNumberOfPlayersAlive(Team.DefaultTeams.ZOMBIES.getTeam()) <= 0) {
					return Game.DEFAULT_FREEZE_TIME;
				}
			}
			return null;
		}

		@Override
		public boolean isGameCompleted(Game game) {
			return false;
		}

		@Override
		public void startNewGame(Game game) {
			Player[] players = game.getPlayers();
			if (players != null) {
				for (Player p : players) {
					p.setMoney(INITIAL_MONEY);
				}
			}
			this.botsGenerated = 0;
			for (int i = 0; i < this.nOfZombies; i++) {
				Player bot = createBot(game);
				game.addPlayer(bot);
				if (bot != null) {
					bot.setSpeed(this.zombieSpeed);
				}
			}
		}
		
		private double getZombieSpeed(int waveNumber) {
			return Math.pow(waveNumber, 0.1) * Math.log(waveNumber);
		}
		
		private int getNumberOfZombies(int waveNumber) {
			return (waveNumber + 3) / 4;
		}

		@Override
		public void startNewRound(Game game) {
			if (this.zombieWave != null) {
				int maxPlayers = game.getMap().getMaxPlayers() != null ? game.getMap().getMaxPlayers() / 2 : 10;
				int spawnNumber;
				synchronized (this) {
					this.zombieWave++;
					this.zombieSpeed = getZombieSpeed(this.zombieWave);
					this.nOfZombies = getNumberOfZombies(this.zombieWave);
					spawnNumber = Math.min(this.nOfZombies, maxPlayers);
					this.nOfZombiesAwaitingSpawn = Math.max(this.nOfZombies - spawnNumber, 0);
				}
				for (int i = 0; i < spawnNumber - game.getNumberOfPlayers(Team.DefaultTeams.ZOMBIES.getTeam()); i++) {
					Player bot = createBot(game);
					game.addPlayer(bot);
					if (bot != null) {
						bot.setSpeed(this.zombieSpeed);
					}
				}
				for (Player p : game.getPlayers()) {
					if (Team.DefaultTeams.ZOMBIES.getTeam().equals(p.getTeam())) {
						p.setSpeed(this.zombieSpeed);
					} else if (Team.DefaultTeams.COUNTER_ZOMBIES.getTeam().equals(p.getTeam())) {
						p.setMoney(p.getMoney() + this.zombieWave * 200);
					}
				}
				int count = 1;
				int jumpZombies = 0;
				for (BotAI.ZombieAI ai : ZOMBIE_AI) {
					boolean willJump = Math.random() - (this.zombieWave < 10 ? 0.9 : 0) >= count * 1d / (ZOMBIE_AI.size() + 1);
					count++;
					if (willJump) {
						jumpZombies++;
					}
					ai.setJumpingZombie(willJump);
				}
				game.showMainMessage("Wave " + this.zombieWave + "\nSpeed: " + this.zombieSpeed + "\nZombies: " + this.nOfZombies + "\nJumping Zombies: " + jumpZombies, 3000);
			}
		}

		@Override
		public void playerTerminated(Player player) {
			if (Team.DefaultTeams.COUNTER_ZOMBIES.getTeam().equals(player.getTeam())) {
				int playersAlive = 0;
				for (Player p : player.getGame().getPlayers()) {
					if (Team.DefaultTeams.COUNTER_ZOMBIES.getTeam().equals(p.getTeam()) && p.isAlive()) {
						playersAlive++;
					}
				}
				if (this.zombieWave == null && playersAlive == 0) {
					player.getGame().endRound(0);
				} else if (this.zombieWave != null && playersAlive == 0) {
					player.getGame().stop();
					ProjectionPlane.getSingleton().freezeCurrentInputState();
					JOptionPane.showInternalMessageDialog(ProjectionPlane.getSingleton(), "Your brains were eaten by a zombie in " + (player.getGame().getRoundDuration() / 1000d) + " seconds! Zombies killed: " + this.zombiesKilled + (this.zombieWave != null ? " Waves Completed: " + (this.zombieWave - 1) : ""), "Game Completed", JOptionPane.INFORMATION_MESSAGE);
					ProjectionPlane.getSingleton().restorePreviousInputState();
				}
			} else if (player.getTeam() != null) {
				synchronized (this) {
					this.zombiesKilled++;
				}
				if (this.zombieWave == null) {
					player.setHealth(player.getMaxHealth());
					player.getGame().setToSpawnLocation(player);
				} else if (this.nOfZombiesAwaitingSpawn > 0) {
					synchronized (this) {
						this.nOfZombiesAwaitingSpawn--;
					}
					player.setHealth(player.getMaxHealth());
					player.getGame().setToSpawnLocation(player);
				}
				if (this.zombieWave != null) {
					for (Player p : player.getGame().getPlayers()) {
						if (Team.DefaultTeams.COUNTER_ZOMBIES.getTeam().equals(p.getTeam())) {
							p.setMoney(p.getMoney() + (int) Math.pow(this.zombieWave, 2));
						}
					}
				}
			}
		}

		@Override
		public void playerMoved(Player player) {
		}

		@Override
		public void gameCompleted(Game game) {
		}

		@Override
		public boolean roundCompleted(Game game) {
			boolean value = true;
			if (this.zombieWave == null) {
				ProjectionPlane.getSingleton().freezeCurrentInputState();
				value = JOptionPane.showInternalConfirmDialog(ProjectionPlane.getSingleton(), "Game Over! Again?", "Game Completed", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
				ProjectionPlane.getSingleton().restorePreviousInputState();
			}
			return !value;
		}

		private Player createBot(Game game) {
			Player target = null;
			for (Player p : game.getPlayers()) {
				if (Team.DefaultTeams.COUNTER_ZOMBIES.getTeam().equals(p.getTeam()) && p.isAlive()) {
					target = p;
					break;
				}
			}
			Player zombie = null;
			if (target != null) {
				BotAI.ZombieAI ai = new BotAI.ZombieAI(target, this.jumpingZombies);
				synchronized (ZOMBIE_AI) {
					ZOMBIE_AI.add(ai);
				}
				zombie = new Player.Builder("Merle " + ++this.botsGenerated, game, this.zombieSpeed, 1.5).height(1).stepHeight(0.5).horizontalDirection(Math.random() * Math.PI * 2).touchDamage(3).susceptibleToTouchDamage(false).team(game.getMap().getTeam(1)).projectedImagePath(Main.ZOMBIE_IMAGE_PATH).movementController(ai).build();
			}
			return zombie;
		}

		@Override
		public void assignTeam(Player player) {
			player.setTeam(Team.DefaultTeams.COUNTER_ZOMBIES.getTeam(), true);
		}

		@Override
		public void requestTeamChange(Player player) {
		}

		@Override
		public void assignBomb(Game game) {
		}

		@Override
		public Player createDefaultPlayer(Game game) {
			Player player = new Player.Builder("", game, 6, 1.5).location(0.5, 0.5).height(1).stepHeight(0.5).horizontalDirection(Math.random() * Math.PI * 2).horizontalFOVD(90).build();
			player.addCarryItems(Melee.createDefaultKnife());
			player.setMoney(INITIAL_MONEY);
			game.assignTeam(player);
			return player;
		}
	}
	
	private static class MazeGameMode implements GameMode, SpawnLocator, StoreLocator {
		private static final long serialVersionUID = -8247938413749954045L;

		private static final Team LOST_TEAM = new Team("Lost", Store.DEFAULT_MARKERS_STORE, Color.RED, false, false);
		private boolean cancelSetUp = true;
		
		private double startX = 2.5;
		private double startY = 2.5;
		private int finishX = 0;
		private int finishY = 0;
		private Map.MazeAlgorithms mazeAlgorithm = Map.MazeAlgorithms.DEPTH_FIRST_SEARCH;
		private boolean foundFinish = false;
		
		@Override
		public String getName() {
			return "MAZE";
		}

		@Override
		public void setUpGameMode(final ProjectionPlane.DialogDisposedAction<Game.Builder> ACTION) {
			this.cancelSetUp = true;
			final SpinnerNumberModel[] SPINNERS_MODEL = new SpinnerNumberModel[2];
			final JCheckBox ALLOW_RADAR_CHECKBOX = new JCheckBox("Radar", false);
			final JCheckBox ALLOW_COMPASS_CHECKBOX = new JCheckBox("Compass", false);
			final JComboBox<Map.MazeAlgorithms> MAZE_ALGORITHMS_COMBOBOX = new JComboBox<Map.MazeAlgorithms>(Map.MazeAlgorithms.values());	
			final ProjectionPlane.ModalInternalFrame DIALOG = ProjectionPlane.getSingleton().addInternalFrame("New Maze Game", true, true, true, true, true, true, true, new ProjectionPlane.DialogDisposedSimpleAction() {
				@Override
				public void dialogDisposed() {
					if (!MazeGameMode.this.cancelSetUp) {
						Map map = new Map(SPINNERS_MODEL[0].getNumber().intValue() * 2 - 1, SPINNERS_MODEL[1].getNumber().intValue() * 2 - 1, "Maze", null, Map.DEFAULT_WALL_COLOR, Map.DEFAULT_FLOOR_COLOR, Map.DEFAULT_BACKGROUND_COLOR, Map.DEFAULT_GRAVITY, MazeGameMode.this, MazeGameMode.this, null, null, null, null, null, 0, new Team[] {
								LOST_TEAM, new Team("The Others", null, Color.BLUE, false, false)
						});
						synchronized (MazeGameMode.this) {
							try {
								MazeGameMode.this.mazeAlgorithm = (Map.MazeAlgorithms) MAZE_ALGORITHMS_COMBOBOX.getSelectedItem();
							} catch (ClassCastException ex) {
								MazeGameMode.this.mazeAlgorithm = Map.MazeAlgorithms.DEPTH_FIRST_SEARCH;
							}
							MazeGameMode.this.startX = ((int) (Math.random() * (map.getWidth() / 2))) * 2 + 2.5;
							MazeGameMode.this.startY = ((int) (Math.random() * (map.getHeight() / 2))) * 2 + 2.5;
							MazeGameMode.this.finishX = 0;
							MazeGameMode.this.finishY = 0;
						}
						ACTION.dialogDisposed(new Game.Builder(MAZE).map(map).allowsRadar(ALLOW_RADAR_CHECKBOX.isSelected()).allowsHorizontalDirection(ALLOW_COMPASS_CHECKBOX.isSelected()));
					} else {
						ACTION.dialogDisposed(null);
					}
				}
			});
			DIALOG.getContentPane().setLayout(new BorderLayout());
			JPanel mainPanel = new JPanel(new GridLayout(0, 1));
			JPanel sizesPanel = new JPanel(new GridLayout(1, 0));
			sizesPanel.setBorder(BorderFactory.createTitledBorder("Map Size:"));
			final JSpinner[] SPINNERS = new JSpinner[2];
			for (int i = 0; i < 2; i++) {
				SPINNERS_MODEL[i] = new SpinnerNumberModel(5, 3, 30, 2);
				SPINNERS[i] = new JSpinner(SPINNERS_MODEL[i]);
				sizesPanel.add(SPINNERS[i]);
			}
			JPanel additionalOptionsPanel = new JPanel(new GridLayout(0, 1));
			additionalOptionsPanel.setBorder(BorderFactory.createTitledBorder("Additional Options:"));
			additionalOptionsPanel.add(ALLOW_RADAR_CHECKBOX);
			additionalOptionsPanel.add(ALLOW_COMPASS_CHECKBOX);
			JPanel algorithmsPanel = new JPanel(new GridLayout(1, 0));
			algorithmsPanel.setBorder(BorderFactory.createTitledBorder("Maze Generation Algorithm:"));
			algorithmsPanel.add(MAZE_ALGORITHMS_COMBOBOX);
			JButton startButton = new JButton("Start Game");
			startButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MazeGameMode.this.cancelSetUp = false;
					DIALOG.dispose();
				}
			});
			mainPanel.add(sizesPanel);
			mainPanel.add(additionalOptionsPanel);
			mainPanel.add(algorithmsPanel);
			DIALOG.getContentPane().add(mainPanel, BorderLayout.CENTER);
			DIALOG.getContentPane().add(startButton, BorderLayout.SOUTH);
			DIALOG.setSize(400, 300);
		}

		@Override
		public Long isRoundCompleted(Game game) {
			return null;
		}

		@Override
		public boolean isGameCompleted(Game game) {
			for (Player p : game.getPlayers()) {
				if (Math.floor(p.getLocationX()) == this.finishX && Math.floor(p.getLocationY()) == this.finishY) {
					synchronized (this) {
						this.foundFinish = true;
					}
					return true;
				}
			}
			return false;
		}

		@Override
		public void startNewGame(Game game) {
			synchronized (this) {
				this.foundFinish = false;
			}
			ProjectionPlane.getSingleton().freezeCurrentInputState();
			JOptionPane.showInternalMessageDialog(ProjectionPlane.getSingleton(), "Find the green square!", "Mission", JOptionPane.INFORMATION_MESSAGE);
			ProjectionPlane.getSingleton().restorePreviousInputState();
			game.getMap().generateMaze(this.mazeAlgorithm, 5, false, 0, 0, Color.GREEN, true);
			for (Player p : game.getPlayers()) {
				p.sendToHighestFloorHeight();
				p.setMoney(game.getMap().getWidth() * game.getMap().getHeight());
			}
		}

		@Override
		public void startNewRound(Game game) {
		}

		@Override
		public void playerTerminated(Player player) {
			player.setHealth(player.getMaxHealth());
			player.setLocation(this.startX, this.startY);
		}

		@Override
		public void playerMoved(Player player) {
		}

		@Override
		public void gameCompleted(Game game) {
			if (this.foundFinish) {
				ProjectionPlane.getSingleton().freezeCurrentInputState();
				// JOptionPane.showInternalMessageDialog(ProjectionPlane.getSingleton(), "You found the green square in " + (game.getRoundDuration() / 1000d) + "s", "Game Completed", JOptionPane.INFORMATION_MESSAGE);  TODO dialog freezes current thread
				ProjectionPlane.getSingleton().restorePreviousInputState();
			}
		}

		@Override
		public boolean roundCompleted(Game game) {
			return false;
		}
		
		@Override
		public Map.Point3D getSpawnLocation(Player player) {
			return new Map.Point3D(((int) (Math.random() * (player.getGame().getMap().getWidth() / 2))) * 2 + 2.5, ((int) (Math.random() * (player.getGame().getMap().getHeight() / 2))) * 2 + 2.5, 0);
		}
		
		@Override
		public Map.Point3D getBotSpawnLocation(Player player) {
			return getSpawnLocation(player);
		}

		@Override
		public boolean isStoreLocation(Map map, double x, double y, double z, Player player) {
			return true;
		}

		@Override
		public void openStore(Player player, ProjectionPlane.DialogDisposedAction<StoreItem[]> action, boolean isAutobuy) {
			Store.DEFAULT_MARKERS_STORE.openStore(player, action, isAutobuy);
		}

		@Override
		public void assignTeam(Player player) {
			player.setTeam(LOST_TEAM, true);
		}

		@Override
		public void requestTeamChange(Player player) {
		}

		@Override
		public void assignBomb(Game game) {
		}

		@Override
		public Player createDefaultPlayer(Game game) {
			Player player = new Player.Builder(ProjectionPlane.getSingleton().getPlaneName(), game, 2, 0.7).height(1).stepHeight(0.2).horizontalDirection(Math.random() * Math.PI * 2).build();
			game.assignTeam(player);
			return player;
		}
	}
}