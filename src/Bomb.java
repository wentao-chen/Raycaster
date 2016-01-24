import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;

import com.sun.glass.events.KeyEvent;


public class Bomb implements HoldItem, CauseOfDeath {
	private static final long serialVersionUID = -7846730809122569178L;
	
	static final String BOMBS_DIRECTORY = Main.RESOURCES_DIRECTORY + "/Bombs";
	
	public static final String BOMB_PLANTED_MESSAGE = "The bomb has been planted.";
	public static final long BOMB_PLANTED_MESSAGE_DURATION = 1400;
	public static final String BOMB_DEFUSED_MESSAGE = "The bomb has been defused.";
	public static final long BOMB_DEFUSED_MESSAGE_DURATION = 1800;
	
	private static final AtomicInteger ID_ASSIGNER = new AtomicInteger();

	private final int ID;
	private final String NAME;
	private final Double DAMAGE_RADIUS;
	private final double SPEED_MULTIPLIER;
	private final long PLANT_TIME;
	private final long BOMB_TIME;
	private final Long DEFUSE_TIME;
	private final long DEFUSE_KIT_TIME;
	private final double DEFUSE_RADIUS;
	private final double BLAST_RADIUS;
	private final double FATAL_BLAST_RADIUS;
	private final String HOLD_IMAGE_PATH;
	private transient BufferedImage holdImage;
	private final String PLANTING_IMAGE_PATH;
	private transient BufferedImage plantingImage;
	private final String DROPPED_BOMB_IMAGE_PATH;
	private final DroppedBomb DROPPED_BOMB;
	private final String PLANTED_BOMB_IMAGE_PATH;
	private final PlantedBomb PLANTED_BOMB;
	private final String PLANTING_BOMB_SOUND_PATH;
	private final File PLANTING_BOMB_SOUND_FILE;
	private transient Clip plantingBombSound = null;
	private final String BOMB_PLANTED_SOUND_PATH;
	private final File BOMB_PLANTED_SOUND_FILE;
	private final String BOMB_DEFUSED_SOUND_PATH;
	private final File BOMB_DEFUSED_SOUND_FILE;
	private final String BOMB_EXPLOSION_SOUND_PATH;
	private final File BOMB_EXPLOSION_SOUND_FILE;
	private final String BOMB_BEEP_SOUND_PATH;
	private final File BOMB_BEEP_SOUND_FILE;
	private transient ScheduledExecutorService bombPlanter = null;
	
	private Long startPlantTime = null;
	private Long plantedTime = null;
	private transient Long lastBeepTime = null;
	
	private static final Bomb DEFAULT_BOMB = new Bomb("Bomb", null, 1, 3000, 14500, 10000l, 5000, 1, 40, 20, 0.3, 0.35, 0.17, 0.25, "HoldBomb.png", "HoldBomb.png", "DroppedBomb.png", "PlantedBomb.png", "PlantingBombSound.wav", "BombHasBeenPlantedSound.wav", "BombDefusedSound.wav", "BombExplosion.wav", "BombBeep.wav");
	
	public Bomb(Bomb bomb) {
		this(bomb.getName(), bomb.getDamageRadius(), bomb.getSpeedMultiplier(), bomb.getPlantTime(), bomb.getBombTime(), bomb.getDefuseTime(), bomb.getDefuseKitTime(), bomb.getDefuseRadius(), bomb.getBlastRadius(), bomb.getFatalBlastRadius(), bomb.getDroppedBomb().getPhysicalHeight(), bomb.getDroppedBomb().getPhysicalHalfWidth() * 2, bomb.getPlantedBomb().getPhysicalHeight(), bomb.getPlantedBomb().getPhysicalHalfWidth() * 2, bomb.getHoldImagePath(), bomb.getPlantingImagePath(), bomb.getDroppedBombImagePath(), bomb.getPlantedBombImagePath(), bomb.getPlantingBombSoundPath(), bomb.BOMB_PLANTED_SOUND_PATH, bomb.BOMB_DEFUSED_SOUND_PATH, bomb.BOMB_EXPLOSION_SOUND_PATH, bomb.BOMB_BEEP_SOUND_PATH, false);
	}
	
