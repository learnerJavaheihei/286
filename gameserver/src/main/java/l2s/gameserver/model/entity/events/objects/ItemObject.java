package l2s.gameserver.model.entity.events.objects;

/**
 * @author Bonux
**/
public class ItemObject
{
	private final int _itemId;
	private final long _count;

	public ItemObject(int itemId, long count)
	{
		_itemId = itemId;
		_count = count;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public long getCount()
	{
		return _count;
	}
}
