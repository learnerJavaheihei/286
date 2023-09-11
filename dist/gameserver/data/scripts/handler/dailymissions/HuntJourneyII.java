package handler.dailymissions;

/**
 * @author L2-scripts.com - (sSanyaDC)
 **/
public class HuntJourneyII extends DailyHunting {
	//сад горгон 20199,20248,20145,20249,20158,20176
	//долина смерти 20176,20550,20551,20552,20553,20554
	//Муравейник 20075,20079,20080,20081,20082,20084,20087,20086,20088,20089,20090
	private final int[] MONSTER_IDS = {
			20199,20248,20145,20249,20158,20176,20550,20551,20552,20553,20554,20075,20079,20080,20081,20082,20084,20087,20086,20088,20089,20090
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
