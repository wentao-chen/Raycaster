import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;


public class BasicPrimaryGun extends Gun {
	private static final long serialVersionUID = -4981434342202062034L;
	
	private static final LinkedHashMap<BasicPrimaryGunType, LinkedHashSet<BasicPrimaryGun>> ADDITIONAL_BASIC_PRIMARY_GUNS = loadAdditionalBasicPrimaryGuns();
	
	private final BasicPrimaryGunType GUN_TYPE;
	
	public BasicPrimaryGun(BasicPrimaryGun basicPrimaryGun) {
		super(basicPrimaryGun);
		GUN_TYPE = basicPrimaryGun.GUN_TYPE;
	}
	
	public BasicPrimaryGun(String name, BasicPrimaryGunType basicPrimaryGunType, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, BulletDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, double accuracy, ZoomGraphicsDrawer[] zoomLevels, boolean drawCrossHairs, double bulletRadius, int projectilesPerShot, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath) {
		super(name, basicPrimaryGunType, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, false, accuracy, zoomLevels, drawCrossHairs, bulletRadius, basicPrimaryGunType.getReloadType(), new Gun.Ammo(Gun.AmmoCategory.PRIMARY, basicPrimaryGunType.getAmmoProjectile(), projectilesPerShot), ammoCostPerClip, dropItemWidth, dropItemHeight, basicPrimaryGunType.getResourcesDirectory() + "/" + imagePath, basicPrimaryGunType.getResourcesDirectory() + "/" + dropItemImagePath, basicPrimaryGunType.getResourcesDirectory() + "/" + fireWeaponSoundPath, basicPrimaryGunType.getResourcesDirectory() + "/" + reloadSoundPath, basicPrimaryGunType.getResourcesDirectory() + "/" + emptyMagazineSoundPath);
		GUN_TYPE = basicPrimaryGunType;
	}
	
	@Override
	public BasicPrimaryGunType getWeaponType() {
		return GUN_TYPE;
	}
	
	public static BasicPrimaryGun createBasicPrimaryGun(GameObjectProperties<BasicPrimaryGunProperties> properties) throws GameObjectProperties.InvalidPropertiesException {
		if (properties == null) {
			return null;
		}
		BasicPrimaryGunType gunType = null;
		for (BasicPrimaryGunType t : BasicPrimaryGunType.values()) {
			if (t.getName().equals(properties.stringProperty(BasicPrimaryGunProperties.BASIC_PRIMARY_GUN_TYPE))) {
				gunType = t;
				break;
			}
		}
		Double[] zooms = properties.getDoubleProperties(BasicPrimaryGunProperties.ZOOM);
		if (zooms == null) {
			return null;
		}
		ZoomGraphicsDrawer[] zoom = new ZoomGraphicsDrawer[zooms.length];
		for (int i = 0; i < zoom.length; i++) {
			zoom[i] = new Weapon.ZoomGraphicsDrawerScope(zooms[i], properties.doubleProperty(BasicPrimaryGunProperties.SCOPE_RATE_OF_FIRE), properties.doubleProperty(BasicPrimaryGunProperties.SCOPE_SPEED_MULTIPLIER), properties.doubleProperty(BasicPrimaryGunProperties.SCOPE_ACCURACY));
		}
		BasicPrimaryGun gun = null;
		try {
			gun = new BasicPrimaryGun(properties.stringProperty(BasicPrimaryGunProperties.NAME),
					gunType,
					properties.intProperty(BasicPrimaryGunProperties.COST),
					properties.booleanProperty(BasicPrimaryGunProperties.IS_AUTOMATIC),
					null,
					(long) properties.intProperty(BasicPrimaryGunProperties.RELOAD_TIME),
					properties.doubleProperty(BasicPrimaryGunProperties.RATE_OF_FIRE),
					properties.doubleProperty(BasicPrimaryGunProperties.MUZZLE_VELOCITY),
					new BulletDamage.StandardBulletDamage(properties.intProperty(BasicPrimaryGunProperties.DEFAULT_DAMAGE), properties.doubleProperty(BasicPrimaryGunProperties.ARMOR_PROTECTION)),
					properties.intProperty(BasicPrimaryGunProperties.MAGAZINE_CAPACITY),
					properties.intProperty(BasicPrimaryGunProperties.CARRY_CAPACITY),
					properties.doubleProperty(BasicPrimaryGunProperties.SPEED_MULTIPLIER),
					properties.doubleProperty(BasicPrimaryGunProperties.ACCURACY),
					zoom,
					properties.booleanProperty(BasicPrimaryGunProperties.CROSS_HAIRS),
					properties.doubleProperty(BasicPrimaryGunProperties.BULLET_RADIUS),
					gunType.getAmmoProjectile() == Gun.AmmoProjectile.BULLET ? 1 : 6,
					properties.intProperty(BasicPrimaryGunProperties.AMMO_COST),
					properties.doubleProperty(BasicPrimaryGunProperties.DROP_ITEM_WIDTH),
					properties.doubleProperty(BasicPrimaryGunProperties.DROP_ITEM_HEIGHT),
					properties.stringProperty(BasicPrimaryGunProperties.IMAGE_PATH),
					properties.stringProperty(BasicPrimaryGunProperties.DROP_ITEM_IMAGE_PATH),
					properties.stringProperty(BasicPrimaryGunProperties.FIRE_WEAPON_SOUND_PATH),
					properties.stringProperty(BasicPrimaryGunProperties.RELOAD_SOUND_PATH),
					properties.stringProperty(BasicPrimaryGunProperties.EMPTY_MAGAZINE_SOUND_PATH)
					);
		} catch (Exception e) {
			throw new GameObjectProperties.InvalidPropertiesException();
		}
		return gun;
	}
	
