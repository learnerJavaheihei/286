package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.pledge.Clan;

import java.util.HashMap;
import java.util.Map;

public class DiePacket extends L2GameServerPacket {
	private final int objectId;
	private final boolean hideDieAnimation;

	private int flags = 0;
	private boolean sweepable = false;
	private int blessingFeatherDelay = 0;

	public DiePacket(Creature cha, boolean hideDieAnimation) {
		objectId = cha.getObjectId();
		this.hideDieAnimation = hideDieAnimation;

		if (cha.isMonster())
			sweepable = ((MonsterInstance) cha).isSweepActive();
		else if (cha.isPlayer()) {
			Map<RestartType, Boolean> types = new HashMap<>(RestartType.VALUES.length);
			Player player = (Player) cha;
			types.put(RestartType.FIXED, player.canFixedRessurect());
			types.put(RestartType.AGATHION, player.isAgathionResAvailable());
			types.put(RestartType.TO_VILLAGE, true);
			types.put(RestartType.ADVENTURES_SONG, player.getAbnormalList().contains(22410) || player.getAbnormalList().contains(22411));

			for (Abnormal effect : player.getAbnormalList()) {
				if (effect.getSkill().getId() == 7008) {
					blessingFeatherDelay = effect.getTimeLeft();
					break;
				}
			}

			Clan clan = null;
			if (types.getOrDefault(RestartType.TO_VILLAGE, false))
				clan = player.getClan();

			if (clan != null) {
				types.put(RestartType.TO_CLANHALL, clan.getHasHideout() != 0);
				types.put(RestartType.TO_CASTLE, clan.getCastle() != 0);
				//types.put(RestartType.TO_FORTRESS, clan.getHasFortress() != 0);
			}

			for (Event e : cha.getEvents())
				e.checkRestartLocs(player, types);

			for (Map.Entry<RestartType, Boolean> entry : types.entrySet()) {
				if (entry.getValue())
					flags |= 1 << entry.getKey().ordinal();
			}
		}
	}

	public DiePacket(Creature cha) {
		this(cha, false);
	}

	@Override
	protected final void writeImpl() {
		writeD(objectId);
		writeD(flags); // 1 - to village, 2 - to CH, 4 - to castle, 8 - fortress, 16 - to Outpost, 32 - fixed
		writeD(0x00); // UNK 228
		writeD(sweepable); // Spoiled virgin
		writeD(blessingFeatherDelay); // Blessing feather delay
		writeD(hideDieAnimation ? 0x01 : 0x02); // Die animation
		writeD(0x00); // UNK 228
		writeD(0x00); // UNK 228

		int itemsCount = 0;
		writeD(itemsCount);
		for (int i = 0; i < itemsCount; i++)
			writeD(0x00); // item Id
	}
}