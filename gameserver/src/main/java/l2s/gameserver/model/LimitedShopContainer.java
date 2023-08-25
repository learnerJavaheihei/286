package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.base.LimitedShopEntry;

/**
 * @author nexvill
 */
public class LimitedShopContainer
{
	private int _listId;
	private List<LimitedShopEntry> entries = new ArrayList<LimitedShopEntry>();

	public void setListId(int listId)
	{
		_listId = listId;
	}

	public int getListId()
	{
		return _listId;
	}

	public void addEntry(LimitedShopEntry e)
	{
		entries.add(e);
	}

	public List<LimitedShopEntry> getEntries()
	{
		return entries;
	}

	public boolean isEmpty()
	{
		return entries.isEmpty();
	}
}
