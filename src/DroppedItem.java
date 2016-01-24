import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;


public class DroppedItem extends Projectile {
	private static final long serialVersionUID = -1221223008034625986L;

	public static final double DEFAULT_DROP_VELOCITY = 2.5;
	
	private final String DISPLAY_NAME;
	private final HoldItem ITEM;
	private final Boolean WILL_PICK_UP;
	private final boolean CAN_BE_PUSHED_BY_BULLETS;
	
	public DroppedItem(String displayName, HoldItem item, double width, double height, boolean canBePushedByBullets, BufferedImage projectedImage) {
		this(displayName, item, width, height, null, canBePushedByBullets, projectedImage);
	}
	
	public DroppedItem(String displayName, HoldItem item, double width, double height, Boolean willPickUp, boolean canBePushedByBullets, BufferedImage projectedImage) {
		super(width / 2, height, projectedImage);
		DISPLAY_NAME = displayName;
		ITEM = item;
		WILL_PICK_UP = willPickUp;
		CAN_BE_PUSHED_BY_BULLETS = canBePushedByBullets;
	}
	
	@Override
	public String getDisplayName(Player player) {
		return DISPLAY_NAME;
	}
	
	public void drop(Player dropper) {
		drop(dropper, DEFAULT_DROP_VELOCITY);
	}
	
	public void drop(Player dropper, double throwVelocity) {
		itemDropped(dropper);
		launch(dropper, dropper.getLocationX(), dropper.getLocationY(), dropper.getViewHeight(), dropper.getHorizontalDirection(), dropper.getVerticalDirection(), throwVelocity, false);
		dropper.getGame().dropItem(this);
	}
	
	public HoldItem getItem() {
		return ITEM;
	}
	
	public Player getDropper() {
		return getLauncher();
	}
	
	public boolean isCompletedDrop() {
		return isVerticallySettled();
	}
	
	public Boolean willPickUp(Player player) {
		return WILL_PICK_UP;
	}

	@Override
	public void hitByMelee(Melee melee, boolean isConsecutive, Map.Ray2D ray, Player slasher, Melee.AttackType attackType) {
	}

	@Override
	public boolean hitByBullet(Bullet b, Game game, double distanceTravelled, boolean hitTopOrBottomSurface) {
		if (CAN_BE_PUSHED_BY_BULLETS) {
			final double BULLET_GUN_MASS_RATIO = 0.00130835734870317002881844380403;
			launch(game, getLocationX(), getLocationY(), getBottomHeight(), b.getHorizontalDirection() + Math.random() * Math.PI / 5 - Math.PI / 10, Math.abs(b.getVerticalDirection()), BULLET_GUN_MASS_RATIO * b.getSpeed() * Math.cos(b.getVerticalDirection()), false);
		}
		return true;
	}

	@Override
	public void hitByExplosion(ThrownWeapon w, double explodeX, double explodeY) {
	}

	public double getTopHeight() {
		return getBottomHeight() + getPhysicalHeight();
	}

	@Override
	public void drawOverImage(Graphics g, int screenWidth, int screenHeight, int imageX, int imageY, int imageWidth, int imageHeight) {
	}

	@Override
	public void itemColliding(CylinderMapItem collisionItem, Graphics g, int screenWidth, int screenHeight) {
	}

	@Override
	public boolean canDetectOnRadar(Player player) {
		return false;
	}

	@Override
	public Color getRadarColor(Player player) {
		return null;
	}
	
	public void itemPickedUp(Player player) {
		player.addCarryItems(getItem());
	}
	
	protected void itemDropped(Player player) {
	}
}
