package l2s.gameserver.model;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.base.PledgeRank;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.model.entity.ranking.player.PlayerRankingManager;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.NpcUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class RankersAuthorityDecoy extends Creature {
    private static final int RANKER_NPC_ID = 18485;
    private static final int DECOY_ACTION_TASK_DELAY = 120_000; // 10 sec ---- 2 min
    private static final SkillEntry RANKERS_AUTHORITY_SKILL_ENTRY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 52018, 1);
    private final HardReference<Player> ownerRef;
    private final long despawnTime;
    private final int[][] inventoryPaperdolls = new int[PcInventory.PAPERDOLL_MAX][4];
    private String name;
    private int nameColor;
    private String title;
    private int titleColor;
    private double collisionRadius;
    private double collisionHeight;
    private Clan clan;
    private int level;
    private int enchantEffect;
    private Race race;
    private Sex sex;
    private int baseClassId;
    private int hairStyle;
    private int hairColor;
    private int face;
    private int beautyHairStyle;
    private int beautyHairColor;
    private int beautyFace;
    private int recomHave;
    private int claimHave;
    private int currentClassId;
    private PledgeRank pledgeRank;
    private int pledgeType;
    private boolean hideHeadAccessories;
    private int armorSetEnchantForPacket;
    private ScheduledFuture<?> decoyActionTask;
    private NpcInstance decoyNpc = null;

    public RankersAuthorityDecoy(int objectId, Player owner, long despawnTime) {
        super(objectId, owner.getTemplate());

        ownerRef = owner.getRef();
        this.despawnTime = despawnTime;

        name = owner.getName();
        nameColor = owner.getNameColor();
        title = owner.getTitle();
        titleColor = owner.getTitleColor();
        collisionRadius = owner.getCollisionRadius();
        collisionHeight = owner.getCollisionHeight();
        clan = owner.getClan();
        level = owner.getLevel();
        enchantEffect = owner.getEnchantEffect();
        race = owner.getRace();
        sex = owner.getSex();
        baseClassId = owner.getBaseClassId();
        hairStyle = owner.getHairStyle();
        hairColor = owner.getHairColor();
        face = owner.getFace();
        beautyHairStyle = owner.getBeautyHairStyle();
        beautyHairColor = owner.getBeautyHairColor();
        beautyFace = owner.getBeautyFace();
        recomHave = owner.getRecomHave();
//        claimHave = owner.getClaimHave();
        currentClassId = owner.getClassId().getId();
        pledgeRank = owner.getPledgeRank();
        pledgeType = owner.getPledgeType();
        hideHeadAccessories = owner.hideHeadAccessories();
//        armorSetEnchantForPacket = owner.getArmorSetEnchantForPacket();

        for (int paperdollId : ExCharInfo.PAPERDOLL_ORDER) {
            if (!ExCharInfo.isVisiblePaperdoll(paperdollId)) {
                continue;
            }
            inventoryPaperdolls[paperdollId][0] = owner.getInventory().getPaperdollItemId(paperdollId);
            inventoryPaperdolls[paperdollId][1] = owner.getInventory().getPaperdollVariation1Id(paperdollId);
            inventoryPaperdolls[paperdollId][2] = owner.getInventory().getPaperdollVariation2Id(paperdollId);
            inventoryPaperdolls[paperdollId][3] = owner.getInventory().getPaperdollVisualId(paperdollId);
        }
    }

    public Player getOwner() {
        return ownerRef == null ? null : ownerRef.get();
    }

    @Override
    public String getName() {
        Player owner = getOwner();
        if (owner != null)
            name = owner.getName();
        return name;
    }

    public int getNameColor() {
        Player owner = getOwner();
        if (owner != null)
            nameColor = owner.getNameColor();
        return nameColor;
    }

    @Override
    public String getTitle() {
        Player owner = getOwner();
        if (owner != null)
            title = owner.getTitle();
        return title;
    }




    public int getTitleColor() {
        Player owner = getOwner();
        if (owner != null)
            titleColor = owner.getTitleColor();
        return titleColor;
    }

    @Override
    public double getCollisionRadius() {
        Player owner = getOwner();
        if (owner != null)
            collisionRadius = owner.getCollisionRadius();
        return collisionRadius;
    }

    @Override
    public double getCollisionHeight() {
        Player owner = getOwner();
        if (owner != null)
            collisionHeight = owner.getCollisionHeight();
        return collisionHeight;
    }

    public Clan getClan() {
        Player owner = getOwner();
        if (owner != null)
            clan = owner.getClan();
        return clan;
    }

    public HardReference<Player> getOwnerRef() {
        return ownerRef;
    }

    @Override
    public int getLevel() {
        Player owner = getOwner();
        if (owner != null)
            level = owner.getLevel();
        return level;
    }

    @Override
    public int getEnchantEffect() {
        Player owner = getOwner();
        if (owner != null)
            enchantEffect = owner.getEnchantEffect();
        return enchantEffect;
    }

    public Race getRace() {
        Player owner = getOwner();
        if (owner != null)
            race = owner.getRace();
        return race;
    }

    @Override
    public Sex getSex() {
        Player owner = getOwner();
        if (owner != null)
            sex = owner.getSex();
        return sex;
    }

    public int getBaseClassId() {
        Player owner = getOwner();
        if (owner != null)
            baseClassId = owner.getBaseClassId();
        return baseClassId;
    }

    public int getHairStyle() {
        Player owner = getOwner();
        if (owner != null)
            hairStyle = owner.getHairStyle();
        return hairStyle;
    }

    public int getHairColor() {
        Player owner = getOwner();
        if (owner != null)
            hairColor = owner.getHairColor();
        return hairColor;
    }

    public int getFace() {
        Player owner = getOwner();
        if (owner != null)
            face = owner.getFace();
        return face;
    }

    public int getBeautyHairStyle() {
        Player owner = getOwner();
        if (owner != null)
            beautyHairStyle = owner.getBeautyHairStyle();
        return beautyHairStyle;
    }

    public int getBeautyHairColor() {
        Player owner = getOwner();
        if (owner != null)
            beautyHairColor = owner.getBeautyHairColor();
        return beautyHairColor;
    }

    public int getBeautyFace() {
        Player owner = getOwner();
        if (owner != null)
            beautyFace = owner.getBeautyFace();
        return beautyFace;
    }

    public int getRecomHave() {
        Player owner = getOwner();
        if (owner != null)
            recomHave = owner.getRecomHave();
        return recomHave;
    }

