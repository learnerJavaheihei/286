package l2s.gameserver.model.items;

import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.templates.item.RecipeTemplate;

public class ManufactureItem
{
	private final int _recipeId;
	private final long _cost;

	public ManufactureItem(int recipeId, long cost)
	{
		_recipeId = recipeId;
		_cost = cost;
	}

	public int getRecipeId()
	{
		return _recipeId;
	}

	public boolean canCrit()
	{
		RecipeTemplate template = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);
		if(template == null)
			return false;
		return template.canCrit();
	}

	public long getCost()
	{
		return _cost;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == this)
			return true;
		if(o == null)
			return false;
		if(o.getClass() != this.getClass())
			return false;
		return ((ManufactureItem) o).getRecipeId() == this.getRecipeId();
	}

	@Override
	public int hashCode()
	{
		return 17 * _recipeId + 11021;
	}
}
