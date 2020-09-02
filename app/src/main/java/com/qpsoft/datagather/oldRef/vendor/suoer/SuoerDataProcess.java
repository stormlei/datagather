package com.qpsoft.datagather.oldRef.vendor.suoer;


import com.qpsoft.datagather.oldRef.RefractionData;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modify:
 * caotian caotian@qq.com 2018-12-12 14:27
 * Description:
 */
public class SuoerDataProcess {
    public static RefractionData parse(List<String> dataList) {
        if(dataList.size() == 0){
            return null;
        }

        int index = 0;
        RefractionData refractionData = new RefractionData();
        RefractionData.EyeData od = new RefractionData.EyeData();
        RefractionData.EyeData os = new RefractionData.EyeData();

        for(String str : dataList){
            //第2行提取时间
            if(index == 2){
                String dateTime = str.replace("Time:", "");
                refractionData.setDate(dateTime);
            }

            //第5行提取OD
            if(index == 6){
                Pattern ds = Pattern.compile("[+-]?[0-9]+(\\.[0-9]+)?");
                Matcher matcher = ds.matcher(str);

                String result = "";
                List<String> odData = new ArrayList<>();
                while (matcher.find()){
                    odData.add(matcher.group());
                }
                if(odData.size() > 0){
                    od.setS(odData.get(0));
                }
                if(odData.size() > 1){
                    od.setC(odData.get(1));
                }
                if(odData.size() > 2){
                    od.setA(odData.get(2));
                }
            }

            //第7行提取OD的SE
            if(index == 7){
                String se = str.replace("SE:   ", "");
                od.setSe(se);
            }

            //第8行提取OD的PS
            if(index == 8){
                String ps = str.replace("PS:", "");
                od.setPs(ps);
            }

            //第9行提取OD的GV
            if(index == 9){
                Pattern pattern = Pattern.compile("[0-9]+([.]{1}[0-9]+){0,1}");
                Matcher matcher = pattern.matcher(str);
                List<String> odGv = new ArrayList<>();
                while (matcher.find()){
                    odGv.add(matcher.group());
                }

                String gv = odGv.size() > 0 ? odGv.get(0) : "";

                if(str.contains("Down")){
                    gv = "+" + gv;
                }else{
                    gv = "-" + gv;
                }

                od.setHeterV(gv);
            }

            //第10行提取OD的GH
            if(index == 10){
                Pattern pattern = Pattern.compile("[0-9]+([.]{1}[0-9]+){0,1}");
                Matcher matcher = pattern.matcher(str);
                List<String> odGh = new ArrayList<>();
                while (matcher.find()){
                    odGh.add(matcher.group());
                }

                String gh = odGh.size() > 0 ? odGh.get(0) : "";

                if(str.contains("Nasal")){
                    gh = "-" + gh;
                }else{
                    gh = "+" + gh;
                }

                od.setHeterH(gh);
            }

            //第13行提取OS
            if(index == 13){
                Pattern ds = Pattern.compile("[+-]?[0-9]+(\\.[0-9]+)?");
                Matcher matcher = ds.matcher(str);

                String result = "";
                List<String> osData = new ArrayList<>();
                while (matcher.find()){
                    osData.add(matcher.group());
                }

                if(osData.size() > 0){
                    os.setS(osData.get(0));
                }
                if(osData.size() > 1){
                    os.setC(osData.get(1));
                }
                if(osData.size() > 2){
                    os.setA(osData.get(2));
                }
            }

            //第14行提取OS的SE
            if(index == 14){
                String se = str.replace("SE:   ", "");
                os.setSe(se);
            }

            //第15行提取OS的PS
            if(index == 15){
                String ps = str.replace("PS:", "");
                os.setPs(ps);
            }

            //第16行提取OS的GV
            if(index == 16){
                Pattern pattern = Pattern.compile("[0-9]+([.]{1}[0-9]+){0,1}");
                Matcher matcher = pattern.matcher(str);
                List<String> osGv = new ArrayList<>();
                while (matcher.find()){
                    osGv.add(matcher.group());
                }

                String gv = osGv.size() > 0 ? osGv.get(0) : "";

                if(str.contains("Down")){
                    gv = "+" + gv;
                }else{
                    gv = "-" + gv;
                }

                os.setHeterV(gv);
            }

            //第17行提取OS的GH
            if(index == 17){
                Pattern pattern = Pattern.compile("[0-9]+([.]{1}[0-9]+){0,1}");
                Matcher matcher = pattern.matcher(str);
                List<String> osGh = new ArrayList<>();
                while (matcher.find()){
                    osGh.add(matcher.group());
                }

                String gh = osGh.size() > 0 ? osGh.get(0) : "";

                if(str.contains("Nasal")){
                    gh = "+" + gh;
                }else{
                    gh = "-" + gh;
                }

                os.setHeterH(gh);
            }

            //第19行提取PD
            if(index == 19){
                String pd = str.replace("PD:", "");
                refractionData.setPd(pd);
            }

            index++;
        }
        refractionData.setOd(od);
        refractionData.setOs(os);

        return refractionData;
    }
}
