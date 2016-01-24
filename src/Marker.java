import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Set;

import javax.imageio.ImageIO;


public class Marker implements HoldItem, StoreItem {
	private static final long serialVersionUID = 5249223529466439697L;
	
	private final Color COLOR;
	private final DroppedItem DROPPED_ITEM;
	
	private transient BufferedImage image = null;
	

	public Marker(Marker marker) {
		this(marker.COLOR);
	}
	
	/**
	 * Generates a marker of random color.
	 */
	public Marker() {
		this(new Color((int) Math.floor(Math.random() * 256), (int) Math.floor(Math.random() * 256), (int) Math.floor(Math.random() * 256)));
	}

	public Marker(Color color) {
		DROPPED_ITEM = new DroppedMarker(this, getName(), 0.3, 0.3, color);
		COLOR = color;
		this.image = new BufferedImage(50, 50, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = this.image.createGraphics();
		graphics.setPaint(COLOR);
		graphics.fillRect(0, 0, this.image.getWidth(), this.image.getHeight());
	}

	@Override
	public String getName() {
		return "Marker";
	}

	@Override
	public DroppedItem getDropItem() {
		return DROPPED_ITEM;
	}
	
	@Override
	public boolean canHoldTacticalShield() {
		return true;
	}

	@Override
	public boolean isDropable(Player player) {
		return true;
	}

	@Override
	public HoldItemSlot getHoldSlot() {
		return null;
	}

	@Override
	public void drawImage(Graphics g, int screenWidth, int screenHeight, Color color) {
		g.setColor(COLOR);
		g.fillRect((int) (screenWidth * 0.8), (int) (screenHeight * 0.8), (int) (screenWidth * 0.2), (int) (screenHeight * 0.2));
	}

	@Override
	public Double getCrossHairsFocus(Player p) {
		return null;
	}

	@Override
	public void reset() {
	}

	@Override
	public double getSpeedMultiplier() {
		return 1;
	}

	@Override
	public void stopCurrentAction() {
	}

	@Override
	public void keyPressed(int keyCode, Player player) {
		if (keyCode == KeyEvent.VK_G) {
			if (!(player.getMainHoldItem() instanceof Melee)) {
				HoldItem holdItem = player.getMainHoldItem();
				if (holdItem.getDropItem() != null) {
					player.removeCarryItems(holdItem);
					holdItem.getDropItem().drop(player);
				}
			}
		}
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
	public void mousePressed(MouseEvent e, Player player) {
	}

	@Override
	public void mouseReleased(MouseEvent e, Player player) {
	}

	@Override
	public void checkButtons(Set<Integer> pressedButtons, Player player) {
	}

	@Override
	public int getCost() {
		return 1;
	}

	@Override
	public BufferedImage getImage() {
		return this.image;
	}

	@Override
	public ArrayList<String> getStoreInformation() {
		ArrayList<String> info = new ArrayList<String>();
		info.add("Cost: $" + getCost());
		info.add("Drop me!");
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

	@Override
	public StoreItem getItemCopy() {
		return new Marker(this);
	}

	@Override
	public void itemSwitched(Player player) {
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.image, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.image = ImageIO.read(in);
    }

	public static class DroppedMarker extends DroppedItem {
		private static final long serialVersionUID = -4220730847765902991L;

		private DroppedMarker(Marker marker, String name, double width, double height, Color color) {
			super(name, marker, width, height, false, false, createImage(color));
		}
		
		private static BufferedImage createImage(Color color) {
			BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D graphics = image.createGraphics();
			graphics.setPaint(color);
			graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
			return image;
		}
		
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
	    }

	    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	        in.defaultReadObject();
	    }
	}
}
