import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;


public class TacticalShield implements StoreItem {
	private static final long serialVersionUID = -483246184882263594L;
	
	static final String TACTICAL_SHIELD_DIRECTORY = Weapon.WEAPONS_DIRECTORY + "/TacticalShield";

	private static final TacticalShield DEFAULT_TACTICAL_SHIELD = new TacticalShield("Tactical Shield", 2200, -Math.PI / 2, 0.1, 0.2, 0.5, 0.9, 0.4, 0, 0.35, new Color(50, 50, 50), new Color(0, 0, 128, 50), null, 0.9, 0.6, "TacticalShield/TacticalShield.png", "TacticalShield/TacticalShieldView.png", Main.getImage(TACTICAL_SHIELD_DIRECTORY + "/" + "TacticalShield/DropTacticalShield.png", Color.WHITE));
	
	private final String NAME;
	private final int COST;
	private final double UNDEPLOYED_ANGLE;
	private final double SLIT_TOP_HEIGHT;
	private final double SLIT_BOTTOM_HEIGHT;
	private final double SHIELD_WIDTH;
	private final double SHIELD_HEIGHT;
	private final double SHIELD_HOLD_HEIGHT;
	private final double SHIELD_HOLD_DISTANCE;
	private final double SLIT_WIDTH;
	private final Color COLOR;
	private final Color SLIT_COLOR;
	private final Double CROSS_HAIRS_FOCUS;
	private final double SPEED_MULTIPLIER;
	private final double DEPLOYED_SPEED_MULTIPLIER;
	private final DroppedTacticalShield DROPPED_SHIELD;
	private final String IMAGE_PATH;
	private final String DEPLOYED_IMAGE_PATH;
	private transient BufferedImage image = null;
	private transient BufferedImage deployedImage = null;
	
	private boolean deployed = false;
	
	public TacticalShield(TacticalShield shield) {
		this(shield.getName(), shield.getCost(), shield.getUndeployedAngle(), shield.getSlitTopHeight(), shield.getSlitBottomHeight(), shield.getShieldWidth(), shield.getShieldHeight(), shield.getShieldHoldHeight(), shield.getShieldHoldDistance(), shield.getSlitWidth(), shield.getColor(), shield.getSlitColor(), shield.getCrossHairsFocus(), shield.getSpeedMultiplier(), shield.getDeployedSpeedMultiplier(), shield.getImagePath(), shield.getDeployedImagePath(), shield.getDroppedShield().getProjectedImage(), false);
	}
	
	public TacticalShield(String name, int cost, double undeployedAngle, double slitTopHeight, double slitBottomHeight, double shieldWidth, double shieldHeight, double shieldHoldHeight, double shieldHoldDistance, double slitWidth, Color color, Color slitColor, Double crossHairsFocus, double speedMultiplier, double deployedSpeedMultiplier, String imagePath, String deployedImagePath, BufferedImage droppedShieldImage) {
		this(name, cost,  undeployedAngle, slitTopHeight, slitBottomHeight, shieldWidth, shieldHeight, shieldHoldHeight, shieldHoldDistance, slitWidth, color, slitColor, crossHairsFocus, speedMultiplier, deployedSpeedMultiplier, TACTICAL_SHIELD_DIRECTORY + "/" + imagePath, TACTICAL_SHIELD_DIRECTORY + "/" + deployedImagePath, droppedShieldImage, true);
	}
	
