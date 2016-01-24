
public class Compass {
	
	public interface CompassPoints {
		/**
		 * Gets the 1-, 2-, or 3- letter abbreviation for the compass point.
		 * @return the abbreviation for the compass point
		 */
		public String getAbbr();
		/**
		 * Gets the bearing in radians relative to North going clockwise
		 * @return the compass bearing of the compass point
		 */
		public double getBearing();
	}

	public enum Cardinal implements CompassPoints {
		NORTH(AllPoints.NORTH), EAST(AllPoints.EAST), SOUTH(AllPoints.SOUTH), WEST(AllPoints.WEST);
		private final String ABBR;
		private final double BEARING;
		Cardinal(AllPoints point) {
			ABBR = point.getAbbr();
			BEARING = point.getBearing();
		}
		@Override
		public String getAbbr() {
			return ABBR;
		}
		@Override
		public double getBearing() {
			return BEARING;
		}
	}

	public enum PrincipalWinds implements CompassPoints {
		NORTH(AllPoints.NORTH), EAST(AllPoints.EAST), SOUTH(AllPoints.SOUTH), WEST(AllPoints.WEST),
		NORTHEAST(AllPoints.NORTHEAST), SOUTHEAST(AllPoints.SOUTHEAST), SOUTHWEST(AllPoints.SOUTHWEST), NORTHWEST(AllPoints.NORTHWEST);
		private final String ABBR;
		private final double BEARING;
		PrincipalWinds(AllPoints point) {
			ABBR = point.getAbbr();
			BEARING = point.getBearing();
		}
		@Override
		public String getAbbr() {
			return ABBR;
		}
		@Override
		public double getBearing() {
			return BEARING;
		}
	}

	public enum AllPoints implements CompassPoints {
		NORTH("N", 0), EAST("E", Math.PI / 2), SOUTH("S", Math.PI), WEST("W", Math.PI * 3 / 2),
		NORTHEAST("NE", Math.PI / 4), SOUTHEAST("SE", Math.PI * 3 / 4), SOUTHWEST("SW", Math.PI * 5 / 4), NORTHWEST("NW", Math.PI * 7 / 4),
		NORTHNORTHEAST("NNE", Math.PI / 8), EASTNORTHEAST("ENE", Math.PI * 3 / 8), EASTSOUTHEAST("ESE", Math.PI * 5 / 8), SOUTHSOUTHEAST("SSE", Math.PI * 7 / 8),
		SOUTHSOUTHWEST("SSW", Math.PI * 9 / 8), WESTSOUTHWEST("WSW", Math.PI * 11 / 8), WESTNORTHWEST("WNW", Math.PI * 13 / 8), NORTHNORTHWEST("NNW", Math.PI * 15 / 8),;
		private final String ABBR;
		private final double BEARING;
		AllPoints(String abbr, double angle) {
			ABBR = abbr;
			BEARING = angle;
		}
		@Override
		public String getAbbr() {
			return ABBR;
		}
		@Override
		public double getBearing() {
			return BEARING;
		}
	}
	
	public static int directionToX(CompassPoints point) {
		for (int i = 0; i < point.getAbbr().length(); i++) {
			if (Character.toUpperCase(point.getAbbr().charAt(i)) == 'E') {
				return 1;
			} else if (Character.toUpperCase(point.getAbbr().charAt(i)) == 'W') {
				return -1;
			}
		}
		return 0;
	}
	
	public static int directionToY(CompassPoints point) {
		for (int i = 0; i < point.getAbbr().length(); i++) {
			if (Character.toUpperCase(point.getAbbr().charAt(i)) == 'N') {
				return 1;
			} else if (Character.toUpperCase(point.getAbbr().charAt(i)) == 'S') {
				return -1;
			}
		}
		return 0;
	}
	
	public static CompassPoints degressToCompassPoint(double angle, int nOfPoints) {
		final double pointAzimuth = Math.PI * 2 / nOfPoints;
		angle = (angle % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		for (AllPoints point : AllPoints.values()) {
			if (angle >= point.getBearing() - pointAzimuth / 2 && angle < point.getBearing() + pointAzimuth / 2) {
				return point;
			}
		}
		return AllPoints.values()[0];
	}
	
	public static double getPrincipleAngle(double originX, double originY, double x, double y) {
		return getPrincipleAngle(x - originX, y - originY);
	}
	
	public static double getPrincipleAngle(double x, double y) {
		double angle = 0;
		if (x != 0 || y != 0) {
			if (x == 0) {
				angle = Math.abs(y) * Math.PI / 2 / y;
			} else if (x > 0) {
				angle = Math.atan(y / x);
			} else {
				angle = Math.atan(y / x) + Math.PI;
			}
		}
		return (angle % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
	}
	
	/**
	 * Checks if an angle lies between two boundary angles (inclusive) on a circle with the angle increasing counter-clockwise
	 * @param angle the angle to check for
	 * @param boundary1 the angle of the bound in the clockwise direction
	 * @param boundary2 the angle of the bound in the counter-clockwise direction
	 * @return true if the angle lies between the two boundaries; otherwise, false
	 */
	public static boolean isAngleInclusiveBetween(double angle, double boundary1, double boundary2) {
		angle = (angle % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		boundary1 = (boundary1 % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		boundary2 = (boundary2 % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
		return boundary2 >= (angle > boundary2 ? angle - Math.PI * 2 : angle) && (angle > boundary2 ? angle - Math.PI * 2 : angle) >= (boundary1 > boundary2 ? boundary1 - Math.PI * 2 : boundary1);
	}
	
	public enum CircularDirection {
		CLOCKWISE, COUNTER_CLOCKWISE;
	}
}
