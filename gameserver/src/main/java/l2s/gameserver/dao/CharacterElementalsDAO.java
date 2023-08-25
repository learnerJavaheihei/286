package l2s.gameserver.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.model.actor.instances.player.Elemental;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
 */
public class CharacterElementalsDAO
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterElementalsDAO.class);

	private static final CharacterElementalsDAO _instance = new CharacterElementalsDAO();

	public static CharacterElementalsDAO getInstance()
	{
		return _instance;
	}

	public void restore(Player owner, Map<ElementalElement, Elemental> map)
	{
		map.clear();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM character_elementals WHERE char_obj_id = ? AND class_index = ?");
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, owner.getActiveClassId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				Elemental elemental = new Elemental(ElementalElement.getElementById(rset.getInt("element_id")));
				elemental.setEvolutionLevel(rset.getInt("evolution_level"));
				elemental.setExp(rset.getLong("exp"));
				elemental.setAttackPoints(rset.getInt("attack_points"));
				elemental.setDefencePoints(rset.getInt("defence_points"));
				elemental.setCritRatePoints(rset.getInt("crit_rate_points"));
				elemental.setCritAttackPoints(rset.getInt("crit_attack_points"));
				map.put(elemental.getElement(), elemental);
			}
		}
		catch(Exception e)
		{
			_log.error("CharacterElementalsDAO.select(Player): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public boolean store(Player owner, Collection<Elemental> elementals)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_elementals (char_obj_id,class_index,element_id,evolution_level,exp,attack_points,defence_points,crit_rate_points,crit_attack_points) VALUES(?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, owner.getActiveClassId());
			for(Elemental elemental : elementals)
			{
				statement.setInt(3, elemental.getElementId());
				statement.setInt(4, elemental.getEvolutionLevel());
				statement.setLong(5, elemental.getExp());
				statement.setInt(6, elemental.getAttackPoints());
				statement.setInt(7, elemental.getDefencePoints());
				statement.setInt(8, elemental.getCritRatePoints());
				statement.setInt(9, elemental.getCritAttackPoints());
				statement.addBatch();
			}
			statement.executeBatch();
		}
		catch(Exception e)
		{
			_log.warn(owner.getElementalList() + " could not store elementals list: ", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
}
