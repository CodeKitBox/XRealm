package com.kits.xrealm.bean;

import java.util.List;

import io.realm.RealmAny;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public class Boy extends RealmObject {
    @PrimaryKey
    private String number;
    private RealmAny test1;
    private RealmList<Float> test2;


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public RealmAny getTest1() {
        return test1;
    }

    public void setTest1(RealmAny test1) {
        this.test1 = test1;
    }

    public RealmList<Float> getTest2() {
        return test2;
    }

    public void setTest2(RealmList<Float> test2) {
        this.test2 = test2;
    }
}
