package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.network.l2.s2c.ExElementalSpiritInfo;

/**
 * @author Bonux
**/
public class RequestExElementalSpiritChangeType extends L2GameClientPacket
{
	private int _elementId;
	private int _unk1;

	@Override
	protected boolean readImpl()
	{
		_unk1 = readC(); // TODO: Приходит 2.
		_elementId = readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!activeChar.changeActiveElement(ElementalElement.getElementById(_elementId)))
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.sendElementalInfo();
		activeChar.sendPacket(new ExElementalSpiritInfo(activeChar, 0));
	}
}