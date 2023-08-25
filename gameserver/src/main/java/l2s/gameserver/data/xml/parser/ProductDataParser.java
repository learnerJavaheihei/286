package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ProductDataHolder;
import l2s.gameserver.templates.item.product.ProductItem;
import l2s.gameserver.templates.item.product.ProductItemComponent;

/**
 * @author Bonux
**/
public final class ProductDataParser extends AbstractParser<ProductDataHolder>
{
	private static final ProductDataParser _instance = new ProductDataParser();

	public static ProductDataParser getInstance()
	{
		return _instance;
	}

	private ProductDataParser()
	{
		super(ProductDataHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/product_data.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "product_data.dtd";
	}

	@Override
	public boolean isDisabled()
	{
		return !Config.EX_USE_PRIME_SHOP;
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			if("config".equalsIgnoreCase(element.getName()))
			{
				Config.IM_PAYMENT_ITEM_ID = element.attributeValue("points_item_id") == null ? -1 : Integer.parseInt(element.attributeValue("points_item_id"));
			}
			else if("product".equalsIgnoreCase(element.getName()))
			{
				int productId = Integer.parseInt(element.attributeValue("id"));
				int category = Integer.parseInt(element.attributeValue("category")) + 10;
				int price = element.attributeValue("price") == null ? 0 : Integer.parseInt(element.attributeValue("price"));
				int silverCoinCount = element.attributeValue("silver_coin_count") == null ? 0 : Integer.parseInt(element.attributeValue("silver_coin_count"));
				int goldCoinCount = element.attributeValue("gold_coin_count") == null ? 0 : Integer.parseInt(element.attributeValue("gold_coin_count"));
				int minVipLevel = element.attributeValue("min_vip_level") == null ? 0 : Integer.parseInt(element.attributeValue("min_vip_level"));
				int maxVipLevel = element.attributeValue("max_vip_level") == null ? 7 : Integer.parseInt(element.attributeValue("max_vip_level"));
				int limit = element.attributeValue("limit") == null ? -1 : Integer.parseInt(element.attributeValue("limit"));
				String limitRefreshPattern = element.attributeValue("limit_refresh_pattern");
				boolean isHot = element.attributeValue("is_hot") == null ? false : Boolean.parseBoolean(element.attributeValue("is_hot"));
				boolean isNew = element.attributeValue("is_new") == null ? false : Boolean.parseBoolean(element.attributeValue("is_new"));
				boolean onSale = element.attributeValue("on_sale") == null ? false : Boolean.parseBoolean(element.attributeValue("on_sale"));
				long startTimeSale = element.attributeValue("sale_start_date") == null ? 0 : getMillisecondsFromString(element.attributeValue("sale_start_date"));
				long endTimeSale = element.attributeValue("sale_end_date") == null ? 0 : getMillisecondsFromString(element.attributeValue("sale_end_date"));
				int locationId = element.attributeValue("location_id") == null ? -1 : Integer.parseInt(element.attributeValue("location_id"));

				ProductItem product = new ProductItem(productId, category, price, silverCoinCount, goldCoinCount, minVipLevel, maxVipLevel, limit, limitRefreshPattern, isHot, isNew, startTimeSale, endTimeSale, onSale, locationId);
				for(Iterator<Element> subIterator = element.elementIterator(); subIterator.hasNext();)
				{
					Element subElement = subIterator.next();
					if("component".equalsIgnoreCase(subElement.getName()))
					{
						int item_id = Integer.parseInt(subElement.attributeValue("item_id"));
						int count = Integer.parseInt(subElement.attributeValue("count"));

						product.addComponent(new ProductItemComponent(item_id, count));
					}
				}

				getHolder().addProduct(product);
			}
		}
	}

	private static long getMillisecondsFromString(String datetime)
	{
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		try
		{
			Date time = df.parse(datetime);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);

			return calendar.getTimeInMillis();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return 0;
	}
}