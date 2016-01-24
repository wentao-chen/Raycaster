import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedHashSet;


public class Pistol extends Gun {
	private static final long serialVersionUID = 690818487947703039L;
	
	protected static final String PISTOLS_DIRECTORY = Gun.GUNS_DIRECTORY + "/Pistols";
	
	private final double DRAW_IMAGE_WIDTH;
	private final double DRAW_IMAGE_HEIGHT;
	
	private static final LinkedHashSet<Pistol> ADDITIONAL_PISTOLS = new LinkedHashSet<Pistol>();
	
	public Pistol(Pistol pistol) {
		super(pistol);
		DRAW_IMAGE_WIDTH = pistol.DRAW_IMAGE_WIDTH;
		DRAW_IMAGE_HEIGHT = pistol.DRAW_IMAGE_HEIGHT;
	}

	public Pistol(String name, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, BulletDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, boolean canHoldTacticalShield, double accuracy, boolean drawCrossHairs, double bulletRadius, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, double drawImageWidth, double drawImageHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath) {
		this(name, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, canHoldTacticalShield, accuracy, drawCrossHairs, bulletRadius, ammoCostPerClip, dropItemWidth, dropItemHeight, drawImageWidth, drawImageHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath, false);
	}

	private Pistol(String name, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, BulletDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, boolean canHoldTacticalShield, double accuracy, boolean drawCrossHairs, double bulletRadius, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, double drawImageWidth, double drawImageHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath, boolean playReloadSoundTwice) {
		super(name, PistolType.PISTOL, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, canHoldTacticalShield, accuracy, null, drawCrossHairs, bulletRadius, Gun.ReloadType.MAGAZINE, new Gun.Ammo(Gun.AmmoCategory.SECONDARY, Gun.AmmoProjectile.BULLET, 1), ammoCostPerClip, dropItemWidth, dropItemHeight, PISTOLS_DIRECTORY + "/" + imagePath, PISTOLS_DIRECTORY + "/" + dropItemImagePath, PISTOLS_DIRECTORY + "/" + fireWeaponSoundPath, PISTOLS_DIRECTORY + "/" + reloadSoundPath, PISTOLS_DIRECTORY + "/" + emptyMagazineSoundPath, playReloadSoundTwice);
		DRAW_IMAGE_WIDTH = drawImageWidth;
		DRAW_IMAGE_HEIGHT = drawImageHeight;
	}
	
	public static Pistol[] createAllPistols() {
		DefaultPistols[] defaultPistols = DefaultPistols.values();
		Pistol[] pistols = new Pistol[defaultPistols.length + ADDITIONAL_PISTOLS.size()];
		int i = 0;
		for (DefaultPistols p : defaultPistols) {
			pistols[i++] = p.createGun();
		}
		for (Pistol p : ADDITIONAL_PISTOLS) {
			pistols[i++] = new Pistol(p);
		}
		return pistols;
	}

	@Override
	public void addToAllGunTypeCollection() {
		synchronized (ADDITIONAL_PISTOLS) {
			ADDITIONAL_PISTOLS.add(this);
		}
	}

	@Override
	public void drawImage(Graphics g, int screenWidth, int screenHeight, Color color) {
		if (isReloading()) {
			drawGunInfo(g, screenWidth, screenHeight, color);
			if (PLAY_RELOAD_SOUND_TWICE) {
				if (System.currentTimeMillis() - getReloadStartTime() <= getReloadTime() / 2) {
					g.drawImage(getImage(), (int) Math.floor(screenWidth * DRAW_IMAGE_WIDTH), (int) Math.floor(screenHeight * (1 - DRAW_IMAGE_HEIGHT)), (int) -Math.floor(screenWidth * DRAW_IMAGE_WIDTH), (int) Math.floor(screenHeight * DRAW_IMAGE_HEIGHT), null);
				} else {
					g.drawImage(getImage(), (int) Math.floor(screenWidth * (1 - DRAW_IMAGE_WIDTH)), (int) Math.floor(screenHeight * (1 - DRAW_IMAGE_HEIGHT)), (int) Math.floor(screenWidth * DRAW_IMAGE_WIDTH), (int) Math.floor(screenHeight * DRAW_IMAGE_HEIGHT), null);
				}
			}
			return;
		}
		if (this.lastShotTime >= 0) {
			if (System.currentTimeMillis() - this.lastShotTime >= 30000 / getRateOfFire() || isAutomatic()) {
				this.lastShotTime = -1;
			}
			double rightSideFireShift = (PLAY_RELOAD_SOUND_TWICE ? getBullets() % 2 == 1 : true) ? 0.8 : 1;
			g.drawImage(getImage(), (int) Math.floor(screenWidth * (1 - DRAW_IMAGE_WIDTH * rightSideFireShift)), (int) Math.floor(screenHeight * (1 - DRAW_IMAGE_HEIGHT * rightSideFireShift)), (int) Math.floor(screenWidth * DRAW_IMAGE_WIDTH), (int) Math.floor(screenHeight * DRAW_IMAGE_HEIGHT), null);
			if (PLAY_RELOAD_SOUND_TWICE) {
				g.drawImage(getImage(), (int) Math.floor(screenWidth * DRAW_IMAGE_WIDTH * (1.8 - rightSideFireShift)), (int) Math.floor(screenHeight * (1 - DRAW_IMAGE_HEIGHT * (1.8 - rightSideFireShift))), -(int) Math.floor(screenWidth * DRAW_IMAGE_WIDTH), (int) Math.floor(screenHeight * DRAW_IMAGE_HEIGHT), null);
			}
		} else {
			if (isShooting() && isAutomatic()) {
				this.lastShotTime = System.currentTimeMillis();
			}
			g.drawImage(getImage(), (int) Math.floor(screenWidth * (1 - DRAW_IMAGE_WIDTH)), (int) Math.floor(screenHeight * (1 - DRAW_IMAGE_HEIGHT)), (int) Math.floor(screenWidth * DRAW_IMAGE_WIDTH), (int) Math.floor(screenHeight * DRAW_IMAGE_HEIGHT), null);
			if (PLAY_RELOAD_SOUND_TWICE) {
				g.drawImage(getImage(), (int) Math.floor(screenWidth * DRAW_IMAGE_WIDTH), (int) Math.floor(screenHeight * (1 - DRAW_IMAGE_HEIGHT)), (int) -Math.floor(screenWidth * DRAW_IMAGE_WIDTH), (int) Math.floor(screenHeight * DRAW_IMAGE_HEIGHT), null);
			}
		}
		drawGunInfo(g, screenWidth, screenHeight, color);
	}

