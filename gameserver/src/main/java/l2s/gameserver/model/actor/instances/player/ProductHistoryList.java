package l2s.gameserver.model.actor.instances.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.ProductHistoryDAO;
import l2s.gameserver.data.xml.holder.ProductDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExBR_NewIConCashBtnWnd;
import l2s.gameserver.templates.item.product.ProductItem;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

/**
 * @author Bonux
**/
public class ProductHistoryList
{
	/** Блокировка для чтения/записи вещей из списка и внешних операций */
	protected final ReadWriteLock lock = new ReentrantReadWriteLock();
	protected final Lock readLock = lock.readLock();
	protected final Lock writeLock = lock.writeLock();

	public final void writeLock()
	{
		writeLock.lock();
	}

	public final void writeUnlock()
	{
		writeLock.unlock();
	}

	public final void readLock()
	{
		readLock.lock();
	}

	public final void readUnlock()
	{
		readLock.unlock();
	}

	private IntObjectMap<ProductHistoryItem> _productHistoryMap = new HashIntObjectMap<ProductHistoryItem>(0);
	private final Player _owner;

	private ScheduledFuture<?> _limitRefreshTask = null;

	public ProductHistoryList(Player owner)
	{
		_owner = owner;
	}

	public void restore()
	{
		if(Config.EX_USE_PRIME_SHOP)
		{
			ProductHistoryDAO.getInstance().select(_owner, _productHistoryMap);
			refreshLimits();
		}
	}

	public boolean add(ProductHistoryItem historyItem)
	{
		if(!Config.EX_USE_PRIME_SHOP)
			return false;

		if(historyItem.getProduct().getLimit() > 0) // Сохраняем только лимитированные предметы.
		{
			if(!ProductHistoryDAO.getInstance().replace(_owner, historyItem))
				return false;

			writeLock();
			try
			{
				_productHistoryMap.put(historyItem.getProduct().getId(), historyItem);
			}
			finally
			{
				writeUnlock();
			}
		}
		return true;
	}

	public ProductHistoryItem get(int productId)
	{
		readLock();
		try
		{
			return _productHistoryMap.get(productId);
		}
		finally
		{
			readUnlock();
		}
	}

	public boolean remove(int productId)
	{
		boolean removed;

		writeLock();
		try
		{
			removed = _productHistoryMap.remove(productId) != null;
		}
		finally
		{
			writeUnlock();
		}

		if(removed)
		{
			ProductHistoryDAO.getInstance().delete(_owner, productId);
			return true;
		}
		return false;
	}

	public boolean contains(int productId)
	{
		readLock();
		try
		{
			return _productHistoryMap.containsKey(productId);
		}
		finally
		{
			readUnlock();
		}
	}

	public int size()
	{
		readLock();
		try
		{
			return _productHistoryMap.size();
		}
		finally
		{
			readUnlock();
		}
	}

	public ProductHistoryItem[] values()
	{
		readLock();
		try
		{
			return _productHistoryMap.values(new ProductHistoryItem[_productHistoryMap.size()]);
		}
		finally
		{
			readUnlock();
		}
	}

	public Collection<ProductItem> productValues()
	{
		List<ProductItem> products = new ArrayList<ProductItem>();

		for(ProductHistoryItem item : values())
			products.add(item.getProduct());

		return products;
	}

	public boolean isEmpty()
	{
		readLock();
		try
		{
			return _productHistoryMap.isEmpty();
		}
		finally
		{
			readUnlock();
		}
	}

	@Override
	public String toString()
	{
		return "ProductHistoryList[owner=" + _owner.getName() + "]";
	}

	public void startTask()
	{
		if(!Config.EX_USE_PRIME_SHOP)
			return;

		stopTask();

		long limitRefreshTime = 0L;

		writeLock();
		try
		{
			for(ProductHistoryItem item : values())
			{
				if(item.getProduct().getLimit() == -1)
					continue;

				SchedulingPattern pattern = item.getProduct().getLimitRefreshPattern();
				if(pattern == null)
					continue;

				long time = pattern.next(item.getLastPurchaseTime() * 1000L);
				if(time <= System.currentTimeMillis())
				{
					remove(item.getProduct().getId());
					continue;
				}

				if(limitRefreshTime > 0 && limitRefreshTime < time)
					continue;

				limitRefreshTime = time;
			}
		}
		finally
		{
			writeUnlock();
		}

		long delay = limitRefreshTime - System.currentTimeMillis();
		if(delay < 0)
			return;

		_limitRefreshTask = ThreadPoolManager.getInstance().schedule(() ->{
			refreshLimits();
			_owner.sendPacket(new ExBR_NewIConCashBtnWnd(_owner));
			startTask();
		}, delay);
	}

	public void stopTask()
	{
		if(_limitRefreshTask != null)
		{
			_limitRefreshTask.cancel(false);
			_limitRefreshTask = null;
		}
	}

	private void refreshLimits()
	{
		writeLock();
		try
		{
			for(ProductHistoryItem item : values())
			{
				if(item.getProduct().getLimit() == -1)
					continue;

				SchedulingPattern pattern = item.getProduct().getLimitRefreshPattern();
				if(pattern == null)
					continue;

				long time = pattern.next(item.getLastPurchaseTime() * 1000L);
				if(time > System.currentTimeMillis())
					continue;

				remove(item.getProduct().getId());
			}
		}
		finally
		{
			writeUnlock();
		}
	}

	public void onPurchaseProduct(ProductItem product, int count)
	{
		writeLock();
		try
		{
			ProductHistoryItem item = get(product.getId());
			if(item != null)
			{
				item.setPurchasedCount(item.getPurchasedCount() + count);
				item.setLastPurchaseTime((int) (System.currentTimeMillis() / 1000));
			}
			else
				item = new ProductHistoryItem(product, count, (int) (System.currentTimeMillis() / 1000));

			add(item);
			startTask();
		}
		finally
		{
			writeUnlock();
		}
	}

	public boolean isExpended(int productId)
	{
		ProductHistoryItem item = get(productId);
		if(item != null)
			return item.isExpended();

		return false;
	}

	public boolean haveGifts()
	{
		if(!Config.EX_USE_PRIME_SHOP)
			return false;

		for(ProductItem product : ProductDataHolder.getInstance().getProductsOnSale(_owner))
		{
			if(product.getLimit() == -1)
				continue;

			if(product.getPrice() > 0)
				continue;

			if(product.getSilverCoinCount() > 0 || product.getGoldCoinCount() > 0)
				continue;

			ProductHistoryItem historyItem = get(product.getId());
			if(historyItem != null)
			{
				if(historyItem.isExpended())
					continue;
			}
			return true;
		}
		return false;
	}
}