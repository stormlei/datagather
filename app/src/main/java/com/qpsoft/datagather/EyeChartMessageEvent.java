package com.qpsoft.datagather;

public class EyeChartMessageEvent {
    private EyeChartData eyeChartData;
    private String sn;

    public EyeChartMessageEvent(EyeChartData eyeChartData, String sn) {
        this.eyeChartData = eyeChartData;
        this.sn = sn;
    }

    public EyeChartData getEyeChartData() {
        return eyeChartData;
    }

    public void setEyeChartData(EyeChartData eyeChartData) {
        this.eyeChartData = eyeChartData;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
