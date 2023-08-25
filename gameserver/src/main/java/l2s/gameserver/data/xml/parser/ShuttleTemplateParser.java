package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import l2s.gameserver.geometry.Location;
import org.dom4j.Element;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ShuttleTemplateHolder;
import l2s.gameserver.templates.ShuttleTemplate;
import l2s.gameserver.templates.ShuttleTemplate.ShuttleStop;

/**
 * @author Bonux
 */
public final class ShuttleTemplateParser extends AbstractParser<ShuttleTemplateHolder>
{
	private static final ShuttleTemplateParser _instance = new ShuttleTemplateParser();

	public static ShuttleTemplateParser getInstance()
	{
		return _instance;
	}

	protected ShuttleTemplateParser()
	{
		super(ShuttleTemplateHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/shuttle_data.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "shuttle_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element shuttleElement = iterator.next();

			int shuttleId = Integer.parseInt(shuttleElement.attributeValue("id"));
			ShuttleTemplate template = new ShuttleTemplate(shuttleId);
			for(Iterator<Element> doorsIterator = shuttleElement.elementIterator("stops"); doorsIterator.hasNext();)
			{
				Element doorsElement = doorsIterator.next();
				for(Iterator<Element> doorIterator = doorsElement.elementIterator("stop"); doorIterator.hasNext();)
				{
					Element doorElement = doorIterator.next();
					int doorId = Integer.parseInt(doorElement.attributeValue("id"));
					ShuttleStop shuttleStop = new ShuttleStop(doorId);
					for(Iterator<Element> dimensionIterator = doorElement.elementIterator("dimension"); dimensionIterator.hasNext();)
					{
						Element dimensionElement = dimensionIterator.next();
						shuttleStop.getDimensions().add(Location.parse(dimensionElement));
					}
					template.addStop(shuttleStop);
				}

			}

			getHolder().addTemplate(template);
		}
	}
}
