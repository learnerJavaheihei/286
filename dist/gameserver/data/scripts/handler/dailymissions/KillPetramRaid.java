package handler.dailymissions;

/**
 * @author Bonux
 **/
public class KillPetramRaid extends DailyHunting {
	private final int[] MONSTER_IDS = {
			29108    // Петрам
	};

	@Override
	protected int[] getMonsterIds() {
		return MONSTER_IDS;
	}

	@Override
	public boolean isReusable()
	{
		return false;
	}
}