	public Bomb(String name, Double damageRadius, double speedMultiplier, long plantTime, long bombTime, Long defuseTime, long defuseKitTime, double defuseRadius, double blastRadius, double fatalBlastRadius, double dropBombHeight, double dropBombWidth, double plantedBombHeight, double plantedBombWidth, String holdImagePath, String plantingImagePath, String dropImagePath, String plantedImagePath, String plantSoundPath, String bombPlantedSoundPath, String bombDefusedSoundPath, String explosionSoundPath, String bombBeepSoundPath) {
		this (name, damageRadius, speedMultiplier, plantTime, bombTime, defuseTime, defuseKitTime, defuseRadius, blastRadius, fatalBlastRadius, dropBombHeight, dropBombWidth, plantedBombHeight, plantedBombWidth, BOMBS_DIRECTORY + "/" + holdImagePath, BOMBS_DIRECTORY + "/" + plantingImagePath, BOMBS_DIRECTORY + "/" + dropImagePath, BOMBS_DIRECTORY + "/" + plantedImagePath, BOMBS_DIRECTORY + "/" + plantSoundPath, BOMBS_DIRECTORY + "/" + bombPlantedSoundPath, BOMBS_DIRECTORY + "/" + bombDefusedSoundPath, BOMBS_DIRECTORY + "/" + explosionSoundPath, BOMBS_DIRECTORY + "/" + bombBeepSoundPath, true);
	}
	
	private Bomb(String name, Double damageRadius, double speedMultiplier, long plantTime, long bombTime, Long defuseTime, long defuseKitTime, double defuseRadius, double blastRadius, double fatalBlastRadius, double dropBombHeight, double dropBombWidth, double plantedBombHeight, double plantedBombWidth, String holdImagePath, String plantingImagePath, String dropImagePath, String plantedImagePath, String plantSoundPath, String bombPlantedSoundPath, String bombDefusedSoundPath, String explosionSoundPath, String bombBeepSoundPath, boolean a) {
		if (name == null) throw new IllegalArgumentException("name cannot be null");
		ID = ID_ASSIGNER.getAndIncrement();
		NAME = name;
		DROPPED_BOMB = new DroppedBomb(dropBombWidth, dropBombHeight, dropImagePath);
		PLANTED_BOMB = new PlantedBomb(plantedBombWidth, plantedBombHeight, plantedImagePath);
		DAMAGE_RADIUS = damageRadius;
		SPEED_MULTIPLIER = speedMultiplier;
		PLANT_TIME = Math.abs(plantTime);
		BOMB_TIME = Math.abs(bombTime);
		DEFUSE_TIME = defuseTime != null ? Math.abs(defuseTime) : null;
		DEFUSE_KIT_TIME = Math.abs(defuseKitTime);
		DEFUSE_RADIUS = Math.abs(defuseRadius);
		BLAST_RADIUS = Math.abs(blastRadius);
		FATAL_BLAST_RADIUS = Math.abs(fatalBlastRadius);
		HOLD_IMAGE_PATH = holdImagePath;
		this.holdImage = Main.getImage(getHoldImagePath(), Color.WHITE);
		PLANTING_IMAGE_PATH = plantingImagePath;
		this.plantingImage = Main.getImage(getPlantingImagePath(), Color.WHITE);
		DROPPED_BOMB_IMAGE_PATH = dropImagePath;
		PLANTED_BOMB_IMAGE_PATH = plantedImagePath;
		PLANTING_BOMB_SOUND_PATH = plantSoundPath;
		PLANTING_BOMB_SOUND_FILE = new File(PLANTING_BOMB_SOUND_PATH);
		BOMB_PLANTED_SOUND_PATH = bombPlantedSoundPath;
		BOMB_PLANTED_SOUND_FILE = new File(BOMB_PLANTED_SOUND_PATH);
		BOMB_DEFUSED_SOUND_PATH = bombDefusedSoundPath;
		BOMB_DEFUSED_SOUND_FILE = new File(BOMB_DEFUSED_SOUND_PATH);
		BOMB_EXPLOSION_SOUND_PATH = explosionSoundPath;
		BOMB_EXPLOSION_SOUND_FILE = new File(BOMB_EXPLOSION_SOUND_PATH);
		BOMB_BEEP_SOUND_PATH = bombBeepSoundPath;
		BOMB_BEEP_SOUND_FILE = new File(BOMB_BEEP_SOUND_PATH);
	}
	
