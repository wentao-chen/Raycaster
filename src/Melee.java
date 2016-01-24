import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Set;

import javax.imageio.ImageIO;


public class Melee extends Weapon {
	private static final long serialVersionUID = 6356484382353365879L;
	
	public static final String MELEE_DIRECTORY = Weapon.WEAPONS_DIRECTORY + "/Melee";

	private static final Melee DEFAULT_KNIFE = new Melee("Knife", 0, 150, new MeleeDamage.StandardMeleeDamage(15, 0.625, 40, 65, 90, 180, 0.85), 1, true, "Knife/Knife.png", "Knife/KnifeSlash.png", "Knife/knife1.wav");
	
	private final MeleeDamage DAMAGE;
	private final String IMAGE_PATH2;
	private transient BufferedImage image2;
	
	private transient boolean isShooting = false;
	private transient long lastShotTime = -1;
	
	public Melee(Melee melee) {
		this(melee.getName(), melee.getCost(), melee.getRateOfFire(), melee.getDamage(), melee.getSpeedMultiplier(), melee.canHoldTacticalShield(), melee.getImagePath(), melee.getImagePath2(), melee.getFireWeaponSoundPath(), false);
	}

	public Melee(String name, int cost, double rateOfFire, MeleeDamage damage, double speedMultipler, boolean canHoldTacticalShield, String imagePath, String imagePath2, String fireWeaponSoundPath) {
		this(name, cost, rateOfFire, damage, speedMultipler, canHoldTacticalShield, MELEE_DIRECTORY + "/" + imagePath, MELEE_DIRECTORY + "/" + imagePath2, MELEE_DIRECTORY + "/" + fireWeaponSoundPath, true);
	}
	
	private Melee(String name, int cost, double rateOfFire, MeleeDamage damage, double speedMultipler, boolean canHoldTacticalShield, String imagePath, String imagePath2, String fireWeaponSoundPath, boolean b) {
		super(name, Melee.MeleeType.MELEE, cost, rateOfFire, speedMultipler, canHoldTacticalShield, null, imagePath, fireWeaponSoundPath);
		if (damage == null) throw new IllegalArgumentException("damage cannot be null");
		DAMAGE = damage;
		IMAGE_PATH2 = imagePath2;
		this.image2 = Main.getImage(IMAGE_PATH2, Color.WHITE);
	}
	
	public static Melee createDefaultKnife() {
		return new Melee(DEFAULT_KNIFE);
	}
	
	@Override
	public MeleeDamage getDamage() {
		return DAMAGE;
	}
	
	public String getImagePath2() {
		return IMAGE_PATH2;
	}

	public BufferedImage getImage2() {
		return this.image2;
	}

	@Override
	public boolean isDropable(Player player) {
		return false;
	}
	
	@Override
	public ArrayList<String> getStoreInformation() {
		ArrayList<String> info = super.getStoreInformation();
		info.add("Damage: " + getDamage().getDefaultDamage());
		return info;
	}
	
	public boolean isShooting() {
		return this.isShooting;
	}
	
	public void drawMeleeInfo(Graphics g, int screenWidth, int screenHeight, Color color) {
		g.setColor(color);
		g.setFont(Weapon.getWeaponsInfoFont(screenWidth, screenHeight));
		FontMetrics fm = g.getFontMetrics(g.getFont());
		String nOfBulletsStr = ("- | -");
		g.drawString(nOfBulletsStr, screenWidth - 5 - fm.stringWidth(nOfBulletsStr), screenHeight - 5 - fm.getDescent());
	}
	
	@Override
	public synchronized void reset() {
		this.isShooting = false;
		this.lastShotTime = -1;
	}

	@Override
	public void stopCurrentAction() {
	}

	@Override
	public void checkKeys(Set<Integer> pressedKeys, Player player) {
	}

	@Override
	public void keyReleased(int keyCode, Player player) {
	}

	@Override
	public void keyTyped(int keyCode, Player player) {
	}
	
