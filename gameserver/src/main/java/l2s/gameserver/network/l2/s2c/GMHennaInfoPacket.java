package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Henna;
import l2s.gameserver.model.actor.instances.player.HennaList;

public class GMHennaInfoPacket extends L2GameServerPacket
{
	private final Player _player;
	private final HennaList _hennaList;

	public GMHennaInfoPacket(Player player)
	{
		_player = player;
		_hennaList = player.getHennaList();
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_hennaList.getINT()); //equip INT
		writeH(_hennaList.getSTR()); //equip STR
		writeH(_hennaList.getCON()); //equip CON
		writeH(_hennaList.getMEN()); //equip MEN
		writeH(_hennaList.getDEX()); //equip DEX
		writeH(_hennaList.getWIT()); //equip WIT
		writeH(0x00); // LUC
		writeH(0x00); // CHA
		writeD(HennaList.MAX_SIZE); //interlude, slots?
		writeD(_hennaList.size());
		for(Henna henna : _hennaList.values(false))
		{
			writeD(henna.getTemplate().getSymbolId());
			writeD(_hennaList.isActive(henna));
		}

		Henna henna = _hennaList.getPremiumHenna();
		if(henna != null)
		{
			writeD(henna.getTemplate().getSymbolId());	// Premium symbol ID
			writeD(_hennaList.isActive(henna));	// Premium symbol active
			writeD(henna.getLeftTime());	// Premium symbol left time
		}
		else
		{
			writeD(0x00);	// Premium symbol ID
			writeD(0x00);	// Premium symbol active
			writeD(0x00);	// Premium symbol left time
		}
	}
}