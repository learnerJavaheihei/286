package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.AutoFarm;
import l2s.gameserver.network.l2.s2c.ExAutoplaySetting;

public class RequestExAutoplaySetting extends L2GameClientPacket
{
	private int _unkParam1, _unkParam2, _healPercent;
	private boolean _farmActivate, _autoPickUpItems, _meleeAttackMode, _politeFarm;;

	@Override
	protected boolean readImpl()
	{
		//cchcdch
		_unkParam1 = readH(); // 16 UNK
		_farmActivate = readC() > 0; // Auto Farm Enabled
		_autoPickUpItems = readC() > 0; // Auto Pick Up items
		_unkParam2 = readH();
		_meleeAttackMode = readC() > 0;
		_healPercent = readD(); // Auto Heal Percent
		readC(); //unk
		readC(); //unk
		readC(); //unk
		readC(); //unk
		_politeFarm = readC() > 0;
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
			return;

		AutoFarm autoFarm = player.getAutoFarm();
		autoFarm.setUnkParam1(_unkParam1);
		autoFarm.setFarmActivate(_farmActivate);
		autoFarm.setAutoPickUpItems(_autoPickUpItems);
		autoFarm.setUnkParam2(_unkParam2);
		autoFarm.setMeleeAttackMode(_meleeAttackMode);
		autoFarm.setHealPercent(_healPercent);
		autoFarm.setPoliteFarm(_politeFarm);
		autoFarm.doAutoFarm();

		player.sendPacket(new ExAutoplaySetting(player));
	}
}