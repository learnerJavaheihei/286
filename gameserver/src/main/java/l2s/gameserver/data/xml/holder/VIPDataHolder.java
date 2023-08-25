package l2s.gameserver.data.xml.holder;

import java.util.Collection;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.VIPTemplate;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

/**
 * @author Bonux
**/
public class VIPDataHolder extends AbstractHolder
{
	private static final VIPDataHolder _instance = new VIPDataHolder();

	private final IntObjectMap<VIPTemplate> _vipTemplates = new TreeIntObjectMap<VIPTemplate>();

	public static VIPDataHolder getInstance()
	{
		return _instance;
	}

	public void addVIPTemplate(VIPTemplate vipData)
	{
		_vipTemplates.put(vipData.getLevel(), vipData);
	}

	public VIPTemplate getVIPTemplate(int level)
	{
		if(level == 0 && !_vipTemplates.containsKey(level))
			return VIPTemplate.DEFAULT_VIP_TEMPLATE;
		return _vipTemplates.get(level);
	}

	public VIPTemplate getVIPTemplateByPoints(long points)
	{
		VIPTemplate result = VIPTemplate.DEFAULT_VIP_TEMPLATE;
		for(VIPTemplate vipData : getVIPDatas())
		{
			if(vipData.getPoints() <= points)
			{
				if(result.getLevel() < vipData.getLevel())
					result = vipData;
			}
		}
		return result;
	}

	public Collection<VIPTemplate> getVIPDatas()
	{
		return _vipTemplates.valueCollection();
	}

	@Override
	public int size()
	{
		return _vipTemplates.size();
	}

	@Override
	public void clear()
	{
		_vipTemplates.clear();
	}

	@Override
	public void log()
	{
		info(String.format("loaded %d VIP data(s) count.", size()));
	}
}
