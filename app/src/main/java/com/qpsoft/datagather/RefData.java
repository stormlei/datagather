package com.qpsoft.datagather;

import java.io.Serializable;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:07
 * Description: 抽象的验光仪数据, 目前只有
 */
public class RefData implements Serializable {
    public EyeData od = new EyeData();
    public EyeData os = new EyeData();
    public String pd = "";
    public int id;
    public String timestamp = "";

    public static class EyeData implements Serializable {
        //S球镜
        public String s;
        //C柱镜
        public String c;
        //A轴位
        public String a;
        //A轴位
        public String se;

        @Override
        public String toString() {
            return "EyeData{" +
                    "s='" + s + '\'' +
                    ", c='" + c + '\'' +
                    ", a='" + a + '\'' +
                    ", se='" + se + '\'' +
                    '}';
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getSe() {
            return se;
        }

        public void setSe(String se) {
            this.se = se;
        }
    }

    @Override
    public String toString() {
        return "RefData{" +
                "od=" + od +
                ", os=" + os +
                ", pd='" + pd + '\'' +
                '}';
    }

    public EyeData getOd() {
        return od;
    }

    public void setOd(EyeData od) {
        this.od = od;
    }

    public EyeData getOs() {
        return os;
    }

    public void setOs(EyeData os) {
        this.os = os;
    }

    public String getPd() {
        return pd;
    }

    public void setPd(String pd) {
        this.pd = pd;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
