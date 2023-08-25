package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.TeleportListHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.templates.TeleportInfo;
import org.dom4j.Element;

public class TeleportListParser extends AbstractParser<TeleportListHolder>
{
	private static TeleportListParser _instance = new TeleportListParser();

	public static TeleportListParser getInstance()
	{
		return _instance;
	}

	private TeleportListParser()
	{
		super(TeleportListHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/teleport_list.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "teleport_list.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for (Iterator<Element> iterator = rootElement.elementIterator("teleport"); iterator.hasNext(); )
		{
			Element element = iterator.next();

			int id = parseInt(element, "id");
			int x = parseInt(element, "x");
			int y = parseInt(element, "y");
			int z = parseInt(element, "z");
			long price = parseInt(element, "price");

			getHolder().addTeleportInfo(new TeleportInfo(id, new Location(x, y, z), price));
		}
	}
}
