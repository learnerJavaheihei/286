package l2s.gameserver.templates.item.support;

import java.util.ArrayList;
import java.util.List;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

/**
 * @author Bonux
**/
public class Ensoul
{
	private final int _id;
	private final int _itemId;
	private final int _extractionItemId;
	private final List<SkillEntry> _skills = new ArrayList<SkillEntry>();

	public Ensoul(int id, int itemId, int extractionItemId)
	{
		_id = id;
		_itemId = itemId;
		_extractionItemId = extractionItemId;
	}

	public int getId()
	{
		return _id;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getExtractionItemId()
	{
		return _extractionItemId;
	}

	public void addSkill(int id, int level)
	{
		SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.ITEM, id, level);
		if(skillEntry != null)
			_skills.add(skillEntry);
	}

	public List<SkillEntry> getSkills()
	{
		return _skills;
	}
}
