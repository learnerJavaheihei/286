package l2s.gameserver.templates;

import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.item.data.ItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class CostumeTemplate {
	public static class ExtractData {
		private final int itemId;
		private final long itemCount;
		private final List<ItemData> fee = new ArrayList<>();

		public ExtractData(int itemId, long itemCount) {
			this.itemId = itemId;
			this.itemCount = itemCount;
		}

		public int getItemId() {
			return itemId;
		}

		public long getItemCount() {
			return itemCount;
		}

		public void addFee(ItemData itemData) {
			fee.add(itemData);
		}

		public List<ItemData> getFee() {
			return fee;
		}
	}

	private final int id;
	private final SkillEntry skillEntry;
	private final int castItemId;
	private final long castItemCount;
	private final int evolutionCostumeId;
	private final int evolutionMod;
	private final int grade;
	private final int locationId;

	private ExtractData extractData;

	public CostumeTemplate(int id, int skillId, int skillLevel, int castItemId, long castItemCount, int evolutionCostumeId, int evolutionMod, int grade, int locationId) {
		this.id = id;
		this.skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.COSTUME, skillId, skillLevel);
		this.castItemId = castItemId;
		this.castItemCount = castItemCount;
		this.evolutionCostumeId = evolutionCostumeId;
		this.evolutionMod = evolutionMod;
		this.grade = grade;
		this.locationId = locationId;
	}

	public int getId() {
		return id;
	}

	public SkillEntry getSkillEntry() {
		return skillEntry;
	}

	public int getCastItemId() {
		return castItemId;
	}

	public long getCastItemCount() {
		return castItemCount;
	}

	public int getEvolutionCostumeId() {
		return evolutionCostumeId;
	}

	public int getEvolutionMod() {
		return evolutionMod;
	}

	public int getGrade() {
		return grade;
	}

	public int getLocationId() {
		return locationId;
	}

	public ExtractData getExtractData() {
		return extractData;
	}

	public void setExtractData(ExtractData extractData) {
		this.extractData = extractData;
	}
}
