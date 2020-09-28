package com.qpsoft.datagather.multiConn;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.LogUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DataGatherUtils {

    public static List<HoldDevice> getHoldDeviceList() {
        List<HoldDevice> holdDeviceList = new ArrayList<>();
        org.json.JSONArray saveJsonArray = CacheDiskStaticUtils.getJSONArray("holdDeviceArray");
        LogUtils.e("------------"+saveJsonArray);
        if (saveJsonArray != null) {
            for (int i = 0; i < saveJsonArray.length(); i++) {
                try {
                    holdDeviceList.add(JSON.parseObject(saveJsonArray.getString(i), HoldDevice.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
        return holdDeviceList;
    }

    public static boolean addHoldDevice(HoldDevice holdDevice) {
        try {
            org.json.JSONObject jsonObj = new org.json.JSONObject(JSON.toJSONString(holdDevice));
            org.json.JSONArray saveJsonArray = CacheDiskStaticUtils.getJSONArray("holdDeviceArray");
            if (saveJsonArray != null && saveJsonArray.length() > 0) {
                for (int i = 0; i < saveJsonArray.length(); i++) {
                    HoldDevice saveHoldDevice = JSON.parseObject(saveJsonArray.getString(i), HoldDevice.class);
                    if (holdDevice.getDeviceType() == HoldDeviceType.Suo) {
                        if (saveHoldDevice.getDeviceType() == holdDevice.getDeviceType()) {
                            return false;
                        }
                    } else if (holdDevice.getDeviceType() == HoldDeviceType.Wel) {
                        if (saveHoldDevice.getDeviceType() == holdDevice.getDeviceType() && saveHoldDevice.getIp().equals(holdDevice.getIp())) {
                            return false;
                        }
                    } else if (holdDevice.getDeviceType() == HoldDeviceType.EyeChart) {
                        if (saveHoldDevice.getDeviceType() == holdDevice.getDeviceType()) {
                            return false;
                        }
                    }
                }
                saveJsonArray.put(jsonObj);
                CacheDiskStaticUtils.put("holdDeviceArray", saveJsonArray);
            } else {
                org.json.JSONArray jsonArray = new org.json.JSONArray();
                jsonArray.put(jsonObj);
                CacheDiskStaticUtils.put("holdDeviceArray", jsonArray);
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean editHoldDevice(HoldDevice holdDevice) {
        try {
            org.json.JSONObject jsonObj = new org.json.JSONObject(JSON.toJSONString(holdDevice));
            org.json.JSONArray saveJsonArray = CacheDiskStaticUtils.getJSONArray("holdDeviceArray");
            if (saveJsonArray != null) {
                for (int i = 0; i < saveJsonArray.length(); i++) {
                    HoldDevice saveHoldDevice = JSON.parseObject(saveJsonArray.getString(i), HoldDevice.class);
                    if (saveHoldDevice.getSn().equals(holdDevice.getSn())) {
                        saveJsonArray.put(i, jsonObj);
                        CacheDiskStaticUtils.put("holdDeviceArray", saveJsonArray);
                        return true;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void delHoldDevice(HoldDevice holdDevice) {
        try {
            org.json.JSONArray saveJsonArray = CacheDiskStaticUtils.getJSONArray("holdDeviceArray");
            if (saveJsonArray != null) {
                for (int i = 0; i < saveJsonArray.length(); i++) {
                    HoldDevice saveHoldDevice = JSON.parseObject(saveJsonArray.getString(i), HoldDevice.class);
                    if (saveHoldDevice.getDeviceType() == holdDevice.getDeviceType() && saveHoldDevice.getIp().equals(holdDevice.getIp())) {
                        saveJsonArray.remove(i);
                        CacheDiskStaticUtils.put("holdDeviceArray", saveJsonArray);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
