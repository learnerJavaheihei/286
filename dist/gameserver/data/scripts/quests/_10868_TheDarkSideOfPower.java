package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassType;

//SanyaDC

public class _10868_TheDarkSideOfPower extends Quest {
    public final int DUH = 34022;
    public final int NIKIA = 34020;
    public final int AIN = 34017;
    public final int GAROM = 34021;
    public final int SOMBRI = 34029;
    public final int RISUOUS = 34031;
    public final int GALLU = 34033;
    public final int KUKONI = 34035;


    //items
    public final int KUKONIPROOF = 90745;
    public final int GALLUPROOF = 90744;
    public final int RISSUOSPROOF = 90743;
    public final int SOMBRIPROOF = 90742;
    public final int RELIKZAK = 90746;
    public final int RELIKORIG = 90747;
    public final int RELIKZAKIMIT = 90748;


    public _10868_TheDarkSideOfPower() {
        super(PARTY_NONE, ONETIME);

        addStartNpc(DUH);
        addTalkId(NIKIA);
        addTalkId(AIN);
        addTalkId(GAROM);
        addTalkId(SOMBRI);
        addTalkId(RISUOUS);
        addTalkId(GALLU);
        addTalkId(KUKONI);
        addQuestItem(RELIKZAK);
        addQuestItem(RELIKORIG);
        addQuestItem(RELIKZAKIMIT);
        addQuestItem(KUKONIPROOF);
        addQuestItem(GALLUPROOF);
        addQuestItem(RISSUOSPROOF);
        addQuestItem(SOMBRIPROOF);

        addLevelCheck("lvl.htm", 70);
        addQuestCompletedCheck("questnotdone.htm", 10867);
    }


    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        String htmltext = event;
        if (event.equalsIgnoreCase("DUH2.htm")) {
            st.setCond(1);
        } else if (event.equalsIgnoreCase("NIKIA2.htm")) {
            st.setCond(2);
        } else if (event.equalsIgnoreCase("AIN2.htm")) {
            st.setCond(3);
        } else if (event.equalsIgnoreCase("NIKIA4.htm")) {
            st.setCond(4);
        } else if (event.equalsIgnoreCase("GAROM2.htm")) {
            st.setCond(5);
        } else if (event.equalsIgnoreCase("SOMBRI2.htm") && st.getQuestItemsCount(SOMBRIPROOF) < 1) {
            st.giveItems(SOMBRIPROOF, 1);
            if (st.getQuestItemsCount(RISSUOSPROOF) > 0 && st.getQuestItemsCount(GALLUPROOF) > 0 && st.getQuestItemsCount(KUKONIPROOF) > 0) {
                st.setCond(6);
            }
        } else if (event.equalsIgnoreCase("RISSUOUS2.htm") && st.getQuestItemsCount(RISSUOSPROOF) < 1) {
            st.giveItems(RISSUOSPROOF, 1);
            if (st.getQuestItemsCount(SOMBRIPROOF) > 0 && st.getQuestItemsCount(GALLUPROOF) > 0 && st.getQuestItemsCount(KUKONIPROOF) > 0) {
                st.setCond(6);
            }
        } else if (event.equalsIgnoreCase("GALLU2.htm") && st.getQuestItemsCount(GALLUPROOF) < 1) {
            st.giveItems(GALLUPROOF, 1);
            if (st.getQuestItemsCount(SOMBRIPROOF) > 0 && st.getQuestItemsCount(RISSUOSPROOF) > 0 && st.getQuestItemsCount(KUKONIPROOF) > 0) {
                st.setCond(6);
            }
        } else if (event.equalsIgnoreCase("KUKONI2.htm") && st.getQuestItemsCount(KUKONIPROOF) < 1) {
            st.giveItems(KUKONIPROOF, 1);
            if (st.getQuestItemsCount(SOMBRIPROOF) > 0 && st.getQuestItemsCount(RISSUOSPROOF) > 0 && st.getQuestItemsCount(GALLUPROOF) > 0) {
                st.setCond(6);
            }
        } else if (event.equalsIgnoreCase("GAROM4.htm")) {
            st.setCond(7);
        } else if (event.equalsIgnoreCase("GAROM6.htm") && st.getQuestItemsCount(RELIKORIG) >= 10) {

            st.addExpAndSp(4200000, 126000);
            st.giveItems(57, 100000);
            st.giveItems(90766, 5);

            st.takeItems(SOMBRIPROOF, -1);
            st.takeItems(RISSUOSPROOF, -1);
            st.takeItems(GALLUPROOF, -1);
            st.takeItems(KUKONIPROOF, -1);
            st.takeItems(RELIKORIG, 10);
            st.finishQuest();

        }
        return htmltext;
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        String htmltext = "noquest";
        int npcId = npc.getNpcId();
        int cond = st.getCond();

        if (npcId == DUH) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 70)

                    htmltext = "DUH.htm";
                else
                    htmltext = "lvl.htm";
            }
            if (cond == 1) {
                htmltext = "DUH2.htm";
            }

        }
        if (npcId == NIKIA) {
            if (cond == 1) {
                htmltext = "NIKIA.htm";
            }
            if (cond == 2) {
                htmltext = "NIKIA2.htm";
            }
            if (cond == 3) {
                htmltext = "NIKIA3.htm";
            }
            if (cond == 4) {
                htmltext = "NIKIA4.htm";
            }
        }
        if (npcId == AIN) {
            {

                if (cond == 2)
                    htmltext = "AIN.htm";

            }
            if (cond == 3) {
                htmltext = "AIN2.htm";
            }
        }
        if (npcId == GAROM) {
            if (cond == 4) {
                htmltext = "GAROM.htm";
            }
            if (cond == 5) {
                htmltext = "GAROM2.htm";
            }
            if (cond == 6) {
                htmltext = "GAROM3.htm";
            }
            if (cond == 7 && st.getQuestItemsCount(RELIKORIG) >= 10) {
                htmltext = "GAROM5.htm";
            }
            if (cond == 7 && st.getQuestItemsCount(RELIKORIG) < 10) {
                htmltext = "GAROM7.htm";
            }
        }
        if (npcId == SOMBRI) {
            if (cond == 5 && st.getQuestItemsCount(SOMBRIPROOF) < 1) {
                htmltext = "SOMBRI.htm";
            } else {
                htmltext = "SOMBRI3.htm";
            }
        }
        if (npcId == RISUOUS) {
            if (cond == 5 && st.getQuestItemsCount(RISSUOSPROOF) < 1) {
                htmltext = "RISSUOUS.htm";
            } else {
                htmltext = "RISSUOUS3.htm";
            }
        }
        if (npcId == GALLU) {
            if (cond == 5 && st.getQuestItemsCount(GALLUPROOF) < 1) {
                htmltext = "GALLU.htm";
            } else {
                htmltext = "GALLU3.htm";
            }
        }
        if (npcId == KUKONI) {
            if (cond == 5 && st.getQuestItemsCount(KUKONIPROOF) < 1) {
                htmltext = "KUKONI.htm";
            } else {
                htmltext = "KUKONI3.htm";
            }
        }
        return htmltext;
    }

}

