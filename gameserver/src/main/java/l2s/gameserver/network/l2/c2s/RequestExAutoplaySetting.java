package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
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
		BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
		botConfig.setPickUpItem(_autoPickUpItems);
		botConfig.set_autoAdjustRange(!_meleeAttackMode);
		BotEngine.getInstance().Adjust(player,botConfig);
		player.setAutoLoot(_autoPickUpItems);

		autoFarm.setAutoPickUpItems(_autoPickUpItems);
		autoFarm.setPoliteFarm(_politeFarm);
		autoFarm.setHealPercent(_healPercent);
		autoFarm.setMeleeAttackMode(_meleeAttackMode);
		if (_farmActivate){
			BotEngine.getInstance().startBotTask(player);
			autoFarm.setUnkParam1(_unkParam1);
			autoFarm.setFarmActivate(_farmActivate);
			autoFarm.setUnkParam2(_unkParam2);
			player.sendPacket(new ExAutoplaySetting(player));
		}
		else{
			botConfig.setAbort(true, "");
		}
//		autoFarm.doAutoFarm();
//		player.sendPacket(new ExAutoplaySetting(player));
	}
}