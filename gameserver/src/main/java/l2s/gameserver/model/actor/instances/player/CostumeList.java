package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CharacterCostumesDAO;
import l2s.gameserver.data.xml.holder.CostumesHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExCostumeEvolution;
import l2s.gameserver.network.l2.s2c.ExCostumeExtract;
import l2s.gameserver.network.l2.s2c.ExSendCostumeList;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.CostumeTemplate;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.commons.collections4.bidimap.UnmodifiableBidiMap;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
/*TODO: Пересмотреть абнормал типы, сделать правильное наложение/замену костюма, если висит уже другой костюм. Выводить правильное сообщение отката.
msg_begin	id=6840	UNK_0=1	message=[Через $s1 мин. эффект от коллекции можно будет выбрать заново.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
msg_begin	id=6841	UNK_0=1	message=[Эффект коллекции уже активирован.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
msg_begin	id=6842	UNK_0=1	message=[Коллекция не собрана полностью, активировать эффект нельзя.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
msg_begin	id=6843	UNK_0=1	message=[Нельзя активировать эффект из-за ошибки системы. Повторите попытку позже.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
msg_begin	id=6844	UNK_0=1	message=[Нельзя продолжить из-за ошибки системы. Повторите попытку позже.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
msg_begin	id=6847	UNK_0=1	message=[Неподходящий предмет.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
msg_begin	id=6848	UNK_0=1	message=[Не хватает предметов.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
msg_begin	id=6864	UNK_0=1	message=[Через $s1 сек. эффект от коллекции можно будет выбрать заново.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
msg_begin	id=6866	UNK_0=1	message=[В данный момент нельзя использовать эволюцию трансформации. Повторите попытку позже.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
msg_begin	id=6867	UNK_0=1	message=[В данный момент нельзя извлечь трансформацию. Повторите попытку позже.]	group=0	color=799BB0FF	sound=[None]	voice=[None]	win=0	font=0	lftime=0	bkg=0	anim=0	scrnmsg=[]	gfxscrnmsg=[]	gfxscrnparam=[]	type=[none]	msg_end
*/
public class CostumeList implements Iterable<Costume> {
	public static int getShortCutId(int page, int slot) {
		return (page + 1) * 100 + slot + 1;
	}

	public static int getPageId(int shortCutId) {
		return shortCutId / 100 - 1;
	}

	public static int getSlotId(int shortCutId) {
		return shortCutId % 100 - 1;
	}

	private static final BidiMap<Integer, Integer> EMPTY_BIDI_MAP = UnmodifiableBidiMap.unmodifiableBidiMap(new DualHashBidiMap<>());

	private final Player owner;
	private final Map<Integer, Costume> costumes;
	private final BidiMap<Integer, Integer> shortcuts;
	private final Lock lock = new ReentrantLock();

	public CostumeList(Player owner) {
		this.owner = owner;
		if (!Config.EX_COSTUME_DISABLE) {
			costumes = new TreeMap<>();
			shortcuts = new TreeBidiMap<>();
		} else {
			costumes = Collections.emptyMap();
			shortcuts = EMPTY_BIDI_MAP;
		}
	}

	public Player getOwner() {
		return owner;
	}

	public void restore() {
		if (!Config.EX_COSTUME_DISABLE)
			CharacterCostumesDAO.getInstance().restore(owner, costumes, shortcuts);
	}

	public void store() {
		if (!Config.EX_COSTUME_DISABLE)
			CharacterCostumesDAO.getInstance().store(owner);
	}

	@Override
	public Iterator<Costume> iterator() {
		return costumes.values().iterator();
	}

	public int size() {
		return costumes.size();
	}

	public Collection<Costume> values() {
		return costumes.values();
	}

	public boolean add(CostumeTemplate costumeTemplate) {
		Costume costume = get(costumeTemplate.getId());
		if (costume == null) {
			costume = new Costume(costumeTemplate, 1, Costume.IS_NEW);
			if (costumes.put(costume.getId(), costume) != null)
				return false;
			CharacterCostumesDAO.getInstance().insert(owner, costume);
		} else {
			costume.setCount(costume.getCount() + 1);
			CharacterCostumesDAO.getInstance().update(owner, costume);
		}
		return true;
	}

	public boolean remove(int costumeId) {
		if (costumes.remove(costumeId) == null)
			return false;
		CharacterCostumesDAO.getInstance().delete(owner, costumeId);
		return true;
	}

	public Costume get(int id) {
		return costumes.get(id);
	}

	public boolean contains(int id) {
		return get(id) != null;
	}

	public BidiMap<Integer, Integer> getShortcuts() {
		return shortcuts;
	}

