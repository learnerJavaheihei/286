package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;

/**
 * Этот пакет отвечает за анимацию высасывания душ из трупов
 * @author SYS
 */
public class ExSpawnEmitterPacket extends L2GameServerPacket
{
	private int _monsterObjId;
	private int _playerObjId;
	private int _type;

	public ExSpawnEmitterPacket(Creature target, Player player, int type)
	{
		_playerObjId = player.getObjectId();
		_monsterObjId = target.getObjectId();
		_type = type;
	}

	@Override
	protected final void writeImpl()
	{
		//ddd
		writeD(_monsterObjId);
		writeD(_playerObjId);
		writeD(_type); //soul type
	}
}