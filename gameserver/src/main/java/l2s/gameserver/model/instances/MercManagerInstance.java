package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.HtmlUtils;

/**
 * @author Bonux
**/
public final class MercManagerInstance extends MerchantInstance
{
	private static int COND_ALL_FALSE = 0;
	private static int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static int COND_OWNER = 2;

	public MercManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == 0)
		{
			showMainChatWindow(player, false);
		}
		else if(ask == -201)
		{
			int condition = validateCondition(player);
			if(condition == COND_OWNER)
			{
				if(reply >= 1 && reply <= 6)
					showShopWindow(player, (int) reply, false);
			}
		}
		else if(ask == -202)
		{
			int castleId = getCastle().getId();

			String prefix = "";
			if(castleId == 5)
				prefix = "aden_";
			else if(castleId == 8)
				prefix = "rune_";

			showChatWindow(player, "residence2/castle/" + prefix + "msellerlimit.htm", false, "<?feud_name?>", HtmlUtils.htmlNpcString(1001000 + castleId));
		}
	}

	@Override
	public void showMainChatWindow(Player player, boolean firstTalk, Object... replace)
	{
		String filename = "residence2/castle/mseller002.htm";
		int condition = validateCondition(player);
		if(condition == COND_BUSY_BECAUSE_OF_SIEGE)
			filename = "residence2/castle/mseller003.htm"; // Busy because of siege
		else if(condition == COND_OWNER)
			filename = "residence2/castle/mseller001.htm";
		showChatWindow(player, filename, firstTalk);
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return "residence2/castle/";
	}

	private int validateCondition(Player player)
	{
		if(player.isGM())
			return COND_OWNER;

		if(getCastle() != null && getCastle().getId() != 0)
		{
			if(player.getClan() != null)
			{
				if(getCastle().getSiegeEvent().isInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				else if(getCastle().getOwnerId() == player.getClanId() // Clan owns castle
						&& (player.getClanPrivileges() & Clan.CP_CS_MERCENARIES) == Clan.CP_CS_MERCENARIES) // has merc rights
					return COND_OWNER; // Owner
			}
		}
		return COND_ALL_FALSE;
	}
}