//    public int getClaimHave() {
//        Player owner = getOwner();
//        if (owner != null)
//            claimHave = owner.getClaimHave();
//        return claimHave;
//    }

    public int getCurrentClassId() {
        Player owner = getOwner();
        if (owner != null)
            currentClassId = owner.getClassId().getId();
        return currentClassId;
    }

    public PledgeRank getPledgeRank() {
        Player owner = getOwner();
        if (owner != null)
            pledgeRank = owner.getPledgeRank();
        return pledgeRank;
    }

    public int getPledgeType() {
        Player owner = getOwner();
        if (owner != null)
            pledgeType = owner.getPledgeType();
        return pledgeType;
    }

    public boolean hideHeadAccessories() {
        Player owner = getOwner();
        if (owner != null)
            hideHeadAccessories = owner.hideHeadAccessories();
        return hideHeadAccessories;
    }

//    public int getArmorSetEnchantForPacket() {
//        Player owner = getOwner();
//        if (owner != null)
//            armorSetEnchantForPacket = owner.getArmorSetEnchantForPacket();
//        return armorSetEnchantForPacket;
//    }

    public int[][] getInventoryPaperdolls() {
        Player owner = getOwner();
        if (owner != null) {
            for (int paperdollId : ExCharInfo.PAPERDOLL_ORDER) {
                if (!ExCharInfo.isVisiblePaperdoll(paperdollId)) {
                    continue;
                }
                inventoryPaperdolls[paperdollId][0] = owner.getInventory().getPaperdollItemId(paperdollId);
                inventoryPaperdolls[paperdollId][1] = owner.getInventory().getPaperdollVariation1Id(paperdollId);
                inventoryPaperdolls[paperdollId][2] = owner.getInventory().getPaperdollVariation2Id(paperdollId);
                inventoryPaperdolls[paperdollId][3] = owner.getInventory().getPaperdollVisualId(paperdollId);
            }
        }
        return inventoryPaperdolls;
    }

    private void stopAllTasks() {
        if (decoyActionTask != null) {
            decoyActionTask.cancel(false);
            decoyActionTask = null;
        }
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        decoyActionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new DecoyActionTask(), 10_000, DECOY_ACTION_TASK_DELAY);
        decoyNpc = NpcUtils.spawnSingle(RANKER_NPC_ID, getLoc(), getReflection());
    }

    @Override
    protected void onDeath(Creature killer) {
        stopAllTasks();
        super.onDeath(killer);
    }

    @Override
    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }

    @Override
    public boolean isAttackable(Creature attacker) {
        return false;
    }

    @Override
    protected void onDelete() {
        stopAllTasks();
        super.onDelete();
        decoyNpc.deleteMe();
        PlayerRankingManager.getInstance().onDeleteRankersAuthority(this);
    }

    @Override
    public void onAction(Player player, boolean shift) {
        //
    }

    @Override
    public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper) {
        List<L2GameServerPacket> list = new ArrayList<>();
        list.add(new ExCharInfo(this, forPlayer));

        GameObject target = getTarget();
        if (target != null && target.isCreature()) {
            list.add(new TargetSelectedPacket(getObjectId(), target.getObjectId(), getLoc()));
        }

        if (isInCombat())
            list.add(new AutoAttackStartPacket(objectId));

        return list;
    }

    @Override
    public WeaponTemplate getActiveWeaponTemplate() {
        return null;
    }

    @Override
    public ItemInstance getActiveWeaponInstance() {
        return null;
    }

    @Override
    public WeaponTemplate getSecondaryWeaponTemplate() {
        return null;
    }

    @Override
    public ItemInstance getSecondaryWeaponInstance() {
        return null;
    }

    @Override
    public boolean isEffectImmune(Creature effector) {
        return true;
    }

    private class DecoyActionTask implements Runnable {
        @Override
        public void run() {
            if (System.currentTimeMillis() >= despawnTime) {
                deleteMe();
            } else {
                broadcastPacket(new SocialActionPacket(getObjectId(), Rnd.chance(50) ? SocialActionPacket.GREETING : SocialActionPacket.VICTORY));
                if (RANKERS_AUTHORITY_SKILL_ENTRY != null && decoyNpc != null)
                    decoyNpc.doCast(RANKERS_AUTHORITY_SKILL_ENTRY, decoyNpc, false);
            }
        }
    }
}
