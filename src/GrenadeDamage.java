
public interface GrenadeDamage extends Damage {
	
	/**
	 * Gets the duration of the flash in milliseconds
	 * @param distance the distance from the explosion
	 * @return the duration of the flash in milliseconds
	 */
	public long flashDuration(double distance);
	
	public int getDamage(double distance, int armor);
	public int getArmorDamageAbsorbed(double distance, int armor);

	public static class StandardGrenadeDamage implements GrenadeDamage {
		private static final long serialVersionUID = 8339750820104517206L;
		private final int DEFAULT_DAMAGE;
		private final double FATAL_RADIUS;
		private final double EXPLOSION_RADIUS;
		private final long MAX_FLASH_DURATION;
		private final double FLASH_RADIUS;
		private final double ARMOR_PENETRATION;
		
		/**
		 * Creates the standard grenade damage for basic explosions.
		 * @param defaultDamage the damage of the explosion
		 * @param fatalRadius the distance from the point of the explosion within which causes the maximum damage
		 * @param explosionRadius the distance from the point of the explosion within which causes damage
		 * @param armorPenetration the armor penetration of the explosion from {@code 0.0} to {@code 1.0}, inclusive
		 */
		public StandardGrenadeDamage(int defaultDamage, double fatalRadius, double explosionRadius, double armorPenetration) {
			this(defaultDamage, fatalRadius, explosionRadius, 0, 0, armorPenetration);
		}
		
		/**
		 * Creates the standard grenade damage for flashbangs.
		 * @param maxFlashDuration the duration of the flash
		 * @param flashRadius the radius of the flash
		 */
		public StandardGrenadeDamage(long maxFlashDuration, double flashRadius) {
			this(0, 0, 0, maxFlashDuration, flashRadius, 1);
		}
		
		public StandardGrenadeDamage(int defaultDamage, double fatalRadius, double explosionRadius, long maxFlashDuration, double flashRadius, double armorPenetration) {
			DEFAULT_DAMAGE = Math.abs(defaultDamage);
			FATAL_RADIUS = Math.min(fatalRadius, explosionRadius);
			EXPLOSION_RADIUS = Math.max(fatalRadius, explosionRadius);
			MAX_FLASH_DURATION = Math.abs(maxFlashDuration);
			FLASH_RADIUS = Math.abs(flashRadius);
			ARMOR_PENETRATION = Math.min(Math.max(armorPenetration, 0), 1);
		}
		
		@Override
		public int getDefaultDamage() {
			return DEFAULT_DAMAGE;
		}
		
		@Override
		public long flashDuration(double distance) {
			return Math.round(Math.max(Math.pow(1 - distance / FLASH_RADIUS, 1.3), 0) * MAX_FLASH_DURATION);
		}
		
		private double getDistanceMultiplier(double distance) {
			if (distance <= FATAL_RADIUS) {
				return 1;
			} else if (distance > EXPLOSION_RADIUS) {
				return 0;
			} else {
				return 1 - (distance - FATAL_RADIUS) / (EXPLOSION_RADIUS - FATAL_RADIUS);
			}
		}
		
		@Override
		public int getDamage(double distance, int armor) {
			return (int) Math.round(getDistanceMultiplier(distance) * getDefaultDamage() * (armor > 0 ? ARMOR_PENETRATION : 1));
		}
		
		@Override
		public int getArmorDamageAbsorbed(double distance, int armor) {
			return (int) Math.round(getDistanceMultiplier(distance) * getDefaultDamage()) - getDamage(distance, armor);
		}
	}
}