	public static BasicPrimaryGun createBasicPrimaryGun(String propertiesFilePath) throws GameObjectProperties.InvalidPropertiesException {
		if (new File(propertiesFilePath).exists()) {
			return createBasicPrimaryGun(GameObjectProperties.loadGameObject(propertiesFilePath, BasicPrimaryGunProperties.values()));
		}
		return null;
	}

	@Override
	public void addToAllGunTypeCollection() {
		ADDITIONAL_BASIC_PRIMARY_GUNS.get(getWeaponType()).add(this);
	}

	@Override
	public void drawImage(Graphics g, int screenWidth, int screenHeight, Color color) {
		if (isReloading()) {
			drawGunInfo(g, screenWidth, screenHeight, color);
			return;
		}
		if (this.lastShotTime >= 0) {
			if (System.currentTimeMillis() - this.lastShotTime >= 60000 / getRateOfFire() || isAutomatic()) {
				if (isShooting() && isAutomatic()) {
					if (System.currentTimeMillis() - this.lastShotTime >= 10000 / getRateOfFire()) {
						this.lastShotTime = -1;
					}
				} else {
					this.lastShotTime = -1;
				}
			}
			if (!isZoomed()) {
				g.drawImage(getImage(), (int) Math.floor(screenWidth * 0.7), (int) Math.floor(screenHeight * 0.7), (int) Math.floor(screenWidth * 0.4), (int) Math.floor(screenHeight * 0.4), null);
			} else {
				g.drawImage(getImage(), (int) Math.floor(screenWidth * 0.7), (int) Math.floor(screenHeight * 0.7), (int) Math.floor(screenWidth * 0.7), (int) Math.floor(screenHeight * 0.7), null);
			}
		} else {
			if (!isZoomed()) {
				g.drawImage(getImage(), (int) Math.floor(screenWidth * 0.6), (int) Math.floor(screenHeight * 0.6), (int) Math.floor(screenWidth * 0.4), (int) Math.floor(screenHeight * 0.4), null);
			} else {
				g.drawImage(getImage(), (int) Math.floor(screenWidth * 0.6), (int) Math.floor(screenHeight * 0.6), (int) Math.floor(screenWidth * 0.7), (int) Math.floor(screenHeight * 0.7), null);
			}
		}
		drawGunInfo(g, screenWidth, screenHeight, color);
	}

	@Override
	public BasicPrimaryGun getItemCopy() {
		return new BasicPrimaryGun(this);
	}
	
