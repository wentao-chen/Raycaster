import java.io.Serializable;


public interface CauseOfDeath extends Serializable {
	
	public static final CauseOfDeath LAVA_DEATH = new StandardCauseOfDeath("BURNED BY LAVA");
	
	public static final CauseOfDeath ZOMBIE_DEATH = new StandardCauseOfDeath("ATE BRAINS");
	
	public static final CauseOfDeath FALL_DEATH = new StandardCauseOfDeath("FELL");
	
	/**
	 * Gets a description of the cause of death.
	 * @return a description of the cause of death
	 */
	public String getName();

	public static class StandardCauseOfDeath implements CauseOfDeath {
		private static final long serialVersionUID = 8816852387349689894L;
		
		private final String NAME;
		
		public StandardCauseOfDeath(String name) {
			if (name == null) throw new IllegalArgumentException("name cannot be null");
			NAME = name.toUpperCase();
		}
		
		@Override
		public String getName() {
			return NAME;
		}
	}
}