	private void hit(Player slasher, boolean isConsecutive, AttackType attackType, Float relativeVolume) {
		Map.Ray2D attackRay = new Map.Ray2D(slasher.getLocationX(), slasher.getLocationY(), slasher.getHorizontalDirection());
		for (Player p : slasher.getGame().getPlayers()) {
			if (!p.equals(slasher) && p.intersectsRay(attackRay)) {
				double hitHeight = slasher.getViewHeight() + Math.tan(slasher.getVerticalDirection()) * Map.findDistance2D(slasher, p);
				if (hitHeight >= p.getBottomHeight() && hitHeight <= p.getTopHeight()) {
					p.hitByMelee(this, isConsecutive, attackRay, slasher, attackType);
					playFireWeaponSound(p);
					return;
				}
			}
		}
		for (Wall w : slasher.getMap().getWallsInRange(slasher.getLocationX(), slasher.getLocationY(), slasher.getPhysicalHalfWidth() + slasher.getReachLength())) {
			if (w.intersectsRay(attackRay)) {
				double hitHeight1 = slasher.getViewHeight() + Math.tan(slasher.getVerticalDirection()) * w.getDistance(attackRay);
				double hitHeight2 = slasher.getViewHeight() + Math.tan(slasher.getVerticalDirection()) * Math.min(w.getDistance(attackRay) + w.getTopSurfaceDistance(attackRay), slasher.getReachLength());
				for (int i = 0; i < w.getNumberOfGaps(); i++) {
					if ((hitHeight1 >= w.getBottomHeight(i) || hitHeight2 >= w.getBottomHeight(i)) && (hitHeight1 <= w.getTopHeight(i) || hitHeight2 <= w.getTopHeight(i))) {
						w.hitByMelee(this, isConsecutive, attackRay, slasher, attackType);
						return;
					}
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e, Player player) {
		if (((e.getButton() == MouseEvent.BUTTON1 && !player.isTacticalShieldDeployed()) || (e.getButton() == MouseEvent.BUTTON3 && player.getTacticalShield() == null)) && System.currentTimeMillis() - this.lastShotTime >= 60000 / getRateOfFire()) {
			this.lastShotTime = System.currentTimeMillis();
			hit(player, false, e.getButton() == MouseEvent.BUTTON1 ? AttackType.SLASH : AttackType.STAB, ProjectionPlane.getSingleton().getDesiredVolume());
		}
	}

	@Override
	public void mousePressed(final MouseEvent E, final Player PLAYER) {
	}

	@Override
	public void mouseReleased(MouseEvent e, Player player) {
		if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {
			this.isShooting = false;
		}
	}

	@Override
	public void checkButtons(Set<Integer> pressedButtons, Player player) {
	}

	@Override
	public void drawImage(Graphics g, int screenWidth, int screenHeight, Color color) {
		drawMeleeInfo(g, screenWidth, screenHeight, color);
		if (System.currentTimeMillis() - this.lastShotTime < Math.floor(60000 / getRateOfFire()) - 120) {
			g.drawImage(getImage2(), 0, (int) Math.floor(screenHeight * 0.2), (int) Math.floor(screenWidth * 1), (int) Math.floor(screenHeight * 0.8), null);
			return;
		} else {
			g.drawImage(getImage(), 0, (int) Math.floor(screenHeight * 0.2), (int) Math.floor(screenWidth * 1), (int) Math.floor(screenHeight * 0.8), null);
			drawMeleeInfo(g, screenWidth, screenHeight, color);
		}
	}

	@Override
	public Double getCrossHairsFocus(Player p) {
		return null;
	}

	@Override
	public StoreItem getItemCopy() {
		return new Melee(this);
	}

	@Override
	public void itemSwitched(Player player) {
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.image2, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.image2 = ImageIO.read(in);
        this.isShooting = false;
        this.lastShotTime = -1;
    }
	
	public enum AttackType {
		SLASH, STAB;
	}

	public enum MeleeType implements Weapon.WeaponType {
		MELEE("Melee", Weapon.DEFAULT_MELEE_WEAPON_SLOT);
		
		private final String NAME;
		private final HoldItem.HoldItemSlot SLOT;
		
		private MeleeType(String name, HoldItem.HoldItemSlot slot) {
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
