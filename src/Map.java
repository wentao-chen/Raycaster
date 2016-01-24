import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * {@code Map} represents the 2-dimensional layout of the space in which the game takes place.
 * <br><br>
 * The width and height of the map is defined in the constructor and cannot be modified.
 * <br>
 * The coordinates of the cell that a pair of double-precision coordinates lie can be found using the floor function. For example, the point (1.23, 3.99) lies within the cell (1, 3).
 * @author Wentao Chen
 *
 */
public class Map implements Serializable {
	private static final long serialVersionUID = 1450273801596934337L;
	
	/**
	 * The default wall color.
	 * @see Color#BLUE
	 */
	public static final Color DEFAULT_WALL_COLOR = Color.BLUE;
	/**
	 * The default floor color (R = 50, G = 50, B = 50).
	 */
	public static final Color DEFAULT_FLOOR_COLOR = new Color(50, 50, 50);
	/**
	 * The default background color.
	 * @see Color#BLACK
	 */
	public static final Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;
	/**
	 * The standard acceleration due to gravity near the surface of the earth of {@code -9.8 [m s^-2]}
	 */
	public static final double DEFAULT_GRAVITY = -9.8;
	/**
	 * A {@code StoreLocator} that allows the store to be opened anywhere within the map.
	 */
	public static final StoreLocator DEFAULT_EVERYWHERE_STORE_LOCATOR = new StoreLocator.EveryWhereStoreLocator();
	/**
	 * A {@code SpawnLocator} that spawns at a random location on the map.
	 */
	public static final SpawnLocator DEFAULT_EVERYWHERE_SPAWNER = new SpawnLocator.RandomSpawner();
	
	private static AtomicBoolean CAN_RADAR_VIEW_ALL_PLAYERS = new AtomicBoolean(false);

	/**
	 * The default console command for setting perfect accuracy.
	 */
	public static final Console.Command DEFAULT_RADAR_DETECTION_COMMAND = new Console.Command("Radar Detection", "", "radar") {
    	@Override
    	public String getDescription() {
    		return "Sets radar detection to " + (CAN_RADAR_VIEW_ALL_PLAYERS.get() ? "limited" : "all");
    	}
		@Override
		public boolean execute(Console console, Game game, String input) {
			CAN_RADAR_VIEW_ALL_PLAYERS.set(!CAN_RADAR_VIEW_ALL_PLAYERS.get());
			console.appendText("ACTION: Radar Detection " + (CAN_RADAR_VIEW_ALL_PLAYERS.get() ? "ALL" : "LIMITED"));
			return true;
		}
    };
	
	private final String NAME;
	private final ArrayList<Fence> FENCES = new ArrayList<Fence>();
	private final ArrayList<Ceiling> CEILINGS = new ArrayList<Ceiling>();
	private final ArrayList<Door> DOORS = new ArrayList<Door>();
	private final ArrayList<Ladder> LADDERS = new ArrayList<Ladder>();
	private final Wall[][] GRID;
	private final Team[] TEAMS;
	private final Integer MAX_PLAYERS;
	private Color floorColor;
	private Color wallColor;
	private Color backgroundColor;
	private transient BufferedImage backgroundImage = null;
	private final double GRAVITY;
	private final StoreLocator STORE_LOCATOR;
	private final double DEFAULT_SIGHT_RANGE;
	private final SpawnLocator SPAWN_LOCATOR;
	private final BombSiteLocator BOMB_SITE_LOCATOR;
	private final Color EDGE_WALL_COLOR;
	private final double EDGE_WALL_HEIGHT;
	private final ArrayList<Bullet.BulletMarking> BULLET_MARKINGS = new ArrayList<Bullet.BulletMarking>();
	
	private final DefaultPlayerCharacteristics DEFAULT_PLAYER_CHARACTERISTICS;
	private final InitialDroppedWeapon[] INITIAL_DROPPED_WEAPONS;
	private final InitialSpectatorViewLocation INITIAL_SPECTATOR_VIEW_LOCATION;
	
	private transient Team tempTeam = null;
	
	/**
	 * Checks a shallow copy of a map.
	 * @param map the map to be copied
	 * @see #Map(int, int, String, Integer, Color, Color, Color, double, StoreLocator, double, SpawnLocator, DefaultPlayerCharacteristics, BombSiteLocator, InitialDroppedWeapon[], InitialSpectatorViewLocation, Color, double, Team...)
	 */
	public Map(Map map) {
		this(map.getWidth(), map.getHeight(), map.getName(), map.getMaxPlayers(), map.getWallColor(), map.getFloorColor(), map.getBackgroundColor(), map.getGravity(), map.STORE_LOCATOR, map.SPAWN_LOCATOR, map.DEFAULT_PLAYER_CHARACTERISTICS, map.BOMB_SITE_LOCATOR, map.INITIAL_DROPPED_WEAPONS, map.INITIAL_SPECTATOR_VIEW_LOCATION, map.EDGE_WALL_COLOR, map.EDGE_WALL_HEIGHT, map.getTeams());
		for (int y = 0; y < map.getHeight(); y++) {
			for (int x = 0; x <  map.getWidth(); x++) {
				setWall(map.getWall(x, y));
			}
		}
	}
	
	/**
	 * Constructs a basic map. Convenience method for {@link #Map(int, int, String, Integer, Color, Color, Color, double, StoreLocator, SpawnLocator, DefaultPlayerCharacteristics, BombSiteLocator, InitialDroppedWeapon[], InitialSpectatorViewLocation, Color, double, Team...)} with
	 * <ul>
	 * <li>{@code maxPlayers = null}</li>
	 * <li>{@code wallColor = }{@link #DEFAULT_WALL_COLOR}</li>
	 * <li>{@code floorColor = }{@link #DEFAULT_FLOOR_COLOR}</li>
	 * <li>{@code backgroundColor = }{@link #DEFAULT_BACKGROUND_COLOR}</li>
	 * <li>{@code gravity = }{@link #DEFAULT_GRAVITY}</li>
	 * <li>{@code storeLocator = }{@link #DEFAULT_EVERYWHERE_STORE_LOCATOR}</li>
	 * <li>{@code spawner = }{@link #DEFAULT_EVERYWHERE_SPAWNER}</li>
	 * <li>{@code defaultPlayerCharacteristics = null}</li>
	 * </ul>
	 * @param width the width of the map
	 * @param height the height of the map
	 * @param name the name of the map
	 * @param teams the teams in the map
	 */
	public Map(int width, int height, String name, Team... teams) {
		this(width, height, name, null, DEFAULT_WALL_COLOR, DEFAULT_FLOOR_COLOR, DEFAULT_BACKGROUND_COLOR, DEFAULT_GRAVITY, DEFAULT_EVERYWHERE_STORE_LOCATOR, DEFAULT_EVERYWHERE_SPAWNER, null, null, null, null, null, 0, teams);
	}
	
	/**
	 * Constructs a map. Convenience method for {@link #Map(int, int, String, Integer, Color, Color, Color, double, StoreLocator, double, SpawnLocator, DefaultPlayerCharacteristics, BombSiteLocator, InitialDroppedWeapon[], InitialSpectatorViewLocation, Color, double, Team...)} with {@code defaultSightRange} as the maximum possible distance
	 * @param width the width of the map
	 * @param height the height of the map
	 * @param name the name of the map
	 * @param maxPlayers the maximum number of players allowed in the map at the same time
	 * @param wallColor the default wall color of the walls within the map
	 * @param floorColor the default floor color of the map
	 * @param backgroundColor the default background color of the map
	 * @param gravity the default gravitational acceleration of the map
	 * @param storeLocator the default store location for determining whether the store can be opened at a specific location
	 * @param spawnLocator the spawn locator that finds the spawn location of a player
	 * @param defaultPlayerCharacteristics the default characteristics assigned to players initially when joined to the map
	 * @param bombSiteLocator the locator used to determine the location where a bomb can be planted or {@code null} if the map does not allow bombs to be planted
	 * @param initialDroppedWeapons the weapons placed at the start of a game in the map
	 * @param initialSpectatorViewLocation the initial spectator view location when joining the game
	 * @param edgeWallColor the color of the wall at the edge of the map
	 * @param edgeWallHeight the height of the wall at the edge of the map
	 * @param teams the teams in the map
	 */
	public Map(int width, int height, String name, Integer maxPlayers, Color wallColor, Color floorColor, Color backgroundColor, double gravity, StoreLocator storeLocator, SpawnLocator spawnLocator, DefaultPlayerCharacteristics defaultPlayerCharacteristics, BombSiteLocator bombSiteLocator, InitialDroppedWeapon[] initialDroppedWeapons, InitialSpectatorViewLocation initialSpectatorViewLocation, Color edgeWallColor, double edgeWallHeight, Team... teams) {
		this(width, height, name, maxPlayers, wallColor, floorColor, backgroundColor, gravity, storeLocator, Map.findDistance(width, height), spawnLocator, defaultPlayerCharacteristics, bombSiteLocator, initialDroppedWeapons, initialSpectatorViewLocation, edgeWallColor, edgeWallHeight, teams);
	}
	
