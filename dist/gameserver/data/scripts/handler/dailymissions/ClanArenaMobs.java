package handler.dailymissions;

/**
 * @author L2-scripts.com - (sSanyaDC)
 **/
public class ClanArenaMobs extends DailyHunting {
	private final int[] MONSTER_IDS = {
			25794,25795,25796,25797,25798,25799,25800,25801,25802,25803,25804,25805,25806,25807,25808,25809,25810,25811,25812,25813,25834,25835,25836,25837,25838
	};

	@Override
	public boolean isReusable() {
		return false;
	}

	@Override
	protected int[] getMonsterIds() {
		return MONSTER_IDS;
	}
}
