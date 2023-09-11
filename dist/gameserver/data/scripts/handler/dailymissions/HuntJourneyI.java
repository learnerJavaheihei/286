package handler.dailymissions;

/**
 * @author L2-scripts.com - (sSanyaDC)
 **/
public class HuntJourneyI extends DailyHunting {
	//руины страданий 20026,20029,20035,20042,20359,20045,20051,20050,20055,20054,20059,20060,20548,20062,20064 
	// заброшеный лагерь 20782,20053,20437,20058,20061,20436,20063,20439,20066,20438,20076

	private final int[] MONSTER_IDS = {
			20026,20029,20035,20042,20359,20045,20051,20050,20055,20054,20059,20060,20548,20062,20064,20782,20053,20437,20058,20061,20436,20063,20439,20066,20438,20076
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
