import java.awt.Color;


public class Ladder implements Map.MapLocation2D {
	
	private final double CENTER_X;
	private final double CENTER_Y;
	private final double INNER_RADIUS;
	private final double RADIUS;
	private final double BOTTOM_HEIGHT;
	private final double TOP_HEIGHT;
	private final int RUNGS;
	private final double RUNG_HALF_HEIGHT;
	private final Color COLOR;
	
	public Ladder(double centerX, double centerY, double innerRadius, double radius, double bottomHeight, double topHeight, int rungs, double rungHeight, Color color) {
		CENTER_X = centerX;
		CENTER_Y = centerY;
		INNER_RADIUS = innerRadius;
		RADIUS = radius;
		BOTTOM_HEIGHT = bottomHeight;
		TOP_HEIGHT = topHeight;
		RUNGS = rungs;
		RUNG_HALF_HEIGHT = rungHeight / 2;
		COLOR = color;
	}

	@Override
	public double getLocationX() {
		return CENTER_X;
	}

	@Override
	public double getLocationY() {
		return CENTER_Y;
	}
	
	public double getInnerRadius() {
		return INNER_RADIUS;
	}
	
	public double getRadius() {
		return RADIUS;
	}
	
	public double getBottomHeight() {
		return BOTTOM_HEIGHT;
	}
	
	public double getTopHeight() {
		return TOP_HEIGHT;
	}
	
	public int getRungs() {
		return RUNGS;
	}
	
	public double getRungHalfHeight() {
		return RUNG_HALF_HEIGHT;
	}
	
	public Color getColor() {
		return COLOR;
	}

	@Override
	public String toString() {
		return "Ladder [CENTER_X=" + CENTER_X + ", CENTER_Y=" + CENTER_Y + ", RADIUS=" + RADIUS + ", BOTTOM_HEIGHT=" + BOTTOM_HEIGHT + ", TOP_HEIGHT="
				+ TOP_HEIGHT + ", COLOR=" + COLOR + "]";
	}
}
