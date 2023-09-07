package l2s.gameserver.network.l2.s2c.timerestrictfield;

import java.util.HashMap;
import java.util.Map;

import l2s.gameserver.data.xml.holder.TimeRestrictFieldHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.TimeRestrictFieldInfo;

/**
 * @author nexvill
 */
public class ExTimeRestrictFieldList extends L2GameServerPacket
{
	private final Player _player;
	private Map<Integer, TimeRestrictFieldInfo> _fields = new HashMap<>();

	public ExTimeRestrictFieldList(Player player)
	{
		_player = player;
		_fields = TimeRestrictFieldHolder.getInstance().getFields();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_fields.size());
		
		if (_fields.size() > 0)
		{
			for (int id : _fields.keySet())
			{
				final TimeRestrictFieldInfo field = _fields.get(id);
				
				writeD(1);
				writeD(field.getItemId());
				writeQ(field.getItemCount());
				writeD(field.getResetCycle());
				writeD(id);
				writeD(field.getMinLevel());
				writeD(field.getMaxLevel());
				
				
				int reflectionId = 0;
				switch (id)
				{
					case 2:
					{
						reflectionId = -1000;
						break;
					}
				}
				
				int remainTime = 0;
				if ((_player.getReflection().getId() <= -1000) && (_player.getReflection().getId() == reflectionId))
				{
					remainTime = _player.getTimeRestrictFieldRemainTime();
				}
				else
				{
					remainTime = _player.getVarInt(PlayerVariables.RESTRICT_FIELD_TIMELEFT + "_" 
							+ reflectionId, field.getRemainTimeBase());
				}
				int remainTimeRefill = _player.getVarInt(PlayerVariables.RESTRICT_FIELD_TIMELEFT + "_"
						+ reflectionId + "_refill", field.getRemainTimeMax() - field.getRemainTimeBase());
				
				writeD(field.getRemainTimeBase());
				writeD(remainTime);
				writeD(field.getRemainTimeMax());
				writeD(remainTimeRefill);
				writeD(field.getRemainTimeMax() - field.getRemainTimeBase());
				writeC(1); // is field active
				writeC(0);
				writeC(0);
				writeC(0);
				writeC(0);
				writeC(0); // is cross-server field
			}
		}
	}
}