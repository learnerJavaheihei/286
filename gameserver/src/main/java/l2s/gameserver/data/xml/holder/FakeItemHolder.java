package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.ArmorTemplate;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;

import org.napile.primitive.Containers;
import org.napile.primitive.lists.IntList;
import org.napile.primitive.lists.impl.ArrayIntList;
import org.napile.primitive.sets.IntSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 LIO 
 29.01.2016
 */
public class FakeItemHolder extends AbstractHolder
{
	private static class ClassWeaponAndArmor
	{
		private final int classId;
		private final List<WeaponTemplate.WeaponType> weaponTypes = new ArrayList<>();
		private final List<ArmorTemplate.ArmorType> armorTypes = new ArrayList<>();

		public ClassWeaponAndArmor(int classId, String weaponTypes, String armorTypes)
		{
			this.classId = classId;
			for(String s : weaponTypes.split(";"))
			{
				this.weaponTypes.add(WeaponTemplate.WeaponType.valueOf(s));
			}
			for(String s : armorTypes.split(";"))
			{
				this.armorTypes.add(ArmorTemplate.ArmorType.valueOf(s));
			}
		}

		public WeaponTemplate.WeaponType getRandomWeaponType()
		{
			return weaponTypes.get(Rnd.get(weaponTypes.size()));
		}

		public ArmorTemplate.ArmorType getRandomArmorType()
		{
			return armorTypes.get(Rnd.get(armorTypes.size()));
		}
	}

	private static FakeItemHolder ourInstance = new FakeItemHolder();

	public static FakeItemHolder getInstance()
	{
		return ourInstance;
	}

	private final Map<ItemGrade, Map<WeaponTemplate.WeaponType, IntList>> weapons = new HashMap<>();
	private final Map<ItemGrade, Map<ArmorTemplate.ArmorType, List<IntList>>> armors = new HashMap<>();
	private final Map<ItemGrade, List<IntList>> accessorys = new HashMap<>();
	private final Map<Integer, ClassWeaponAndArmor> classWeaponAndArmors = new HashMap<>();
	private final IntList _hairAccessories = new ArrayIntList();
	private final IntList _cloaks = new ArrayIntList();

	public void addWeapons(ItemGrade grade, IntList list)
	{
		Map<WeaponTemplate.WeaponType, IntList> map = new HashMap<>();
		for(int itemId : list.toArray())
		{
			ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
			if(template == null || !template.isWeapon())
			{
				//TODO Log
				continue;
			}
			WeaponTemplate.WeaponType weaponType = ((WeaponTemplate) template).getItemType();
			if(template.isMagicWeapon())
			{
				weaponType = WeaponTemplate.WeaponType.MAGIC;
			}
			if(map.get(weaponType) == null)
			{
				map.put(weaponType, new ArrayIntList());
			}
			map.get(weaponType).add(itemId);
		}
		weapons.put(grade, map);
	}

	public void addArmors(ItemGrade grade, Map<ArmorTemplate.ArmorType, List<IntList>> map)
	{
		armors.put(grade, map);
	}

	public void addAccessorys(ItemGrade grade, List<IntList> list)
	{
		accessorys.put(grade, list);
	}

	public void addClassWeaponAndArmors(int classId, String weaponTypes, String armorTypes)
	{
		classWeaponAndArmors.put(classId, new ClassWeaponAndArmor(classId, weaponTypes, armorTypes));
	}

	public void addHairAccessories(IntList list)
	{
		_hairAccessories.addAll(list);
	}

	public IntList getHairAccessories()
	{
		return _hairAccessories;
	}

	public void addCloaks(IntList list)
	{
		_cloaks.addAll(list);
	}

	public IntList getCloaks()
	{
		return _cloaks;
	}

	public IntList getRandomItems(Player player, String type, int expertiseIndex)
	{
		ItemGrade grade = ItemGrade.values()[expertiseIndex];
		IntList result = null;
		switch(type)
		{
			case "Accessory":
			{
				List<IntList> packs = accessorys.get(grade);
				if(packs == null || packs.isEmpty())
					return Containers.EMPTY_INT_LIST;
				result = packs.get(Rnd.get(packs.size()));
				break;
			}
			case "Armor":
			{
				try
				{
					ClassWeaponAndArmor classWeaponAndArmor = classWeaponAndArmors.get(player.getClassId().getId());
					if(classWeaponAndArmor == null)
						return Containers.EMPTY_INT_LIST;
					Map<ArmorTemplate.ArmorType, List<IntList>> packs = armors.get(grade);
					if(packs == null || packs.isEmpty())
						return Containers.EMPTY_INT_LIST;
					List<IntList> armors = packs.get(classWeaponAndArmor.getRandomArmorType());
					result = Rnd.get(armors);
				}
				catch(Exception e)
				{
					System.out.println(player.getClassId().getId());
				}
				break;
			}
			case "Weapon":
			{
				ClassWeaponAndArmor classWeaponAndArmor = classWeaponAndArmors.get(player.getClassId().getId());
				if(classWeaponAndArmor == null)
					return Containers.EMPTY_INT_LIST;
				Map<WeaponTemplate.WeaponType, IntList> packs = weapons.get(grade);
				if(packs == null || packs.isEmpty())
					return Containers.EMPTY_INT_LIST;
				result = new ArrayIntList();
				while(result.isEmpty())
				{
					IntList list = packs.get(classWeaponAndArmor.getRandomWeaponType());
					if(list != null)
					{
						result.add(list.get(list.size() - 1));
					}
				}
				break;
			}
		}
		if(result != null)
			return result;
		return Containers.EMPTY_INT_LIST;
	}

	@Override
	public void log()
	{
		info("loaded fake items.");
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		//
	}
}