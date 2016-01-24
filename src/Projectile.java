import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;


public abstract class Projectile extends CylinderMapItem {

	private static final long serialVersionUID = 5430001745435630620L;
	
	private double horizontalAngle = 0;
	private double horizontalVelocity = 0;
	private double verticalVelocity = 0;
	private Player launcher = null;
	private Game game = null;
	private boolean bounce = true;
	private boolean verticalSettled = true;
	
	public Projectile(double physicalHalfWidth, double physicalHeight, BufferedImage projectedImage) {
		super(physicalHalfWidth, physicalHeight, projectedImage);
	}
	
	protected void drop(Game game, Map.MapLocation3D location) {
		launch(null, game, location.getLocationX(), location.getLocationY(), location.getLocationZ(), 0, 0, 0, false);
	}
	
	protected void drop(Game game, double initialX, double initialY, double initialZ) {
		launch(null, game, initialX, initialY, initialZ, 0, 0, 0, false);
	}
	
	protected void launch(Player launcher, double initialX, double initialY, double initialZ, double initialHorizontalAngle, double initialVerticalAngle, double initialVelocity, boolean bounce) {
		launch(launcher, launcher != null ? launcher.getGame() : null, initialX, initialY, initialZ, initialHorizontalAngle, initialVerticalAngle, initialVelocity, bounce);
	}
	
	protected void launch(Game game, double initialX, double initialY, double initialZ, double initialHorizontalAngle, double initialVerticalAngle, double initialVelocity, boolean bounce) {
		launch(null, game, initialX, initialY, initialZ, initialHorizontalAngle, initialVerticalAngle, initialVelocity, bounce);
	}
	