	public static Bomb createDefaultBomb() {
		return new Bomb(DEFAULT_BOMB);
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	public Double getDamageRadius() {
		return DAMAGE_RADIUS;
	}

	@Override
	public DroppedItem getDropItem() {
		return isPlanted() ? getPlantedBomb() : getDroppedBomb();
	}
	
	public DroppedItem getDroppedBomb() {
		return DROPPED_BOMB;
	}
	
	public PlantedBomb getPlantedBomb() {
		return PLANTED_BOMB;
	}

	@Override
	public boolean canHoldTacticalShield() {
		return false;
	}
	
	public long getPlantTime() {
		return PLANT_TIME;
	}
	
	/**
	 * Gets the time taken from the time when the bomb is planted to the time it explodes in milliseconds.
	 * @return the time taken for the bomb to detonate in milliseconds
	 */
	public long getBombTime() {
		return BOMB_TIME;
	}
	
	public Long getDefuseTime() {
		return DEFUSE_TIME;
	}
	
	public long getDefuseKitTime() {
		return DEFUSE_KIT_TIME;
	}
	
	public double getDefuseRadius() {
		return DEFUSE_RADIUS;
	}

	public double getBlastRadius() {
		return BLAST_RADIUS;
	}

	public double getFatalBlastRadius() {
		return FATAL_BLAST_RADIUS;
	}
	
	public int getBlastDamage(double distance) {
		if (distance <= getFatalBlastRadius()) {
			return 100;
		} else if (distance >= getBlastRadius()) {
			return 0;
		} else {
			return Math.max(Math.min(100 - (int) Math.round(100 * (distance - getFatalBlastRadius()) / (getBlastRadius() - getFatalBlastRadius())), 100), 0);
		}
	}
	
	public String getHoldImagePath() {
		return HOLD_IMAGE_PATH;
	}
	
	public BufferedImage getHoldImage() {
		return this.holdImage;
	}
	
	public String getPlantingImagePath() {
		return PLANTING_IMAGE_PATH;
	}
	
	public String getDroppedBombImagePath() {
		return DROPPED_BOMB_IMAGE_PATH;
	}
	
	public String getPlantedBombImagePath() {
		return PLANTED_BOMB_IMAGE_PATH;
	}
	
	public String getPlantingBombSoundPath() {
		return PLANTING_BOMB_SOUND_PATH;
	}
	
	public void playPlantingBombSound(Player bombPlanter) {
		synchronized (this) {
			this.plantingBombSound = ProjectionPlane.playSoundFile(PLANTING_BOMB_SOUND_FILE, Map.findDistance2D(bombPlanter, ProjectionPlane.getSingleton().getPlayer()));
		}
	}
	
	public void playBombPlantedSound() {
		ProjectionPlane.playSoundFile(BOMB_PLANTED_SOUND_FILE, 0d);
	}
	
	public void playBombDefusedSound() {
		ProjectionPlane.playSoundFile(BOMB_DEFUSED_SOUND_FILE, Map.findDistance2D(getPlantedBomb(), ProjectionPlane.getSingleton().getPlayer()));
	}
	
	public void playBombExplosionSound() {
		ProjectionPlane.playSoundFile(BOMB_EXPLOSION_SOUND_FILE, Map.findDistance2D(getPlantedBomb(), ProjectionPlane.getSingleton().getPlayer()));
	}
	
	public void playBombBeepSound() {
		ProjectionPlane.playSoundFile(BOMB_BEEP_SOUND_FILE, Map.findDistance2D(getPlantedBomb(), ProjectionPlane.getSingleton().getPlayer()));
	}
	
	public Long getTimeToDetonation() {
		return this.plantedTime != null ? this.plantedTime + getBombTime() - System.currentTimeMillis() : null;
	}
	
	public synchronized void setLastBeepTime() {
		this.lastBeepTime = System.currentTimeMillis();
	}
	
	public Long getNextBeepInterval(boolean useLastBeepTime) {
		if (isPlanted()) {
			useLastBeepTime = useLastBeepTime && this.lastBeepTime != null && this.lastBeepTime < System.currentTimeMillis();
			long currentTime = useLastBeepTime ? this.lastBeepTime : System.currentTimeMillis();
			long timeToDetonation = this.plantedTime + getBombTime() - currentTime;
			Long nextInterval = null;
			if (timeToDetonation < getDefuseKitTime()) {
				nextInterval = isPlanted() ? currentTime + 100 : null;
			} else if (getDefuseTime() != null && timeToDetonation < getDefuseTime()) {
				nextInterval = isPlanted() ? currentTime + 500 : null;
			} else {
				nextInterval = isPlanted() ? currentTime + 1000 : null;
			}
			return nextInterval;
		}
		return null;
	}
	
	public BufferedImage getPlantingImage() {
		return this.plantingImage;
	}
	
	public boolean isPlanting() {
		return this.startPlantTime != null;
	}
	
	public boolean isPlanted() {
		return this.plantedTime != null;
	}
	
	public boolean isDetonated() {
		return System.currentTimeMillis() >= this.plantedTime + getBombTime();
	}
	
	public synchronized void cancelPlanting() {
		this.startPlantTime = null;
		if (this.plantingBombSound != null) {
			this.plantingBombSound.stop();
			this.plantingBombSound = null;
			if (this.bombPlanter != null) {
				this.bombPlanter.shutdownNow();
				this.bombPlanter = null;
			}
		}
	}

	@Override
	public void drawImage(Graphics g, int screenWidth, int screenHeight, Color color) {
		if (isPlanting()) {
			g.drawImage(getPlantingImage(), (int) Math.floor(screenWidth * 0.2), (int) Math.floor(screenHeight * 0.6), (int) Math.floor(screenWidth * 0.4), (int) Math.floor(screenHeight * 0.4), null);
			DefuseKit.drawProgressBar(g, screenWidth, screenHeight, DefuseKit.DEFUSE_BAR_COLOR, (System.currentTimeMillis() - this.startPlantTime) * 1d / getPlantTime());
		} else {
			g.drawImage(getHoldImage(), (int) Math.floor(screenWidth * 0.6), (int) Math.floor(screenHeight * 0.6), (int) Math.floor(screenWidth * 0.4), (int) Math.floor(screenHeight * 0.4), null);
		}
	}

	@Override
	public Double getCrossHairsFocus(Player p) {
		return null;
	}

	@Override
	public void reset() {
	}

	@Override
	public double getSpeedMultiplier() {
		return SPEED_MULTIPLIER;
	}

	@Override
	public boolean isDropable(Player player) {
		return true;
	}

	@Override
	public HoldItemSlot getHoldSlot() {
		return getBombHoldSlot();
	}

	@Override
	public void stopCurrentAction() {
		cancelPlanting();
	}
	
	public static HoldItem.HoldItemSlot getBombHoldSlot() {
		return Weapon.DEFAULT_BOMB_WEAPON_SLOT;
	}

	@Override
	public void keyPressed(int keyCode, Player player) {
		if (keyCode == KeyEvent.VK_G) {
			dropBomb(player);
			cancelPlanting();
		}
	}

	@Override
	public void keyReleased(int keyCode, Player player) {
	}

	@Override
	public void keyTyped(int keyCode, Player player) {
	}

	@Override
	public void checkKeys(Set<Integer> pressedKeys, Player player) {
	}

	@Override
	public void mouseClicked(MouseEvent e, Player player) {
	}

	@Override
	public void mousePressed(MouseEvent e, final Player PLAYER) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (PLAYER.canPlantBomb()) {
				playPlantingBombSound(PLAYER);
				synchronized (this) {
					this.startPlantTime = System.currentTimeMillis();
					this.bombPlanter = Executors.newSingleThreadScheduledExecutor();
					this.bombPlanter.schedule(new Runnable() {
						@Override
						public void run() {
							synchronized (Bomb.this) {
								Bomb.this.plantedTime = System.currentTimeMillis();
							}
							dropBomb(PLAYER);
							PLAYER.getGame().plantBomb(Bomb.this);
						}
					}, getPlantTime(), TimeUnit.MILLISECONDS);
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Player player) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			cancelPlanting();
		}
	}