	/**
	 * Constructs a map. The width and height of the map must be greater than 0.
	 * <br><br>
	 * The walls of the map are automatically constructed with a height of 0 and the specified default wall color.
	 * <br><br>
	 * The gravitational acceleration of the map is taken as the negative absolute value of the specified gravity.
	 * <br><br>
	 * The background of the map includes the space behind all objects in the map that is above the horizon
	 * @param width the width of the map
	 * @param height the height of the map
	 * @param name the name of the map
	 * @param maxPlayers the maximum number of players allowed in the map at the same time
	 * @param wallColor the default wall color of the walls within the map
	 * @param floorColor the default floor color of the map
	 * @param backgroundColor the default background color of the map
	 * @param gravity the default gravitational acceleration of the map
	 * @param storeLocator the default store location for determining whether the store can be opened at a specific location
	 * @param defaultSightRange the default sight range for projections plane in the map
	 * @param spawnLocator the spawn locator that finds the spawn location of a player
	 * @param defaultPlayerCharacteristics the default characteristics assigned to players initially when joined to the map
	 * @param bombSiteLocator the locator used to determine the location where a bomb can be planted or {@code null} if the map does not allow bombs to be planted
	 * @param initialDroppedWeapons the weapons placed at the start of a game in the map
	 * @param initialSpectatorViewLocation the initial spectator view location when joining the game
	 * @param edgeWallColor the color of the wall at the edge of the map
	 * @param edgeWallHeight the height of the wall at the edge of the map
	 * @param teams the teams in the map
	 */
	public Map(int width, int height, String name, Integer maxPlayers, Color wallColor, Color floorColor, Color backgroundColor, double gravity, StoreLocator storeLocator, double defaultSightRange, SpawnLocator spawnLocator, DefaultPlayerCharacteristics defaultPlayerCharacteristics, BombSiteLocator bombSiteLocator, InitialDroppedWeapon[] initialDroppedWeapons, InitialSpectatorViewLocation initialSpectatorViewLocation, Color edgeWallColor, double edgeWallHeight, Team... teams) {
		if (width < 1) throw new IllegalArgumentException("Map width cannot be less than 1");
		if (height < 1) throw new IllegalArgumentException("Map height cannot be less than 1");
		if (name == null) throw new IllegalArgumentException("name cannot be null");
		if (spawnLocator == null) throw new IllegalArgumentException("spawn locator cannot be null");
		if (wallColor == null) throw new IllegalArgumentException("wall color cannot be null");
		if (floorColor == null) throw new IllegalArgumentException("floor color cannot be null");
		if (backgroundColor == null) throw new IllegalArgumentException("background color cannot be null");
		GRID = new Wall[height][width];
		for (int y = 0; y < GRID.length; y++) {
			for (int x = 0; x < GRID[y].length; x++) {
				GRID[y][x] = new Wall.Builder(x, y).color(wallColor).build();
			}
		}
		if (teams != null && teams.length > 0) {
			int nOfTeams = 0;
			for (Team t : teams) {
				if (t != null) {
					nOfTeams++;
				}
			}
			TEAMS = new Team[nOfTeams];
			nOfTeams = 0;
			for (int i = 0; i < teams.length; i++) {
				if (teams[i] != null) {
					TEAMS[nOfTeams++] = teams[i];
				}
			}
		} else {
			TEAMS = new Team[0];
		}
		NAME = name;
		MAX_PLAYERS = maxPlayers != null ? Math.max(maxPlayers, 1) : null;
		this.wallColor = wallColor;
		this.floorColor = floorColor;
		this.backgroundColor = backgroundColor;
		GRAVITY = -Math.abs(gravity);
		STORE_LOCATOR = storeLocator;
		DEFAULT_SIGHT_RANGE = Math.abs(defaultSightRange);
		SPAWN_LOCATOR = spawnLocator;
		DEFAULT_PLAYER_CHARACTERISTICS = defaultPlayerCharacteristics != null ? defaultPlayerCharacteristics : new DefaultPlayerCharacteristics();
		BOMB_SITE_LOCATOR = bombSiteLocator;
		INITIAL_DROPPED_WEAPONS = initialDroppedWeapons;
		INITIAL_SPECTATOR_VIEW_LOCATION = initialSpectatorViewLocation;
		EDGE_WALL_COLOR = edgeWallColor;
		EDGE_WALL_HEIGHT = edgeWallHeight;
	}
	
	/**
	 * Gets the width of the map in blocks.
	 * @return the width of the map
	 */
	public int getWidth() {
		return GRID[0].length;
	}
	
	/**
	 * Gets the height (or length) of the map in blocks
	 * @return the height of the map
	 */
	public int getHeight() {
		return GRID.length;
	}
	
	/**
	 * Checks if point {@code (x, y)} is within the map in blocks.
	 * @param x the x-coordinate of the location in blocks
	 * @param y the y-coordinate of the location in blocks
	 * @return whether if the point is within the map
	 */
	public boolean inGrid(double x, double y) {
		return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
	}
	
	public boolean inGrid(double x, double y, double radius) {
		return x >= radius && x < getWidth() - radius && y >= radius && y < getHeight() - radius;
	}
	
	public String getName() {
		return NAME;
	}
	
	/**
	 * Gets the wall at location {@code (x, y)} in blocks.
	 * @param x the x-coordinate of the location in blocks
	 * @param y the y-coordinate of the location in blocks
	 * @return the wall at {@code (x, y)}
	 */
	public Wall getWall(double x, double y) {
		return GRID[(int) y][(int) x];
	}
	
	/**
	 * Sets the wall at a specified location.
	 * @param wall the wall to be placed
	 */
	public void setWall(Wall wall) {
		for (int x = (int) wall.getLocationX(); x < (int) wall.getLocationX() + wall.getBaseWidth(); x++) {
			for (int y = (int) wall.getLocationY(); y < (int) wall.getLocationY() + wall.getBaseHeight(); y++) {
				if (getWall(x, y) != null && !wall.equals(getWall(x, y))) {
					if (wall.getTopHeight(0) > getWall(x, y).getBottomHeight(0) && getWall(x, y).getTopHeight(Integer.MAX_VALUE) > wall.getTopHeight(0)) {
						getWall(x, y).setTemporaryBottomHeight(wall.getTopHeight(0));
					} else if (getWall(x, y).getTopHeight(0) > wall.getBottomHeight(0) && wall.getTopHeight(Integer.MAX_VALUE) > getWall(x, y).getTopHeight(0)) {
						wall.setTemporaryBottomHeight(getWall(x, y).getTopHeight(0));
					} else {
						getWall(x, y).setTemporaryBottomHeight(null);
						wall.setTemporaryBottomHeight(null);
					}
				}
				GRID[y][x] = wall;
			}
		}
	}
	
	public Integer getMaxPlayers() {
		return MAX_PLAYERS;
	}
	
	public void addFence(Fence fence) {
		if (fence != null) {
			synchronized (FENCES) {
				FENCES.add(fence);
			}
		}
	}
	
	public Fence[] getFences() {
		return FENCES.toArray(new Fence[FENCES.size()]);
	}
	
	public void addCeiling(Ceiling ceiling) {
		if (ceiling != null) {
			synchronized (CEILINGS) {
				CEILINGS.add(ceiling);
			}
		}
	}
	
	public Ceiling[] getCeilings() {
		return CEILINGS.toArray(new Ceiling[CEILINGS.size()]);
	}
	
	public void addDoor(Door door) {
		if (door != null) {
			synchronized (DOORS) {
				DOORS.add(door);
			}
			addFence(door);
		}
	}
	
	public Door[] getDoors() {
		return DOORS.toArray(new Door[DOORS.size()]);
	}
	
	public void updateDoors() {
		for (Door d : DOORS) {
			d.updateDoorPosition();
		}
	}
	
	public void activateNearbyDoors(Player player) {
		Ray2D ray = new Ray2D(player.getLocationX(), player.getLocationY(), player.getHorizontalDirection());
		double bottomHeight = player.getBottomHeight();
		double topHeight = player.getTopHeight();
		Door doorActivated = null;
		double closestDistance = player.getReachLength();
		for (Door d : DOORS) {
			if (d.canActivateDoor(player)) {
				Point2D intersection = d.getIntersectionPoint(ray);
				if (intersection != null) {
					double distance = findDistance(player, intersection.getX(), intersection.getY());
					if (distance < closestDistance && d.getBottomHeightAt(intersection.getX(), intersection.getY()) <= topHeight && d.getTopHeight(intersection, Integer.MAX_VALUE) >= bottomHeight) {
						doorActivated = d;
						closestDistance = distance;
					}
				}
			}
		}
		if (doorActivated != null) {
			doorActivated.setOpened(!doorActivated.isOpened());
		}
	}
	
	public void addLadder(Ladder ladder) {
		if (ladder != null) {
			synchronized (LADDERS) {
				LADDERS.add(ladder);
			}
		}
	}
	
	public Ladder[] getLadders() {
		return LADDERS.toArray(new Ladder[LADDERS.size()]);
	}
	
