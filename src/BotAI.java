import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;


public abstract class BotAI implements PlayerMovementController {
	private static final long serialVersionUID = 5598101916854273213L;

	public static class ZombieAI extends BotAI {
		private static final long serialVersionUID = 1772331397054560287L;
		
		private boolean jumpingZombie;
		private final Player TARGET;
		private transient SearchNode mostRecentFinalNode = null;
		private static boolean isSearching = false;
		
		public ZombieAI(Player target, boolean jumpingZombies) {
			if (target == null) throw new IllegalArgumentException("zombie target cannot be null");
			TARGET = target;
			this.jumpingZombie = jumpingZombies;
		}
		
		public Player getTarget() {
			return TARGET;
		}

	    public boolean isJumpingZombie() {
			return this.jumpingZombie;
		}
	    
	    public synchronized void setJumpingZombie(boolean jump) {
	    	this.jumpingZombie = jump;
	    }

		@Override
		public void movePlayer(final Player PLAYER, final long TIME_PASSED) {
			if (PLAYER.isSpectator()) {
				return;
			}
			
			if (isJumpingZombie()) {
				PLAYER.jump(TIME_PASSED);
			}
			synchronized (ZombieAI.class) {
				ZombieAI.isSearching = false;
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					SearchNode node = findPath((int) Math.floor(PLAYER.getLocationX()), (int) Math.floor(PLAYER.getLocationY()), (int) Math.floor(TARGET.getLocationX()), (int) Math.floor(TARGET.getLocationY()), PLAYER, TIME_PASSED, TARGET);
					synchronized (ZombieAI.this) {
						ZombieAI.this.mostRecentFinalNode = node;
					}
				}
			}).start();
			
			Double direction = findNextDirection(this.mostRecentFinalNode, PLAYER);
			if (direction != null) {
				PLAYER.setHorizontalDirection(direction);
				PLAYER.move(TIME_PASSED, 0, false);
			} else {
				PLAYER.setHorizontalDirection(Compass.getPrincipleAngle(getTarget().getLocationX() - PLAYER.getLocationX(), PLAYER.getLocationY() - getTarget().getLocationY()));
				Point2D.Double nextLocation = PLAYER.nextLocation(TIME_PASSED, 0, false);
				if (PLAYER.getGame().getMap().getWall(nextLocation.x, nextLocation.y).getLavaDamage() <= 0) {
					PLAYER.move(TIME_PASSED, 0, false);
				}
			}
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	         in.defaultReadObject();
	         this.mostRecentFinalNode = null;
	    }
		
		private static Double findNextDirection(SearchNode finalNode, Player player) {
			SearchNode secondNode = finalNode != null ? finalNode.findSecondNode() : null;
			return secondNode != null && player != null ? Compass.getPrincipleAngle(player.getLocationX(), secondNode.Y + 0.5, secondNode.X + 0.5, player.getLocationY()) : null;
		}
		
		private static SearchNode findPath(int startX, int startY, int endX, int endY, Player player, long timePassed, Player target) {
			// A* method path finding algorithm [SOURCE: A* Pathfinding for Beginners by Patrick Lester (July 18, 2005)]
			
			List<SearchNode> openList = new ArrayList<SearchNode>();
			// TODO List<SearchNode> closedList = new ArrayList<SearchNode>();
			
			// 1) Add the starting square (or node) to the open list.
			SearchNode firstNode = new SearchNode(startX, startY, null, 0, 0);
			SearchNode finalNode = null;
			openList.add(firstNode);
			
			synchronized (ZombieAI.class) {
				ZombieAI.isSearching = true;
			}
			// 2) Repeat the following:
			do {
				// a) Look for the lowest F cost square on the open list. We refer to this as the current square.
				SearchNode current = SearchNode.findLowestCostNode(openList);
				// b) Switch it to the closed list.
				openList.remove(current);
				// TODO closedList.add(current);
				if (current.X == endX && current.Y == endY) {
					finalNode = current;
					break;
				}
				// c) For each of the squares adjacent to this current square …
				for (Compass.Cardinal c : Compass.Cardinal.values()) {
					int deltaX = (int) Math.round(Math.cos(Math.PI / 2 - c.getBearing()));
					int deltaY = (int) Math.round(Math.sin(Math.PI / 2 - c.getBearing()));
					int x = current.X + deltaX;
					int y = current.Y - deltaY;
					// - If it is not walkable or if it is on the closed list, ignore it. Otherwise do the following.s
					// - If it isn't on the open list, add it to the open list. Make the current square the parent of this square. Record the F, G, and H costs of the square.
					// - If it is on the open list already, check to see if this path to that square is better, using G cost as the measure. A lower G cost means that this is a better path.
					//   	If so, change the parent of the square to the current square, and recalculate the G and F scores of the square. If you are keeping your open list sorted by F score,
					//   	you may need to resort the list to account for the change.
					if (player.getMap().inGrid(x, y) && player.getMap().getWall(x, y).getTopHeight(Integer.MAX_VALUE) <= player.getMap().getWall(current.X, current.Y).getTopHeight(Integer.MAX_VALUE) + player.getStepHeight() && player.getMap().getWall(x, y).getLavaDamage() <= 0/*player.isMoveableTo(x, y, false, true)*/) {
						SearchNode openListNode = SearchNode.findNode(openList, x, y);
						double currentPresentCost = 1 + current.presentCost;
						if (openListNode == null) {
							SearchNode newNode = new SearchNode(x, y, current, currentPresentCost, Map.findDistance(current.X + deltaX, current.Y - deltaY, target.getLocationX(), target.getLocationY()));
							openList.add(newNode);
						} else {
							if (currentPresentCost < openListNode.presentCost) {
								openListNode.parent = current;
								openListNode.presentCost = currentPresentCost;
							}
						}
					}
				}
				// d) Stop when you:
				// - Add the target square to the closed list, in which case the path has been found (see note below), or
				// - Fail to find the target square, and the open list is empty. In this case, there is no path.
			} while(ZombieAI.isSearching && openList.size() > 0);
			return finalNode;
		}
		
		private static class SearchNode {
			private final int X;
			private final int Y;
			private SearchNode parent;
			private double presentCost;
			private final double ESTIMATED_FUTURE_COST;
			private SearchNode(int x, int y, SearchNode parent, double presentCost, double estimatedFutureCost) {
				X = x;
				Y = y;
				this.parent = parent;
				this.presentCost = presentCost;
				ESTIMATED_FUTURE_COST = estimatedFutureCost;
			}
			private SearchNode findSecondNode() {
				if (this.parent == null) {
					return null;
				} else if (this.parent.parent == null) {
					return this;
				} else {
					return this.parent.findSecondNode();
				}
			}
			private double getTotalCost() {
				return this.presentCost + ESTIMATED_FUTURE_COST;
			}
			private static SearchNode findNode(List<SearchNode> nodes, int x, int y) {
				for (SearchNode n : nodes) {
					if (n.X == x && n.Y == y) {
						return n;
					}
				}
				return null;
			}
			private static SearchNode findLowestCostNode(List<SearchNode> nodes) {
				SearchNode lowestCost = null;
				for (SearchNode n : nodes) {
					if (lowestCost == null || lowestCost.getTotalCost() > n.getTotalCost()) {
						lowestCost = n;
					}
				}
				return lowestCost;
			}
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + X;
				result = prime * result + Y;
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
				SearchNode other = (SearchNode) obj;
				if (X != other.X)
					return false;
				if (Y != other.Y)
					return false;
				return true;
			}
		}
	}
}
