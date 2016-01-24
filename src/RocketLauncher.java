import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import javax.imageio.ImageIO;


public class RocketLauncher extends Gun {
	private static final long serialVersionUID = -7829475403952101563L;

	protected static final String ROCKET_LAUNCHERS_DIRECTORY = Weapon.WEAPONS_DIRECTORY + "/RocketLaunchers";

	private final GrenadeDamage DAMAGE;
	private final double EMPTY_SPEED_MULTIPLIER;
	private final long EXPLOSION_TIME;
	private final double EXPLOSION_SIZE;
	private final double ROCKET_HALF_WIDTH;
	private final double ROCKET_HEIGHT;
	private final double DRAW_IMAGE_WIDTH;
	private final double DRAW_IMAGE_HEIGHT;
	private transient BufferedImage rocketImage;
	private transient BufferedImage explosionImage;
	
	private static final LinkedHashSet<RocketLauncher> ADDITIONAL_ROCKET_LAUNCHERS = new LinkedHashSet<RocketLauncher>();
	
	public RocketLauncher(RocketLauncher rocketLauncher) {
		super(rocketLauncher);
		DAMAGE = rocketLauncher.DAMAGE;
		EMPTY_SPEED_MULTIPLIER = rocketLauncher.EMPTY_SPEED_MULTIPLIER;
		EXPLOSION_TIME = rocketLauncher.EXPLOSION_TIME;
		EXPLOSION_SIZE = rocketLauncher.EXPLOSION_SIZE;
		ROCKET_HALF_WIDTH = rocketLauncher.ROCKET_HALF_WIDTH;
		ROCKET_HEIGHT = rocketLauncher.ROCKET_HEIGHT;
		DRAW_IMAGE_WIDTH = rocketLauncher.DRAW_IMAGE_WIDTH;
		DRAW_IMAGE_HEIGHT = rocketLauncher.DRAW_IMAGE_HEIGHT;
		this.rocketImage = rocketLauncher.rocketImage;
		this.explosionImage = rocketLauncher.explosionImage;
	}

	public RocketLauncher(String name, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, GrenadeDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, double emptySpeedMultiplier, boolean canHoldTacticalShield, double accuracy, boolean drawCrossHairs, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, double drawImageWidth, double drawImageHeight, long explosionTime, double explosionSize, double rocketWidth, double rocketHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath, String rocketImagePath, String explosionImagePath) {
		this(name, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, emptySpeedMultiplier, canHoldTacticalShield, accuracy, drawCrossHairs, ammoCostPerClip, dropItemWidth, dropItemHeight, drawImageWidth, drawImageHeight, explosionTime, explosionSize, rocketWidth, rocketHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath, rocketImagePath, explosionImagePath, false);
	}

	private RocketLauncher(String name, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, GrenadeDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, double emptySpeedMultiplier, boolean canHoldTacticalShield, double accuracy, boolean drawCrossHairs, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, double drawImageWidth, double drawImageHeight, long explosionTime, double explosionSize, double rocketWidth, double rocketHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath, String rocketImagePath, String explosionImagePath, boolean playReloadSoundTwice) {
		super(name, RocketLauncherType.BASIC_ROCKET_LAUNCHER, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, canHoldTacticalShield, accuracy, null, drawCrossHairs, 0, Gun.ReloadType.MAGAZINE, new Gun.Ammo(Gun.AmmoCategory.PRIMARY, Gun.AmmoProjectile.BULLET, 1), ammoCostPerClip, dropItemWidth, dropItemHeight, ROCKET_LAUNCHERS_DIRECTORY + "/" + imagePath, ROCKET_LAUNCHERS_DIRECTORY + "/" + dropItemImagePath, ROCKET_LAUNCHERS_DIRECTORY + "/" + fireWeaponSoundPath, ROCKET_LAUNCHERS_DIRECTORY + "/" + reloadSoundPath, ROCKET_LAUNCHERS_DIRECTORY + "/" + emptyMagazineSoundPath, playReloadSoundTwice);
		DAMAGE = damage;
		EMPTY_SPEED_MULTIPLIER = emptySpeedMultiplier;
		EXPLOSION_TIME = explosionTime;
		EXPLOSION_SIZE = explosionSize;
		ROCKET_HALF_WIDTH = rocketWidth / 2;
		ROCKET_HEIGHT = rocketHeight;
		DRAW_IMAGE_WIDTH = drawImageWidth;
		DRAW_IMAGE_HEIGHT = drawImageHeight;
		this.rocketImage = Main.getImage(ROCKET_LAUNCHERS_DIRECTORY + "/" + rocketImagePath, Color.WHITE);
		this.explosionImage = Main.getImage(ROCKET_LAUNCHERS_DIRECTORY + "/" + explosionImagePath, Color.WHITE);
	}
	