	public enum BasicPrimaryGunProperties implements GameObjectProperties.PropertyKey<BasicPrimaryGunProperties>, GameObjectProperties.StringIdentifier, ObjectCreatorDialog.GameObjectCreatorDialog.SaveFileDirectoryGetter {
		NAME("Name", null, GameObjectProperties.PropertyType.STRING, "name", false, false),
		BASIC_PRIMARY_GUN_TYPE("Gun Type", null, GameObjectProperties.PropertyType.ENUMERATION, "guntype", false, false, new ObjectCreatorDialog.GameObjectCreatorDialog.PropertyValueChangeListener<BasicPrimaryGunProperties>() {
			@Override
			public void propertyValueChanged(ObjectCreatorDialog.GameObjectCreatorDialog<BasicPrimaryGunProperties> panel, GameObjectProperties.PropertyKey<BasicPrimaryGunProperties> KEY) {
				String newValue = panel.getValue(KEY.getPropertyKey());
				if (newValue != null) {
					try {
						synchronized (BasicPrimaryGunProperties.class) {
							BasicPrimaryGunProperties.gunType = BasicPrimaryGunType.values()[Integer.parseInt(newValue)];
						}
					} catch (NumberFormatException e) {
						for (BasicPrimaryGunType t : BasicPrimaryGunType.values()) {
							if (newValue.equals(t.toString())) {
								synchronized (BasicPrimaryGunProperties.class) {
									BasicPrimaryGunProperties.gunType = t;
								}
								break;
							}
						}
					}
				}
				panel.updateAllDescriptions();
			}
			
		}, BasicPrimaryGunType.values()),
		COST("Cost ($)", null, GameObjectProperties.PropertyType.INTEGER, "cost", false, false),
		IS_AUTOMATIC("Automatic?", null, GameObjectProperties.PropertyType.BOOLEAN, "automatic", false, false),
		RELOAD_TIME("Reload Time (milliseconds)", null, GameObjectProperties.PropertyType.INTEGER, "reload", false, false),
		RATE_OF_FIRE("Rate of Fire (rounds per minute)", null, GameObjectProperties.PropertyType.DOUBLE, "firerate", false, false),
		SCOPE_RATE_OF_FIRE("Scope Rate of Fire (rounds per minute)", "Rate of rate when zoomed.", GameObjectProperties.PropertyType.DOUBLE, "scopefirerate", false, false),
		MUZZLE_VELOCITY("Muzzle Velocity (meters per second)", null, GameObjectProperties.PropertyType.DOUBLE, "muzzlevelocity", false, false),
		DEFAULT_DAMAGE("Default Damage", null, GameObjectProperties.PropertyType.INTEGER, "defaultdamage", false, false),
		ARMOR_PROTECTION("Armor Protection (0 <= x <= 1)", null, GameObjectProperties.PropertyType.DOUBLE, "armorprotection", false, false),
		MAGAZINE_CAPACITY("Magazine Capacity", null, GameObjectProperties.PropertyType.INTEGER, "magazinecapacity", false, false),
		CARRY_CAPACITY("Carrying Capacity", null, GameObjectProperties.PropertyType.INTEGER, "carrycapacity", false, false),
		SPEED_MULTIPLIER("Speed Multiplier", null, GameObjectProperties.PropertyType.DOUBLE, "speedmultiplier", false, false),
		SCOPE_SPEED_MULTIPLIER("Scope Speed Multiplier", "Speed Multiplier when zoomed.", GameObjectProperties.PropertyType.DOUBLE, "scopespeedmultiplier", false, false),
		ACCURACY("Accurate Distance (meters)", null, GameObjectProperties.PropertyType.DOUBLE, "accuracy", false, false),
		SCOPE_ACCURACY("Scope Accurate Distance (meters)", "Accuracy when zoomed.", GameObjectProperties.PropertyType.DOUBLE, "scopeaccuracy", false, false),
		ZOOM("Zoom Levels", null, GameObjectProperties.PropertyType.DOUBLE, "zoom", true, false),
		CROSS_HAIRS("Draw Cross Hairs", null, GameObjectProperties.PropertyType.BOOLEAN, "crosshairs", false, false),
		BULLET_RADIUS("Bullet Radius (m)", null, GameObjectProperties.PropertyType.DOUBLE, "bulletradius", false, false),
		AMMO_COST("Ammo Cost ($)", null, GameObjectProperties.PropertyType.INTEGER, "ammocost", false, false),
		DROP_ITEM_WIDTH("Drop Item Width (m)", null, GameObjectProperties.PropertyType.DOUBLE, "dropitemwidth", false, false),
		DROP_ITEM_HEIGHT("Drop Item Height (m)", null, GameObjectProperties.PropertyType.DOUBLE, "dropitemheight", false, false),
		IMAGE_PATH("Image Path", null, GameObjectProperties.PropertyType.STRING, "image", false, true),
		DROP_ITEM_IMAGE_PATH("Drop Item Image Path", null, GameObjectProperties.PropertyType.STRING, "dropitemimage", false, true),
		FIRE_WEAPON_SOUND_PATH("Fire Weapon Sound Path", null, GameObjectProperties.PropertyType.STRING, "fireweaponsound", false, true),
		RELOAD_SOUND_PATH("Reload Sound Path", null, GameObjectProperties.PropertyType.STRING, "reloadsound", false, true),
		EMPTY_MAGAZINE_SOUND_PATH("Empty Magazine Sound Path", null, GameObjectProperties.PropertyType.STRING, "emptymagazinesound", false, true);
		
