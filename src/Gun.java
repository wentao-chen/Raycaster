import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class Gun extends Weapon implements ObjectCreatorDialog.CreatableObject, ZoomGraphicsDrawer {
	private static final long serialVersionUID = -1751075738222331151L;
	
	static final String GUNS_DIRECTORY = Weapon.WEAPONS_DIRECTORY + "/Guns";

	/**
	 * The default console command for setting perfect accuracy.
	 */
	public static final Console.Command DEFAULT_PERFECT_ACCURACY_COMMAND = new Console.Command("Accuracy", "", "accuracy") {
    	@Override
    	public String getDescription() {
    		return "Sets the perfect accuracy of the current player " + (Gun.isPerfectAccuracyOn() ? "off" : "on");
    	}
		@Override
		public boolean execute(Console console, Game game, String input) {
			Gun.setPerfectAccuracy(!Gun.isPerfectAccuracyOn());
			console.appendText("ACTION: Perfect Accuracy " + (Gun.isPerfectAccuracyOn() ? "ON" : "OFF"));
			return true;
		}
    };

	/**
	 * The default console command for setting recoil.
	 */
	public static final Console.Command DEFAULT_RECOIL_COMMAND = new Console.Command("Recoil", "", "recoil") {
    	@Override
    	public String getDescription() {
    		return "Sets recoil of the current player " + (Gun.isRecoilOn() ? "off" : "on");
    	}
		@Override
		public boolean execute(Console console, Game game, String input) {
			Gun.setRecoil(!Gun.isRecoilOn());
			console.appendText("ACTION: Recoil " + (Gun.isRecoilOn() ? "ON" : "OFF"));
			return true;
		}
    };

	private static final AtomicBoolean PERFECT_ACCUARACY = new AtomicBoolean(false);
	private static final AtomicBoolean RECOIL_ON = new AtomicBoolean(true);
	private final boolean IS_AUTOMATIC;
	private final BurstFire BURST_FIRE;
	private final long RELOAD_TIME;
	private final double MUZZLE_VELOCITY;
	private final Damage DAMAGE;
	private final BulletDamage BULLET_DAMAGE;
	private final boolean DRAW_CROSS_HAIRS;
	private final int MAGAZINE_CAPACITY;
	private final int CARRY_CAPACITY;
	private final double ACCURACY;
	private final ZoomGraphicsDrawer[] ZOOM_LEVELS;
	private final double BULLET_RADIUS;
	private final ReloadType RELOAD_TYPE;
	protected final boolean PLAY_RELOAD_SOUND_TWICE;
	private final Ammo AMMO;
	private final int AMMO_COST_PER_CLIP;
	private final String RELOAD_SOUND_PATH;
	private final File RELOAD_SOUND_FILE;
	private final String EMPTY_MAGAZINE_SOUND_PATH;
	private final File EMPTY_MAGAZINE_FILE;

	private Integer currentZoomLevel = null;
	private int bullets = 0;
	private int carryBullets = 0;
	private transient Long reloadStartTime = null;
	private transient boolean isShooting = false;
	protected transient long lastShotTime = -1;
	private transient Long previousShotTime = null;
	private transient Long startConsecutiveShotsTime = null;
	private transient int consecutiveShots = 0;
	private transient int burstFireShots = 0;
	private transient Thread reloadingThread = null;
	
	public Gun(Gun gun) {
		this(gun.getName(), gun.getWeaponType(), gun.getCost(), gun.isAutomatic(), gun.getBurstFire(), gun.getReloadTime(), gun.getRegularRateOfFire(), gun.getMuzzleVelocity(), gun.getDamage(), gun.getBulletDamage(), gun.getMagazineCapacity(), gun.getCarryCapacity(), gun.getRegularSpeedMultiplier(), gun.canHoldTacticalShield(), gun.getRegularAccuracy(), gun.getZoomGraphicsDrawers(), gun.drawCrossHairs(), gun.getBulletRadius(), gun.getReloadType(), gun.getAmmo(), gun.getAmmoCostPerClip(), gun.getDropItemWidth(), gun.getDropItemHeight(), gun.getImagePath(), gun.getDropItemImagePath(), gun.getFireWeaponSoundPath(), gun.getReloadSoundPath(), gun.getEmptyMagazineSoundPath(), gun.PLAY_RELOAD_SOUND_TWICE);
	}
	
	public Gun(String name, WeaponType weaponType, int cost, boolean isAutomatic, BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, Damage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, boolean canHoldTacticalShield, double accuracy, ZoomGraphicsDrawer[] zoomLevels, boolean drawCrossHairs, double bulletRadius, ReloadType reloadType, Ammo ammo, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath) {
		this(name, weaponType, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, null, magazineCapacity, carryCapacity, speedMultiplier, canHoldTacticalShield, accuracy, zoomLevels, drawCrossHairs, bulletRadius, reloadType, ammo, ammoCostPerClip, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath, false);
	}
	
	public Gun(String name, WeaponType weaponType, int cost, boolean isAutomatic, BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, BulletDamage bulletDamage, int magazineCapacity, int carryCapacity, double speedMultiplier, boolean canHoldTacticalShield, double accuracy, ZoomGraphicsDrawer[] zoomLevels, boolean drawCrossHairs, double bulletRadius, ReloadType reloadType, Ammo ammo, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath) {
		this(name, weaponType, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, bulletDamage, bulletDamage, magazineCapacity, carryCapacity, speedMultiplier, canHoldTacticalShield, accuracy, zoomLevels, drawCrossHairs, bulletRadius, reloadType, ammo, ammoCostPerClip, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath, false);
	}
	
	Gun(String name, WeaponType weaponType, int cost, boolean isAutomatic, BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, Damage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, boolean canHoldTacticalShield, double accuracy, ZoomGraphicsDrawer[] zoomLevels, boolean drawCrossHairs, double bulletRadius, ReloadType reloadType, Ammo ammo, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath, boolean playReloadSoundTwice) {
		this(name, weaponType, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, null, magazineCapacity, carryCapacity, speedMultiplier, canHoldTacticalShield, accuracy, zoomLevels, drawCrossHairs, bulletRadius, reloadType, ammo, ammoCostPerClip, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath, playReloadSoundTwice);
	}
	
	Gun(String name, WeaponType weaponType, int cost, boolean isAutomatic, BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, BulletDamage bulletDamage, int magazineCapacity, int carryCapacity, double speedMultiplier, boolean canHoldTacticalShield, double accuracy, ZoomGraphicsDrawer[] zoomLevels, boolean drawCrossHairs, double bulletRadius, ReloadType reloadType, Ammo ammo, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath, boolean playReloadSoundTwice) {
		this(name, weaponType, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, bulletDamage, bulletDamage, magazineCapacity, carryCapacity, speedMultiplier, canHoldTacticalShield, accuracy, zoomLevels, drawCrossHairs, bulletRadius, reloadType, ammo, ammoCostPerClip, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath, playReloadSoundTwice);
	}
	
	private Gun(String name, WeaponType weaponType, int cost, boolean isAutomatic, BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, Damage damage, BulletDamage bulletDamage, int magazineCapacity, int carryCapacity, double speedMultiplier, boolean canHoldTacticalShield, double accuracy, ZoomGraphicsDrawer[] zoomLevels, boolean drawCrossHairs, double bulletRadius, ReloadType reloadType, Ammo ammo, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath, boolean playReloadSoundTwice) {
		super(name, weaponType, cost, rateOfFire, speedMultiplier, canHoldTacticalShield, zoomLevels, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath);
		if (damage == null) throw new IllegalArgumentException("damage cannot be null");
		if (reloadType == null) throw new IllegalArgumentException("reload type cannot be null");
		IS_AUTOMATIC = isAutomatic;
		BURST_FIRE = burstFire;
		RELOAD_TIME = Math.abs(reloadTime);
		MUZZLE_VELOCITY = Math.abs(muzzleVelocity);
		DAMAGE = damage;
		BULLET_DAMAGE = bulletDamage;
		DRAW_CROSS_HAIRS = drawCrossHairs;
		MAGAZINE_CAPACITY = Math.max(magazineCapacity, 1);
		CARRY_CAPACITY = carryCapacity;
		ACCURACY = Math.max(accuracy, 0);
		ZOOM_LEVELS = zoomLevels;
		BULLET_RADIUS = Math.abs(bulletRadius);
		RELOAD_TYPE = reloadType;
		PLAY_RELOAD_SOUND_TWICE = playReloadSoundTwice;
		AMMO = ammo;
		AMMO_COST_PER_CLIP = Math.abs(ammoCostPerClip);
		RELOAD_SOUND_PATH = reloadSoundPath;
		RELOAD_SOUND_FILE = new File(getReloadSoundPath());
		EMPTY_MAGAZINE_SOUND_PATH = emptyMagazineSoundPath;
		EMPTY_MAGAZINE_FILE = new File(getEmptyMagazineSoundPath());
		this.currentZoomLevel = null;
		this.bullets = getMagazineCapacity();
	}

	public boolean isAutomatic() {
		return IS_AUTOMATIC;
	}
	
	public BurstFire getBurstFire() {
		return BURST_FIRE;
	}
	
	public long getReloadTime() {
		return RELOAD_TIME;
	}
	
	public static boolean isPerfectAccuracyOn() {
		return PERFECT_ACCUARACY.get();
	}
	
	public static void setPerfectAccuracy(boolean perfectAccuracy) {
		PERFECT_ACCUARACY.set(perfectAccuracy);
	}
	
	public static boolean isRecoilOn() {
		return RECOIL_ON.get();
	}
	
	public static void setRecoil(boolean recoil) {
		RECOIL_ON.set(recoil);
	}
	
	/**
	 * Gets the muzzle velocity in meters per second.
	 * @return the muzzle velocity in meters per second
	 */
	public double getMuzzleVelocity() {
		return MUZZLE_VELOCITY;
	}
	
	@Override
	public Damage getDamage() {
		return DAMAGE;
	}
	
	public BulletDamage getBulletDamage() {
		return BULLET_DAMAGE;
	}
	
	public int getMagazineCapacity() {
		return MAGAZINE_CAPACITY;
	}
	
	public int getCarryCapacity() {
		return CARRY_CAPACITY;
	}
	
	@Override
	public double getRateOfFire() {
		return (getBurstFire() != null && (this.burstFireShots % getBurstFire().getBurstRounds()) < (getBurstFire().getBurstRounds() - 1)) ? getBurstFire().getBurstRateOfFire() : (isZoomed() ? getScopeRateOfFire() : getRegularRateOfFire());
	}
	
	public double getScopeRateOfFire() {
		if (hasZoom() && isZoomed()) {
			return Math.max(ZOOM_LEVELS[getZoomLevel()].getRateOfFire(), 1);
		}
		return getRegularRateOfFire();
	}
	
	@Override
	public double getSpeedMultiplier() {
		return isZoomed() ? getScopeSpeedMultiplier() : getRegularSpeedMultiplier();
	}
	
	public double getScopeSpeedMultiplier() {
		if (hasZoom() && isZoomed()) {
			return Math.max(ZOOM_LEVELS[getZoomLevel()].getSpeedMultiplier(), 1);
		}
		return getRegularSpeedMultiplier();
	}
	
	public ZoomGraphicsDrawer[] getZoomGraphicsDrawers() {
		if (ZOOM_LEVELS != null) {
			ZoomGraphicsDrawer[] copy = new ZoomGraphicsDrawer[ZOOM_LEVELS.length];
			System.arraycopy(ZOOM_LEVELS, 0, copy, 0, ZOOM_LEVELS.length);
			return copy;
		}
		return null;
	}
	
	public boolean isZoomed() {
		return getZoomLevel() != null;
	}
	
	public boolean hasZoom() {
		return ZOOM_LEVELS != null && ZOOM_LEVELS.length > 0;
	}
	
	public Integer getZoomLevel() {
		return this.currentZoomLevel;
	}
	
	public synchronized void setZoomLevel(Integer zoomLevel) {
		this.currentZoomLevel = zoomLevel != null ? Math.max(Math.min(zoomLevel, ZOOM_LEVELS != null ? ZOOM_LEVELS.length - 1 : 0), 0) : null;
	}
	
	public void cycleZoom() {
		if (isZoomed() && getZoomLevel() >= ZOOM_LEVELS.length - 1) {
			setZoomLevel(null);
		} else if (isZoomed()) {
			setZoomLevel(getZoomLevel() + 1);
		} else {
			setZoomLevel(0);
		}
	}
	
	public double getAccuracy() {
		return isZoomed() ? getScopeAccuracy() : getRegularAccuracy();
	}
	
	public double getRegularAccuracy() {
		return ACCURACY;
	}
	
	public double getScopeAccuracy() {
		if (hasZoom() && isZoomed()) {
			return Math.max(getZoomGraphicsDrawers()[getZoomLevel()].getAccuracy(), 0);
		}
		return getRegularAccuracy();
	}
	
	public ReloadType getReloadType() {
		return RELOAD_TYPE;
	}
	
	public Ammo getAmmo() {
		return AMMO;
	}
	
	public double getBulletRadius() {
		return BULLET_RADIUS;
	}
	
	public int getAmmoCostPerClip() {
		return AMMO_COST_PER_CLIP;
	}
	
	public String getReloadSoundPath() {
		return RELOAD_SOUND_PATH;
	}
	
	public String getEmptyMagazineSoundPath() {
		return EMPTY_MAGAZINE_SOUND_PATH;
	}

	@Override
	public boolean isDropable(Player player) {
		return true;
	}
	
	@Override
	public ArrayList<String> getStoreInformation() {
		ArrayList<String> info = super.getStoreInformation();
		if (getBurstFire() != null) {
			info.add("Burst Fire: " + getBurstFire().getBurstRounds() + "-round burst");
		} else {
			info.add("Automatic: " + (isAutomatic() ? "Yes" : "No"));
		}
		if (hasZoom()) {
			String zoomString = "Zoom:";
			for (ZoomGraphicsDrawer z : ZOOM_LEVELS) {
				zoomString += " (" + z.zoomMultiplier() + "x)";
			}
			info.add(zoomString);
		}
		info.add("Reload Time: " + (getReloadTime() / 1000d) + "s");
		info.add("Muzzle Velocity: " + getMuzzleVelocity() + "m/s");
		info.add("Damage: " + getDamage().getDefaultDamage());
		info.add("Capacity: " + getMagazineCapacity() + "/" + getCarryCapacity());
		info.add("Accuracy: " + Math.max(getAccuracy(), getScopeAccuracy()) + "m");
		return info;
	}
	
	public boolean drawCrossHairs() {
		return DRAW_CROSS_HAIRS;
	}
	
	@Override
	public Double getCrossHairsFocus(Player p) {
		if (drawCrossHairs()) {
			return getAccurateRange(p);
		}
		return null;
	}
	
	public int getBullets() {
		return this.bullets;
	}
	
	public int getCarryBullets() {
		return this.carryBullets;
	}
	
	protected Long getReloadStartTime() {
		return this.reloadStartTime;
	}
	
	public abstract void addToAllGunTypeCollection();

	@Override
	public void objectCreated() {
		addToAllGunTypeCollection();
	}
	
	@Override
	public synchronized void reset() {
		this.bullets = getMagazineCapacity();
		this.carryBullets = getCarryCapacity();
		this.reloadStartTime = null;
		this.isShooting = false;
		this.lastShotTime = -1;
		this.startConsecutiveShotsTime = null;
		this.consecutiveShots = 0;
		this.burstFireShots = 0;
	}
	
	protected void shoot(Player player) {
		player.getGame().shootBullet(createBullet(player, player.getLocationX(), player.getLocationY(), player.getViewHeight(), player.getHorizontalDirection(), player.getVerticalDirection()));
	}
	
	private synchronized boolean fire(Player player) {
		if (player != null && player.isAlive() && getBullets() > 0 && !isReloading() && (this.previousShotTime == null || System.currentTimeMillis() - this.previousShotTime >= 60000 / getRateOfFire())) {
			this.bullets -= 1;
			this.lastShotTime = System.currentTimeMillis();
			this.previousShotTime = System.currentTimeMillis();
			if (this.startConsecutiveShotsTime != null) {
				this.consecutiveShots -= Math.min((int) Math.floor(Math.pow(Math.max(Math.min((System.currentTimeMillis() - this.startConsecutiveShotsTime) * getRateOfFire() / (60000 * getMagazineCapacity() + 1000), 1), 0), 1.2) * getMagazineCapacity()), this.consecutiveShots);
			}
			this.startConsecutiveShotsTime = System.currentTimeMillis();
			this.consecutiveShots++;
			this.burstFireShots++;
			playFireWeaponSound(player);
			for (int i = 0; i < getAmmo().getProjectilesPerShot(); i++) {
				shoot(player);
			}
			return true;
		} else if (getBullets() <= 0) {
			playEmptyMagazineSound(player);
		}
		return false;
	}
	
	@Override
	public void stopCurrentAction() {
		cancelReload();
	}
	
	public void cancelReload() {
		synchronized (this) {
			this.reloadingThread = null;
			this.reloadStartTime = null;
		}
	}
	
	private boolean canReload(Player player) {
		return getCarryBullets() > 0 && getBullets() < getMagazineCapacity() && (player.getTacticalShield() == null || !player.getTacticalShield().isDeployed()) && player.getGame() != null && player.getGame().isRunning();
	}
	
	public boolean reload(final Player RELOADING_PLAYER) {
		if (canReload(RELOADING_PLAYER) && !isReloading()) {
			if (getReloadType() == ReloadType.MAGAZINE) {
				playReloadSound(RELOADING_PLAYER);
				final int BULLETS_REFILLED = Math.min(Math.min(getMagazineCapacity(), getCarryBullets()), getMagazineCapacity() - getBullets());
				final Integer PREVIOUS_ZOOM_LEVEL = getZoomLevel();
				if (RELOADING_PLAYER != null) {
					setZoomLevel(null);
					RELOADING_PLAYER.setZoom(1);
				}
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						if (PLAY_RELOAD_SOUND_TWICE) {
							try {
								Thread.sleep(getReloadTime() / 2);
							} catch (InterruptedException e) {
							}
							if (Gun.this.reloadingThread == null && RELOADING_PLAYER.getGame().isRunning()) {
								return;
							}
							playReloadSound(RELOADING_PLAYER);
							try {
								Thread.sleep(getReloadTime() / 2);
							} catch (InterruptedException e) {
							}
						} else {
							try {
								Thread.sleep(getReloadTime());
							} catch (InterruptedException e) {
							}
						}
						if (Gun.this.reloadingThread == null && RELOADING_PLAYER.getGame().isRunning()) {
							return;
						}
						if (RELOADING_PLAYER != null) {
							if (Gun.this.equals(RELOADING_PLAYER.getMainHoldItem())) {
								setZoomLevel(PREVIOUS_ZOOM_LEVEL);
								RELOADING_PLAYER.setZoom(zoomMultiplier());
							}
						}
						synchronized (Gun.this) {
							Gun.this.consecutiveShots = 0;
							Gun.this.burstFireShots = 0;
							Gun.this.bullets += BULLETS_REFILLED;
							Gun.this.carryBullets -= BULLETS_REFILLED;
						}
					}
				});
				synchronized (this) {
					Gun.this.reloadStartTime = System.currentTimeMillis();
					Gun.this.reloadingThread = thread;
				}
				Gun.this.reloadingThread.start();
			} else if (getReloadType() == ReloadType.CARTRIDGE) {
				this.consecutiveShots = 0;
				this.burstFireShots = 0;
				final Integer PREVIOUS_ZOOM_LEVEL = getZoomLevel();
				if (RELOADING_PLAYER != null) {
					setZoomLevel(null);
					RELOADING_PLAYER.setZoom(1);
				}
				if (this.reloadingThread == null && RELOADING_PLAYER.getGame().isRunning()) {
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							while (canReload(RELOADING_PLAYER)) {
								if (PLAY_RELOAD_SOUND_TWICE) {
									try {
										Thread.sleep(getReloadTime() / 2);
									} catch (InterruptedException e) {
									}
									if (Gun.this.reloadingThread == null && RELOADING_PLAYER.getGame().isRunning()) {
										break;
									}
									playReloadSound(RELOADING_PLAYER);
									try {
										Thread.sleep(getReloadTime() / 2);
									} catch (InterruptedException e) {
									}
								} else {
									try {
										Thread.sleep(getReloadTime());
									} catch (InterruptedException e) {
									}
								}
								if (Gun.this.reloadingThread == null && RELOADING_PLAYER.getGame().isRunning()) {
									break;
								}
								playReloadSound(RELOADING_PLAYER);
								if (RELOADING_PLAYER != null) {
									if (Gun.this.equals(RELOADING_PLAYER.getMainHoldItem())) {
										setZoomLevel(PREVIOUS_ZOOM_LEVEL);
										RELOADING_PLAYER.setZoom(zoomMultiplier());
									}
								}
								synchronized (Gun.this) {
									Gun.this.reloadStartTime = System.currentTimeMillis();
									Gun.this.bullets++;
									Gun.this.carryBullets--;
								}
							}
							synchronized (Gun.this) {
								Gun.this.reloadingThread = null;
							}
						}
					});
					synchronized (this) {
						this.reloadingThread = thread;
					}
					this.reloadingThread.start();
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	public int buyAmmo(int money) {
		if (this.carryBullets < getCarryCapacity()) {
			int cost = 0;
			int startCarryAmmo = this.carryBullets;
			for (int i = 0; i < (getCarryCapacity() - startCarryAmmo - 1) / getMagazineCapacity() + 1 && money - cost >= getAmmoCostPerClip(); i++) {
				cost += getAmmoCostPerClip();
				this.carryBullets = Math.min(this.carryBullets + getMagazineCapacity(), getCarryCapacity());
			}
			return cost;
		}
		return 0;
	}
	
	private double recoilFunction(double x, double horizontalControlFactor, double horizontalControlRelative, double frequencyFactor, double phaseShiftFactor) {
		return Math.pow(Math.E, horizontalControlRelative * (x - 1)) * horizontalControlFactor * Math.cos(frequencyFactor * (x + 1) + phaseShiftFactor);
	}
	
	private double getRecoilHorizontal(int consecutiveShots, double maxRecoilHorizontal) {
		maxRecoilHorizontal = Math.abs(maxRecoilHorizontal);
		double x = Math.max(Math.min(consecutiveShots * 1d / getMagazineCapacity(), 1), 0);
		if (consecutiveShots < 1) {
			return 0;
		} else if (consecutiveShots < 2) {
			return maxRecoilHorizontal * 0.01;
		} else if (consecutiveShots < 3) {
			return maxRecoilHorizontal * -0.02;
		} else {
			return maxRecoilHorizontal * Math.max(Math.min(recoilFunction(x, 2, 4.8, 14.4, -5.8), 1), -1);
		}
	}
	
	private double getRecoilVertical(int consecutiveShots, double maxRecoilUp) {
		maxRecoilUp = Math.abs(maxRecoilUp);
		return Math.min(Math.max(consecutiveShots * 1d / getMagazineCapacity(), 0) * maxRecoilUp, maxRecoilUp);
	}
	
	private double getAccurateRange(Player player) {
		return Math.PI - (player.getHorizontalFOV() / (2 * Math.PI) * Math.pow(1 - Math.atan(0.5 / (getAccuracy() * (player.getCrouchLevel() == Player.CrouchLevel.CROUCH ? 2 : 1))) / Math.PI, Math.pow(this.consecutiveShots * 30 / getMagazineCapacity(), 0.8)) + 1 - player.getHorizontalFOV() / (2 * Math.PI)) * Math.PI;
	}
	
	private Bullet createBullet(Player player, double x, double y, double z, double horizontalAngle, double verticalAngle) {
		if (isAutomatic() && isRecoilOn()) {
			double recoilVertical = Math.abs(getRecoilVertical(this.consecutiveShots, Math.PI / 25 * Math.sqrt(getMagazineCapacity() / 30d)) - getRecoilVertical(this.consecutiveShots - 1, Math.PI / 25 * Math.sqrt(getMagazineCapacity() / 30d)));
			double recoilHorizontal = getRecoilHorizontal(this.consecutiveShots, Math.PI / 100);
			player.setHorizontalDirection(player.getHorizontalDirection() + recoilHorizontal);
			player.setVerticalDirection(player.getVerticalDirection() + recoilVertical);
		}
		double accurateRange = isPerfectAccuracyOn() ? 0 : getAccurateRange(player);
		double accuracyDistanceFromCenter = Math.random() * accurateRange;
		double accuracyAngle = Math.random() * Math.PI * 2;
		return new Bullet(x, y, z, horizontalAngle + Math.cos(accuracyAngle) * accuracyDistanceFromCenter, verticalAngle - Math.sin(accuracyAngle) * accuracyDistanceFromCenter, getMuzzleVelocity(), player, this, getBulletDamage(), getBulletRadius());
	}
	
	public boolean isShooting() {
		return this.isShooting;
	}
	
	public boolean isReloading() {
		return getReloadStartTime() != null && System.currentTimeMillis() >= getReloadStartTime() && System.currentTimeMillis() <= getReloadStartTime() + getReloadTime();
	}
	
	public void drawGunInfo(Graphics g, int screenWidth, int screenHeight, Color color) {
		g.setColor(color);
		g.setFont(Weapon.getWeaponsInfoFont(screenWidth, screenHeight));
		FontMetrics fm = g.getFontMetrics(g.getFont());
		String nOfBulletsStr = (isReloading() ? "R" : getBullets()) + " | " + (isReloading() ? "R" : getCarryBullets());
		g.drawString(nOfBulletsStr, screenWidth - 5 - fm.stringWidth(nOfBulletsStr), screenHeight - 5 - fm.getDescent());
		if (getReloadType() == ReloadType.CARTRIDGE && isReloading()) {
			g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 0.8f));
			FontMetrics fm2 = g.getFontMetrics(g.getFont());
			String nOfBulletsStr2 = "(" + getBullets() + ")";
			g.drawString(nOfBulletsStr2, screenWidth - 5 - fm.stringWidth(nOfBulletsStr) - (int) Math.round(fm2.stringWidth(nOfBulletsStr2) * 1.2), screenHeight - 5 - fm.getDescent());
		}
	}

	@Override
	public void checkKeys(Set<Integer> pressedKeys, Player player) {
	}

	@Override
	public void keyPressed(int keyCode, Player player) {
		super.keyPressed(keyCode, player);
		if (player.getMainHoldItem() != this) {
			releaseTrigger(player);
		} else {
			if (keyCode == KeyEvent.VK_R) {
				reload(player);
			}
		}
	}

	@Override
	public void keyReleased(int keyCode, Player player) {
	}

	@Override
	public void keyTyped(int keyCode, Player player) {
	}
	
	public boolean allowClickZoom() {
		return System.currentTimeMillis() - this.lastShotTime >= 60000 / getRateOfFire() && !isReloading();
	}
	
	public void pullTrigger(final Player PLAYER) {
		if (!isAutomatic() && (PLAYER.getTacticalShield() == null || !PLAYER.getTacticalShield().isDeployed()) && System.currentTimeMillis() - this.lastShotTime >= 60000 / getRateOfFire() && fire(PLAYER)) {
			if (!isAutomatic()) {
				final Integer PREVIOUS_ZOOM_LEVEL = getZoomLevel();
				setZoomLevel(null);
				PLAYER.setZoom(1);
				Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
					@Override
					public void run() {
						if (Gun.this.equals(PLAYER.getMainHoldItem())) {
							setZoomLevel(PREVIOUS_ZOOM_LEVEL);
							PLAYER.setZoom(zoomMultiplier());
						}
					}
				}, Math.max(Math.round(60000 / getRateOfFire()) - 1, 0), TimeUnit.MILLISECONDS);
			}
			releaseTrigger(PLAYER);
		}
	}
	
	public void holdTrigger(final Player PLAYER) {
		if (isAutomatic() && (PLAYER.getTacticalShield() == null || !PLAYER.getTacticalShield().isDeployed()) && getBullets() > 0) {
			if (System.currentTimeMillis() - this.lastShotTime >= 60000 / getRateOfFire() && fire(PLAYER)) {
				releaseTrigger(PLAYER);
			}
			this.isShooting = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (isShooting() && PLAYER.getGame().isRunning()) {
						if (fire(PLAYER)) {
						} else if (System.currentTimeMillis() - Gun.this.previousShotTime >= 60000 / getRateOfFire()) {
							synchronized (this) {
								Gun.this.isShooting = false;
							}
							break;
						} else {
							try {Thread.sleep((long) Math.ceil(60000 / getRateOfFire() - (System.currentTimeMillis() - Gun.this.previousShotTime)));} catch (InterruptedException e) {}
						}
					}
				}
			}).start();
		} else if (getBullets() <= 0) {
			playEmptyMagazineSound(PLAYER);
		}
	}
	
	public synchronized void releaseTrigger(Player player) {
		this.isShooting = false;
		this.consecutiveShots = 0;
	}

	@Override
	public void mouseClicked(MouseEvent e, Player player) {
	}

	@Override
	public void mousePressed(MouseEvent e, Player player) {
		if (getReloadType() == ReloadType.CARTRIDGE) {
			cancelReload();
		}
		if (e.getButton() == MouseEvent.BUTTON1) {
			holdTrigger(player);
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			if (hasZoom()) {
				cycleZoom();
				player.setZoom(zoomMultiplier());
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Player player) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (isAutomatic()) {
				releaseTrigger(player);
			} else {
				pullTrigger(player);
			}
		}
	}

	@Override
	public void checkButtons(Set<Integer> pressedButtons, Player player) {
	}

	@Override
	public double zoomMultiplier() {
		if (hasZoom() && isZoomed()) {
			return ZOOM_LEVELS[getZoomLevel()].zoomMultiplier();
		} else {
			return 1;
		}
	}
	
	@Override
	public boolean clearGraphicsForZoom() {
		if (hasZoom() && isZoomed()) {
			return ZOOM_LEVELS[getZoomLevel()].clearGraphicsForZoom();
		} else {
			return false;
		}
	}
	
	@Override
	public void drawZoomGraphics(Graphics g, int screenWidth, int screenHeight) {
		if (hasZoom() && isZoomed()) {
			ZOOM_LEVELS[getZoomLevel()].drawZoomGraphics(g, screenWidth, screenHeight);
		}
	}
	
	public void playReloadSound(Player shooter) {
		ProjectionPlane.playSoundFile(RELOAD_SOUND_FILE, Map.findDistance2D(shooter, ProjectionPlane.getSingleton().getPlayer()));
	}
	
	public void playEmptyMagazineSound(Player shooter) {
		ProjectionPlane.playSoundFile(EMPTY_MAGAZINE_FILE, Map.findDistance2D(shooter, ProjectionPlane.getSingleton().getPlayer()));
	}
	
	@Override
	public void itemSwitched(Player player) {
		releaseTrigger(player);
		setZoomLevel(null);
		player.setZoom(1);
		cancelReload();
	}

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.reloadStartTime = null;
    	this.isShooting = false;
    	this.lastShotTime = -1;
    	this.previousShotTime = null;
		this.startConsecutiveShotsTime = null;
    	this.consecutiveShots = 0;
    	this.burstFireShots = 0;
    	this.reloadingThread = null;
    }
	
	public interface DefaultGuns {
		public Gun createGun();
	}
    
    public static class Ammo implements Serializable {
		private static final long serialVersionUID = 6913231512350837866L;
		
		private final AmmoCategory AMMO_CATEGORY;
    	private final AmmoProjectile AMMO_PROJECTILE;
    	private final int PROJECTILES_PER_SHOT;
    	
    	public Ammo(AmmoCategory ammoCategory, AmmoProjectile ammoProjectile, int projectilesPerShot) {
    		if (ammoCategory == null) throw new IllegalArgumentException("ammo category cannot be null");
    		if (ammoProjectile == null) throw new IllegalArgumentException("ammo projectile cannot be null");
    		AMMO_CATEGORY = ammoCategory;
    		AMMO_PROJECTILE = ammoProjectile;
    		PROJECTILES_PER_SHOT = Math.max(projectilesPerShot, 1);
    	}
    	
    	public AmmoCategory getAmmoCategory() {
    		return AMMO_CATEGORY;
    	}
    	
    	public AmmoProjectile getAmmoProjectile() {
    		return AMMO_PROJECTILE;
    	}
    	
    	public int getProjectilesPerShot() {
    		return PROJECTILES_PER_SHOT;
    	}
    }
	
	public enum AmmoCategory implements StoreItem {
		PRIMARY("Primary Ammo", "PrimaryAmmo.png"), SECONDARY("Secondary Ammo", "SecondaryAmmo.png");
		
		public static final String AMMO_DIRECTORY = Gun.GUNS_DIRECTORY + "/Ammo";
		
		private final String NAME;
		private final BufferedImage IMAGE;
		
		AmmoCategory(String name, String imagePath) {
			NAME = name;
			IMAGE = Main.getImage(AMMO_DIRECTORY + "/" + imagePath, Color.WHITE);
		}

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public int getCost() {
			return 0;
		}

		@Override
		public BufferedImage getImage() {
			return IMAGE;
		}

		@Override
		public ArrayList<String> getStoreInformation() {
			return new ArrayList<String>();
		}

		@Override
		public HoldItem getHoldItem() {
			return null;
		}

		@Override
		public void itemBought(Player buyer) {
			for (HoldItem i : buyer.getCarryItems()) {
				if (i instanceof Gun && ((Gun) i).getAmmo().getAmmoCategory() == this) {
					buyer.setMoney(buyer.getMoney() - ((Gun) i).buyAmmo(buyer.getMoney()));
					break;
				}
			}
		}

		@Override
		public StoreItem getItemCopy() {
			return this;
		}
	}
    
    public enum AmmoProjectile {
    	BULLET, PELLET;
    }
	
	public enum ReloadType {
		MAGAZINE, CARTRIDGE;
	}
	
	public static class BurstFire implements Serializable {
		private static final long serialVersionUID = 2442252132100864337L;
		
		private final int BURST_ROUNDS;
		private final double BURST_RATE_OF_FIRE;
		
		public BurstFire(int burstRounds, double burstRateOfFire) {
			BURST_ROUNDS = Math.max(burstRounds, 1);
			BURST_RATE_OF_FIRE = Math.max(burstRateOfFire, Double.MIN_VALUE);
		}
		
		public int getBurstRounds() {
			return BURST_ROUNDS;
		}
		
		public double getBurstRateOfFire() {
			return BURST_RATE_OF_FIRE;
		}
	}
	
	public interface GunType extends Weapon.WeaponType {
	}
}
