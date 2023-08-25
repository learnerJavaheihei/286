package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.actor.instances.player.Macro;

/**
 * packet type id 0xe7
 *
 * sample
 *
 * e7
 * d // unknown change of Macro edit,add,delete
 * c // unknown
 * c //count of Macros
 * c // unknown
 *
 * d // id
 * S // macro name
 * S // desc
 * S // acronym
 * c // icon
 * c // count
 *
 * c // entry
 * c // type
 * d // skill id
 * c // shortcut id
 * S // command name
 *
 * format:		cdccdSSScc (ccdcS)
 */
public class MacroListPacket extends L2GameServerPacket
{
	public static enum Action
	{
		DELETE,
		ADD,
		UPDATE
	}

	private final int _macroId, _count;
	private final Action _action;
	private final Macro _macro;

	public MacroListPacket(int macroId, Action action, int count, Macro macro)
	{
		_macroId = macroId;
		_action = action;
		_count = count;
		_macro = macro;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_action.ordinal()); //unknown
		writeD(_macroId); //Macro ID
		writeC(_count); //count of Macros

		if(_macro != null)
		{
			writeC(1); //checked
			writeD(_macro.getId()); //Macro ID
			writeS(_macro.getName()); //Macro Name
			writeS(_macro.getDescr()); //Desc
			writeS(_macro.getAcronym()); //acronym
			writeD(_macro.getIcon()); //icon

			writeC(_macro.getCommands().length); //count

			for(int i = 0; i < _macro.getCommands().length; i++)
			{
				Macro.L2MacroCmd cmd = _macro.getCommands()[i];
				writeC(i + 1); //i of count
				writeC(cmd.getType()); //type  1 = skill, 3 = action, 4 = shortcut
				writeD(cmd.getParam1()); // skill id
				writeC(cmd.getParam2()); // shortcut id
				writeS(cmd.getCmd()); // command name
			}
		}
		else
			writeC(0); //checked
	}
}