		private static BasicPrimaryGunType gunType = BasicPrimaryGunType.values()[0];
		private final String TITLE;
		private final String DESCRIPTION;
		private final GameObjectProperties.PropertyType PROPERTY_TYPE;
		private final boolean IS_ARRAY;
		private final boolean IS_FILE_PATH;
		private final ObjectCreatorDialog.GameObjectCreatorDialog.PropertyValueChangeListener<BasicPrimaryGunProperties> PROPERTY_VALUE_CHANGE_LISTENER;
		private final String IDENTIFIER;
		private final Object[] POSSIBLE_VALUES;
		
		BasicPrimaryGunProperties(String title, String description, GameObjectProperties.PropertyType propertyType, String identifier, boolean isArray, boolean isFilePath) {
			this(title, description, propertyType, identifier, isArray, isFilePath, null, null);
		}
		
		BasicPrimaryGunProperties(String title, String description, GameObjectProperties.PropertyType propertyType, String identifier, boolean isArray, boolean isFilePath, ObjectCreatorDialog.GameObjectCreatorDialog.PropertyValueChangeListener<BasicPrimaryGunProperties> propertyValueChangeListener, Object[] possibleValues) {
			if (identifier == null) throw new IllegalArgumentException("identifier cannot be null");
			TITLE = title;
			DESCRIPTION = description;
			PROPERTY_TYPE = propertyType;
			IS_ARRAY = isArray;
			IS_FILE_PATH = GameObjectProperties.PropertyType.STRING.equals(getPropertyType()) && isFilePath;
			IDENTIFIER = identifier;
			PROPERTY_VALUE_CHANGE_LISTENER = propertyValueChangeListener;
			POSSIBLE_VALUES = possibleValues;
		}
		
		public String getTitle() {
			return TITLE;
		}
		
		@Override
		public String getDescription() {
			if (this == IMAGE_PATH || this == DROP_ITEM_IMAGE_PATH || this == FIRE_WEAPON_SOUND_PATH || this == RELOAD_SOUND_PATH || this == EMPTY_MAGAZINE_SOUND_PATH) {
				return BasicPrimaryGunProperties.gunType != null ? "Path within \"" + BasicPrimaryGunProperties.gunType.getResourcesDirectory() + "\"" : "";
			} else {
				return DESCRIPTION;
			}
		}

		@Override
		public GameObjectProperties.PropertyType getPropertyType() {
			return PROPERTY_TYPE;
		}

		@Override
		public BasicPrimaryGunProperties getPropertyKey() {
			return this;
		}

		@Override
		public boolean isArray() {
			return IS_ARRAY;
		}

		@Override
		public String getIdentifier() {
			return IDENTIFIER;
		}

		@Override
		public String getSaveFileDirectoryPath() {
			return IS_FILE_PATH && BasicPrimaryGunProperties.gunType != null ? BasicPrimaryGunProperties.gunType.getResourcesDirectory() : null;
		}
		
		public ObjectCreatorDialog.GameObjectCreatorDialog.PropertyValueChangeListener<BasicPrimaryGunProperties> getPropertyValueChangeListener() {
			return PROPERTY_VALUE_CHANGE_LISTENER;
		}

