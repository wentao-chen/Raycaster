import java.awt.Graphics;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;


public abstract class CylinderMapItem implements MapItem {
	private static final long serialVersionUID = -58449417831536642L;
	
	private final double PHYSICAL_HALF_WIDTH;
	private final double PHYSICAL_HEIGHT;
	
	private double locationX = 0;
	private double locationY = 0;
	private double locationZ = 0;
	
	private transient BufferedImage projectedImage = null;
	
	public CylinderMapItem(double physicalHalfWidth, double physicalHeight, BufferedImage projectedImage) {
		PHYSICAL_HALF_WIDTH = Math.max(physicalHalfWidth, Double.MIN_VALUE);
		PHYSICAL_HEIGHT = Math.max(physicalHeight, Double.MIN_VALUE);
		setProjectedImage(projectedImage);
	}

	@Override
	public double getLocationX() {
		return this.locationX;
	}

	@Override
	public double getLocationY() {
		return this.locationY;
	}

	public double getBottomHeight() {
		return this.locationZ;
	}

	/**
	 * Sets a new x-coordinate for the map item.
	 * @param locationX the x-coordinate of the new location in meters
	 */
	protected synchronized void setLocationX(double locationX) {
		this.locationX = locationX;
	}

	/**
	 * Sets a new y-coordinate for the map item.
	 * @param locationY the y-coordinate of the new location in meters
	 */
	protected synchronized void setLocationY(double locationY) {
		this.locationY = locationY;
	}

	/**
	 * Sets a new z-coordinate (bottom height) for the map item.
	 * @param locationZ the z-coordinate of the new location in meters
	 */
	protected synchronized void setLocationZ(double locationZ) {
		this.locationZ = locationZ;
	}
	
	/**
	 * Sets the new location of the map item.
	 * @param locationX the x-coordinate of the new location in meters
	 * @param locationY the y-coordinate of the new location in meters
	 * @param locationZ the z-coordinate (bottom height) of the new location in meters
	 */
	protected synchronized void setLocation(double locationX, double locationY, double locationZ) {
		setLocationX(locationX);
		setLocationY(locationY);
		setLocationZ(locationZ);
	}

	/**
	 * Gets half of the physical width (radius) in meters of the item.
	 * @return the physical width of the item
	 */
	public double getPhysicalHalfWidth() {
		return PHYSICAL_HALF_WIDTH;
	}

	public double getPhysicalHeight() {
		return PHYSICAL_HEIGHT;
	}
	
	public double getTopHeight() {
		return getBottomHeight() + getPhysicalHeight();
	}

	public boolean intersectsRay(Map.Ray2D ray) {
		return ray.ptRayDist(getLocationX(), getLocationY()) < getPhysicalHalfWidth();
	}
	
	@Override
	public Point2D getIntersectionPoint(Map.Ray2D ray) {
		return ray.getIntersectionWithCircle(getLocationX(), getLocationY(), getPhysicalHalfWidth());
	}
	
	@Override
	public boolean intersects(Line2D line) {
		return line.ptSegDist(getLocationX(), getLocationY()) <= getPhysicalHalfWidth();
	}

	/*@Override
	public Double getShotDistance(Bullet b) {
		double firstHitDistance = getDistance(b.getTravelPath2D());
		double heightAtFirstHit = b.getHeightAt(firstHitDistance);
		if (heightAtFirstHit >= getBottomHeight() && heightAtFirstHit <= getTopHeight()) {
			return firstHitDistance;
		} else {
			double xyPlaneTravelledDistance = 0;
			if (b.getVerticalDirection() != Math.PI / 2 && b.getVerticalDirection() != Math.PI * 3 / 2) {
				if (heightAtFirstHit > getTopHeight()) {
					xyPlaneTravelledDistance = (getTopHeight() - b.getInitialLocationZ()) / Math.tan(b.getVerticalDirection());
				} else {
					xyPlaneTravelledDistance = (getBottomHeight() - b.getInitialLocationZ()) / Math.tan(b.getVerticalDirection());
				}
			}
			if (xyPlaneTravelledDistance >= firstHitDistance && xyPlaneTravelledDistance <= firstHitDistance + getTopSurfaceDistance(b.getTravelPath2D())) {
				return xyPlaneTravelledDistance;
			}
		}
		return null;
	}*/

	public double getTopSurfaceDistance(Point2D intersection) {
		return Math.sqrt(Math.pow((getPhysicalHalfWidth()), 2) - Math.pow(Map.findDistance(this, intersection.getX(), intersection.getY()), 2)) * 2;
	}

