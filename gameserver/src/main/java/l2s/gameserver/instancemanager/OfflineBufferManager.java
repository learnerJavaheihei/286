package l2s.gameserver.instancemanager;

import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.htm.HtmTemplates;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillTargetType;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.TradeHelper;
import l2s.gameserver.utils.Util;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager para manejar todas las funciones del offline buffer
 * Creacion de los schemes de venta, chequeos, precios, consumos, buffeos, htmls, etc
 *
 * @author Prims
 */
//TODO[BONUX]: Навести тут порядок:
//1. Вынести сообщения в датапак и добавить мультиязычноть.
public class OfflineBufferManager
{
	public static class BufferData
	{
		private final Player _owner;
		private final String _saleTitle;
		private final long _buffPrice;
		private final IntObjectMap<SkillEntry> _buffs = new HashIntObjectMap<SkillEntry>();

		public BufferData(Player player, String title, long price, List<SkillEntry> buffs)
		{
			_owner = player;
			_saleTitle = title;
			_buffPrice = price;
			if(buffs != null)
			{
				for(SkillEntry buff : buffs)
					_buffs.put(buff.getId(), buff);
			}
		}

		public Player getOwner()
		{
			return _owner;
		}

		public String getSaleTitle()
		{
			return _saleTitle;
		}

		public long getBuffPrice()
		{
			return _buffPrice;
		}

		public IntObjectMap<SkillEntry> getBuffs()
		{
			return _buffs;
		}
	}

	private static final OfflineBufferManager _instance = new OfflineBufferManager();

	public static final OfflineBufferManager getInstance()
	{
		return _instance;
	}

	private static final Logger _log = LoggerFactory.getLogger(OfflineBufferManager.class);

	private static final Set<SkillTargetType> SUITABLE_TARGET_TYPES = new HashSet<SkillTargetType>();
	static
	{
		SUITABLE_TARGET_TYPES.add(SkillTargetType.TARGET_ONE);
		SUITABLE_TARGET_TYPES.add(SkillTargetType.TARGET_PARTY);
		SUITABLE_TARGET_TYPES.add(SkillTargetType.TARGET_PARTY_WITHOUT_ME);
		SUITABLE_TARGET_TYPES.add(SkillTargetType.TARGET_PARTY_ONE);
		SUITABLE_TARGET_TYPES.add(SkillTargetType.TARGET_PARTY_ONE_WITHOUT_ME);
		SUITABLE_TARGET_TYPES.add(SkillTargetType.TARGET_COMMCHANNEL);
		SUITABLE_TARGET_TYPES.add(SkillTargetType.TARGET_CLAN);
		SUITABLE_TARGET_TYPES.add(SkillTargetType.TARGET_CLAN_ONE);
		SUITABLE_TARGET_TYPES.add(SkillTargetType.TARGET_CLAN_ONLY);
		SUITABLE_TARGET_TYPES.add(SkillTargetType.TARGET_ALLY);
	}

	private static final Set<SkillTargetType> IGNORE_TARGET_CONDITIONS_TARGET_TYPES = new HashSet<SkillTargetType>();
	static
	{
		IGNORE_TARGET_CONDITIONS_TARGET_TYPES.add(SkillTargetType.TARGET_PARTY);
		IGNORE_TARGET_CONDITIONS_TARGET_TYPES.add(SkillTargetType.TARGET_PARTY_WITHOUT_ME);
		IGNORE_TARGET_CONDITIONS_TARGET_TYPES.add(SkillTargetType.TARGET_PARTY_ONE);
		IGNORE_TARGET_CONDITIONS_TARGET_TYPES.add(SkillTargetType.TARGET_PARTY_ONE_WITHOUT_ME);
		IGNORE_TARGET_CONDITIONS_TARGET_TYPES.add(SkillTargetType.TARGET_COMMCHANNEL);
		IGNORE_TARGET_CONDITIONS_TARGET_TYPES.add(SkillTargetType.TARGET_CLAN);
	}

	private final IntObjectMap<BufferData> _buffStores = new CHashIntObjectMap<BufferData>();

	/**
	 * @return Devuelve todas las stores de buffs
	 */
	public IntObjectMap<BufferData> getBuffStores()
	{
		return _buffStores;
	}

	public BufferData getBuffStore(int id)
	{
		return _buffStores.get(id);
	}