	private TacticalShield(String name, int cost, double undeployedAngle, double slitTopHeight, double slitBottomHeight, double shieldWidth, double shieldHeight, double shieldHoldHeight, double shieldHoldDistance, double slitWidth, Color color, Color slitColor, Double crossHairsFocus, double speedMultiplier, double deployedSpeedMultiplier, String imagePath, String deployedImagePath, BufferedImage droppedShieldImage, boolean b) {
		NAME = name;
		COST = Math.abs(cost);
		UNDEPLOYED_ANGLE = undeployedAngle;
		SLIT_TOP_HEIGHT = Math.max(Math.min(Math.min(slitTopHeight, slitBottomHeight), 1), 0);
		SLIT_BOTTOM_HEIGHT = Math.max(Math.min(Math.max(slitTopHeight, slitBottomHeight), 1), 0);
		SHIELD_WIDTH = Math.abs(shieldWidth);
		SHIELD_HEIGHT = Math.abs(shieldHeight);
		SHIELD_HOLD_HEIGHT = Math.abs(shieldHoldHeight);
		SHIELD_HOLD_DISTANCE = Math.abs(shieldHoldDistance);
		SLIT_WIDTH = Math.abs(slitWidth);
		COLOR = color;
		SLIT_COLOR = slitColor;
		CROSS_HAIRS_FOCUS = crossHairsFocus;
		SPEED_MULTIPLIER = speedMultiplier;
		DEPLOYED_SPEED_MULTIPLIER = deployedSpeedMultiplier;
		DROPPED_SHIELD = new DroppedTacticalShield("Tactical Shield", 0.9, 0.4, droppedShieldImage);
		IMAGE_PATH = imagePath;
		this.image = Main.getImage(IMAGE_PATH, Color.WHITE);
		DEPLOYED_IMAGE_PATH = deployedImagePath;
		this.deployedImage = Main.getImage(DEPLOYED_IMAGE_PATH, null);
	}
	
	public static TacticalShield createDefaultTacticalShield() {
		return new TacticalShield(DEFAULT_TACTICAL_SHIELD);
	}
	
	public boolean isDeployed() {
		return this.deployed;
	}
	
	public void setDeployed(boolean deployed, Player p) {
		if (deployed && p.getMainHoldItem() != null) {
			p.getMainHoldItem().stopCurrentAction();
		}
		this.deployed = deployed;
	}
	
	public double getUndeployedAngle() {
		return UNDEPLOYED_ANGLE;
	}
	
	public double getCurrentShieldAngle() {
		return isDeployed() ? 0 : getUndeployedAngle();
	}

	public double getSlitTopHeight() {
		return SLIT_TOP_HEIGHT;
	}

	public double getSlitBottomHeight() {
		return SLIT_BOTTOM_HEIGHT;
	}
	
	public double getShieldWidth() {
		return SHIELD_WIDTH;
	}
	
	public double getShieldHeight() {
		return SHIELD_HEIGHT;
	}
	
	public double getShieldHoldHeight() {
		return SHIELD_HOLD_HEIGHT;
	}
	
	public double getShieldHoldDistance() {
		return SHIELD_HOLD_DISTANCE;
	}
	
