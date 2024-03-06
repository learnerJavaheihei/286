package l2s.gameserver.network.l2.c2s;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.FakePlayer;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingManager;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author nexvill
 */
public class RequestExRankingCharSpawnBuffzoneNpc extends L2GameClientPacket
{
	private ScheduledFuture<?> _taskBuff, _taskClear;
	private NpcInstance _hiddenNpc;
	private FakePlayer fp;

	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		PlayerRankingManager.getInstance().spawnRankersAuthority(activeChar);
		int delay = (int) TimeUnit.MILLISECONDS.toSeconds(Math.max(0, activeChar.getVarExpireTime(PlayerRankingManager.RANKER_AUTHORITY_REUSE_VAR) - System.currentTimeMillis()));
		activeChar.sendPacket(new ExRankingCharBuffzoneNpcInfo(delay));
	}
}