package npc.model;

import handler.bbs.custom.communitybuffer.BuffSkill;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.velocity.runtime.directive.Break;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NPC輔助
 **/
public class BuffInstance extends NpcInstance
{
	// 技能 等級 最小等級需求  金額
	public static final int[][] BUFFS_MAGIC = {
			{ 1033, 1, 35, 10 },		//毒性抵抗
			{ 1033, 2, 40, 30 },		//毒性抵抗
			{ 1033, 3, 44, 50 },		//毒性抵抗
			{ 1304, 1, 42, 10 },		//伊娃之盾
			{ 1304, 2, 50, 30 },		//伊娃之盾
			{ 1304, 3, 58, 50 },		//伊娃之盾
			{ 1304, 4, 66, 50 },		//伊娃之盾
			{ 1304, 5, 72, 50 },		//伊娃之盾
			{ 1284, 1, 62, 10 },		//復仇
			{ 1284, 2, 68, 30 },		//復仇
			{ 1284, 3, 74, 50 },		//復仇
			{ 1362, 1, 77, 50 },		//聖靈
			{ 1068, 1, 1, 10 },		//力量強化
			{ 1068, 2, 20, 30 },	//力量強化
			{ 1068, 3, 40, 50 },	//力量強化
			{ 1040, 1, 1, 10 },		//保護盾
			{ 1040, 2, 25, 30 },	//保護盾
			{ 1040, 3, 44, 50 },	//保護盾
			{ 1086, 1, 1, 10 },		//速度激發
			{ 1086, 2, 52, 50 },	//速度激發
			{ 1085, 1, 1, 10 },		//靈活思緒
			{ 1085, 2, 35, 30 },	//靈活思緒
			{ 1085, 3, 48, 50 },	//靈活思緒
			{ 1204, 1, 1, 10 },		//風之疾走
			{ 1204, 2, 30, 50 },	//風之疾走
			{ 1059, 1, 1, 10 },		//魔力催化
			{ 1059, 2, 44, 30 },	//魔力催化
			{ 1059, 3, 52, 50 },	//魔力催化
			{ 1078, 1, 20, 5 },		//精神專注
			{ 1078, 2, 30, 10 },	//精神專注
			{ 1078, 3, 44, 20 },	//精神專注
			{ 1078, 4, 52, 30 },	//精神專注
			{ 1078, 5, 60, 40 },	//精神專注
			{ 1078, 6, 68, 50 },	//精神專注
			{ 1077, 1, 1, 10 },		//弱點偵測
			{ 1077, 2, 44, 30 },	//弱點偵測
			{ 1077, 3, 52, 50 },	//弱點偵測
			{ 1035, 1, 1, 10 },		//心靈防護
			{ 1035, 2, 40, 30 },	//心靈防護
			{ 1035, 3, 48, 50 },	//心靈防護
			{ 1035, 4, 56, 100 },	//心靈防護
			{ 1397, 1, 1, 10 },		//澄澈思緒
			{ 1397, 2, 66, 45 },	//澄澈思緒
			{ 1397, 3, 74, 80 },	//澄澈思緒
			{ 1268, 1, 1, 10 },		//吸血怒擊
			{ 1268, 2, 44, 50 },	//吸血怒擊
			{ 1268, 3, 58, 70 },	//吸血怒擊
			{ 1268, 4, 72, 120 },	//吸血怒擊
			{ 1062, 1, 1, 10 },		//狂戰士魂
			{ 1062, 2, 52, 50 },	//狂戰士魂
			{ 1044, 1, 35, 10 },	//強癒術
			{ 1044, 2, 48, 30 },	//強癒術
			{ 1044, 3, 56, 50 },	//強癒術
			{ 1045, 1, 44, 10 },	//神佑之體
			{ 1045, 2, 48, 30 },	//神佑之體
			{ 1045, 3, 52, 50 },	//神佑之體
			{ 1045, 4, 56, 100 },	//神佑之體
			{ 1045, 5, 64, 150 },	//神佑之體
			{ 1045, 6, 72, 200 },	//神佑之體
			//{ 1047, 1, 40, 5000 },	魔力再生
			//{ 1047, 2, 48, 7000 },	魔力再生
			//{ 1047, 3, 60, 20000 },	魔力再生
			//{ 1047, 4, 70, 35000 },	魔力再生
			//{ 1047, 5, 80, 50000 },	魔力再生
			{ 1048, 1, 44, 10 },	//神佑之魂
			{ 1048, 2, 48, 30 },	//神佑之魂
			{ 1048, 3, 52, 50 },	//神佑之魂
			{ 1048, 4, 56, 100 },	//神佑之魂
			{ 1048, 5, 62, 150 },	//神佑之魂
			{ 1048, 6, 70, 200 },	//神佑之魂
			{ 1087, 1, 25, 10 },	//敏捷術
			{ 1087, 2, 44, 40 },	//敏捷術
			//{ 1087, 3, 52, 50 },	敏捷術
			{ 1240, 1, 40, 10 },	//導引
			{ 1240, 2, 48, 30 },	//導引
			{ 1240, 3, 56, 50 },	//導引
			{ 1242, 1, 40, 10 },	//死之呢喃
			{ 1242, 2, 48, 30 },	//死之呢喃
			{ 1242, 3, 56, 50 },	//死之呢喃
			{ 1243, 1, 40, 10 },	//祝福之盾
			{ 1243, 2, 48, 30 },	//祝福之盾
			{ 1243, 3, 56, 50 },	//祝福之盾
			//{ 1243, 4, 62, 20000 },	祝福之盾
			//{ 1243, 5, 66, 20000 },	祝福之盾
			//{ 1243, 6, 70, 35000 },	祝福之盾
			{ 1259, 1, 40, 25 },	//抵抗休克
			{ 1259, 2, 52, 50 },	//抵抗休克
			{ 1259, 3, 64, 75 },	//抵抗休克
			{ 1259, 4, 72, 100 },	//抵抗休克
			{ 1303, 1, 1, 10 },		//野性魔力
			{ 1303, 2, 70, 100 },	//野性魔力
			//{ 1354, 1, 76, 500 },	//奧祕加護
			{ 1355, 1, 76, 1000 },	//水之預言
			//{ 1355, 2, 82, 50000 },	水之預言
			{ 1356, 1, 76, 1000 },	//火之預言
			//{ 1356, 2, 82, 50000 },	火之預言
			{ 1357, 1, 76, 1000 },	//風之預言
			//{ 1357, 2, 82, 50000 },	風之預言
			{ 1363, 1, 76, 1000 },	//凱旋頌歌
			//{ 1363, 2, 82, 50000 },	凱旋頌歌
			{ 1388, 1, 58, 100 },	//高級力量強化
			{ 1388, 2, 66, 150 },	//高級力量強化
			{ 1388, 3, 74, 200 },	//高級力量強化
			{ 1389, 1, 58, 100 },	//高級保護盾
			{ 1389, 2, 66, 150 },	//高級保護盾
			{ 1389, 3, 74, 200 },	//高級保護盾
			{ 1414, 1, 76, 1000 },	//帕格立歐必勝
			//{ 1414, 2, 76, 50000 },	帕格立歐必勝
			{ 1461, 1, 79, 1000 },	//保護頌歌
			{ 1542, 1, 80, 200 },	//反制爆擊
			//{ 826, 1, 49, 100 },	//釘鎚穿刺
			{ 1036, 1, 44, 30 },	//魔法屏障
			{ 1036, 2, 52, 50 },	//魔法屏障
	};
	public static final int[][] BUFFS_DANCE = { 
			{ 264, 1, 55, 500 },	//大地之歌
			{ 270, 1, 43, 500 },	//
			{ 366, 1, 77, 1000 },	//
			//{ 264, 2, 71, 500 },	大地之歌
			{ 265, 1, 52, 500 },	//生命之歌
			{ 266, 1, 58, 500 },	//水靈之歌
			//{ 266, 2, 73, 500 },	水靈之歌
			{ 267, 1, 40, 500 },	//護衛之歌
			//{ 267, 2, 61, 500 },	護衛之歌
			{ 268, 1, 46, 500 },	//風靈之歌
			{ 269, 1, 49, 500 },	//獵者之歌
			{ 271, 1, 55, 500 },	//戰士之舞
			//{ 271, 2, 71, 500 },	//戰士之舞
			{ 272, 1, 46, 500 },	//士氣之舞
			{ 273, 1, 49, 500 },	//神祕之舞
			//{ 273, 2, 69, 500 },	神祕之舞
			{ 274, 1, 40, 500 },	//火靈之舞
			//{ 274, 2, 61, 500 },	火靈之舞
			{ 275, 1, 58, 500 },	//狂暴之舞
			{ 276, 1, 52, 500 },	//專注之舞
			{ 304, 1, 55, 500 },	//勝利之歌
			{ 305, 1, 74, 500 },	//復仇之歌
			{ 310, 1, 74, 500 },	//吸血之舞
			{ 349, 1, 77, 500 },	//奮迅之歌
			{ 364, 1, 78, 1000 },	//鬥者之歌
			{ 365, 1, 78, 500 },	//艷惑之舞
			{ 915, 1, 80, 2000 },	//狂戰士之舞
	};

