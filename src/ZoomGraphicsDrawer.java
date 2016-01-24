import java.awt.Graphics;
import java.io.Serializable;


public interface ZoomGraphicsDrawer extends Serializable {
	
	public double zoomMultiplier();
	public boolean clearGraphicsForZoom();
	public void drawZoomGraphics(Graphics g, int screenWidth, int screenHeight);
	public double getRateOfFire();
	public double getSpeedMultiplier();
	public double getAccuracy();

}