	public Double getTopSurfaceDistance(Map.Ray2D ray) {
		Point2D intersection = getIntersectionPoint(ray);
		if (intersection != null) {
			return getTopSurfaceDistance(intersection);
		}
		return null;
	}

	/**
	 * Calculates the distance from the starting point of a ray that passing through the base of the map item to the first intersection between the ray and the outline of the base.
	* @param ray the ray passing through the map item
	 * @return the distance (in meters) between the point and the nearest intersection of the line and the outline of the base or {@code null} if the ray does not intersect the map item
	 */
	public Double getDistance(Map.Ray2D ray) {
		Point2D intersection = getIntersectionPoint(ray);
		if (intersection != null) {
			return Map.findDistance(ray.getLocationX(), ray.getLocationY(), intersection.getX(), intersection.getY()) - getTopSurfaceDistance(intersection) / 2;
		}
		return null;
	}

	/**
	 * Gets the shape of the base of the map item.
	 * @return the shape (with dimensions in meters) of the base
	 */
	public Area getBase() {
		return new Area(new Ellipse2D.Double(getLocationX() - getPhysicalHalfWidth(), getLocationY() - getPhysicalHalfWidth(), getPhysicalHalfWidth() * 2, getPhysicalHalfWidth() * 2));
	}

	/**
	 * Draws graphics over the image.
	 * @param g the graphics to be drawn
	 * @param screenWidth the width of the screen in pixels
	 * @param screenHeight the height of the screen in pixels
	 * @param imageX the x-location in pixels of the top left corner of the projected image
	 * @param imageY the y-location in pixels of the top left corner of the projected image
	 * @param imageWidth the width of the projected image in pixels
	 * @param imageHeight the height of the projected image in pixels
	 */
	public abstract void drawOverImage(Graphics g, int screenWidth, int screenHeight, int imageX, int imageY, int imageWidth, int imageHeight);

	/**
	 * Gets the projected image of the item.
	 * @return the projected image of the item
	 */
	public BufferedImage getProjectedImage() {
		return this.projectedImage;
	}
	
	/**
	 * Sets the projected image of the player.
	 * @param image the projected image
	 */
	protected synchronized void setProjectedImage(BufferedImage image) {
		if (image == null) throw new IllegalArgumentException("projected image cannot be null");
		this.projectedImage = image;
	}

	@Override
	public final boolean baseHitByBullet(Bullet b, Game game, double distanceTraveled) {
		double locationVerticalOnWall = b.getHeightAt(distanceTraveled);
		if (locationVerticalOnWall >= getBottomHeight() && locationVerticalOnWall <= getTopHeight()) {
			return hitByBullet(b, game, distanceTraveled, false);
		}
		Double topSurfaceDistance = getTopSurfaceDistance(b.getTravelPath2D());
		if (topSurfaceDistance != null) {
			locationVerticalOnWall = b.getHeightAt(distanceTraveled + topSurfaceDistance);
			if (locationVerticalOnWall >= getBottomHeight() && locationVerticalOnWall <= getTopHeight()) {
				double distanceAtTopHeight = b.getDistanceTraveledAtHeight(getTopHeight());
				if (distanceAtTopHeight >= distanceTraveled && distanceAtTopHeight <= distanceTraveled + topSurfaceDistance) {
					return hitByBullet(b, game, distanceAtTopHeight, true);
				}
				return hitByBullet(b, game, b.getDistanceTraveledAtHeight(getBottomHeight()), true);
			}
		}
		return false;
	}

	/**
	 * Invoked when the map item is collides with the 3D path of a bullet. If this method returns {@code true}, the bullet is consumed. If this method returns {@code false}, the bullet continues to move and search for the next object that will be hit.
	 * The collision point for {@code distanceTraveled} is when the bullet collides with the map item. This may be on the edge of the item or the top or bottom surfaces
	 * @param b the bullet that hit the map item
	 * @param game the game in which the bullet hit
	 * @param distanceTraveled a positive value specifying the distance traveled on the x y plane by the bullet in meters when it initially collides with the map item
	 * @param hitTopOrBottomSurface {@code true} if the bullet first hits the top or bottom surface of the map item; otherwise {@code false}
	 * @return true if the bullet is consumed; otherwise, false
	 */
	public abstract boolean hitByBullet(Bullet b, Game game, double distanceTraveled, boolean hitTopOrBottomSurface);

	public abstract void itemColliding(CylinderMapItem collisionItem, Graphics g, int screenWidth, int screenHeight);
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
        ImageIO.write(this.projectedImage, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.projectedImage = ImageIO.read(in);
    }
}