	@Override
	public Pistol getItemCopy() {
		return new Pistol(this);
	}
	
	public enum PistolType implements Gun.GunType {
		PISTOL("Pistol", Weapon.DEFAULT_SECONDARY_WEAPON_SLOT);
		
		private final String NAME;
		private final HoldItem.HoldItemSlot SLOT;
		
		private PistolType(String name, HoldItem.HoldItemSlot slot) {
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
	
	public enum DefaultPistols implements Gun.DefaultGuns {
		USPS("USP-S", 200, false, null, 2200, 352, 420.624, new BulletDamage.StandardBulletDamage(34, 0.505), 12, 24, 240d / 250, true, 29, true, 0.005715, 25, 0.4, 0.15, 0.35, 0.35, "USPS/USPS.png", "USPS/USPSDrop.png", "USPS/USPS.wav", "USPS/USPSReload.wav", "emptymagazine.wav", false), // TODO silencer
		GLOCK18("Glock-18", 400, false, new Gun.BurstFire(3, 1200), 2200, 400, 345.034, new BulletDamage.StandardBulletDamage(24, 0.475), 20, 120, 250d / 250, true, 28, true, 0.0045, 14, 0.225, 0.15, 0.25, 0.25, "Glock18/Glock18.png", "Glock18/Glock18Drop.png", "Glock18/Glock18.wav", "Glock18/Glock18Reload.wav", "emptymagazine.wav", false),
		P228("P228", 600, false, null, 2700, 400, 426.72, new BulletDamage.StandardBulletDamage(31, 0.625), 13, 52, 250d / 250, true, 28, true, 0.0045, 25, 0.225, 0.15, 0.25, 0.25, "P228/P228.png", "P228/P228Drop.png", "P228/P228.wav", "reload.wav", "emptymagazine.wav", false),
		DESERT_EAGLE("Desert Eagle", 650, false, null, 2200, 267, 420.624, new BulletDamage.StandardBulletDamage(53, 0.932), 7, 35, 237d / 250, true, 35, true, 0.00635, 40, 0.2, 0.15, 0.25, 0.25, "DesertEagle/DesertEagle.png", "DesertEagle/DesertEagleDrop.png", "DesertEagle/deagle1.wav", "reload.wav", "emptymagazine.wav", false),
		DUAL_BERETTAS("Dual Berettas", 800, false, null, 4600, 750, 390.144, new BulletDamage.StandardBulletDamage(35, 0.525), 30, 120, 250d / 250, false, 24, true, 0.0045, 20, 0.25, 0.2, 0.3, 0.29, "DualBerettas/DualBerettas.png", "DualBerettas/DualBerettasDrop.png", "DualBerettas/DualBerettas.wav", "reload.wav", "emptymagazine.wav", true),
		FIVE_SEVEN("FN Five-seven", 750, false, null, 2700, 400, 390.144, new BulletDamage.StandardBulletDamage(19, 0.9115), 20, 100, 250d / 250, true, 19, true, 0.00285, 20, 0.2, 0.15, 0.25, 0.25, "FiveSeven/FiveSeven.png", "FiveSeven/FiveSevenDrop.png", "FiveSeven/FiveSeven.wav", "reload.wav", "emptymagazine.wav", false);
		
		private final Pistol PISTOL;
		DefaultPistols(String name, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, BulletDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, boolean canHoldTacticalShield, double accuracy, boolean drawCrossHairs, double bulletRadius, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, double drawImageWidth, double drawImageHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath, boolean playReloadSoundTwice) {
			PISTOL = new Pistol(name, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, canHoldTacticalShield, accuracy, drawCrossHairs, bulletRadius, ammoCostPerClip, dropItemWidth, dropItemHeight, drawImageWidth, drawImageHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath, playReloadSoundTwice);
		}
		@Override
		public Pistol createGun() {
			return new Pistol(PISTOL);
		}
	}
}