	//$action$ 指令
	//$icon$ 圖示
	//$name$ 名稱
	//$level$ 等級
	//$money$ 金幣
	private String StringTmp = "<table width=260 border=0><tr><td width=32 height=32 background=\"$icon$\"></td><td width=200><font color=00FF00>Lv$level$.</font> <font color=2A9EFF>$name$</font><br1><font color=LEVEL>$money$ 金幣</font></td><td width=40><button value=\"使用\" action=\"$action$\" width=40 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>";
	//$pgnum$ 編號
	//$pgact$ 指令
	private String button = "<td fixwidth=30><button value=\"$pgnum$\" action=\"$pgact$\" width=32 height=20 back=\"L2UI_CT1.ItemWindow_DF_Frame fore=L2UI_CT1.ItemWindow_DF_Frame\"></td>";
	//$name$技能名稱
	//$level$ 技能等級
	//$levelLock$ 幾級解鎖
	//$icon$ 圖示
	private String StringLock = "<table width=260 border=0><tr><td width=32 height=32 background=\"$icon$\"></td><td width=150>Lv-$level$. - $name$</td><td width=65>$levelLock$級時解鎖</td></tr></table>";
	private static Map<String, BuffTemplate> _buffTemplate = new HashMap<String, BuffTemplate>();
    static {
		for(int i = 0; i < BUFFS_MAGIC.length; i++)
		{
			BuffTemplate b = new BuffTemplate(BUFFS_MAGIC[i][0], BUFFS_MAGIC[i][1], BUFFS_MAGIC[i][2], BUFFS_MAGIC[i][3]);
			_buffTemplate.put(BUFFS_MAGIC[i][0] + "," + BUFFS_MAGIC[i][1], b);
		}
		for(int i = 0; i < BUFFS_DANCE.length; i++)
		{
			BuffTemplate b = new BuffTemplate(BUFFS_DANCE[i][0], BUFFS_DANCE[i][1], BUFFS_DANCE[i][2], BUFFS_DANCE[i][3]);
			_buffTemplate.put(BUFFS_DANCE[i][0] + "," + BUFFS_DANCE[i][1], b);
		}
    }
	public BuffInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final String[] buypassOptions = command.split(" ");
		
