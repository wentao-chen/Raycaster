import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.sun.glass.events.KeyEvent;


public abstract class Weapon implements StoreItem, HoldItem, CauseOfDeath {
	private static final long serialVersionUID = 8624738575236367303L;

	static final String WEAPONS_DIRECTORY = Main.RESOURCES_DIRECTORY + "/Weapons";
	
	public static final HoldItem.HoldItemSlot DEFAULT_PRIMARY_WEAPON_SLOT = new HoldItem.HoldItemSlot("Primary", KeyEvent.VK_1, 1);
	public static final HoldItem.HoldItemSlot DEFAULT_SECONDARY_WEAPON_SLOT = new HoldItem.HoldItemSlot("Secondary", KeyEvent.VK_2, 1);
	public static final HoldItem.HoldItemSlot DEFAULT_MELEE_WEAPON_SLOT = new HoldItem.HoldItemSlot("Melee", KeyEvent.VK_3, 1);
	public static final HoldItem.HoldItemSlot DEFAULT_THROWABLE_WEAPON_SLOT = new HoldItem.HoldItemSlot("Throwable", KeyEvent.VK_4, 3);
	public static final HoldItem.HoldItemSlot DEFAULT_BOMB_WEAPON_SLOT = new HoldItem.HoldItemSlot("Bomb", KeyEvent.VK_5, 13);

	private final String NAME;
	private final WeaponType WEAPON_TYPE;
	private final int COST;
	private final double RATE_OF_FIRE;
	private final double SPEED_MULTIPLIER;
	private final String IMAGE_PATH;
	private final DroppedItem DROP_ITEM;
	private final boolean CAN_HOLD_TACTICAL_SHIELD;
	private final double DROP_ITEM_WIDTH;
	private final double DROP_ITEM_HEIGHT;
	private final String DROP_ITEM_IMAGE_PATH;
	private transient BufferedImage image = null;
    private final String FIRE_WEAPON_SOUND_PATH;
	private final File FIRE_WEAPON_SOUND_FILE;
	
	public Weapon(String name, WeaponType weaponType, int cost, double rateOfFire, double speedMultipler, boolean canHoldTacticalShield, ZoomGraphicsDrawer[] zoomLevels, String imagePath, String fireWeaponSoundPath) {
		this(name, weaponType, cost, rateOfFire, speedMultipler, canHoldTacticalShield, null, 0, 0, imagePath, null, fireWeaponSoundPath);
	}
	
	public Weapon(String name, WeaponType weaponType, int cost, double rateOfFire, double speedMultipler, boolean canHoldTacticalShield, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath) {
		this(name, weaponType, cost, rateOfFire, speedMultipler, canHoldTacticalShield, null, dropItemWidth, dropItemHeight, imagePath, dropItemImagePath, fireWeaponSoundPath);
	}
	
