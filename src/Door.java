import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;


public class Door extends Fence.StraightFence.RectangularFence {
	private static final long serialVersionUID = 349433145057125137L;
	
	private final DoorMovementController POINT_1;
	private final DoorMovementController POINT_2;
	private final long CHANGE_POSITION_TIME;
	private final DoorActivationSelector DOOR_ACTIVATION_SELECTOR;
	private final ArrayList<Door> LINKED_DOORS = new ArrayList<Door>();
	private final AutomaticallyClosingDoorTester AUTOMATICALLY_CLOSING_DOOR_TESTER;
	private boolean isOpen = false;
	private Long previousChangeStateTime = null;

	public Door(DoorMovementController point1, DoorMovementController point2, double height, double bottomHeight, long changePositionTime, DoorActivationSelector doorActivationSelector, AutomaticallyClosingDoorTester automaticallyClosingTesterDoor, boolean isOpen, Color color1, Color color2, Bitmap bitmap) {
		super(point1.getPositionX(isOpen), point1.getPositionY(isOpen), point2.getPositionX(isOpen), point2.getPositionY(isOpen), height, bottomHeight, color1, color2, bitmap);
		if (changePositionTime < 0) throw new IllegalStateException("change position time cannot be less than 0");
		POINT_1 = point1;
		POINT_2 = point2;
		CHANGE_POSITION_TIME = changePositionTime;
		DOOR_ACTIVATION_SELECTOR = doorActivationSelector;
		AUTOMATICALLY_CLOSING_DOOR_TESTER = automaticallyClosingTesterDoor;
		this.isOpen = isOpen;
		this.previousChangeStateTime = null;
	}
	
	@Override
	public String getDisplayName(Player player) {
		return "Door";
	}
	
	public boolean canActivateDoor(Player player) {
		return DOOR_ACTIVATION_SELECTOR == null || DOOR_ACTIVATION_SELECTOR.canActivateDoor(player);
	}
	