	public boolean useCostume(int skillId) {
		if (Config.EX_COSTUME_DISABLE)
			return false;

		CostumeTemplate costumeTemplate = CostumesHolder.getInstance().getCostumeBySkillId(skillId);
		if (costumeTemplate == null)
			return false;

		lock.lock();
		try {
			Costume costume = get(costumeTemplate.getId());
			if (costume == null)
				return false;

			costume.setFlag(Costume.IS_NEW, false);

			if (costume.getCount() <= 0)
				return false;

			SkillEntry skillEntry = costumeTemplate.getSkillEntry();
			if (skillEntry == null) {
				owner.sendPacket(SystemMsg.LIST_OF_TRANSFORMATIONS_HASNT_LOADED_TRY_AGAIN_LATER);
				return false;
			}

			Skill skill = skillEntry.getTemplate();
			AbnormalType abnormalType = skill.getAbnormalType();
			if (abnormalType != AbnormalType.NONE && skill.getAbnormalLvl() <= owner.getAbnormalList().getAbnormalLevel(abnormalType)) {
				owner.sendPacket(SystemMsg.TRANSFORMATION_IS_NOT_AVAILABLE_AT_THE_MOMENT_TRY_AGAIN_LATER);
				return false;
			}

			if (owner.isSkillDisabled(skill)) {
				owner.sendPacket(SystemMsg.TRANSFORMATION_IS_NOT_AVAILABLE_AT_THE_MOMENT_TRY_AGAIN_LATER);
				return false;
			}

			IBroadcastPacket condMsg = checkCondition(true, false, false);
			if (condMsg != null) {
				owner.sendPacket(condMsg);
				return false;
			}

			if (!ItemFunctions.deleteItem(owner, costumeTemplate.getCastItemId(), costumeTemplate.getCastItemCount(), true)) {
				return false;
			}

			//costume.setCount(costume.getCount() - 1);
			//CharacterCostumesDAO.getInstance().update(owner, costume);

			if (skill.getReuseDelay() > 10){
				long calcReuseDelay = (long) owner.getStat().calc(Stats.TRANSFORMER_SKILL_REUSE, skill.getReuseDelay(), null, skill);
				owner.disableSkill(skill, calcReuseDelay);
			}
			int calcAbnormalTime = (int)owner.getStat().calc(Stats.TRANSFORMER_SKILL_ADD_HIT_TIME, skill.getAbnormalTimeFinal(), null, skill);
			skill.setAbnormalTime(calcAbnormalTime);
			owner.forceUseSkill(skillEntry, owner);
			owner.sendPacket(new ExSendCostumeList(owner));
			return true;
		} finally {
			lock.unlock();
		}
	}

	public boolean evolutionCostume(int costumeId, List<int[]> costumesConsume) {
		lock.lock();
		try {
			Costume costume = get(costumeId);
			if (costume == null || costume.getCount() <= 0) {
				owner.sendPacket(SystemMsg.EVOLUTION_OF_TRANSFORMATION_IS_NOT_AVAILABLE_AT_THE_MOMENT_TRY_AGAIN_LATER);
				owner.sendPacket(ExCostumeEvolution.FAIL);
				return false;
			}

			CostumeTemplate costumeTemplate = costume.getTemplate();
			if (costumeTemplate.getEvolutionMod() <= 0) {
				owner.sendPacket(SystemMsg.EVOLUTION_IS_NOT_AVAILABLE_FOR_THIS_TRANSFORMATION);
				owner.sendPacket(ExCostumeEvolution.FAIL);
				return false;
			}

			CostumeTemplate evolutionCostumeTemplate = CostumesHolder.getInstance().getCostume(costumeTemplate.getEvolutionCostumeId());
			if (evolutionCostumeTemplate == null) {
				owner.sendPacket(SystemMsg.EVOLUTION_IS_NOT_AVAILABLE_FOR_THIS_TRANSFORMATION);
				owner.sendPacket(ExCostumeEvolution.FAIL);
				return false;
			}

			for (int[] consumeData : costumesConsume) {
				int consumeCostumeId = consumeData[0];
				int consumeCount = consumeData[1];
				//int consumeUnk = consumeData[2];
				Costume consumeCostume = get(consumeCostumeId);
				if (consumeCostume == null || consumeCostume.getCount() < consumeCount) {
					owner.sendPacket(SystemMsg.NOT_ENOUGH_MATERIAL_FOR_EVOLUTION);
					owner.sendPacket(ExCostumeEvolution.FAIL);
					return false;
				}
			}

			costume.setCount(costume.getCount() - 1);
			costume.setFlag(Costume.IS_NEW, false);
			CharacterCostumesDAO.getInstance().update(owner, costume);

			for (int[] consumeData : costumesConsume) {
				int consumeCostumeId = consumeData[0];
				int consumeCount = consumeData[1];
				//int consumeUnk = consumeData[2];
				Costume consumeCostume = get(consumeCostumeId);
				consumeCostume.setCount(consumeCostume.getCount() - consumeCount);
				CharacterCostumesDAO.getInstance().update(owner, consumeCostume);
			}
			add(evolutionCostumeTemplate);
			owner.sendPacket(new ExCostumeEvolution(0, evolutionCostumeTemplate.getId(), 1));
			return true;
		} finally {
			lock.unlock();
		}
	}

