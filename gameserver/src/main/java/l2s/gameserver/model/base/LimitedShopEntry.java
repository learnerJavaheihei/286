package l2s.gameserver.model.base;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nexvill
 */
public class LimitedShopEntry
{
	private int _entryId;
	private List<LimitedShopIngredient> _ingredients = new ArrayList<LimitedShopIngredient>();
	private List<LimitedShopProduction> _production = new ArrayList<LimitedShopProduction>();

	public LimitedShopEntry()
	{}

	public LimitedShopEntry(int id)
	{
		_entryId = id;
	}

	/**
	 * @param entryId The entryId to set.
	 */
	public void setEntryId(int entryId)
	{
		_entryId = entryId;
	}

	/**
	 * @return Returns the entryId.
	 */
	public int getEntryId()
	{
		return _entryId;
	}

	/**
	 * @param ingredients The ingredients to set.
	 */
	public void addIngredient(LimitedShopIngredient ingredient)
	{
		_ingredients.add(ingredient);
	}

	/**
	 * @return Returns the ingredients.
	 */
	public List<LimitedShopIngredient> getIngredients()
	{
		return _ingredients;
	}

	/**
	 * @param production The production to set.
	 */
	public void addProduct(LimitedShopProduction production)
	{
		_production.add(production);
	}

	/**
	 * @return Returns the production.
	 */
	public List<LimitedShopProduction> getProduction()
	{
		return _production;
	}

	@Override
	public int hashCode()
	{
		return _entryId;
	}

	@Override
	public LimitedShopEntry clone()
	{
		LimitedShopEntry ret = new LimitedShopEntry(_entryId);
		for(LimitedShopIngredient i : _ingredients)
			ret.addIngredient(i.clone());
		for(LimitedShopProduction i : _production)
			ret.addProduct(i.clone());
		return ret;
	}
}