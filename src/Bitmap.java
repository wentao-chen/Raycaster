import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;


public final class Bitmap implements Serializable {
	private static final long serialVersionUID = -4789692962772801561L;
	
	private transient BufferedImage image;
	private final int RESOLUTION;
	private transient BufferedImage[] imageSlices;
	
	public Bitmap(String imagePath, int resolution) {
		this(Main.getImage(imagePath, Color.WHITE), resolution);
	}
	
	public Bitmap(BufferedImage image, int resolution) {
		if (resolution <= 0) throw new IllegalArgumentException("resolution must be greater than 0");
		this.image = image;
		RESOLUTION = Math.min(resolution, image.getWidth());
		this.imageSlices = getImageSlices(this.image, RESOLUTION);
	}

	private BufferedImage[] getImageSlices(BufferedImage image, int resolution) {
		BufferedImage[] imageSlices = new BufferedImage[Math.min(resolution, image.getWidth())];
		for (int i = 0; i < imageSlices.length; i++) {
			imageSlices[i] = image.getSubimage((int) Math.round(i * 1d * image.getWidth() / imageSlices.length), 0, (int) (Math.round((i + 1) * 1d * image.getWidth() / imageSlices.length) - Math.round(i * 1d * image.getWidth() / imageSlices.length)), image.getHeight());
		}
		return imageSlices;
	}
	
	public int getNumberOfSlices() {
		return this.imageSlices.length;
	}
	
	public BufferedImage getImageSlice(int index) {
		return this.imageSlices[Math.min(Math.max(index, 0), this.imageSlices.length - 1)];
	}
	
	public BufferedImage getImageSlice(double d) {
		return getImageSlice((int) Math.floor(d * getNumberOfSlices()));
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.image, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.image = ImageIO.read(in);
		this.imageSlices = getImageSlices(this.image, RESOLUTION);
    }
}
