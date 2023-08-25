package l2s.gameserver.data.xml.holder;

import java.util.Map;
import java.util.TreeMap;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.templates.elemental.ElementalTemplate;

/**
 * @author Bonux
**/
public final class ElementalDataHolder extends AbstractHolder
{
	private static final ElementalDataHolder _instance = new ElementalDataHolder();

	private final Map<ElementalElement, ElementalTemplate> _templates = new TreeMap<ElementalElement, ElementalTemplate>();

	public static ElementalDataHolder getInstance()
	{
		return _instance;
	}

	public void addTemplate(ElementalTemplate template)
	{
		_templates.put(template.getElement(), template);
	}

	public ElementalTemplate getTemplate(ElementalElement element)
	{
		return _templates.get(element);
	}

	@Override
	public int size()
	{
		return _templates.size();
	}

	@Override
	public void clear()
	{
		_templates.clear();
	}
}
