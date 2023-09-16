package services;

import l2s.gameserver.Announcements;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ResidenceSide;
import l2s.gameserver.model.pledge.ClanWar;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Bonux
**/
public class PvPAnnounce implements OnInitScriptListener
{
	private static class KillInfo
	{
		public int kills;
		public long lastKillTime;
	}

	private static final int MAX_PVP_KILL_DELAY = 60 * 60 * 1000; // 60 min
	private static final int MAX_PK_KILL_DELAY = 60 * 60 * 1000; // 60 min

	private final IntObjectMap<IntObjectMap<KillInfo>> _pvpKills = new CHashIntObjectMap<IntObjectMap<KillInfo>>();
	private final IntObjectMap<IntObjectMap<KillInfo>> _pkKills = new CHashIntObjectMap<IntObjectMap<KillInfo>>();

	private class KillListener implements OnKillListener
	{
		@Override
		public void onKill(Creature actor, Creature victim)
		{
			Player killer = actor.getPlayer();
			if(killer == null)
				return;

			Player target = victim.getPlayer();
			if(target == null)
				return;

			boolean war = target.atMutualWarWith(killer);

			//TODO [VISTALL] fix it
			if(war /*|| target.getClan().getSiege() != null && target.getClan().getSiege() == killer.getClan().getSiege() && (target.getClan().isDefender() && killer.getClan().isAttacker() || target.getClan().isAttacker() && killer.getClan().isDefender())*/)
			{
				ClanWar clanWar = target.getClan().getWarWith(killer.getClan().getClanId());
				if(clanWar != null)
					clanWar.onKill(killer, target);
			}

			if(target.isInSiegeZone())
				return;

			Castle castle = target.getCastle();
			if(target.getPvpFlag() > 0 || war || castle != null && castle.getResidenceSide() == ResidenceSide.DARK)
			{
				IntObjectMap<KillInfo> killerMap = _pvpKills.get(killer.getObjectId());
				if(killerMap == null)
				{
					killerMap = new CHashIntObjectMap<KillInfo>();
					_pvpKills.put(killer.getObjectId(), killerMap);
				}

				KillInfo killInfo = killerMap.get(target.getObjectId());
				if(killInfo == null)
				{
					killInfo = new KillInfo();
					killerMap.put(target.getObjectId(), killInfo);
				}

				if((killInfo.lastKillTime + MAX_PVP_KILL_DELAY) < System.currentTimeMillis())
					killInfo.kills = 0;

				killInfo.kills++;
				killInfo.lastKillTime = System.currentTimeMillis();

				int msgid = 0;
				switch(killInfo.kills)
				{
					/*case 1:
						Announcements.announceToAll("玉树临风的『" + killer.getName() + "』打败了『" + target.getName() + "』.");
						break;
					case 2:
						Announcements.announceToAll("牛气哄哄的『" + killer.getName() + "』打败了『" + target.getName() + "』完成了两连杀.");
						break;
					case 3:
						Announcements.announceToAll("勇冠天下的『" + killer.getName() + "』打败了『" + target.getName() + "』完成了三连杀.");
						break;
					default:
						Announcements.announceToAll("独孤求败的『" + killer.getName() + "』打败了『" + target.getName() + "』已经超神了.");
						break;*/
					case 1:
						msgid = 14003;
						break;
					case 2:
						msgid = 14004;
						break;
					case 3:
						msgid = 14005;
						break;
					default:
						msgid = 14006;
						break;
						// 信息范例：信息id	1	u,玉树临风的『$s1』打败了『$s1』,位置『$s3』。\0	0	0	FF	FF	FF	a,	a,	0	0	0	0	0	a,	a,none\0
				}
				Announcement(killer,target,msgid);
			}
			else
			{
				IntObjectMap<KillInfo> killerMap = _pkKills.get(killer.getObjectId());
				if(killerMap == null)
				{
					killerMap = new CHashIntObjectMap<KillInfo>();
					_pkKills.put(killer.getObjectId(), killerMap);
				}

				KillInfo killInfo = killerMap.get(target.getObjectId());
				if(killInfo == null)
				{
					killInfo = new KillInfo();
					killerMap.put(target.getObjectId(), killInfo);
				}

				if((killInfo.lastKillTime + MAX_PK_KILL_DELAY) < System.currentTimeMillis())
					killInfo.kills = 0;

				killInfo.kills++;
				killInfo.lastKillTime = System.currentTimeMillis();
				int msgid = 0;
				switch(killInfo.kills)
				{
					/*case 1:
						Announcements.announceToAll("玉树临风的『" + target.getName() + "』被『" + killer.getName()+ "』, 打的头破血流。");
						break;
					case 2:
						Announcements.announceToAll("垂头丧气的『" + target.getName() + "』被『" + killer.getName()+ "』, 打的满地找牙。");
						break;
					default:
						Announcements.announceToAll("倒霉透顶的『" + target.getName() + "』被『" + killer.getName()+ "』, 打的鼻青脸肿,变成猪头了。");
						break;*/
					case 1:
						msgid = 14007;
						break;
					case 2:
						msgid = 14008;
						break;
					default:
						msgid = 14009;
						break;
						// 信息范例：信息id	1	u,玉树临风的『$s1』被『$s2』, 打的头破血流,位置『$s3』。\0	0	0	FF	FF	FF	a,	a,	0	0	0	0	0	a,	a,none\0

				}
				Announcement(target,killer,msgid);
			}
		}

		@Override
		public boolean ignorePetOrSummon()
		{
			return true;
		}
	}

	public void Announcement(Player pl1, Player pl2, int mesid)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String dateString = sdf.format(new Date());
		Announcements.announceToAll(new SystemMessage(mesid).addString(dateString).addZoneName(pl1.getLoc()).addName(pl1).addName(pl2));
	}

	@Override
	public void onInit()
	{
		CharListenerList.addGlobal(new KillListener());
	}
}