	@Override
	public void checkButtons(Set<Integer> pressedButtons, Player player) {
	}
	
	private void dropBomb(Player player) {
		player.dropCarryItem(player.getMainHoldItem());
		cancelPlanting();
	}
	
	public static void drawBombIcon(Graphics g, Color color, int x, int y, int width, int height, Color flashingColor, long flashInterval, long flashDuration) {
		g.setColor(flashingColor != null && System.currentTimeMillis() % Math.abs(flashInterval) <= Math.abs(flashDuration) ? flashingColor : color);
		g.fillRect(x, y + (int) (height * 0.35), width, (int) (height * 0.65));
		g.fillRect(x + (int) (width * 0.2), y + (int) (height * 0.1), (int) Math.ceil(width * 0.6) + 1, (int) (height * 0.25));
	}

	@Override
	public void itemSwitched(Player player) {
		cancelPlanting();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.holdImage, "png", out);
        ImageIO.write(this.plantingImage, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.holdImage = ImageIO.read(in);
        this.plantingImage = ImageIO.read(in);
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
		Bomb other = (Bomb) obj;
		if (ID != other.ID)
			return false;
		return true;
	}

	public class DroppedBomb extends DroppedItem {
		private static final long serialVersionUID = -585091704496484480L;

		private DroppedBomb(double height, double width, String imagePath) {
			super(getName(), Bomb.this, width, height, true, Main.getImage(imagePath, Color.WHITE));
		}
		
