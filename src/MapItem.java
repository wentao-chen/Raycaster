import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;


public interface MapItem extends Map.MapLocation2D, Serializable {
	
	/**
	 * Gets the name that will be displayed when the cursor is pointed at the item. If {@code null} is returned, nothing is displayed.
	 * @param player the player viewing the displayed name
	 * @return the name that will be displayed.
	 */
	public String getDisplayName(Player player);
	
	/**
	 * Gets the intersection point between a ray and the initial contact point between the map item and the ray
	 * @param ray the ray to be tested
	 * @return the point of intersection or {@code null} if no intersection occurs
	 */
	public Point2D getIntersectionPoint(Map.Ray2D ray);
	
	/**
	 * Tests if the map item intersects a line segment
	 * @param line the line segment to be tested
	 * @return {@code true} if an intersection occurs; otherwise {@code false}
	 */
	public boolean intersects(Line2D line);
	
	/**
	 * Called whenever the item is hit by a melee weapon.
	 * @param melee the melee weapon used
	 * @param isConsecutive true if the hit is part of a series of hits; otherwise, false
	 * @param ray a ray representing the direction and the distance of the hit
	 * @param slasher the attacker
	 * @param attackType the type of melee attack
	 */
	public void hitByMelee(Melee melee, boolean isConsecutive, Map.Ray2D ray, Player slasher, Melee.AttackType attackType);
	
	/**
	 * Invoked when the base of the map item is collides with the 2D path of a bullet. If this method returns {@code true}, the bullet is consumed. If this method returns {@code false}, the bullet continues to move and search for the next object that will be hit.
	 * Note: This method does not consider the height at which the bullet collides with the map item.
	 * @param b the bullet that hit the map item
	 * @param game the game in which the bullet hit
	 * @param distanceTraveled a positive value specifying the distance traveled on the x y plane by the bullet in meters when it collides with the base of the map item
	 * @return true if the bullet is consumed; otherwise, false
	 */
	public boolean baseHitByBullet(Bullet b, Game game, double distanceTraveled);
	
	/**
	 * Called when the item is hit by an explosion.
	 * @param w the weapon causing the explosion
	 * @param explodeX the x-coordinate of the location of the center of the explosion
	 * @param explodeY the y-coordinate of the location of the center of the explosion
	 */
	public void hitByExplosion(ThrownWeapon w, double explodeX, double explodeY);

	/**
	 * Tests if a player can view the item on the radar.
	 * @param player the owner of the radar observing the item
	 * @return {@code true} if the item can be viewed on the radar; otherwise {@code false}
	 */
	public boolean canDetectOnRadar(Player player);
	
	/**
	 * Gets the color of the item when displayed on a radar. If {@code null} is returned, the item will not be displayed.
	 * @param player the owner of the radar observing the item
	 * @return the color displayed on the radar
	 */
	public Color getRadarColor(Player player);

}
