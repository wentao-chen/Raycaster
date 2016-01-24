import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;


public final class Game implements PlayerListener, Serializable {
	private static final long serialVersionUID = -7077757849653288891L;
	
	public static final long DEFAULT_FREEZE_TIME = 200;
	
	private static int numberOfGames = 0;
	
	private final AtomicBoolean IS_RUNNING = new AtomicBoolean(false);
	private Long isRoundCompleted = null;
	private final AtomicBoolean BOT_MOVEMENT_PAUSED = new AtomicBoolean(false);
	
	private final GameMode GAME_MODE;
	private final Map MAP;
	private final HashMap<Team, Integer> TEAM_SCORES = new HashMap<Team, Integer>();
	private final boolean ALLOWS_RADAR;
	private final boolean ALLOWS_GENERATE_MAZE;
	private final boolean ALLOWS_HORIZONTAL_DIRECTION;
	private final ArrayList<Bullet> BULLETS = new ArrayList<Bullet>();
	private final ArrayList<ThrownWeapon> THROWN_WEAPONS = new ArrayList<ThrownWeapon>();
	private final ArrayList<DroppedItem> DROPPED_ITEMS = new ArrayList<DroppedItem>();
	private final HashMap<Integer, Player> PLAYERS = new HashMap<Integer, Player>();
	private transient ArrayList<KillEvent> recentKillEvents = new ArrayList<KillEvent>();
	private Bomb plantedBomb = null;
	private Long nextBombBeepInterval = null;
	private long roundStartTimer = 0;
	private long gameStartTimer = 0;
	private boolean friendlyFire = true;
	private Boolean bombDetonationState = null;
	private transient String mainMessage = null;
	private transient Long mainMessageExpireTime = null;
	
	private transient MultiplayerGameConnection multiplayerGameConnection = null;
	private transient ArrayList<GameListener> listeners = new ArrayList<GameListener>();
	
	public Game(Builder builder) {
		if (builder.gameMode == null) throw new IllegalStateException("Game mode cannot be null");
		if (builder.map == null) throw new IllegalStateException("Map cannot be null");
		GAME_MODE = builder.gameMode;
		MAP = builder.map;
		for (Team t : getMap().getTeams()) {
			TEAM_SCORES.put(t, 0);
		}
		ALLOWS_RADAR = builder.allowsRadar;
		ALLOWS_GENERATE_MAZE = builder.allowsGenerateMaze;
		ALLOWS_HORIZONTAL_DIRECTION = builder.allowsHorizontalDirection;
	}
	
	public GameMode getGameMode() {
		return GAME_MODE;
	}
	
	public Map getMap() {
		return MAP;
	}
	
	public int getTeamScore(Team t) {
		if (TEAM_SCORES.get(t) == null) throw new IllegalArgumentException("Invalid team");
		return TEAM_SCORES.get(t);
	}
	
	public void setTeamScore(Team t, int score) {
		if (TEAM_SCORES.get(t) == null) throw new IllegalArgumentException("Invalid team");
		synchronized (TEAM_SCORES) {
			TEAM_SCORES.put(t, score);
		}
	}
	
	public boolean allowsRadar() {
		return ALLOWS_RADAR;
	}
	
	public boolean allowsGenerateMaze() {
		return ALLOWS_GENERATE_MAZE;
	}
	
	public boolean allowsHorizontalDirection() {
		return ALLOWS_HORIZONTAL_DIRECTION;
	}
	
	public void assignTeam(Player player) {
		getGameMode().assignTeam(player);
	}
	
	public void requestTeamChange(Player player) {
		getGameMode().requestTeamChange(player);
	}
	
	/**
	 * Attempts to add a valid player to the game.
	 * @param player the player to be added
	 * @return {@code true} if the player was added to the game; otherwise, {@code false}
	 */
	public boolean addPlayer(Player player) {
		if (player != null && !hasPlayer(player) && (getMap().getMaxPlayers() == null || getNumberOfPlayers() < getMap().getMaxPlayers()) && getMap().hasTeam(player.getTeam())) {
			if (PLAYERS.containsKey(player.getID())) throw new IllegalStateException("Player ID(" + player.getID() + ") conflict between " + player.getName() + " and " + PLAYERS.get(player.getID()).getName());
			while (hasName(player.getName())) {
				int openBracketIndex = -1;
				int index = 0;
				try {
					index = Integer.parseInt(player.getName().substring(player.getName().lastIndexOf("(") + 1, player.getName().lastIndexOf(")")));
					openBracketIndex = player.getName().lastIndexOf("(");
				} catch (Exception e) {
					index = 0;
				}
				String newName = player.getName();
				if (openBracketIndex >= 0) {
					newName = newName.substring(0, openBracketIndex);
				}
				newName += "(" + (index + 1) + ")";
				player.setName(newName);
			}
			synchronized (PLAYERS) {
				PLAYERS.put(player.getID(), player);
			}
			if (player.getGame() != this) {
				player.setGame(this);
			}
			getMap().getDefaultPlayerCharacteristics().setToDefaultPlayer(player);
			setToSpawnLocation(player);
			return true;
		}
		return false;
	}
	
