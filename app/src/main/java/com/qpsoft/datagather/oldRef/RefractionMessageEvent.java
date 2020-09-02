package com.qpsoft.datagather.oldRef;


public class RefractionMessageEvent {
    private RefractionData refractionData;
    private String sn;

    public RefractionMessageEvent(RefractionData refractionData, String sn) {
        this.refractionData = refractionData;
        this.sn = sn;
    }

    public RefractionData getRefractionData() {
        return refractionData;
    }

    public void setRefractionData(RefractionData refractionData) {
        this.refractionData = refractionData;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
