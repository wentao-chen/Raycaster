
public interface MeleeDamage extends Damage {
	
	public int getSlashDamage(HitBox hitBox, int armor, boolean isConsecutive, boolean isBackStab);
	public int getStabDamage(HitBox hitBox, int armor, boolean isConsecutive, boolean isBackStab);
	public int getSlashArmorDamageAbsorbed(HitBox hitBox, int armor, boolean isConsecutive, boolean isBackStab);
	public int getStabArmorDamageAbsorbed(HitBox hitBox, int armor, boolean isConsecutive, boolean isBackStab);

	public static class StandardMeleeDamage implements MeleeDamage {
		private static final long serialVersionUID = -3553958079995512539L;
		
		private final int DEFAULT_DAMAGE;
		private final double CONSECUTIVE_MULTIPLIER;
		private final int SLASH_DAMAGE;
		private final int STAB_DAMAGE;
		private final int BACK_SLASH_DAMAGE;
		private final int BACK_STAB_DAMAGE;
		private final double ARMOR_PENETRATION;
		
		public StandardMeleeDamage(int defaultDamage, double consecutiveMultiplier, int slashDamage, int stabDamage, int backSlashDamage, int backStabDamage, double armorPenetration) {
			DEFAULT_DAMAGE = Math.abs(defaultDamage);
			CONSECUTIVE_MULTIPLIER = Math.abs(consecutiveMultiplier);
			SLASH_DAMAGE = Math.abs(slashDamage);
			STAB_DAMAGE = Math.abs(stabDamage);
			BACK_SLASH_DAMAGE = Math.abs(backSlashDamage);
			BACK_STAB_DAMAGE = Math.abs(backStabDamage);
			ARMOR_PENETRATION = Math.max(Math.min(armorPenetration, 1), 0);
		}

		@Override
		public int getDefaultDamage() {
			return DEFAULT_DAMAGE;
		}

		@Override
		public int getSlashDamage(HitBox hitBox, int armor, boolean isConsecutive, boolean isBackStab) {
			return (int) Math.round((isBackStab ? BACK_SLASH_DAMAGE : SLASH_DAMAGE) * CONSECUTIVE_MULTIPLIER * (armor > 0 ? ARMOR_PENETRATION : 1));
		}

		@Override
		public int getSlashArmorDamageAbsorbed(HitBox hitBox, int armor, boolean isConsecutive, boolean isBackStab) {
			return (int) Math.round((isBackStab ? BACK_SLASH_DAMAGE : SLASH_DAMAGE) * CONSECUTIVE_MULTIPLIER) - getSlashDamage(hitBox, armor, isConsecutive, isBackStab);
		}

		@Override
		public int getStabDamage(HitBox hitBox, int armor, boolean isConsecutive, boolean isBackStab) {
			return (int) Math.round((isBackStab ? BACK_STAB_DAMAGE : STAB_DAMAGE) * CONSECUTIVE_MULTIPLIER * (armor > 0 ? ARMOR_PENETRATION : 1));
		}

		@Override
		public int getStabArmorDamageAbsorbed(HitBox hitBox, int armor, boolean isConsecutive, boolean isBackStab) {
			return (int) Math.round((isBackStab ? BACK_STAB_DAMAGE : STAB_DAMAGE) * CONSECUTIVE_MULTIPLIER) - getStabDamage(hitBox, armor, isConsecutive, isBackStab);
		}
	}
}