	/**
	 * Removes any the association between specific players and the game.
	 * @param players the players to be removed
	 */
	public void removePlayers(Player... players) {
		if (players != null) {
			for (Player p : players) {
				if (hasPlayer(p)) {
					synchronized (PLAYERS) {
						PLAYERS.remove(p.getID());
					}
					p.setGame(null);
				}
			}
		}
	}
	
	public void removeAllPlayers() {
		ArrayList<Player> players = new ArrayList<Player>(PLAYERS.values());
		synchronized (PLAYERS) {
			PLAYERS.clear();
		}
		for (Player p : players) {
			p.setGame(null);
		}
	}
	
	/**
	 * Checks if a player is part of the game, including the main player and spectators(players without a team). Does not include players part of the queue to be added.
	 * @param player the player to be checked
	 * @return {@code true} if the player is part of the game; otherwise, {@code false}
	 */
	public boolean hasPlayer(Player player) {
		return PLAYERS.containsValue(player);
	}
	
	/**
	 * Gets all the players part of the game, including the main player and spectators(players without a team). Does not include players part of the queue to be added.
	 * @return all players part of the game
	 */
	public Player[] getPlayers() {
		return PLAYERS.values().toArray(new Player[getNumberOfPlayers()]);
	}
	
	public Player getPlayerByID(int id) {
		return PLAYERS.get(id);
	}
	
	/**
	 * Gets the total number of players in the game including the main player and any spectators(players with no team). Does not include players part of the queue to be added.
	 * @return the total number of players in the game
	 */
	public int getNumberOfPlayers() {
		return PLAYERS.size();
	}
	
	public int getNumberOfPlayers(Team t) {
		int count = 0;
		for (Player p : getPlayers()) {
			if (t == p.getTeam() || (t != null && t.equals(p.getTeam()))) {
				count++;
			}
		}
		return count;
	}
	
	public int getNumberOfPlayersAlive() {
		int count = 0;
		for (Player p : getPlayers()) {
			if (p.isAlive()) {
				count++;
			}
		}
		return count;
	}
	
	public int getNumberOfPlayersAlive(Team t) {
		int count = 0;
		for (Player p : getPlayers()) {
			if ((t == p.getTeam() || (t != null && t.equals(p.getTeam()))) && p.isAlive()) {
				count++;
			}
		}
		return count;
	}
	