		@Override
		public Object[] getPossibleValues() {
			return POSSIBLE_VALUES;
		}

		@Override
		public String getSaveFileDirectory() {
			return BasicPrimaryGunProperties.gunType != null ? BasicPrimaryGunProperties.gunType.getResourcesDirectory() : null;
		}
	}
	
	public interface DefaultBasicPrimaryGuns extends Gun.DefaultGuns {
		public BasicPrimaryGun createGun();
	}
	
	public enum DefaultShotguns implements DefaultBasicPrimaryGuns {
		M3("M3", 1700, false, null, 575, 68, 381, new BulletDamage.StandardBulletDamage(19, 0.5), 8, 32, 220d / 250, 5, null, true, 0.00926725 / 4, 9, 65, 0.6, 0.192, "M3/M3.png", "M3/M3Drop.png", "M3/M3.wav", "ReloadShotgun.wav", "emptymagazine.wav"),
		XM1014("XM1014", 3000, true, null, 400, 240, 381, new BulletDamage.StandardBulletDamage(18, 0.8), 7, 32, 240d / 250, 4.8, null, true, 0.00926725 / 4, 6, 57, 0.6, 0.192, "XM1014/XM1014.png", "XM1014/XM1014Drop.png", "XM1014/XM1014.wav", "ReloadShotgun.wav", "emptymagazine.wav");
		
		private final BasicPrimaryGun DEFAULT_GUN;
		DefaultShotguns(String name, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, BulletDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, double accuracy, ZoomGraphicsDrawer[] zoomLevels, boolean drawCrossHairs, double bulletRadius, int projectilesPerRound, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath) {
			DEFAULT_GUN = new BasicPrimaryGun(name, BasicPrimaryGunType.SHOTGUN, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, accuracy, zoomLevels, drawCrossHairs, bulletRadius, projectilesPerRound, ammoCostPerClip, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath);
		}
		@Override
		public BasicPrimaryGun createGun() {
			return new BasicPrimaryGun(DEFAULT_GUN);
		}
	}
	
	public enum DefaultSubmachineGuns implements DefaultBasicPrimaryGuns {
		MAC10("MAC-10", 1400, true, null, 3200, 800, 280.111, new BulletDamage.StandardBulletDamage(28, 0.575), 30, 100, 250d / 250, 15, null, true, 0.005715, 60, 0.25, 0.25, "MAC10/MAC10.png", "MAC10/MAC10Drop.png", "MAC10/MAC10.wav", "reload.wav", "emptymagazine.wav"),
		TMP("TMP", 1250, true, null, 2100, 857, 390.144, new BulletDamage.StandardBulletDamage(25, 0.5), 30, 120, 250d / 250, 15, null, true, 0.0045, 20, 0.3, 0.2, "TMP/TMP.png", "TMP/TMPDrop.png", "TMP/TMP.wav", "reload.wav", "emptymagazine.wav"),
		MP5("MP5", 1500, true, null, 2600, 750, 345.034, new BulletDamage.StandardBulletDamage(25, 0.5), 30, 120, 250d / 250, 15, null, true, 0.0045, 20, 0.5, 0.2, "MP5/MP5.png", "MP5/MP5Drop.png", "MP5/MP5.wav", "reload.wav", "emptymagazine.wav"),
		P90("P90", 2350, true, null, 3300, 875, 218.237, new BulletDamage.StandardBulletDamage(26, 0.69), 50, 100, 245d / 250, 15, null, true, 0.00285, 50, 0.6, 0.2, "P90/P90.png", "P90/P90Drop.png", "P90/p90.wav", "reload.wav", "emptymagazine.wav"),
		UMP45("UMP-45", 1700, true, null, 3500, 571, 306.324, new BulletDamage.StandardBulletDamage(35, 0.65), 25, 100, 250d / 250, 15, null, true, 0.005715, 50, 0.4, 0.22, "UMP45/UMP45.png", "UMP45/UMP45Drop.png", "UMP45/UMP45.wav", "UMP45/UMP45Reload.wav", "emptymagazine.wav");
		
