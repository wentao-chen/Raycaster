import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class Wall implements MapItem3D {
	private static final long serialVersionUID = -5100356906080242616L;
	
	private static final File BREAK_GLASS_SOUND_FILE = new File(Main.BREAK_GLASS_SOUND_FILE_PATH);
	
	static final double DEFAULT_KINETIC_COEFFICIENT_OF_FRICTION = 0.1;
	
	private final int LOCATION_X;
	private final int LOCATION_Y;
	private final int WIDTH;
	private final int HEIGHT;
	private final int GLASS_ALPHA;
	private final Color DEFAULT_COLOR;
	private final Color TOP_SURFACE_COLOR;
	private final HashMap<Compass.Cardinal, Color> WALL_SIDES_COLOR = new HashMap<Compass.Cardinal, Color>();
	private final double[] SURFACE_HEIGHTS;
	private final double KINETIC_COEFFICIENT_OF_FRICTION;
	private final boolean SHOW_ON_RADAR;
	private int lavaDamage = 0;
	private final Bitmap BITMAP;
	private final ArrayList<Bullet.BulletMarking> BULLET_MARKINGS = new ArrayList<Bullet.BulletMarking>();
	private Double temporaryBottomHeight = null;

	private final Rectangle2D BASE_SHAPE;
	private transient Area baseArea = null;
	
	/**
	 * Creates a new {@code Wall} using the default values of the {@code Builder} class.
	 * @param x the x-coordinate of the location of the wall in blocks
	 * @param y the y-coordinate of the location of the wall in blocks
	 * @see Builder
	 */
	public Wall(int x, int y) {
		this(new Builder(x, y));
	}
	
	/**
	 * Creates a new {@code Wall} using the values of the builder.
	 * @param builder the {@code Builder}
	 * @see Builder
	 */
	public Wall(Builder builder) {
		this(builder.X, builder.Y, builder.width, builder.height, builder.SURFACE_HEIGHTS.toArray(new Double[builder.SURFACE_HEIGHTS.size()]), builder.lavaDamage, builder.kineticCoefficientOfFriction, builder.showOnRadar, builder.color, builder.topColor, builder.glassAlpha, builder.WALL_SIDES_COLOR, builder.bitmap);
	}
	
	private Wall(int x, int y, int width, int height, Double[] surfaceHeights, int lavaDamage, double kineticCoefficientOfFriction, boolean showOnRadar, Color color, Color topColor, int glassAlpha, HashMap<Compass.Cardinal, Color> wallSidesColor, Bitmap bitmap) {
		if (width <= 0) throw new IllegalArgumentException("width must be greater than 0");
		if (height <= 0) throw new IllegalArgumentException("height must be greater than 0");
		LOCATION_X = x;
		LOCATION_Y = y;
		WIDTH = width;
		HEIGHT = height;
		GLASS_ALPHA = glassAlpha;
		if (surfaceHeights != null && surfaceHeights.length > 0) {
			int nOfAboveZeroHeights = 0;
			for (Double i : surfaceHeights) {
				if (i != null && i >= 0) {
					nOfAboveZeroHeights++;
				}
			}
			if (nOfAboveZeroHeights == 0) {
				SURFACE_HEIGHTS = new double[]{0};
			} else {
				if (nOfAboveZeroHeights % 2 == 0) throw new IllegalArgumentException("number of surfaces heights must be odd");
				SURFACE_HEIGHTS = new double[nOfAboveZeroHeights];
				nOfAboveZeroHeights = 0;
				for (int i = 0; i < surfaceHeights.length; i++) {
					if (surfaceHeights[i] != null && surfaceHeights[i] > 0) {
						SURFACE_HEIGHTS[nOfAboveZeroHeights++] = surfaceHeights[i];
					}
				}
				Arrays.sort(SURFACE_HEIGHTS);
			}
		} else {
			SURFACE_HEIGHTS = new double[]{0};
		}
		setLavaDamage(lavaDamage);
		KINETIC_COEFFICIENT_OF_FRICTION = Math.min(Math.abs(kineticCoefficientOfFriction), 1);
		SHOW_ON_RADAR = showOnRadar;
		if (isGlass()) {
			DEFAULT_COLOR = color != null ? new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() < 255 ? color.getAlpha() : getGlassAlpha()) : null;
			TOP_SURFACE_COLOR = topColor != null ? new Color(topColor.getRed(), topColor.getGreen(), topColor.getBlue(), topColor.getAlpha() < 255 ? topColor.getAlpha() : getGlassAlpha()) : topColor;
		} else {
			DEFAULT_COLOR = color != null ? new Color(color.getRed(), color.getGreen(), color.getBlue()) : null;
			TOP_SURFACE_COLOR = topColor != null ? new Color(topColor.getRed(), topColor.getGreen(), topColor.getBlue()) : topColor;
		}

		BASE_SHAPE = new Rectangle2D.Double(LOCATION_X, LOCATION_Y, WIDTH, HEIGHT);
		this.baseArea = new Area(BASE_SHAPE);
		for (Compass.Cardinal c : wallSidesColor.keySet()) {
			if (wallSidesColor.get(c) != null) {
				WALL_SIDES_COLOR.put(c, wallSidesColor.get(c));
			}
		}
		BITMAP = bitmap;
	}

	/**
	 * Builder class for {@code Wall}.
	 * @author Wentao
	 */
	public static final class Builder {
		private final int X;
		private final int Y;
		private int width = 1;
		private int height = 1;
		private int glassAlpha = 255;
		private final ArrayList<Double> SURFACE_HEIGHTS = new ArrayList<Double>();
		private int lavaDamage = 0;
		private double kineticCoefficientOfFriction = Wall.DEFAULT_KINETIC_COEFFICIENT_OF_FRICTION;
		private boolean showOnRadar = false;
		private Color color = null;
		private Color topColor = null;
		private final HashMap<Compass.Cardinal, Color> WALL_SIDES_COLOR = new HashMap<Compass.Cardinal, Color>();
		private Bitmap bitmap = null;
		
		public Builder(Wall wall) {
			this((int) wall.getLocationX(), (int) wall.getLocationY());
			lavaDamage(wall.getLavaDamage());
			kineticCoefficientOfFriction(wall.getKineticCoefficientOfFriction());
			color(wall.getColor());
			topColor(wall.getTopColor());
			glass(wall.getGlassAlpha());
			for (Compass.Cardinal c : wall.WALL_SIDES_COLOR.keySet()) {
				sidesColor(c, wall.WALL_SIDES_COLOR.get(c));
			}
		}
		
		public Builder(int x, int y) {
			X = x;
			Y = y;
		}
		
		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}
		
		public Builder surfaceHeights(double... surfaceHeights) {
			for (double h : surfaceHeights) {
				SURFACE_HEIGHTS.add(h);
			}
			return this;
		}
		
		public Builder removeSurfaceHeights(double... surfaceHeights) {
			for (double h : surfaceHeights) {
				SURFACE_HEIGHTS.remove(h);
			}
			return this;
		}
		
		public Builder lavaDamage(int lavaDamage) {
			this.lavaDamage = lavaDamage;
			return this;
		}
		
		public Builder showOnRadar(boolean showOnRadar) {
			this.showOnRadar = showOnRadar;
			return this;
		}
		
		public Builder kineticCoefficientOfFriction(double kineticCoefficientOfFriction) {
			this.kineticCoefficientOfFriction = kineticCoefficientOfFriction;
			return this;
		}
		
		public Builder color(Color color) {
			this.color = color;
			return this;
		}
		
		public Builder topColor(Color topColor) {
			this.topColor = topColor;
			return this;
		}
		
		public Builder sidesColor(Compass.Cardinal wall, Color color) {
			WALL_SIDES_COLOR.put(wall, color);
			return this;
		}
		
		public Builder glass(int glassAlpha) {
			this.glassAlpha = glassAlpha;
			return this;
		}
		
		public Builder bitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
			return this;
		}

		/**
		 * Creates a new {@code Wall}
		 * @return a new {@code Wall}
		 */
		public Wall build() {
			return new Wall(X, Y, this.width, this.height, SURFACE_HEIGHTS.toArray(new Double[SURFACE_HEIGHTS.size()]), this.lavaDamage, this.kineticCoefficientOfFriction, this.showOnRadar, this.color, this.topColor, this.glassAlpha, WALL_SIDES_COLOR, this.bitmap);
		}
	}
	
	@Override
	public String getDisplayName(Player player) {
		return "Wall";
	}
	
	@Override
	public double getLocationX() {
		return LOCATION_X;
	}

	@Override
	public double getLocationY() {
		return LOCATION_Y;
	}
	
	public int getBaseWidth() {
		return WIDTH;
	}
	
	public int getBaseHeight() {
		return HEIGHT;
	}
	
	public static boolean isInWallBase(Rectangle2D rect, double x, double y) {
		return x >= rect.getX() && x <= rect.getX() + rect.getWidth() && y >= rect.getY() && y <= rect.getY() + rect.getHeight();
	}
	
	public boolean isInWallBase(double x, double y) {
		return isInWallBase(getBaseShape(), x, y);
	}
	
	public boolean isGlass() {
		return getGlassAlpha() >= 0 && getGlassAlpha() < 255;
	}
	
	private int getGlassAlpha() {
		return GLASS_ALPHA;
	}

	@Override
	public double getTopHeight(int index) {
		if (SURFACE_HEIGHTS.length == 1) {
			return SURFACE_HEIGHTS[0];
		} else if (index >= Integer.MAX_VALUE / 2) {
			return SURFACE_HEIGHTS[SURFACE_HEIGHTS.length - 1];
		} else {
			return SURFACE_HEIGHTS[Math.max(Math.min(2 * index, SURFACE_HEIGHTS.length - 1), 0)];
		}
	}
	
	@Override
	public double getTopHeight(Point2D point, int index) {
		return getTopHeight(index);
	}
	
	synchronized void setTemporaryBottomHeight(Double height) {
		this.temporaryBottomHeight = height;
	}

	@Override
	public double getBottomHeight(int index) {
		double height = 0;
		if (index == 0) {
		} else if (SURFACE_HEIGHTS.length == 1) {
		} else if (index >= Integer.MAX_VALUE / 2) {
			height = SURFACE_HEIGHTS[SURFACE_HEIGHTS.length - 2];
		} else {
			height = SURFACE_HEIGHTS[Math.max(Math.min(2 * index - 1, SURFACE_HEIGHTS.length - 2), 1)];
		}
		return height;
	}
	

	@Override
	public double getDrawBottomHeight(Point2D point, int index) {
		double height = getBottomHeight(index);
		if (this.temporaryBottomHeight != null && this.temporaryBottomHeight > height) {
			return this.temporaryBottomHeight;
		}
		return height;
	}
	
	@Override
	public double getBottomHeight(Point2D point, int index) {
		return getBottomHeight(index);
	}
	
	@Override
	public int getNumberOfGaps() {
		return SURFACE_HEIGHTS.length / 2 + 1;
	}
	
	public boolean hasWallAtHeight(double height) {
		for (int i = 0; i < getNumberOfGaps(); i++) {
			if (height > getBottomHeight(i) && height < getTopHeight(i)) {
				return true;
			} else if (height < getBottomHeight(i)) {
				return false;
			}
		}
		return false;
	}
	
	public double getHighestTopHeightUnder(double height) {
		for (int i = getNumberOfGaps() - 1; i >= 0; i--) {
			if (getTopHeight(i) < height) {
				return getTopHeight(i);
			}
		}
		return 0;
	}
	
	public double getLowestBottomHeightAbove(double height) {
		for (int i = 0; i < getNumberOfGaps(); i++) {
			if (getBottomHeight(i) > height) {
				return getBottomHeight(i);
			}
		}
		return Double.MAX_VALUE;
	}
	
	public int getLavaDamage() {
		return this.lavaDamage;
	}
	
	public void setLavaDamage(int lavaDamage) {
		this.lavaDamage = lavaDamage;
	}
	
	public double getKineticCoefficientOfFriction() {
		return KINETIC_COEFFICIENT_OF_FRICTION;
	}
	
	public Color getColor() {
		return DEFAULT_COLOR;
	}
	
	@Override
	public Color getTopColor() {
		return TOP_SURFACE_COLOR;
	}
	
	@Override
	public Color getTopColor(Map.Ray2D ray) {
		return getTopColor();
	}
	
	@Override
	public Color getBottomColor(Map.Ray2D ray) {
		return getTopColor();
	}
	
	@Override
	public Color getProjectedColor(Map.Ray2D ray) {
		Color color = null;
		double x = ray.getLocationX();
		double y = ray.getLocationY();
		double angle = ray.getDirection();
		Integer horizontalWall = null;
		if (y < getLocationY()) {
			horizontalWall = (int) getLocationY();
		} else if (y >= getLocationY() + getBaseHeight()) {
			horizontalWall = (int) getLocationY() + getBaseHeight();
		}
		Double distanceToHorizontalWall = null;
		if (horizontalWall != null) {
			distanceToHorizontalWall = (y - horizontalWall) / Math.sin(angle);
		}
		Integer verticalWall = null;
		if (x < getLocationX()) {
			verticalWall = (int) getLocationX();
		} else if (x >= getLocationX() + getBaseWidth()) {
			verticalWall = (int) getLocationX() + getBaseWidth();
		}
		Double distanceToVerticalWall = null;
		if (verticalWall != null) {
			distanceToVerticalWall = (verticalWall - x) / Math.cos(angle);
		}
		Compass.Cardinal foundDirection = null;
		if (distanceToHorizontalWall == null) {
			foundDirection = x < getLocationX() ? Compass.Cardinal.WEST : Compass.Cardinal.EAST;
		} else if (distanceToVerticalWall == null) {
			foundDirection = y < getLocationY() ? Compass.Cardinal.NORTH : Compass.Cardinal.SOUTH;
		} else {
			if (distanceToHorizontalWall < distanceToVerticalWall) {
				foundDirection = x < getLocationX() ? Compass.Cardinal.WEST : Compass.Cardinal.EAST;
			} else {
				foundDirection = y < getLocationY() ? Compass.Cardinal.NORTH : Compass.Cardinal.SOUTH;
			}
		}
		color = WALL_SIDES_COLOR.get(foundDirection);
		if (color == null) {
			color = getColor();
			if (color == null) {
				return null;
			}
		}
		
		if (foundDirection == Compass.Cardinal.EAST) {
			color = ProjectionPlane.getNewColorWithIntensity(color, 0.03);
		} else if (foundDirection == Compass.Cardinal.NORTH) {
			color = ProjectionPlane.getNewColorWithIntensity(color, 0.1);
		} else if (foundDirection == Compass.Cardinal.WEST) {
			color = ProjectionPlane.getNewColorWithIntensity(color, 0.07);
		}
		
		return color;
	}

	@Override
	public BufferedImage getBitmap(Point2D pointOfIntersection) {
		if (BITMAP == null) {
			return null;
		} else {
			double locationX = (pointOfIntersection.getX() - getLocationX());
			double locationY = (pointOfIntersection.getY() - getLocationY());
			if (Math.abs(locationX) <= 0.001) {
				return BITMAP.getImageSlice(locationY / getBaseHeight());
			} else if (Math.abs(locationY) <= 0.001) {
				return BITMAP.getImageSlice(locationX / getBaseWidth());
			} else if (Math.abs(locationX - getBaseWidth()) <= 0.001) {
				return BITMAP.getImageSlice(1 - locationY / getBaseHeight());
			} else {
				return BITMAP.getImageSlice(1 - locationX / getBaseWidth());
			}
		}
	}
	
	public void setWallSidesColor(Compass.Cardinal side, Color color) {
		WALL_SIDES_COLOR.put(side, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() < 255 ? color.getAlpha() : getGlassAlpha()));
	}
	
	@Override
	public int getOpacityAlpha(Map.Ray2D ray, double verticalAngle) {
		return getGlassAlpha();
	}
	
	@Override
	public boolean isTransparent(Map.Ray2D ray) {
		return isGlass();
	}
	
	public Rectangle2D getBaseShape() {
		return BASE_SHAPE;
	}

	/**
	 * Gets the shape of the base of the map item.
	 * @return the shape (with dimensions in meters) of the base
	 */
	public Area getBase() {
		return this.baseArea;
	}
	
	public boolean intersectsRay(Map.Ray2D ray) {
		double x1;
		double y1;
		if (ray.getLocationY() < getLocationY() && ray.getLocationX() <= getLocationX() + getBaseWidth()) {
			x1 = getLocationX() + getBaseWidth();
			y1 = getLocationY();
		} else if (ray.getLocationX() < getLocationX() && ray.getLocationY() >= getLocationY()) {
			x1 = getLocationX();
			y1 = getLocationY();
		} else if (ray.getLocationY() > getLocationY() + getBaseHeight() && ray.getLocationX() >= getLocationX()) {
			x1 = getLocationX();
			y1 = getLocationY() + getBaseHeight();
		} else if (ray.getLocationX() > getLocationX() + getBaseWidth() && ray.getLocationY() <= getLocationY() + getBaseHeight()) {
			x1 = getLocationX() + getBaseWidth();
			y1 = getLocationY() + getBaseHeight();
		} else {
			return true;
		}
		double x2;
		double y2;
		if (ray.getLocationY() < getLocationY() && ray.getLocationX() >= getLocationX()) {
			x2 = getLocationX();
			y2 = getLocationY();
		} else if (ray.getLocationX() < getLocationX() && ray.getLocationY() <= getLocationY() + getBaseHeight()) {
			x2 = getLocationX();
			y2 = getLocationY() + getBaseHeight();
		} else if (ray.getLocationY() > getLocationY() + getBaseHeight() && ray.getLocationX() <= getLocationX() + getBaseWidth()) {
			x2 = getLocationX() + getBaseWidth();
			y2 = getLocationY() + getBaseHeight();
		} else if (ray.getLocationX() > getLocationX() + getBaseWidth() && ray.getLocationY() >= getLocationY()) {
			x2 = getLocationX() + getBaseWidth();
			y2 = getLocationY();
		} else {
			return true;
		}
		double angle1 = Compass.getPrincipleAngle(ray.getLocationX(), y1, x1, ray.getLocationY());
		double angle2 = Compass.getPrincipleAngle(ray.getLocationX(), y2, x2, ray.getLocationY());
		return Compass.isAngleInclusiveBetween(ray.getDirection(), angle2, angle1);
	}
	
	@Override
	public boolean intersects(Line2D line) {
		return line.intersects(getLocationX(), getLocationY(), getBaseWidth(), getBaseHeight());
	}

	@Override
	public Point2D getIntersectionPoint(Map.Ray2D ray) {
		//double distance = getDistance(ray);
		//return new Point2D.Double(ray.getLocationX() + Math.cos(ray.getDirection()) * distance, ray.getLocationY() - Math.sin(ray.getDirection()) * distance);
		return ray.getIntersectionPoint(getBaseShape());
	}
	
	/**
	 * Checks if a player (or part of a player) is above or below a wall.
	 * @param p the player to be checked
	 * @return {@code true} if the player (or part of a player) is above or below a wall; otherwise, {@code false}
	 */
	private boolean doesPlayerIntersectWall(Player p) {
		Area player = new Area(p.getBase());
		player.intersect(getBase());
		return !player.isEmpty();
	}
	
	private void breakGlass(Game game) {
		if (isGlass()) {
			final double SOUND_RADIUS = 20;
			for (int i = 0; i < SURFACE_HEIGHTS.length; i++) {
				SURFACE_HEIGHTS[i] = 0;
			}
			for (Player p : game.getPlayers()) {
				ProjectionPlane.playSoundFile(ProjectionPlane.getSingleton().getDesiredVolume() != null ? (float) ((1 - Map.findDistance2D(this, p) / SOUND_RADIUS) * (ProjectionPlane.getSingleton().getDesiredVolume() - SettingsMenu.MIN_DECIBEL_VOLUME) + SettingsMenu.MIN_DECIBEL_VOLUME) : null, BREAK_GLASS_SOUND_FILE);
				if (doesPlayerIntersectWall(p)) {
					p.updatePosition();
				}
			}
		}
	}
	
	public Bullet.BulletMarking[] getBulletMarkings(Map.Ray2D ray) {
		return getBulletMarkings(ray, getDistance(ray));
	}
	
	@Override
	public Bullet.BulletMarking[] getBulletMarkings(Map.Ray2D ray, double distance) {
		return Bullet.BulletMarking.getBulletMarkings(BULLET_MARKINGS, ray, distance);
	}
	
	public Bullet.BulletMarking[] getAllBulletMarkings() {
		return BULLET_MARKINGS.toArray(new Bullet.BulletMarking[BULLET_MARKINGS.size()]);
	}
	
	public void clearAllBulletMarkings() {
		synchronized (BULLET_MARKINGS) {
			BULLET_MARKINGS.clear();
		}
	}
	
	public Compass.Cardinal getWallDirection(Map.Ray2D ray) {
		double x = ray.getLocationX();
		double y = ray.getLocationY();
		double angle = ray.getDirection();
		Integer horizontalWall = null;
		if (y < getLocationY()) {
			horizontalWall = (int) getLocationY();
		} else if (y >= getLocationY() + getBaseHeight()) {
			horizontalWall = (int) getLocationY() + getBaseHeight();
		}
		Double distanceToHorizontalWall = null;
		if (horizontalWall != null) {
			distanceToHorizontalWall = (y - horizontalWall) / Math.sin(angle);
		}
		Integer verticalWall = null;
		if (x < getLocationX()) {
			verticalWall = (int) getLocationX();
		} else if (x >= getLocationX() + getBaseWidth()) {
			verticalWall = (int) getLocationX() + getBaseWidth();
		}
		Double distanceToVerticalWall = null;
		if (verticalWall != null) {
			distanceToVerticalWall = (verticalWall - x) / Math.cos(angle);
		}
		if (distanceToHorizontalWall == null) {
			return x < getLocationX() ? Compass.Cardinal.WEST : Compass.Cardinal.EAST;
		} else if (distanceToVerticalWall == null) {
			return y < getLocationY() ? Compass.Cardinal.NORTH : Compass.Cardinal.SOUTH;
		} else {
			if (distanceToHorizontalWall < distanceToVerticalWall) {
				return x < getLocationX() ? Compass.Cardinal.WEST : Compass.Cardinal.EAST;
			} else {
				return y < getLocationY() ? Compass.Cardinal.NORTH : Compass.Cardinal.SOUTH;
			}
		}
	}
	
	@Override
	public void hitByMelee(Melee melee, boolean isConsecutive, Map.Ray2D ray, Player slasher, Melee.AttackType attackType) {
		breakGlass(slasher.getGame());
	}

	@Override
	public boolean baseHitByBullet(Bullet b, Game game, double distanceTraveled) {
		double locationVerticalOnWall = b.getHeightAt(distanceTraveled);
		if (hasWallAtHeight(locationVerticalOnWall)) {
			if (!isGlass()) {
				synchronized (BULLET_MARKINGS) {
					BULLET_MARKINGS.add(new Bullet.BulletMarking(b.getTravelPath2D().getLocationXAtDistance(distanceTraveled), b.getTravelPath2D().getLocationYAtDistance(distanceTraveled), locationVerticalOnWall, b.getRadius()));
				}
			}
		} else {
			double exitHeight = b.getHeightAt(distanceTraveled + getTopSurfaceDistance(b.getTravelPath2D()));
			for (double h : SURFACE_HEIGHTS) {
				if ((locationVerticalOnWall <= h && h <= exitHeight) || (locationVerticalOnWall >= h && h >= exitHeight)) {
					breakGlass(game);
					return true;
				}
			}
			return false;
		}
		breakGlass(game);
		return true;
	}

	@Override
	public void hitByExplosion(ThrownWeapon w, double explodeX, double explodeY) {
		if (w.getGrenadeDamage().getDamage(Map.findDistance(this, explodeX, explodeY), 0) > 0) {
			breakGlass(w.getGame());
		}
	}

	/**
	 * Calculates the distance from the starting point of a ray that passing through the base of the map item to the first intersection between the ray and the outline of the base.
	* @param ray the ray passing through the map item
	 * @return the distance (in meters) between the point and the nearest intersection of the line and the outline of the base or {@code null} if the ray does not intersect the map item
	 */
	public Double getDistance(Map.Ray2D ray) {
		double x = ray.getLocationX();
		double y = ray.getLocationY();
		double angle = ray.getDirection();
		if (isInWallBase(x, y)) {
			return 0d;
		} else {
			Integer horizontalWall = null;
			if (y < getLocationY()) {
				horizontalWall = (int) getLocationY();
			} else if (y >= getLocationY() + getBaseHeight()) {
				horizontalWall = (int) getLocationY() + getBaseHeight();
			}
			Double distanceToHorizontalWall = null;
			if (horizontalWall != null) {
				distanceToHorizontalWall = (y - horizontalWall) / Math.sin(angle);
			}
			Integer verticalWall = null;
			if (x < getLocationX()) {
				verticalWall = (int) getLocationX();
			} else if (x >= getLocationX() + getBaseWidth()) {
				verticalWall = (int) getLocationX() + getBaseWidth();
			}
			Double distanceToVerticalWall = null;
			if (verticalWall != null) {
				distanceToVerticalWall = (verticalWall - x) / Math.cos(angle);
			}
			if (distanceToHorizontalWall == null) {
				return distanceToVerticalWall;
			} else if (distanceToVerticalWall == null) {
				return distanceToHorizontalWall;
			} else {
				return Math.max(distanceToHorizontalWall, distanceToVerticalWall);
			}
		}
	}

	@Override
	public double getClosestDistance(double x, double y) {
		Integer closestY = null;
		if (y < getLocationY()) {
			closestY = (int) getLocationY();
		} else if (y > getLocationY() + getBaseHeight()) {
			closestY = (int) getLocationY() + getBaseHeight();
		}
		Integer closestX = null;
		if (x < getLocationX()) {
			closestX = (int) getLocationX();
		} else if (x > getLocationX() + getBaseWidth()) {
			closestX = (int) getLocationX() + getBaseWidth();
		}
		if (closestX != null && closestY != null) {
			return Map.findDistance(x, y, closestX, closestY);
		} else if (closestX != null) {
			return Math.abs(x - closestX);
		} else if (closestY != null) {
			return Math.abs(y - closestY);
		}
		return 0;
	}
	
	public static double getTopSurfaceDistance(Rectangle2D rect, Map.Ray2D ray) {
		double x = ray.getLocationX();
		double y = ray.getLocationY();
		double angle = ray.getDirection();
		if (isInWallBase(rect, x, y)) {
			if (angle == 0) {
				return rect.getX() + rect.getWidth() - x;
			} else if (angle == Math.PI / 2) {
				return y - rect.getY();
			} else if (angle == Math.PI) {
				return x - rect.getX();
			} else if (angle == Math.PI * 3 / 2) {
				return rect.getY() + rect.getHeight() - y;
			}
			double distanceToHorizontalEdge = 0;
			if (angle < Math.PI) {
				distanceToHorizontalEdge = (y - rect.getY()) / Math.sin(angle);
			} else {
				distanceToHorizontalEdge = (y - (rect.getY() + rect.getHeight())) / Math.sin(angle);
			}
			double distanceToVerticalEdge = 0;
			if (angle > Math.PI / 2 && angle < Math.PI * 3 / 2) {
				distanceToVerticalEdge = (rect.getX() - x) / Math.cos(angle);
			} else {
				distanceToVerticalEdge = ((rect.getX() + rect.getWidth()) - x) / Math.cos(angle);
			}
			return Math.min(distanceToHorizontalEdge, distanceToVerticalEdge);
		} else {
			Integer closeHorizontalWall = null;
			Integer farHorizontalWall = null;
			if (y < rect.getY()) {
				closeHorizontalWall = (int) rect.getY();
				farHorizontalWall = (int) (rect.getY() + rect.getHeight());
			} else if (y >= rect.getY() + rect.getHeight()) {
				closeHorizontalWall = (int) (rect.getY() + rect.getHeight());
				farHorizontalWall = (int) rect.getY();
			}
			if (farHorizontalWall == null && angle != 0 && angle != Math.PI) {
				if (angle < Math.PI) {
					farHorizontalWall = (int) rect.getY();
				} else {
					farHorizontalWall = (int) (rect.getY() + rect.getHeight());
				}
			}
			Integer closeVerticalWall = null;
			Integer farVerticalWall = null;
			if (x < rect.getX()) {
				closeVerticalWall = (int) rect.getX();
				farVerticalWall = (int) (rect.getX() + rect.getWidth());
			} else if (x >= rect.getX() + rect.getWidth()) {
				closeVerticalWall = (int) (rect.getX() + rect.getWidth());
				farVerticalWall = (int) rect.getX();
			}
			if (farVerticalWall == null && angle != Math.PI / 2 && angle != Math.PI * 3 / 2) {
				if (angle > Math.PI / 2 && angle < Math.PI * 3 / 2) {
					farVerticalWall = (int) rect.getX();
				} else {
					farVerticalWall = (int) (rect.getX() + rect.getWidth());
				}
			}
			
			Double[] distances = new Double[4];
			if (closeVerticalWall != null && farHorizontalWall != null) {
				distances[0] = ((y - farHorizontalWall) / Math.tan(angle) + x - closeVerticalWall) / Math.cos(angle);
			}
			if (closeHorizontalWall != null && farVerticalWall != null) {
				distances[1] = (closeHorizontalWall - (y - (farVerticalWall - x) * Math.tan(angle))) / Math.sin(angle);
			}
			if (closeHorizontalWall != null && farHorizontalWall != null) {
				distances[2] = (closeHorizontalWall - farHorizontalWall) / Math.sin(angle);
			}
			if (closeVerticalWall != null && farVerticalWall != null) {
				distances[3] = (farVerticalWall - closeVerticalWall) / Math.cos(angle);
			}
			
			double shortest = Double.MAX_VALUE;
			for (Double d : distances) {
				if (d != null && d >= 0) {
					shortest = Math.min(d, shortest);
				}
			}
			return shortest;
		}
	}
	
	@Override
	public double getTopSurfaceDistance(Map.Ray2D ray) {
		return getTopSurfaceDistance(getBaseShape(), ray);
	}

	@Override
	public boolean canDetectOnRadar(Player player) {
		return SHOW_ON_RADAR;
	}

	@Override
	public Color getRadarColor(Player player) {
		return SHOW_ON_RADAR ? getColor() : null;
	}

    @Override
	public String toString() {
		return "Wall [LOCATION_X=" + LOCATION_X + ", LOCATION_Y=" + LOCATION_Y + ", WIDTH=" + WIDTH + ", HEIGHT=" + HEIGHT + ", SURFACE_HEIGHTS="
				+ Arrays.toString(SURFACE_HEIGHTS) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + LOCATION_X;
		result = prime * result + LOCATION_Y;
		result = prime * result + HEIGHT;
		result = prime * result + WIDTH;
		result = prime * result + Arrays.hashCode(SURFACE_HEIGHTS);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Wall other = (Wall) obj;
		if (LOCATION_X != other.LOCATION_X)
			return false;
		if (LOCATION_Y != other.LOCATION_Y)
			return false;
		if (WIDTH != other.WIDTH)
			return false;
		if (HEIGHT != other.HEIGHT)
			return false;
		if (!Arrays.equals(SURFACE_HEIGHTS, other.SURFACE_HEIGHTS))
			return false;
		return true;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.baseArea = new Area(BASE_SHAPE);
    }
}