package com.androidex.face.utils;

/**
 * Created by cts on 17/4/7.
 */

public class CardInfo {
    public String name;
    public String imgPic;
    public String sex;
    public String nation;
    public String birthday;
    public String address;
    public String idnum;
    public String head;

    public CardInfo() {
    }

    /**
     * db.execSQL("create table cardinfo(" +
     * "_id integer primary key autoincrement," +
     * "name text," +
     * "imgPic text" +
     * "sex text" +
     * "nation text" +
     * "birthday text" +
     * "address text" +
     * "idnum text"
     * );
     */
    public CardInfo(String name, String imgPic, String sex, String nation, String birthday, String address, String idnum, String head) {
        this.name = name;
        this.imgPic = imgPic;
        this.sex = sex;
        this.nation = nation;
        this.birthday = birthday;
        this.address = address;
        this.idnum = idnum;
        this.head = head;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgPic() {
        return imgPic;
    }

    public void setImgPic(String imgPic) {
        this.imgPic = imgPic;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdnum() {
        return idnum;
    }

    public void setIdnum(String idnum) {
        this.idnum = idnum;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    @Override
    public String toString() {
        return "CardInfo{" +
                "name='" + name + '\'' +
                ", imgPic='" + imgPic + '\'' +
                ", sex='" + sex + '\'' +
                ", nation='" + nation + '\'' +
                ", birthday='" + birthday + '\'' +
                ", address='" + address + '\'' +
                ", idnum='" + idnum + '\'' +
                ", head='" + head + '\'' +
                '}';
    }
}
