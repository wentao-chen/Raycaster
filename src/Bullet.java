import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Bullet implements Serializable {
	private static final long serialVersionUID = -520241165325943180L;
	
	private double locationX;
	private double locationY;
	private final Map.Ray2D TRAVEL_PATH2D;
	private final double INITIAL_LOCATION_Z;
	private final double VERTICAL_DIRECTION;
	private final double SPEED;
	private final int SHOOTER_ID;
	private final CauseOfDeath CAUSE_OF_DEATH;
	private final BulletDamage DAMAGE;
	private final double RADIUS;
	
	public Bullet(double locationX, double locationY, double locationZ, double horizontalDirection, double verticalDirection, double speed, Player shooter, Gun gun, BulletDamage damage, double radius) {
		TRAVEL_PATH2D = new Map.Ray2D(locationX, locationY, horizontalDirection);
		INITIAL_LOCATION_Z = locationZ;
		VERTICAL_DIRECTION = verticalDirection;
		SPEED = speed;
		SHOOTER_ID = shooter.getID();
		CAUSE_OF_DEATH = new CauseOfDeath.StandardCauseOfDeath(gun.getName());
		DAMAGE = damage;
		RADIUS = Math.abs(radius);
		this.locationX = locationX;
		this.locationY = locationY;
	}

	public double getLocationX() {
		return locationX;
	}

	public double getLocationY() {
		return locationY;
	}
	
	public Map.Ray2D getTravelPath2D() {
		return TRAVEL_PATH2D;
	}
	
	public double getInitialLocationZ() {
		return INITIAL_LOCATION_Z;
	}
	
	/**
	 * Gets the height of the bullet after a certain distance.
	 * @param distanceTraveled the distance traveled in meters
	 * @return the bottom height of the bullet in meters of the bullet
	 */
	public double getHeightAt(double distanceTraveled) {
		return getInitialLocationZ() + ((getVerticalDirection() != Math.PI / 2 && getVerticalDirection() != Math.PI * 3 / 2) ? Math.tan(getVerticalDirection()) * distanceTraveled : 0);
	}
	
	public double getDistanceTraveledAtHeight(double height) {
		return (height - getInitialLocationZ()) / Math.tan(getVerticalDirection());
	}
	
	public void moveBullet(long timePassed) {
		this.locationX += Math.cos(getHorizontalDirection()) * Math.cos(getVerticalDirection()) * getSpeed() * timePassed / 1000d;
		this.locationY -= Math.sin(getHorizontalDirection()) * Math.cos(getVerticalDirection()) * getSpeed() * timePassed / 1000d;
	}

	public double getHorizontalDirection() {
		return TRAVEL_PATH2D.getDirection();
	}

	public double getVerticalDirection() {
		return VERTICAL_DIRECTION;
	}
	
	public double getSpeed() {
		return SPEED;
	}
	
	public int getShooterID() {
		return SHOOTER_ID;
	}
	
	public CauseOfDeath getCauseOfDeath() {
		return CAUSE_OF_DEATH;
	}
	
	public BulletDamage getDamage() {
		return DAMAGE;
	}
	
	public double getRadius() {
		return RADIUS;
	}
	
	public static final class BulletMarking implements Map.MapLocation3D, Serializable {
		private static final long serialVersionUID = -203792155591238185L;
		
		public static final Color BULLET_MARKINGS_COLOR = Color.BLACK;

		private final double LOCATION_X;
		private final double LOCATION_Y;
		private final double LOCATION_Z;
		private final double MARKING_RADIUS;
		
		public BulletMarking(double locationX, double locationY, double locationZ, double markingRadius) {
			LOCATION_X = locationX;
			LOCATION_Y = locationY;
			LOCATION_Z = locationZ;
			MARKING_RADIUS = Math.abs(markingRadius);
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
		
		public boolean nearBulletMarking(double x, double y) {
			return Math.abs(getLocationX() - x) <= getMarkingRadius() && Math.abs(getLocationY() - y) <= getMarkingRadius();
		}
		
		public double getMarkingRadius() {
			return MARKING_RADIUS;
		}
		
		public static BulletMarking[] getBulletMarkings(List<BulletMarking> MARKINGS, Map.Ray2D ray, double distance) {
			ArrayList<Bullet.BulletMarking> markings = new ArrayList<Bullet.BulletMarking>();
			synchronized (MARKINGS) {
				for (Bullet.BulletMarking m : MARKINGS) {
					if (m.nearBulletMarking(ray.getLocationXAtDistance(distance), ray.getLocationYAtDistance(distance))) {
						markings.add(m);
					}
				}
			}
			return markings.toArray(new Bullet.BulletMarking[markings.size()]);
		}
	}
}
