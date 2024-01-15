package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Skill;

import java.util.ArrayList;
import java.util.List;

public class CostumesMulCollectHolder extends AbstractHolder {

    private static CostumesMulCollectHolder _instance = new CostumesMulCollectHolder();
    public static CostumesMulCollectHolder getInstance() {
        return _instance;
    }
    private static List<CostumesMulCollect> costumesMulCollects = new ArrayList<>();
    private static List<CostumesSuitList> costumesSuitList = new ArrayList<>();
    public void addCostumesMulCollects(CostumesMulCollect costumesMulCollect){
        costumesMulCollects.add(costumesMulCollect);
    }

    public void addCostumesSuitLists(CostumesSuitList csl){
        costumesSuitList.add(csl);
    }
    private CostumesMulCollectHolder() {
        //
    }
    public List<CostumesMulCollect> getCostumesMulCollects(){
        return costumesMulCollects;
    }
    public List<CostumesSuitList> getCostumesSuitLists(){
        return costumesSuitList;
    }

    public class CostumesMulCollect{
        Integer mulCollectId;
        List<Skill> skills;
        public CostumesMulCollect(){};
        public CostumesMulCollect(Integer mulCollectId, List<Skill> skills) {
            this.mulCollectId = mulCollectId;
            this.skills = skills;
        }

        public List<Skill> getSkills() {
            return skills;
        }

        public void setSkills(List<Skill> skills) {
            this.skills = skills;
        }
        public Integer getMulCollectId() {
            return mulCollectId;
        }

        public void setMulCollectId(Integer mulCollectId) {
            this.mulCollectId = mulCollectId;
        }
    }

    public class CostumesSuitList{
        Integer suitNum;
        List<Integer> costumeId;
        public Integer getSuitNum() {
            return suitNum;
        }

        public void setSuitNum(Integer suitNum) {
            this.suitNum = suitNum;
        }

        public List<Integer> getCostumeId() {
            return costumeId;
        }

        public void setCostumeId(List<Integer> costumeId) {
            this.costumeId = costumeId;
        }
        public CostumesSuitList(){}
        public CostumesSuitList(Integer suitNum, List<Integer> costumeId) {
            this.suitNum = suitNum;
            this.costumeId = costumeId;
        }
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {

    }
}
