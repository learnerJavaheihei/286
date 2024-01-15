package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.CostumesMulCollectHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CostumesMulCollectParser extends AbstractParser<CostumesMulCollectHolder> {

    private static final CostumesMulCollectParser _instance = new CostumesMulCollectParser();

    public static CostumesMulCollectParser getInstance()
    {
        return _instance;
    }

    protected CostumesMulCollectParser() {
        super(CostumesMulCollectHolder.getInstance());
    }

    @Override
    public File getXMLPath() {
        return new File(Config.DATAPACK_ROOT, "data/costumes_mul_collect.xml");
    }

    @Override
    public String getDTDFileName() {
        return "costumes_mul_collect.dtd";
    }

    @Override
    protected void readData(Element rootElement) throws Exception {
        for (Element element : rootElement.elements()) {
            if("costumes_suit_list".equals(element.getName())){
                for (Iterator<Element> suitIterator = element.elementIterator("suit"); suitIterator.hasNext(); ) {
                    CostumesMulCollectHolder.CostumesSuitList costumesSuitList = CostumesMulCollectHolder.getInstance().new CostumesSuitList();
                    Element suit = suitIterator.next();
                    int num = parseInt(suit, "num");
                    costumesSuitList.setSuitNum(num);
                    List<Integer> costumeIds = new ArrayList<>();
                    for (Iterator<Element> costumeIdIterator = suit.elementIterator("costumeId"); costumeIdIterator.hasNext(); ) {
                        String stringValue = costumeIdIterator.next().getStringValue();
                        costumeIds.add(Integer.parseInt(stringValue));
                    }
                    costumesSuitList.setCostumeId(costumeIds);
                    getHolder().addCostumesSuitLists(costumesSuitList);
                }
            }
            else if("mul_collect".equals(element.getName())){
                CostumesMulCollectHolder.CostumesMulCollect costumesMulCollect = CostumesMulCollectHolder.getInstance().new CostumesMulCollect();
                costumesMulCollect.setMulCollectId(parseInt(element,"id"));
                for (Iterator<Element> skillListIterator = element.elementIterator("skillList"); skillListIterator.hasNext(); ) {
                    List<Skill> skills = new ArrayList<>();
                    for (Iterator<Element> skillIterator = skillListIterator.next().elementIterator("skill"); skillIterator.hasNext(); ) {
                        Element skillElement = skillIterator.next();
                        int skillId = parseInt(skillElement, "id");
                        int levelId = parseInt(skillElement, "level");
                        Skill skill = SkillHolder.getInstance().getSkill(skillId, levelId);
                        if (skill==null) {
                            warn("costumes_mul_collect parser skillId "+skillId+" wrong!");
                            continue;
                        }
                        skills.add(skill);
                    }
                    costumesMulCollect.setSkills(skills);
                }
                getHolder().addCostumesMulCollects(costumesMulCollect);
            }
        }
    }
}