	public boolean extractCostume(int costumeId) {
		lock.lock();
		try {
			Costume costume = get(costumeId);
			if (costume == null || costume.getCount() <= 0) {
				owner.sendPacket(SystemMsg.TRANSFORMATION_CANNOT_BE_EXTRACTED_AT_THE_MOMENT_TRY_AGAIN_LATER);
				owner.sendPacket(ExCostumeExtract.FAIL);
				return false;
			}

			CostumeTemplate.ExtractData extractData = costume.getTemplate().getExtractData();
			if (extractData == null) {
				owner.sendPacket(SystemMsg.EXTRACTION_IS_NOT_AVAILABLE_FOR_THIS_TRANSFORMATION);
				owner.sendPacket(ExCostumeExtract.FAIL);
				return false;
			}

			IBroadcastPacket condMsg = checkCondition(false, false, true);
			if (condMsg != null) {
				owner.sendPacket(condMsg);
				owner.sendPacket(ExCostumeExtract.FAIL);
				return false;
			}

			if (owner.isInventoryFull()) {
				owner.sendPacket(SystemMsg.NOT_ENOUGH_SPACE_IN_INVENTORY_FREE_SOME_SPACE_AND_TRY_AGAIN);
				owner.sendPacket(ExCostumeExtract.FAIL);
				return false;
			}

			int extractItemId = 0;
			long extractItemCount = 0L;

			owner.getInventory().writeLock();
			try {
				for (ItemData itemData : extractData.getFee()) {
					if (!ItemFunctions.haveItem(owner, itemData.getId(), itemData.getCount())) {
						owner.sendPacket(SystemMsg.NOT_ENOUGH_MATERIALS_FOR_EXTRACTION);
						owner.sendPacket(ExCostumeExtract.FAIL);
						return false;
					}
				}

				for (ItemData itemData : extractData.getFee()) {
					ItemFunctions.deleteItem(owner, itemData.getId(), itemData.getCount(), true);
					if (extractItemId == 0 || extractItemCount == 0L) {
						extractItemId = itemData.getId();
						extractItemCount = itemData.getCount();
					}
				}
			} finally {
				owner.getInventory().writeUnlock();
			}

			ItemFunctions.addItem(owner, extractData.getItemId(), extractData.getItemCount(), true);
			costume.setCount(costume.getCount() - 1);
			costume.setFlag(Costume.IS_NEW, false);
			CharacterCostumesDAO.getInstance().update(owner, costume);
			owner.sendPacket(new ExCostumeExtract(1, extractItemId, extractItemCount, extractData.getItemId(), extractData.getItemCount(), owner.getAdena()));
			return true;
		} finally {
			lock.unlock();
		}
	}

	public IBroadcastPacket checkCondition(boolean use, boolean evolution, boolean extract) {
		if (use || evolution || extract) {
			if (owner.isDead())
				return SystemMsg.DEAD_CHARACTER_CANNOT_USE_TRANSFORMATION_EVOLUTION_AND_EXTRACTION;

			if (owner.isInCombat()) {
				if (use)
					return SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_IN_A_FIGHT;
				return SystemMsg.EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_DURING_A_FIGHT;
			}

			if (owner.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
				return SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_DURING_PRIVATE_STORE_OR_WORKSHOP_USE;

			if (owner.isInTrade())
				return SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_DURING_EXCHANGE;

			if (owner.isFishing())
				return SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_DURING_FISHING;

			if (owner.isSitting())
				return SystemMsg.YOU_CANNOT_USE_TRANSFORMATION_EVOLUTION_AND__EXTRACTION_WHILE_SITTING;

			if (owner.isParalyzed())
				return SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_IN_THE_PETRIFICATION_STATE;

			if (owner.isImmobilized())
				return SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_IN_THE_FREEZE_STATE;
		}
		return null;
	}

	public boolean setShortCut(int page, int slot, int costumeId) {
		lock.lock();
		try {
			int shortCutId = getShortCutId(page, slot);
			if (costumeId == 0) {
				if (shortcuts.remove(shortCutId) == null)
					return false;
			} else {
				Costume costume = get(costumeId);
				if (costume == null)
					return false;

				if (getShortCutId(costumeId) == shortCutId)
					return false;

				costume.setFlag(Costume.IS_NEW, false);
				shortcuts.put(shortCutId, costumeId);
			}
		} finally {
			lock.unlock();
		}
		return true;
	}

	public int getShortCutId(int costumeId) {
		Integer currentShortCutId = shortcuts.getKey(costumeId);
		if (currentShortCutId != null) {
			return currentShortCutId;
		}
		return 0;
	}

	public Lock getLock() {
		return lock;
	}

	@Override
	public String toString() {
		return "CostumeList[owner=" + owner.getName() + "]";
	}
}