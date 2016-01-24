import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;


public class Ceiling implements Map.MapLocation3D, MapItem, ProjectionPlane.ProjectionPlaneItem {
	private static final long serialVersionUID = 8491713566222186390L;
	
	private final int LOCATION_X;
	private final int LOCATION_Y;
	private final double LOCATION_Z;
	private final int WIDTH;
	private final int HEIGHT;
	private final double KINETIC_COEFFICIENT_OF_FRICTION;
	private final boolean SHOW_ON_RADAR;
	private final int LAVA_DAMAGE;
	private final Color CEILING_COLOR;
	private final Color FLOOR_COLOR;
	private final Rectangle2D BASE_SHAPE;
	private final boolean IS_INTERACTABLE;
	private transient Area baseArea = null;
	
	public Ceiling(int x, int y, double z, int width, int height, Color ceilingColor, Color floorColor) {
		this(x, y, z, width, height, Wall.DEFAULT_KINETIC_COEFFICIENT_OF_FRICTION, false, 0, ceilingColor, floorColor);
	}
	
	public Ceiling(int x, int y, double z, int width, int height, double kineticCoefficientOfFriction, boolean showOnRadar, int lavaDamage, Color ceilingColor, Color floorColor) {
		this(x, y, z, width, height, kineticCoefficientOfFriction, showOnRadar, lavaDamage, ceilingColor, floorColor, true);
	}
	
	Ceiling(int x, int y, double z, int width, int height, double kineticCoefficientOfFriction, boolean showOnRadar, int lavaDamage, Color ceilingColor, Color floorColor, boolean isInteractable) {
		LOCATION_X = x;
		LOCATION_Y = y;
		LOCATION_Z = z;
		WIDTH = width;
		HEIGHT = height;
		KINETIC_COEFFICIENT_OF_FRICTION = kineticCoefficientOfFriction;
		SHOW_ON_RADAR = showOnRadar;
		LAVA_DAMAGE = lavaDamage;
		CEILING_COLOR = ceilingColor;
		FLOOR_COLOR = floorColor;
		BASE_SHAPE = new Rectangle2D.Double(LOCATION_X, LOCATION_Y, WIDTH, HEIGHT);
		IS_INTERACTABLE = isInteractable;
        this.baseArea = new Area(BASE_SHAPE);
	}

	@Override
	public String getDisplayName(Player player) {
		return "Ceiling";
	}

	@Override
	public int getOpacityAlpha(Map.Ray2D ray, double verticalDirection) {
		if (verticalDirection >= 0) {
			return getCeilingColor().getAlpha();
		} else {
			return getFloorColor().getAlpha();
		}
	}

	@Override
	public double getLocationX() {
		return LOCATION_X;
	}

	@Override
	public double getLocationY() {
		return LOCATION_Y;
	}

	@Override
	public double getLocationZ() {
		return LOCATION_Z;
	}
	
	public double getWidth() {
		return WIDTH;
	}
	
	public double getHeight() {
		return HEIGHT;
	}

	@Override
	public int getNumberOfGaps() {
		return 1;
	}

	@Override
	public double getTopHeight(Point2D point, int index) {
		return getLocationZ();
	}

	@Override
	public double getBottomHeight(Point2D point, int index) {
		return getTopHeight(point, index);
	}

	@Override
	public double getDrawBottomHeight(Point2D point, int index) {
		return getBottomHeight(point, index);
	}

	public double getKineticCoefficientOfFriction() {
		return KINETIC_COEFFICIENT_OF_FRICTION;
	}

	public boolean canDetectOnRadar(Player player) {
		return SHOW_ON_RADAR;
	}

	public Color getRadarColor(Player player) {
		return canDetectOnRadar(player) ? getCeilingColor() : null;
	}

	public Color getCeilingColor() {
		return CEILING_COLOR;
	}

	public Color getFloorColor() {
		return FLOOR_COLOR;
	}

	@Override
	public Color getTopColor(Map.Ray2D ray) {
		return getFloorColor();
	}

	@Override
	public Color getBottomColor(Map.Ray2D ray) {
		return getCeilingColor();
	}
	
	public int getLavaDamage() {
		return LAVA_DAMAGE;
	}
	
	Rectangle2D getBaseShape() {
		return BASE_SHAPE;
	}
	
	public Area getBase() {
		return this.baseArea;
	}

	@Override
	public boolean intersects(Line2D line) {
		return line.intersects(getLocationX(), getLocationY(), getWidth(), getHeight());
	}
	
	public Point2D getIntersectionPoint(Map.Ray2D ray) {
		return ray.getIntersectionPoint(getBaseShape());
	}
	
	public double getTopSurfaceDistance(Map.Ray2D ray) {
		return Wall.getTopSurfaceDistance(getBaseShape(), ray);
	}
	
	boolean isInteractable() {
		return IS_INTERACTABLE;
	}

	@Override
	public Color getProjectedColor(Map.Ray2D ray) {
		return null;
	}

	@Override
	public BufferedImage getBitmap(Point2D pointOfIntersection) {
		return null;
	}

	@Override
	public Bullet.BulletMarking[] getBulletMarkings(Map.Ray2D ray, double distance) {
		return null;
	}

	@Override
	public void hitByMelee(Melee melee, boolean isConsecutive, Map.Ray2D ray, Player slasher, Melee.AttackType attackType) {
	}

	@Override
	public boolean baseHitByBullet(Bullet b, Game game, double distanceTraveled) {
		double enterHeight = b.getHeightAt(distanceTraveled);
		double exitHeight = b.getHeightAt(distanceTraveled + getTopSurfaceDistance(b.getTravelPath2D()));
		return (enterHeight <= getLocationZ() && getLocationZ() <= exitHeight) || (enterHeight >= getLocationZ() && getLocationZ() >= exitHeight);
	}

	@Override
	public void hitByExplosion(ThrownWeapon w, double explodeX, double explodeY) {
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.baseArea = new Area(getBaseShape());
    }

	@Override
	public String toString() {
		return "Ceiling [LOCATION_X=" + LOCATION_X + ", LOCATION_Y=" + LOCATION_Y + ", LOCATION_Z=" + LOCATION_Z + ", WIDTH=" + WIDTH + ", HEIGHT=" + HEIGHT
				+ ", CEILING_COLOR=" + CEILING_COLOR + ", FLOOR_COLOR=" + FLOOR_COLOR + "]";
	}
}
