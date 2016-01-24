
public enum HitBox {
	HEAD(0.8125, 1, 4), CHEST(0.625, 0.8125, 1), ABDOMEN(0.5, 0.625, 1.25), LEG(0, 0.5, 0.75);
	
	private final double BOTTOM_HEIGHT;
	private final double TOP_HEIGHT;
	private final double MULTIPLIER;
	
	HitBox(double bottomHeight, double topHeight, double multiplier) {
		BOTTOM_HEIGHT = bottomHeight;
		TOP_HEIGHT = topHeight;
		MULTIPLIER = multiplier;
	}
	
	public double getBottomHeight() {
		return BOTTOM_HEIGHT;
	}
	
	public double getTopHeight() {
		return TOP_HEIGHT;
	}
	
	public double getMultiplier() {
		return MULTIPLIER;
	}
}