	public static RocketLauncher[] createAllRocketLaunchers() {
		DefaultRocketLaunchers[] defaultRocketLaunchers = DefaultRocketLaunchers.values();
		RocketLauncher[] rocketLaunchers = new RocketLauncher[defaultRocketLaunchers.length + ADDITIONAL_ROCKET_LAUNCHERS.size()];
		int i = 0;
		for (DefaultRocketLaunchers r : defaultRocketLaunchers) {
			rocketLaunchers[i++] = r.createGun();
		}
		for (RocketLauncher r : ADDITIONAL_ROCKET_LAUNCHERS) {
			rocketLaunchers[i++] = new RocketLauncher(r);
		}
		return rocketLaunchers;
	}

	@Override
	public void addToAllGunTypeCollection() {
		synchronized (ADDITIONAL_ROCKET_LAUNCHERS) {
			ADDITIONAL_ROCKET_LAUNCHERS.add(this);
		}
	}
	
	@Override
	public ArrayList<String> getStoreInformation() {
		ArrayList<String> info = super.getStoreInformation();
		info.add("Speed when unloaded: -" + (EMPTY_SPEED_MULTIPLIER < 1 ? Math.round(100 - EMPTY_SPEED_MULTIPLIER * 100) + "%" : ""));
		return info;
	}
	
	@Override
	public double getSpeedMultiplier() {
		return getBullets() > 0 ? super.getSpeedMultiplier() : EMPTY_SPEED_MULTIPLIER;
		
	}
	
	@Override
	protected void shoot(Player player) {
		Rocket rocket = new Rocket(this.rocketImage, this.explosionImage);
		rocket.throwWeapon(player, getMuzzleVelocity());
		player.getGame().throwWeapon(rocket);
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
	public RocketLauncher getItemCopy() {
		return new RocketLauncher(this);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.rocketImage, "png", out);
        ImageIO.write(this.explosionImage, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.rocketImage = ImageIO.read(in);
        this.explosionImage = ImageIO.read(in);
        if (this.explosionImage == null) {
        	this.explosionImage = Main.getImage(null, Color.WHITE);
        }
    }
	
	private class Rocket extends ThrownWeapon {
		private static final long serialVersionUID = 2375976545488221871L;

		public Rocket(BufferedImage projectedImage, BufferedImage explosionImage) {
			super(RocketLauncher.this, false, EXPLOSION_TIME, EXPLOSION_SIZE, ROCKET_HALF_WIDTH, ROCKET_HEIGHT, null, DAMAGE, projectedImage, explosionImage);
		}
		
		@Override
		public String getDisplayName(Player player) {
			return RocketLauncher.this.getName() + " Projectile";
		}
		
		@Override
		protected void hitMapItem(Game game, MapItem item) {
			if (!isExploding()) {
				explode();
				for (MapItem i : game.getMapItems()) {
					i.hitByExplosion(this, getLocationX(), getLocationY());
				}
			}
		}
		
		@Override
		public boolean hitByBullet(Bullet b, Game game, double distanceTraveled, boolean hitTopOrBottomSurface) {
			if (!isExploding()) {
				explode();
				for (MapItem i : game.getMapItems()) {
					i.hitByExplosion(this, getLocationX(), getLocationY());
				}
			}
			return true;
		}
	}
	
	public enum RocketLauncherType implements Gun.GunType {
		BASIC_ROCKET_LAUNCHER("Rocket Launcher", Weapon.DEFAULT_PRIMARY_WEAPON_SLOT);
		
		private final String NAME;
		private final HoldItem.HoldItemSlot SLOT;
		
		private RocketLauncherType(String name, HoldItem.HoldItemSlot slot) {
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
	
	public enum DefaultRocketLaunchers implements Gun.DefaultGuns {
		M72_LAW("M72 LAW", 1200, false, null, 0, 120, 145, new GrenadeDamage.StandardGrenadeDamage(120, 2, 6, 0.6), 1, 0, 220d / 250, 240d / 250, false, 20, true, 500, 0.9, 0.2, 0.45, 0.45, 300, 6, 0.3, 0.2, "M72Law/M72Law.png", "M72Law/M72LawDrop.png", "M72Law/M72Law.wav", null, null, "M72Law/M72LawRocket.png", "M72Law/M72LawExplosion.png", false);
		
		private final RocketLauncher ROCKET_LAUNCHER;
		
		DefaultRocketLaunchers(String name, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, GrenadeDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, double emptySpeedMultiplier, boolean canHoldTacticalShield, double accuracy, boolean drawCrossHairs, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, double drawImageWidth, double drawImageHeight, long explosionTime, double explosionSize, double rocketWidth, double rocketHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath, String rocketImagePath, String explosionImagePath, boolean playReloadSoundTwice) {
			ROCKET_LAUNCHER = new RocketLauncher(name, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, emptySpeedMultiplier, canHoldTacticalShield, accuracy, drawCrossHairs, ammoCostPerClip, dropItemWidth, dropItemHeight, drawImageWidth, drawImageHeight, explosionTime, explosionSize, rocketWidth, rocketHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath, rocketImagePath, explosionImagePath, playReloadSoundTwice);
		}
		
		@Override
		public RocketLauncher createGun() {
			return new RocketLauncher(ROCKET_LAUNCHER);
		}
	}
}
