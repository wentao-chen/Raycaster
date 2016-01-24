import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class representing a player.
 * @author Wentao
 *
 */
public class Player extends CylinderMapItem implements ChatMessage.ChatMessageTarget {
	private static final long serialVersionUID = 7851778635978968552L;
	
	/**
	 * The maximum vertical angle of any player.
	 */
	public static final double MAX_TILT_UP_ANGLE = Math.PI / 3;
	/**
	 * The minimum vertical angle of any player.
	 */
	public static final double MAX_TILT_DOWN_ANGLE = -Math.PI / 3;
	
	/**
	 * The default fall damage calculator.
	 */
	public static final FallDamageCalculator DEFAULT_FALL_DAMAGE_CALCULATOR = new FallDamageCalculator.StandardFallDamageCalaculator();
	
	/**
	 * The step size of a player.
	 */
	public static final double PLAYER_STEP_SIZE = 0.8;
	
	/**
	 * The additional speed and jump multiplier when the player has muscles.
	 */
	public static final double MUSCLE_SIZE = 1.1;
	
	/**
	 * The default console command for adjusting the speed of a player.
	 */
	public static final Console.Command DEFAULT_PLAYER_SPEED_COMMAND = new Console.PlayerModificationCommand("Speed Change", "Change the speed of a player", "speed", true) {
		@Override
		public String getPlayerFoundMessage(Player foundPlayer) {
			return foundPlayer.getName() + " CURRENT SPEED: " + foundPlayer.getDefaultSpeed() +"m/s. Enter new speed:";
		}
		@Override
		public void modify(Console console, Player targetPlayer, String input) {
			try {
				double newValue = Double.parseDouble(input);
				targetPlayer.setSpeed(Math.abs(newValue));
				console.appendText("ACTION: Speed of " + targetPlayer.getName() +" changed to " + targetPlayer.getDefaultSpeed() +"m/s.");
			} catch (NumberFormatException e) {
				console.appendText("ERROR: Invalid decimal number format. Command canceled");
			}
		}
    };
	
	/**
	 * The default console command for adjusting the jump height of a player.
	 */
	public static final Console.Command DEFAULT_PLAYER_JUMP_COMMAND = new Console.PlayerModificationCommand("Jump Change", "Change the max jump height of a player", "jump", true) {
		@Override
		public String getPlayerFoundMessage(Player foundPlayer) {
			return foundPlayer.getName() + " CURRENT JUMP: " + foundPlayer.getDefaultMaxJumpHeight() +"m. Enter new max jump height:";
		}
		@Override
		public void modify(Console console, Player targetPlayer, String input) {
			try {
				double newValue = Double.parseDouble(input);
				targetPlayer.setMaxJumpHeight(Math.abs(newValue));
				console.appendText("ACTION: Jump of " + targetPlayer.getName() +" changed to " + targetPlayer.getDefaultMaxJumpHeight() +"m.");
			} catch (NumberFormatException e) {
				console.appendText("ERROR: Invalid decimal number format. Command canceled");
			}
		}
    };

	/**
	 * The default console command for setting fall damage.
	 */
	public static final Console.Command DEFAULT_FALL_DAMAGE_COMMAND = new Console.Command("Fall Damage", "", "falldamage") {
    	@Override
    	public String getDescription() {
    		return "Sets the fall damage of the current player " + (Player.isAllowFallDamage() ? "off" : "on");
    	}
		@Override
		public boolean execute(Console console, Game game, String input) {
			Player.setAllowFallDamage(!Player.isAllowFallDamage());
			console.appendText("ACTION: Fall Damage " + (Player.isAllowFallDamage() ? "ON" : "OFF"));
			return true;
		}
    };
	
	/**
	 * The default console command for adjusting the money amount of a player.
	 */
	public static final Console.Command DEFAULT_PLAYER_MONEY_COMMAND = new Console.PlayerModificationCommand("Money Change", "Change the money of a player", "money", true) {
		@Override
		public String getPlayerFoundMessage(Player foundPlayer) {
			return foundPlayer.getName() + " CURRENT MONEY: $" + foundPlayer.getMoney() +". Enter new money amount:";
		}
		@Override
		public void modify(Console console, Player targetPlayer, String input) {
			try {
				int newValue = Integer.parseInt(input);
				targetPlayer.setMoney(Math.abs(newValue));
				console.appendText("ACTION: Money of " + targetPlayer.getName() +" changed to $" + targetPlayer.getMoney() +".");
			} catch (NumberFormatException e) {
				console.appendText("ERROR: Invalid integer number format. Command canceled");
			}
		}
    };
	
	/**
	 * The default console command for viewing the statistics of a player.
	 */
	public static final Console.Command DEFAULT_PLAYER_STATS_COMMAND = new Console.PlayerSelectionCommand("Stats", "View the stats of a player", "stats") {
		@Override
		public String getPlayerFoundMessage(Player foundPlayer) {
			String string = "The following are stats for " + foundPlayer.getName() + ":\n";
			string += "ID: " + foundPlayer.getID() + "\n";
			string += "CURRENT LOCATION: (" + foundPlayer.getLocationX() + ", " + foundPlayer.getLocationY() + ")\n";
			string += "CURRENT HORIZONTAL FIELD OF VIEW: " + Math.toDegrees(foundPlayer.getHorizontalFOV()) + "degrees (DEFAULT HORIZONTAL FIELD OF VIEW: " + Math.toDegrees(foundPlayer.getDefaultHorizontalFOV()) + "degrees)\n";
			string += "CURRENT HORIZONTAL DIRECTION: " + Math.toDegrees(foundPlayer.getHorizontalDirection()) + "degrees\n";
			string += "CURRENT VERTICAL DIRECTION: " + Math.toDegrees(foundPlayer.getVerticalDirection()) + "degrees\n";
			string += "CURRENT SPEED: " + foundPlayer.getSpeed() + "m/s (DEFAULT SPEED: " + foundPlayer.getDefaultSpeed() + "m/s)\n";
			string += "CURRENT VIEW HEIGHT: " + foundPlayer.getViewHeight() + "m\n";
			string += "CURRENT TOUCH DAMAGE: " + foundPlayer.getTouchDamage() + "\n";
			string += "CURRENT SUSCEPTIBILITY TO TOUCH DAMAGE: " + foundPlayer.isSusceptibleToTouchDamage() + "\n";
			string += "CURRENT HEALTH: " + foundPlayer.getHealth() + " (MAX HEALTH: " + foundPlayer.getMaxHealth() + ")\n";
			string += "CURRENT ARMOR: " + foundPlayer.getArmor() + " with" + (foundPlayer.hasHelmet() ? "" : "out") +  " helmet (MAX ARMOR: " + foundPlayer.getMaxArmor() + ")\n";
			string += "CURRENT MONEY: $" + foundPlayer.getMoney() + "\n";
			string += "CURRENT REACH_LENGTH: " + foundPlayer.getReachLength() + "m\n";
			string += "CURRENT TEAM: " + (foundPlayer.getTeam() != null ? foundPlayer.getTeam().getName() : Team.SPECTATOR_NAME) + "\n";
			string += "CURRENT ZOOM: " + foundPlayer.getZoom() + "x\n";
			string += "CURRENT SPECTATOR HEIGHT: " + foundPlayer.getSpectatorHeight() + "m\n";
			string += "CURRENT STEP HEIGHT: " + foundPlayer.getStepHeight() + "m\n";
			string += "CURRENT MAX JUMP HEIGHT: " + foundPlayer.getMaxJumpHeight() + "m (DEFAULT MAX JUMP HEIGHT: " + foundPlayer.getDefaultMaxJumpHeight() + "m)\n";
			string += "CURRENT JUMP: " + (foundPlayer.isJumping() ? foundPlayer.getJumpHeight() + "m" : "NOT JUMPING") + "\n";
			string += "CURRENT FALL: " + (foundPlayer.isFalling() ? foundPlayer.getFallHeight() + "m" : "NOT FALLING") + "\n";
			string += "CURRENT STANCE: " + (foundPlayer.getCrouchLevel() != null ? foundPlayer.getCrouchLevel() : "STANDING") + "\n";
			string += "CURRENT CONTROLLER: " + (foundPlayer.MOVEMENT_CONTROLLER != null ? foundPlayer.MOVEMENT_CONTROLLER : "USER") + "\n";
			string += "CURRENT ITEM HELD: " + (foundPlayer.getMainHoldItem() != null ? foundPlayer.getMainHoldItem().getName() : "NOTHING") + "\n";
			string += "PREVIOUS ITEM HELD: " + (foundPlayer.getLastHoldItem() != null ? foundPlayer.getLastHoldItem().getName() : "NOTHING") + "\n";
			string += "CURRENT ITEMS CARRIED:\n";
			for (int i = 0; i < foundPlayer.CARRY_ITEMS.size(); i++) {
				string += "CARRIED ITEM (" + i + "): " + foundPlayer.CARRY_ITEMS.get(i).getName() + "\n";
			}
			string += "CURRENT TACTICAL SHIELD: " + (foundPlayer.getTacticalShield() != null ? foundPlayer.getTacticalShield().getName() + " (DEPLOYED: " + foundPlayer.getTacticalShield().isDeployed() + ")" : "NONE") + "\n";
			string += "CURRENT BOMB: " + (foundPlayer.getBomb() != null ? foundPlayer.getBomb().getName() : "NONE") + "\n";
			string += "CURRENT DEFUSE KIT: " + (foundPlayer.getDefuseKit() != null ? foundPlayer.getDefuseKit().getName() : "NONE") + "\n";
			string += "CURRENT SCORE: " + foundPlayer.getKills() + "\n";
			string += "CURRENT DEATHS: " + foundPlayer.getDeaths() + "\n";
			string += "CURRENT DISTANCE TRAVELLED: " + foundPlayer.getDistanceTravelled() + "m\n";
			string += "CURRENT STEPS: " + foundPlayer.getSteps() + "\n";
			string += "DEFUSING BOMB: " + foundPlayer.isDefusingBomb() + (foundPlayer.isDefusingBomb() ? "(DEFUSED PROGRESS: " + Math.round(foundPlayer.getDefuseProgress() * 100) + "%)" :"") + "\n";
			string += "CURRENT LATENCY: " + foundPlayer.getLatency() + "\n";
			string += "HAS MUSCLES: " + foundPlayer.hasMuscles();
			return string;
		}
    };
	
	private static final AtomicInteger ID_ASSIGNER = new AtomicInteger();
	private static final AtomicBoolean ALLOW_FALL_DAMAGE = new AtomicBoolean(true);
	private final int ID;
	private String name = "";
	private final double DEFAULT_HORIZONTAL_FOV;
	private transient double tempHorizontalFOV = Math.PI / 2;
	private double horizontalDirection = 0;
	private double verticalDirection = 0;
	private Game game = null;
	private double speed = 0;
	private final double SIGHT_HEIGHT;
	private final int TOUCH_DAMAGE;
	private final boolean SUSCEPTIBLE_TO_TOUCH_DAMAGE;
	private final int MAX_HEALTH;
	private int health = 100;
	private final int MAX_ARMOR;
	private int armor = 0;
	private boolean hasHelmet = false;
	private int money = 0;
	private final double REACH_LENGTH;
	private Team team = null;
	private final File TERMINATION_SOUND_FILE;
	private final ArrayList<File> STEP_SOUND_FILES = new ArrayList<File>();
	
	private double zoom = 1;
	private double spectatorHeight = 0;
	private double lastFloorHeight = Double.MAX_VALUE;
	private Double lastLowestBottomHeightAbove = null;
	private double stepHeight = 0;
	private double maxJumpHeight = 0;
	private Long jumpStartTime = null;
	private double jumpFromHeight = 0;
	private double fallFromHeight = 0;
	private Long fallStartTime = null;
	private double ladderHeight = 0;
	private CrouchLevel crouchLevel = null;
	private final FallDamageCalculator FALL_DAMAGE_CALCULATOR;
	private final PlayerMovementController MOVEMENT_CONTROLLER;
	private HoldItem mainHoldItem = null;
	private HoldItem lastHoldItem = null;
	private final ArrayList<HoldItem> CARRY_ITEMS = new ArrayList<HoldItem>();
	private TacticalShield tacticalShield = null;
	private Bomb bomb = null;
	private DefuseKit defuseKit = null;
	private long startFlashTime = 0;
	private long flashDuration = 0;
	private int kills = 0;
	private int deaths = 0;
	private double distanceTravelled = 0;
	private int steps = 0;
	private final ArrayList<AttackEvent> ATTACK_EVENTS = new ArrayList<AttackEvent>();
	private transient ArrayList<ChatMessage.MessageContent> recentChatMessages = new ArrayList<ChatMessage.MessageContent>();
	private transient Long startDefuseTime = null;
	private transient ScheduledExecutorService defuseBombExecutor = null;
	private boolean hasMuscles = false;
	
