package l2s.gameserver.network.l2.s2c;

import java.util.*;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.templates.item.ItemTemplate;

public class PrivateStoreManageList extends L2GameServerPacket
{
	private final int _type;
	private final int _sellerId;
	private final long _adena;
	private final boolean _package;
	private final List<TradeItem> _sellList;
	private final Map<Integer, TradeItem> _sellList0;

	/**
	 * Окно управления личным магазином продажи
	 * @param seller
	 */
	public PrivateStoreManageList(int type, Player seller, boolean pkg)
	{
		_type = type;
		_sellerId = seller.getObjectId();
		_adena = seller.getAdena();
		_package = pkg;
		_sellList0 = seller.getSellList(_package);
		_sellList = new ArrayList<>();

		// Проверяем список вещей в инвентаре, если вещь остутствует - убираем из списка продажи
		Iterator<Map.Entry<Integer, TradeItem>> iterator = _sellList0.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, TradeItem> entry = iterator.next();

			TradeItem si = entry.getValue();
			if(si.getCount() <= 0)
			{
				iterator.remove();
				continue;
			}

			ItemInstance item = seller.getInventory().getItemByObjectId(si.getObjectId());
			if(item == null)
				//вещь недоступна, пробуем найти такую же по itemId
				item = seller.getInventory().getItemByItemId(si.getItemId());

			if(item == null || !item.canBePrivateStore(seller) || item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
			{
				iterator.remove();
				continue;
			}

			//корректируем количество
			si.setCount(Math.min(item.getCount(), si.getCount()));
		}

		ItemInstance[] items = seller.getInventory().getItems();
		// Проверяем список вещей в инвентаре, если вещь остутствует в списке продажи, добавляем в список доступных для продажи
		loop: for(ItemInstance item : items)
			if(item.canBePrivateStore(seller) && item.getItemId() != ItemTemplate.ITEM_ID_ADENA)
			{
				for(TradeItem si : _sellList0.values())
					if(si.getObjectId() == item.getObjectId())
					{
						if(si.getCount() == item.getCount())
							continue loop;
						// Показывает остаток вещей для продажи
						TradeItem ti = new TradeItem(item, item.getTemplate().isBlocked(seller, item));
						ti.setCount(item.getCount() - si.getCount());
						_sellList.add(ti);
						continue loop;
					}
				_sellList.add(new TradeItem(item, item.getTemplate().isBlocked(seller, item)));
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeD(_sellerId);
			writeD(_package ? 1 : 0);
			writeQ(_adena);
			writeD(_sellList0.size());
			//Список вещей уже поставленых на продажу
			for(TradeItem si : _sellList0.values())
			{
				writeItemInfo(si);
				writeQ(si.getOwnersPrice());
				writeQ(si.getStorePrice());
			}
			writeD(_sellList.size());
		}
		else if(_type == 2)
		{
			//Список имеющихся вещей
			writeD(_sellList.size());
			writeD(_sellList.size());
			for(TradeItem si : _sellList)
			{
				writeItemInfo(si);
				writeQ(si.getStorePrice());
			}
		}
	}
}