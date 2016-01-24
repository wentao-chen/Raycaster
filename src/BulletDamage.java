
public interface BulletDamage extends Damage {
	
	public int getDamage(HitBox hitBox, int armor, boolean hasHelmet);
	
	public int getArmorDamageAbsorbed(HitBox hitBox, int armor, boolean hasHelmet);
	
	public static class StandardBulletDamage implements BulletDamage {
		private static final long serialVersionUID = 6761509465563650395L;
		
		private final int DEFAULT_DAMAGE;
		private final double ARMOR_PENETRATION;
		
		public StandardBulletDamage(int defaultDamage, double armorPenetration) {
			DEFAULT_DAMAGE = Math.abs(defaultDamage);
			ARMOR_PENETRATION = Math.min(Math.max(armorPenetration, 0), 1);
		}

		@Override
		public int getDefaultDamage() {
			return DEFAULT_DAMAGE;
		}

		@Override
		public int getDamage(HitBox hitBox, int armor, boolean hasHelmet) {
			if (hitBox == HitBox.LEG) {
				return (int) Math.round(getDefaultDamage() * HitBox.LEG.getMultiplier());
			} else if (hitBox == HitBox.HEAD && !hasHelmet) {
				return (int) Math.round(getDefaultDamage() * HitBox.HEAD.getMultiplier());
			} else {
				for (HitBox h : HitBox.values()) {
					if (h == hitBox) {
						if (armor > 0) {
							return (int) Math.round(getDefaultDamage() * h.getMultiplier() * ARMOR_PENETRATION);
						} else {
							return (int) Math.round(getDefaultDamage() * h.getMultiplier());
						}
					}
				}
				return getDefaultDamage();
			}
		}

		@Override
		public int getArmorDamageAbsorbed(HitBox hitBox, int armor, boolean hasHelmet) {
			return Math.max((int) Math.round(getDefaultDamage() * hitBox.getMultiplier()) - getDamage(hitBox, armor, hasHelmet), 0);
		}
	}
}