	private transient Long latency = null;
	private transient boolean listenersOn = true;
	private transient ArrayList<PlayerListener> listeners = new ArrayList<PlayerListener>();
	
	private Player(String name, double horizontalFOV, double horizontalDirection, double verticalDirection, Double locationX, Double locationY, Double spectatorHeight, double initialStartFloorHeight, double height, Game game, double speed, double stepHeight, double maxJumpHeight, double sightHeight, int touchDamage, boolean susceptibleToTouchDamage, int health, int armor, double physicalWidth, double reachLength, String projectedImagePath, FallDamageCalculator fallDamageCalculator, PlayerMovementController movementController, Team team) {
		super(physicalWidth / 2, height, Main.getImage(projectedImagePath, Color.WHITE));
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		if (height <= 0) throw new IllegalArgumentException("Height of the player must be greater than 0");
		if (physicalWidth <= 0) throw new IllegalArgumentException("Physical width must be greater than 0");
		if (stepHeight <= 0) throw new IllegalArgumentException("Step height must be greater than 0");
		health = Math.max(health, 1);
		
		ID = ID_ASSIGNER.getAndIncrement();
		this.name = name;
		this.team = team;
		MAX_HEALTH = health;
		setSpeed(speed);
		SIGHT_HEIGHT = sightHeight;
		TOUCH_DAMAGE = touchDamage;
		SUSCEPTIBLE_TO_TOUCH_DAMAGE = susceptibleToTouchDamage;
		MAX_ARMOR = armor;
		REACH_LENGTH = reachLength;
		FALL_DAMAGE_CALCULATOR = fallDamageCalculator;
		MOVEMENT_CONTROLLER = movementController;
		TERMINATION_SOUND_FILE = new File(Main.PLAYER_TERMINATION_SOUND_PATH);
		for (String s : Main.PLAYER_STEP_SOUND_PATHS) {
			if (s != null) {
				STEP_SOUND_FILES.add(new File(s));
			}
		}
		DEFAULT_HORIZONTAL_FOV = Math.abs(horizontalFOV);
		this.tempHorizontalFOV = getDefaultHorizontalFOV();
		setHorizontalDirection(horizontalDirection);
		setVerticalDirection(verticalDirection);
		setHealth(health);
		setMaxJumpHeight(maxJumpHeight);
		setStepHeight(stepHeight);
		setGame(game);
		this.lastFloorHeight = 0;
		if (locationX != null && locationY != null) {
			setLocation(locationX, locationY, initialStartFloorHeight);
		}
		if (spectatorHeight != null) {
			this.spectatorHeight = Math.abs(spectatorHeight);
		}
		updatePosition(initialStartFloorHeight);
		resetBasicTermination(false, false, null, true);
	}
	
	/**
	 * Designed for resetting after the termination of a player.<br>
	 * Resets the following:
	 * <ul>
	 * 	<li>carry items -> removes all (such as tactical shield, bomb, and defuse kit)</li>
	 * 	<li>money</li>
	 * 	<li>armor (including helmet) -> {@code 0}</li>
	 * 	<li>jump and fall states</li>
	 * 	<li>crouch level -> {@code null}</li>
	 * 	<li>health -> {@link #getMaxHealth()}</li>
	 * 	<li>location -> spawn location if {@link #getGame()} {@code != null}</li>
	 * </ul>
	 * @param dropAllCarryItems {@code true} to drop all carry items or {@code false} to ignore current carry items
	 * @param provideMelee {@code true} to ensure the player has a weapon from a melee slot; the default if one does not exist is created from {@link Melee#createDefaultKnife()}; otherwise {@code false}
	 * @param money the amount of money of the player after the reset or {@code null} to the amount of money should not be changed
	 * @param makeAlive {@code true} if the player should be alive; otherwise {@code false}
	 * @see #removeAllCarryItems()
	 */
	synchronized void resetBasicTermination(boolean dropAllCarryItems, boolean provideMelee, Integer money, boolean makeAlive) {
		setBomb(null, false);
		if (dropAllCarryItems) {
			dropAllCarryItems(provideMelee ? Weapon.DEFAULT_MELEE_WEAPON_SLOT : null);
		}
		if (provideMelee && getNumberOfItemsInSlot(Weapon.DEFAULT_MELEE_WEAPON_SLOT) <= 0) {
			addCarryItems(Melee.createDefaultKnife());
		}
		cancelDefusingBomb();
		if (money != null) {
			setMoney(money);
		}
		setArmor(0);
		setHelmet(false);
		resetJumpAndFall();
		this.jumpFromHeight = 0;
		this.fallFromHeight = 0;
		this.crouchLevel = null;
		
		if (makeAlive) {
			setHealth(getMaxHealth());
			if (getGame() != null) {
				getGame().setToSpawnLocation(this);
			}
		}
	}
	
	/**
	 * Resets the jump and fall of the player.
	 */
	private synchronized void resetJumpAndFall() {
		this.jumpStartTime = null;
		this.fallStartTime = null;
	}
	
	/**
	 * Builder class for {@code Player}.
	 * @author Wentao
	 */
	public static class Builder {
		private final String NAME;
		private double horizontalFOV = Math.PI * 5 / 12;
		private double horizontalDirection = Math.PI / 2;
		private double verticalDirection = 0;
		private Double locationX = null;
		private Double locationY = null;
		private Double spectatorHeight = null;
		private double initialStartFloorHeight = 0;
		private double height = 1;
		private Game game = null;
		private double speed = 2;
		private double stepHeight = 0.1;
		private double maxJumpHeight = 0.5;
		private double sightHeight = height * -0.1;
		private int touchDamage = 0;
		private boolean susceptibleToTouchDamage = true;
		private int health = 100;
		private int armor = 100;
		private double physicalWidth = 0.5;
		private double reachLength = 1;
		private String projectedImagePath = "";
		private FallDamageCalculator fallDamageCalculator = Player.DEFAULT_FALL_DAMAGE_CALCULATOR;
		private PlayerMovementController movementController = null;
		private Team team = null;
		
		/**
		 * Constructs a builder for a player.
		 * @param name the name of the player
		 * @param game the game that the player will be a part of
		 * @param speed the default speed of the player
		 * @param maxJumpHeight the maximum jump height of the player
		 */
		public Builder(String name, Game game, double speed, double maxJumpHeight) {
			NAME = name;
			this.game = game;
			this.speed = speed;
			this.maxJumpHeight = maxJumpHeight;
		}
		
		/**
		 * Sets the horizontal field of view in radians. The field of view must be a positive non-zero value.
		 * @param horizontalFOV the horizontal field of view in radians
		 * @return the builder
		 * @see #horizontalFOVD(double)
		 */
		public Builder horizontalFOV(double horizontalFOV) {
			if (horizontalFOV <= 0) throw new IllegalArgumentException("Field of view must be greater than 0");
			this.horizontalFOV = horizontalFOV;
			return this;
		}

		/**
		 * Sets the horizontal field of view in degrees. The field of view must be a positive non-zero value.
		 * @param horizontalFOVD the horizontal field of view in degrees
		 * @return the builder
		 * @see #horizontalFOV(double)
		 */
		public Builder horizontalFOVD(double horizontalFOVD) {
			horizontalFOV(Math.toRadians(horizontalFOVD));
			return this;
		}
		
		/**
		 * Sets the horizontal direction in radians. The direction is the principle angle with east at 0.
		 * @param horizontalDirection the horizontal direction in radians
		 * @return the builder
		 * @see #horizontalDirectionD(double)
		 */
		public Builder horizontalDirection(double horizontalDirection) {
			this.horizontalDirection = (horizontalDirection % (Math.PI * 2) + (Math.PI * 2)) % (Math.PI * 2);
			return this;
		}
		
		/**
		 * Sets the horizontal direction in degrees. The direction is the principle angle with east at 0.
		 * @param horizontalDirectionD the horizontal direction in degrees
		 * @return the builder
		 * @see #horizontalDirection(double)
		 */
		public Builder horizontalDirectionD(double horizontalDirectionD) {
			horizontalDirection(Math.toRadians(horizontalDirectionD));
			return this;
		}

		/**
		 * Sets the vertical direction in radians.
		 * @param verticalDirection the vertical direction in radians
		 * @return the builder
		 */
		public Builder verticalDirection(double verticalDirection) {
			this.verticalDirection = verticalDirection;
			return this;
		}
		
		/**
		 * Sets the initial location of the player
		 * @param locationX the x-coordinate of the location
		 * @param locationY the y-coordinate of the location
		 * @return the builder
		 */
		public Builder location(double locationX, double locationY) {
			this.locationX = locationX;
			this.locationY = locationY;
			return this;
		}
		
		/**
		 * Sets the spectator height in meters.
		 * @param height the spectator height in meters
		 * @return the builder
		 */
		public Builder spectatorHeight(double height) {
			this.spectatorHeight = height;
			return this;
		}
		
		/**
		 * Sets the spectator view location of the player. If {@code location == null}, nothing occurs.
		 * @param location the spectator view location of the player
		 * @return the builder
		 */
		public Builder initialSpectatorLocation(Map.InitialSpectatorViewLocation location) {
			if (location != null) {
				location(location.getLocationX(), location.getLocationY());
				spectatorHeight(location.getLocationZ());
				horizontalDirection(location.getHorizontalAngle());
				verticalDirection(location.getVerticalAngle());
			}
			return this;
		}
		
		/**
		 * Sets the minimum height of the floor that the player starts on in meters. If the height is greater than the maximum height, the top height of the wall is used.
		 * @param initialStartFloorHeight the minimum height of the floor that the player starts on
		 * @return the Builder
		 */
		public Builder initialStartFloor(int initialStartFloorHeight) {
			this.initialStartFloorHeight = initialStartFloorHeight;
			return this;
		}
		
		/**
		 * Sets the height in blocks.
		 * @param height the height in blocks
		 * @return the builder
		 */
		public Builder height(double height) {
			this.height = height;
			return this;
		}
		
		/**
		 * Sets the step height of the player.
		 * @param stepHeight the step height of the player
		 * @return the builder
		 */
		public Builder stepHeight(double stepHeight) {
			this.stepHeight = stepHeight;
			return this;
		}

		/**
		 * Sets the sight height of the player.
		 * @param sightHeight the sight height of the player
		 * @return the builder
		 */
		public Builder sightHeight(double sightHeight) {
			this.sightHeight = sightHeight;
			return this;
		}

		/**
		 * Sets the damage inflicted to other players due to contact.
		 * @param touchDamage the damage inflicted
		 * @return the builder
		 */
		public Builder touchDamage(int touchDamage) {
			this.touchDamage = touchDamage;
			return this;
		}
		
		/**
		 * Sets the susceptibility of the player to touch damage
		 * @param susceptibleToTouchDamage true if the player will be affected by touch damage; otherwise, false
		 * @return the builder
		 */
		public Builder susceptibleToTouchDamage(boolean susceptibleToTouchDamage) {
			this.susceptibleToTouchDamage = susceptibleToTouchDamage;
			return this;
		}
		
		/**
		 * Sets the initial and maximum health level of the player.
		 * @param health the initial and maximum health level of the player
		 * @return the Builder
		 */
		public Builder health(int health) {
			this.health = health;
			return this;
		}

		/**
		 * Sets the maximum armor level of the player.
		 * @param armor the maximum armor level of the player
		 * @return the Builder
		 */
		public Builder armor(int armor) {
			this.armor = armor;
			return this;
		}
		
		/**
		 * Sets the physical width of the player in meters.
		 * @param physicalWidth the physical width of the player
		 * @return the Builder
		 */
		public Builder physicalWidth(double physicalWidth) {
			this.physicalWidth = physicalWidth;
			return this;
		}
		
		/**
		 * Sets the reach length of the player in meters.
		 * @param reachLength the reach length of the player
		 * @return the Builder
		 */
		public Builder reachLength(double reachLength) {
			this.reachLength = reachLength;
			return this;
		}
		
		/**
		 * Sets the image path of the projected image of the player.
		 * @param projectedImagePath the image path of the projected image of the player
		 * @return the Builder
		 */
		public Builder projectedImagePath(String projectedImagePath) {
			this.projectedImagePath = projectedImagePath;
			return this;
		}
		
		/**
		 * Sets the fall damage calculator of the player.
		 * @param fallDamageCalculator the fall damage calculator of the player
		 * @return the Builder
		 */
		public Builder fallDamageCalculator(FallDamageCalculator fallDamageCalculator) {
			this.fallDamageCalculator = fallDamageCalculator;
			return this;
		}
		
		/**
		 * Sets the movement controller of the player.
		 * @param movementController the movement controller of the player
		 * @return the Builder
		 */
		public Builder movementController(PlayerMovementController movementController) {
			this.movementController = movementController;
			return this;
		}
		
