package l2s.gameserver.model.entity.olympiad;

import l2s.gameserver.Announcements;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.olympiad.ExOlympiadInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CompEndTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(CompEndTask.class);

	@Override
	public void run()
	{
		if(Olympiad.isOlympiadEnd())
			return;

		OlympiadManager manager = Olympiad._manager;
		if(manager != null && !manager.getOlympiadGames().isEmpty()) // Если остались игры, ждем их завершения еще одну минуту
		{
			Olympiad.startCompEndTask(60000);
			return;
		}

		Olympiad._inCompPeriod = false;
		
		for (Player player: GameObjectsStorage.getPlayers(false, false))
		{
			player.sendPacket(new ExOlympiadInfo());
		}

		Announcements.announceToAll(SystemMsg.MUCH_CARNAGE_HAS_BEEN_LEFT_FOR_THE_CLEANUP_CREW_OF_THE_OLYMPIAD_STADIUM);

		_log.info("Olympiad System: Olympiad Game Ended");

		try
		{
			OlympiadDatabase.save();
		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Failed to save Olympiad configuration:", e);
		}
		Olympiad.init();
	}
}