		if(command.startsWith("Chat"))
		{
			//bypass -h npc_%objectId%_Chat 1
			showChatWindow(player, "special/" + getNpcId() + "-" + buypassOptions[1] + ".htm", false);
		}
		//bypass -h npc?BuffMagic
		else if(buypassOptions[0].equals("BuffMagic"))
		{
			int Pages = 1;
			if(buypassOptions.length == 2)
			{
				Pages = Integer.parseInt(buypassOptions[1]);
			}
			SendBuffHtml(player, BUFFS_MAGIC, Pages, "輔助魔法清單", "bypass -h npc?BuffMagic ");
		}
		else if(buypassOptions[0].equals("BuffDance"))
		{
			int Pages = 1;
			if(buypassOptions.length == 2)
			{
				Pages = Integer.parseInt(buypassOptions[1]);
			}
			SendBuffHtml(player, BUFFS_DANCE, Pages, "輔助歌舞清單", "bypass -h npc?BuffDance ");
		}
		else if(buypassOptions[0].equals("BuffPlayer"))
		{
			//BuffPlayer 技能 等級  title
			if(buypassOptions.length == 5)
			{
				int skillId = Integer.parseInt(buypassOptions[1]);
				int skillLv = Integer.parseInt(buypassOptions[2]);
				if(_buffTemplate.containsKey(skillId+","+ skillLv))//判斷是否設置的id buff
				{
					int  Pages = Integer.parseInt(buypassOptions[4]);
					if(buypassOptions[3].startsWith("輔助魔法清單"))
					{
						if(CheckCanBuff(player, BUFFS_MAGIC, skillId, skillLv))
						{
							BuffSkill buff = BuffSkill.makeBuffSkill(skillId, skillLv, 1, -1, false);
							doBuff(player, buff);
							SendBuffHtml(player, BUFFS_MAGIC, Pages, "輔助魔法清單", "bypass -h npc?BuffMagic ");
						}
					}
					else
					{
						if(CheckCanBuff(player, BUFFS_DANCE, skillId, skillLv))
						{
							BuffSkill buff = BuffSkill.makeBuffSkill(skillId, skillLv, 1, -1, false);
							doBuff(player, buff);
							SendBuffHtml(player, BUFFS_DANCE, Pages, "輔助歌舞清單", "bypass -h npc?BuffDance ");
						}
					}
				}
			}
		}
		else if(buypassOptions[0].equals("GiveMpHpCp"))
		{
			String html = HtmCache.getInstance().getHtml("special/34071.htm", player);
			HtmlMessage htmlMessage = new HtmlMessage(5);
			htmlMessage.setHtml(html);
			int needMoneyHp = 1;//一血 換一金
			int needMoneyCp = 1;//一键回满
			int needMoneyMp = 3;//1MP 換3金
			int needAllHp = (int)(player.getMaxHp() - player.getCurrentHp());
			int needAllMp = (int)(player.getMaxMp() - player.getCurrentMp());
			int needAllCp = (int)(player.getMaxCp() - player.getCurrentCp());
			if (needAllHp==0 && needAllMp==0 && needAllCp==0 )
			{
				player.sendMessage("你的状态健康无需使用。");
				player.sendPacket(htmlMessage);
				return;
			}
			if(!ItemFunctions.deleteItem(player, 57, needAllHp*needMoneyHp+needMoneyCp*needAllCp+needMoneyMp *needAllMp))
			{
				player.sendMessage("金幣不足無法使用");
				player.sendPacket(htmlMessage);
				return;
			}
			player.setCurrentHp(player.getMaxHp(),false,true);
			player.setCurrentMp(player.getMaxMp(),true);
			player.setCurrentCp(player.getMaxCp());

			player.sendPacket(htmlMessage);
		}
		else if(buypassOptions[0].equals("myLockBuff"))
		{
			int Pages = 1;
			if(buypassOptions.length == 2)
			{
				Pages = Integer.parseInt(buypassOptions[1]);
			}			
			ShowHtmlLock(player,Pages);
		}
		else if(buypassOptions[0].equals("Suite"))
		{
			//這一個是 套裝服務主頁
			ShowHtmlSuite(player);
		}
		else if(buypassOptions[0].equals("CreateSuite"))//這一個是去資料庫找之前設置的套裝資料。
		{
			if(buypassOptions.length == 2)
			{
				if(buypassOptions[1].length()==0)
				{
					//沒有輸入名稱
					String errs = HtmCache.getInstance().getHtml("special/"+getNpcId()+"-3_Err_Empty.htm", player);
					HtmlMessage msg = new HtmlMessage(5);
					msg.setHtml(errs);
					player.sendPacket(msg);
					player.sendActionFailed();
				}else
				{
					ShowHtmlSuiteItem(player, buypassOptions[1]);
				}
			}
		}
		else if(buypassOptions[0].equals("AddBuffList"))
		{
			if(buypassOptions.length == 3)
			{
				if(checkSuiteSize(player))
				{
					addSuiteByName(player,buypassOptions[1],buypassOptions[2]);
					ShowHtmlSuite(player);//傳回主頁
				}else if (checkSuiteSize(player) == false){
					deleteSuiteByName(player,buypassOptions[1]);
					addSuiteByName(player,buypassOptions[1],buypassOptions[2]);
					ShowHtmlSuite(player);//傳回主頁
				}else{
					String html = HtmCache.getInstance().getHtml("special/"+getNpcId()+"-3_Err_Many.htm", player);
					HtmlMessage msg = new HtmlMessage(5);
					msg.setHtml(html);
					player.sendPacket(msg);
					player.sendActionFailed();		
				}
			}
			//SaveBuffList(player, buypassOptions[1].replaceAll("\\s", ""));
		}
		else if(buypassOptions[0].equals("CustomBuffList"))
		{
			if(buypassOptions.length == 2)
			{
				if(player.getLevel() > free)//這一區是免費的
				{
					int money = getMoneyCount(buypassOptions[1], player.getLevel());

					if(!ItemFunctions.deleteItem(player, 57, money))//花費需要乘上玩家等級
					{
						player.sendMessage("金幣不足無法使用");
						return;
					}
				}
				String[] buff = buypassOptions[1].split("#");
				
				for (int i = 0;i<buff.length;i++)
				{
					doBuff(player, _buffTemplate.get(buff[i]).getBuff());
				}
				ShowHtmlSuite(player);
			}
			//SaveBuffList(player, buypassOptions[1].replaceAll("\\s", ""));
		}
		//bypass -h npc?DeleteBuffList 名稱  1068,3#1040,3#1059,2#1086,1#1077,2#1078,3#272,1
		else if(buypassOptions[0].equals("DeleteShowBuffList"))
		{
			if(buypassOptions.length == 3)
			{
				String html = HtmCache.getInstance().getHtml("special/"+getNpcId()+"-3_ShowDeleteBuff.htm", player);
				String tmp = getHtmlByBuff(buypassOptions[2], player);
				html = html.replace("<$content$>", tmp);
				html = html.replace("<$name$>", buypassOptions[1]);			
				HtmlMessage msg = new HtmlMessage(5);
				msg.setHtml(html);
				player.sendPacket(msg);
				player.sendActionFailed();					
			}
		}
		else if(buypassOptions[0].equals("DeleteBuff"))
		{
			if(buypassOptions.length == 2)
			{
				deleteSuiteByName(player, buypassOptions[1] );
				String html = HtmCache.getInstance().getHtml("special/"+getNpcId()+"-3_DeleteOK.htm", player);
				html = html.replace("<$name$>", buypassOptions[1]);				
				HtmlMessage msg = new HtmlMessage(5);
				msg.setHtml(html);
				player.sendPacket(msg);
				player.sendActionFailed();	
			}
		}
		else if(buypassOptions[0].equals("clearAll")){
			AbnormalList abnormalList = player.getAbnormalList();
			if (abnormalList.isEmpty()) {
				return;
			}
			ArrayList<Abnormal> toExits = new ArrayList<>();
			abnormalList.forEach( e -> {
				loop:
				for (BuffTemplate buff : _buffTemplate.values()) {
					if (buff.getId()== e.getSkill().getId()) {
						toExits.add(e);
						e.exit();
						break loop;
					}
				}
			});
			if (toExits.isEmpty()) {
				player.sendMessage("當前沒有可清除的輔助狀態!");
			}
			showChatWindow(player, "special/" + getNpcId() + ".htm", false);
		}

	}
	private int maxSuite = 3;//這一個參數是限制了一個玩家最多能設置幾組預設buff
	private String suitebutton = "<table><tr><td align=\"center\" width=215><table width=215 bgcolor=\"1F4362\"><tr><td  bgcolor=\"0000FF\" fixwidth=200>套餐 <font color=B5B5B5>$value$</font>&nbsp;[<font color=FF0000>$size$</font>]&nbsp;<font color=LEVEL>$money$</font>金</td></tr></table></td><td align=\"center\" width=45><table><tr><td><button value=\"使用\" action=\"$act$\" width=45 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1><button value=\"刪除\" action=\"$delete$\" width=45 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></td><br></tr></table>";

	private void ShowHtmlSuite(Player player )
	{
		//這裡是第一次進來主畫面，只需要顯示出之前是否有設置幾樣參數輔助
		String html = HtmCache.getInstance().getHtml("special/"+getNpcId()+"-3.htm", player);
		//這裡需要找出資料庫裡面是否有設置的套裝行程。
		Map<String, String> allsute = getBuffSuite(player);
		if(allsute.size() > 0)
		{
			StringBuilder result = new StringBuilder();
			for (Map.Entry<String, String> entry : allsute.entrySet())			
			{
				int money = getMoneyCount(entry.getValue(), player.getLevel());
				int size = entry.getValue().split("#").length;
				String joinstr = suitebutton.replace("$value$", entry.getKey()) ;//$delete$
				joinstr = joinstr.replace("$act$", "bypass -h npc?CustomBuffList " + entry.getValue());				
				joinstr = joinstr.replace("$money$", String.valueOf(money));
				joinstr = joinstr.replace("$size$", String.valueOf(size));				
				joinstr = joinstr.replace("$delete$", "bypass -h npc?DeleteShowBuffList " + entry.getKey() + " " + entry.getValue());				
				result.append(joinstr);
			}
			html = html.replace("<$suites$>", result.toString());
		}
		else
		{
			html = html.replace("<$suites$>", "");
		}
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();		
	}

	private int getMoneyCount(String buffList,int playerLevel)
	{
		int sum = 0;
		String[] arr = buffList.split("#");
		for(int i = 0; i < arr.length; i++)
		{
			if(_buffTemplate.containsKey(arr[i]))
			{
				BuffTemplate b = _buffTemplate.get(arr[i]);
				sum += (b.getCost() * playerLevel);
			}
		}
		return sum;
	}
	private void ShowHtmlSuiteItem(Player player ,String SuiteName)
	{
		//先判斷是否這一組文字
		//創建 或是查看清單
		String value = getSuiteFromName(player, SuiteName);//先從資料庫找是否有這一個BUFF名稱
		String tmp = "";
		String list = value;
		if(value.length() > 1)
		{
			tmp = getHtmlByBuff(tmp, player);
		}
		else
		{
			tmp = getBuffListBySelf(player);
			if(tmp.length() == 0)
			{
				String errs = HtmCache.getInstance().getHtml("special/"+getNpcId()+"-3_Err.htm", player);
				HtmlMessage msg = new HtmlMessage(5);
				msg.setHtml(errs);
				player.sendPacket(msg);
				player.sendActionFailed();	
				return;
			}
			list = tmp;
			tmp = getHtmlByBuff(tmp, player);
		}
		//WitreFileTxt("aa.txt",tmp);
		String html = HtmCache.getInstance().getHtml("special/"+getNpcId()+"-3_ShowEdit.htm", player);
		html = html.replace("<$content$>", tmp);
		html = html.replace("<$name$>", SuiteName);
		html = html.replace("<$cmd$>", SuiteName + " " + list);
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();		
	}
	int tdcount = 2;
	//$icon$ 圖示
	//$name$ 名稱
	//$level$ 等級
	//$money$ 金幣
	private String StringHtmlByBuff = "<td width=32 height=32 background=\"$icon$\"></td><td width=90><font color=00FF00>Lv$level$. <font color=2A9EFF>$name$</font><br1><font color=LEVEL>$money$ 金幣</font></font></td>";
	private String getHtmlByBuff(String html,Player player)
	{
		String[] arr = html.split("#");
		
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < arr.length; i++)
		{
			String[] tmp = arr[i].split(",");
			int id = Integer.parseInt(tmp[0]);
			int level = Integer.parseInt(tmp[1]);
			BuffTemplate b = _buffTemplate.get(arr[i]);
			if(i % tdcount == 0)
			{
				result.append("<tr>");
			}
			result.append(StringHtmlByBuff.replace("$icon$","icon.skill"+ numberFormat.format(id)).replace("$level$", String.valueOf(level)).replace("$money$", String.valueOf(b.getCost() * player.getLevel())).replace("$name$", b.getBuff().getSkill().getName(player)));
			if(i % tdcount == (tdcount - 1))
			{
				result.append("</tr>");
			}
		}
		if((arr.length % tdcount) != 0)
		{
			int count = tdcount - arr.length;
			for(int i = 0; i < count; i++)
			{
				result.append("<td></td>");
			}
			result.append("</tr>");
		}
		return result.toString();		
	}

	private String getBuffListBySelf(Player player)
	{
		StringBuilder result = new StringBuilder();
		//Skill sk;
		for(Abnormal e : player.getAbnormalList())
		{
			String keys = e.getSkill().getId() + "," + e.getSkill().getLevel();
			if(_buffTemplate.containsKey(keys))
			{
				BuffTemplate b = _buffTemplate.get(keys);
				if((player.getLevel() >= b.getLimiteLv()) || (free >= player.getLevel())) //免費等級之類的判定
				{
					result.append(keys + "#");
					continue;
				}
			}
		}
		String str = result.toString();
		if(str.length() == 0)
			return "";
		return str.substring(0, str.length() - 1);
	}
	private int BuffSize = 30;//這一個參數指定一頁幾個BUFF
	private void ShowHtmlLock(Player player , int pages)
	{
		String html = HtmCache.getInstance().getHtml("special/"+getNpcId()+"-4.htm", player);		
		int[][] arr = new int[_buffTemplate.size()][4];
		int playerLv = player.getLevel();
		int sum = 0;
		for (BuffTemplate b : _buffTemplate.values())
		{
			if(b.getLimiteLv() > playerLv)
			{
				arr[sum][0] = b.getId();
				arr[sum][1] = b.getLevel();
				arr[sum][2] = b.getLimiteLv();
				arr[sum][3] = b.getCost();
				sum++;
			}
		}
		if(sum == 0)
		{
			//已完成所有解鎖。
			html = HtmCache.getInstance().getHtml("special/"+getNpcId()+"-4Full.htm", player);
			HtmlMessage msg = new HtmlMessage(5);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			return;
		}
		sum =  sum -1;
		int allPages = sum / BuffSize;
		if((sum % BuffSize) > 0)
		{
			allPages++;
		}
		if(pages > allPages)
		{
			pages = allPages;
		}
		int startNum =  BuffSize * (pages -1);//起始數字
		int endNum = Math.min(BuffSize*pages-1, sum);//結束數字		
		StringBuilder result = new StringBuilder();
		for(int j = startNum; j <= endNum; j++)
		{
			//$name$技能名稱
			//$level$ 技能等級
			//$levelLock$ 幾級解鎖
			//$icon$ 圖示
			String str = StringLock.replace("$name$", SkillHolder.getInstance().getSkill(arr[j][0], 1).getName(player));
			str = str.replace("$level$", String.valueOf(arr[j][1]));
			str = str.replace("$levelLock$",  String.valueOf(arr[j][2]));
			str = str.replace("$icon$", "icon.skill" + numberFormat.format(arr[j][0]) );
			result.append(str);
		}
		html = html.replace("<$packMoney$>", String.valueOf(player.getInventory().getAdena()));
		html = html.replace("<$content$>", result.toString());		
		String Nav = "";
		for (int i = 1 ;i<= allPages;i++)
		{
			Nav += button.replace("$pgnum$", String.valueOf(i)).replace("$pgact$", "bypass -h npc?myLockBuff " + i);
		}
		html = html.replace("<$Nav$>", Nav);
		
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();		
	}
	private boolean CheckCanBuff(Player player, int[][] BUFFS, int skill,int level )
	{
		for(int i = 0; i < BUFFS.length; i++)
		{
			String keys = skill + "," + level;
			if(_buffTemplate.containsKey(keys))
			{
				if(free >= player.getLevel())//這一區是免費的
				{
					return true;
				}
				BuffTemplate b = _buffTemplate.get(keys);
				if(player.getLevel() < b.getLimiteLv())
				{
					player.sendMessage("等級過低無法使用");
					return false;
				}
				if(!ItemFunctions.deleteItem(player, 57, b.getCost() * player.getLevel()))//花費需要乘上玩家等級
				{
					player.sendMessage("金幣不足無法使用");
					return false;
				}
				return true;
			}
		}
		return false;
	}
	private static void doBuff(Player player, BuffSkill buffs)
	{
		if(_buffTemplate.containsKey(buffs.getId() + "," + buffs.getLevel()))//最終確認是否為可使用的buff
		{
			ThreadPoolManager.getInstance().execute(() -> 
			{
				boolean success = false;

				buffs.getSkill().getEffects(player, player, buffs.getTimeAssign() * 60 * 1000, buffs.getTimeModifier());

				success = true;
				try
				{
					Thread.sleep(20L);
				}
				catch (Exception e)
				{
				}
				if(success)
					player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
			});
		}
	}
	DecimalFormat numberFormat = new DecimalFormat("0000");
	private int listSize = 8;//這一個參數指定一頁幾個BUFF
	private int free = 20;//這一個是設置等級，多少級的可以免費給buff 包含20級
	private void SendBuffHtml(Player player, int[][] BUFFS, int Pages,String title ,String order )
	{
		String html = HtmCache.getInstance().getHtml("special/"+getNpcId()+"Tmp.htm", player);

		int[][] arr = new int[100][4];
		int playerLv = player.getLevel();
		int sum = 0;
		for(int i = 0; i < BUFFS.length; i++)
		{
			if(BUFFS[i][2] <= playerLv)//判斷技能是否小於玩家本身等級，才可以使用
			{
				if(sum == 0)
				{
					for(int j = 0; j < 4; j++)
					{
						arr[sum][j] = BUFFS[i][j];
					}
					sum++;
				}
				else
				{
					if(BUFFS[i][0] == arr[sum - 1][0])//比較技能編號是否一樣 一樣的話取代成較高的等級
					{
						for(int j = 0; j < 4; j++)
						{
							arr[sum - 1][j] = BUFFS[i][j];
						}
					}
					else
					{
						for(int j = 0; j < 4; j++)
						{
							arr[sum][j] = BUFFS[i][j];
						}
						sum++;
					}
				}
			}
		}
		if(sum == 0)//當沒有所需要的技能表示等級不夠需要給他一個請升級後再來
		{
			//已完成所有解鎖。
			html = HtmCache.getInstance().getHtml("special/"+getNpcId()+"-4-NeedLevelUp.htm", player);
			HtmlMessage msg = new HtmlMessage(5);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			return;
		}
		int allPages = sum / listSize;
		if((sum % listSize) > 0)
		{
			allPages++;
		}
		if(Pages > allPages)
		{
			Pages = allPages;
		}
		int startNum = listSize * (Pages - 1);//起始數字
		int endNum = Math.min(listSize * Pages - 1, sum - 1);//結束數字
		StringBuilder result = new StringBuilder();
		for(int j = startNum; j <= endNum; j++)
		{
			//$action$ 指令
			//$icon$ 圖示
			//$name$ 名稱
			//$level$ 等級
			//$money$ 金幣
			//bypass -h npc?BuffPlayer 技能 等級  title 頁數
			String str = StringTmp.replace("$action$", "bypass -h npc?BuffPlayer " + arr[j][0] + " " + arr[j][1] + " " + title + " " + Pages);

			str = str.replace("$icon$", "icon.skill" + numberFormat.format(arr[j][0]));
			str = str.replace("$name$", SkillHolder.getInstance().getSkill(arr[j][0], 1).getName(player));
			str = str.replace("$level$", String.valueOf(arr[j][1]));
			str = str.replace("$money$", String.valueOf(arr[j][3] * player.getLevel()));
			result.append(str);
		}

		html = html.replace("<$packMoney$>", String.valueOf(player.getInventory().getAdena()));
		html = html.replace("<$content$>", result.toString());
		html = html.replace("<$title$>", title);
		String Nav = "";
		for(int i = 1; i <= allPages; i++)
		{
			Nav += button.replace("$pgnum$", String.valueOf(i)).replace("$pgact$", order + i);
		}
		html = html.replace("<$Nav$>", Nav);
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();	
	}
	//String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/buff.htm", player);
	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		//這一區是第一次對話時該出現的對話框區域
		if(val == 0)
		{
			showChatWindow(player, "special/" + getNpcId() + ".htm", firstTalk);
		}
		else
		{
			showChatWindow(player, "special/" + getNpcId() + "-" + val + ".htm", firstTalk);
		}
	}
	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == 0)//	1
		{
			if(reply == 1 || reply == 2 || reply == 3 || reply == 8)
			{
				//showHtml(player, stone[ask], (int) reply);
			}
		}
		return;
	}

	private int  getCountSuite(Player player,String Name)
	{		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int lv =0 ;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM _buff_suite where object_id = ? ");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			if(rset.next())
			lv = rset.getInt("ensoul_id");
			statement.close();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}		
		return lv;
	}

	private Map<String, String> getBuffSuite(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		Map<String, String> items = new HashMap<String, String>();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT name ,value  FROM _buff_suite where obj_Id = ? ");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while (rset.next())
			{
				items.put(rset.getString("name"), rset.getString("value"));
			}
			statement.close();
		}
		catch (Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return items;
	}

	private String  getSuiteFromName(Player player,String Name)
	{		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		String value  ="" ;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT value FROM _buff_suite where obj_Id = ? and name = ? ");
			statement.setInt(1, player.getObjectId());
			statement.setString(1, Name);
			rset = statement.executeQuery();
			if(rset.next())
				value = rset.getString("value");
			statement.close();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}		
		return value;
	}

	private boolean checkSuiteSize(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int count = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT count(*) as cnt  FROM _buff_suite where obj_Id = ? ");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			if(rset.next())
				count = rset.getInt("cnt");
			statement.close();
		}
		catch (Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return count < maxSuite;
	}

	private String  addSuiteByName(Player player,String Name ,String Value)
	{		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		String value  ="" ;
		try
		{
			//REPLACE INTO epic_boss_spawn (bossId,respawnDate,state) VALUES(?,?,?)
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO _buff_suite (obj_Id,name,value) VALUES(?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, Name);
			statement.setString(3, Value);
			statement.execute();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}		
		return value;
	}
	
	private String deleteSuiteByName(Player player,String Name)
	{		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		String value  ="" ;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM _buff_suite where obj_Id = ? and name =? ");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, Name);
			statement.execute();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}		
		return value;
	}
	private void WitreFileTxt(String WriteFileName,String WiteTxt )
	{
		try
		{ // 防止檔案建立或讀取失敗，用catch捕捉錯誤並列印，也可以throw
			/* 寫入Txt檔案 */
			File writename = new File(WriteFileName); // 相對路徑，如果沒有則要建立一個新的output。txt檔案
			writename.createNewFile(); // 建立新檔案
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			out.write(WiteTxt); // \r\n即為換行
			out.flush(); // 把快取區內容壓入檔案
			out.close(); // 最後記得關閉檔案
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static class BuffTemplate
	{
		private final int _id;
		private final int _level;
		private final int _limiteLv;
		private final int _cost;
		private final BuffSkill _buff ;
		public BuffTemplate(int id, int level, int limitelv, int cost)
		{
			_id = id;
			_level = level;
			_limiteLv = limitelv;
			_cost = cost;
			_buff = BuffSkill.makeBuffSkill(_id, _level, 1, -1, false);
		}
	
		public int getId()
		{
			return _id;
		}

		public int getLevel()
		{
			return _level;
		}

		public int getLimiteLv()
		{
			return _limiteLv;
		}

		public int getCost()
		{
			return _cost;
		}
		
		public BuffSkill getBuff()
		{
			return _buff;
		}
	}
}