		/**
		 * Sets the team of the player.
		 * @param team the team of the player
		 * @return the Builder
		 */
		public Builder team(Team team) {
			this.team = team;
			return this;
		}
		
		/**
		 * Creates a new {@code Player}
		 * @return a new {@code Player}
		 */
		public Player build() {
			return new Player(NAME, this.horizontalFOV, this.horizontalDirection, this.verticalDirection, this.locationX, this.locationY, this.spectatorHeight, this.initialStartFloorHeight, this.height, this.game, this.speed, this.stepHeight, this.maxJumpHeight, this.sightHeight, this.touchDamage, this.susceptibleToTouchDamage, this.health, this.armor, this.physicalWidth, this.reachLength, this.projectedImagePath, this.fallDamageCalculator, this.movementController, this.team);
		}
	}
	
	/**
	 * Gets the id number corresponding to the player.
	 * @return the id of the player
	 */
	public int getID() {
		return ID;
	}
	
	/**
	 * Gets the name of the player.
	 * @return the name of the player
	 */
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getDisplayName(Player player) {
		return getName() + (isSpectator() ? "" : "(" + (getTeam().equals(player.getTeam()) ? "Friendly" : "Hostile") + ")");
	}
	
	/**
	 * Sets the name of the player.
	 * @param name the name of the player
	 */
	public void setName(String name) {
		if (name == null) throw new IllegalArgumentException("name cannot be null");
		this.name = name;
	}
	
	/**
	 * Checks if fall damage is enabled.
	 * @return {@code true} if fall damage is enabled; otherwise {@code false}
	 */
	public static boolean isAllowFallDamage() {
		return ALLOW_FALL_DAMAGE.get();
	}
	
	/**
	 * Sets fall damage.
	 * @param allow {@code true} to enable fall damage or {@code false} to disable fall damage
	 */
	public static void setAllowFallDamage(boolean allow) {
		ALLOW_FALL_DAMAGE.set(allow);
	}
	
	/**
	 * Gets the default horizontal field of view in radians without any modifications
	 * @return the default horizontal field of view
	 */
	public double getDefaultHorizontalFOV() {
		return DEFAULT_HORIZONTAL_FOV;
	}
	
	/**
	 * Gets the horizontal field of view in radians.
	 * @return the horizontal field of view in radians.
	 */
	public double getHorizontalFOV() {
		return this.tempHorizontalFOV;
	}

	/**
	 * Gets the horizontal direction in radians. The direction is the principle angle with east at 0. The returned angle is in the range of 0 to 2 * pi.
	 * @return the horizontal direction in radians.
	 */
	public double getHorizontalDirection() {
		return this.horizontalDirection;
	}

	/**
	 * Gets the angle of elevation (or depression if the value is negative) of the player in radians.
	 * @return the angle of elevation in radians
	 */
	public double getVerticalDirection() {
		return this.verticalDirection;
	}

	public void setVerticalDirection(double verticalDirection) {
		this.verticalDirection = Math.min(Math.max(verticalDirection, MAX_TILT_DOWN_ANGLE), MAX_TILT_UP_ANGLE);
	}

	/**
	 * Sets the horizontal direction in radians. The direction is the principle angle with east at 0.
	 * @param horizontalDirection the horizontal direction in radians
	 */
	public void setHorizontalDirection(double horizontalDirection) {
		setHorizontalDirection(horizontalDirection, true);
	}

	/**
	 * Sets the horizontal direction in radians. The direction is the principle angle with east at 0.
	 * @param horizontalDirection the horizontal direction in radians
	 * @param willCancelDefuse {@code true} if the new horizontal direction does not face the bomb, defuse will be cancelled, otherwise {@code false}
	 */
	public void setHorizontalDirection(double horizontalDirection, boolean willCancelDefuse) {
		this.horizontalDirection = (horizontalDirection % (Math.PI * 2) + (Math.PI * 2)) % (Math.PI * 2);
		if (willCancelDefuse && isDefusingBomb() && getGame() != null && !getGame().isLookingAtBomb(this)) {
			cancelDefusingBomb();
		}
	}
	
	/**
	 * Checks if an angle in radians relative to the map is within the horizontal field of view of the player based on the width of the field of view and the current horizotnal direction.
	 * @param angle the principle angle relative to the map
	 * @return true if an angle in radians relative to the map is within the horizontal field of view; otherwise, false
	 * @see #getHorizontalDirection()
	 * @see #getHorizontalFOV()
	 * @see #isHorizontalAngleInView(double, double, double)
	 */
	public boolean isHorizontalAngleInView(double angle) {
		return isHorizontalAngleInView(getHorizontalDirection(), getHorizontalFOV(), angle);
	}
	