	public boolean isNearLadder(Player player) {
		for (Ladder l : LADDERS) {
			if (Map.findDistance2D(player, l) <= l.getRadius() + player.getPhysicalHalfWidth() && player.getBottomHeight() <= l.getTopHeight() && player.getTopHeight() >= l.getBottomHeight()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the default wall color of the map.
	 * @return the default wall color
	 */
	public Color getWallColor() {
		return this.wallColor;
	}

	/**
	 * Sets the default background color of the map.
	 * @param color the new default background color
	 */
	public void setWallColor(Color color) {
		if (color == null) throw new IllegalArgumentException("Color cannot be null");
		this.wallColor = color;
	}
	
	/**
	 * Gets the default floor color of the map.
	 * @return the default floor color
	 */
	public Color getFloorColor() {
		return this.floorColor;
	}

	/**
	 * Sets the default floor color of the map.
	 * @param color the new default floor color
	 */
	public void setFloorColor(Color color) {
		if (color == null) throw new IllegalArgumentException("Color cannot be null");
		this.floorColor = color;
	}

	/**
	 * Gets the default background color of the map.
	 * @return the default background color
	 */
	public Color getBackgroundColor() {
		return this.backgroundColor;
	}

	/**
	 * Sets the default background color of the map.
	 * @param color the new default background color
	 */
	public void setBackgroundColor(Color color) {
		if (color == null) throw new IllegalArgumentException("Color cannot be null");
		this.backgroundColor = color;
	}
	
	/**
	 * Gets the gravitational acceleration of the map in [m s^-2].<br>
	 * Note: This is usually a negative value.
	 * @return the gravitational acceleration of the map
	 */
	public double getGravity() {
		return GRAVITY;
	}
	
	/**
	 * Checks if a specified location is a valid store location. A valid store location allows a player to open the store.
	 * @param x the x-coordinate of the specified location in blocks
	 * @param y the y-coordinate of the specified location in blocks
	 * @param z the z-coordinate of the specified location in blocks
	 * @param player the player to enter the store
	 * @return true if the specified location is a valid store location; otherwise, false
	 * @see StoreLocator#isStoreLocation(Map, double, double, double, Player)
	 */
	public boolean isStoreLocation(double x, double y, double z, Player player) {
		return STORE_LOCATOR != null ? STORE_LOCATOR.isStoreLocation(this, x, y, z, player) : false;
	}
	
	public void openStore(Player player, ProjectionPlane.DialogDisposedAction<StoreItem[]> action, boolean isAutobuy) {
		STORE_LOCATOR.openStore(player, action, isAutobuy);
	}
	
	/**
	 * Gets the default sight range for the map in meters.
	 * @return the default sight range
	 */
	public double getDefaultSightRange() {
		return DEFAULT_SIGHT_RANGE;
	}
	
	/**
	 * Finds a location for a given player to spawn. The location should not conflict with the current state of the game. For example, an existing player should
	 * not be at the location
	 * @param player the player to be spawned
	 * @return the spawning location of the player
	 */
	public Point3D getSpawnLocation(Player player) {
		return SPAWN_LOCATOR.getSpawnLocation(player);
	}

	/**
	 * Finds a location for a given bot to spawn. The location should not conflict with the current state of the game. For example, an existing player should
	 * not be at the location
	 * @param player the bot to be spawned
	 * @return the spawning location of the bot
	 */
	public Point3D getBotSpawnLocation(Player player) {
		return SPAWN_LOCATOR.getBotSpawnLocation(player);
	}
	
	public boolean isBombMap() {
		return BOMB_SITE_LOCATOR != null;
	}
	
	/**
	 * Checks if a point in 3D-space in the map is a location where a bomb can be planted.
	 * @param x the x-coordinate of the point
	 * @param y the y-coordinate of the point
	 * @param z the z-coordinate of the point
	 * @return {@code true} if a bomb can be planted at the specified location; otherwise {@code false}
	 */
	public boolean isBombSite(double x, double y, double z) {
		return BOMB_SITE_LOCATOR != null && BOMB_SITE_LOCATOR.isBombSite(x, y, z);
	}

	/**
	 * Checks if a point in 3D-space in the map is a location where a bomb can be planted.
	 * @param location the location of the point
	 * @return {@code true} if a bomb can be planted at the specified location; otherwise {@code false}
	 */
	public boolean isBombSite(MapLocation3D location) {
		return BOMB_SITE_LOCATOR != null && BOMB_SITE_LOCATOR.isBombSite(location.getLocationX(), location.getLocationY(), location.getLocationZ());
	}
	
	public DefaultPlayerCharacteristics getDefaultPlayerCharacteristics() {
		return DEFAULT_PLAYER_CHARACTERISTICS;
	}
	
	InitialDroppedWeapon[] getInitialDroppedWeapons() {
		return INITIAL_DROPPED_WEAPONS;
	}

	public InitialSpectatorViewLocation getInitialSpecatatorViewLocation() {
		return INITIAL_SPECTATOR_VIEW_LOCATION;
	}
	
	public Team getTeam(int index) {
		return index >= 0 && index < TEAMS.length ? TEAMS[index] : null;
	}
	
	public Color getEdgeWallColor() {
		return EDGE_WALL_COLOR;
	}
	
	public double getEdgeWallHeight() {
		return EDGE_WALL_HEIGHT;
	}
	
	public double getDistanceToEdge(Ray2D ray) {
		Integer edgeY = null;
		if (ray.getDirection() < Math.PI) {
			edgeY = 0;
		} else if (ray.getDirection() > Math.PI) {
			edgeY = getHeight();
		}
		Integer edgeX = null;
		if (ray.getDirection() < Math.PI / 2 || ray.getDirection() > Math.PI * 3 / 2) {
			edgeX = getWidth();
		} else if (ray.getDirection() > Math.PI / 2 && ray.getDirection() < Math.PI * 3 / 2) {
			edgeX = 0;
		}
		if (edgeX != null && edgeY != null) {
			return Math.min((edgeX - ray.getLocationX()) / Math.cos(ray.getDirection()), (ray.getLocationY() - edgeY) / Math.sin(ray.getDirection()));
		} else if (edgeX != null) {
			return Math.abs(edgeX - ray.getLocationX());
		} else if (edgeY != null) {
			return Math.abs(edgeY - ray.getLocationY());
		} else {
			return 0;
		}
	}
	
	public void edgeHitByBullet(Bullet b) {
		double distance = getDistanceToEdge(b.getTravelPath2D());
		double height = b.getHeightAt(distance);
		if (height >= 0 && height <= getEdgeWallHeight()) {
			synchronized (BULLET_MARKINGS) {
				BULLET_MARKINGS.add(new Bullet.BulletMarking(b.getTravelPath2D().getLocationXAtDistance(distance), b.getTravelPath2D().getLocationYAtDistance(distance), height, b.getRadius()));
			}
		}
	}
	
	public Bullet.BulletMarking[] getBulletMarkings(Ray2D ray, double distance) {
		return Bullet.BulletMarking.getBulletMarkings(BULLET_MARKINGS, ray, distance);
	}
	
	public void showTeamSelectionInternalDialog(boolean allowZombieTeam, final ProjectionPlane.DialogDisposedAction<Team> ACTION) {
		this.tempTeam = null;
		if (getNumberOfTeams() - (!allowZombieTeam && hasTeam(Team.DefaultTeams.ZOMBIES.getTeam()) ? 1 : 0) <= 0) {
			return;
		}
		final ProjectionPlane.ModalInternalFrame DIALOG = ProjectionPlane.getSingleton().addInternalFrame("Select Team", true, true, true, true, true, true, true, new ProjectionPlane.DialogDisposedSimpleAction() {
			@Override
			public void dialogDisposed() {
				ACTION.dialogDisposed(Map.this.tempTeam);
			}
		});
		DIALOG.getContentPane().setLayout(new BorderLayout());
		DIALOG.getContentPane().add(new JLabel("Select a team"), BorderLayout.NORTH);
		JPanel mainPanel = new JPanel(new GridLayout(0, 1));
		for (Team t : getTeams()) {
			if (t != null) {
				final Team T = t;
				JButton button = new JButton(t.getName());
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						synchronized (Map.this) {
							Map.this.tempTeam = T;
						}
						DIALOG.dispose();
					}
				});
				button.setEnabled(!Team.DefaultTeams.ZOMBIES.getTeam().equals(T) || allowZombieTeam);
				mainPanel.add(button);
			}
		}
		JButton button = new JButton(Team.SPECTATOR_NAME);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (Map.this) {
					Map.this.tempTeam = null;
				}
				DIALOG.dispose();
			}
		});
		mainPanel.add(button);
		DIALOG.add(mainPanel, BorderLayout.CENTER);
		DIALOG.pack();
	}
	
	/**
	 * Checks if a team exists for the map. A {@code null} team returns {@code true};
	 * @param team the team to be checked
	 * @return {@code true} if the team exists; otherwise, {@code false}
	 */
	public boolean hasTeam(Team team) {
		return team == null || Arrays.asList(TEAMS).contains(team);
	}
	
	public Team[] getTeams() {
		Team[] teams = new Team[TEAMS.length];
		for (int i = 0; i < teams.length; i++) {
			teams[i] = TEAMS[i];
		}
		return teams;
	}
	
	public int getNumberOfTeams() {
		return TEAMS.length;
	}
	
	public Point3D getRandomLocation() {
		return new Point3D(Math.random() * getWidth(), Math.random() * getHeight(), Double.MAX_VALUE);
	}
	
	/**
	 * Searches for the highest height of the walls in the map. This method searches through all the walls in the map and returns the highest height of the walls.
	 * @return the highest top height of the walls in blocks
	 * @see Wall#getTopHeight(int)
	 */
	public double getMaxHeight() {
		double max = 0;
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				max = Math.max(max, getWall((double) x, (double) y).getTopHeight(Integer.MAX_VALUE));
			}
		}
		return max;
	}
	
	/**
	 * Returns all walls that are located within a specified circle on the map. A cell that intersects (touches) the circle is returned in the array.
	 * @param x the x-coordinate of the center of the circle in blocks
	 * @param y the y-coordinate of the center of the circle in blocks
	 * @param range the radius of the circle
	 * @return all walls that are located within the specified circle
	 */
	public LinkedList<Wall> getWallsInRange(double x, double y, double range) {
		LinkedList<Wall> walls = new LinkedList<Wall>();
		int xBounds = Math.min((int) Math.floor(x + range), getWidth());
		int yBounds = Math.min((int) Math.floor(y + range), getHeight());
		for (int y2 = Math.max((int) Math.floor(y - range), 0); y2 < getHeight() &&  y2 < yBounds; y2++) {
			for (int x2 = Math.max((int) Math.floor(x - range), 0); x2 < getWidth() && x2 < xBounds; x2++) {
				if (getWall(x2, y2) != null && getWall(x2, y2).getTopHeight(Integer.MAX_VALUE) > 0) {
					walls.add(getWall(x2, y2));
				}
			}
		}
		return walls;
	}
	
	/**
	 * Returns all {@code MapItem3D} that are located <b>and is not a {@code Wall}</b> within a specified circle on the map.
	 * @param x the x-coordinate of the center of the circle in blocks
	 * @param y the y-coordinate of the center of the circle in blocks
	 * @param range the radius of the circle
	 * @return all {@code MapItem3D} that are located within the specified circle
	 * @see #getWallsInRange(double, double, double)
	 */
	public LinkedList<? extends MapItem3D> get3DItemsInRange(double x, double y, double range) {
		return null;
	}
	
	/**
	 * Searches for the highest top height among the walls that intersect (touches) a specified area.
	 * @param area the area to check for
	 * @return the highest top height in blocks of the walls intersecting the area
	 * @see Wall#getTopHeight(int)
	 */
	double getHighestWallInArea(Area area) {
		double highest = 0;
		Rectangle2D bounds = area.getBounds2D();
		for (double x = Math.max(Math.floor(bounds.getX()), 0); x <= Math.min(Math.floor(bounds.getX() + bounds.getWidth()), getWidth() - 1); x++) {
			for (double y = Math.max(Math.floor(bounds.getY()), 0); y <= Math.min(Math.floor(bounds.getY() + bounds.getHeight()), getHeight() - 1); y++) {
				if (area.intersects(getWall(x, y).getBaseShape()) && getWall(x, y).getTopHeight(Integer.MAX_VALUE) > highest) {
					highest = getWall(x, y).getTopHeight(Integer.MAX_VALUE);
				}
			}
		}
		return highest;
	}
	
	/**
	 * Searches for the lowest height among the walls that intersect (touches) a specified area and fits a cylinder that is above a minimum height. If no height
	 * exists above the minimum height, the highest possible height is returned.
	 * @param area the area to check for
	 * @param minimumFloorHeight the minimum height
	 * @param cylinderHeight the height of the cylinder
	 * @return the highest top height in blocks of the walls intersecting the area
	 * @see Wall#getTopHeight(int)
	 * @see #getHighestWallInArea(Area)
	 */
	double getHighestWallInArea(Area area, double minimumFloorHeight, double cylinderHeight) {
		Double highest = null;
		Rectangle2D bounds = area.getBounds2D();
		for (double x = Math.max(Math.floor(bounds.getX()), 0); x <= Math.min(Math.floor(bounds.getX() + bounds.getWidth()), getWidth() - 1); x++) {
			for (double y = Math.max(Math.floor(bounds.getY()), 0); y <= Math.min(Math.floor(bounds.getY() + bounds.getHeight()), getHeight() - 1); y++) {
				if (area.intersects(getWall(x, y).getBaseShape())) {
					for (int i = 0; i < getWall(x, y).getNumberOfGaps(); i++) {
						if (getWall(x, y).getTopHeight(i) >= minimumFloorHeight && (highest == null || getWall(x, y).getTopHeight(i) >= highest) && doesCylinderIntersectWall(area, getWall(x, y).getTopHeight(i), cylinderHeight) == null) {
							highest = getWall(x, y).getTopHeight(i);
							break;
						}
					}
				}
			}
		}
		if (highest == null) {
			highest = getHighestWallInArea(area);
		}
		return highest;
	}
	
	double getHighestCeilingInArea(Area area) {
		double highest = 0;
		for (Ceiling c : getCeilings()) {
			if (c.isInteractable() && area.intersects(c.getBaseShape())) {
				highest = Math.max(highest, c.getLocationZ());
			}
		}
		return highest;
	}
	
	double getHighestCeilingInArea(Area area, double minimumFloorHeight, double cylinderHeight) {
		Double highest = null;
		for (Ceiling c : getCeilings()) {
			if (c.isInteractable() && area.intersects(c.getBaseShape())) {
				if (c.getLocationZ() >= minimumFloorHeight && (highest == null || c.getLocationZ() >= highest) && doesCylinderIntersectCeiling(area, c.getLocationZ(), cylinderHeight) == null) {
					highest = c.getLocationZ();
					break;
				}
			}
		}
		if (highest == null) {
			highest = getHighestCeilingInArea(area);
		}
		return highest;
	}
	
	double getHighestFenceInCircle(double x, double y, double radius) {
		Area area = new Area(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
		double highest = 0;
		for (Fence f : getFences()) {
			if (f.getClosestDistanceToPoint(x, y) <= radius) {
				highest = Math.max(highest, f.getHighestHeightInArea(area));
			}
		}
		return highest;
	}
	
	double getHighestFenceInCircle(double x, double y, double radius, double minimumFloorHeight, double cylinderHeight) {
		Area area = new Area(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
		ArrayList<Fence> baseIntersectingFences = new ArrayList<Fence>();
		ArrayList<Double> fenceLowestHeights = new ArrayList<Double>();
		for (Fence f : getFences()) {
			if (f.getClosestDistanceToPoint(x, y) <= radius) {
				baseIntersectingFences.add(f);
				fenceLowestHeights.add(f.getLowestHeightInArea(area));
			}
		}
		Collections.sort(fenceLowestHeights);
		Double highest = null;
		for (Fence f : baseIntersectingFences) {
			double topHeight = f.getHighestHeightInArea(area);
			if (topHeight >= minimumFloorHeight && (highest == null || topHeight >= highest)) {
				for (Double d : fenceLowestHeights) {
					if (d > topHeight && d >= topHeight + cylinderHeight) {
						highest = topHeight;
						break;
					}
				}
			}
		}
		if (highest == null) {
			highest = getHighestFenceInCircle(x, y, radius);
		}
		return highest;
	}
	
	boolean doesCylinderPathIntersectFence(double oldX, double oldY, double newX, double newY, double radius, double bottomHeight, double topHeight, double stepHeight) {
		double angle = Compass.getPrincipleAngle(oldX, newY, newX, oldY);
		Rectangle2D movementRect = new Rectangle2D.Double(oldX, oldY - radius, Map.findDistance(oldX,  oldY, newX, newY), radius * 2);
		AffineTransform transform = new AffineTransform();
		transform.rotate(angle, oldX, oldY);
		Area area = new Area(transform.createTransformedShape(movementRect));
		area.add(new Area(new Ellipse2D.Double(newX - radius, newY - radius, radius * 2, radius * 2)));
		Line2D leftLine = new Line2D.Double(oldX + Math.cos(angle + Math.PI / 2) * radius, oldY - Math.sin(angle + Math.PI / 2) * radius, newX + Math.cos(angle + Math.PI / 2) * radius, newY - Math.sin(angle + Math.PI / 2) * radius);
		Line2D centerLine = new Line2D.Double(oldX, oldY, newX, newY);
		Line2D rightLine = new Line2D.Double(oldX + Math.cos(angle - Math.PI / 2) * radius, oldY - Math.sin(angle - Math.PI / 2) * radius, newX + Math.cos(angle - Math.PI / 2) * radius, newY - Math.sin(angle - Math.PI / 2) * radius);
		for (Fence f : getFences()) {
			if (f.getClosestDistanceToPoint(newX, newY) <= radius || f.intersects(leftLine) || f.intersects(rightLine) || f.getClosestDistanceToPoint(oldX, oldY) <= radius || f.intersects(centerLine)) {
				if (topHeight >= f.getLowestHeightInArea(area) && bottomHeight + stepHeight <= f.getHighestHeightInArea(area)) {
					return true;
				}
			}
		}
		return false;
	}
	
	Wall doesCylinderIntersectWall(Area area, double bottomHeight, double height) {
		return doesCylinderIntersectWall(area, bottomHeight, height, 0);
	}
	
	Wall doesCylinderIntersectWall(Area area, double bottomHeight, double height, double stepHeight) {
		height = Math.abs(height);
		Rectangle2D bounds = area.getBounds2D();
		for (double x = Math.max(Math.floor(bounds.getX()), 0); x <= Math.min(Math.floor(bounds.getX() + bounds.getWidth()), getWidth() - 1); x++) {
			for (double y = Math.max(Math.floor(bounds.getY()), 0); y <= Math.min(Math.floor(bounds.getY() + bounds.getHeight()), getHeight() - 1); y++) {
				for (int i = 0; i < getWall(x, y).getNumberOfGaps(); i++) {
					if (area.intersects(getWall(x, y).getBaseShape()) && getWall(x, y).getTopHeight(i) > bottomHeight + stepHeight) {
						if (getWall(x, y).getBottomHeight(i) < bottomHeight + height) {
							return getWall(x, y);
						}
					}
				}
			}
		}
		return null;
	}
	
	Ceiling doesCylinderIntersectCeiling(Area area, double bottomHeight, double height) {
		return doesCylinderIntersectCeiling(area, bottomHeight, height, 0);
	}
	
	Ceiling doesCylinderIntersectCeiling(Area area, double bottomHeight, double height, double stepHeight) {
		height = Math.abs(height);
		for (Ceiling c : getCeilings()) {
			if (c.isInteractable() && area.intersects(c.getBaseShape()) && c.getLocationZ() > bottomHeight + stepHeight && c.getLocationZ() < bottomHeight + height) {
				return c;
			}
		}
		return null;
	}
	
	Fence doesCylinderIntersectFence(double x, double y, double radius, double bottomHeight, double height) {
		return doesCylinderIntersectFence(x, y, radius, bottomHeight, height, 0);
	}
	
	Fence doesCylinderIntersectFence(double x, double y, double radius, double bottomHeight, double height, double stepHeight) {
		height = Math.abs(height);
		Area area = new Area(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
		for (Fence f : getFences()) {
			if (f.getClosestDistanceToPoint(x, y) <= radius && f.getHighestHeightInArea(area) > bottomHeight + stepHeight) {
				if (f.getLowestHeightInArea(area) < bottomHeight + height) {
					return f;
				}
				
			}
		}
		return null;
	}
	
	double getHighestOccupiableCylinderHeight(Area area, double locationX, double locationY, double radius, double maxHeight) {
		double highestHeight = 0;
		Rectangle2D bounds = area.getBounds2D();
		for (int x = Math.max((int) Math.floor(bounds.getX()), 0); x <= Math.min((int) Math.floor(bounds.getX() + bounds.getWidth()), getWidth() - 1); x++) {
			for (int y = Math.max((int) Math.floor(bounds.getY()), 0); y <= Math.min((int) Math.floor(bounds.getY() + bounds.getHeight()), getHeight() - 1); y++) {
				if (area.intersects(getWall(x, y).getBaseShape())) {
					for (int j = getWall(x, y).getNumberOfGaps() - 1; j >= 0; j--) {
						if (getWall(x, y).getTopHeight(j) <= maxHeight) {
							highestHeight = Math.max(highestHeight, getWall(x, y).getTopHeight(j));
							break;
						}
					}
				}
			}
		}
		Ceiling highestCeiling = getHighestCeilingUnder(area, maxHeight);
		if (highestCeiling != null) {
			highestHeight = Math.max(highestHeight, highestCeiling.getLocationZ());
		}
		for (Fence f : getFences()) {
			if (f.getClosestDistanceToPoint(locationX, locationY) <= radius) {
				double height = f.getHighestHeightInArea(area);
				if (height <= maxHeight) {
					highestHeight = Math.max(highestHeight, height);
				}
			}
		}
		return highestHeight;
	}
	
	Ceiling getHighestCeilingUnder(Area area, double maxHeight) {
		Ceiling ceiling = null;
		double highestHeight = 0;
		for (Ceiling c : getCeilings()) {
			if (c.isInteractable() && area.intersects(c.getBaseShape())) {
				if (c.getLocationZ() <= maxHeight && c.getLocationZ() > highestHeight) {
					highestHeight = c.getLocationZ();
					ceiling = c;
				}
			}
		}
		return ceiling;
	}
	
	Ceiling getLowestCeilingAbove(Area area, double minHeight) {
		Ceiling ceiling = null;
		double lowestHeight = Double.MAX_VALUE;
		for (Ceiling c : getCeilings()) {
			if (c.isInteractable() && area.intersects(c.getBaseShape())) {
				if (c.getLocationZ() >= minHeight && c.getLocationZ() < lowestHeight) {
					lowestHeight = c.getLocationZ();
					ceiling = c;
				}
			}
		}
		return ceiling;
	}
	
	public void clearAllBulletMarkings() {
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				getWall(x, y).clearAllBulletMarkings();
			}
		}
		for (Fence f : getFences()) {
			f.clearAllBulletMarkings();
		}
		synchronized (BULLET_MARKINGS) {
			BULLET_MARKINGS.clear();
		}
	}
	
	public void setBackgroundImage(BufferedImage image) {
		synchronized (this) {
			this.backgroundImage = image;
		}
	}
	
	public BufferedImage getBackgroundImage() {
		return this.backgroundImage;
	}
	
	/**
	 * Finds the distance between the origin and a point in the x and y plane using the Pythagorean theorem.
	 * @param p the location of the point
	 * @return the distance between the origin and the point
	 */
	public static double findDistance2D(MapLocation2D p) {
		return findDistance(0, 0, p.getLocationX(), p.getLocationY());
	}
	
	/**
	 * Finds the distance between the origin and a point in the x and y plane using the Pythagorean theorem.
	 * @param x the x-coordinate of the point
	 * @param y the y-coordinate of the point
	 * @return the distance between the origin and the point
	 */
	public static double findDistance(double x, double y) {
		return findDistance(0, 0, x, y);
	}
	
	/**
	 * Finds the distance between the origin and a point in 3D space using the Pythagorean theorem.
	 * @param p the location of the point
	 * @return the distance between the origin and the point
	 */
	public static double findDistance3D(MapLocation3D p) {
		return findDistance(0, 0, 0, p.getLocationX(), p.getLocationY(), p.getLocationZ());
	}
	
	/**
	 * Finds the distance between the origin and a point in 3D space using the Pythagorean theorem.
	 * @param x the x-coordinate of the point
	 * @param y the y-coordinate of the point
	 * @param z the z-coordinate of the point
	 * @return the distance between the origin and the point
	 */
	public static double findDistance(double x, double y, double z) {
		return findDistance(0, 0, 0, x, y, z);
	}
	
	/**
	 * Finds the distance between two points using the Pythagorean theorem.
	 * @param x1 the x-coordinate of the first point
	 * @param y1 the y-coordinate of the first point
	 * @param x2 the x-coordinate of the second point
	 * @param y2 the y-coordinate of the second point
	 * @return the distance between the first and second point
	 */
	public static double findDistance(double x1, double y1, double x2, double y2) {
		return findDistance(x1, y1, 0, x2, y2, 0);
	}
	
	/**
	 * Finds the distance between two points using the Pythagorean theorem.
	 * @param x1 the x-coordinate of the first point
	 * @param y1 the y-coordinate of the first point
	 * @param z1 the z-coordinate of the first point
	 * @param x2 the x-coordinate of the second point
	 * @param y2 the y-coordinate of the second point
	 * @param z2 the z-coordinate of the second point
	 * @return the distance between the first and second point
	 */
	public static double findDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
	}

	/**
	 * Finds the distance between two points in the x and y plane using the Pythagorean theorem.
	 * @param p the location of the first point
	 * @param x the x-coordinate of the second point
	 * @param y the y-coordinate of the second point
	 * @return the distance between the first and second point
	 */
	public static double findDistance(MapLocation2D p, double x, double y) {
		return findDistance(p.getLocationX(), p.getLocationY(), x, y);
	}

	/**
	 * Finds the distance between two points in the x and y plane using the Pythagorean theorem.
	 * @param p1 the location of the first point
	 * @param p2 the location of the second point
	 * @return the distance between the first and second point
	 */
	public static double findDistance2D(MapLocation2D p1, MapLocation2D p2) {
		return findDistance(p1.getLocationX(), p1.getLocationY(), p2.getLocationX(), p2.getLocationY());
	}

	/**
	 * Finds the distance between two points in the 3D space using the Pythagorean theorem.
	 * @param p the location of the first point
	 * @param x the x-coordinate of the second point
	 * @param y the y-coordinate of the second point
	 * @param z the z-coordinate of the second point
	 * @return the distance between the first and second point
	 */
	public static double findDistance3D(MapLocation3D p, double x, double y, double z) {
		return findDistance(p.getLocationX(), p.getLocationY(), p.getLocationZ(), x, y, z);
	}

	/**
	 * Finds the distance between two points in the 3D space using the Pythagorean theorem.
	 * @param p1 the location of the first point
	 * @param p2 the location of the second point
	 * @return the distance between the first and second point
	 */
	public static double findDistance3D(MapLocation3D p1, MapLocation3D p2) {
		return findDistance(p1.getLocationX(), p1.getLocationY(), p1.getLocationZ(), p2.getLocationX(), p2.getLocationY(), p2.getLocationZ());
	}
	
	public void drawRadar(Graphics g, int centerX, int centerY, int radius, Color backColor, long scanPeriod, double scanDistance, Player player, MapItem[] items) {
		Color transparentColor = new Color(backColor.getRed(), backColor.getGreen(), backColor.getBlue(), backColor.getAlpha() / 2);
		Color lessTransparentColor = new Color(backColor.getRed(), backColor.getGreen(), backColor.getBlue(), backColor.getAlpha() * 3 / 4);
		Color lessLessTransparentColor = new Color(backColor.getRed(), backColor.getGreen(), backColor.getBlue(), backColor.getAlpha() * 9 / 10);
		g.setColor(transparentColor);
		g.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, (int) (90 + Math.toDegrees(player.getHorizontalFOV() / 2)), (int) (360 - Math.toDegrees(player.getHorizontalFOV())));
		
		g.setColor(lessTransparentColor);
		g.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, (int) (90 - Math.toDegrees(player.getHorizontalFOV() / 2)), (int) Math.toDegrees(player.getHorizontalFOV()));

		g.setColor(lessLessTransparentColor);
		int directionLines = 4;
		for (int i = 0; i < directionLines; i++) {
			g.drawLine(centerX, centerY, centerX + (int) (radius * Math.cos(Math.PI * 2 * i / directionLines)), centerY + (int) (radius * Math.sin(Math.PI * 2 * i / directionLines)));
		}
		g.setColor(backColor);
		g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
		
		double scanAngle = (Math.PI * 2 - System.currentTimeMillis() % scanPeriod * Math.PI * 2 / scanPeriod);
		g.drawLine(centerX, centerY, centerX + (int) (radius * Math.cos(scanAngle)), centerY - (int) (radius * Math.sin(scanAngle)));

		for (MapItem i : items) {
			if (!player.equals(i) && (CAN_RADAR_VIEW_ALL_PLAYERS.get() || i.canDetectOnRadar(player))) {
				Color color = i.getRadarColor(player);
				if (color != null) {
					double relativeDistance = findDistance2D(player, i);
					if (relativeDistance <= scanDistance) {
						relativeDistance = relativeDistance * radius / scanDistance;
						double relativeAngle = Compass.getPrincipleAngle(player.getLocationX(), i.getLocationY(), i.getLocationX(), player.getLocationY()) - player.getHorizontalDirection() + Math.PI / 2;
						int size = 3;
						g.setColor(color);
						g.fillOval(centerX + (int) (relativeDistance * Math.cos(relativeAngle)) - size / 2, centerY - (int) (relativeDistance * Math.sin(relativeAngle)) - size / 2, size, size);
						if (Math.abs(relativeAngle - scanAngle) < Math.PI / 9) {
							g.setColor(backColor);
							g.drawOval(centerX + (int) (relativeDistance * Math.cos(relativeAngle)) - size / 2 - 1, centerY - (int) (relativeDistance * Math.sin(relativeAngle)) - size / 2 - 1, size + 2, size + 2);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Draws an 2D birds-eye view image representation of the map within a rectangular area of graphics
	 * @param g the graphics to be drawn onto
	 * @param leftX the x-coordinate of the top left corner of the area in which the map will be drawn
	 * @param topY the y-coordinate of the top left corner of the area in which the map will be drawn
	 * @param width the width of the area in which the map will be drawn
	 * @param height the height of the area in which the map will be drawn
	 */
	public void drawMapPreview(Graphics g, int leftX, int topY, int width, int height) {
		double maxHeight = getMaxHeight();
		int wallWidth = width / getWidth();
		int wallHeight = height / getHeight();
		int indentLeft = (width - wallWidth * getWidth()) / 2;
		int indentTop = (height - wallHeight * getHeight()) / 2;
		g.clearRect(leftX, topY, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(leftX, topY, width, height);
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				if (getWall((double) x, (double) y).getTopHeight(Integer.MAX_VALUE) == 0) {
					g.setColor(Color.WHITE);
				} else if (getWall((double) x, (double) y).getTopHeight(Integer.MAX_VALUE) >= maxHeight) {
					g.setColor(Color.BLACK);
				} else {
					g.setColor(new Color(255 - Math.min((int) (getWall((double) x, (double) y).getTopHeight(Integer.MAX_VALUE) * 255 / maxHeight), 255), 255 - Math.min((int) (getWall((double) x, (double) y).getTopHeight(Integer.MAX_VALUE) * 255 / maxHeight), 255), 255 - Math.min((int) (getWall((double) x, (double) y).getTopHeight(Integer.MAX_VALUE) * 255 / maxHeight), 255)));
				}
				g.fillRect(x * wallWidth + indentLeft, y * wallHeight + indentTop, wallWidth, wallHeight);
				g.setColor(Color.BLACK);
				g.drawRect(x * wallWidth + indentLeft, y * wallHeight + indentTop, wallWidth, wallHeight);
			}
		}
	}
	
	/**
	 * Draws a birds-eye view of the layout of the map near a player within a rectangular area of graphics. The radius of the area drawn is the sight range of the player.
	 * @param g the graphics to be drawn onto
	 * @param player the player that the image will be centered around
	 * @param leftX the x-coordinate of the top left corner of the area in which the map will be drawn
	 * @param topY the y-coordinate of the top left corner of the area in which the map will be drawn
	 * @param width the width of the area in which the map will be drawn
	 * @param height the height of the area in which the map will be drawn
	 * @param zoom the zoom multiplier of the drawn area
	 * @param sightRange the range of the map
	 */
	public void drawMap(Graphics g, Player player, int leftX, int topY, int width, int height, double zoom, double sightRange) {
		if (Math.floor(Math.min(width, height) / 2) <= 1 || zoom < 1 || zoom > 5) return;
		
		g.setColor(Color.WHITE);
		g.fillRect(leftX, topY, width, height);
		double playerAngle = player.getHorizontalDirection();
		int circleRadius = (int) Math.floor(Math.min(width, height) / 2 * (1 - 1 / (sightRange / zoom + Math.sqrt(2)))) - 1;
		int blockSize = (int) Math.floor(((Math.min(width, height) / 2) - 1) / (sightRange / zoom + Math.sqrt(2))) - 1;
		int refX = Math.max((int) Math.round(player.getLocationX() - sightRange / zoom), 0);
		int refY = Math.max((int) Math.round(player.getLocationY() - sightRange / zoom), 0);
		int refPixelX = leftX + width / 2 - (int) Math.round(findDistance(player, refX - 0.5, refY - 0.5) * Math.cos(Compass.getPrincipleAngle(refX - 0.5, refY - 0.5, player.getLocationX(), player.getLocationY()) - playerAngle + Math.PI / 2) * (blockSize + 1));
		int refPixelY = topY + height / 2 + (int) Math.round(findDistance(player, refX - 0.5, refY - 0.5) * Math.sin(Compass.getPrincipleAngle(refX - 0.5, refY - 0.5, player.getLocationX(), player.getLocationY()) - playerAngle + Math.PI / 2) * (blockSize + 1)) ;
		for (int x = Math.max((int) Math.round(player.getLocationX() - sightRange / zoom), 0); x <= Math.min((int) Math.round(player.getLocationX() + sightRange / zoom), getWidth()); x++) {
			for (int y = Math.max((int) Math.round(player.getLocationY() - sightRange / zoom), 0); y <= Math.min((int) Math.round(player.getLocationY() + sightRange / zoom), getHeight()); y++) {
				if (Math.round(player.getLocationX()) == x || Math.round(player.getLocationY()) == y || findDistance(player, (player.getLocationX() > x) ? x + 0.5 : x - 0.5, (player.getLocationY() > y) ? y + 0.5 : y - 0.5) <= sightRange / zoom) {
					double objectAngle = Compass.getPrincipleAngle((x - 0.5) - (refX - 0.5), (y - 0.5) - (refY - 0.5)) - playerAngle + Math.PI / 2;
					double objectDist = findDistance(refX - 0.5, refY - 0.5, x - 0.5, y - 0.5);
					int xDist = (int) Math.round(Math.cos(objectAngle) * objectDist * (blockSize + 1));
					int yDist = (int) Math.round(Math.sin(objectAngle) * objectDist * (blockSize + 1));
					
					int[] xPoints = {0, (int) Math.floor(Math.cos(Math.PI / 2 - playerAngle) * blockSize), (int) Math.floor(Math.cos(Math.PI * 3 / 4 - playerAngle) * Math.sqrt(2 * Math.pow(blockSize , 2))), (int) Math.floor(Math.cos(Math.PI - playerAngle) * blockSize)};
					int[] yPoints = {0, (int) Math.ceil(Math.sin(Math.PI / 2 - playerAngle) * blockSize), (int) Math.ceil(Math.sin(Math.PI * 3 / 4 - playerAngle) * Math.sqrt(2 * Math.pow(blockSize, 2))), (int) Math.ceil(Math.sin(Math.PI - playerAngle) *blockSize)};
					for (int i = 0; i < 4; i++) {
						xPoints[i] = refPixelX + xDist + xPoints[i];
						yPoints[i] = refPixelY - yDist - yPoints[i];
					}
					if (inGrid((double) x, (double) y) && getWall((double) x, (double) y).getTopHeight(Integer.MAX_VALUE) > player.getFloorHeight()) {
						g.setColor(Color.RED);
						g.fillPolygon(xPoints, yPoints, 4);
					} else if (inGrid((double) x, (double) y) && getWall((double) x, (double) y).getTopHeight(Integer.MAX_VALUE) < player.getFloorHeight()) {
						g.setColor(Color.BLUE);
						g.fillPolygon(xPoints, yPoints, 4);
					}
				}
			}
		}
		g.setColor(Color.GRAY);
		g.drawLine(leftX + width / 2, topY + height / 2, (int) Math.round(leftX + width / 2 - Math.sin(player.getHorizontalFOV() / 2) * circleRadius), (int) Math.round(topY + height / 2 - Math.cos(player.getHorizontalFOV() / 2) * circleRadius));
		g.drawLine(leftX + width / 2, topY + height / 2, (int) Math.round(leftX + width / 2 + Math.sin(player.getHorizontalFOV() / 2) * circleRadius), (int) Math.round(topY + height / 2 - Math.cos(player.getHorizontalFOV() / 2) * circleRadius));
		g.setColor(Color.GREEN);
		g.fillOval(leftX + width / 2 - 2, topY + height / 2 - 2, 4, 4);
		
		g.setColor(new Color(222, 222, 222));
        Graphics2D g2d = (Graphics2D) g.create();
		Rectangle fill = new Rectangle(width, height);
		Ellipse2D hole = new Ellipse2D.Float(leftX + width / 2 - circleRadius, topY + height / 2 - circleRadius, circleRadius * 2, circleRadius * 2);
        Area area = new Area(fill);
        area.subtract(new Area(hole));
        g2d.fill(area);
        g2d.dispose();
        
        g.setColor(Color.BLACK);
		Font previousFont = g.getFont();
		Font font = new Font("segou ui", Font.PLAIN, 9);
		g.setFont(font);
		String zoomStr = "x" + String.valueOf(Math.round(zoom * 100) / 100d);
		g.drawString(zoomStr, leftX + (int) Math.ceil(width * 0.05), topY + (int) Math.floor(height * 0.95));
		g.setFont(previousFont);
	}
	
	private static Compass.Cardinal findNextRandomCell(int x, int y, boolean[][] visited) {
		boolean[] available = new boolean[4];
		int[] randomnessWeight = new int[4];
		int nOfAvailable = 4;
		for (int i = 0; i < available.length; i++) {
			available[i] = true;
			randomnessWeight[i] = 1;
		}
		if (x <= 0 || visited[x - 1][y]) {
			available[3] = false;
			nOfAvailable--;
		}
		if (y <= 0 || visited[x][y - 1]) {
			available[2] = false;
			nOfAvailable--;
		}
		if (x >= visited.length - 1 || visited[x + 1][y]) {
			available[1] = false;
			nOfAvailable--;
		}
		if (y >= visited[0].length - 1 || visited[x][y + 1]) {
			available[0] = false;
			nOfAvailable--;
		}
		int totalWeight = 0;
		for (int i = 0; i < available.length; i++) {
			if (available[i]) {
				totalWeight += randomnessWeight[i];
			}
		}
		
		if (nOfAvailable > 0) {
			int choice = (int) (Math.random() * totalWeight);
			int count = 0;
			for (int i = 0; i < available.length; i++) {
				if (available[i]) {
					count += randomnessWeight[i];
					if (count > choice) {
						switch (i) {
							case 0: return Compass.Cardinal.NORTH;
							case 1: return Compass.Cardinal.EAST;
							case 2: return Compass.Cardinal.SOUTH;
							case 3: return Compass.Cardinal.WEST;
							default: return null;
						}
					}
				}
			}
		}
		return null;
	}
	
	private static boolean checkAllTrue(boolean[][] a) {
	    for (int x = 0; x < a.length; x++) {
		    for (int y = 0; y < a[x].length; y++) {
		        if (a[x][y] == false) {
		            return false;
		        }
	        }
		}
		return true;
	}
	
	/**
	 * Changes the layout of the map into a maze by modifying the heights of the walls in the map.
	 * @param algorithm the algorithm used to generate the maze
	 * @param mazeHeight the height of the walls in the maze
	 * @param addWallBorder {@code true} to add a wall around the border of the map; otherwise, {@code false}
	 * @param mazeStartX the x-coordinate of the starting location of the map
	 * @param mazeStartY the y-coordinate of the starting location of the map
	 * @param startColor the color of the walls at the start location
	 * @param showOnRadar {@code true} if the maze exit wall should be shown on the radar; otherwise {@code false}
	 * @return {@code true} if a maze was successfully generated and integrated into the map; otherwise, {@code false}
	 */
	public boolean generateMaze(MazeAlgorithms algorithm, double mazeHeight, boolean addWallBorder, int mazeStartX, int mazeStartY, Color startColor, boolean showOnRadar) {
		if (!inGrid(mazeStartX, mazeStartY)) throw new IllegalArgumentException("maze start location must be inside the grid");
		mazeHeight = Math.abs(mazeHeight);
		double[][] newMaze = new double[getHeight()][getWidth()];
		if (addWallBorder) {
			for (int x = 0; x < getWidth(); x++) {
				newMaze[0][x] = mazeHeight;
			}
			for (int y = 0; y < getHeight(); y++) {
				newMaze[y][0] = mazeHeight;
			}
			for (int x = 0; x < getWidth(); x++) {
				for (int y = getHeight() + (getHeight() % 2) - 2; y < getHeight(); y++) {
					newMaze[y][x] = mazeHeight;
				}
			}
			for (int x = getWidth() + (getWidth() % 2) - 2; x < getHeight(); x++) {
				for (int y = 0; y < getHeight(); y++) {
					newMaze[y][x] = mazeHeight;
				}
			}
		}
		
		if (mazeHeight != 0) {
			boolean[][] mazeLayout;
			int count = 0;
			do {
				mazeLayout = algorithm.createMaze((int) ((getWidth() - 1) / 2) + (addWallBorder ? 0 : 1),  (int) ((getHeight() - 1) / 2) + (addWallBorder ? 0 : 1), new Point(mazeStartX, mazeStartY));
				if (++count == 5) {
					return false;
				}
			} while (mazeLayout[mazeStartX][mazeStartY]);
			for (int y = 0; y < getHeight(); y++) {
				for (int x = 0; x < getWidth(); x++) {
					if (x == mazeStartX && y == mazeStartY) {
						setWall(new Wall.Builder(x + (addWallBorder ? 1 : 0), y + (addWallBorder ? 1 : 0)).surfaceHeights(0.001).topColor(startColor).color(startColor).showOnRadar(showOnRadar).build());
					} else if (mazeLayout[y][x]) {
						setWall(new Wall.Builder(x + (addWallBorder ? 1 : 0), y + (addWallBorder ? 1 : 0)).surfaceHeights(mazeHeight).color(DEFAULT_WALL_COLOR).build());
					} else {
						setWall(new Wall.Builder(x + (addWallBorder ? 1 : 0), y + (addWallBorder ? 1 : 0)).surfaceHeights(0).build());
					}
				}
			}
			if (inGrid(mazeStartX, mazeStartY + 1)) {
				getWall(mazeStartX, mazeStartY + 1).setWallSidesColor(Compass.Cardinal.NORTH, startColor);
			}
			if (inGrid(mazeStartX, mazeStartY - 1)) {
				getWall(mazeStartX, mazeStartY - 1).setWallSidesColor(Compass.Cardinal.SOUTH, startColor);
			}
			if (inGrid(mazeStartX - 1, mazeStartY)) {
				getWall(mazeStartX - 1, mazeStartY).setWallSidesColor(Compass.Cardinal.EAST, startColor);
			}
			if (inGrid(mazeStartX + 1, mazeStartY)) {
				getWall(mazeStartX + 1, mazeStartY).setWallSidesColor(Compass.Cardinal.WEST, startColor);
			}
			
			return true;
		}
		return false;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.backgroundImage, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.backgroundImage = ImageIO.read(in);
    }
	
	public static class InitialDroppedWeapon implements MapLocation3D, Serializable {
		private static final long serialVersionUID = 3358524192801003463L;
		private final double X;
		private final double Y;
		private final double Z;
		private final Weapon WEAPON;
		
		public InitialDroppedWeapon(double x, double y, double z, Weapon weapon) {
			X = x;
			Y = y;
			Z = z;
			WEAPON = weapon;
		}

		@Override
		public double getLocationX() {
			return X;
		}

		@Override
		public double getLocationY() {
			return Y;
		}

		@Override
		public double getLocationZ() {
			return Z;
		}
		
		public Weapon getWeapon() {
			return WEAPON;
		}
	}

	public static class Point3D implements MapLocation3D, Serializable {
		private static final long serialVersionUID = 3943497751559955166L;
		
		public double x = 0;
		public double y = 0;
		public double z = 0;
		
		public Point3D(MapLocation3D location) {
			this(location.getLocationX(), location.getLocationY(), location.getLocationZ());
		}
		
		public Point3D(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public double getLocationX() {
			return this.x;
		}

		@Override
		public double getLocationY() {
			return this.y;
		}

		@Override
		public double getLocationZ() {
			return this.z;
		}

		@Override
		public String toString() {
			return "Point3D [x=" + x + ", y=" + y + ", z=" + z + "]";
		}
	}
	
	public interface MapLocation2D {
		/**
		 * Gets the x-coordinate of the location of the item within the map.
		 * @return the x-coordinate of the location in meters
		 */
		public double getLocationX();
		
		/**
		 * Gets the y-coordinate of the location of the item within the map.
		 * @return the y-coordinate of the location in meters
		 */
		public double getLocationY();
	}
	
	public interface MapLocation3D extends MapLocation2D {
		/**
		 * Gets the z-coordinate of the location of the item within the map.
		 * @return the z-coordinate of the location in meters
		 */
		public double getLocationZ();
	}
	
	public static class Ray2D implements MapLocation2D, Serializable {
		private static final long serialVersionUID = 1246200512569023064L;
		
		private double X;
		private double Y;
		private double DIRECTION;
		public Ray2D(double x, double y, double direction) {
			X = x;
			Y = y;
			DIRECTION = (direction % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		}

		@Override
		public double getLocationX() {
			return X;
		}
		
		@Override
		public double getLocationY() {
			return Y;
		}
		
		public double getDirection() {
			return DIRECTION;
		}
		
		public double getLocationXAtDistance(double distance) {
			return getLocationX() + Math.cos(getDirection()) * distance;
		}
		
		public double getLocationYAtDistance(double distance) {
			return getLocationY() - Math.sin(getDirection()) * distance;
		}
		
		public Point2D getLocationAtDistance(double distance) {
			return new Point2D.Double(getLocationXAtDistance(distance), getLocationYAtDistance(distance));
		}
		
		public double ptRayDist(double x, double y) {
			double parallelProjectionDistance = Math.cos(getDirection()) * (x - getLocationX()) - Math.sin(getDirection()) * (y - getLocationY());
			if (parallelProjectionDistance >= 0) {
				return Math.abs(Math.cos(getDirection()) * (y - getLocationY()) + Math.sin(getDirection()) * (x - getLocationX()));
			} else {
				return Map.findDistance(this, x, y);
			}
		}
		
		public Double getIntersectionPointRelativeToLine(Line2D line) {
			double x1 = line.getX1();
			double y1 = line.getY1();
			double deltaX = line.getX2() - x1;
			double deltaY = line.getY2() - y1;
			double x = getLocationX();
			double y = getLocationY();
			double theta = getDirection();
			
			double denominator = deltaY * Math.cos(theta) + deltaX * Math.sin(theta);
			if (denominator != 0) {
				double t = (deltaX * (y - y1) - deltaY * (x - x1)) / denominator;
				if (t >= 0) {
					double s;
					if (deltaX != 0) {
						s = (x - x1 + t * Math.cos(theta)) / deltaX;
					} else {
						s = (y - y1 - t * Math.sin(theta)) / deltaY;
					}
					return s;
				}
			}
			return null;
		}
		
		public Point2D getIntersectionPoint(Line2D line) {
			double x1 = line.getX1();
			double y1 = line.getY1();
			double deltaX = line.getX2() - x1;
			double deltaY = line.getY2() - y1;
			double x = getLocationX();
			double y = getLocationY();
			double theta = getDirection();
			
			double denominator = deltaY * Math.cos(theta) + deltaX * Math.sin(theta);
			if (denominator != 0) {
				double t = (deltaX * (y - y1) - deltaY * (x - x1)) / denominator;
				if (t >= 0) {
					double s;
					if (deltaX != 0) {
						s = (x - x1 + t * Math.cos(theta)) / deltaX;
					} else {
						s = (y - y1 - t * Math.sin(theta)) / deltaY;
					}
					if (0 <= s && s <= 1) {
						return new Point2D.Double(x + t * Math.cos(theta), y - t * Math.sin(theta));
					}
				}
			}
			return null;
		}
		
		public Point2D getIntersectionWithCircle(double centerX, double centerY, double radius) {
			double intersectX = getLocationX();
			double intersectY = centerY;
			if (getDirection() == 0 || getDirection() == Math.PI) {
				intersectX = centerX;
				intersectY = getLocationY();
			} else if (getDirection() != Math.PI / 2 && getDirection() != Math.PI * 3 / 2) {
				double slope = -Math.tan(getDirection());
				intersectX = (centerY - getLocationY() + slope * getLocationX() + centerX / slope) / (slope + 1 / slope);
				intersectY = slope * (intersectX - getLocationX()) + getLocationY();
			}
			return new Point2D.Double(intersectX, intersectY);
		}
		
		public Point2D getIntersectionPoint(Rectangle2D rect) {
			double x = getLocationX();
			double y = getLocationY();
			if (x >= rect.getX() && x <= rect.getX() + rect.getWidth() && y >= rect.getY() && y <= rect.getY() + rect.getHeight()) {
				return new Point2D.Double(x, y);
			}
			double angle = getDirection();
			Integer closestVerticalWall = null;
			if (angle != Math.PI && angle != Math.PI * 3 / 2) {
				if (x > rect.getX() && ((rect.getX() + rect.getWidth() > x) == (angle < Math.PI / 2 || angle > Math.PI * 3 / 2))) {
					closestVerticalWall = (int) (rect.getX() + rect.getWidth());
				} else if (x < rect.getX() + rect.getWidth() && ((rect.getX() > x) == (angle < Math.PI / 2 || angle > Math.PI * 3 / 2))) {
					closestVerticalWall = (int) rect.getX();
				}
			}
			Integer closestHorizontalWall = null;
			if (angle != 0 && angle  != Math.PI) {
				if (y > rect.getY() && ((rect.getY() + rect.getHeight() > y) == (angle > Math.PI))) {
					closestHorizontalWall = (int) (rect.getY() + rect.getHeight());
				} else if (y < rect.getY() + rect.getHeight() && ((rect.getY() > y) == (angle > Math.PI))) {
					closestHorizontalWall = (int) rect.getY();
				}
			}
			if (closestVerticalWall == null && closestHorizontalWall == null) {
				return null;
			} else {
				Point2D verticalPointIntersection = null;
				if (closestVerticalWall != null) {
					double intersectionY = y - (closestVerticalWall - x) * Math.tan(angle);
					if (Math.abs(rect.getY() + rect.getHeight() / 2d - intersectionY) <= rect.getHeight() / 2d) {
						verticalPointIntersection = new Point2D.Double(closestVerticalWall, intersectionY);
					}
				}
				if (x > rect.getX() + rect.getWidth() || x < rect.getX()) {
					if (verticalPointIntersection != null) {
						return verticalPointIntersection;
					} else if (y > rect.getY() && y < rect.getY() + rect.getHeight()) {
						return null;
					}
				}
				Point2D horizontalPointIntersection = null;
				if (closestHorizontalWall != null) {
					double intersectionX = x - (closestHorizontalWall - y) / Math.tan(angle);
					if (Math.abs(rect.getX() + rect.getWidth() / 2d - intersectionX) <= rect.getWidth() / 2d) {
						horizontalPointIntersection = new Point2D.Double(intersectionX, closestHorizontalWall);
					}
				}
				if (verticalPointIntersection != null && horizontalPointIntersection != null && (y > rect.getY() + rect.getHeight() || y < rect.getY())) {
					return horizontalPointIntersection;
				} else if (verticalPointIntersection != null) {
					return verticalPointIntersection;
				} else if (horizontalPointIntersection != null) {
					return horizontalPointIntersection;
				} else {
					return null;
				}
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(DIRECTION);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(X);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(Y);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Ray2D other = (Ray2D) obj;
			if (Double.doubleToLongBits(DIRECTION) != Double.doubleToLongBits(other.DIRECTION))
				return false;
			if (Double.doubleToLongBits(X) != Double.doubleToLongBits(other.X))
				return false;
			if (Double.doubleToLongBits(Y) != Double.doubleToLongBits(other.Y))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Ray2D [X=" + X + ", Y=" + Y + ", DIRECTION=" + DIRECTION + "]";
		}
	}
	
	/**
	 * Represents the algorithms that can be used for generating a random maze.
	 * @author Wentao
	 *
	 */
	public enum MazeAlgorithms {
		 KRUSKAL, DEPTH_FIRST_SEARCH;
		 
		/**
		 * Creates a random 2D layout of a maze. In the returned array, a {@code true} value represents a wall while a {@code false} value represents an empty cell
		 * @param mazeWidth the width of the maze (excluding walls)
		 * @param mazeHeight the height of the maze (excluding walls)
		 * @param mazeStart the starting coordinates of the map
		 * @return an array representing the layout of the maze
		 */
		public boolean[][] createMaze(int mazeWidth, int mazeHeight, Point mazeStart) {
			// mazeWidth and mazeHeight do not include walls
			boolean[][] mazeWall = new boolean[mazeWidth * 2 - 1][mazeHeight * 2 - 1];
			for (int x = 0; x < mazeWidth * 2 - 1; x++) {
				for (int y = 0; y < mazeHeight * 2 - 1; y++) {
					mazeWall[x][y] = x % 2 == 1 || y % 2 == 1;
				}
			}

			if (this == MazeAlgorithms.KRUSKAL) {
				int nOfGroups = 0;
				int[][] groupID = new int[mazeWidth][mazeHeight];
				for (int x = 0; x < mazeWidth; x++) {
					for (int y = 0; y < mazeHeight; y++) {
						groupID[x][y] = nOfGroups++;
					}
				}
				int nOfWalls = 0;
				Point[] checkWall = new Point[(mazeWidth - 1) * mazeHeight + (mazeHeight - 1) * mazeWidth];
				for (int x = 1; x < mazeWidth * 2 - 1; x += 2) {
					for (int y = 0; y <= mazeHeight * 2 - 1; y += 2) {
						checkWall[nOfWalls++] = new Point(x, y);
					}
				}
				for (int x = 0; x <= mazeWidth * 2 - 1; x += 2) {
					for (int y = 1; y < mazeHeight * 2 - 1; y += 2) {
						checkWall[nOfWalls++] = new Point(x, y);
					}
				}
				while (nOfWalls > 0) {
					int chosenWall = (int) (Math.random() * nOfWalls);
					Point increase = new Point(0, 0);
					if (checkWall[chosenWall].x % 2 == 1) {
						increase.x = 1;
					} else {
						increase.y = 1;
					}
					if (groupID[(int) (checkWall[chosenWall].x / 2)][(int) (checkWall[chosenWall].y / 2)] != groupID[(int) (checkWall[chosenWall].x / 2) + increase.x][(int) (checkWall[chosenWall].y / 2) + increase.y]) {
						mazeWall[checkWall[chosenWall].x][checkWall[chosenWall].y] = false;
						int lowerGroup = Math.min(groupID[(int) (checkWall[chosenWall].x / 2)][(int) (checkWall[chosenWall].y / 2)], groupID[(int) (checkWall[chosenWall].x / 2) + increase.x][(int) (checkWall[chosenWall].y / 2) + increase.y]);
						int higherGroup = Math.max(groupID[(int) (checkWall[chosenWall].x / 2)][(int) (checkWall[chosenWall].y / 2)], groupID[(int) (checkWall[chosenWall].x / 2) + increase.x][(int) (checkWall[chosenWall].y / 2) + increase.y]);
						for (int x = 0; x < groupID.length; x++) {
							for (int y = 0; y < groupID[x].length; y++) {
								if (groupID[x][y] == lowerGroup || groupID[x][y] == higherGroup) {
									groupID[x][y] = lowerGroup;
								}
							}
						}
						checkWall[chosenWall].x = checkWall[--nOfWalls].x;
						checkWall[chosenWall].y = checkWall[nOfWalls].y;
					} else {
						checkWall[chosenWall].x = checkWall[--nOfWalls].x;
						checkWall[chosenWall].y = checkWall[nOfWalls].y;
					}
				}
			} else if (this == MazeAlgorithms.DEPTH_FIRST_SEARCH) {
				ArrayList<Compass.Cardinal> path = new ArrayList<Compass.Cardinal>();
				boolean[][] visited = new boolean[mazeWidth][mazeHeight];
				for (int x = 0; x < visited.length; x++) {
					for (int y = 0; y < visited[x].length; y++) {
						visited[x][y] = false;
					}
				}
				Point location = new Point((int) (mazeStart.x / 2), (int) (mazeStart.y / 2));
				visited[location.x][location.y] = true;
				do {
					Compass.Cardinal d = findNextRandomCell(location.x, location.y, visited);
					if (d != null) {
						path.add(d);
		                mazeWall[location.x * 2 + Compass.directionToX(d)][location.y * 2 + Compass.directionToY(d)] = false;
		                location.x += Compass.directionToX(d);
		                location.y += Compass.directionToY(d);
					} else if (path.size() > 0) {
		                location.x -= Compass.directionToX(path.get(path.size() - 1));
		                location.y -= Compass.directionToY(path.get(path.size() - 1));
		                path.remove(path.size() - 1);
					} else {
						break;
					}
		            visited[location.x][location.y] = true;
				} while (!checkAllTrue(visited));
			}
			return mazeWall;
		}
	}
	
	public static final class InitialSpectatorViewLocation implements Map.MapLocation3D, Serializable {
		private static final long serialVersionUID = 4524188075463177612L;
		
		private final double LOCATION_X;
		private final double LOCATION_Y;
		private final double LOCATION_Z;
		private final double HORIZONTAL_ANGLE;
		private final double VERTICAL_ANGLE;
		
		public InitialSpectatorViewLocation(double locationX, double locationY, double locationZ, double horizontalAngle, double verticalAngle) {
			LOCATION_X = locationX;
			LOCATION_Y = locationY;
			LOCATION_Z = locationZ;
			HORIZONTAL_ANGLE = horizontalAngle;
			VERTICAL_ANGLE = verticalAngle;
		}
		
		@Override
		public double getLocationX() {
			return LOCATION_X;
		}
		
		@Override
		public double getLocationY() {
			return LOCATION_Y;
		}
		
		@Override
		public double getLocationZ() {
			return LOCATION_Z;
		}
		
		public double getHorizontalAngle() {
			return HORIZONTAL_ANGLE;
		}

		public double getVerticalAngle() {
			return VERTICAL_ANGLE;
		}
	}
}
