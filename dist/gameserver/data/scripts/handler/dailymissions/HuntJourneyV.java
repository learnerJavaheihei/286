package handler.dailymissions;

/**
 * @author L2-scripts.com - (sSanyaDC)
 **/
public class HuntJourneyV extends DailyHunting {
	//земля ветров 21777,21778,21779,21780,21781,21782,21783,21784
	//кузница богов 21765,21685,21674,21673,21676,21678,21686
	//стена агроса 21813,21814,21817,21815,21816,21774,21818,21819,21821,21820
	//сады богини евы 20794,20792,20796,20795,20798,20797,20799
	private final int[] MONSTER_IDS = {
			21777,21778,21779,21780,21781,21782,21783,21784,21765,21685,21674,21673,21676,21678,21686,21813,21814,21817,21815,21816,21774,21818,21819,21821,21820,20794,20792,20796,20795,20798,20797,20799
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
