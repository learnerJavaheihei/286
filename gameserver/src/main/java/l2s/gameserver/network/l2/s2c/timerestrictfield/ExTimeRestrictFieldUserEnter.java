package l2s.gameserver.network.l2.s2c.timerestrictfield;

import java.util.HashMap;
import java.util.Map;

import l2s.gameserver.data.xml.holder.TimeRestrictFieldHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.TimeRestrictFieldInfo;

/**
 * @author nexvill
 */
public class ExTimeRestrictFieldUserEnter extends L2GameServerPacket
{
	private final Player _player;
	private final int _fieldId;
	private Map<Integer, TimeRestrictFieldInfo> _fields = new HashMap<>();

	public ExTimeRestrictFieldUserEnter(Player player, int fieldId)
	{
		_player = player;
		_fieldId = fieldId;
		_fields = TimeRestrictFieldHolder.getInstance().getFields();
	}

	@Override
	protected final void writeImpl()
	{
		TimeRestrictFieldInfo field = _fields.get(_fieldId);
		
		int reflectionId = 0;
		switch (field.getFieldId())
		{
			case 2:
			{
				reflectionId = -1000;
				break;
			}
		}
		
		int remainTime = _player.getVarInt(PlayerVariables.RESTRICT_FIELD_TIMELEFT + "_" + reflectionId,
				field.getRemainTimeBase());
		int remainTimeRefill = _player.getVarInt(PlayerVariables.RESTRICT_FIELD_TIMELEFT + "_" + reflectionId
				+ "_refill", field.getRemainTimeMax() - field.getRemainTimeBase());
		
		if (_player.consumeItem(field.getItemId(), field.getItemCount(), true))
		{
			_player.setVar(PlayerVariables.RESTRICT_FIELD_TIMESTART + "_" + reflectionId, System.currentTimeMillis());
			_player.setVar(PlayerVariables.RESTRICT_FIELD_TIMELEFT + "_" + reflectionId, remainTime);
			_player.setVar(PlayerVariables.RESTRICT_FIELD_TIMELEFT + "_" + reflectionId + "_refill", remainTimeRefill);
			switch (field.getFieldId())
			{
				case 2:
				{
					_player.teleToLocation(field.getEnterLoc(), ReflectionManager.ANCIENT_PIRATES_TOMB);
					_player.setReflection(ReflectionManager.ANCIENT_PIRATES_TOMB);
					_player.setActiveReflection(ReflectionManager.ANCIENT_PIRATES_TOMB);
					break;
				}
			}
			
			_player.startTimeRestrictField();
			
			writeC(1);
			writeD(_fieldId);
			writeD((int) (System.currentTimeMillis() / 1000));
			writeD(remainTime);
		}
		else
		{
			_player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
		}
	}
}