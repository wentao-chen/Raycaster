import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;


public class Bench implements StoreItem {
	private static final long serialVersionUID = -4664436645456652560L;
	
	public static final Bench DEFAULT_BENCH = new Bench(9999, Main.BENCH_IMAGE_PATH);
	
	private final int COST;
	private final String IMAGE_PATH;
	private transient BufferedImage image = null;
	
	public Bench(Bench bench) {
		this(bench.COST, bench.IMAGE_PATH);
	}
	
	public Bench(int cost, String imagePath) {
		COST = Math.abs(cost);
		IMAGE_PATH = imagePath;
		this.image = Main.getImage(IMAGE_PATH, Color.WHITE);
	}

	@Override
	public String getName() {
		return "Bench";
	}

	@Override
	public BufferedImage getImage() {
		return this.image;
	}

	@Override
	public ArrayList<String> getStoreInformation() {
		ArrayList<String> info = new ArrayList<String>();
		info.add("Is Nice?: Yes");
		info.add("Gain muscles " + Math.round(Player.MUSCLE_SIZE * 100) + "%");
		info.add("Cost: $" + getCost());
		return info;
	}

	@Override
	public HoldItem getHoldItem() {
		return null;
	}

	@Override
	public int getCost() {
		return COST;
	}

	@Override
	public void itemBought(Player buyer) {
		buyer.setMoney(buyer.getMoney() - getCost());
		buyer.setMuscles(true);
	}

	@Override
	public StoreItem getItemCopy() {
		return new Bench(this);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.image, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.image = ImageIO.read(in);
    }
}