	/**
	 * Checks if an angle lies between two other angles, inclusive.
	 * @param boundary1 the first boundary angle in radians
	 * @param boundary2 the second boundary angle in radians counter-clockwise after {@code boundary1}
	 * @param angle the angle 
	 * @return true if the angle lies within the boundaries; otherwise, false
	 */
	public static boolean doesAngleLieBetween(double boundary1, double boundary2, double angle) {
		boundary1 = (boundary1 % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		boundary2 = (boundary2 % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		angle = (angle % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		if (boundary1 == boundary2) {
			return angle == boundary1;
		} else if (boundary1 < boundary2) {
			return boundary2 >= angle && angle >= boundary1;
		} else {
			return angle >= boundary1 || angle <= boundary2;
		}
	}
	
	/**
	 * Checks if an angle lies within a region in a circle that is of a specific size centered at a different angle.
	 * <br><br>
	 * Any range greater or equal to a full circle will return {@code true}. Any range less than or equal to 0 will return {@code false}.
	 * @param horizontalDirection the angle that the region is centered around
	 * @param range the size of the region in radians
	 * @param angle the angle to check for
	 * @return true if the angle lies within the region; otherwise, false
	 */
	public static boolean isHorizontalAngleInView(double horizontalDirection, double range, double angle) {
		if (range >= Math.PI * 2) {
			return true;
		} else if (range <= 0) {
			return false;
		} else {
			return (angle > horizontalDirection? angle - Math.PI * 2 : angle) > horizontalDirection - range / 2 || (angle < horizontalDirection ? angle + Math.PI * 2 : angle) < horizontalDirection + range / 2;
		}
	}

	/**
	 * Sets the location and updates the position of the player in meters relative to the map.
	 * @param locationX the x-coordinate of the location in meters
	 * @param locationY the y-coordinate of the location in meters
	 * @see #updatePosition()
	 */
	public void setLocation(double locationX, double locationY) {
		setLocation(locationX, locationY, getBottomHeight());
	}
	
	/**
	 * Sets the location and updates the position of the player in meters relative to the map and updates the position height of the player.
	 * @param locationX the x-coordinate of the location in meters
	 * @param locationY the y-coordinate of the location in meters
	 * @param minimumFloorHeight the minimum height
	 * @see #updatePosition(double)
	 */
	@Override
	public void setLocation(double locationX, double locationY, double minimumFloorHeight) {
		if (isPlayerListenersOn() && (getLocationX() != locationX || getLocationY() != locationY)) {
			Iterator<PlayerListener> iterator = this.listeners.iterator();
			while (iterator.hasNext()) {
				iterator.next().playerMoved(this);
			}
		}
		synchronized (this) {
			this.distanceTravelled += Map.findDistance(locationX, locationY, getLocationX(), getLocationY());
		}
		setLocationX(locationX);
		setLocationY(locationY);
		updatePosition(minimumFloorHeight);
	}
	
	/**
	 * Convenience method for {@link #updatePosition(double)} with the floor being the highest possible floor.
	 */
	public void updatePosition() {
		updatePosition(Double.MAX_VALUE);
	}
	
	/**
	 * Updates the position of the player if the game is not {@code null}. If the current location of the player conflicts with a wall in the map, the height of the player is set to the
	 * lowest possible height above a minimum height (i.e. the next higher available height). If the player is no longer on top of a floor afterwards, the player falls.
	 * <br><br><br>
	 * The following are updated based on the current location of the player:
	 * <ol>
	 * <li>The cached floor height</li>
	 * <li>The lowest ceiling height above the player is up</li>
	 * </ol>
	 * @param minimumFloorHeight the minimum height
	 */
	public void updatePosition(double minimumFloorHeight) {
		if (getGame() != null) {
			double previousHeight = getFloorHeight();
			if (getMap().doesCylinderIntersectWall(getBase(), getBottomHeight(), getPhysicalHeight(), getStepHeight()) != null ||
					getMap().doesCylinderIntersectCeiling(getBase(), getBottomHeight(), getPhysicalHeight(), getStepHeight()) != null ||
					getMap().doesCylinderIntersectFence(getLocationX(), getLocationY(), getPhysicalHalfWidth(), getBottomHeight(), getPhysicalHeight(), getStepHeight()) != null) {
				this.lastFloorHeight = Math.max(getMap().getHighestWallInArea(getBase(), minimumFloorHeight, getPhysicalHeight()),
						getMap().getHighestFenceInCircle(getLocationX(), getLocationY(), getPhysicalHalfWidth(), minimumFloorHeight, getPhysicalHeight()));
				this.lastFloorHeight = Math.max(this.lastFloorHeight, getMap().getHighestCeilingInArea(getBase(), minimumFloorHeight, getPhysicalHeight()));
			} else {
				getAndUpdateFloorHeight();
			}
			this.lastLowestBottomHeightAbove = getLowestBottomHeightAbove();
			if (previousHeight < getFloorHeight()) {
				fall(getFallHeight());
			}
		}
	}
	
	/**
	 * Gets a positive multiplier representing the amount of zoom currently observed by the player. A zoom multiplier of 1 is the default zoom level.
	 * @return the zoom multiplier of the player
	 */
	public double getZoom() {
		return this.zoom;
	}
	
	/**
	 * Sets the amount of zoom observed by the player. A zoom multiplier of 1 is the default zoom level.
	 * @param zoom the zoom multiplier
	 */
	public synchronized void setZoom(double zoom) {
		if (zoom <= 0) throw new IllegalArgumentException("zoom must be greater than 0");
		this.tempHorizontalFOV = Math.atan(Math.tan(getDefaultHorizontalFOV() / 2) / zoom) * 2;
		this.zoom = zoom;
	}
	
	/**
	 * Inflict damage to the player due to lava.
	 * @param lavaDamage the damage caused by lava
	 */
	public void onLava(int lavaDamage) {
		addAttackEvents(new AttackEvent(Math.PI - getHorizontalDirection()));
		setHealth(getHealth() - lavaDamage, null, CauseOfDeath.LAVA_DEATH, false);
	}
	
	/**
	 * Inflict damage to the player due to contact with other players.
	 * @param damage the damage caused due to the contact
	 * @param attackLine a line joining the center of the attacking player to the attacked player
	 * @param attacker the attacker
	 */
	public void hitByTouchDamage(int damage, Line2D attackLine, Player attacker) {
		if (isSpectator() || attacker.isSpectator() || (!getGame().allowsFriendlyFire() && attacker.getTeam() != null && attacker.getTeam().equals(getTeam()))) {
			return;
		}
		double angle = Compass.getPrincipleAngle(attackLine.getX1(), attackLine.getY1(), attackLine.getX2(), attackLine.getY2());
		if (isSusceptibleToTouchDamage() && (getTacticalShield() == null || !attackLine.intersectsLine(getCurrentTacticalShieldBaseLocation())) && (attacker.getTacticalShield() == null || !attackLine.intersectsLine(attacker.getCurrentTacticalShieldBaseLocation()))) {
			addAttackEvents(new AttackEvent(angle));
			int takenDamage = Math.max(damage / (hasHelmet() ? 3 : 2), 0);
			setHealth(getHealth() - takenDamage, attacker, CauseOfDeath.ZOMBIE_DEATH, false);
			setArmor(getArmor() - (damage - takenDamage));
		}
	}
	
	@Override
	public void hitByMelee(Melee melee, boolean isConsecutive, Map.Ray2D ray, Player slasher, Melee.AttackType attackType) {
		if (isSpectator() || (!getGame().allowsFriendlyFire() && slasher.getTeam() != null && slasher.getTeam().equals(getTeam()))) {
			return;
		}
		if (getTacticalShield() != null) {
			Line2D attackLine = new Line2D.Double(slasher.getLocationX(), slasher.getLocationY(), getLocationX(), getLocationY());
			if (attackLine.intersectsLine(getCurrentTacticalShieldBaseLocation())) {
				return;
			}
		}
		addAttackEvents(new AttackEvent(ray.getDirection()));
		HitBox hitBox = HitBox.values()[HitBox.values().length - 1];
		double hitHeight = slasher.getViewHeight() + Math.tan(slasher.getVerticalDirection()) * Map.findDistance2D(this, slasher);
		double bottomHeight = getBottomHeight();
		for (HitBox h : HitBox.values()) {
			if (hitHeight > bottomHeight + h.getBottomHeight() * getPhysicalHeight()) {
				hitBox = h;
				break;
			}
		}
		boolean isBackStab = !isHorizontalAngleInView(Compass.getPrincipleAngle(getLocationX(), slasher.getLocationY(), slasher.getLocationX(), getLocationY()));
		if (attackType == Melee.AttackType.SLASH) {
			int armorLost = melee.getDamage().getSlashArmorDamageAbsorbed(hitBox, getArmor(), isConsecutive, isBackStab);
			setHealth(getHealth() - melee.getDamage().getSlashDamage(hitBox, getArmor(), isConsecutive, isBackStab), slasher, melee, hitBox == HitBox.HEAD);
			setArmor(getArmor() - armorLost);
		} else {
			int armorLost = melee.getDamage().getStabArmorDamageAbsorbed(hitBox, getArmor(), isConsecutive, isBackStab);
			setHealth(getHealth() - melee.getDamage().getStabDamage(hitBox, getArmor(), isConsecutive, isBackStab), slasher, melee, hitBox == HitBox.HEAD);
			setArmor(getArmor() - armorLost);
		}
	}

	@Override
	public boolean hitByBullet(Bullet b, Game game, double distanceTravelled, boolean hitTopOrBottomSurface) {
		Player shooter = game.getPlayerByID(b.getShooterID());
		if (isSpectator() || (!getGame().allowsFriendlyFire() && shooter.getTeam() != null && shooter.getTeam().equals(getTeam()))) {
			return false;
		}
		addAttackEvents(new AttackEvent(b.getHorizontalDirection()));
		double shotHeight = b.getHeightAt(distanceTravelled);
		double bottomHeight = getBottomHeight();
		for (HitBox h : HitBox.values()) {
			if (shotHeight > bottomHeight + h.getBottomHeight() * getPhysicalHeight()) {
				if (h != HitBox.HEAD || Line2D.ptLineDist(b.getTravelPath2D().getLocationX(), b.getTravelPath2D().getLocationY(), b.getLocationX(), b.getLocationY(), getLocationX(), getLocationY()) <= getPhysicalHalfWidth() / 3.5) {
					int armorLost = b.getDamage().getArmorDamageAbsorbed(h, getArmor(), hasHelmet());
					setHealth(getHealth() - b.getDamage().getDamage(h, getArmor(), hasHelmet()), shooter, b.getCauseOfDeath(), h == HitBox.HEAD);
					setArmor(getArmor() - armorLost);
					return true;
				} else {
					return false;
				}
			}
		}
		setHealth(getHealth() - b.getDamage().getDefaultDamage(), shooter, b.getCauseOfDeath(), false);
		return true;
	}
	
	@Override
	public void hitByExplosion(ThrownWeapon w, double explodeX, double explodeY) {
		if (isSpectator() || (!getGame().allowsFriendlyFire() && w.getLauncher().getTeam() != null && w.getLauncher().getTeam().equals(getTeam()))) {
			return;
		}
		double distance = Map.findDistance(this, explodeX, explodeY);
		int armorLost = w.getGrenadeDamage().getArmorDamageAbsorbed(distance, getArmor());
		setHealth(getHealth() - w.getGrenadeDamage().getDamage(distance, getArmor()), w.getLauncher(), w.getWeapon(), false);
		setArmor(getArmor() - armorLost);
		this.startFlashTime = System.currentTimeMillis();
		this.flashDuration = w.getGrenadeDamage().flashDuration(distance);
	}

	/**
	 * Checks if the player is able to move to a location at the same height.
	 * @param x the x-coordinate of the location
	 * @param y the y-coordinate of the location
	 * @param allowMovementWhenPlantingOrDefusingBomb {@code true} if the player can move when planting or defusing a bomb, otherwise {@code false}
	 * @return true if the player is able to move to the specified location; otherwise, false
	 */
	public boolean isMoveableTo(double x, double y, boolean allowMovementWhenPlantingOrDefusingBomb) {
		return isMoveableTo(x, y, false, allowMovementWhenPlantingOrDefusingBomb);
	}

	/**
	 * Checks if the player is able to move to a location within the map it is currently in.
	 * @param x the x-coordinate of the location
	 * @param y the y-coordinate of the location
	 * @param ignoreHeight true to ignore the height of the player and only consider intersections with other players(height is not ignored for other players); otherwise false
	 * @param allowMovementWhenPlantingOrDefusingBomb {@code true} if the player can move when planting or defusing a bomb, otherwise {@code false}
	 * @return true if the player is able to move to the specified location; otherwise, false
	 */
	public boolean isMoveableTo(double x, double y, boolean ignoreHeight, boolean allowMovementWhenPlantingOrDefusingBomb) {
		return isMoveableTo(x, y, ignoreHeight, getGame(), allowMovementWhenPlantingOrDefusingBomb);
	}

	/**
	 * Checks if the player is able to move to a location.
	 * @param x the x-coordinate of the location
	 * @param y the y-coordinate of the location
	 * @param ignoreHeight true to ignore the height of the player and only consider intersections with other players(height is not ignored for other players); otherwise false
	 * @param game the game to check the player in
	 * @param allowMovementWhenPlantingOrDefusingBomb {@code true} if the player can move when planting or defusing a bomb, otherwise {@code false}
	 * @return true if the player is able to move to the specified location; otherwise, false
	 */
	public boolean isMoveableTo(double x, double y, boolean ignoreHeight, Game game, boolean allowMovementWhenPlantingOrDefusingBomb) {
		return isMoveableTo(x, y, ignoreHeight, getBottomHeight(), getTopHeight(), game, allowMovementWhenPlantingOrDefusingBomb);
	}
	
	boolean isMoveableTo(double x, double y, boolean ignoreHeight, double bottomHeight, double topHeight, Game game, boolean allowMovementWhenPlantingOrDefusingBomb) {
		if (!allowMovementWhenPlantingOrDefusingBomb && (getBomb() != null && getBomb().isPlanting() || isDefusingBomb())) {
			return false;
		} else if (!game.canPlayersMove()) {
			return false;
		} else if (game.getMap().inGrid(x, y, getPhysicalHalfWidth())) {
			Area newLocation = new Area(new Ellipse2D.Double(x - getPhysicalHalfWidth(), y - getPhysicalHalfWidth(), getPhysicalHalfWidth() * 2, getPhysicalHalfWidth() * 2));
			double stepHeight = isJumping() || isFalling() ? 0 : getStepHeight();
			if (isSpectator()) {
				return true;
			} else if (ignoreHeight || game.getMap().doesCylinderIntersectWall(newLocation, bottomHeight, topHeight - bottomHeight, stepHeight) == null) {
				if (ignoreHeight || game.getMap().doesCylinderIntersectCeiling(newLocation, bottomHeight, topHeight - bottomHeight, stepHeight) == null) {
					newLocation.intersect(game.getLayoutOfPlayers(this, false, bottomHeight, topHeight));
					return (ignoreHeight || !game.getMap().doesCylinderPathIntersectFence(getLocationX(), getLocationY(), x, y, getPhysicalHalfWidth(), bottomHeight, topHeight, stepHeight)) && newLocation.isEmpty();
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if the player is currently jumping.
	 * @return true if the player is jumping; otherwise, false
	 */
	public boolean isJumping() {
		return this.jumpStartTime != null && getJumpHeight() > 0;
	}

	/**
	 * Checks if the player is currently falling.
	 * @return true if the player is falling; otherwise, false
	 */
	public boolean isFalling() {
		return this.fallStartTime != null && this.fallFromHeight > getFloorHeight();
	}
	
	/**
	 * Attempt to stand.
	 * @return true if the player was able to stand; otherwise, false
	 */
	public boolean stand() {
		if (getMap().doesCylinderIntersectWall(getBase(), getBottomHeight(), getDefaultHeight()) == null && getMap().doesCylinderIntersectCeiling(getBase(), getBottomHeight(), getDefaultHeight()) == null && getMap().doesCylinderIntersectFence(getLocationX(), getLocationY(), getPhysicalHalfWidth(), getBottomHeight(), getDefaultHeight()) == null && isMoveableTo(getLocationX(), getLocationY(), false, false)) {
			synchronized (this) {
				this.crouchLevel = null;
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Attempt to crouch. The player can only crouch if the player is not jumping.
	 * @param changeInTime the time taken to move in milliseconds
	 * @see #isJumping()
	 */
	public void crouch(double changeInTime) {
		if (getMap().isNearLadder(this)) {
			synchronized (this) {
				this.ladderHeight = Math.max(this.ladderHeight + Math.sin(-Math.PI / 2) * getSpeed() * (changeInTime / 1000d), 0);
			}
		} else if (!isJumping() && getGame().canPlayersMove() && getMap().doesCylinderIntersectWall(getBase(), getBottomHeight(), getDefaultHeight() * CrouchLevel.CROUCH.HEIGHT_MULTIPLIER) == null &&
				getMap().doesCylinderIntersectCeiling(getBase(), getBottomHeight(), getDefaultHeight() * CrouchLevel.CROUCH.HEIGHT_MULTIPLIER) == null &&
				getMap().doesCylinderIntersectFence(getLocationX(), getLocationY(), getPhysicalHalfWidth(), getBottomHeight(), getDefaultHeight() * CrouchLevel.CROUCH.HEIGHT_MULTIPLIER) == null) {
			synchronized (this) {
				this.crouchLevel = CrouchLevel.CROUCH;
			}
		}
	}
	
	/**
	 * Attempt to stand, then jump. The player can only jump if the player is not falling or jumping.
	 * @param changeInTime the time taken to move in milliseconds
	 * @see #isJumping()
	 * @see #isFalling()
	 */
	public void jump(double changeInTime) {
		stand();
		if (getMap().isNearLadder(this)) {
			synchronized (this) {
				this.ladderHeight = Math.max(this.ladderHeight + Math.sin(Math.PI / 2) * getSpeed() * (changeInTime / 1000d), 0);
			}
		} else if (!isJumping() && !isFalling() && getCrouchLevel() == null && (getBomb() == null || !getBomb().isPlanting()) && !isDefusingBomb() && getGame().canPlayersMove()) {
			synchronized (this) {
				this.jumpFromHeight = getFloorHeight();
				this.jumpStartTime = System.currentTimeMillis();
			}
		}
	}
	
	/**
	 * Requests the player to fall to the next lowest possible height.
	 * @param fromHeight the at which the player falls from
	 */
	public void fall(double fromHeight) {
		if (fromHeight > 0) {
			synchronized (this) {
				this.fallFromHeight = fromHeight;
				this.fallStartTime = System.currentTimeMillis();
			}
		}
	}
	
	/**
	 * Finds the location that the player will be if {@link #move(double, double, boolean)} was to be called instead with the same arguments.
	 * @param changeInTime the time taken to move in milliseconds
	 * @param angle the angle, in radians, starting at {@link #getHorizontalDirection()} as 0 counter-clockwise
	 * @param allowMovementWhenPlantingBomb {@code true} if the player can move when planting a bomb, otherwise {@code false}
	 * @return the location that the player would have been
	 */
	public Point2D.Double nextLocation(double changeInTime, double angle, boolean allowMovementWhenPlantingBomb) {
		angle = (angle % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		double speed = getSpeed();
		double tempPlayerX = getLocationX() + Math.cos(getHorizontalDirection() + angle) * speed * (changeInTime / 1000d);
		double tempPlayerY = getLocationY() - Math.sin(getHorizontalDirection() + angle) * speed * (changeInTime / 1000d);
		if (isMoveableTo(tempPlayerX, tempPlayerY, allowMovementWhenPlantingBomb)) {
			return new Point2D.Double(tempPlayerX, tempPlayerY);
		} else if (isMoveableTo(getLocationX(), tempPlayerY, allowMovementWhenPlantingBomb)) {
			return new Point2D.Double(getLocationX(), tempPlayerY);
		} else if (isMoveableTo(tempPlayerX, getLocationY(), allowMovementWhenPlantingBomb)) {
			return new Point2D.Double(tempPlayerX, getLocationY());
		}
		return new Point2D.Double(getLocationX(), getLocationY());
	}
	
	/**
	 * Moves the player spectator height one unit up or down vertically.
	 * @param changeInTime the time taken to move in milliseconds
	 * @param upOrDown {@code true} if the player is moving up; otherwise {@code down}
	 */
	public void moveSpectatorHeight(double changeInTime, boolean upOrDown) {
		double speed = getSpeed();
		setSpectatorHeight(getSpectatorHeight() + (upOrDown ? 1 : -1) * speed * (changeInTime / 1000d));
	}
	
	/**
	 * Move the player one unit in the angle relative to the direction that the player is facing
	 * @param changeInTime the time taken to move in milliseconds
	 * @param angle the angle, in radians, starting at {@link #getHorizontalDirection()} as 0 counter-clockwise
	 * @param allowMovementWhenPlantingBomb {@code true} if the player can move when planting a bomb, otherwise {@code false}
	 * @return {@code true} if the location of the player in the x-y plane changed or the spectator height; otherwise, {@code false}
	 */
	public boolean move(double changeInTime, double angle, boolean allowMovementWhenPlantingBomb) {
		angle = (angle % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		boolean moved = false;
		double speed = getSpeed();
		double tempPlayerX = getLocationX() + (isSpectator() ? Math.cos(getVerticalDirection()) : 1) * Math.cos(getHorizontalDirection() + angle) * speed * (changeInTime / 1000d);
		double tempPlayerY = getLocationY() - (isSpectator() ? Math.cos(getVerticalDirection()) : 1) * Math.sin(getHorizontalDirection() + angle) * speed * (changeInTime / 1000d);
		double previousFloorHeight = getFloorHeight();
		if (isMoveableTo(tempPlayerX, tempPlayerY, allowMovementWhenPlantingBomb)) {
			setLocation(tempPlayerX, tempPlayerY);
			moved = true;
		} else if (isMoveableTo(getLocationX(), tempPlayerY, allowMovementWhenPlantingBomb)) {
			setLocation(getLocationX(), tempPlayerY);
			moved = true;
		} else if (isMoveableTo(tempPlayerX, getLocationY(), allowMovementWhenPlantingBomb)) {
			setLocation(tempPlayerX, getLocationY());
			moved = true;
		}
		if (isSpectator()) {
			setSpectatorHeight(getSpectatorHeight() + Math.cos(angle) * Math.sin(getVerticalDirection()) * speed * (changeInTime / 1000d));
			return moved;
		}
		if (getFloorHeight() < previousFloorHeight) {
			fall(previousFloorHeight);
		}
		if (getMap().isNearLadder(this)) {
			synchronized (this) {
				this.ladderHeight = Math.max(this.ladderHeight + Math.sin(getVerticalDirection()) * speed * (changeInTime / 1000d), 0);
			}
		} else if (getLadderHeight() > 0) {
			double previousLadderHeight = getLadderHeight();
			synchronized (this) {
				this.ladderHeight = 0;
			}
			fall(previousFloorHeight + previousLadderHeight);
		}

        if (Math.floor(this.distanceTravelled / PLAYER_STEP_SIZE) > getSteps()) {
        	this.steps = (int) Math.floor(this.distanceTravelled / PLAYER_STEP_SIZE);
        	if (STEP_SOUND_FILES.size() > 0 && getCrouchLevel() == null && !isJumping() && !isFalling()) {
        		ProjectionPlane.playSoundFile(STEP_SOUND_FILES.get(this.steps % STEP_SOUND_FILES.size()), Map.findDistance2D(this, ProjectionPlane.getSingleton().getPlayer()));
        	}
        }
		return moved;
	}
	
	/**
	 * Attempts to move the player using its movement controller.
	 * @param timePassed the time in milliseconds that passed during the movement
	 */
	public void moveWithController(long timePassed) {
		if (MOVEMENT_CONTROLLER != null) {
			MOVEMENT_CONTROLLER.movePlayer(this, timePassed);
		}
	}
	
	public int getNumberOfItemsInSlot(HoldItem.HoldItemSlot slot) {
		int count = 0;
		if (slot != null) {
			if (slot.equals(Bomb.getBombHoldSlot()) && getBomb() != null) {
				count++;
			}
			for (HoldItem i : getCarryItems()) {
				if (slot.equals(i.getHoldSlot())) {
					count++;
				}
			}
		}
		return count;
	}
	
	/**
	 * Determines if the player will pick up an item after walking past it.
	 * @param item the item that may be picked up
	 * @return true if the item will be picked up; otherwise, false
	 */
	public boolean willPickUp(DroppedItem item) {
		if (item == null) {
			return false;
		} else if (item.getItem() != null && item.getItem().getHoldSlot() != null && item.getItem().getHoldSlot().getCapacity() != null) {
			return getNumberOfItemsInSlot(item.getItem().getHoldSlot()) < item.getItem().getHoldSlot().getCapacity();
		}
		return true;
	}
	
	/**
	 * Gets the previously item that was the main hold item.
	 * @return the previous main hold item
	 */
	public HoldItem getLastHoldItem() {
		return this.lastHoldItem;
	}
	
	/**
	 * Gets the current item held by the player.
	 * @return the current item held by the player
	 */
	public HoldItem getMainHoldItem() {
		return this.mainHoldItem;
	}
	
	/**
	 * Sets the item to be held by the player. The new item must also be carried by the player.
	 * @param holdItem the item to be held by the player
	 * @return true if the player successfully switched items; otherwise, false
	 */
	public boolean setMainHoldItem(HoldItem holdItem) {
		if (getMainHoldItem() != null) {
			getMainHoldItem().itemSwitched(this);
		}
		if (holdItem == null || (holdItem.equals(getBomb())) || isCarrying(holdItem)) {
			synchronized (this) {
				HoldItem mainHoldItem = getMainHoldItem();
				if (mainHoldItem != null && !mainHoldItem.equals(getLastHoldItem())) {
					this.lastHoldItem = mainHoldItem;
				}
				this.mainHoldItem = holdItem;
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Drops an item currently carried by the player.
	 * @param holdItem the item to be dropped
	 */
	public void dropCarryItem(HoldItem holdItem) {
		if (holdItem != null && holdItem.isDropable(this)) {
			if (holdItem.getDropItem() != null) {
				HoldItem lastItem = getLastHoldItem();
				HoldItem mainHoldItem = getMainHoldItem();
				removeCarryItems(holdItem);
				holdItem.getDropItem().drop(this);
				if (lastItem != null && holdItem.equals(mainHoldItem)) {
					setMainHoldItem(lastItem);
				} else {
					for (HoldItem i : getCarryItems()) {
						if (i != null) {
							setMainHoldItem(i);
							break;
						}
					}
				}
			}
		}
	}
	
	public void dropDefuseKit() {
		if (hasDefuseKit() && getDefuseKit().getDroppedItem() != null) {
			DroppedItem droppedItem = getDefuseKit().getDroppedItem();
			droppedItem.drop(this);
			setDefuseKit(null);
		}
	}
	
	/**
	 * Finds the first item in a slot and sets it as the main hold item. If a {@code null} slot is used, the first item will be used.
	 * Note: The search is independent of the current main hold item.
	 * @param slot the slot to be searched
	 * @return {@code true} if a new item was set; otherwise {@code false}
	 * @see #setItemInSlotAsMainHoldItem(HoldItem.HoldItemSlot, HoldItem)
	 */
	public boolean setItemInSlotAsMainHoldItem(HoldItem.HoldItemSlot slot) {
		return setItemInSlotAsMainHoldItem(slot, null);
	}
	
	/**
	 * Finds an item in a slot and sets it as the main hold item. If a {@code null} slot is used, every item will be treated as being part of the slot. If the slot of {@code beforeItem}
	 * is not in the searched for slot, the before item is treated as {@code null}.
	 * Note: The search ignores the tactical shield.<br>
	 * Note: The search is independent of the current main hold item.
	 * @param slot the slot to be searched
	 * @param beforeItem an item that should found before the next item that meets the criteria should be set as the main hold item or {@code null} if the first item to be found should be used.
	 * @return {@code true} if a new item was set; otherwise {@code false}
	 */
	public boolean setItemInSlotAsMainHoldItem(HoldItem.HoldItemSlot slot, HoldItem beforeItem) {
		if (slot != null || beforeItem != null || getNumberOfCarryItems() > 0) {
			boolean beforeItemFound = beforeItem == null || (slot != null && !slot.equals(beforeItem.getHoldSlot()));
			if (getBomb() != null && (slot == null || slot.equals(Bomb.getBombHoldSlot()))) {
				if (beforeItemFound) {
					setMainHoldItem(getBomb());
					return true;
				} else if (getBomb().equals(beforeItem)) {
					beforeItemFound = true;
				}
			}
			for (HoldItem i : getCarryItems()) {
				if (slot == null || slot.equals(i.getHoldSlot())) {
					if (beforeItemFound) {
						setMainHoldItem(i);
						return true;
					} else if (i.equals(beforeItem)) {
						beforeItemFound = true;
					}
				}
			}
			if (beforeItemFound) {
				return setItemInSlotAsMainHoldItem(slot);
			}
		}
		return false;
	}
	
	public HoldItem.HoldItemSlot[] getCurrentHoldItemSlots() {
		Set<HoldItem.HoldItemSlot> slots = new LinkedHashSet<HoldItem.HoldItemSlot>();
		if (getBomb() != null) {
			slots.add(Bomb.getBombHoldSlot());
		}
		for (HoldItem i : getCarryItems()) {
			slots.add(i.getHoldSlot());
		}
		return slots.toArray(new HoldItem.HoldItemSlot[slots.size()]);
	}
	
	/**
	 * Gets all items currently being carried by the player. This does not include the tactical shield or the bomb.
	 * @return the items carried by the player
	 */
	public HoldItem[] getCarryItems() {
		return CARRY_ITEMS.toArray(new HoldItem[CARRY_ITEMS.size()]);
	}
	
	private boolean addItem(HoldItem item) {
		if (item != null) {
			if (!item.canHoldTacticalShield()) {
				setTacticalShield(null);
			}
			HoldItem.HoldItemSlot slot = item.getHoldSlot();
			if (slot != null && slot.getCapacity() != null) {
				int slotAmount = 0;
				if (slot.equals(Bomb.getBombHoldSlot()) && getBomb() != null) {
					slotAmount++;
				}
				for (HoldItem h : getCarryItems()) {
					if (slot.equals(h.getHoldSlot())) {
						if (++slotAmount >= slot.getCapacity()) {
							removeCarryItems(h);
							if (h.getDropItem() != null) {
								h.getDropItem().drop(this);
							}
						}
					}
				}
			}
			if (item instanceof TacticalShield) {
				setTacticalShield((TacticalShield) item);
			} else if (item instanceof Bomb) {
				setBomb((Bomb) item, false);
			} else {
				CARRY_ITEMS.add(item);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Add items to be carried by the player and sets the last item added as the new item held.
	 * @param holdItems the items to be carried by the player
	 */
	public void addCarryItems(HoldItem... holdItems) {
		if (holdItems != null && holdItems.length > 0) {
			HoldItem lastItem = null;
			for (HoldItem h : holdItems) {
				if (addItem(h)) {
					lastItem = h;
				}
			}
			if (CARRY_ITEMS.size() > 0 && lastItem != null) {
				setMainHoldItem(lastItem);
			}
		}
	}
	
	/**
	 * Remove items currently being carried by the player.
	 * @param holdItems the items to be removed
	 */
	public void removeCarryItems(HoldItem... holdItems) {
		if (holdItems != null) {
			for (HoldItem h : holdItems) {
				if (h instanceof Gun) {
					Gun g = (Gun) h;
					if (g.allowClickZoom() && g.hasZoom()) {
						g.setZoomLevel(null);
						setZoom(1);
					}
				}
				if (getBomb() != null && getBomb().equals(h)) {
					setBomb(null, false);
				}
				synchronized (CARRY_ITEMS) {
					CARRY_ITEMS.remove(h);
				}
			}
			if (!CARRY_ITEMS.contains(getMainHoldItem())) {
				setMainHoldItem(null);
			}
			if (CARRY_ITEMS.size() == 1 || (getMainHoldItem() == null && CARRY_ITEMS.size() > 0)) {
				setMainHoldItem(CARRY_ITEMS.get(0));
			}
		}
	}
	
	/**
	 * Removes all items carried by the player
	 */
	public void removeAllCarryItems() {
		synchronized (CARRY_ITEMS) {
			CARRY_ITEMS.clear();
		}
		synchronized (this) {
			this.mainHoldItem = null;
		}
		setTacticalShield(null);
		setBomb(null, false);
		setDefuseKit(null);
	}
	
	private void dropAllCarryItems(HoldItem.HoldItemSlot exceptionSlot) {
		ArrayList<HoldItem> items = new ArrayList<HoldItem>(CARRY_ITEMS);
		for (HoldItem i : items) {
			if (exceptionSlot == null || !exceptionSlot.equals(i.getHoldSlot())) {
				dropCarryItem(i);
			}
		}
		setTacticalShield(null);
		if (exceptionSlot == null || !exceptionSlot.equals(Bomb.getBombHoldSlot())) {
			setBomb(null, true);
		}
		dropDefuseKit();
	}
	
	/**
	 * Checks if an item is currently being carried by the player.
	 * @param holdItem the item to be checked
	 * @return true if the item is currently being carried; otherwise, false
	 */
	public boolean isCarrying(HoldItem holdItem) {
		return CARRY_ITEMS.contains(holdItem);
	}
	
	/**
	 * Gets the number of items being carried by the player.
	 * @return the number of items being carried
	 */
	public int getNumberOfCarryItems() {
		return CARRY_ITEMS.size();
	}
	
	/**
	 * Switches the item being held to the previous item.
	 * @return the new item that is held.
	 */
	public HoldItem setPreviousMainItem() {
		if (getNumberOfCarryItems() > 1) {
			if (CARRY_ITEMS.get(0).equals(getMainHoldItem())) {
				setMainHoldItem(CARRY_ITEMS.get(getNumberOfCarryItems() - 1));
				return CARRY_ITEMS.get(getNumberOfCarryItems() - 1);
			} else {
				for (int i = 1; i < getNumberOfCarryItems(); i++) {
					if (CARRY_ITEMS.get(i).equals(getMainHoldItem())) {
						setMainHoldItem(CARRY_ITEMS.get(i - 1));
						return CARRY_ITEMS.get(i - 1);
					}
				}
			}
		}
		return getMainHoldItem();
	}

	/**
	 * Switches the item being held to the next item.
	 * @return the new item that is held.
	 */
	public HoldItem setNextMainItem() {
		if (getNumberOfCarryItems() > 1) {
			if (CARRY_ITEMS.get(getNumberOfCarryItems() - 1).equals(getMainHoldItem())) {
				setMainHoldItem(CARRY_ITEMS.get(0));
				return CARRY_ITEMS.get(0);
			} else {
				for (int i = 0; i < getNumberOfCarryItems() - 1; i++) {
					if (CARRY_ITEMS.get(i).equals(getMainHoldItem())) {
						setMainHoldItem(CARRY_ITEMS.get(i + 1));
						return CARRY_ITEMS.get(i + 1);
					}
				}
			}
		}
		return getMainHoldItem();
	}
	
	/**
	 * Gets the tactical shield currently used by the player. If no tactical shield is currently being used by the player, returns {@code null}.
	 * @return the tactical shield currently used by the player
	 */
	public TacticalShield getTacticalShield() {
		return this.tacticalShield;
	}
	
	Line2D getCurrentTacticalShieldBaseLocation() {
		if (getTacticalShield() != null) {
			double shieldAngle = getHorizontalDirection() - getTacticalShield().getCurrentShieldAngle();
			double xMid = getLocationX() + Math.cos(shieldAngle) * (getPhysicalHalfWidth() + getTacticalShield().getShieldHoldDistance());
			double yMid = getLocationY() - Math.sin(shieldAngle) * (getPhysicalHalfWidth() + getTacticalShield().getShieldHoldDistance());
			
			double x1 = xMid + Math.cos(shieldAngle + Math.PI / 2) * getTacticalShield().getShieldWidth() / 2;
			double y1 = yMid - Math.sin(shieldAngle + Math.PI / 2) * getTacticalShield().getShieldWidth() / 2;
			double x2 = xMid + Math.cos(shieldAngle - Math.PI / 2) * getTacticalShield().getShieldWidth() / 2;
			double y2 = yMid - Math.sin(shieldAngle - Math.PI / 2) * getTacticalShield().getShieldWidth() / 2;
			return new Line2D.Double(x1, y1, x2, y2);
		}
		return null;
	}
	
	/**
	 * Sets the tactical shield to be used by the player. Removes any weapons occupying the tactical shield slot. Drops the previous shield at the current location if one exists.
	 * @param tacticalShield the tactical shield to be used used by the player
	 */
	public void setTacticalShield(TacticalShield tacticalShield) {
		TacticalShield previousShield = getTacticalShield();
		this.tacticalShield = tacticalShield;
		if (getTacticalShield() != null) {
			ArrayList<HoldItem> itemsToRemove = new ArrayList<HoldItem>();
			for (HoldItem item : getCarryItems()) {
				if (!item.canHoldTacticalShield()) {
					itemsToRemove.add(item);
				}
			}
			if (getBomb() != null) {
				itemsToRemove.add(getBomb());
			}
			for (HoldItem item : itemsToRemove) {
				dropCarryItem(item);
			}
		}
		if (previousShield != null && !previousShield.equals(getTacticalShield())) {
			previousShield.getDroppedShield().drop(this);
		}
	}
	
	/**
	 * Checks if the player can plant a bomb at the current time and location.
	 * @return {@code true} if the player can plant a bomb; otherwise {@code false}
	 */
	public boolean canPlantBomb() {
		return getGame().canPlayersMove() && getMap().isBombSite(getLocationX(), getLocationY(), getViewHeight());
	}
	
	/**
	 * Gets the bomb currently carried by the player. If no bomb is currently being carried by the player, returns {@code null}.
	 * @return the bomb currently carried by the player
	 */
	public Bomb getBomb() {
		return this.bomb;
	}
	
	/**
	 * Sets the bomb to be carried by the player.
	 * @param bomb the bomb to be carried by the player
	 * @param dropBomb {@code true} to drop the previous bomb if one exists; otherwise {@code false}
	 */
	public void setBomb(Bomb bomb, boolean dropBomb) {
		Bomb previousBomb = getBomb();
		this.bomb = bomb;
		if (getBomb() != null && !getBomb().canHoldTacticalShield()) {
			setTacticalShield(null);
		}
		if (dropBomb && previousBomb != null && !previousBomb.equals(getBomb())) {
			previousBomb.getDropItem().drop(this);
		}
	}
	
	/**
	 * Checks if the player has a defuse kit.
	 * @return {@code true} if the player has a defuse kit; otherwise, {@code false}
	 */
	public boolean hasDefuseKit() {
		return getDefuseKit() != null;
	}
	
	/**
	 * Gets the defuse kit of the player.
	 * @return the defuse kit
	 */
	public DefuseKit getDefuseKit() {
		return this.defuseKit;
	}
	
	/**
	 * Sets the defuse kit of the player.
	 * @param defuseKit the defuse kit
	 */
	public synchronized void setDefuseKit(DefuseKit defuseKit) {
		this.defuseKit = defuseKit;
	}
	
	/**
	 * Checks if the tactical shield is in the deployed position
	 * @return true if the tactical shield is in the deployed position; otherwise, false
	 */
	public boolean isTacticalShieldDeployed() {
		return getTacticalShield() != null && getTacticalShield().isDeployed();
	}
	
	/**
	 * Gets the start time of the most recent flash caused by a flash bang.
	 * @return the start time of the most recent flash
	 */
	long getStartFlashTime() {
		return this.startFlashTime;
	}

	/**
	 * Gets the duration of the most recent flash caused by a flash bang in milliseconds.
	 * @return the duration of the most recent flash
	 */
	long getFlashDuration() {
		return this.flashDuration;
	}
	
	/**
	 * Gets the cached floor height of the player. The floor height is the height of the highest floor under the player.
	 * @return the cached floor height
	 */
	public double getFloorHeight() {
		return this.lastFloorHeight;
	}
	
	/**
	 * Gets and updates the cache floor height of the player. The floor height is the height of the highest floor under the player.
	 * @return the updated floor height
	 * @see #updatePosition()
	 */
	public double getAndUpdateFloorHeight() {
		Double h = getGame().getHighestPlayerUnderPlayer2DBase(this, false, this.lastFloorHeight + getFallHeight() + getJumpHeight());
		this.lastFloorHeight = getUpdatedFloorHeight() + (h != null ? h : 0);
		return this.lastFloorHeight;
	}
	
	private double getHighestHeightAtBase() {
		double height = Math.max(getMap().getHighestWallInArea(getBase()), getMap().getHighestFenceInCircle(getLocationX(), getLocationY(), getPhysicalHalfWidth()));
		height = Math.max(height, getMap().getHighestCeilingInArea(getBase()));
		return height;
	}
	
	/**
	 * Sets the player's floor height to the highest possible height at the current location.
	 */
	public void sendToHighestFloorHeight() {
		this.lastFloorHeight = getHighestHeightAtBase();
		updatePosition();
	}
	
	/**
	 * Gets the current floor height of the player without modifying the cached floor height. The floor height is the height of the highest floor under the player.
	 * @return  the current floor height of the player
	 */
	public double getUpdatedFloorHeight() {
		return getMap().getHighestOccupiableCylinderHeight(getBase(), getLocationX(), getLocationY(), getPhysicalHalfWidth(), getBottomHeight() + getStepHeight());
	}
	
	/**
	 * Gets the height above the floor height caused by jumping.
	 * @return the height caused by jumping
	 */
	public double getJumpHeight() {
		if (this.jumpStartTime == null) {
			return 0;
		}
		double acceleration = getMap().getGravity() / 1000000;
		double initialVelocity = Math.sqrt(-2 * acceleration * getMaxJumpHeight() * getMovementMultiplier());
		if (this.jumpStartTime == null) {
			return 0;
		}
		long timePassed = System.currentTimeMillis() - this.jumpStartTime;
		double height = this.jumpFromHeight - getFloorHeight() + initialVelocity * timePassed + acceleration * timePassed * timePassed / 2;
		if (initialVelocity + acceleration * timePassed <= 0) {
			synchronized (this) {
				this.jumpStartTime = null;
			}
			fall(getFloorHeight() + height);
			return getFloorHeight() + height;
		} else {
			height = Math.max(height, 0);
			if (this.lastLowestBottomHeightAbove != null && getFloorHeight() + height + getPhysicalHeight() + getLadderHeight() > this.lastLowestBottomHeightAbove) {
				synchronized (this) {
					this.jumpStartTime = null;
				}
				fall(getFloorHeight() + height);
				return 0;
			}
			return height;
		}
	}

	/**
	 * Gets the height above the floor height caused by falling.
	 * @return the height caused by falling
	 */
	public double getFallHeight() {
		if (!isFalling() || getMap() == null) {
			return 0;
		} else {
			if (isJumping()) {
				return 0;
			} else {
				double acceleration = getMap().getGravity() / 1000000;
				long timePassed = System.currentTimeMillis() - this.fallStartTime;
				double height = this.fallFromHeight - getFloorHeight() + acceleration * timePassed * timePassed / 2;
				if (height < 0) {
					if (isAllowFallDamage()) {
						setHealth(getHealth() - (getFallDamageCalculator() != null ? getFallDamageCalculator().getFallDamage(this.fallFromHeight - getFloorHeight()) : 0), this, CauseOfDeath.FALL_DEATH, false);
						if (getFallDamageCalculator() != null && getFallDamageCalculator().getFallDamage(this.fallFromHeight - getFloorHeight()) > 0) {
							addAttackEvents(new AttackEvent(Math.PI - getHorizontalDirection()));
						}
					}
					this.fallStartTime = null;
				}
				return Math.max(height, 0);
			}
		}
	}

	/**
	 * The physical height of the player based on the current stance of the player.
	 * @return the physical height of the player based on the current stance
	 * @see #getDefaultHeight()
	 */
	@Override
	public double getPhysicalHeight() {
		if (getCrouchLevel() == CrouchLevel.CROUCH) {
			return getDefaultHeight() / 2d;
		} else {
			return getDefaultHeight();
		}
	}
	
	public double getLadderHeight() {
		return this.ladderHeight;
	}
	
	/**
	 * Gets the stance of the player. A {@code null} value implies a standing position.
	 * @return the stance of the player
	 */
	public CrouchLevel getCrouchLevel() {
		return this.crouchLevel;
	}

	@Override
	public double getBottomHeight() {
		return getFloorHeight() + getFallHeight() + getJumpHeight() + getLadderHeight();
	}

	/**
	 * Gets the height of the highest point of the player.
	 * @return the bottom height of the player
	 */
	public double getTopHeight() {
		return getBottomHeight() + getPhysicalHeight();
	}

	/**
	 * Gets the view height of the player in meters. This is the height at which the player observes the world (i.e. eye-level).
	 * @return the view height of the player
	 */
	public double getViewHeight() {
		return isSpectator() ? getSpectatorHeight() : getTopHeight() + (getSightHeight() * getPhysicalHeight());
	}
	
	/**
	 * Gets the view height as a spectator in meters.
	 * @return the spectator height of the player
	 */
	public double getSpectatorHeight() {
		return this.spectatorHeight;
	}
	
	/**
	 * Sets the spectator height in meters
	 * @param spectatorHeight the new spectator height
	 */
	public void setSpectatorHeight(double spectatorHeight) {
		this.spectatorHeight = Math.max(spectatorHeight, 0);
	}

	/**
	 * Gets the height of the player in meters independent of the current stance.
	 * @return the height of the player in meters
	 * @see #getPhysicalHeight()
	 */
	public double getDefaultHeight() {
		return super.getPhysicalHeight();
	}
	
	public Map getMap() {
		return getGame().getMap();
	}

	/**
	 * Gets the current game that the player is in
	 * @return the game that the player is in
	 */
	public Game getGame() {
		return this.game;
	}

	/**
	 * Sets the game that the player will be in. If the game refuses to add the player, the player will not have a game.
	 * @param game the new game that the player will be in
	 */
	public void setGame(Game game) {
		Game previousGame = getGame();
		this.game = game;
		if (previousGame != null) {
			previousGame.removePlayers(this);
			removePlayerListeners(previousGame);
		}
		if (getGame() != null && !getGame().hasPlayer(this)) {
			if (getGame().addPlayer(this)) {
				addPlayerListeners(getGame());
				this.lastFloorHeight = Double.MAX_VALUE;
				updatePosition();
			} else {
				this.game = null;
			}
		}
	}
	
	/**
	 * Gets the multiplier for movement after taking into account crouch position, objects held, etc...
	 * @return  the multiplier for movement
	 */
	public double getMovementMultiplier() {
		return (getCrouchLevel() != null ? getCrouchLevel().getSpeedMultiplier() : 1) * (getMainHoldItem() != null ? getMainHoldItem().getSpeedMultiplier() : 1) * (getTacticalShield() != null ? getTacticalShield().getSpeedMultiplier() : 1);
	}
	
	/**
	 * Gets the default speed of the player in meters per second.
	 * @return the default speed of the player
	 */
	public double getDefaultSpeed() {
		return this.speed;
	}
	
	/**
	 * Gets the current speed of the player in meters per second taking into account the current conditions such as crouch position, items held, muscles, etc...
	 * @return the modified current speed of the player
	 */
	public double getSpeed() {
		return getDefaultSpeed() * getMovementMultiplier() * (getMainHoldItem() != null ? getMainHoldItem().getSpeedMultiplier() : 1) * (getTacticalShield() != null ? getTacticalShield().getSpeedMultiplier() : 1) * (hasMuscles() ? MUSCLE_SIZE : 1);
	}
	
	/**
	 * Sets the speed of the player in meters per second. The minimum speed is 0.
	 * @param speed the new speed of the player
	 */
	public synchronized void setSpeed(double speed) {
		this.speed = Math.max(speed, 0);
	}
	
	/**
	 * Gets the step height (in meters) of the player. The step height is the height difference the the player can move without the need to jump.
	 * @return the step height of the player
	 */
	public double getStepHeight() {
		return this.stepHeight;
	}
	
	/**
	 * Sets the step height (in meters) of the player. The step height is the height difference the the player can move without the need to jump.
	 * @param stepHeight the step height of the player
	 */
	public synchronized void setStepHeight(double stepHeight) {
		this.stepHeight = stepHeight;
	}

	/**
	 * Gets the maximum height (in meters) that the player can jump.
	 * @return the maximum jump height of the player
	 */
	public double getDefaultMaxJumpHeight() {
		return this.maxJumpHeight;
	}

	/**
	 * Gets the maximum height (in meters) that the player can jump including additional multipliers from muscles.
	 * @return the maximum jump height of the player
	 */
	public double getMaxJumpHeight() {
		return getDefaultMaxJumpHeight() * (hasMuscles() ? MUSCLE_SIZE : 1);
	}

	/**
	 * Sets the maximum height (in meters) that the player can jump.
	 * @param maxJumpHeight the maximum jump height of the player
	 */
	public synchronized void setMaxJumpHeight(double maxJumpHeight) {
		this.maxJumpHeight = maxJumpHeight;
	}
	
	/**
	 * Gets the sight height of the player in meters.<br>
	 * The sight height is the height difference relative to the top of the player.
	 * It is the height relative to physicalHeight of where the player sees(i.e. eye-level). A sightHeight of 0 representing seeing at the top of the player, a typical sight height is -0.1 of the physical height.
	 * The sight height is expressed as the proportion of the player's physical height above the physical height (e.x. -1 is the bottom of the player, 1 is twice the player's height, -0.5 is halfway up the player)
	 * @return the sight height of the player
	 */
	public double getSightHeight() {
		return SIGHT_HEIGHT;
	}
	
	/**
	 * Gets the damage that the player will inflict when in contact with other players.
	 * @return the damage that the player will inflict when in contact with other players
	 */
	public int getTouchDamage() {
		return TOUCH_DAMAGE;
	}
	
	/**
	 * Checks if the player is affected by damage caused by contact with other players.
	 * @return true if the player is affected by damage caused by contact with other players; otherwise, false
	 */
	public boolean isSusceptibleToTouchDamage() {
		return SUSCEPTIBLE_TO_TOUCH_DAMAGE;
	}
	
	/**
	 * Checks if the player is alive (health is above 0).
	 * @return true if the player is alive; otherwise, false
	 * @see #getHealth()
	 */
	public boolean isAlive() {
		return getHealth() > 0;
	}
	
	/**
	 * Gets the current health level of the player.
	 * @return the current health level of the player
	 */
	public int getHealth() {
		return this.health;
	}
	
	/**
	 * Gets the maximum health level of the player.
	 * @return the maximum health level of the player
	 */
	public int getMaxHealth() {
		return MAX_HEALTH;
	}
	
	/**
	 * Sets the health of the player. If the new health level is less than or equal to 0, the player is terminated.
	 * @param health the new health level of the player
	 */
	public void setHealth(int health) {
		setHealth(health, null, null, false);
	}
	
	private void setHealth(int health, Player killer, CauseOfDeath cause, boolean isHeadShot) {
		health = Math.min(health, getMaxHealth());
		this.health = health;
		if (!isAlive() && getGame() != null) {
			this.deaths++;
			resetJumpAndFall();
			setZoom(1);
			setSpectatorHeight(getHighestHeightAtBase());
			if (getTeam() != null) {
				playTerminationSound();
			}
			if (killer != null && !equals(killer)) {
				if (killer.getTeam() != null && killer.getTeam().equals(getTeam())) {
					synchronized (killer) {
						killer.kills--;
					}
				} else {
					synchronized (killer) {
						killer.kills++;
					}
				}
			}
			getGame().addRecentKillEvents(new Game.KillEvent(killer, cause, isHeadShot, this));
			getGame().playerTerminated(this);
		}
	}
	
	public void playTerminationSound() {
		ProjectionPlane.playSoundFile(TERMINATION_SOUND_FILE, Map.findDistance2D(this, ProjectionPlane.getSingleton().getPlayer()));
	}
	
	/**
	 * Gets the current armor level of the player.
	 * @return the current armor level of the player
	 */
	public int getArmor() {
		return this.armor;
	}
	
	/**
	 * Gets the maximum armor level of the player.
	 * @return the maximum armor level of the player
	 */
	public int getMaxArmor() {
		return MAX_ARMOR;
	}
	
	/**
	 * Gets the armor level of the player.
	 * @param armor the new armor level of the player
	 */
	public synchronized void setArmor(int armor) {
		armor = Math.max(Math.min(armor, getMaxArmor()), 0);
		this.armor = armor;
		if (getArmor() <= 0) {
			setHelmet(false);
		}
	}
	
	/**
	 * Tests if the player currently has a helmet.
	 * @return {@code true} if the player has a helmet; otherwise, {@code false}
	 */
	public boolean hasHelmet() {
		return this.hasHelmet;
	}
	
	/**
	 * Sets the helmet of the player.
	 * @param hasHelmet {@code true} if the player is to have a helmet; otherwise {@code false}
	 */
	public synchronized void setHelmet(boolean hasHelmet) {
		this.hasHelmet = hasHelmet;
	}
	
	/**
	 * Gets the amount of money the player currently owns.
	 * @return the amount of money the player currently owns
	 */
	public int getMoney() {
		return this.money;
	}
	
	/**
	 * Sets the amount of money that the player will own.
	 * <br><br>
	 * Note: The money can be a negative value representing a debt.
	 * @param money the amount of money that the player will own
	 */
	public synchronized void setMoney(int money) {
		this.money = money;
	}
	
	/**
	 * Gets the distance that the player can reach in meters.
	 * @return he distance that the player can reach
	 */
	public double getReachLength() {
		return REACH_LENGTH;
	}
	
	/**
	 * Checks if the player is currently a spectator. This does not necessarily mean the player has no team, a player is a spectator if the player is not alive
	 * @return {@code true} if the player is a spectator; otherwise {@code false}
	 * @see #isSpectatorTeam()
	 */
	public boolean isSpectator() {
		return getTeam() == null || !isAlive();
	}
	
	/**
	 * Checks if the player has no team and is a spectator.
	 * @return {@code true} if the player is a spectator; otherwise {@code false}
	 * @see #isSpectator()
	 */
	public boolean isSpectatorTeam() {
		return getTeam() == null;
	}
	
	/**
	 * Gets the team of the player.
	 * @return the team of the player
	 */
	public Team getTeam() {
		return this.team;
	}

	/**
	 * Sets the team of the player. If the player is currently in a game, the team will only be changed if the team exists for the map in the game.
	 * @param team the team of the player
	 * @param removeCarryItemsIfNull {@code true} if all carry items should be removed if the new team is {@code null}, otherwise, {@code false}
	 */
	public void setTeam(Team team, boolean removeCarryItemsIfNull) {
		if (team == null) {
			synchronized (this) {
				this.team = null;
			}
		} else if (getGame() != null) {
			if (getGame().getMap().hasTeam(team)) {
				synchronized (this) {
					this.team = team;
				}
				setProjectedImage(getTeam().getTeamPlayerImage());
			}
		}
		if (getTeam() == null) {
			setSpectatorHeight(getHighestHeightAtBase());
			if (removeCarryItemsIfNull) {
				removeAllCarryItems();
			}
		}
		if (isPlayerListenersOn()) {
			Iterator<PlayerListener> iterator = this.listeners.iterator();
			while (iterator.hasNext()) {
				iterator.next().playerTeamChanged(this);
			}
		}
	}
	
	/**
	 * Gets the player's number of kills.
	 * @return the number of kills
	 */
	public int getKills() {
		return this.kills;
	}
	
	/**
	 * Gets the player's number of deaths.
	 * @return the number of deaths
	 */
	public int getDeaths() {
		return this.deaths;
	}
	
	/**
	 * Resets the kills and deaths scores of the player to 0.
	 */
	public synchronized void resetKillsAndDeaths() {
		this.kills = 0;
		this.deaths = 0;
	}
	
	/**
	 * Gets the distance that the player has traveled in meters.
	 * @return the distance that the player has traveled
	 */
	public double getDistanceTravelled() {
		return this.distanceTravelled;
	}
	
	/**
	 * Gets the steps that the player has traveled.
	 * @return the number of steps the player has taken since the last reset
	 */
	public int getSteps() {
		return this.steps;
	}
	
	/**
	 * Resets the distance traveled and steps to 0.
	 */
	public void resetDistanceTravelled() {
		this.distanceTravelled = 0;
		this.steps = 0;
	}
	
	Double getLowestBottomHeightAbove() {
		double min = Double.MAX_VALUE;
		double height = getTopHeight();
		Rectangle2D bounds = getBase().getBounds2D();
		for (double x = Math.max(Math.floor(bounds.getX()), 0); x <= Math.min(Math.floor(bounds.getX() + bounds.getWidth()), getMap().getWidth() - 1); x++) {
			for (double y = Math.max(Math.floor(bounds.getY()), 0); y <= Math.min(Math.floor(bounds.getY() + bounds.getHeight()), getMap().getHeight() - 1); y++) {
				for (int i = 0; i < getMap().getWall(x, y).getNumberOfGaps(); i++) {
					if (getMap().getWall(x, y).getBottomHeight(i) >= height) {
						min = Math.min(min, getMap().getWall(x, y).getBottomHeight(i));
						break;
					}
				}
			}
		}
		Ceiling ceiling = getMap().getLowestCeilingAbove(getBase(), height);
		if (ceiling != null && ceiling.getLocationZ() < min) {
			min = ceiling.getLocationZ();
		}
		if (min == Double.MAX_VALUE) {
			return null;
		} else {
			return min;
		}
	}

	@Override
	public void drawOverImage(Graphics g, int screenWidth, int screenHeight, int imageX, int imageY, int imageWidth, int imageHeight) {
	}
	
	public void setProjectedImage(String projectedImagePath) {
		setProjectedImage(Main.getImage(projectedImagePath, Color.WHITE));
	}
	
	/**
	 * Gets the calculator used to calculate the fall damage.
	 * @return the calculator used to calculate the fall damage
	 */
	public FallDamageCalculator getFallDamageCalculator() {
		return FALL_DAMAGE_CALCULATOR;
	}
	
	/**
	 * Gets the current attack events experienced by the player. If any attack event is expired, it is removed.
	 * @return the current attack events experienced by the player
	 */
	ArrayList<AttackEvent> getAttackEvents() {
		synchronized (this) {
			Iterator<AttackEvent> iterator = ATTACK_EVENTS.iterator();
			while (iterator.hasNext()) {
				AttackEvent a = iterator.next();
				if (System.currentTimeMillis() - a.getHitTime() > AttackEvent.HIT_DISPLAY_TIME) {
					iterator.remove();
				}
			}
		}
		return new ArrayList<AttackEvent>(ATTACK_EVENTS);
	}
	
	/**
	 * Add attack events to be experienced by the player.
	 * @param events the attack events to be experienced by the player
	 */
	void addAttackEvents(AttackEvent... events) {
		synchronized (this) {
			for (AttackEvent e : events) {
				if (e != null) {
					ATTACK_EVENTS.add(e);
				}
			}
		}
	}
	
	/**
	 * Adds messages to be displayed to the player.
	 * @param messages the messages to be displayed
	 */
	public void addChatMessage(ChatMessage.MessageContent... messages) {
		if (messages != null) {
			synchronized (this) {
				for (ChatMessage.MessageContent e : messages) {
					if (e != null) {
						e.startExpireTime();
						this.recentChatMessages.add(e);
					}
				}
			}
		}
	}
	
	/**
	 * Gets the most recent messages sent to the player.
	 * @param mostRecent the number of most recent messages
	 * @return the most recent messages
	 */
	public ChatMessage.MessageContent[] getRecentChatMessages(int mostRecent) {
		synchronized (this) {
			Iterator<ChatMessage.MessageContent> iterator = this.recentChatMessages.iterator();
			while (iterator.hasNext()) {
				ChatMessage.MessageContent m = iterator.next();
				if (m.isExpired()) {
					iterator.remove();
				}
			}
		}
		ChatMessage.MessageContent[] messages = new ChatMessage.MessageContent[Math.min(this.recentChatMessages.size(), mostRecent)];
		int count = 0;
		for (int i = Math.max(this.recentChatMessages.size() - mostRecent, 0); i < this.recentChatMessages.size(); i++) {
			messages[count++] = this.recentChatMessages.get(i);
		}
		return messages;
	}
	
	/**
	 * Attempts to defuse a bomb.
	 * @return {@code true} if the player begins to defuse a bomb; otherwise {@code false}
	 */
	public boolean startDefusingBomb() {
		if (getGame().canDefuseBomb(this)) {
			synchronized (this) {
				this.startDefuseTime = System.currentTimeMillis();
				this.defuseBombExecutor = Executors.newSingleThreadScheduledExecutor();
				this.defuseBombExecutor.schedule(new Runnable() {
					@Override
					public void run() {
						getGame().defuseBomb(true);
					}
				}, getGame().getDefuseBombTime(hasDefuseKit()), TimeUnit.MILLISECONDS);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the player is defusing a bomb.
	 * @return {@code true} if the player is defusing a bomb; otherwise {@code false}
	 */
	public boolean isDefusingBomb() {
		return this.startDefuseTime != null;
	}
	
	/**
	 * Gets a double from 0 to 1, inclusive, representing the amount of progress made of defusing the bomb or {@code null} if no bomb is being defused or no bombs can be defused
	 * @return the progress of defusing a bomb or {@code null} if none is being defused
	 */
	public Double getDefuseProgress() {
		return isDefusingBomb() && getGame().getDefuseBombTime(hasDefuseKit()) != null ? Math.min(Math.max((System.currentTimeMillis() - this.startDefuseTime) * 1d / getGame().getDefuseBombTime(hasDefuseKit()), 0), 1) : null;
	}
	
	/**
	 * Cancels defusing a bomb.
	 */
	public void cancelDefusingBomb() {
		if (this.startDefuseTime != null) {
			synchronized (this) {
				this.startDefuseTime = null;
				if (this.defuseBombExecutor != null) {
					this.defuseBombExecutor.shutdownNow();
					this.defuseBombExecutor = null;
				}
			}
		}
	}
	
	/**
	 * Checks if the player has muscles.
	 * @return true if the player has muscles; otherwise, false
	 */
	public boolean hasMuscles() {
		return this.hasMuscles;
	}
	
	/**
	 * Sets muscles to the player.
	 * @param muscles true to add muscles, false to remove muscles
	 */
	public void setMuscles(boolean muscles) {
		this.hasMuscles = muscles;
	}

	@Override
	public String getRecieverName() {
		return "Player (" + getName() + ") Chat";
	}

	@Override
	public Color getMessageColor() {
		return Color.DARK_GRAY;
	}

	@Override
	public boolean willRecieveMessage(Player p) {
		return equals(p);
	}

	@Override
	public void itemColliding(CylinderMapItem collisionItem, Graphics g, int screenWidth, int screenHeight) {
	}
	
	/**
	 * Adds Player Listeners to listen for events.
	 * @param listeners the listeners to be added
	 */
	public void addPlayerListeners(PlayerListener... listeners) {
		if (listeners != null) {
			for (PlayerListener l : listeners) {
				if (l != null) {
					synchronized (this) {
						this.listeners.add(l);
					}
				}
			}
		}
	}
	
	/**
	 * Removes Player Listeners.
	 * @param listeners the listeners to be removed
	 */
	public void removePlayerListeners(PlayerListener... listeners) {
		if (listeners != null) {
			for (PlayerListener l : listeners) {
				if (l != null) {
					synchronized (this) {
						this.listeners.remove(l);
					}
				}
			}
		}
	}
	
	/**
	 * Removes all Player Listeners.
	 */
	public synchronized void removeAllListeners() {
		this.listeners.clear();
	}
	
	boolean isPlayerListenersOn() {
		return this.listenersOn;
	}
	
	synchronized void setPlayerListeners(boolean state) {
		this.listenersOn = state;
	}
	
	/**
	 * Gets the latency of the player in milliseconds.
	 * @return the latency of the player
	 */
	public Long getLatency() {
		return this.latency;
	}
	
	synchronized void setLatency(long latency) {
		this.latency = latency;
	}

	@Override
	public boolean canDetectOnRadar(Player player) {
		return !isSpectator() && getTeam().equals(player.getTeam());
	}

	@Override
	public Color getRadarColor(Player player) {
		return isSpectator() ? Team.SPECTATOR_COLOR : getTeam().getColor();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
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
		Player other = (Player) obj;
		if (ID != other.ID)
			return false;
		return true;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.tempHorizontalFOV = getDefaultHorizontalFOV();
        this.recentChatMessages = new ArrayList<ChatMessage.MessageContent>();
        this.latency = null;
        this.listenersOn = true;
        this.listeners = new ArrayList<PlayerListener>();
        if (getGame() != null) {
        	this.listeners.add(getGame());
        }
    }

	/**
	 * Represents the different stance positions that a player can be in.
	 * @author Wentao
	 *
	 */
	public enum CrouchLevel {
		CROUCH(0.4, 0.5);
		
		private final double SPEED_MULTIPLIER;
		private final double HEIGHT_MULTIPLIER;
		
		CrouchLevel(double speedMultiplier, double heightMultiplier) {
			SPEED_MULTIPLIER = speedMultiplier;
			HEIGHT_MULTIPLIER = heightMultiplier;
		}
		
		/**
		 * Gets the speed multiplier during the current stance.
		 * @return the speed multiplier
		 */
		public double getSpeedMultiplier() {
			return SPEED_MULTIPLIER;
		}
		
		/**
		 * Gets the physical height multiplier during the current stance.
		 * @return the physical height multiplier
		 */
		public double getHeightMultiplier() {
			return HEIGHT_MULTIPLIER;
		}
	}
	
	public static void drawKevlarAndHelmetSymbol(Graphics g, int topLeftX, int topLeftY, int width, int height, boolean hasHelmet) {
		if (hasHelmet) {
			g.setColor(Color.BLACK);
			g.drawArc((int) (topLeftX + width * 0.4), (int) (topLeftY + height * 0.3), (int) (width * 0.4), (int) (height * 0.4), 270, 90);
		}
		int[] xPoints = new int[]{
				(int) (topLeftX + width * (hasHelmet ? 0.1 : 0.225)),
				(int) (topLeftX + width * (hasHelmet ? 0.1 : 0.225)),
				(int) (topLeftX + width * (hasHelmet ? 0.15 : 0.275)),
				(int) (topLeftX + width * (hasHelmet ? 0.15 : 0.275)),
				(int) (topLeftX + width * (hasHelmet ? 0.2 : 0.325)),
				(int) (topLeftX + width * (hasHelmet ? 0.3 : 0.425)),
				(int) (topLeftX + width * (hasHelmet ? 0.45 : 0.575)),
				(int) (topLeftX + width * (hasHelmet ? 0.5 : 0.625)),
				(int) (topLeftX + width * (hasHelmet ? 0.6 : 0.725)),
				(int) (topLeftX + width * (hasHelmet ? 0.6 : 0.725)),
				(int) (topLeftX + width * (hasHelmet ? 0.65 : 0.775)),
				(int) (topLeftX + width * (hasHelmet ? 0.65 : 0.775))
		};
		int[] yPoints = new int[]{
				(int) (topLeftY + height * 0.9),
				(int) (topLeftY + height * 0.4),
				(int) (topLeftY + height * 0.35),
				(int) (topLeftY + height * 0.15),
				(int) (topLeftY + height * 0.1),
				(int) (topLeftY + height * 0.12),
				(int) (topLeftY + height * 0.12),
				(int) (topLeftY + height * 0.1),
				(int) (topLeftY + height * 0.15),
				(int) (topLeftY + height * 0.35),
				(int) (topLeftY + height * 0.4),
				(int) (topLeftY + height * 0.9),
		};
		g.setColor(new Color(50, 50, 50));
		g.fillPolygon(xPoints, yPoints, xPoints.length);
		if (hasHelmet) {
			g.setColor(new Color(184, 134, 11));
			g.fillArc((int) (topLeftX + width * 0.3), (int) (topLeftY + height * 0.25), (int) (width * 0.6), (int) (height * 0.5), -20, 200);
		}
	}
	
	public static class Kevlar extends Store.BasicStoreItem {
		private static final long serialVersionUID = -1406434267640601798L;
		
		public static final Kevlar DEFAULT_KEVLAR = new Kevlar(650, Main.MISC_DIRECTORY + "/Kevlar.png");
		
		public Kevlar(int cost, String imagePath) {
			this(cost, Main.getImage(imagePath, Color.WHITE));
		}
		
		private Kevlar(int cost, BufferedImage image) {
			super("Kevlar", cost, image);
		}

		@Override
		public ArrayList<String> getStoreInformation() {
			ArrayList<String> info = new ArrayList<String>();
			info.add("Cost: $" + getCost());
			info.add("(Vest only)");
			info.add("Reduces damage.");
			return info;
		}

		@Override
		public HoldItem getHoldItem() {
			return null;
		}

		@Override
		public void itemBought(Player buyer) {
			if (buyer.getMoney() >= getCost()) {
				buyer.setMoney(buyer.getMoney() - getCost());
				buyer.setArmor(buyer.getMaxArmor());
			}
		}

		@Override
		public StoreItem getItemCopy() {
			return new Kevlar(getCost(), getImage());
		}
	}
	
	public static class KevlarAndHelmet extends Store.BasicStoreItem {
		private static final long serialVersionUID = 1658407895642329428L;
		
		public static final KevlarAndHelmet DEFAULT_KEVLAR_AND_HELMET = new KevlarAndHelmet(1000, Main.MISC_DIRECTORY + "/KevlarAndHelmet.png");
		
		public KevlarAndHelmet(int cost, String imagePath) {
			this(cost, Main.getImage(imagePath, Color.WHITE));
		}
		
		private KevlarAndHelmet(int cost, BufferedImage image) {
			super("Kevlar + Helmet", cost, image);
		}

		@Override
		public ArrayList<String> getStoreInformation() {
			ArrayList<String> info = new ArrayList<String>();
			info.add("Cost: $" + getCost());
			info.add("(Vest and helmet)");
			info.add("Reduces damage.");
			return info;
		}

		@Override
		public HoldItem getHoldItem() {
			return null;
		}

		@Override
		public void itemBought(Player buyer) {
			if (buyer.getMoney() >= getCost()) {
				if (buyer.getArmor() >= buyer.getMaxArmor()) {
					buyer.setMoney(buyer.getMoney() - (getCost() - Kevlar.DEFAULT_KEVLAR.getCost()));
				} else {
					buyer.setMoney(buyer.getMoney() - getCost());
					buyer.setArmor(buyer.getMaxArmor());
				}
				buyer.setHelmet(true);
			}
		}

		@Override
		public KevlarAndHelmet getItemCopy() {
			return new KevlarAndHelmet(getCost(), getImage());
		}
	}
}