	public Weapon(String name, WeaponType weaponType, int cost, double rateOfFire, double speedMultipler, boolean canHoldTacticalShield, ZoomGraphicsDrawer[] zoomLevels, double dropItemWidth, double dropItemHeight, String imagePath, String dropItemImagePath, String fireWeaponSoundPath) {
		if (weaponType == null) throw new IllegalArgumentException("Weapon type cannot be null");
		NAME = name == null ? "" : name;
		WEAPON_TYPE = weaponType;
		COST = Math.abs(cost);
		RATE_OF_FIRE = Math.max(rateOfFire, 1);
		SPEED_MULTIPLIER = speedMultipler;
		CAN_HOLD_TACTICAL_SHIELD = canHoldTacticalShield;
		DROP_ITEM_WIDTH = Math.abs(dropItemWidth);
		DROP_ITEM_HEIGHT = Math.abs(dropItemHeight);
		DROP_ITEM_IMAGE_PATH = dropItemImagePath;
		if (dropItemImagePath != null) {
			DROP_ITEM = new DroppedWeapon(true, Main.getImage(DROP_ITEM_IMAGE_PATH, Color.WHITE));
		} else {
			DROP_ITEM = null;
		}
		IMAGE_PATH = imagePath;
		this.image = Main.getImage(imagePath, Color.WHITE);
		FIRE_WEAPON_SOUND_PATH = fireWeaponSoundPath;
		FIRE_WEAPON_SOUND_FILE = new File(getFireWeaponSoundPath());
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public DroppedItem getDropItem() {
		return DROP_ITEM;
	}
	
	@Override
	public boolean canHoldTacticalShield() {
		return CAN_HOLD_TACTICAL_SHIELD;
	}
	
	public double getDropItemWidth() {
		return DROP_ITEM_WIDTH;
	}
	
	public double getDropItemHeight() {
		return DROP_ITEM_HEIGHT;
	}
	
	public WeaponType getWeaponType() {
		return WEAPON_TYPE;
	}
	
	public String getDropItemImagePath() {
		return DROP_ITEM_IMAGE_PATH;
	}
	
	public String getFireWeaponSoundPath() {
		return FIRE_WEAPON_SOUND_PATH;
	}
	
	@Override
	public int getCost() {
		return COST;
	}
	
	/**
	 * Gets the rate of fire of the gun in rounds per minute.
	 * @return the rate of fire
	 */
	public double getRateOfFire() {
		return getRegularRateOfFire();
	}
	
	public final double getRegularRateOfFire() {
		return RATE_OF_FIRE;
	}
	
	@Override
	public double getSpeedMultiplier() {
		return getRegularSpeedMultiplier();
	}
	
	public final double getRegularSpeedMultiplier() {
		return SPEED_MULTIPLIER;
	}

	@Override
	public HoldItem.HoldItemSlot getHoldSlot() {
		return getWeaponType().getHoldSlot();
	}
	
	public abstract Damage getDamage();
	
	public String getImagePath() {
		return IMAGE_PATH;
	}

	@Override
	public BufferedImage getImage() {
		return this.image;
	}
	
	@Override
	public ArrayList<String> getStoreInformation() {
		ArrayList<String> info = new ArrayList<String>();
		info.add("Weapon Type: " + getWeaponType().getName());
		info.add("Cost: $" + getCost());
		info.add("Rate of Fire: " + getRateOfFire() + "rpm");
		info.add("Speed: -" + (getSpeedMultiplier() < 1 ? Math.round(100 - getSpeedMultiplier() * 100) + "%" : ""));
		return info;
	}
	
	@Override
	public HoldItem getHoldItem() {
		return this;
	}
	
	@Override
	public void itemBought(Player buyer) {
		if (buyer.getMoney() >= getCost()) {
			buyer.setMoney(buyer.getMoney() - getCost());
			buyer.addCarryItems(this);
		}
	}

	public void playFireWeaponSound(Map.MapLocation2D location) {
		ProjectionPlane.playSoundFile(FIRE_WEAPON_SOUND_FILE, Map.findDistance2D(location, ProjectionPlane.getSingleton().getPlayer()));
	}

	@Override
	public void keyPressed(int keyCode, Player player) {
		if (keyCode == KeyEvent.VK_G) {
			player.dropCarryItem(player.getMainHoldItem());
		}
	}
	
	public static Font getWeaponsInfoFont(int screenWidth, int screenHeight) {
		return new Font(Main.DEFAULT_FONT, Font.BOLD, screenHeight / 25);
	}
	
	public interface WeaponType {
		public String getName();
		public HoldItem.HoldItemSlot getHoldSlot();
	}
	
	public static class ZoomGraphicsDrawerEnlarge implements ZoomGraphicsDrawer {
		private static final long serialVersionUID = -6602361401585222231L;
		private final double ZOOM_MULTIPLIER;
		private final double SCOPE_RATE_OF_FIRE;
		private final double SCOPE_SPEED_MULTIPLIER;
		private final double SCOPE_ACCURACY;
		
		public ZoomGraphicsDrawerEnlarge(double zoomMultiplier, double scopeRateOfFire, double scopeSpeedMultiplier, double scopeAccuracy) {
			ZOOM_MULTIPLIER = zoomMultiplier;
			SCOPE_RATE_OF_FIRE= scopeRateOfFire;
			SCOPE_SPEED_MULTIPLIER = scopeSpeedMultiplier;
			SCOPE_ACCURACY = scopeAccuracy;
		}

		@Override
		public double zoomMultiplier() {
			return ZOOM_MULTIPLIER;
		}

		@Override
		public double getRateOfFire() {
			return SCOPE_RATE_OF_FIRE;
		}

		@Override
		public double getSpeedMultiplier() {
			return SCOPE_SPEED_MULTIPLIER;
		}

		@Override
		public double getAccuracy() {
			return SCOPE_ACCURACY;
		}

		@Override
		public boolean clearGraphicsForZoom() {
			return false;
		}

		@Override
		public void drawZoomGraphics(Graphics g, int screenWidth, int screenHeight) {
		}
	}
	
	public static class ZoomGraphicsDrawerEnlargeScope implements ZoomGraphicsDrawer {
		private static final long serialVersionUID = -6602361401585222231L;
		private final double ZOOM_MULTIPLIER;
		private final double SCOPE_RATE_OF_FIRE;
		private final double SCOPE_SPEED_MULTIPLIER;
		private final double SCOPE_ACCURACY;
		
		public ZoomGraphicsDrawerEnlargeScope(double zoomMultiplier, double scopeRateOfFire, double scopeSpeedMultiplier, double scopeAccuracy) {
			ZOOM_MULTIPLIER = zoomMultiplier;
			SCOPE_RATE_OF_FIRE= scopeRateOfFire;
			SCOPE_SPEED_MULTIPLIER = scopeSpeedMultiplier;
			SCOPE_ACCURACY = scopeAccuracy;
		}

		@Override
		public double zoomMultiplier() {
			return ZOOM_MULTIPLIER;
		}

		@Override
		public double getRateOfFire() {
			return SCOPE_RATE_OF_FIRE;
		}

		@Override
		public double getSpeedMultiplier() {
			return SCOPE_SPEED_MULTIPLIER;
		}

		@Override
		public double getAccuracy() {
			return SCOPE_ACCURACY;
		}

		@Override
		public boolean clearGraphicsForZoom() {
			return true;
		}

		@Override
		public void drawZoomGraphics(Graphics g, int screenWidth, int screenHeight) {
	        Graphics2D g2d = (Graphics2D) g.create();
	        int outerCircleRadius = Math.min(screenWidth * 9 / 20, screenHeight * 9 / 20);
			Ellipse2D fill = new Ellipse2D.Double(screenWidth / 2 - outerCircleRadius, screenHeight / 2 - outerCircleRadius, outerCircleRadius * 2, outerCircleRadius * 2);
			int innerCircleRadius = Math.min(screenWidth / 3, screenHeight / 3);
			Ellipse2D hole = new Ellipse2D.Float(screenWidth / 2 - innerCircleRadius, screenHeight / 2 - innerCircleRadius, innerCircleRadius * 2, innerCircleRadius * 2);
			Rectangle2D fill2 = new Rectangle2D.Double(screenWidth / 2 - innerCircleRadius / 4, screenHeight / 2, innerCircleRadius / 2, screenHeight / 2 + 1);
			Area area = new Area(fill);
			area.add(new Area(fill2));
			Area blurArea = new Area(new Rectangle2D.Double(0, 0, screenWidth, screenHeight));
			blurArea.subtract(area);
			area.subtract(new Area(hole));

			g2d.setColor(new Color(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue(), 200));
			g2d.fill(blurArea);
			
			g2d.setColor(Color.BLACK);
	        g2d.fill(area);
	        g2d.drawLine(screenWidth / 2 - innerCircleRadius, screenHeight / 2, screenWidth / 2 + innerCircleRadius, screenHeight / 2);
	        g2d.drawLine(screenWidth / 2, screenHeight / 2 - innerCircleRadius, screenWidth / 2, screenHeight / 2 + innerCircleRadius);
	        g2d.dispose();
		}
	}
	
	public static class ZoomGraphicsDrawerScope implements ZoomGraphicsDrawer {
		private static final long serialVersionUID = 7993539936432508759L;
		private final double ZOOM_MULTIPLIER;
		private final double SCOPE_RATE_OF_FIRE;
		private final double SCOPE_SPEED_MULTIPLIER;
		private final double SCOPE_ACCURACY;
		
		public ZoomGraphicsDrawerScope(double zoomMultiplier, double scopeRateOfFire, double scopeSpeedMultiplier, double scopeAccuracy) {
			ZOOM_MULTIPLIER = zoomMultiplier;
			SCOPE_RATE_OF_FIRE= scopeRateOfFire;
			SCOPE_SPEED_MULTIPLIER = scopeSpeedMultiplier;
			SCOPE_ACCURACY = scopeAccuracy;
		}

		@Override
		public double zoomMultiplier() {
			return ZOOM_MULTIPLIER;
		}

		@Override
		public double getRateOfFire() {
			return SCOPE_RATE_OF_FIRE;
		}

		@Override
		public double getSpeedMultiplier() {
			return SCOPE_SPEED_MULTIPLIER;
		}

		@Override
		public double getAccuracy() {
			return SCOPE_ACCURACY;
		}

		@Override
		public boolean clearGraphicsForZoom() {
			return true;
		}

		@Override
		public void drawZoomGraphics(Graphics g, int screenWidth, int screenHeight) {
			g.setColor(Color.BLACK);
	        Graphics2D g2d = (Graphics2D) g.create();
			Rectangle fill = new Rectangle(screenWidth, screenHeight);
			int circleRadius = Math.min(screenWidth / 2, screenHeight / 2) - 5;
			Ellipse2D hole = new Ellipse2D.Float(screenWidth / 2 - circleRadius, screenHeight / 2 - circleRadius, circleRadius * 2, circleRadius * 2);
	        Area area = new Area(fill);
	        area.subtract(new Area(hole));
	        g2d.fill(area);
	        g2d.drawLine(0, screenHeight / 2, screenWidth / 2 - 6, screenHeight / 2);
	        g2d.drawLine(screenWidth / 2 + 6, screenHeight / 2, screenWidth, screenHeight / 2);
	        g2d.drawLine(screenWidth / 2, 0, screenWidth / 2, screenHeight / 2 - 6);
	        g2d.drawLine(screenWidth / 2, screenHeight / 2 + 6, screenWidth / 2, screenHeight);
	        g2d.setColor(Color.RED);
	        g2d.fillOval(screenWidth / 2 - 2, screenHeight / 2 - 2, 4, 4);
	        g2d.dispose();
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.image, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.image = ImageIO.read(in);
        if (this.image == null) {
        	this.image = Main.getImage(null, Color.WHITE);
        }
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((NAME == null) ? 0 : NAME.hashCode());
		result = prime * result + ((WEAPON_TYPE == null) ? 0 : WEAPON_TYPE.hashCode());
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
		Weapon other = (Weapon) obj;
		if (NAME == null) {
			if (other.NAME != null)
				return false;
		} else if (!NAME.equals(other.NAME))
			return false;
		if (WEAPON_TYPE != other.WEAPON_TYPE)
			return false;
		return true;
	}
	
	private class DroppedWeapon extends DroppedItem {
		private static final long serialVersionUID = 7590571238439694228L;

		public DroppedWeapon(boolean canBePushedByBullets, BufferedImage projectedImage) {
			super(getName(), Weapon.this, getDropItemWidth(), getDropItemHeight(), canBePushedByBullets, projectedImage);
		}
		
		@Override
		public Boolean willPickUp(Player player) {
			return (getHoldSlot() == null || player.getNumberOfItemsInSlot(getHoldSlot()) <= 0) && (player.getTacticalShield() == null || canHoldTacticalShield());
		}
	}
}
