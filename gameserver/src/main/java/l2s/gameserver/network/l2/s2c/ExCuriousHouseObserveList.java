package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux
**/
public class ExCuriousHouseObserveList extends L2GameServerPacket
{
	private static class ArenaInfo
	{
		public final int id;
		public final String unk;
		public final int status;
		public final int participants;

		public ArenaInfo(int id, String unk, int status, int participants)
		{
			this.id = id;
			this.unk = unk;
			this.status = status;
			this.participants = participants;
		}
	}

	private final List<ArenaInfo> _arenas = new ArrayList<ArenaInfo>();

	public ExCuriousHouseObserveList(int currentId)
	{
		/*ChaosFestivalEvent event = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, 6);
		if(event == null || !event.isInProgress())
			return;

		for(ChaosFestivalArenaObject arena : event.getArenas())
		{
			if(arena.getId() == currentId)
				continue;

			_arenas.add(new ArenaInfo(arena.getId(), "Arena #" + arena.getId(), arena.getBattleState().ordinal(), arena.getMembers().size()));
		}*/
	}

	public ExCuriousHouseObserveList()
	{
		this(-1);
	}

	@Override
	protected void writeImpl()
	{
		writeD(_arenas.size()); // Arena Count
		for(ArenaInfo arena : _arenas)
		{
			writeD(arena.id); // Arena
			writeS(arena.unk); // UNK
			writeH(arena.status); // Status
			writeD(arena.participants); // Patricipants Count
		}
	}
}