		private final BasicPrimaryGun DEFAULT_GUN;
		DefaultSubmachineGuns(String name, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, BulletDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, double accuracy, ZoomGraphicsDrawer[] zoomLevels, boolean drawCrossHairs, double bulletRadius, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath) {
			DEFAULT_GUN = new BasicPrimaryGun(name, BasicPrimaryGunType.SUBMACHINE_GUN, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, accuracy, zoomLevels, drawCrossHairs, bulletRadius, 1, ammoCostPerClip, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath);
		}
		@Override
		public BasicPrimaryGun createGun() {
			return new BasicPrimaryGun(DEFAULT_GUN);
		}
	}
	
	public enum DefaultRifles implements DefaultBasicPrimaryGuns {
		FAMAS("FAMAS", 2250, true, null, 3300, 666, 674.218, new BulletDamage.StandardBulletDamage(30, 0.7), 25, 90, 220d / 250, 21, null, true, 0.00278, 60, 0.5, 0.15, "FAMAS/FAMAS.png", "FAMAS/FAMASDrop.png", "FAMAS/famas.wav", "reload.wav", "emptymagazine.wav"),
		GALIL("Galil", 2000, true, null, 2600, 666, 613.562, new BulletDamage.StandardBulletDamage(29, 0.775), 35, 90, 210d / 250, 28, null, true, 0.00278, 70, 0.7, 0.2, "Galil/Galil.png", "Galil/GalilDrop.png", "Galil/Galil.wav", "reload.wav", "emptymagazine.wav"),
		AK47("AK-47", 2500, true, null, 2500, 600, 709.879, new BulletDamage.StandardBulletDamage(35, 0.775), 30, 90, 221d / 250, 31, null, true, 0.00381, 80, 0.6, 0.2, "AK47/AK47.png", "AK47/AK47Drop.png", "AK47/ak47.wav", "reload.wav", "emptymagazine.wav"),
		M4A1("Maverick M4A1 Carbine", 3100, true, null, 3070, 666, 883.92, new BulletDamage.StandardBulletDamage(38, 0.7), 30, 90, 230d / 250, 41, null, true, 0.00278, 60, 0.65, 0.25, "M4A1/M4A1.png", "M4A1/M4A1Drop.png", "M4A1/m4a1.wav", "reload.wav", "emptymagazine.wav"),
		KRIEG552("Krieg 552", 3500, true, null, 2900, 666, 883.92, new BulletDamage.StandardBulletDamage(32, 0.69), 30, 90, 230d / 250, 39, new ZoomGraphicsDrawer[]{
					new Weapon.ZoomGraphicsDrawerEnlarge(2.5, 429, 230d / 250, 39)
				}, true, 0.00278, 60, 0.6, 0.2, "Krieg552/Krieg552.png", "Krieg552/Krieg552Drop.png", "Krieg552/Krieg552.wav", "reload.wav", "emptymagazine.wav"),
		AUG("AUG", 3500, true, null, 3300, 666, 883.92, new BulletDamage.StandardBulletDamage(31, 0.9), 30, 90, 221d / 250, 49, new ZoomGraphicsDrawer[]{
					new Weapon.ZoomGraphicsDrawerEnlarge(2.5, 429, 221d / 250, 49)
				}, true, 0.00278, 60, 0.6, 0.3, "AUG/AUG.png", "AUG/AUGDrop.png", "AUG/aug1.wav", "reload.wav", "emptymagazine.wav"),
		SCHMIDT_SCOUT("Schmidt Scout", 2750, true, null, 2000, 48, 853.44, new BulletDamage.StandardBulletDamage(74, 0.9), 10, 90, 260d / 250, 48, new ZoomGraphicsDrawer[]{
					new Weapon.ZoomGraphicsDrawerScope(2.5, 48, 220d / 250, 96), new Weapon.ZoomGraphicsDrawerScope(6, 48, 220d / 250, 96)
				}, true, 0.00381, 30, 0.8, 0.15, "SchmidtScout/SchmidtScout.png", "SchmidtScout/SchmidtScoutDrop.png", "SchmidtScout/schmidtscout.wav", "reload.wav", "emptymagazine.wav"),
		AWP("AWP", 4750, false, null, 2500, 41, 914.4, new BulletDamage.StandardBulletDamage(115, 0.975), 10, 30, 210d / 250, 48, new ZoomGraphicsDrawer[]{
					new Weapon.ZoomGraphicsDrawerScope(3, 41, 150d / 250, 96), new Weapon.ZoomGraphicsDrawerScope(8, 41, 150d / 250, 96)
				}, false, 0.00429, 125, 0.8, 0.15, "AWP/AWP.png", "AWP/AWPDrop.png", "AWP/awp1.wav", "reload.wav", "emptymagazine.wav"),
		SG553("SG 553", 3000, true, null, 2800, 666, 883.92, new BulletDamage.StandardBulletDamage(30, 1.0), 30, 90, 210d / 250, 50, new ZoomGraphicsDrawer[]{
					new Weapon.ZoomGraphicsDrawerEnlargeScope(4, 666, 210d / 250, 50)
				}, false, 0.00278, 60, 0.6, 0.2, "SG553/SG553.png", "SG553/SG553Drop.png", "SG553/SG553.wav", "SG553/SG553Reload.wav", "emptymagazine.wav"),
		SG550("SG 550", 4200, true, null, 3800, 240, 883.92, new BulletDamage.StandardBulletDamage(69, 0.725), 30, 90, 210d / 250, 36, new ZoomGraphicsDrawer[]{
					new Weapon.ZoomGraphicsDrawerScope(5, 41, 150d / 250, 72)
				}, false, 0.00278, 60, 0.8, 0.2, "SG550/SG550.png", "SG550/SG550Drop.png", "SG550/SG550.wav", "SG550/SG550Reload.wav", "emptymagazine.wav");
		private final BasicPrimaryGun DEFAULT_GUN;
		DefaultRifles(String name, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, BulletDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, double accuracy, ZoomGraphicsDrawer[] zoomLevels, boolean drawCrossHairs, double bulletRadius, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath) {
			DEFAULT_GUN = new BasicPrimaryGun(name, BasicPrimaryGunType.RIFLE, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, accuracy, zoomLevels, drawCrossHairs, bulletRadius, 1, ammoCostPerClip, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath);
		}
		@Override
		public BasicPrimaryGun createGun() {
			return new BasicPrimaryGun(DEFAULT_GUN);
		}
	}
	