	public boolean willDoorAutomaticallyClose(Player... players) {
		for (Player p : players) {
			if (AUTOMATICALLY_CLOSING_DOOR_TESTER.isPlayerNearby(p)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isOpened() {
		return this.isOpen;
	}
	
	public void setOpened(boolean isOpen) {
		setOpened(isOpen, new ArrayList<Door>());
	}
	
	private void setOpened(boolean isOpen, ArrayList<Door> doNotActivateDoors) {
		if (isOpen != isOpened()) {
			doNotActivateDoors.add(this);
			synchronized (this) {
				this.isOpen = isOpen;
				if (this.previousChangeStateTime == null) {
					this.previousChangeStateTime = System.currentTimeMillis();
				} else {
					this.previousChangeStateTime = 2 * System.currentTimeMillis() - getChangePositionTime() - this.previousChangeStateTime;
				}
			}
			for (Door d : LINKED_DOORS) {
				if (!doNotActivateDoors.contains(d)) {
					d.setOpened(isOpen, doNotActivateDoors);
				}
			}
		}
	}
	
	public void addLinkedDoor(Door door) {
		if (door != null) {
			synchronized (LINKED_DOORS) {
				LINKED_DOORS.add(door);
			}
		}
	}
	
	public long getChangePositionTime() {
		return CHANGE_POSITION_TIME;
	}
	
	public void updateDoorPosition() {
		Long previousChangeStateTime = this.previousChangeStateTime;
		if (previousChangeStateTime != null) {
			long time = System.currentTimeMillis() - previousChangeStateTime;
			double position = (isOpened() ? getChangePositionTime() - time : time) * 1d / getChangePositionTime();
			setBase(POINT_1.getPositionX(position, !isOpened()), POINT_1.getPositionY(position, !isOpened()), POINT_2.getPositionX(position, !isOpened()), POINT_2.getPositionY(position, !isOpened()));
			if (getChangePositionTime() + previousChangeStateTime < System.currentTimeMillis()) {
				synchronized (this) {
					this.previousChangeStateTime = null;
				}
			}
		}
	}
	
	private interface DoorMovementController extends Serializable {
		public double getPositionX(double positionFromOpen, boolean isOpenToClose);
		public double getPositionY(double positionFromOpen, boolean isOpenToClose);
		public double getPositionX(boolean isOpen);
		public double getPositionY(boolean isOpen);
	}
	
	public static class DoorStationaryPoint implements DoorMovementController {
		private static final long serialVersionUID = -245987594029306217L;
		
		private final double X;
		private final double Y;
		
		public DoorStationaryPoint(double x, double y) {
			X = x;
			Y = y;
		}

		@Override
		public double getPositionX(double positionFromOpen, boolean isOpenToClose) {
			return X;
		}

		@Override
		public double getPositionY(double positionFromOpen, boolean isOpenToClose) {
			return Y;
		}

		@Override
		public double getPositionX(boolean isOpen) {
			return X;
		}

		@Override
		public double getPositionY(boolean isOpen) {
			return Y;
		}
	}
	
	public static class DoorLinearMovementController implements DoorMovementController {
		private static final long serialVersionUID = -4885942096324835706L;
		
		private final double OPEN_X;
		private final double OPEN_Y;
		private final double CLOSE_X;
		private final double CLOSE_Y;
		
		public DoorLinearMovementController(double openX, double openY, double closeX, double closeY) {
			OPEN_X = openX;
			OPEN_Y = openY;
			CLOSE_X = closeX;
			CLOSE_Y = closeY;
		}
		
		@Override
		public double getPositionX(double positionFromOpen, boolean isOpenToClose) {
			positionFromOpen = Math.max(Math.min(positionFromOpen, 1), 0);
			return (CLOSE_X - OPEN_X) * positionFromOpen + OPEN_X;
		}

		@Override
		public double getPositionY(double positionFromOpen, boolean isOpenToClose) {
			positionFromOpen = Math.max(Math.min(positionFromOpen, 1), 0);
			return (CLOSE_Y - OPEN_Y) * positionFromOpen + OPEN_Y;
		}

		@Override
		public double getPositionX(boolean isOpen) {
			return isOpen ? OPEN_X : CLOSE_X;
		}

		@Override
		public double getPositionY(boolean isOpen) {
			return isOpen ? OPEN_Y : CLOSE_Y;
		}
	}
	
	public interface DoorActivationSelector extends Serializable {
		public boolean canActivateDoor(Player player);
	}
	
	public static class DoorActivationTeamSelector implements DoorActivationSelector {
		private static final long serialVersionUID = -4846379271897310272L;
		private final Team[] TEAMS;
		
		public DoorActivationTeamSelector(Team... teams) {
			TEAMS = teams;
		}

		@Override
		public boolean canActivateDoor(Player player) {
			for (Team t : TEAMS) {
				if (player.getTeam() == t || (t != null && t.equals(player.getTeam()))) {
					return true;
				}
			}
			return false;
		}
	}
	
	public static class DoorEllipticalMovementController implements DoorMovementController {
		private static final long serialVersionUID = -7701908236169538319L;
		
		private final double CENTER_X;
		private final double CENTER_Y;
		private final double RADIUS_X;
		private final double RADIUS_Y;
		private final double OPEN_POSITION_ANGLE;
		private final double CLOSE_POSITION_ANGLE;
		private final Compass.CircularDirection OPEN_TO_CLOSE_DIRECTION;
		private final Compass.CircularDirection CLOSE_TO_OPEN_DIRECTION;
		
		public DoorEllipticalMovementController(double centerX, double centerY, double radiusX, double radiusY, double openPositionAngle, double closePositionAngle, Compass.CircularDirection openToCloseDirection, Compass.CircularDirection closeToOpenDirection) {
			if (openToCloseDirection == null) throw new IllegalArgumentException("open to close direction cannot be null");
			if (closeToOpenDirection == null) throw new IllegalArgumentException("close to open direction cannot be null");
			CENTER_X = centerX;
			CENTER_Y = centerY;
			RADIUS_X = radiusX;
			RADIUS_Y = radiusY;
			OPEN_POSITION_ANGLE = (openPositionAngle % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
			CLOSE_POSITION_ANGLE = (closePositionAngle % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
			OPEN_TO_CLOSE_DIRECTION = openToCloseDirection;
			CLOSE_TO_OPEN_DIRECTION = closeToOpenDirection;
		}
		
		private double getAngle(double positionFromOpen, boolean isOpenToClose) {
			double angle;
			if (isOpenToClose) {
				if (CLOSE_POSITION_ANGLE > OPEN_POSITION_ANGLE) {
					if (OPEN_TO_CLOSE_DIRECTION == Compass.CircularDirection.COUNTER_CLOCKWISE) {
						angle = OPEN_POSITION_ANGLE + (CLOSE_POSITION_ANGLE - OPEN_POSITION_ANGLE) * positionFromOpen;
					} else {
						angle = OPEN_POSITION_ANGLE + (CLOSE_POSITION_ANGLE - OPEN_POSITION_ANGLE) * positionFromOpen - Math.PI * 2 * positionFromOpen;
					}
				} else {
					if (OPEN_TO_CLOSE_DIRECTION == Compass.CircularDirection.COUNTER_CLOCKWISE) {
						angle = OPEN_POSITION_ANGLE - (OPEN_POSITION_ANGLE - CLOSE_POSITION_ANGLE) * positionFromOpen + Math.PI * 2 * positionFromOpen;
					} else {
						angle = OPEN_POSITION_ANGLE - (OPEN_POSITION_ANGLE - CLOSE_POSITION_ANGLE) * positionFromOpen;
					}
				}
			} else {
				if (CLOSE_POSITION_ANGLE > OPEN_POSITION_ANGLE) {
					if (CLOSE_TO_OPEN_DIRECTION == Compass.CircularDirection.COUNTER_CLOCKWISE) {
						angle = OPEN_POSITION_ANGLE + (CLOSE_POSITION_ANGLE - OPEN_POSITION_ANGLE) * positionFromOpen - Math.PI * 2 * positionFromOpen;
					} else {
						angle = OPEN_POSITION_ANGLE + (CLOSE_POSITION_ANGLE - OPEN_POSITION_ANGLE) * positionFromOpen;
					}
				} else {
					if (CLOSE_TO_OPEN_DIRECTION == Compass.CircularDirection.COUNTER_CLOCKWISE) {
						angle = OPEN_POSITION_ANGLE - (OPEN_POSITION_ANGLE - CLOSE_POSITION_ANGLE) * positionFromOpen;
					} else {
						angle = OPEN_POSITION_ANGLE - (OPEN_POSITION_ANGLE - CLOSE_POSITION_ANGLE) * positionFromOpen + Math.PI * 2 * positionFromOpen;
					}
				}
			}
			return angle;
		}
		
		@Override
		public double getPositionX(double positionFromOpen, boolean isOpenToClose) {
			positionFromOpen = Math.max(Math.min(positionFromOpen, 1), 0);
			return CENTER_X + Math.cos(getAngle(positionFromOpen, isOpenToClose)) * RADIUS_X;
		}
		
		@Override
		public double getPositionY(double positionFromOpen, boolean isOpenToClose) {
			positionFromOpen = Math.max(Math.min(positionFromOpen, 1), 0);
			return CENTER_Y - Math.sin(getAngle(positionFromOpen, isOpenToClose)) * RADIUS_Y;
		}

		@Override
		public double getPositionX(boolean isOpen) {
			return isOpen ? CENTER_X + Math.cos(OPEN_POSITION_ANGLE) * RADIUS_X : CENTER_X + Math.cos(CLOSE_POSITION_ANGLE) * RADIUS_X;
		}

		@Override
		public double getPositionY(boolean isOpen) {
			return isOpen ? CENTER_Y - Math.sin(OPEN_POSITION_ANGLE) * RADIUS_Y : CENTER_Y - Math.sin(CLOSE_POSITION_ANGLE) * RADIUS_Y;
		}
	}
	
	public interface AutomaticallyClosingDoorTester {
		public boolean isPlayerNearby(Player p);
		
		public static class CubicalArea implements AutomaticallyClosingDoorTester {
			
			private final double X;
			private final double Y;
			private final double Z;
			private final double WIDTH;
			private final double DEPTH;
			private final double HEIGHT;
			
			public CubicalArea(double x, double y, double z, double width, double depth, double height) {
				X = x;
				Y = y;
				Z = z;
				WIDTH = width;
				DEPTH = depth;
				HEIGHT = height;
			}
			
			public boolean isPlayerNearby(Player p) {
				return p.getLocationX() + p.getPhysicalHalfWidth() >= X && p.getLocationX() - p.getPhysicalHalfWidth() <= X + WIDTH &&
						p.getLocationY() + p.getPhysicalHalfWidth() >= Y && p.getLocationY() - p.getPhysicalHalfWidth() <= Y + DEPTH &&
						p.getBottomHeight() <= Z + HEIGHT && p.getTopHeight() >= Z;
			}
		}
	}
}
