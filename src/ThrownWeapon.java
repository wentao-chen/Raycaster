import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;


public class ThrownWeapon extends Projectile {
	private static final long serialVersionUID = 8164073917066247709L;
	
	private long throwTime = -1;
	private final Weapon WEAPON;
	private final boolean EXPANDS;
	private final long EXPLODE_TIME;
	private final double EXPLOSION_SIZE;
	private final Long FUSE;
	private final GrenadeDamage GRENADE_DAMAGE;
	private transient BufferedImage explosionImage;
	private Long explodeStartTime = null;
	
	public ThrownWeapon(ThrownWeapon weapon) {
		this(weapon.getWeapon(), weapon.expands(), weapon.getExplodeTime(), weapon.getExplosionSize(), weapon.getPhysicalHalfWidth(), weapon.getPhysicalHeight(), weapon.getFuse(), weapon.getGrenadeDamage(), weapon.getProjectedImage(), weapon.explosionImage);
	}
	
	public ThrownWeapon(ThrowableWeapon weapon, String projectedImagePath, String explosionImagePath) {
		this(weapon, weapon.expands(), weapon.getExplodeTime(), weapon.getExplosionSize(), weapon.getDefaultHalfWidth(), weapon.getDefaultHeight(), weapon.getFuse(), weapon.getDamage(), Main.getImage(projectedImagePath, Color.WHITE), Main.getImage(explosionImagePath, Color.WHITE));
	}
	
	protected ThrownWeapon(Weapon weapon, boolean expands, long explodeTime, double explosionSize, double halfWidth, double height, Long fuse, GrenadeDamage grenadeDamage, BufferedImage projectedImage, BufferedImage explosionImage) {
		super(halfWidth, height, projectedImage);
		WEAPON = weapon;
		EXPANDS = expands;
		EXPLODE_TIME = Math.abs(explodeTime);
		EXPLOSION_SIZE = explosionSize;
		FUSE = fuse;
		GRENADE_DAMAGE = grenadeDamage;
		this.explosionImage = explosionImage;
	}
	
	public void throwWeapon(Player thrower, double initialVelocity) {
		this.throwTime = System.currentTimeMillis();
		this.explodeStartTime = null;
		launch(thrower, thrower.getLocationX(), thrower.getLocationY(), thrower.getViewHeight(), thrower.getHorizontalDirection(), thrower.getVerticalDirection(), initialVelocity, true);
	}
	
	@Override
	public String getDisplayName(Player player) {
		return getWeapon().getDropItem() != null ? getWeapon().getDropItem().getDisplayName(player) : "";
	}
	
	public Weapon getWeapon() {
		return WEAPON;
	}
	
	public boolean expands() {
		return EXPANDS;
	}
	
	public long getExplodeTime() {
		return EXPLODE_TIME;
	}
	
	public double getExplosionSize() {
		return EXPLOSION_SIZE;
	}
	
	public Long getFuse() {
		return FUSE;
	}
	
	public boolean isExplosionCompleted() {
		return (getFuse() != null && getTimeInAir() >= getFuse() + getExplodeTime()) || (getFuse() == null && isExploding() && System.currentTimeMillis() - getExplodeStartTime() >= getExplodeTime());
	}
	
	public GrenadeDamage getGrenadeDamage() {
		return GRENADE_DAMAGE;
	}
	
	public boolean explode() {
		if (getLauncher() != null)  {
			getWeapon().playFireWeaponSound(this);
			this.explodeStartTime = System.currentTimeMillis();
			return true;
		}
		return false;
	}
	
	public boolean isExploding() {
		return this.explodeStartTime != null;
	}
	
	public long getExplodeStartTime() {
		return this.explodeStartTime;
	}
	
	public long getTimeInAir() {
		return System.currentTimeMillis() - this.throwTime;
	}

	@Override
	public void hitByMelee(Melee melee, boolean isConsecutive, Map.Ray2D ray, Player slasher, Melee.AttackType attackType) {
	}

	@Override
	public boolean hitByBullet(Bullet b, Game game, double distanceTravelled, boolean hitTopOrBottomSurface) {
		return true;
	}

	@Override
	public double getPhysicalHalfWidth() {
		if (isExploding()) {
			if (expands()) {
				return (System.currentTimeMillis() - getExplodeStartTime()) * 1d / getExplodeTime() * getExplosionSize();
			} else {
				return getExplosionSize();
			}
		} else {
			return super.getPhysicalHalfWidth();
		}
	}

	@Override
	public double getPhysicalHeight() {
		return (isExploding() ? (expands() ? (System.currentTimeMillis() - getExplodeStartTime()) * 1d / getExplodeTime() : 1) * getExplosionSize() : super.getPhysicalHeight());
	}

	public double getTopHeight() {
		return getBottomHeight() + getPhysicalHeight();
	}

	@Override
	public void drawOverImage(Graphics g, int screenWidth, int screenHeight, int imageX, int imageY, int imageWidth, int imageHeight) {
		if (isExploding() && expands()) {
			((Graphics2D) g).setPaint(new RadialGradientPaint(new Point2D.Float(imageX + imageWidth / 2, imageY + imageHeight / 2), Math.max(imageWidth, 1), new float[]{0, 1}, new Color[]{new Color(0, 0, 0, 255), new Color(0, 0, 0, 100)}));
			g.fillOval(imageX, imageY, imageWidth, imageHeight);
		}
	}

	@Override
	public BufferedImage getProjectedImage() {
		if (isExploding()) {
			if (expands()) {
				return null;
			} else {
				return this.explosionImage;
			}
		} else {
			return super.getProjectedImage();
		}
	}

	@Override
	public void hitByExplosion(ThrownWeapon w, double explodeX, double explodeY) {
	}

	@Override
	public void itemColliding(CylinderMapItem collisionItem, Graphics g, int screenWidth, int screenHeight) {
		if (isExploding() && getGrenadeDamage().getDefaultDamage() > 0) {
			g.setColor(new Color(156, 42, 0, 191));
			g.fillRect(0, 0, screenWidth, screenHeight);
		} else if (isExploding() && expands()) {
			((Graphics2D) g).setPaint(new RadialGradientPaint(screenWidth / 2f, screenHeight / 2f, (float) Math.min(screenWidth, screenHeight), new float[]{0, 1}, new Color[]{new Color(0, 0, 0, 255), new Color(31, 31, 31, 191)}));
			g.fillRect(0, 0, screenWidth, screenHeight);
		}
	}

	@Override
	public boolean canDetectOnRadar(Player player) {
		return false;
	}

	@Override
	public Color getRadarColor(Player player) {
		return null;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.explosionImage, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.explosionImage = ImageIO.read(in);
    }
}
