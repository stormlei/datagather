package com.qpsoft.datagather;

import java.io.Serializable;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:07
 * Description: 抽象的验光仪数据, 目前只有
 */
public class EyeChartData implements Serializable {
    public String vision_od;
    public String vision_os;
    public String glass_od;
    public String glass_os;

    public String getVision_od() {
        return vision_od;
    }

    public void setVision_od(String vision_od) {
        this.vision_od = vision_od;
    }

    public String getVision_os() {
        return vision_os;
    }

    public void setVision_os(String vision_os) {
        this.vision_os = vision_os;
    }

    public String getGlass_od() {
        return glass_od;
    }

    public void setGlass_od(String glass_od) {
        this.glass_od = glass_od;
    }

    public String getGlass_os() {
        return glass_os;
    }

    public void setGlass_os(String glass_os) {
        this.glass_os = glass_os;
    }


    @Override
    public String toString() {
        return "EyeChartData{" +
                "vision_od='" + vision_od + '\'' +
                ", vision_os='" + vision_os + '\'' +
                ", glass_od='" + glass_od + '\'' +
                ", glass_os='" + glass_os + '\'' +
                '}';
    }
}
