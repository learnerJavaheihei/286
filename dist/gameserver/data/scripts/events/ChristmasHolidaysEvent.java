package events;

import java.util.concurrent.atomic.AtomicBoolean;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;

/**
 * @author Bonux
**/
public class ChristmasHolidaysEvent extends FunEvent
{
	private final double _raidRespawnTimeModifier;
	private final AtomicBoolean _respawnModified = new AtomicBoolean(false);

	public ChristmasHolidaysEvent(MultiValueSet<String> set)
	{
		super(set);
		_raidRespawnTimeModifier = set.getDouble("raid_respawn_time_modifier", 1.);
	}

	@Override
	public void startEvent()
	{
		super.startEvent();

		if(_respawnModified.compareAndSet(false, true))
			Config.ALT_RAID_RESPAWN_MULTIPLIER = Config.ALT_RAID_RESPAWN_MULTIPLIER * _raidRespawnTimeModifier;
	}

	@Override
	public void stopEvent(boolean force)
	{
		super.stopEvent(force);

		if(_respawnModified.compareAndSet(true, false))
			Config.ALT_RAID_RESPAWN_MULTIPLIER = Config.ALT_RAID_RESPAWN_MULTIPLIER / _raidRespawnTimeModifier;
	}
}
