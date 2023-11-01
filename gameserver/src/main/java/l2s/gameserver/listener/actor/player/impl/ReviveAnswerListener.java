package l2s.gameserver.listener.actor.player.impl;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;

/**
 * @author VISTALL
 * @date 11:35/15.04.2011
 */
public class ReviveAnswerListener implements OnAnswerListener
{
	private HardReference<Player> _playerRef;
	private double _power;
	private boolean _forPet;
	private Player _reviver;

	public ReviveAnswerListener(Player player, double power, boolean forPet)
	{
		_playerRef = player.getRef();
		_forPet = forPet;
		_power = power;
	}
	public ReviveAnswerListener(Player player, double power, boolean forPet,Player reviver)
	{
		_playerRef = player.getRef();
		_forPet = forPet;
		_power = power;
		_reviver =reviver;
	}
	@Override
	public void sayYes()
	{
		Player player = _playerRef.get();
		if(player == null)
			return;
		if(!player.isDead() && !_forPet || _forPet && player.getPet() != null && !player.getPet().isDead())
			return;

		if(!_forPet)
			player.doRevive(_power);
		else if(player.getPet() != null)
			player.getPet().doRevive(_power);
		BotConfig reviverBotConfig = BotEngine.getInstance().getBotConfig(_reviver);
		reviverBotConfig.getIsUsedReviveOwner_target().put(_playerRef.get().getObjectId(),true);
	}

	@Override
	public void sayNo()
	{
		BotConfig reviverBotConfig = BotEngine.getInstance().getBotConfig(_reviver);
		reviverBotConfig.getIsUsedReviveOwner_target().put(_playerRef.get().getObjectId(),true);
	}

	public double getPower()
	{
		return _power;
	}

	public boolean isForPet()
	{
		return _forPet;
	}
}
