import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class Fence implements MapItem, ProjectionPlane.ProjectionPlaneItem, Serializable {
	private static final long serialVersionUID = -9153205285039277167L;
	
	private double fenceLength;
	private final Color COLOR1;
	private final Color COLOR2;
	private final Bitmap BITMAP;
	private final ArrayList<Bullet.BulletMarking> BULLET_MARKINGS1 = new ArrayList<Bullet.BulletMarking>();
	private final ArrayList<Bullet.BulletMarking> BULLET_MARKINGS2 = new ArrayList<Bullet.BulletMarking>();
	
	public Fence(double fenceLength, Color color1, Color color2, Bitmap bitmap) {
		if (fenceLength <= 0) throw new IllegalArgumentException("fence length must be greater than 0");
		if (color1 == null || color2 == null) throw new IllegalArgumentException("the color of the fence cannot be null");
		this.fenceLength = fenceLength;
		COLOR1 = color1;
		COLOR2 = color2;
		BITMAP = bitmap;
	}
	
	@Override
	public String getDisplayName(Player player) {
		return "Fence";
	}
	
	protected double getFenceLength() {
		return this.fenceLength;
	}
	
	protected synchronized void setFenceLength(double fenceLength) {
		if (fenceLength <= 0) throw new IllegalArgumentException("fence length must be greater than 0");
		this.fenceLength = fenceLength;
	}
	
	protected Color getColor(FenceSide side) {
		return side == FenceSide.SIDE2 ? COLOR2 : COLOR1;
	}
	
	@Override
	public int getOpacityAlpha(Map.Ray2D ray, double verticalDirection) {
		if (getColor(FenceSide.SIDE1).getAlpha() == 255 && getColor(FenceSide.SIDE2).getAlpha() == 255) {
			return 255;
		} else {
			return getProjectedColor(ray).getAlpha();
		}
	}

	public boolean isTransparent(Map.Ray2D ray) {
		if (getColor(FenceSide.SIDE1).getAlpha() == 255 && getColor(FenceSide.SIDE2).getAlpha() == 255) {
			return false;
		} else if (getColor(FenceSide.SIDE1).getAlpha() < 255 && getColor(FenceSide.SIDE2).getAlpha() < 255) {
			return true;
		} else {
			return getProjectedColor(ray).getAlpha() < 255;
		}
	}
	
	public abstract double getClosestDistanceToPoint(double x, double y);
	
	public abstract double getFenceDirectionAt(double x, double y);

	/**
	 * Finds the point at which the base of the fence intersects a specified ray
	 * @param ray the ray to be tested
	 * @return the point of intersection or {@code null} if no intersection occurs
	 */
	@Override
	public abstract Point2D getIntersectionPoint(Map.Ray2D ray);
	
	/**
	 * Finds the maximum height in meters of the fence in certain regions of the fence. These regions are specified by their distance from the point 1 of the base. The distances do not need to be sorted.<br>
	 * The distances in increasing order determine the regions of the fence to be included. Each distance toggles the following segment after the specified distance until the next distance or the end
	 * of the fence between the states of being part of the regions or not. For example, the following distances specify the regions of a length-15 fence below: 1, 3, 8, 11<br>
	 * {@code -RRR----RRRR---}<br>
	 * If 0 distances are specified, the absolute maximum height of the fence is returned. If an odd number of distances are specified, the last region terminates at the end of the fence.<br>
	 * Note : The list of distances may be modified.
	 * @param distances the distances specifying the regions in meters from the point 1 of the base
	 * @return the maximum height in meters of the specified regions
	 */
	public abstract double getMaxHeightFrom(List<Double> distances);
	
	public abstract double getHeightAt(double x, double y);
	
	/**
	 * Gets the top height location of the fence in meters at a specified point. The point must be located above the base
	 * @param point the point to be tested
	 * @return the height of the fence at the point
	 */
	public double getHeightAt(Point2D point) {
		return getHeightAt(point.getX(), point.getY());
	}

	public abstract double getBottomHeightAt(double x, double y);

	/**
	 * Gets the bottom height location of the fence in meters at a specified point. The point must be located above the base
	 * @param point the point to be tested
	 * @return the height of the fence at the point
	 */
	public double getBottomHeightAt(Point2D point) {
		return getBottomHeightAt(point.getX(), point.getY());
	}
	
	@Override
	public int getNumberOfGaps() {
		return 1;
	}

	@Override
	public double getTopHeight(Point2D point, int index) {
		return getHeightAt(point);
	}

	@Override
	public double getBottomHeight(Point2D point, int index) {
		return getBottomHeightAt(point);
	}

	@Override
	public double getDrawBottomHeight(Point2D point, int index) {
		return getBottomHeight(point, index);
	}
	
	public abstract double getHighestHeightInArea(Area area);
	
	public abstract double getLowestHeightInArea(Area area);

	@Override
	public Color getProjectedColor(Map.Ray2D ray) {
		return getColor(getFenceSide(ray));
	}

	@Override
	public Color getTopColor(Map.Ray2D ray) {
		return null;
	}
	
	@Override
	public Color getBottomColor(Map.Ray2D ray) {
		return getTopColor(ray);
	}
	
	protected abstract double getFenceDistanceToBase1(Point2D point);
	@Override
	public BufferedImage getBitmap(Point2D pointOfIntersection) {
		if (BITMAP == null) {
			return null;
		} else {
			return BITMAP.getImageSlice(getFenceDistanceToBase1(pointOfIntersection) / getFenceLength());
		}
	}
	
	protected void addBulletMarkings(Map.Ray2D ray, Bullet.BulletMarking... markings) {
		addBulletMarkings(getFenceSide(ray), markings);
	}
	
	protected void addBulletMarkings(FenceSide side, Bullet.BulletMarking... markings) {
		if (markings != null) {
			List<Bullet.BulletMarking> MARKINGS = getBulletMarkings(side);
			synchronized (MARKINGS) {
				for (Bullet.BulletMarking b : markings) {
					if (b != null) {
						MARKINGS.add(b);
					}
				}
			}
		}
	}
	
	protected List<Bullet.BulletMarking> getBulletMarkings(FenceSide side) {
		return side == FenceSide.SIDE2 ? BULLET_MARKINGS2 : BULLET_MARKINGS1;
	}
	
	protected List<Bullet.BulletMarking> getAllBulletMarkings() {
		ArrayList<Bullet.BulletMarking> allMarkings = new ArrayList<Bullet.BulletMarking>();
		synchronized (BULLET_MARKINGS1) {
			allMarkings.addAll(BULLET_MARKINGS1);
		}
		synchronized (BULLET_MARKINGS2) {
			allMarkings.addAll(BULLET_MARKINGS2);
		}
		return allMarkings;
	}
	
	public void clearAllBulletMarkings() {
		synchronized (BULLET_MARKINGS1) {
			BULLET_MARKINGS1.clear();
		}
		synchronized (BULLET_MARKINGS2) {
			BULLET_MARKINGS2.clear();
		}
	}

	@Override
	public Bullet.BulletMarking[] getBulletMarkings(Map.Ray2D ray, double distance) {
		if (isTransparent(ray)) {
			return Bullet.BulletMarking.getBulletMarkings(getAllBulletMarkings(), ray, distance);
		} else {
			return Bullet.BulletMarking.getBulletMarkings(getBulletMarkings(getFenceSide(ray)), ray, distance);
		}
	}

	@Override
	public void hitByMelee(Melee melee, boolean isConsecutive, Map.Ray2D ray, Player slasher, Melee.AttackType attackType) {
	}

	@Override
	public void hitByExplosion(ThrownWeapon w, double explodeX, double explodeY) {
	}

	@Override
	public boolean canDetectOnRadar(Player player) {
		return false;
	}

	@Override
	public Color getRadarColor(Player player) {
		return null;
	}
	
	public abstract FenceSide getFenceSide(Map.Ray2D ray);
	
	public enum FenceSide {
		SIDE1, SIDE2;
	}
	
	public static abstract class StraightFence extends Fence {

		private static final long serialVersionUID = -9026125548959334264L;
		
		private final Line2D BASE;
		private double fenceDirection;
		private final double BOTTOM_HEIGHT;
		
		public StraightFence(Line2D lineSegment, double bottomHeight, Color color1, Color color2, Bitmap bitmap) {
			this(lineSegment.getX1(), lineSegment.getY1(), lineSegment.getX2(), lineSegment.getY2(), bottomHeight, color1, color2, bitmap);
		}
		
		public StraightFence(Point2D p1, Point2D p2, double bottomHeight, Color color1, Color color2, Bitmap bitmap) {
			this(p1.getX(), p1.getY(), p2.getX(), p2.getY(), bottomHeight, color1, color2, bitmap);
		}
		
		public StraightFence(double x1, double y1, double x2, double y2, double bottomHeight, Color color1, Color color2, Bitmap bitmap) {
			super(Map.findDistance(x1, y1, x2, y2), color1, color2, bitmap);
			if (x1 == x2 && y1 == y2) throw new IllegalArgumentException("the base of the fence cannot be a single point");
			BASE = new Line2D.Double(x1, y1, x2, y2);
			this.fenceDirection = Compass.getPrincipleAngle(BASE.getX1(), BASE.getY2(), BASE.getX2(), BASE.getY1());
			BOTTOM_HEIGHT = Math.max(bottomHeight, 0);
		}
		
		protected double getFenceDirection() {
			return this.fenceDirection;
		}
		
		public double getPoint1X() {
			return BASE.getX1();
		}
		
		public double getPoint1Y() {
			return BASE.getY1();
		}
		
		public double getPoint2X() {
			return BASE.getX2();
		}
		
		public double getPoint2Y() {
			return BASE.getY2();
		}
		
		protected void setBase(double x1, double y1, double x2, double y2) {
			synchronized (BASE) {
				BASE.setLine(x1, y1, x2, y2);
			}
			synchronized (this) {
				this.fenceDirection = Compass.getPrincipleAngle(BASE.getX1(), BASE.getY2(), BASE.getX2(), BASE.getY1());
			}
			setFenceLength(Map.findDistance(BASE.getX1(), BASE.getY1(), BASE.getX2(), BASE.getY2()));
		}

		public double getBottomHeight() {
			return BOTTOM_HEIGHT;
		}

		public double getBottomHeightAt(double x, double y) {
			return getBottomHeight();
		}
		
		@Override
		public double getLowestHeightInArea(Area area) {
			return getBottomHeight();
		}
		
		@Override
		public FenceSide getFenceSide(Map.Ray2D ray) {
			double angleDifference = (ray.getDirection() - getFenceDirection() + Math.PI * 2) % (Math.PI * 2);
			if (angleDifference < Math.PI) {
				return FenceSide.SIDE2;
			} else {
				return FenceSide.SIDE1;
			}
		}
		
		@Override
		public double getClosestDistanceToPoint(double x, double y) {
			return BASE.ptSegDist(x, y);
		}
		
		@Override
		public double getFenceDirectionAt(double x, double y) {
			return getFenceDirection();
		}
		
		@Override
		protected double getFenceDistanceToBase1(Point2D point) {
			return Map.findDistance(BASE.getX1(), BASE.getY1(), point.getX(), point.getY());
		}

		@Override
		public Point2D getIntersectionPoint(Map.Ray2D ray) {
			return ray.getIntersectionPoint(BASE);
		}
		
		@Override
		public boolean intersects(Line2D line) {
			return line.intersectsLine(BASE);
		}

		@Override
		public double getHeightAt(double x, double y) {
			return getHeightAt(Point2D.distance(BASE.getX1(), BASE.getY1(), x, y));
		}

		/**
		 * Gets the height of the fence in meters at a specified distance in meters from the end 1 of the fence.
		 * @param distance the distance from end 1 of the fence
		 * @return the height of the fence at the specified distance
		 */
		public abstract double getHeightAt(double distance);
		
		/**
		 * Returns the distance in meters from the point 1 of the base to the intersection point between a linear line segment and the base as a line extended to infinity (i.e. not a line segment).
		 * Note: the intersection found can occur beyond the base. A test should be done such that the value returned is between 0 and the length of the fence to ensure that the intersection is within the base.
		 * @param lx1 the x-coordinate of the first end of the line segment
		 * @param ly1 the y-coordinate of the first end of the line segment
		 * @param lx2 the x-coordinate of the second end of the line segment
		 * @param ly2 the y-coordinate of the second end of the line segment
		 * @return the distance in meters from the point 1 of the base of the intersection point or {@code null} if the two line segments do not intersect
		 */
		Double getIntersectionDistanceFromBase1(double lx1, double ly1, double lx2, double ly2) {
			double denominator = (lx2 - lx1) * (BASE.getY2() - BASE.getY1()) - (ly2 - ly1) * (BASE.getX2() - BASE.getX1());
			if (denominator != 0) {
				double t = ((ly2 - ly1) * (BASE.getX1() - lx1) - (lx2 - lx1) * (BASE.getY1() - ly1)) / denominator;
				return t * getFenceLength();
			}
			return null;
		}
		
		/**
		 * Returns the distances in meters from the point 1 of the base to the intersection point between a quadratic curve segment and the base as a line extended to infinity (i.e. not a line segment).
		 * Note: the intersection found can occur beyond the base. A test should be done such that the value returned is between 0 and the length of the fence to ensure that the intersection is within the base.
		 * @param qx0 the x-coordinate of the current point of the quadratic curve
		 * @param qy0 the y-coordinate of the current point of the quadratic curve
		 * @param qx1 the x-coordinate of the first control point of the quadratic curve
		 * @param qy1 the y-coordinate of the first control point of the quadratic curve
		 * @param qx2 the x-coordinate of the final interpolated control point of the quadratic curve
		 * @param qy2 the y-coordinate of the final interpolated control point of the quadratic curve
		 * @return an array of size 0, 1, 2 (based on the number of intersections) containing the distances in meters of from point 1 of the base to the intersection point
		 */
		double[] getIntersectionDistanceFromBase1(double qx0, double qy0, double qx1, double qy1, double qx2, double qy2) {
			double bx1 = BASE.getX1();
			double by1 = BASE.getY1();
			double bx2 = BASE.getX2();
			double by2 = BASE.getY2();
			double a = (bx2 - bx1) * (qy0 - 2 * qy1 + qy2) - (by2 - by1) * (qx0 - 2 * qx1 + qx2);
			if (a == 0) {
				return new double[]{getIntersectionDistanceFromBase1(qx0, qy0, qx2, qy2)};
			} else {
				double b = 2 * ((by2 - by1) * (qx0 - qx1) - (bx2 - bx1) * (qy0 - qy1));
				double c = (bx2 - bx1) * (qy0 - by1) - (by2 - by1) * (qx0 - bx1);
				double discriminate = b * b - 4 * a * c;
				if (discriminate < 0) {
					return new double[0];
				} else if (discriminate == 0) {
					double s = -b / (2 * a);
					if (bx1 != bx2) {
						double t = (Math.pow((1 - s), 2) * qx0 + 2 * (1 - s) * s * qx1 + s * s * qx2 - bx1) / (bx2 - bx1);
						return new double[]{t * getFenceLength()};
					} else {
						double t = (Math.pow((1 - s), 2) * qy0 + 2 * (1 - s) * s * qy1 + s * s * qy2 - by1) / (by2 - by1);
						return new double[]{t * getFenceLength()};
					}
				} else {
					double s1 = (-b + Math.sqrt(discriminate)) / (2 * a);
					double s2 = (-b - Math.sqrt(discriminate)) / (2 * a);
					if (bx1 != bx2) {
						double t1 = (Math.pow((1 - s1), 2) * qx0 + 2 * (1 - s1) * s1 * qx1 + s1 * s1 * qx2 - bx1) / (bx2 - bx1);
						double t2 = (Math.pow((1 - s2), 2) * qx0 + 2 * (1 - s2) * s2 * qx1 + s2 * s2 * qx2 - bx1) / (bx2 - bx1);
						return new double[]{t1 * getFenceLength(), t2 * getFenceLength()};
					} else {
						double t1 = (Math.pow((1 - s1), 2) * qy0 + 2 * (1 - s1) * s1 * qy1 + s1 * s1 * qy2 - by1) / (by2 - by1);
						double t2 = (Math.pow((1 - s2), 2) * qy0 + 2 * (1 - s2) * s2 * qy1 + s2 * s2 * qy2 - by1) / (by2 - by1);
						return new double[]{t1 * getFenceLength(), t2 * getFenceLength()};
					}
				}
			}
		}
		
		/**
		 * Returns the distances in meters from the point 1 of the base to the intersection point between a cubic curve segment and the base as a line extended to infinity (i.e. not a line segment).
		 * Note: the intersection found can occur beyond the base. A test should be done such that the value returned is between 0 and the length of the fence to ensure that the intersection is within the base.
		 * @param x0 the x-coordinate of the current point of the cubic curve
		 * @param y0 the y-coordinate of the current point of the cubic curve
		 * @param x1 the x-coordinate of the first control point of the cubic curve
		 * @param y1 the y-coordinate of the first control point of the cubic curve
		 * @param x2 the x-coordinate of the second control point of the cubic curve
		 * @param y2 the y-coordinate of the second control point of the cubic curve
		 * @param x3 the x-coordinate of the final interpolated control point of the cubic curve
		 * @param y3 the y-coordinate of the final interpolated control point of the cubic curve
		 * @return an array of size 0, 1, 2, 3 (based on the number of intersections) containing the distances in meters of from point 1 of the base to the intersection point
		 */
		double[] getIntersectionDistanceFromBase1(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
			double bx1 = BASE.getX1();
			double by1 = BASE.getY1();
			double bx2 = BASE.getX2();
			double by2 = BASE.getY2();
			
			double kx0 = x3 - 3 * x2 + 3 * x1 - x0;
			double ky0 = y3 - 3 * y2 + 3 * y1 - y0;
			double a = (by2 - by1) * kx0 - (bx2 - bx1) * ky0;
			if (a == 0) {
				return getIntersectionDistanceFromBase1(x0, y0, x1, y1, x3, y3);
			} else {
				double b = 3 * ((by2 - by1) * (x0 - 2 * x1 + x2) - (bx2 - bx1) * (y0 - 2 * y1 + y2));
				double c = 3 * ((by2 - by1) * (x1 - x0) - (bx2 - bx1) * (y1 - y0));
				double d = (by2 - by1) * (x0 - bx1) - (bx2 - bx1) * (y0 - by1);
				
				double[] eqn = {d, c, b, a};
				CubicCurve2D.solveCubic(eqn);
				if (eqn.length == 0) {
					return new double[0];
				} else if (eqn.length == 1) {
					if (bx2 != bx1) {
						double t = (Math.pow(eqn[0], 3) * kx0 + 3 * Math.pow(eqn[0], 2) * (x2 - 2 * x1 + x0) - 3 * eqn[0] * (x0 - x1) + x0 - bx1) / (bx2 - bx1);
						return new double[]{t * getFenceLength()};
					} else {
						double t = (Math.pow(eqn[0], 3) * ky0 + 3 * Math.pow(eqn[0], 2) * (y2 - 2 * y1 + y0) - 3 * eqn[0] * (y0 - y1) + y0 - by1) / (by2 - by1);
						return new double[]{t * getFenceLength()};
					}
				} else if (eqn.length == 2) {
					if (bx2 != bx1) {
						double t1 = (Math.pow(eqn[0], 3) * kx0 + 3 * Math.pow(eqn[0], 2) * (x2 - 2 * x1 + x0) - 3 * eqn[0] * (x0 - x1) + x0 - bx1) / (bx2 - bx1);
						double t2 = (Math.pow(eqn[1], 3) * kx0 + 3 * Math.pow(eqn[1], 2) * (x2 - 2 * x1 + x0) - 3 * eqn[1] * (x0 - x1) + x0 - bx1) / (bx2 - bx1);
						return new double[]{t1 * getFenceLength(), t2 * getFenceLength()};
					} else {
						double t1 = (Math.pow(eqn[0], 3) * ky0 + 3 * Math.pow(eqn[0], 2) * (y2 - 2 * y1 + y0) - 3 * eqn[0] * (y0 - y1) + y0 - by1) / (by2 - by1);
						double t2 = (Math.pow(eqn[1], 3) * ky0 + 3 * Math.pow(eqn[1], 2) * (y2 - 2 * y1 + y0) - 3 * eqn[1] * (y0 - y1) + y0 - by1) / (by2 - by1);
						return new double[]{t1 * getFenceLength(), t2 * getFenceLength()};
					}
				} else {
					if (bx2 != bx1) {
						double t1 = (Math.pow(eqn[0], 3) * kx0 + 3 * Math.pow(eqn[0], 2) * (x2 - 2 * x1 + x0) - 3 * eqn[0] * (x0 - x1) + x0 - bx1) / (bx2 - bx1);
						double t2 = (Math.pow(eqn[1], 3) * kx0 + 3 * Math.pow(eqn[1], 2) * (x2 - 2 * x1 + x0) - 3 * eqn[1] * (x0 - x1) + x0 - bx1) / (bx2 - bx1);
						double t3 = (Math.pow(eqn[2], 3) * kx0 + 3 * Math.pow(eqn[2], 2) * (x2 - 2 * x1 + x0) - 3 * eqn[2] * (x0 - x1) + x0 - bx1) / (bx2 - bx1);
						return new double[]{t1 * getFenceLength(), t2 * getFenceLength(), t3 * getFenceLength()};
					} else {
						double t1 = (Math.pow(eqn[0], 3) * ky0 + 3 * Math.pow(eqn[0], 2) * (y2 - 2 * y1 + y0) - 3 * eqn[0] * (y0 - y1) + y0 - by1) / (by2 - by1);
						double t2 = (Math.pow(eqn[1], 3) * ky0 + 3 * Math.pow(eqn[1], 2) * (y2 - 2 * y1 + y0) - 3 * eqn[1] * (y0 - y1) + y0 - by1) / (by2 - by1);
						double t3 = (Math.pow(eqn[2], 3) * ky0 + 3 * Math.pow(eqn[2], 2) * (y2 - 2 * y1 + y0) - 3 * eqn[2] * (y0 - y1) + y0 - by1) / (by2 - by1);
						return new double[]{t1 * getFenceLength(), t2 * getFenceLength(), t3 * getFenceLength()};
					}
				}
			}
		}
		
		@Override
		public double getHighestHeightInArea(Area area) {
			ArrayList<Double> intersectionDistancesFromBase1 = new ArrayList<Double>();
			double[] last = new double[2];
			for (PathIterator path = area.getPathIterator(null); !path.isDone(); path.next()) {
				double[] coords = new double[6];
				int type = path.currentSegment(coords);
				if (type == PathIterator.SEG_MOVETO) {
				} else if (type == PathIterator.SEG_LINETO) {
					Double intersectionDistancesFromBase = getIntersectionDistanceFromBase1(last[0], last[1], coords[0], coords[1]);
					if (intersectionDistancesFromBase != null) {
						if (intersectionDistancesFromBase >= 0 && intersectionDistancesFromBase <= getFenceLength()) {
							intersectionDistancesFromBase1.add(intersectionDistancesFromBase);
						}
					}
				} else if (type == PathIterator.SEG_QUADTO) {
					double[] intersectionDistancesFromBase = getIntersectionDistanceFromBase1(last[0], last[1], coords[0], coords[1], coords[2], coords[3]);
					for (double i : intersectionDistancesFromBase) {
						if (i >= 0 && i <= getFenceLength()) {
							intersectionDistancesFromBase1.add(i);
						}
					}
				} else if (type == PathIterator.SEG_CUBICTO) {
					double[] intersectionDistancesFromBase = getIntersectionDistanceFromBase1(last[0], last[1], coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
					for (double i : intersectionDistancesFromBase) {
						if (i >= 0 && i <= getFenceLength()) {
							intersectionDistancesFromBase1.add(i);
						}
					}
				} else if (type == PathIterator.SEG_CLOSE) {
					break;
				}
				last[0] = coords[0];
				last[1] = coords[1];
			}
			if (intersectionDistancesFromBase1.size() == 0) {
				return 0;
			} else if (intersectionDistancesFromBase1.size() % 2 == 1) {
				if (area.contains(BASE.getP1())) {
					intersectionDistancesFromBase1.add(0d);
				} else if (area.contains(BASE.getP2())) {
					intersectionDistancesFromBase1.add(getFenceLength());
				}
			}
			return getMaxHeightFrom(intersectionDistancesFromBase1);
		}

		@Override
		public boolean baseHitByBullet(Bullet b, Game game, double distanceTraveled) {
			Map.Ray2D ray = b.getTravelPath2D();
			double hitLocationX = ray.getLocationXAtDistance(distanceTraveled);
			double hitLocationY = ray.getLocationYAtDistance(distanceTraveled);
			double hitLocationZ = b.getHeightAt(distanceTraveled);
			if (hitLocationZ >= getBottomHeightAt(hitLocationX, hitLocationY) && hitLocationZ <= getHeightAt(hitLocationX, hitLocationY)) {
				addBulletMarkings(b.getTravelPath2D(), new Bullet.BulletMarking(hitLocationX, hitLocationY, hitLocationZ, b.getRadius()));
				return true;
			}
			return false;
		}

		@Override
		public double getLocationX() {
			return BASE.getX1();
		}

		@Override
		public double getLocationY() {
			return BASE.getY1();
		}
		
		@Override
		public String toString() {
			return "StraightFence [X1=" + BASE.getX1() + ", Y1=" + BASE.getY1() + ", X2=" + BASE.getX2() + ", Y2=" + BASE.getY2() + "]";
		}

		public static class RectangularFence extends StraightFence {
			private static final long serialVersionUID = -5646253907137755857L;
			
			private final double HEIGHT;

			public RectangularFence(Line2D lineSegment, double topHeight, double bottomHeight, Color color1, Color color2, Bitmap bitmap) {
				this(lineSegment.getX1(), lineSegment.getY1(), lineSegment.getX2(), lineSegment.getY2(), topHeight, bottomHeight, color1, color2, bitmap);
			}

			public RectangularFence(Point2D p1, Point2D p2, double topHeight, double bottomHeight, Color color1, Color color2, Bitmap bitmap) {
				this(p1.getX(), p1.getY(), p2.getX(), p2.getY(), topHeight, bottomHeight, color1, color2, bitmap);
			}

			public RectangularFence(double x1, double y1, double x2, double y2, double topHeight, double bottomHeight, Color color1, Color color2, Bitmap bitmap) {
				super(x1, y1, x2, y2, bottomHeight, color1, color2, bitmap);
				if (topHeight <= bottomHeight) throw new IllegalStateException("height of fence must be greater than bottom height");
				HEIGHT = Math.max(topHeight, 0);
			}

			public double getHeight() {
				return HEIGHT;
			}

			@Override
			public double getHeightAt(double x, double y) {
				return getHeight();
			}

			@Override
			public double getHeightAt(double distance) {
				return getHeight();
			}

			@Override
			public double getMaxHeightFrom(List<Double> distances) {
				return getHeight();
			}
			
			@Override
			public double getHighestHeightInArea(Area area) {
				return getHeight();
			}
		}

		public static class SlopedFence extends StraightFence {
			private static final long serialVersionUID = -5646253907137755857L;
			
			private final double HEIGHT1;
			private final double HEIGHT2;

			public SlopedFence(Line2D lineSegment, double height1, double height2, double bottomHeight, Color color1, Color color2, Bitmap bitmap) {
				this(lineSegment.getX1(), lineSegment.getY1(), lineSegment.getX2(), lineSegment.getY2(), height1, height2, bottomHeight, color1, color2, bitmap);
			}

			public SlopedFence(Point2D p1, Point2D p2, double height1, double height2, double bottomHeight, Color color1, Color color2, Bitmap bitmap) {
				this(p1.getX(), p1.getY(), p2.getX(), p2.getY(), height1, height2, bottomHeight, color1, color2, bitmap);
			}

			public SlopedFence(double x1, double y1, double x2, double y2, double height1, double height2, double bottomHeight, Color color1, Color color2, Bitmap bitmap) {
				super(x1, y1, x2, y2, bottomHeight, color1, color2, bitmap);
				if (height1 <= bottomHeight || height2 <= bottomHeight) throw new IllegalStateException("height of fence must be greater than bottom height");
				HEIGHT1 = Math.max(height1, 0);
				HEIGHT2 = Math.max(height2, 0);
			}

			@Override
			public double getHeightAt(double distance) {
				return HEIGHT1 - distance * (HEIGHT1 - HEIGHT2) / getFenceLength();
			}

			@Override
			public double getMaxHeightFrom(List<Double> distances) {
				Collections.sort(distances);
				if (distances.size() == 0) {
					return Math.max(HEIGHT1, HEIGHT2);
				} else {
					Double distanceMin = null;
					Double distanceMax = null;
					for (Double d : distances) {
						if (d != null && d >= 0) {
							distanceMin = d;
							break;
						}
					}
					for (Double d : distances) {
						if (d != null && d <= getFenceLength()) {
							distanceMax = d;
						} else if (d != null && d > getFenceLength()) {
							break;
						}
					}
					if (distanceMin != null && distanceMax != null) {
						return Math.max(getHeightAt(distanceMin), getHeightAt(distanceMax));
					} else if (distanceMin != null) {
						return getHeightAt(distanceMin);
					} else if (distanceMax != null) {
						return getHeightAt(distanceMax);
					} else {
						return Math.max(HEIGHT1, HEIGHT2);
					}
				}
			}
		}
	}
}
