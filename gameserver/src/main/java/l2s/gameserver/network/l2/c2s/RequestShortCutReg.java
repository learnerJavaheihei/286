package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.CostumesHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.ShortCutRegisterPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.CostumeTemplate;

public class RequestShortCutReg extends L2GameClientPacket
{
	private ShortCut.ShortCutType _type;
	private int _id, _slot, _page, _lvl, _characterType;

	@Override
	protected boolean readImpl()
	{
		try
		{
			_type = ShortCut.ShortCutType.VALUES[readD()];
		}
		catch (Exception e)
		{
			return false;
		}
		int slot = readD();
		int unk0 = readC();
		_id = readD();
		_lvl = readD();
		_characterType = readD();

		int unk1 = readD(); // UNK
		int unk2 = readD(); // UNK

		_slot = slot % 12;
		_page = slot / 12;
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_page < 0 || _page > ShortCut.PAGE_MAX)
		{
			activeChar.sendActionFailed();
			return;
		}

		IBroadcastPacket msg = activeChar.getAutoShortCuts().canRegShortCut(_slot, _page, _type, _id);
		if(msg != null)
		{
			activeChar.sendPacket(msg);
			return;
		}

		ShortCut shortCut = new ShortCut(_slot, _page, _type, _id, _lvl, _characterType);
		activeChar.sendPacket(new ShortCutRegisterPacket(activeChar, shortCut));
		activeChar.registerShortCut(shortCut);

		CostumeTemplate costumeTemplate = CostumesHolder.getInstance().getCostumeBySkillId(_id);
		if (costumeTemplate != null){
			activeChar.addSkill(SkillEntry.makeSkillEntry(SkillEntryType.COSTUME,_id,1),true);
			activeChar.sendSkillList();
		}

		
		if ((_slot == 1) && (_page == 23))
			activeChar.getAutoShortCuts().activate(277, true);
	}
}