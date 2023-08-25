package l2s.gameserver.model.entity.residence;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.math.SafeMath;
import l2s.gameserver.Announcements;
import l2s.gameserver.dao.CastleDAO;
import l2s.gameserver.dao.CastleHiredGuardDAO;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExCastleState;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.support.MerchantGuard;
import l2s.gameserver.utils.Log;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Castle extends Residence
{
	private static final Logger _log = LoggerFactory.getLogger(Castle.class);

	private final IntObjectMap<MerchantGuard> _merchantGuards = new HashIntObjectMap<MerchantGuard>();

	private int _tax;
	private long _treasury;
	private long _collectedShops;

	private final NpcString _npcStringName;

	private ResidenceSide _residenceSide = ResidenceSide.NEUTRAL;

	private static final SkillEntry LIGHT_SIDE_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 19032, 1); // Способности Света
	private static final SkillEntry DARK_SIDE_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 19033, 1); // Способности Тьмы

	private Set<ItemInstance> _spawnMerchantTickets = new CopyOnWriteArraySet<ItemInstance>();

	public Castle(StatsSet set)
	{
		super(set);
		_npcStringName = NpcString.valueOf(1001000 + getId());
	}

	@Override
	public ResidenceType getType()
	{
		return ResidenceType.CASTLE;
	}

	// This method sets the castle owner; null here means give it back to NPC
	@Override
	public void changeOwner(Clan newOwner)
	{
		// Если клан уже владел каким-либо замком/крепостью, отбираем его.
		if(newOwner != null)
		{
			if(newOwner.getCastle() != 0)
			{
				Castle oldCastle = ResidenceHolder.getInstance().getResidence(Castle.class, newOwner.getCastle());
				if(oldCastle != null)
					oldCastle.changeOwner(null);
			}
		}

		Clan oldOwner = null;
		// Если этим замком уже кто-то владел, отбираем у него замок
		if(getOwnerId() > 0 && (newOwner == null || newOwner.getClanId() != getOwnerId()))
		{
			// Удаляем замковые скилы у старого владельца
			removeSkills();

			cancelCycleTask();

			oldOwner = getOwner();
			if(oldOwner != null)
			{
				// Переносим сокровищницу в вархауз старого владельца
				long amount = getTreasury();
				if(amount > 0)
				{
					Warehouse warehouse = oldOwner.getWarehouse();
					if(warehouse != null)
					{
						warehouse.addItem(ItemTemplate.ITEM_ID_ADENA, amount);
						addToTreasuryNoTax(-amount, false);
						Log.add(getName() + "|" + -amount + "|Castle:changeOwner", "treasury");
					}
				}

				// Проверяем членов старого клана владельца, снимаем короны замков и корону лорда с лидера
				for(Player clanMember : oldOwner.getOnlineMembers())
					if(clanMember != null && clanMember.getInventory() != null)
						clanMember.getInventory().validateItems();

				// Отнимаем замок у старого владельца
				oldOwner.setHasCastle(0);
			}
		}

		setOwner(newOwner);

		removeFunctions();

		// Выдаем замок новому владельцу
		if(newOwner != null)
		{
			newOwner.setHasCastle(getId());
			newOwner.broadcastClanStatus(true, false, false);
		}

		// Выдаем замковые скилы новому владельцу
		rewardSkills();

		setJdbcState(JdbcEntityState.UPDATED);
		update();
	}

	// This method loads castle
	@Override
	protected void loadData()
	{
		_treasury = 0;
		_tax = 5;

		CastleDAO.getInstance().select(this);
		CastleHiredGuardDAO.getInstance().load(this);
	}
	
	public void setTaxPercent(int tax)
	{
		_tax = tax;
		setJdbcState(JdbcEntityState.UPDATED);
		update();
	}

	public void setTreasury(long t)
	{
		_treasury = t;
	}

	public long getCollectedShops()
	{
		return _collectedShops;
	}

	public void setCollectedShops(long value)
	{
		_collectedShops = value;
	}

	// This method add to the treasury
	/** Add amount to castle instance's treasury (warehouse). */
	public void addToTreasury(long amount, boolean shop)
	{
		if(getOwnerId() <= 0)
			return;

		if(amount == 0)
			return;

		// TODO [Bonux]: Вынести в датапак.
		double deleteAmount = 0.4;
		if(getId() == 3) // Giran
			deleteAmount = 0.75;
		else if(getId() == 6) // Innadril
			deleteAmount = 0.;

		amount = (long) Math.max(0, amount - (amount * deleteAmount));

		if(amount > 1 && getId() != 5 && getId() != 8) // If current castle instance is not Aden or Rune
		{
			Castle royal = ResidenceHolder.getInstance().getResidence(Castle.class, getId() >= 7 ? 8 : 5);
			if(royal != null)
			{
				// TODO [Bonux]: Вынести в датапак.
				double royalTaxRate = 0.25;
				if(getId() == 3) // Giran
					royalTaxRate = 0.50;

				long royalTax = (long) (amount * royalTaxRate);

				if(royal.getOwnerId() > 0)
				{
					royal.addToTreasury(royalTax, shop); // Only bother to really add the tax to the treasury if not npc owned
					if(getId() == 5)
						Log.add("Aden|" + royalTax + "|Castle:adenTax", "treasury");
					else if(getId() == 8)
						Log.add("Rune|" + royalTax + "|Castle:runeTax", "treasury");
				}

				amount -= royalTax; // Subtract royal castle income from current castle instance's income
			}
		}

		addToTreasuryNoTax(amount, shop);
	}

	/** Add amount to castle instance's treasury (warehouse), no tax paying. */
	public void addToTreasuryNoTax(long amount, boolean shop)
	{
		if(getOwnerId() <= 0)
			return;

		if(amount == 0)
			return;

		// Add to the current treasury total.  Use "-" to substract from treasury
		_treasury = SafeMath.addAndLimit(_treasury, amount);

		if(shop)
			_collectedShops += amount;

		setJdbcState(JdbcEntityState.UPDATED);
		update();
	}

	public int getTaxPercent()
	{
		return _tax;
	}

	public double getTaxRate()
	{
		return getTaxPercent() / 100.;
	}

	public long getTreasury()
	{
		return _treasury;
	}

	@Override
	public void update()
	{
		CastleDAO.getInstance().update(this);
	}

	public NpcString getNpcStringName()
	{
		return _npcStringName;
	}

	public void addMerchantGuard(MerchantGuard merchantGuard)
	{
		_merchantGuards.put(merchantGuard.getItemId(), merchantGuard);
	}

	public MerchantGuard getMerchantGuard(int itemId)
	{
		return _merchantGuards.get(itemId);
	}

	public IntObjectMap<MerchantGuard> getMerchantGuards()
	{
		return _merchantGuards;
	}

	public Set<ItemInstance> getSpawnMerchantTickets()
	{
		return _spawnMerchantTickets;
	}

	@Override
	public void startCycleTask()
	{}

	@Override
	public void setResidenceSide(ResidenceSide side, boolean onRestore)
	{
		if(!onRestore && _residenceSide == side)
			return;

		_residenceSide = side;

		removeSkills();
		switch(_residenceSide)
		{
			case LIGHT:
				removeSkill(DARK_SIDE_SKILL);
				addSkill(LIGHT_SIDE_SKILL);
				break;
			case DARK:
				removeSkill(LIGHT_SIDE_SKILL);
				addSkill(DARK_SIDE_SKILL);
				break;
			default:
				removeSkill(LIGHT_SIDE_SKILL);
				removeSkill(DARK_SIDE_SKILL);
				break;
		}
		rewardSkills();

		if(!onRestore)
		{
			setJdbcState(JdbcEntityState.UPDATED);
			update();

			CastleSiegeEvent siege = getSiegeEvent();
			if(siege != null)
				siege.actActions("change_castle_side");
		}
	}

	@Override
	public ResidenceSide getResidenceSide()
	{
		return _residenceSide;
	}

	@Override
	public void broadcastResidenceState()
	{
		Announcements.announceToAll(new ExCastleState(this));
	}
}