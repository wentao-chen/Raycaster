import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;


public class DefuseKit implements StoreItem {
	private static final long serialVersionUID = 3885233172677194004L;
	
	public static final Color DEFUSE_BAR_COLOR = new Color(189, 183, 107, 220);
	
	private final String NAME;
	private final int COST;
	private final DroppedItem DROPPED_ITEM;
	private transient BufferedImage image = null;
	
	private static final DefuseKit DEFAULT_DEFUSE_KIT = new DefuseKit("Defuse Kit", 200, Main.MISC_DIRECTORY + "/DefuseKit.png", Main.MISC_DIRECTORY + "/DropDefuseKit.png");
	
	public DefuseKit(DefuseKit defuseKit) {
		this(defuseKit.getName(), defuseKit.getCost(), defuseKit.image, defuseKit.DROPPED_ITEM.getProjectedImage());
	}
	
	public DefuseKit(String name, int cost, String imagePath, String dropImagePath) {
		this(name, cost, Main.getImage(imagePath, Color.WHITE), Main.getImage(dropImagePath, Color.WHITE));
	}
	
	private DefuseKit(String name, int cost, BufferedImage image, BufferedImage dropImage) {
		NAME = name;
		COST = Math.abs(cost);
		this.image = image;
		DROPPED_ITEM = new DroppedDefuseKit(0.3, 0.2, dropImage);
	}
	
	public static DefuseKit createDefaultDefuseKit() {
		return new DefuseKit(DEFAULT_DEFUSE_KIT);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getCost() {
		return COST;
	}

	@Override
	public BufferedImage getImage() {
		return this.image;
	}
	
	public DroppedItem getDroppedItem() {
		return DROPPED_ITEM;
	}

	@Override
	public ArrayList<String> getStoreInformation() {
		ArrayList<String> info = new ArrayList<String>();
		info.add(getName());
		info.add("Cost: $" + getCost());
		return info;
	}

	@Override
	public HoldItem getHoldItem() {
		return null;
	}

	@Override
	public void itemBought(Player buyer) {
		if (buyer.getMoney() >= getCost()) {
			buyer.setMoney(buyer.getMoney() - getCost());
			buyer.setDefuseKit(this);
		}
	}

	@Override
	public StoreItem getItemCopy() {
		return new DefuseKit(this);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.image, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.image = ImageIO.read(in);
    }
	
	public static void drawProgressBar(Graphics g, int screenWidth, int screenHeight, Color color, double progress) {
		g.setColor(color);
		g.drawRect((int) Math.floor(screenWidth * 0.12), (int) Math.floor(screenHeight * 0.65), (int) Math.floor(screenWidth * 0.76), (int) Math.floor(screenHeight * 0.03));
		int width = (int) Math.floor(screenWidth * 0.76) - 4;
		width = (int) Math.floor(width * Math.min(Math.max(progress, 0), 1));
		g.fillRect((int) Math.floor(screenWidth * 0.12) + 2, (int) Math.floor(screenHeight * 0.65) + 2, width, (int) Math.floor(screenHeight * 0.03) - 4);
	}
	
	public static void drawDefuseKitIcon(Graphics g, Color color, int x, int y, int width, int height) {
		g.setColor(color);
		g.drawArc(x + (int) (width * 0.35), y + (int) (height * 0.1), (int) Math.ceil(width * 0.3), (int) Math.ceil(height * 0.325), 120, 300);
		g.drawArc(x + (int) (width * 0.3), y + (int) (height * 0.425), (int) Math.ceil(width * 0.4), (int) Math.ceil(height * 0.525), 300, 300);
		g.fillOval(x + (int) (width * 0.45), y + (int) (height * 0.375), (int) Math.ceil(width * 0.1), (int) Math.ceil(height * 0.1));
	}
	
	private class DroppedDefuseKit extends DroppedItem {
		private static final long serialVersionUID = 2184268046244555914L;

		public DroppedDefuseKit(double width, double height, BufferedImage projectedImage) {
			super(getName(), null, width, height, null, true, projectedImage);
		}
		
		@Override
		public Boolean willPickUp(Player player) {
			return !player.hasDefuseKit();
		}
		
		@Override
		public void itemPickedUp(Player player) {
			player.setDefuseKit(DefuseKit.this);
		}
		
		@Override
		public void itemDropped(Player player) {
			player.setDefuseKit(null);
		}
	}
}