		@Override
		public Boolean willPickUp(Player player) {
			return player.getTeam() != null && player.getTeam().isBombTeam() && (canHoldTacticalShield() || player.getTacticalShield() == null);
		}

		@Override
		public boolean canDetectOnRadar(Player player) {
			return player.getTeam() != null && player.getTeam().isBombTeam();
		}

		@Override
		public Color getRadarColor(Player player) {
			return Color.YELLOW;
		}
	}

	public class PlantedBomb extends DroppedItem {
		private static final long serialVersionUID = -935027553598250924L;

		private PlantedBomb(double width, double height, String imagePath) {
			super(getName(), Bomb.this, width, height, false, Main.getImage(imagePath, Color.WHITE));
		}
		
		@Override
		public Boolean willPickUp(Player player) {
			return false;
		}
		
		public double getCenterHeight() {
			return (getBottomHeight() + getTopHeight()) / 2;
		}

		@Override
		public void drawOverImage(Graphics g, int screenWidth, int screenHeight, int imageX, int imageY, int imageWidth, int imageHeight) {
			if (Bomb.this.lastBeepTime != null && Bomb.this.lastBeepTime < System.currentTimeMillis() && System.currentTimeMillis() - Bomb.this.lastBeepTime <= 130) {
				double centerX = 42d / 147;
				double centerY = 12d / 134;
				double circleWidth = 25d / 147;
				double circleHeight = 23d / 134;
				Long timeToDetonation = getTimeToDetonation();
				g.setColor(timeToDetonation != null && timeToDetonation < (getDefuseTime() != null ? Math.min(getDefuseKitTime(), getDefuseTime()) : getDefuseKitTime()) ? Color.WHITE : Color.RED);
				g.fillOval(imageX + (int) (centerX * imageWidth), imageY + (int) (centerY * imageHeight), (int) Math.ceil(circleWidth * imageWidth), (int) Math.ceil(circleHeight * imageHeight));
			}
		}

		@Override
		public boolean canDetectOnRadar(Player player) {
			return player.getTeam() != null && player.getTeam().isBombTeam();
		}

		@Override
		public Color getRadarColor(Player player) {
			return Color.RED;
		}
	}
}
