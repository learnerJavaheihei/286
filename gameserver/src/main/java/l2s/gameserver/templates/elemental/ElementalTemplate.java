package l2s.gameserver.templates.elemental;

import java.util.Collection;

import l2s.gameserver.model.base.ElementalElement;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

/**
 * @author Bonux
**/
public class ElementalTemplate implements Comparable<ElementalTemplate>
{
	private final ElementalElement _element;
	private final IntObjectMap<ElementalEvolution> _evolutions = new TreeIntObjectMap<ElementalEvolution>();
	private final IntObjectMap<ElementalAbsorbItem> _absorbItems = new HashIntObjectMap<ElementalAbsorbItem>();

	public ElementalTemplate(ElementalElement element)
	{
		_element = element;
	}

	public ElementalElement getElement()
	{
		return _element;
	}

	public void addEvolution(ElementalEvolution evolution)
	{
		_evolutions.put(evolution.getLevel(), evolution);
	}

	public ElementalEvolution getEvolution(int evolutionLevel)
	{
		return _evolutions.get(evolutionLevel);
	}

	public ElementalEvolution[] getEvolutions()
	{
		return _evolutions.values(new ElementalEvolution[_evolutions.size()]);
	}

	public void addAbsorbItem(ElementalAbsorbItem item)
	{
		_absorbItems.put(item.getId(), item);
	}

	public ElementalAbsorbItem getAbsorbItem(int itemId)
	{
		return _absorbItems.get(itemId);
	}

	public Collection<ElementalAbsorbItem> getAbsorbItems()
	{
		return _absorbItems.valueCollection();
	}

	@Override
	public int compareTo(ElementalTemplate o)
	{
		return Integer.compare(getElement().ordinal(), o.getElement().ordinal());
	}
}