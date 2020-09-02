package com.qpsoft.datagather.oldRef;

/**
 * Modify:
 * caotian caotian@qq.com 2018-12-09 13:50
 * Description:
 */
public class RefractionData {
    private String ageRange;
    private EyeData od;
    private EyeData os;

    private String pd;
    private String date;


    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "RefData{" +
                "od=" + od +
                ", os=" + os +
                ", pd='" + pd + '\'' +
                '}';
    }

    public static class EyeData {
        //球镜
        private String s;
        //柱镜
        private String c;
        //轴位
        private String a;
        //SE
        private String se;
        //瞳孔大小
        private String ps;

        //斜视(水平)
        private String heterH;
        //斜视(垂直)
        private String heterV;

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

        public String getPs() {
            return ps;
        }

        public void setPs(String ps) {
            this.ps = ps;
        }

        public String getHeterH() {
            return heterH;
        }

        public void setHeterH(String heterH) {
            this.heterH = heterH;
        }

        public String getHeterV() {
            return heterV;
        }

        public void setHeterV(String heterV) {
            this.heterV = heterV;
        }

        @Override
        public String toString() {
            return "EyeData{" +
                    "s='" + s + '\'' +
                    ", c='" + c + '\'' +
                    ", a='" + a + '\'' +
                    ", se='" + se + '\'' +
                    '}';
        }
    }

}