	private synchronized void launch(Player launcher, Game game, double initialX, double initialY, double initialZ, double initialHorizontalAngle, double initialVerticalAngle, double initialVelocity, boolean bounce) {
		if (launcher != null) {
			this.launcher = launcher;
		}
		this.game = this.launcher != null ? this.launcher.getGame() : game;
		setLocation(initialX, initialY, initialZ);
		this.horizontalAngle = (initialHorizontalAngle % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		this.horizontalVelocity = Math.cos(initialVerticalAngle) * initialVelocity;
		this.verticalVelocity = Math.sin(initialVerticalAngle) * initialVelocity;
		this.bounce = bounce;
		this.verticalSettled = false;
	}
	
	protected double getHorizontalAngle() {
		return this.horizontalAngle;
	}
	
	protected double getHorizontalVelocity() {
		return this.horizontalVelocity;
	}
	
	protected double getVerticalVelocity() {
		return this.verticalVelocity;
	}
	
	protected Player getLauncher() {
		return this.launcher;
	}
	
	protected Game getGame() {
		return this.game;
	}
	
	protected boolean isBounce() {
		return this.bounce;
	}
	
	protected boolean isVerticallySettled() {
		return this.verticalSettled;
	}
	
	/**
	 * Invoked when a collision occurs between the projectile and a map item, a boundary or the floor. If the map item is {@code null}, the projectile has collided with the edge of the map, a floor or a ceiling.
	 * @param game the game in which the collision occurs
	 * @param item the map item involved in the collision with the projection or {@code null} if the collision occurred at the boundary of the map
	 */
	protected void hitMapItem(Game game, MapItem item) {
	}

	protected void move(long mspf) {
		if (isVerticallySettled()) {
			return;
		}
		double time = mspf / 1000d;
		double highestTopHeightUnder = 0;
		double lowestBottomHeightAbove = Double.MAX_VALUE;
		double kineticCoefficientOfFriction = 0.01;
		if (getGame().getMap().inGrid(getLocationX(), getLocationY())) {
			highestTopHeightUnder = getGame().getMap().getWall(getLocationX(), getLocationY()).getHighestTopHeightUnder(getBottomHeight());
			lowestBottomHeightAbove = getGame().getMap().getWall(getLocationX(), getLocationY()).getLowestBottomHeightAbove(getTopHeight());
			kineticCoefficientOfFriction = getGame().getMap().getWall(getLocationX(), getLocationY()).getKineticCoefficientOfFriction();
			Ceiling highestCeilingUnder = getGame().getMap().getHighestCeilingUnder(getBase(), getBottomHeight());
			if (highestCeilingUnder != null && highestCeilingUnder.getLocationZ() > highestTopHeightUnder) {
				highestTopHeightUnder = highestCeilingUnder.getLocationZ();
				kineticCoefficientOfFriction = highestCeilingUnder.getKineticCoefficientOfFriction();
			} else {
				Ceiling lowestCeilingAbove = getGame().getMap().getLowestCeilingAbove(getBase(), getBottomHeight());
				if (lowestCeilingAbove != null && lowestCeilingAbove.getLocationZ() < lowestBottomHeightAbove) {
					lowestBottomHeightAbove = lowestCeilingAbove.getLocationZ();
				}
			}
		}
		
		double horizontalAcceleration = kineticCoefficientOfFriction * getGame().getMap().getGravity();
		double horizontalDisplacement = Math.max(getHorizontalVelocity() * time + horizontalAcceleration * time * time / 2, 0);
		double newLocationX = getLocationX() + horizontalDisplacement * Math.cos(getHorizontalAngle());
		double newLocationY = getLocationY() - horizontalDisplacement * Math.sin(getHorizontalAngle());
		
		double verticalAcceleration = getGame().getMap().getGravity();
		double verticalDisplacement = getVerticalVelocity() * Math.max(time, 0.001) + verticalAcceleration * Math.max(time, 0.001) * Math.max(time, 0.001) / 2;
		double newLocationZ = getBottomHeight() + verticalDisplacement;
		
		if (!isVerticallySettled() && getVerticalVelocity() + verticalAcceleration * Math.max(time, 0.001) < 0 && newLocationZ < highestTopHeightUnder) {
			if (isBounce()) {
				hitMapItem(getGame(), null);
				this.horizontalVelocity *= 0.9;
				this.verticalVelocity *= -0.6;
				newLocationZ = 2 * highestTopHeightUnder - newLocationZ;
				if (newLocationZ < 0.01) {
					this.verticalVelocity = 0;
					newLocationZ = highestTopHeightUnder;
					this.verticalSettled = true;
				}
			} else {
				this.verticalVelocity = 0;
				newLocationZ = highestTopHeightUnder;
				this.verticalSettled = true;
			}
		} else if (!isVerticallySettled() && getVerticalVelocity() + verticalAcceleration * Math.max(time, 0.001) > 0 && newLocationZ + getPhysicalHeight() > lowestBottomHeightAbove) {
			hitMapItem(getGame(), null);
			this.horizontalVelocity *= 0.95;
			this.verticalVelocity *= -0.6;
			newLocationZ = Math.max(2 * (lowestBottomHeightAbove - getPhysicalHeight()) - newLocationZ, 0);
			
		}
		
		if (getPhysicalHalfWidth() < getGame().getMap().getWidth() - getPhysicalHalfWidth()) {
			while (newLocationX < getPhysicalHalfWidth() || newLocationX > getGame().getMap().getWidth() - getPhysicalHalfWidth()) {
				if (newLocationX < getPhysicalHalfWidth() && (getHorizontalAngle() > Math.PI / 2 && getHorizontalAngle() < 3 * Math.PI / 2)) {
					this.horizontalAngle += 2 * (Math.PI - getHorizontalAngle()) + Math.PI;
					newLocationX += 2 * (getPhysicalHalfWidth() - newLocationX);
				} else if (newLocationX > getGame().getMap().getWidth() - getPhysicalHalfWidth() && (getHorizontalAngle() < Math.PI / 2 || getHorizontalAngle() > 3 * Math.PI / 2)) {
					this.horizontalAngle -= getHorizontalAngle() > Math.PI ? Math.PI * 2 : 0;
					this.horizontalAngle -= 2 * getHorizontalAngle() - Math.PI;
					newLocationX += 2 * (getGame().getMap().getWidth() - getPhysicalHalfWidth() - newLocationX);
				}  else {
					break;
				}
				hitMapItem(getGame(), null);
				this.horizontalVelocity *= 0.3;
				this.horizontalAngle = (getHorizontalAngle() % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
			}
		}
		if (getPhysicalHalfWidth() < getGame().getMap().getHeight() - getPhysicalHalfWidth()) {
			while (newLocationY < getPhysicalHalfWidth() || newLocationY > getGame().getMap().getHeight() - getPhysicalHalfWidth()) {
				if (newLocationY < getPhysicalHalfWidth() && getHorizontalAngle() < Math.PI) {
					this.horizontalAngle += 2 * (Math.PI / 2 - getHorizontalAngle()) + Math.PI;
					newLocationY += 2 * (getPhysicalHalfWidth() - newLocationY);
				} else if (newLocationY > getGame().getMap().getHeight() - getPhysicalHalfWidth() && getHorizontalAngle() > Math.PI) {
					this.horizontalAngle += 2 * (Math.PI * 3 / 2 - getHorizontalAngle()) + Math.PI;
					newLocationY += 2 * (getGame().getMap().getHeight() - getPhysicalHalfWidth() - newLocationY);
				}  else {
					break;
				}
				hitMapItem(getGame(), null);
				this.horizontalVelocity *= 0.3;
				this.horizontalAngle = (getHorizontalAngle() % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
			}
		}
		Line2D line = new Line2D.Double(getLocationX(), getLocationY(), newLocationX, newLocationY);
		Player closestPlayer = null;
		double closestDistance = Double.MAX_VALUE;
		for (Player p : getGame().getPlayers()) {
			if ((getLauncher() == null || !getLauncher().equals(p)) && p.intersects(line) && getTopHeight() >= p.getBottomHeight() && getBottomHeight() <= p.getTopHeight()) {
				double distance = Map.findDistance2D(this, p);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestPlayer = p;
				}
			}
		}
		if (closestPlayer != null) {
			hitMapItem(getGame(), closestPlayer);
			this.horizontalVelocity = 0;
		} else {
			Map.Ray2D ray = new Map.Ray2D(getLocationX(), getLocationY(), Compass.getPrincipleAngle(getLocationX(), newLocationY, newLocationX, getLocationY()));
			Fence closestFence = null;
			closestDistance = Double.MAX_VALUE;
			for (Fence f : getGame().getMap().getFences()) {
				if (f.intersects(line)) {
					Point2D intersection = f.getIntersectionPoint(ray);
					if (intersection != null && getGame().getMap().doesCylinderIntersectFence(intersection.getX(), intersection.getY(), getPhysicalHalfWidth(), newLocationZ, getPhysicalHeight()) != null) {
						double distance = Map.findDistance(this, intersection.getX(), intersection.getY());
						if (distance < closestDistance) {
							closestDistance = distance;
							closestFence = f;
						}
					}
				}
			}
			if (closestFence != null) {
				hitMapItem(getGame(), closestFence);
				double fenceDirection = closestFence.getFenceDirectionAt(newLocationX, newLocationY);
				this.horizontalAngle = ((2 * fenceDirection - this.horizontalAngle) % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
				if (mspf > 0) {
					move(mspf / 2);
				}
				if (mspf > 1) {
					move(mspf / 2 + mspf % 2);
				}
			} else {
				Wall closestWall = null;
				closestDistance = Double.MAX_VALUE;
				for (double y = 0; y < getGame().getMap().getHeight(); y++) {
					for (double x = 0; x < getGame().getMap().getWidth(); x++) {
						if (x >= getGame().getMap().getWidth() || y >= getGame().getMap().getHeight()) {
							break;
						}
						Wall wall = getGame().getMap().getWall(x, y);
						if (wall.intersects(line)) {
							Point2D intersection = wall.getIntersectionPoint(ray);
							if (intersection != null && (wall.hasWallAtHeight(getBottomHeight())|| wall.hasWallAtHeight(getTopHeight()))) {
								double distance = Map.findDistance(this, intersection.getX(), intersection.getY());
								if (distance < closestDistance) {
									closestDistance = distance;
									closestWall = wall;
								}
							}
						}
					}
				}
				if (closestWall != null) {
					hitMapItem(getGame(), closestWall);
					Compass.Cardinal hitWallDirection = closestWall.getWallDirection(new Map.Ray2D(getLocationX(), getLocationY(), this.horizontalAngle));
					if (hitWallDirection == Compass.Cardinal.WEST) {
						this.horizontalAngle += 2 * (Math.PI - getHorizontalAngle()) + Math.PI;
						setLocationX(closestWall.getLocationX() - getPhysicalHalfWidth());
					} else if (hitWallDirection == Compass.Cardinal.EAST) {
						this.horizontalAngle -= getHorizontalAngle() > Math.PI ? Math.PI * 2 : 0;
						this.horizontalAngle -= 2 * getHorizontalAngle() - Math.PI;
						setLocationX(closestWall.getLocationX() + 1 + getPhysicalHalfWidth());
					} else if (hitWallDirection == Compass.Cardinal.NORTH) {
						this.horizontalAngle += 2 * (Math.PI * 3 / 2 - getHorizontalAngle()) + Math.PI;
						setLocationY(closestWall.getLocationY() - getPhysicalHalfWidth());
					} else if (hitWallDirection == Compass.Cardinal.SOUTH) {
						this.horizontalAngle += 2 * (Math.PI / 2 - getHorizontalAngle()) + Math.PI;
						setLocationY(closestWall.getLocationY() + 1 + getPhysicalHalfWidth());
					} else {
						this.horizontalAngle += Math.PI;
					}
					this.horizontalAngle = (getHorizontalAngle() % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
					if (mspf > 0) {
						move(mspf / 2);
					}
					if (mspf > 1) {
						move(mspf / 2 + mspf % 2);
					}
				} else {
					Ceiling closestCeiling = null;
					closestDistance = Double.MAX_VALUE;
					for (Ceiling c : getGame().getMap().getCeilings()) {
						if (c.intersects(line)) {
							Point2D intersection = c.getIntersectionPoint(ray);
							if (intersection != null && getGame().getMap().doesCylinderIntersectCeiling(getBase(), newLocationZ, getPhysicalHeight()) != null) {
								double distance = Map.findDistance(this, intersection.getX(), intersection.getY());
								if (distance < closestDistance) {
									closestDistance = distance;
									closestCeiling = c;
								}
							}
						}
					}
					if (closestCeiling != null) {
						hitMapItem(getGame(), closestCeiling);
					}
					setLocation(newLocationX, newLocationY, newLocationZ);
					this.horizontalVelocity += horizontalAcceleration * time;
					this.verticalVelocity += verticalAcceleration * Math.max(time, 0.001);
				}
			}
		}
	}
}