	public boolean hasName(String name) {
		if (name != null) {
			for (Player p : new LinkedList<Player>(PLAYERS.values())) {
				if (name.equals(p.getName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void addRecentKillEvents(KillEvent... events) {
		if (events != null) {
			synchronized (this) {
				for (KillEvent e : events) {
					if (e != null) {
						this.recentKillEvents.add(e);
					}
				}
			}
		}
	}
	
	public KillEvent[] getRecentKillEvents(int mostRecent) {
		synchronized (this) {
			Iterator<KillEvent> iterator = this.recentKillEvents.iterator();
			while (iterator.hasNext()) {
				KillEvent e = iterator.next();
				if (e.getExpireTime() <= System.currentTimeMillis()) {
					iterator.remove();
				}
			}
		}
		KillEvent[] events = new KillEvent[Math.min(this.recentKillEvents.size(), mostRecent)];
		int count = 0;
		for (int i = Math.max(this.recentKillEvents.size() - mostRecent, 0); i < this.recentKillEvents.size(); i++) {
			events[count++] = this.recentKillEvents.get(i);
		}
		return events;
	}
	
	public void sendChatMessages(ChatMessage... messages) {
		if (messages != null) {
			for (ChatMessage m : messages) {
				if (this.multiplayerGameConnection != null) {
					this.multiplayerGameConnection.sendChatMessage(m.getSender(), m);
				}
				for (Player p : getPlayers()) {
					if (m.willRecieveMessage(p)) {
						p.addChatMessage(m.getMessageContent());
					}
				}
			}
		}
	}
	
	public String getMainMessage() {
		return this.mainMessage;
	}
	
	public synchronized void showMainMessage(String mainMessage, long duration) {
		this.mainMessage = mainMessage;
		this.mainMessageExpireTime = System.currentTimeMillis() + duration;
	}
	
	public Long getDefuseBombTime(boolean hasDefuseKit) {
		if (getPlantedBomb() != null) {
			return hasDefuseKit ? getPlantedBomb().getDefuseKitTime() : getPlantedBomb().getDefuseTime();
		}
		return null;
	}
	
	public boolean isLookingAtBomb(Player defuser) {
		return defuser != null && getPlantedBomb() != null && defuser.isHorizontalAngleInView(Compass.getPrincipleAngle(defuser.getLocationX(), getPlantedBomb().getPlantedBomb().getLocationY(), getPlantedBomb().getPlantedBomb().getLocationX(), defuser.getLocationY()));
	}
	
	public boolean canDefuseBomb(Player defuser) {
		if (defuser != null && getPlantedBomb() != null && getDefuseBombTime(defuser.hasDefuseKit()) != null) {
			Bomb.PlantedBomb bombLocation = getPlantedBomb().getPlantedBomb();
			if (bombLocation != null && defuser.getZoom() == 1 && defuser.getTeam() != null && defuser.getTeam().isDefuseTeam() && Map.findDistance(bombLocation.getLocationX(), bombLocation.getLocationY(), bombLocation.getCenterHeight(), defuser.getLocationX(), defuser.getLocationY(), defuser.getViewHeight()) <= getPlantedBomb().getDefuseRadius()) {
				return isLookingAtBomb(defuser);
			}
		}
		return false;
	}
	
	public boolean isBombDetonated() {
		return this.bombDetonationState != null && this.bombDetonationState;
	}
	
	public boolean isBombDefused() {
		return this.bombDetonationState != null && !this.bombDetonationState;
	}
	
	public void assignBomb() {
		if (getMap().isBombMap()) {
			getGameMode().assignBomb(this);
		}
	}
	
	public void defuseBomb(boolean playDefuseBombSound) {
		if (getPlantedBomb() != null) {
			synchronized (DROPPED_ITEMS) {
				DROPPED_ITEMS.remove(getPlantedBomb().getPlantedBomb());
				DROPPED_ITEMS.remove(getPlantedBomb().getDroppedBomb());
			}
			synchronized (this) {
				this.nextBombBeepInterval = null;
				this.bombDetonationState = false;
			}
			for (Player p : getPlayers()) {
				p.cancelDefusingBomb();
			}
			if (playDefuseBombSound) {
				showMainMessage(Bomb.BOMB_DEFUSED_MESSAGE, Bomb.BOMB_DEFUSED_MESSAGE_DURATION);
				getPlantedBomb().playBombDefusedSound();
			}
			plantBomb(null);
		}
	}
	
	public Bomb getPlantedBomb() {
		return this.plantedBomb;
	}
	
	public void plantBomb(Bomb bomb) {
		synchronized (this) {
			this.plantedBomb = bomb;
		}
		if (getPlantedBomb() != null) {
			synchronized (this) {
				getPlantedBomb().setLastBeepTime();
				this.nextBombBeepInterval = getPlantedBomb().getNextBeepInterval(false);
			}
			getPlantedBomb().playBombPlantedSound();
			showMainMessage(Bomb.BOMB_PLANTED_MESSAGE, Bomb.BOMB_PLANTED_MESSAGE_DURATION);
		}
	}
	
	public void setToSpawnLocation(Player player) {
		if (hasPlayer(player) && !player.isSpectator()) {
			Map.Point3D spawnLocation = getMap().getSpawnLocation(player);
			if (spawnLocation == null) {
				spawnLocation = getMap().getRandomLocation();
			}
			if (player.isMoveableTo(spawnLocation.x, spawnLocation.y, true, this, true)) {
				player.setLocation(spawnLocation.x, spawnLocation.y, spawnLocation.z);
			}
		}
	}
	
	public MapItem[] getMapItems() {
		ArrayList<MapItem> mapItems = new ArrayList<MapItem>();
		for (int y = 0; y < getMap().getHeight(); y++) {
			for (int x = 0; x < getMap().getWidth(); x++) {
				mapItems.add(getMap().getWall(x, y));
			}
		}
		synchronized (THROWN_WEAPONS) {
			for (ThrownWeapon w : THROWN_WEAPONS) {
				if (w != null) {
					mapItems.add(w);
				}
			}
		}
		synchronized (DROPPED_ITEMS) {
			for (DroppedItem i : DROPPED_ITEMS) {
				if (i != null) {
					mapItems.add(i);
				}
			}
		}
		for (Player p : getPlayers()) {
			if (p != null) {
				mapItems.add(p);
			}
		}
		for (Fence f : getMap().getFences()) {
			if (f != null) {
				mapItems.add(f);
			}
		}
		for (Ceiling c : getMap().getCeilings()) {
			if (c != null) {
				mapItems.add(c);
			}
		}
		return mapItems.toArray(new MapItem[mapItems.size()]);
	}
	
	CylinderMapItem[] getCylinderMapItemsInRange() {
		return getCylinderMapItemsInRange(0, 0, Map.findDistance(getMap().getWidth(), getMap().getHeight()), 0, null, true);
	}
	
	CylinderMapItem[] getCylinderMapItemsInRange(double x, double y, double range, boolean includeSpectators) {
		return getCylinderMapItemsInRange(x, y, range, 0, null, includeSpectators);
	}
	
	CylinderMapItem[] getCylinderMapItemsInRange(double x, double y, double range, double innerRange, Player doNotGetPlayer, boolean includeSpectators) {
		ArrayList<CylinderMapItem> mapItems = new ArrayList<CylinderMapItem>();
		for (Player p : getPlayers()) {
			double distance = Math.max(Map.findDistance(p, x, y) - p.getPhysicalHalfWidth(), 0);
			if (!p.equals(doNotGetPlayer) && distance >= innerRange && distance <= range && (includeSpectators || !p.isSpectator())) {
				mapItems.add(p);
			}
		}
		synchronized (THROWN_WEAPONS) {
			for (ThrownWeapon i : THROWN_WEAPONS) {
				double distance = Math.max(Map.findDistance(i, x, y) - i.getPhysicalHalfWidth(), 0);
				if (distance >= innerRange && distance <= range) {
					mapItems.add(i);
				}
			}
		}
		synchronized (DROPPED_ITEMS) {
			for (DroppedItem i : DROPPED_ITEMS) {
				double distance = Math.max(Map.findDistance(i, x, y) - i.getPhysicalHalfWidth(), 0);
				if (distance >= innerRange && distance <= range) {
					mapItems.add(i);
				}
			}
		}
		return mapItems.toArray(new CylinderMapItem[mapItems.size()]);
	}
	
	Area getLayoutOfPlayers(Player ignorePlayer, boolean includeSpectators, double bottomHeight, double topHeight) {
		Area layout = new Area();
		for (Player p : getPlayers()) {
			if (!p.equals(ignorePlayer) && (includeSpectators || !p.isSpectator()) && p.getBottomHeight() < topHeight && p.getTopHeight() > bottomHeight) {
				layout.add(p.getBase());
			}
		}
		return layout;
	}
	
	Double getHighestPlayerUnderPlayer2DBase(Player player, boolean includeSpectators, double underHeight) {
		Double highest = null;
		for (Player p : getPlayers()) {
			if (!p.equals(player) && (includeSpectators || !p.isSpectator()) && p.getTopHeight() <= underHeight && Map.findDistance2D(player, p) <= player.getPhysicalHalfWidth() + p.getPhysicalHalfWidth()) {
				Area pArea = new Area(p.getBase());
				pArea.intersect(p.getBase());
				if (!pArea.isEmpty()) {
					highest = p.getTopHeight();
				}
			}
		}
		return highest;
	}
	
	public Long isRoundCompleted() {
		return getGameMode().isRoundCompleted(this);
	}
	
	public boolean isGameCompleted() {
		return getGameMode().isGameCompleted(this);
	}
	
	private synchronized void stopGame() {
		for (Player p : getPlayers()) {
			p.cancelDefusingBomb();
		}
		synchronized (BULLETS) {
			BULLETS.clear();
		}
		synchronized (THROWN_WEAPONS) {
			THROWN_WEAPONS.clear();
		}
		synchronized (DROPPED_ITEMS) {
			DROPPED_ITEMS.clear();
		}
		synchronized (this.recentKillEvents) {
			this.recentKillEvents.clear();
		}
		this.bombDetonationState = null;
		if (this.multiplayerGameConnection != null) {
			try {
				this.multiplayerGameConnection.closeConnection();
			} catch (IOException e) {
			}
		}
		getGameMode().gameCompleted(this);
	}
	
	public boolean isRunning() {
		return IS_RUNNING.get();
	}
	
	public boolean isBotMovementPaused() {
		return BOT_MOVEMENT_PAUSED.get();
	}
	
	public boolean canPlayersMove() {
		return getRoundDuration() >= 0;
	}

	public void shootBullet(Bullet bullet) {
		shootBullet(bullet, false);
	}
	
	/**
	 * Shoots a bullet in the game.
	 * @param bullet the bullet being shot
	 * @param addBulletDirectly {@code true} if only the bullet is to be added and any listeners should not be notified, otherwise {@code false}
	 */
	public void shootBullet(Bullet bullet, boolean addBulletDirectly) {
		synchronized (this) {
			BULLETS.add(bullet);
		}
		if (!addBulletDirectly && this.multiplayerGameConnection != null) {
			this.multiplayerGameConnection.shootBullet(getPlayerByID(bullet.getShooterID()), bullet);
		}
	}
	
	private void checkBullets(long timePassed) {
		synchronized (this) {
			Iterator<Bullet> iterator = BULLETS.iterator();
			while (iterator.hasNext()) {
				Bullet b = iterator.next();
				double locationX = b.getLocationX();
				double locationY = b.getLocationY();
				b.moveBullet(timePassed);
				Line2D movementPath = new Line2D.Double(locationX, locationY, b.getLocationX(), b.getLocationY());
				
				ArrayList<MapItemWithShotDistance> hitItems = new ArrayList<MapItemWithShotDistance>();
				for (MapItem i : getMapItems()) {
					if (!getPlayerByID(b.getShooterID()).equals(i) && i.intersects(movementPath)) {
						Point2D pointOfIntersection = i.getIntersectionPoint(b.getTravelPath2D());
						if (pointOfIntersection != null) {
							double distance = Map.findDistance(b.getTravelPath2D(), pointOfIntersection.getX(), pointOfIntersection.getY());
							hitItems.add(new MapItemWithShotDistance(i, distance));
						}
					}
				}
				for (Player p : getPlayers()) {
					if (p.getTacticalShield() != null) {
						Line2D tacticalShieldPosition = p.getCurrentTacticalShieldBaseLocation();
						if (tacticalShieldPosition.intersectsLine(movementPath)) {
							Point2D pointOfIntersection = b.getTravelPath2D().getIntersectionPoint(tacticalShieldPosition);
							if (pointOfIntersection != null) {
								double distance = Map.findDistance(b.getTravelPath2D(), pointOfIntersection.getX(), pointOfIntersection.getY());
								hitItems.add(new MapItemWithShotDistance(p.getTacticalShield(), p, distance));
							}
						}
					}
				}
				Collections.sort(hitItems);
				boolean removed = false;
				for (MapItemWithShotDistance i : hitItems) {
					if (i.baseHitByBullet(b, this)) {
						removed = true;
						iterator.remove();
						break;
					}
				}
				if (!removed && !getMap().inGrid(b.getLocationX(), b.getLocationY())) {
					getMap().edgeHitByBullet(b);
					iterator.remove();
				}
			}
		}
	}
	
	public void throwWeapon(ThrownWeapon weapon) {
		if (weapon != null) {
			synchronized (THROWN_WEAPONS) {
				THROWN_WEAPONS.add(weapon);
			}
		}
	}
	
	private void checkThrowWeapons(long mspf) {
		synchronized (THROWN_WEAPONS) {
			Iterator<ThrownWeapon> iterator = THROWN_WEAPONS.iterator();
			while (iterator.hasNext()) {
				ThrownWeapon w = iterator.next();
				if (w.isExplosionCompleted()) {
					iterator.remove();
				} else {
					if (!w.isExploding()) {
						w.move(mspf);
						if (w.getFuse() != null && w.getTimeInAir() >= w.getFuse()) {
							w.explode();
							for (MapItem i : getMapItems()) {
								i.hitByExplosion(w, w.getLocationX(), w.getLocationY());
							}
						}
					}
					if (w.isExplosionCompleted()) {
						iterator.remove();
					}
				}
			}
		}
	}
	
	public void dropItem(DroppedItem item) {
		if (item != null) {
			synchronized (DROPPED_ITEMS) {
				DROPPED_ITEMS.add(item);
			}
		}
	}
	
	public void dropItem(DroppedItem item, double x, double y, double z) {
		if (item != null) {
			item.drop(this, x, y, z);
			synchronized (DROPPED_ITEMS) {
				DROPPED_ITEMS.add(item);
			}
		}
	}
	
	private void checkDropItems(long mspf) {
		Iterator<DroppedItem> iterator = null;
		synchronized (DROPPED_ITEMS) {
			iterator = new ArrayList<DroppedItem>(DROPPED_ITEMS).iterator();
		}
		ArrayList<DroppedItem> itemsToRemove = new ArrayList<DroppedItem>();
		while (iterator.hasNext()) {
			DroppedItem droppedItem = iterator.next();
			droppedItem.move(mspf);
			if (droppedItem.isCompletedDrop()) {
				Area item = new Area(droppedItem.getBase());
				for (Player p : getPlayers()) {
					if (p != null && !p.isSpectator()) {
						Boolean willPickUp = droppedItem.willPickUp(p);
						if ((willPickUp != null && willPickUp) || (willPickUp == null && p.willPickUp(droppedItem))) {
							Area player = new Area(p.getBase());
							player.intersect(item);
							if (!player.isEmpty()) {
								if (p.getTopHeight() > droppedItem.getBottomHeight() && p.getBottomHeight() < droppedItem.getTopHeight()) {
									itemsToRemove.add(droppedItem);
									droppedItem.itemPickedUp(p);
									break;
								}
							}
						}
					}
				}
			}
		}
		synchronized (DROPPED_ITEMS) {
			for (DroppedItem i : itemsToRemove) {
				DROPPED_ITEMS.remove(i);
			}
		}
	}
	
	private void checkTouchDamage(Player attacker, Player player) {
		if (!attacker.equals(player) && attacker.getTeam() != null && player.getTeam() != null && !attacker.getTeam().equals(player.getTeam())) {
			if (attacker.getTouchDamage() > 0 && attacker.getTopHeight() >= player.getBottomHeight() && attacker.getBottomHeight() <= player.getTopHeight() && Map.findDistance2D(player, attacker) <= player.getPhysicalHalfWidth() + attacker.getPhysicalHalfWidth() + attacker.getReachLength()) {
				player.hitByTouchDamage(attacker.getTouchDamage(), new Line2D.Double(player.getLocationX(), player.getLocationY(), attacker.getLocationX(), attacker.getLocationY()), attacker);
			}
		}
	}
	
	private void checkTouchDamage() {
		for (Player player : getPlayers()) {
			for (Player attacker : getPlayers()) {
				checkTouchDamage(attacker, player);
			}
		}
	}
	
	private void checkLavaDamage() {
		for (Player p : getPlayers()) {
			if (!p.isSpectator() && p.getBottomHeight() <= getMap().getWall(p.getLocationX(), p.getLocationY()).getTopHeight(Integer.MAX_VALUE) + 0.1 && getMap().getWall(p.getLocationX(), p.getLocationY()).getLavaDamage() > 0) {
				p.onLava(getMap().getWall(p.getLocationX(), p.getLocationY()).getLavaDamage());
			}
		}
	}
	
	private void checkBomb() {
		if (getPlantedBomb() != null) {
			if (getPlantedBomb().isDetonated()) {
				for (Player p : getPlayers()) {
					double distance = Map.findDistance2D(p, getPlantedBomb().getDropItem());
					if (distance <= getPlantedBomb().getBlastRadius()) {
						p.setHealth(p.getHealth() - getPlantedBomb().getBlastDamage(distance));
					}
				}
				getPlantedBomb().playBombExplosionSound();
				defuseBomb(false);
				if (getMap().hasTeam(Team.DefaultTeams.TERRORISTS.getTeam())) {
					Team.playTerroristsWinSound();
					showMainMessage(Team.TERRORISTS_WIN_MESSAGE, Team.TERRORISTS_WIN_MESSAGE_DURATION);
				}
				synchronized (this) {
					this.bombDetonationState = true;
				}
			} else if (this.nextBombBeepInterval != null) {
				if (System.currentTimeMillis() >= this.nextBombBeepInterval) {
					getPlantedBomb().playBombBeepSound();
					synchronized (this) {
						getPlantedBomb().setLastBeepTime();
						this.nextBombBeepInterval = getPlantedBomb().getNextBeepInterval(false);
					}
				}
			}
		}
	}
	
	private void checkMainMessage() {
		if (this.mainMessageExpireTime != null && System.currentTimeMillis() >= this.mainMessageExpireTime) {
			synchronized (this) {
				this.mainMessage = null;
				this.mainMessageExpireTime = null;
			}
		}
	}
	
	public void playerTerminated(Player player) {
		if (player.getBomb() != null) {
			player.dropCarryItem(player.getBomb());
		}
		if (player.getDefuseKit() != null) {
			player.dropDefuseKit();
		}
		getGameMode().playerTerminated(player);
	}
	
	@Override
	public void playerMoved(Player player) {
		getGameMode().playerMoved(player);
		if (this.multiplayerGameConnection != null) {
			this.multiplayerGameConnection.playerMoved(player);
		}
	}
	
	public void playerTeamChanged(Player player) {
		if (this.multiplayerGameConnection != null) {
			this.multiplayerGameConnection.playerTeamChanged(player);
		}
	}
	
	public void movePlayersWithControllers(long mspf) {
		if (!isBotMovementPaused()) {
			for (Player p : getPlayers()) {
				p.moveWithController(mspf);
			}
		}
	}
	
	private void checkAutomaticDoors() {
		Player[] players = getPlayers();
		for (Door d : getMap().getDoors()) {
			if (d.willDoorAutomaticallyClose(players)) {
				d.setOpened(false);
			}
		}
	}
	
	private void checkRoundCompleted() {
		Long completionDelay = isRoundCompleted();
		if (completionDelay == null) {
			completionDelay = this.isRoundCompleted;
		}
		if (completionDelay != null) {
			if (getGameMode().roundCompleted(this)) {
				stop();
			} else {
				try {
					Thread.sleep(completionDelay);
				} catch (InterruptedException e) {
				}
				startNewRound();
			}
		}
	}
	
	public long getRoundDuration() {
		return System.currentTimeMillis() - this.roundStartTimer;
	}
	
	public synchronized void resetRoundDuration(long freezeTime) {
		this.roundStartTimer = System.currentTimeMillis() + freezeTime;
	}
	
	public long getGameDuration() {
		return System.currentTimeMillis() - this.gameStartTimer;
	}
	
	public synchronized void resetGameDuration() {
		this.gameStartTimer = System.currentTimeMillis();
	}
	
	public boolean allowsFriendlyFire() {
		return this.friendlyFire;
	}
	
	public void setFriendlyFire(boolean b) {
		this.friendlyFire = b;
	}
	
	public boolean isServer() {
		return this.multiplayerGameConnection != null && this.multiplayerGameConnection.isServer() && this.multiplayerGameConnection.isRunning();
	}
	
	public boolean isClient() {
		return this.multiplayerGameConnection != null && this.multiplayerGameConnection.isClient() && this.multiplayerGameConnection.isRunning();
	}
	
	public String getServerIP() {
		if (this.multiplayerGameConnection != null) {
			return this.multiplayerGameConnection.getServerIP();
		}
		return null;
	}
	
	public synchronized void setServer(MultiplayerGameServer multiplayerGameServer) {
		this.multiplayerGameConnection = multiplayerGameServer;
	}
	
	public synchronized void setClient(MultiplayerGameClient multiplayerGameClient) {
		this.multiplayerGameConnection = multiplayerGameClient;
	}
	
	public void start() {
		if (!isRunning()) {
			getGameMode().startNewGame(Game.this);
			
			IS_RUNNING.set(true);
			BOT_MOVEMENT_PAUSED.set(false);
			resetGameDuration();
			startNewRound();

			new Thread(new Runnable() {
				@Override
				public void run() {
					long cTime = Game.this.gameStartTimer;
					while (isRunning()) {
						long mspf = System.currentTimeMillis() - cTime;
						cTime = System.currentTimeMillis();
						checkBullets(mspf);
						checkThrowWeapons(mspf);
						checkDropItems(mspf);
						checkTouchDamage();
						checkLavaDamage();
						checkBomb();
						checkMainMessage();
						movePlayersWithControllers(mspf);
						checkAutomaticDoors();
						try {Thread.sleep(20);} catch (Exception e) {}
						if (isGameCompleted()) stop();
						checkRoundCompleted();
					}
					stopGame();
				}
			}, "Main Game Thread-" + ++Game.numberOfGames).start();
		}
	}
	
	public synchronized void endRound(long freezeTime) {
		this.isRoundCompleted = freezeTime;
	}
	
	public void startNewRound() {
		getGameMode().startNewRound(this);
		startRoundSetup();
		placeInitialMapItems();
		getMap().clearAllBulletMarkings();
		resetRoundDuration(DEFAULT_FREEZE_TIME);
		synchronized (this) {
			this.isRoundCompleted = null;
		}
	}
	
	private synchronized void startRoundSetup() {
		for (Player p : getPlayers()) {
			p.resetBasicTermination(false, true, null, true);
		}
		synchronized (BULLETS) {
			BULLETS.clear();
		}
		synchronized (THROWN_WEAPONS) {
			THROWN_WEAPONS.clear();
		}
		synchronized (DROPPED_ITEMS) {
			DROPPED_ITEMS.clear();
		}
		this.recentKillEvents.clear();
		this.bombDetonationState = null;
		this.nextBombBeepInterval = null;
		defuseBomb(false);
		assignBomb();
	}
	
	private void placeInitialMapItems() {
		if (getMap() != null && getMap().getInitialDroppedWeapons() != null) {
			for (Map.InitialDroppedWeapon i : getMap().getInitialDroppedWeapons()) {
				if (i != null) {
					i.getWeapon().reset();
					dropItem(i.getWeapon().getDropItem(), i.getLocationX(), i.getLocationY(), i.getLocationZ());
				}
			}
		}
	}
	
	public void pauseBotMovement() {
		BOT_MOVEMENT_PAUSED.set(true);
	}
	
	public void resumeBotMovement() {
		BOT_MOVEMENT_PAUSED.set(false);
	}
	
	public void stop() {
		if (IS_RUNNING.get()) {
			IS_RUNNING.set(false);
		}
	}
	
	public void addGameListeners(GameListener... listeners) {
		if (listeners != null) {
			for (GameListener l : listeners) {
				if (l != null) {
					synchronized (this) {
						this.listeners.add(l);
					}
				}
			}
		}
	}
	
	public void removeGameListeners(GameListener... listeners) {
		if (listeners != null) {
			for (GameListener l : listeners) {
				if (l != null) {
					synchronized (this) {
						this.listeners.remove(l);
					}
				}
			}
		}
	}
	
	public synchronized void removeAllGameListeners() {
		this.listeners.clear();
	}

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.recentKillEvents = new ArrayList<KillEvent>();
        this.multiplayerGameConnection = null;
        IS_RUNNING.set(false);
        BOT_MOVEMENT_PAUSED.set(false);
        this.listeners = new ArrayList<GameListener>();
    }
	
	public static class Builder {
		
		private GameMode gameMode = null;
		private Map map = null;
		private boolean allowsRadar = true;
		private boolean allowsGenerateMaze = false;
		private boolean allowsHorizontalDirection = true;
		
		public Builder(GameMode gameMode) {
			gameMode(gameMode);
		}
		
		public Builder gameMode(GameMode gameMode) {
			this.gameMode = gameMode;
			return this;
		}
		
		public Builder map(Map map) {
			this.map = map;
			return this;
		}
		
		public Builder allowsRadar(boolean allowsRadar) {
			this.allowsRadar = allowsRadar;
			return this;
		}
		
		public Builder allowsGenerateMaze(boolean allowsGenerateMaze) {
			this.allowsGenerateMaze = allowsGenerateMaze;
			return this;
		}
		
		public Builder allowsHorizontalDirection(boolean allowsHorizontalDirection) {
			this.allowsHorizontalDirection = allowsHorizontalDirection;
			return this;
		}
		
		public Game build() {
			return new Game(this);
		}
	}
	
	private static class MapItemWithShotDistance implements Comparable<MapItemWithShotDistance> {
		private final MapItem MAP_ITEM;
		private final TacticalShield TACTICAL_SHIELD;
		private final Player TACTICAL_SHIELD_HOLDER;
		private final double DISTANCE;
		private MapItemWithShotDistance(MapItem mapItem, double distance) {
			MAP_ITEM = mapItem;
			TACTICAL_SHIELD = null;
			TACTICAL_SHIELD_HOLDER = null;
			DISTANCE = Math.abs(distance);
		}
		private MapItemWithShotDistance(TacticalShield tacticalShield, Player tacticalShieldHolder, double distance) {
			MAP_ITEM = null;
			TACTICAL_SHIELD = tacticalShield;
			TACTICAL_SHIELD_HOLDER = tacticalShieldHolder;
			DISTANCE = Math.abs(distance);
		}
		private boolean baseHitByBullet(Bullet b, Game game) {
			if (MAP_ITEM != null) {
				return MAP_ITEM.baseHitByBullet(b, game, DISTANCE);
			} else {
				return TACTICAL_SHIELD.baseHitByBullet(b, TACTICAL_SHIELD_HOLDER, DISTANCE);
			}
		}
		@Override
		public int compareTo(MapItemWithShotDistance o) {
			return DISTANCE == o.DISTANCE ? 0 : (DISTANCE > o.DISTANCE ? 1 : -1);
		}
	}
	
	static class KillEvent {
		public static final long DISPLAY_DURATION = 3000;
		public static final BufferedImage HEADSHOT_ICON = Main.getImage(Main.HEADSHOT_ICON_IMAGE_PATH, null);
		
		private final Player KILLER;
		private final CauseOfDeath CAUSE;
		private final boolean IS_HEAD_SHOT;
		private final Player VICTIM;
		private final long EXPIRE_TIME;
		
		KillEvent(Player killer, CauseOfDeath cause, boolean isHeadShot, Player victim) {
			if (victim == null) throw new IllegalArgumentException("Victim cannot be null");
			KILLER = killer;
			CAUSE = cause;
			IS_HEAD_SHOT = isHeadShot;
			VICTIM = victim;
			EXPIRE_TIME = System.currentTimeMillis() + DISPLAY_DURATION;
		}
		
		Player getKiller() {
			return KILLER;
		}
		
		CauseOfDeath getCauseOfDeath() {
			return CAUSE;
		}
		
		boolean isHeadShot() {
			return IS_HEAD_SHOT;
		}
		
		Player getVictim() {
			return VICTIM;
		}
		
		long getExpireTime() {
			return EXPIRE_TIME;
		}
	}
}