	/**
	 * Procesa el bypass para este sistema y muestra el html que corresponda
	 *
	 * @param player
	 * @param command
	 */
	public void processBypass(Player player, String command)
	{
		final StringTokenizer st = new StringTokenizer(command, "_");
		String cmd = st.nextToken();
		// Sets a new buff store
		if(cmd.equalsIgnoreCase("create"))
		{
			if(!st.hasMoreTokens())
			{
				final HtmlMessage html = new HtmlMessage(0);
				html.setFile("command/buffstore/buff_store_create.htm");
				player.sendPacket(html);
				return;
			}

			long writedPrice;
			try {
				writedPrice = Long.parseLong(st.nextToken().trim());
			} catch (NumberFormatException e) {
				final HtmlMessage html = new HtmlMessage(0);
				html.setFile("command/buffstore/buff_store_create.htm");
				player.sendPacket(html);
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.create.10"));
				return;
			}

			// Check if the player already has an active store, just in case
			if(_buffStores.containsKey(player.getObjectId()))
				return;

			// Check for store
			if(player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.create.1"));
				return;
			}

			// Check if the player can set a store
			if(!Config.BUFF_STORE_ALLOWED_CLASS_LIST.contains(player.getClassId().getId()))
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.create.2"));
				return;
			}

			// Check all the conditions to see if the player can open a private store
			if(!TradeHelper.checksIfCanOpenStore(player, Player.STORE_PRIVATE_BUFF))
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.create.3"));
				return;
			}

			final long price = Math.max(Config.BUFF_STORE_MIN_PRICE, Math.min(Config.BUFF_STORE_MAX_PRICE, writedPrice));
			String title = !st.hasMoreTokens() ? "" : st.nextToken();
			if(!title.isEmpty() && title.charAt(0) == ' ')
				title = title.substring(1);
			while(st.hasMoreTokens())
				title += "_" + st.nextToken();
			title = title.trim();