	public enum DefaultMachineGuns implements DefaultBasicPrimaryGuns {
		M249("M249", 2750, true, null, 4700, 600, 914.4, new BulletDamage.StandardBulletDamage(32, 0.8), 100, 200, 220d / 250, 22, null, true, 0.00278, 210, 0.8, 0.3, "M249/M249.png", "M249/M249Drop.png", "M249/m249.wav", "reload.wav", "emptymagazine.wav");
		
		private final BasicPrimaryGun DEFAULT_GUN;
		DefaultMachineGuns(String name, int cost, boolean isAutomatic, Gun.BurstFire burstFire, long reloadTime, double rateOfFire, double muzzleVelocity, BulletDamage damage, int magazineCapacity, int carryCapacity, double speedMultiplier, double accuracy, ZoomGraphicsDrawer[] zoomLevels, boolean drawCrossHairs, double bulletRadius, int ammoCostPerClip, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath, String reloadSoundPath, String emptyMagazineSoundPath) {
			DEFAULT_GUN = new BasicPrimaryGun(name, BasicPrimaryGunType.MACHINE_GUN, cost, isAutomatic, burstFire, reloadTime, rateOfFire, muzzleVelocity, damage, magazineCapacity, carryCapacity, speedMultiplier, accuracy, zoomLevels, drawCrossHairs, bulletRadius, 1, ammoCostPerClip, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath, reloadSoundPath, emptyMagazineSoundPath);
		}
		@Override
		public BasicPrimaryGun createGun() {
			return new BasicPrimaryGun(DEFAULT_GUN);
		}
	}
	
	public enum BasicPrimaryGunType implements Gun.GunType {
		SHOTGUN("Shotgun", Weapon.DEFAULT_PRIMARY_WEAPON_SLOT, "/Shotguns", Gun.ReloadType.CARTRIDGE, Gun.AmmoProjectile.PELLET), SUBMACHINE_GUN("Submachine Gun", Weapon.DEFAULT_PRIMARY_WEAPON_SLOT, "/SubmachineGuns", Gun.ReloadType.MAGAZINE, Gun.AmmoProjectile.BULLET), RIFLE("Rifle", Weapon.DEFAULT_PRIMARY_WEAPON_SLOT, "/Rifles", Gun.ReloadType.MAGAZINE, Gun.AmmoProjectile.BULLET), MACHINE_GUN("Machine Gun", Weapon.DEFAULT_PRIMARY_WEAPON_SLOT, "/MachineGuns", Gun.ReloadType.MAGAZINE, Gun.AmmoProjectile.BULLET);
		
