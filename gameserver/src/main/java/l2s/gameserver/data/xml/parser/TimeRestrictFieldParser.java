package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.TimeRestrictFieldHolder;
import l2s.gameserver.templates.TimeRestrictFieldInfo;

/**
 * @author nexvill
**/
public final class TimeRestrictFieldParser extends AbstractParser<TimeRestrictFieldHolder>
{
	private static final TimeRestrictFieldParser _instance = new TimeRestrictFieldParser();

	public static TimeRestrictFieldParser getInstance()
	{
		return _instance;
	}

	private TimeRestrictFieldParser()
	{
		super(TimeRestrictFieldHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/time_restrict_field.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "time_restrict_field.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception 
	{
		for (Iterator<Element> iterator = rootElement.elementIterator("field"); iterator.hasNext();)
		{
			Element element = iterator.next();

			int id = parseInt(element, "id");
			int itemId = parseInt(element, "itemId");
			long itemCount = parseLong(element, "itemCount");
			int resetCycle = parseInt(element, "resetCycle");
			int minLevel = parseInt(element, "minLevel");
			int maxLevel = parseInt(element, "maxLevel");
			int remainTimeBase = parseInt(element, "remainTimeBase");
			int remainTimeMax = parseInt(element, "remainTimeMax");
			int enterX = parseInt(element, "enterX");
			int enterY = parseInt(element, "enterY");
			int enterZ = parseInt(element, "enterZ");
			int exitX = parseInt(element, "exitX");
			int exitY = parseInt(element, "exitY");
			int exitZ = parseInt(element, "exitZ");
			
			getHolder().addTimeRestrictFieldInfo(new TimeRestrictFieldInfo(id, itemId, itemCount, resetCycle, minLevel, maxLevel, remainTimeBase, remainTimeMax, enterX, enterY, enterZ, exitX, exitY, exitZ));
		}
	}
}