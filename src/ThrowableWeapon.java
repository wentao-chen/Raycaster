import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class ThrowableWeapon extends Weapon {
	private static final long serialVersionUID = 5398910099286776867L;
	
	static final String THROWABLE_DIRECTORY = Weapon.WEAPONS_DIRECTORY + "/Throwables";
	
	public static final double DEFAULT_GRENADE_WIDTH = 0.3;
	public static final double DEFAULT_GRENADE_HEIGHT = 0.1;
	
	private static final AtomicInteger ID_INDEXER = new AtomicInteger();
	
	private static final ThrowableWeapon HE_GRENADE = new ThrowableWeapon("HE Grenade", 300, 1, 1, true, true, DEFAULT_GRENADE_WIDTH, DEFAULT_GRENADE_HEIGHT, 1000, 3000, false, 300, 12, 25, new GrenadeDamage.StandardGrenadeDamage(100, 3, 12, 0.6), DEFAULT_GRENADE_WIDTH, DEFAULT_GRENADE_HEIGHT, "HEGrenade/HEGrenade.png", "HEGrenade/ThrownHEGrenade.png", "HEGrenade/Explosion.png", "HEGrenade/ThrownHEGrenade.png", "HEGrenade/HEGrenade1.wav", "pinpull.wav");
	
	private static final ThrowableWeapon FLASHBANG = new ThrowableWeapon("Flashbang", 300, 1, 1, true, true, DEFAULT_GRENADE_WIDTH, DEFAULT_GRENADE_HEIGHT, 1000, 3000, false, 300, 12, 25, new GrenadeDamage.StandardGrenadeDamage(15000l, 5), DEFAULT_GRENADE_WIDTH, DEFAULT_GRENADE_HEIGHT, "Flashbang/Flashbang.png", "Flashbang/ThrownFlashbang.png", "", "Flashbang/ThrownFlashbang.png", "Flashbang/flashbang1.wav", "pinpull.wav");
	
	private static final ThrowableWeapon SMOKE_GRENADE = new ThrowableWeapon("Smoke Grenade", 300, 1, 1, true, true, DEFAULT_GRENADE_WIDTH, DEFAULT_GRENADE_HEIGHT, 1000, 3000, true, 10000, 4, 25, new GrenadeDamage.StandardGrenadeDamage(0, 0, 0, 1), DEFAULT_GRENADE_WIDTH, DEFAULT_GRENADE_HEIGHT, "SmokeGrenade/SmokeGrenade.png", "SmokeGrenade/ThrownSmokeGrenade.png", "SmokeGrenade/Smoke.png", "SmokeGrenade/ThrownSmokeGrenade.png", "SmokeGrenade/smokegrenade1.wav", "SmokeGrenade/pinpull.wav");

	private final int ID;
	private final double DEFAULT_HALF_WIDTH;
	private final double DEFAULT_HEIGHT;
	private final boolean DRAW_CROSS_HAIRS;
	private final long CHARGE_TIME;
	private final long FUSE;
	private final boolean EXPANDS;
	private final long EXPLODE_TIME;
	private final double EXPLODE_SIZE;
	private final double MAX_THROW_SPEED;
	private final GrenadeDamage DAMAGE;
	private final String RELEASE_PIN_SOUND_PATH;
	private final File RELEASE_PIN_SOUND_FILE;
	private ThrownWeapon thrownWeapon = null;
	private Long initialHoldTime = null;

	public ThrowableWeapon(ThrowableWeapon throwableWeapon) {
		this(throwableWeapon.getName(), throwableWeapon.getCost(), throwableWeapon.getRateOfFire(), throwableWeapon.getSpeedMultiplier(), throwableWeapon.canHoldTacticalShield(), throwableWeapon.drawCrossHairs(), throwableWeapon.getDefaultHalfWidth() * 2, throwableWeapon.getDefaultHeight(), throwableWeapon.getChargeTime(), throwableWeapon.getFuse(), throwableWeapon.expands(), throwableWeapon.getExplodeTime(), throwableWeapon.getExplosionSize(), throwableWeapon.getMaxThrowSpeed(), throwableWeapon.getDamage(), throwableWeapon.getImagePath(), throwableWeapon.thrownWeapon, null, null, throwableWeapon.getDropItemWidth(), throwableWeapon.getDropItemHeight(), throwableWeapon.getDropItemImagePath(), throwableWeapon.getFireWeaponSoundPath(), throwableWeapon.getReleasePinSoundPath());
	}

	private ThrowableWeapon(String name, int cost, double rateOfFire, double speedMultipler, boolean canHoldTacticalShield, boolean crossHairs, double defaultWidth, double defaultHeight, long chargeTime, long fuse, boolean expands, long explodeTime, double explosionSize, double maxThrowSpeed, GrenadeDamage damage, String imagePath, ThrownWeapon thrownWeapon, String thrownImagePath, String explosionImagePath, double dropItemWidth, double dropItemHeight, String dropItemImagePath, String fireWeaponSoundPath, String releasePinSoundPath) {
		super(name, ThrowableWeaponType.THROWABLE, cost, rateOfFire, speedMultipler, canHoldTacticalShield, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath);
		if (damage == null) throw new IllegalArgumentException("damage cannot be null");
		ID = ID_INDEXER.getAndIncrement();
		DEFAULT_HALF_WIDTH = Math.abs(defaultWidth / 2);
		DEFAULT_HEIGHT = Math.abs(defaultHeight);
		DRAW_CROSS_HAIRS = crossHairs;
		DAMAGE = damage;
		CHARGE_TIME = Math.abs(chargeTime);
		FUSE = Math.abs(fuse);
		EXPANDS = expands;
		EXPLODE_TIME = Math.abs(explodeTime);
		EXPLODE_SIZE = Math.abs(explosionSize);
		MAX_THROW_SPEED = Math.abs(maxThrowSpeed);
		RELEASE_PIN_SOUND_PATH = releasePinSoundPath;
		RELEASE_PIN_SOUND_FILE = new File(RELEASE_PIN_SOUND_PATH);
		if (thrownWeapon != null) {
			this.thrownWeapon = new ThrownWeapon(thrownWeapon);
		} else {
			this.thrownWeapon = new ThrownWeapon(this, THROWABLE_DIRECTORY + "/" + thrownImagePath, THROWABLE_DIRECTORY + "/" + explosionImagePath);
		}
	}
	
	public ThrowableWeapon(String name, int cost, double rateOfFire, double speedMultipler, boolean canHoldTacticalShield, boolean crossHairs, double defaultWidth, double defaultHeight, long chargeTime, long fuse, boolean expands, long explodeTime, double explosionSize, double maxThrowSpeed, GrenadeDamage damage, double dropItemWidth, double dropItemHeight, String imagePath, String thrownImagePath, String explosionImagePath, String dropItemImagePath, String fireWeaponSoundPath, String releasePinSoundPath) {
		this(name, cost, rateOfFire, speedMultipler, canHoldTacticalShield, crossHairs, defaultWidth, defaultHeight, chargeTime, fuse, expands, explodeTime, explosionSize, maxThrowSpeed, damage, THROWABLE_DIRECTORY + "/" + imagePath, null, thrownImagePath, explosionImagePath, dropItemWidth, dropItemHeight, THROWABLE_DIRECTORY + "/" + dropItemImagePath, THROWABLE_DIRECTORY + "/" + fireWeaponSoundPath, THROWABLE_DIRECTORY + "/" + releasePinSoundPath);
	}

	public static ThrowableWeapon createDefaultHEGrenade() {
		return new ThrowableWeapon(HE_GRENADE);
	}
	
	public static ThrowableWeapon createDefaultFlashbang() {
		return new ThrowableWeapon(FLASHBANG);
	}
	
	public static ThrowableWeapon createDefaultSmokeGrenade() {
		return new ThrowableWeapon(SMOKE_GRENADE);
	}
	
	public boolean isThrown() {
		return this.thrownWeapon == null;
	}
	
	public double getDefaultHalfWidth() {
		return DEFAULT_HALF_WIDTH;
	}
	
	public double getDefaultHeight() {
		return DEFAULT_HEIGHT;
	}
	
	public boolean drawCrossHairs() {
		return DRAW_CROSS_HAIRS;
	}
	
	@Override
	public Double getCrossHairsFocus(Player p) {
		final double CROSS_HAIRS_FOCUS_NO_CHARGE = p.getHorizontalFOV() / 30;
		final double CROSS_HAIRS_FOCUS_FULL_CHARGE = p.getHorizontalFOV() / 120;
		if (drawCrossHairs()) {
			return (1 - getChargePower()) * (CROSS_HAIRS_FOCUS_NO_CHARGE - CROSS_HAIRS_FOCUS_FULL_CHARGE) + CROSS_HAIRS_FOCUS_FULL_CHARGE;
		}
		return null;
	}

	@Override
	public boolean isDropable(Player player) {
		return true;
	}
	
	@Override
	public GrenadeDamage getDamage() {
		return DAMAGE;
	}
	
	/**
	 * Gets the time in milliseconds for the power to throw at max speed to be charged
	 * @return the time in milliseconds to charge
	 */
	public long getChargeTime() {
		return CHARGE_TIME;
	}
	
	/**
	 * Gets the charged power for throwing represented by a value from 0 (no charge) to 1 (full charge).
	 * @return the charged power
	 */
	public double getChargePower() {
		return this.initialHoldTime != null ? Math.min(1d * (System.currentTimeMillis() - this.initialHoldTime) / getChargeTime(), 1) : 0;
	}
	
	/**
	 * Gets the time in milliseconds for the weapon to activate after it is thrown
	 * @return the fuse time in milliseconds
	 */
	public long getFuse() {
		return FUSE;
	}
	
	public boolean expands() {
		return EXPANDS;
	}
	
	/**
	 * Gets the duration of the explosion in milliseconds after it is activated
	 * @return the duration of the explosion in milliseconds
	 */
	public long getExplodeTime() {
		return EXPLODE_TIME;
	}
	
	public double getExplosionSize() {
		return EXPLODE_SIZE;
	}
	
	/**
	 * Gets the maximum initial throwing speed in [m s^-2].
	 * @return the maximum initial throwing speed
	 */
	public double getMaxThrowSpeed() {
		return MAX_THROW_SPEED;
	}
	
	public String getReleasePinSoundPath() {
		return RELEASE_PIN_SOUND_PATH;
	}
	
	public void playReleasePinSound(Player thrower) {
		ProjectionPlane.playSoundFile(RELEASE_PIN_SOUND_FILE, Map.findDistance2D(thrower, ProjectionPlane.getSingleton().getPlayer()));
	}

	@Override
	public synchronized void stopCurrentAction() {
		this.initialHoldTime = null;
	}

	@Override
	public void drawImage(Graphics g, int screenWidth, int screenHeight, Color color) {
		double chargeX = getChargePower() * screenWidth * 0.2;
		double chargeY = getChargePower() * screenHeight * 0.2;
		g.drawImage(getImage(), (int) Math.floor(screenWidth * 0.75 + chargeX), (int) Math.floor(screenHeight * 0.6 + chargeY), (int) Math.floor(screenWidth * 0.25), (int) Math.floor(screenHeight * 0.4), null);
	}

	@Override
	public StoreItem getItemCopy() {
		return new ThrowableWeapon(this);
	}

	@Override
	public void reset() {
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
	public synchronized void mouseReleased(MouseEvent e, Player player) {
		if (this.thrownWeapon != null && this.initialHoldTime != null) {
			this.thrownWeapon.throwWeapon(player, Math.min((System.currentTimeMillis() - this.initialHoldTime) * getMaxThrowSpeed() / getChargeTime() , getMaxThrowSpeed()));
			player.getGame().throwWeapon(this.thrownWeapon);
			this.thrownWeapon = null;
			player.setNextMainItem();
			player.removeCarryItems(this);
			this.initialHoldTime = null;
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e, Player player) {
		playReleasePinSound(player);
		synchronized (this) {
			this.initialHoldTime = System.currentTimeMillis();
		}
	}

	@Override
	public void checkButtons(Set<Integer> pressedButtons, Player player) {
	}

	@Override
	public void itemSwitched(Player player) {
		stopCurrentAction();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThrowableWeapon other = (ThrowableWeapon) obj;
		if (ID != other.ID)
			return false;
		return true;
	}
	
	public enum ThrowableWeaponType implements Weapon.WeaponType {
		THROWABLE("Throwable", Weapon.DEFAULT_THROWABLE_WEAPON_SLOT);
		
		private final String NAME;
		private final HoldItem.HoldItemSlot SLOT;
		
		private ThrowableWeaponType(String name, HoldItem.HoldItemSlot slot) {
			NAME = name;
			SLOT = slot;
		}
		
		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public HoldItem.HoldItemSlot getHoldSlot() {
			return SLOT;
		}
	}
}
