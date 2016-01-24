import java.io.Serializable;


public interface FallDamageCalculator extends Serializable {
	
	public int getFallDamage(double height);

	public static class StandardFallDamageCalaculator implements FallDamageCalculator {
		private static final long serialVersionUID = 3552419451070196242L;

		@Override
		public int getFallDamage(double height) {
			height *= 52.63;
			if (height <= 240) return 0;
			else if (height <= 249) return 2;
			else if (height <= 257) return 4;
			else if (height <= 265) return 6;
			else if (height <= 274) return 9;
			else if (height <= 282) return 11;
			else if (height <= 292) return 13;
			else if (height <= 301) return 15;
			else if (height <= 309) return 18;
			else if (height <= 318) return 20;
			else if (height <= 327) return 22;
			else if (height <= 337) return 24;
			else if (height <= 347) return 27;
			else if (height <= 357) return 29;
			else if (height <= 366) return 31;
			else if (height <= 376) return 34;
			else if (height <= 387) return 36;
			else if (height <= 396) return 38;
			else if (height <= 408) return 40;
			else if (height <= 417) return 43;
			else if (height <= 428) return 45;
			else if (height <= 439) return 47;
			else if (height <= 450) return 49;
			else if (height <= 461) return 52;
			else if (height <= 472) return 54;
			else if (height <= 483) return 56;
			else if (height <= 495) return 59;
			else if (height <= 507) return 61;
			else if (height <= 529) return 65;
			else if (height <= 542) return 68;
			else if (height <= 554) return 70;
			else if (height <= 567) return 72;
			else if (height <= 579) return 75;
			else if (height <= 592) return 77;
			else if (height <= 604) return 79;
			else if (height <= 617) return 81;
			else if (height <= 630) return 84;
			else if (height <= 643) return 86;
			else if (height <= 656) return 88;
			else if (height <= 670) return 90;
			else if (height <= 683) return 93;
			else if (height <= 697) return 95;
			else if (height <= 711) return 97;
			else if (height <= 725) return 99;
			else return 100;
		}
	}
}
