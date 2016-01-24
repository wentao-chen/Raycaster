import java.awt.Color;

/**
 * A 3D-prism that can be rendered by {@link ProjectionPlane}. The base of the prism lies on the x-y plane with its height in the z-direction.
 * The base of the prism must be a convex shape.
 */
public interface MapItem3D extends MapItem, ProjectionPlane.ProjectionPlaneItem {

	/**
	 * The distance between the two points of intersection between the outline of the circular base and a specified ray.
	 * @param ray the ray passing through the map item
	 * @return distance between the two points of intersection between the outline of the base and a specified line
	 */
	public double getTopSurfaceDistance(Map.Ray2D ray);
	
	public double getTopHeight(int index);
	
	public double getBottomHeight(int index);
	
	public int getNumberOfGaps();
	
	/**
	 * Gets the color of the top surface of the map item.
	 * @return the color of the top surface
	 */
	public Color getTopColor();
	
	/**
	 * Gets the color of the surface that is first intersected by a ray
	 * @param ray the ray passing through the map item
	 * @return the color of the surface
	 */
	public Color getProjectedColor(Map.Ray2D ray);
	
	/**
	 * Checks if the map item is transparent from the direction of the ray.
	 * @param ray the ray passing through the map item
	 * @return {@code true} if the map item is transparent; otherwise, {@code false}
	 */
	public boolean isTransparent(Map.Ray2D ray);
	
	/**
	 * Finds the shortest distance in meters between a given point and the map item.
	  * @param x the x-coordinate (in meters) of the given point
	 * @param y the y-coordinate (in meters) of the given point
	 * @return the shortest distance between the given point and the map item in meters
	 */
	public double getClosestDistance(double x, double y);

}