		private final String NAME;
		private final HoldItem.HoldItemSlot SLOT;
		private final String RESOURCES_DIRECTORY;
		private final Gun.ReloadType RELOAD_TYPE;
		private final Gun.AmmoProjectile AMMO_PROJECTILE;
		
		private BasicPrimaryGunType(String name, HoldItem.HoldItemSlot slot, String resourcesSubDirectory, Gun.ReloadType reloadType, Gun.AmmoProjectile ammoProjectile) {
			NAME = name;
			SLOT = slot;
			RESOURCES_DIRECTORY = Gun.GUNS_DIRECTORY + resourcesSubDirectory;
			RELOAD_TYPE = reloadType;
			AMMO_PROJECTILE = ammoProjectile;
		}
		
		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public HoldItem.HoldItemSlot getHoldSlot() {
			return SLOT;
		}
		
		String getResourcesDirectory() {
			return RESOURCES_DIRECTORY;
		}
		
		public Gun.ReloadType getReloadType() {
			return RELOAD_TYPE;
		}
		
		public Gun.AmmoProjectile getAmmoProjectile() {
			return AMMO_PROJECTILE;
		}
		
		@Override
		public String toString() {
			return getName();
		}
		
		public BasicPrimaryGun[] createAllGuns() {
			LinkedHashSet<BasicPrimaryGun> additionalGuns = ADDITIONAL_BASIC_PRIMARY_GUNS.get(this);
			DefaultBasicPrimaryGuns[] defaultGuns = null;
			if (this == SHOTGUN) {
				defaultGuns = DefaultShotguns.values();
			} else if (this == SUBMACHINE_GUN) {
				defaultGuns = DefaultSubmachineGuns.values();
			} else if (this == RIFLE) {
				defaultGuns = DefaultRifles.values();
			} else if (this == MACHINE_GUN) {
				defaultGuns = DefaultMachineGuns.values();
			} else {
				defaultGuns = new DefaultBasicPrimaryGuns[0];
			}
			BasicPrimaryGun[] guns = new BasicPrimaryGun[defaultGuns.length + additionalGuns.size()];
			int i = 0;
			for (DefaultBasicPrimaryGuns g : defaultGuns) {
				guns[i++] = g.createGun();
			}
			for (BasicPrimaryGun g : additionalGuns) {
				guns[i++] = new BasicPrimaryGun(g);
			}
			return guns;
		}
	}
	
	private static LinkedHashMap<BasicPrimaryGunType, LinkedHashSet<BasicPrimaryGun>> loadAdditionalBasicPrimaryGuns() {
		LinkedHashMap<BasicPrimaryGunType, LinkedHashSet<BasicPrimaryGun>> guns = new LinkedHashMap<BasicPrimaryGunType, LinkedHashSet<BasicPrimaryGun>>();
		for (BasicPrimaryGunType t : BasicPrimaryGunType.values()) {
			guns.put(t, new LinkedHashSet<BasicPrimaryGun>());
		}
		for (BasicPrimaryGunType t : BasicPrimaryGunType.values()) {
			File[] files = new File(t.getResourcesDirectory()).listFiles();
			if (files != null) {
				for (File f : files) {
					BasicPrimaryGun g = null;
					if (f.isDirectory()) {
						try {
							for (File f2 : f.listFiles()) {
								try {
									g = createBasicPrimaryGun(f2.getAbsolutePath());
								} catch (GameObjectProperties.InvalidPropertiesException e) {
									g = null;
								}
								if (g != null) {
									break;
								}
							}
						} catch (SecurityException e) {
						}
					} else {
						try {
							g = createBasicPrimaryGun(f.getAbsolutePath());
						} catch (GameObjectProperties.InvalidPropertiesException e) {
							g = null;
						}
					}
					if (g != null) {
						guns.get(t).add(g);
					}
				}
			}
		}
		return guns;
	}
}
