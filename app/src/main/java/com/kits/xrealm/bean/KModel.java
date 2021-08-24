package com.kits.xrealm.bean;

import io.realm.RealmModel;

public class KModel implements RealmModel {
    private String testA;
    private String testB;

    public String getTestA() {
        return testA;
    }

    public void setTestA(String testA) {
        this.testA = testA;
    }

    public String getTestB() {
        return testB;
    }

    public void setTestB(String testB) {
        this.testB = testB;
    }
}