			// Check the title
			if(title.isEmpty() && !Util.isMatchingRegexp(title, Config.CLAN_TITLE_TEMPLATE))
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.create.4"));
				return;
			}

			// Check price limits
			if(price < 1)
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.create.5"));
				return;
			}

			// Buff Stores can only be put inside areas designated to it and in clan halls
			final ClanHall ch = ResidenceHolder.getInstance().getResidenceByObject(ClanHall.class, player);
			final Zone chZone = player.getZone(ZoneType.RESIDENCE);
			if(!player.isGM() && !player.isInZone(ZoneType.buff_store) && (ch == null || chZone == null || ch != chZone.getParams().get("residence")))
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.create.6"));
				return;
			}

			// Check for conditions
			if(player.isAlikeDead() || player.isInOlympiadMode() || player.isMounted() || player.isCastingNow() || player.getObservableArena() != null || player.getOlympiadGame() != null  || Olympiad.isRegisteredInComp(player))
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.create.7"));
				return;
			}

			final BufferData buffer = new BufferData(player, title, price, null);

			// Add all the buffs
			for(SkillEntry skillEntry : player.getAllSkills())
			{
				Skill skill = skillEntry.getTemplate();
				// Only active skills
				if(!skill.isActive())
					continue;

				if(skill.isDebuff())
					continue;

				// Only buffs
				if(skill.getSkillType() != SkillType.BUFF)
					continue;

				// Allowed skill list
				if(!Config.BUFF_STORE_ALLOWED_SKILL_LIST.contains(skillEntry.getId()))
					continue;

				if(!SUITABLE_TARGET_TYPES.contains(skill.getTargetType()))
					continue;

				buffer.getBuffs().put(skillEntry.getId(), skillEntry);
			}

			// Case of empty buff list
			if(buffer.getBuffs().isEmpty())
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.create.8"));
				return;
			}

			// Add the buffer data to the array
			_buffStores.put(player.getObjectId(), buffer);

			// Sit the player, put it on store and and change the colors and titles
			player.sitDown(null);

			player.setPrivateStoreType(Player.STORE_PRIVATE_BUFF);
			player.broadcastUserInfo(true);

			player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.create.9"));
		}
		// Stops the current store
		else if(cmd.equalsIgnoreCase("stopstore"))
		{
			if(player.getPrivateStoreType() != Player.STORE_PRIVATE_BUFF)
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.stopstore.1"));
				return;
			}

			// Remove the buffer from the array
			_buffStores.remove(player.getObjectId());

			// Stand the player and put the original colors and title back
			player.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			player.standUp();

			player.broadcastUserInfo(true);

			player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.stopstore.2"));
		}
		// Shows the buff list of the selected buffer
		else if(cmd.equalsIgnoreCase("bufflist"))
		{
			final int playerId = Integer.parseInt(st.nextToken());
			final int page = (st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0);

			// Check if the buffer exists
			final BufferData buffer = _buffStores.get(playerId);
			if(buffer == null)
				return;

			// Check if the player is in the right distance from the buffer
			if(!player.checkInteractionDistance(buffer.getOwner()))
				return;

			showStoreWindow(player, buffer, page);
		}
		// Purchases a particular buff of the store
		else if(cmd.equalsIgnoreCase("purchasebuff"))
		{
			final int playerId = Integer.parseInt(st.nextToken());
			final boolean self = (st.hasMoreTokens() ? st.nextToken().equalsIgnoreCase("self") : true);
			final int buffId = Integer.parseInt(st.nextToken());
			final int page = (st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0);

			// Check if the buffer exists
			final BufferData buffer = _buffStores.get(playerId);
			if(buffer == null)
				return;

			// Check if the buffer has this buff
			if(!buffer.getBuffs().containsKey(buffId))
				return;

			Player caster = buffer.getOwner();

			// Check if the player is in the right distance from the buffer
			if(!player.checkInteractionDistance(caster))
				return;

			// Check if the player has a summon before buffing
			if(!self && !player.hasServitor())
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.purchasebuff.1"));

				// Send window again
				showStoreWindow(player, buffer, page);
				return;
			}

			// Check buffing conditions
			if(player.getPvpFlag() > 0 || player.isInCombat() || player.getKarma() > 0 || player.isAlikeDead()
				|| player.isInJail() || player.isInOlympiadMode()
				|| player.isInStoreMode() || player.isInTrade() || player.getEnchantScroll() != null || player.isFishing() || player.isInTrainingCamp())
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.purchasebuff.2"));
				return;
			}

			
			final SkillEntry skillEntry = buffer.getBuffs().get(buffId);
			if(skillEntry == null)
				return;

			final Skill skill = skillEntry.getTemplate();

			final double buffMpCost = (Config.BUFF_STORE_MP_ENABLED ? skill.getMpConsume() * Config.BUFF_STORE_MP_CONSUME_MULTIPLIER : 0);

			// Check if the buffer has enough mp to sell this buff
			if(buffMpCost > 0 && caster.getCurrentMp() < buffMpCost)
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.purchasebuff.3"));

				// Send window again
				showStoreWindow(player, buffer, page);
				return;
			}

			if(skill.getReferenceItemId() > 0)
			{
				if(!caster.consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				{
					player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.purchasebuff.4"));

					// Send window again
					showStoreWindow(player, buffer, page);
					return;
				}
			}

			if(!skill.checkCondition(skillEntry, caster, player, false, false, false, false, false))
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.purchasebuff.2"));

				// Send window again
				showStoreWindow(player, buffer, page);
				return;
			}

			if(!IGNORE_TARGET_CONDITIONS_TARGET_TYPES.contains(skill.getTargetType()) && !skill.getTargets(skillEntry, caster, player, false).contains(player))
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.purchasebuff.2"));

				// Send window again
				showStoreWindow(player, buffer, page);
				return;
			}

			if(Config.BUFF_STORE_ITEM_CONSUME_ENABLED)
			{
				if(!skill.isHandler() && skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0)
				{
					if(ItemFunctions.getItemCount(caster, skill.getItemConsumeId()) < skill.getItemConsume())
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.purchasebuff.5"));

						// Send window again
						showStoreWindow(player, buffer, page);
						return;
					}
				}
			}

			// Clan Members of the buffer dont have to pay anything
			long buffPrice = player.getClanId() == caster.getClanId() && player.getClanId() != 0 ? 0 : buffer.getBuffPrice();
			if (player.getClan()!=null && caster.getClan()!=null && caster.getAlliance() != null && player.getAlliance() != null) {
				if(player.getAlliance().isMember(caster.getClanId())){
					buffPrice = 1;
				}
			}


			// Check if the player has enough adena to purchase this buff
			if(buffPrice > 0 && (player.getAdena() < buffPrice || !player.reduceAdena(buffPrice, true)))
			{
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.purchasebuff.6"));
				return;
			}

			// Give the adena to the buffer
			if(buffPrice > 0)
				caster.addAdena(buffPrice, true);

			// Reduce the buffer's mp if it consumes something
			if(buffMpCost > 0)
				caster.reduceCurrentMp(buffMpCost, null);

			// Send message
			player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.OfflineBufferManager.purchasebuff.7").addString(skill.getName(player)).addString(caster.getName()));

			// Give the target the buff
			if(self)
				skill.getEffects(player, player);
			else
			{
				for(Servitor servitor : player.getServitors())
					skill.getEffects(servitor, servitor);
			}

			// Send the buff list again after buffing, exactly where it was before
			showStoreWindow(player, buffer, page);
		}
	}

	/**
	 * Sends the to the player the buffer store window with all the buffs and info
	 *
	 * @param player
	 * @param buffer
	 * @param page
	 */
	private void showStoreWindow(Player player, BufferData buffer, int page)
	{
		HtmTemplates tpls = HtmCache.getInstance().getTemplates("command/buffstore/buff_store_buffer.htm", player);

		String html = tpls.get(0);

		final int MAX_ENTRANCES_PER_ROW = 6; // buffs per page
		final double entrancesSize = buffer.getBuffs().size();
		final int maxPage = (int)Math.ceil(entrancesSize / MAX_ENTRANCES_PER_ROW) - 1;
		final int currentPage = Math.min(maxPage, page);

		// Creamos la lista de buffs
		final StringBuilder buffList = new StringBuilder();
		final Iterator<SkillEntry> it = buffer.getBuffs().valueCollection().iterator();
		SkillEntry buff;
		int i = 0;
		boolean changeColor = false;

		while(it.hasNext())
		{
			// Solo mostramos los buffs que sean de esta pagina
			if(i < currentPage * MAX_ENTRANCES_PER_ROW)
			{
				it.next();
				i++;
				continue;
			}

			// Si llegamos al final de la pagina salimos
			if(i >= (currentPage * MAX_ENTRANCES_PER_ROW + MAX_ENTRANCES_PER_ROW))
				break;

			buff = it.next();

			String buffBlock = tpls.get(1);
			buffBlock = buffBlock.replace("<?pet_buff_button?>", player.hasServitor() ? tpls.get(4) : "");
			buffBlock = buffBlock.replace("<?bgcolor?>", changeColor ? "171612" : "23221e");
			buffBlock = buffBlock.replace("<?buff_icon?>", buff.getTemplate().getIcon());
			buffBlock = buffBlock.replace("<?buff_id?>", String.valueOf(buff.getId()));
			buffBlock = buffBlock.replace("<?buff_name?>", buff.getName(player));
			buffBlock = buffBlock.replace("<?buff_level?>", String.valueOf(buff.getLevel()));

			buffList.append(buffBlock);

			i++;
			changeColor = !changeColor;
		}

		// Make the arrows buttons
		String previousPageButton = "";
		if(currentPage > 0)
		{
			previousPageButton = tpls.get(2);
			previousPageButton = previousPageButton.replace("<?page_id?>", String.valueOf(currentPage - 1));
		}

		String nextPageButton = "";
		if(currentPage < maxPage)
		{
			nextPageButton = tpls.get(3);
			nextPageButton = nextPageButton.replace("<?page_id?>", String.valueOf(currentPage + 1));
		}

		html = html.replace("<?buffs_list?>", buffList.toString());
		html = html.replace("<?previous_page_button?>", previousPageButton);
		html = html.replace("<?next_page_button?>", nextPageButton);
		html = html.replace("<?buffer_object_id?>", String.valueOf(buffer.getOwner().getObjectId()));
		html = html.replace("<?buffer_class?>", buffer.getOwner().getClassId().getName(buffer.getOwner()));
		html = html.replace("<?buffer_level?>", (String.valueOf(buffer.getOwner().getLevel() >= 76 && buffer.getOwner().getLevel() < 80 ? 76 : (buffer.getOwner().getLevel() >= 84 ? 84 : Math.round(buffer.getOwner().getLevel() / 10) * 10))));
		html = html.replace("<?buffer_name?>", buffer.getOwner().getName());
		html = html.replace("<?buffer_mp?>", String.valueOf((int) buffer.getOwner().getCurrentMp()));
		html = html.replace("<?buff_price?>", Util.formatAdena(buffer.getBuffPrice()));
		html = html.replace("<?page?>", String.valueOf(currentPage));
		html = html.replace("<?pages_count?>", (currentPage + 1) + "/" + (maxPage + 1));

		Functions.show(html, player);
	}

	public synchronized void storeBufferData(Player trader)
	{
		BufferData buffer = getBuffStore(trader.getObjectId());
		if(buffer != null)
		{
			trader.setVar("offlinebuff_price", buffer.getBuffPrice());
			trader.setVar("offlinebuff_skills", joinAllSkillsToString(buffer.getBuffs().valueCollection()));
			trader.setVar("offlinebuff_title", buffer.getSaleTitle());
		}
	}

	private static String joinAllSkillsToString(Collection<SkillEntry> skills)
	{
		if(skills.isEmpty())
			return "";

		String result = "";
		for(SkillEntry val : skills)
			result += val.getId() + ",";

		return result.substring(0, result.length() - 1);
	}
}
