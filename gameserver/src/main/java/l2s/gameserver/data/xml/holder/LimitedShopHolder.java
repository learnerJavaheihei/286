package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.LimitedShopContainer;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.LimitedShopEntry;
import l2s.gameserver.model.base.LimitedShopProduction;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExPurchaseLimitShopItemListNew;
import l2s.gameserver.utils.Language;

/**
 * @author nexvill
 */
public class LimitedShopHolder extends AbstractHolder
{
	private static LimitedShopHolder _instance = new LimitedShopHolder();

	public static LimitedShopHolder getInstance()
	{
		return _instance;
	}

	private TIntObjectHashMap<LimitedShopContainer> _entries = new TIntObjectHashMap<LimitedShopContainer>();

	public LimitedShopContainer getList(int id)
	{
		return _entries.get(id);
	}

	public LimitedShopHolder()
	{
		//
	}

	public void addLimitedShopContainer(int id, LimitedShopContainer list)
	{
		if(_entries.containsKey(id))
			_log.warn("Limited Shop redefined: " + id);

		list.setListId(id);
		_entries.put(id, list);
	}

	public LimitedShopContainer remove(String s)
	{
		return remove(new File(s));
	}

	public LimitedShopContainer remove(File f)
	{
		return remove(Integer.parseInt(f.getName().replaceAll(".xml", "")));
	}

	public LimitedShopContainer remove(int id)
	{
		return _entries.remove(id);
	}

	public void SeparateAndSend(int listId, Player player)
	{
		LimitedShopContainer list = getList(listId);
		if(list == null)
		{
			player.sendMessage(new CustomMessage("common.Disabled"));
			return;
		}

		SeparateAndSend(list, player);
	}

	public void SeparateAndSend(LimitedShopContainer list, Player player)
	{
		list = generateLimitedShop(list, player);

		LimitedShopContainer temp = new LimitedShopContainer();

		temp.setListId(list.getListId());

		// Запоминаем отсылаемый лист, чтобы не подменили
		player.setLimitedShop(list);

		for(LimitedShopEntry e : list.getEntries())
		{
			LimitedShopProduction product = e.getProduction().get(0);
			if (((player.getLanguage() == Language.ENGLISH) || (player.getLanguage() == Language.ENGLISH_EU)) && (product.getInfo().getInteger("language") == 0))
				temp.addEntry(e);
			else if ((player.getLanguage() == Language.RUSSIAN) && (product.getInfo().getInteger("language") == 1))
				temp.addEntry(e);
			else if ((player.getLanguage() == Language.CHINESE_TW) && (product.getInfo().getInteger("language") == 2))
				temp.addEntry(e);
		}

		player.sendPacket(new ExPurchaseLimitShopItemListNew(player, temp));
	}

	private LimitedShopContainer generateLimitedShop(LimitedShopContainer container, Player player)
	{
		LimitedShopContainer list = new LimitedShopContainer();
		list.setListId(container.getListId());

		for(LimitedShopEntry origEntry : container.getEntries())
		{
			LimitedShopEntry ent = origEntry.clone();

				list.addEntry(ent);
		}

		return list;
	}

	@Override
	public int size()
	{
		return _entries.size();
	}

	@Override
	public void clear()
	{
		_entries.clear();
	}
}