	public double getSlitWidth() {
		return SLIT_WIDTH;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getCost() {
		return COST;
	}
	
	public Color getColor() {
		return COLOR;
	}
	
	public Color getSlitColor() {
		return SLIT_COLOR;
	}
	
	public Double getCrossHairsFocus() {
		return CROSS_HAIRS_FOCUS;
	}
	
	public double getDefaultSpeedMultiplier() {
		return SPEED_MULTIPLIER;
	}
	
	public double getDeployedSpeedMultiplier() {
		return DEPLOYED_SPEED_MULTIPLIER;
	}
	
	public double getSpeedMultiplier() {
		return isDeployed() ? getDeployedSpeedMultiplier() : getDefaultSpeedMultiplier();
	}
	
	public String getImagePath() {
		return IMAGE_PATH;
	}
	
	public String getDeployedImagePath() {
		return DEPLOYED_IMAGE_PATH;
	}
	
	public double getShieldBottomHeight(Player shieldHolder) {
		return shieldHolder.getBottomHeight() + shieldHolder.getPhysicalHeight() / 2 - getShieldHoldHeight();
	}

	public boolean baseHitByBullet(Bullet b, Player shieldHolder, double distance) {
		double height = b.getHeightAt(distance);
		double bottomHeight = getShieldBottomHeight(shieldHolder);
		return height >= bottomHeight && height <= bottomHeight + getShieldHeight();
	}

	@Override
	public BufferedImage getImage() {
		return this.image;
	}

	public void drawImage(Graphics g, int screenWidth, int screenHeight, double distanceToPlane, double distanceToShield, double playerSightHeight, Color color) {
		if (!isDeployed()) {
			g.drawImage(getImage(), 0, 0, (int) Math.floor(screenWidth * 0.4), screenHeight, null);
		} else if (this.deployedImage != null) {
			g.drawImage(this.deployedImage, 0, 0, screenWidth, screenHeight, null);
		} else {
			g.setColor(getSlitColor());
			int slitHalfWidth = (int) (getSlitWidth() / 2);
			int slitLeftX = Math.max(screenWidth / 2 - slitHalfWidth, 0);
			int slitWidth = Math.min(slitHalfWidth * 2, screenWidth / 2 + slitHalfWidth);
			int slitTopY = (int) (screenHeight / 2  + (playerSightHeight + getSlitTopHeight()) * distanceToPlane / distanceToShield);
			int slitBottomY = (int) (screenHeight / 2  + (playerSightHeight + getSlitBottomHeight()) * distanceToPlane / distanceToShield);
			int shieldTopY = (int) (screenHeight / 2  + (playerSightHeight) * distanceToPlane / distanceToShield);
			g.fillRect(slitLeftX, slitTopY, slitWidth, slitBottomY - slitTopY);
			g.setColor(getColor());
			g.fillRect(0, shieldTopY, slitLeftX, screenHeight);
			g.fillRect(slitLeftX + slitWidth, shieldTopY, screenWidth - slitLeftX - slitWidth, screenHeight);
			g.fillRect(0, shieldTopY, screenWidth, slitTopY);
			g.fillRect(0, slitBottomY, screenWidth, screenHeight);
		}
	}
	
	/**
	 * Called when a key released event is called while a player is holding the tactical shield. If the event is consumed, no other key released events will be called to any
	 * items held by the player.
	 * @param keyCode the key code representing the key released for the key event
	 * @param player the player holding the tactical shield
	 * @return {@code true} if the event should be consumed; otherwise, {@code false}
	 */
	public boolean keyPressed(int keyCode, Player player) {
		if (keyCode == KeyEvent.VK_G) {
			if (player != null && player.getTacticalShield() == this) {
				player.setTacticalShield(null);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Called when a mouse clicked event is called while a player is holding the tactical shield. If the event is consumed, no other mouse clicked events will be called to any
	 * items held by the player.
	 * @param e the mouse event called
	 * @param player the player holding the tactical shield
	 * @return {@code true} if the event should be consumed; otherwise, {@code false}
	 */
	public boolean mouseClicked(MouseEvent e, Player player) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			setDeployed(!isDeployed(), player);
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> getStoreInformation() {
		ArrayList<String> info = new ArrayList<String>();
		info.add("Cost: $" + getCost());
		return info;
	}

	@Override
	public HoldItem getHoldItem() {
		return null;
	}

	public DroppedItem getDroppedShield() {
		return DROPPED_SHIELD;
	}

	@Override
	public void itemBought(Player buyer) {
		if (buyer.getMoney() >= getCost()) {
			buyer.setMoney(buyer.getMoney() - getCost());
			buyer.setTacticalShield(this);
		}
	}

	@Override
	public StoreItem getItemCopy() {
		return new TacticalShield(this);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.image, "png", out);
        out.writeBoolean(this.deployedImage != null);
        if (this.deployedImage != null) {
            ImageIO.write(this.deployedImage, "png", out);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.image = ImageIO.read(in);
        boolean b = in.readBoolean();
        if (b) {
            this.deployedImage = ImageIO.read(in);
        } else {
        	this.deployedImage = null;
        }
    }
    
    private class DroppedTacticalShield extends DroppedItem {
		private static final long serialVersionUID = 3022566785805075366L;

		public DroppedTacticalShield(String displayName, double width, double height, BufferedImage projectedImage) {
			super(displayName, null, width, height, null, true, projectedImage);
		}
		
		@Override
		public Boolean willPickUp(Player player) {
			if (player.getTacticalShield() != null || (player.getBomb() != null && !player.getBomb().canHoldTacticalShield())) {
				return false;
			}
			for (HoldItem i : player.getCarryItems()) {
				if (!i.canHoldTacticalShield()) {
					return false;
				}
			}
			return true;
		}
		
		@Override
		public void itemDropped(Player player) {
			setDeployed(false, player);
		}
		
		@Override
		public void itemPickedUp(Player player) {
			player.setTacticalShield(TacticalShield.this);
		}

		@Override
		public boolean canDetectOnRadar(Player player) {
			return false;
		}

		@Override
		public Color getRadarColor(Player player) {
			return null;
		}
    }
}
