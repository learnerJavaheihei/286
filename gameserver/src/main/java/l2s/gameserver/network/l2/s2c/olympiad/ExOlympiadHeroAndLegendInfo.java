package l2s.gameserver.network.l2.s2c.olympiad;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.StatsSet;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.pair.IntObjectPair;

/**
 * @author nexvill
 */
public class ExOlympiadHeroAndLegendInfo extends L2GameServerPacket
{
	private final IntObjectMap<StatsSet> heroes;

	public ExOlympiadHeroAndLegendInfo()
	{
		heroes = Hero.getInstance().getHeroes();
	}

	@Override
	protected void writeImpl()
	{
		writeC(78); // unk, 78 on JP
		writeC(0); // unk 0
		writeString(""); // legend name
		writeString(""); // clan name
		writeD(Config.REQUEST_ID); // server id
		writeD(0); // race
		writeD(0); // sex
		writeD(0); // class id
		writeD(0); // level
		writeD(0); // legend times
		writeD(0); // wins
		writeD(0); // loses
		writeD(0); // points
		writeD(0); // clan level
		
		
		writeD(Hero.getInstance().getHeroes().size());
		for (IntObjectPair<StatsSet> pair : heroes.entrySet())
		{
			StatsSet hero = pair.getValue();
			
			int sex = 0;
			int level = 0;
			int wins = 0;
			int loses = 0;
			int points = 0;
			for (int id : RankManager.getInstance().getPreviousOlyList().keySet())
			{
				StatsSet player = new StatsSet();
				player = RankManager.getInstance().getPreviousOlyList().get(id);
				if (pair.getKey() == player.getInteger("objId"))
				{
					sex = player.getInteger("sex");
					level = player.getInteger("level");
					wins = player.getInteger("competitions_win");
					loses = player.getInteger("competitions_lost");
					points = player.getInteger("olympiad_points");
					break;
				}
			}
			
			
			writeString(hero.getString(Hero.CHAR_NAME));
			writeString(hero.getString(Hero.CLAN_NAME));
			writeD(Config.REQUEST_ID);
			ClassId classId = ClassId.valueOf(hero.getInteger(Hero.CLASS_ID));
			writeD(classId.getRace().ordinal());
			writeD(sex); // sex
			writeD(classId.getId());
			writeD(level); // level
			writeD(hero.getInteger(Hero.COUNT));
			writeD(wins);
			writeD(loses);
			writeD(points);
			
			Clan clan = ClanTable.getInstance().getClanByCharId(pair.getKey());
			if (clan != null)
			{
				writeD(clan.getLevel());
			} else
			{
				writeD(0);
			}
		}
	}
}
