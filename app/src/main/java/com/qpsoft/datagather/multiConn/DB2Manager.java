package com.qpsoft.datagather.multiConn;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.qpsoft.datagather.RefData;

public class DB2Manager {

    private Context mContext;
    private SQLiteDatabase mDB;
    private static String dbPath;
    private static String sn;
    private static DB2Manager instance = null;


    public DB2Manager() {
    }

    public static DB2Manager getInstance(String dbPathStr, String snStr) {
        if (instance == null) {
            instance = new DB2Manager();
        }
        dbPath = dbPathStr;
        sn = snStr;
        return instance;
    }

    /**
     * 打开数据库
     */
    private void openDB() {
        if (mDB == null || !mDB.isOpen())
            mDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }


    private int tempId;

    //查询选择题
    public RefData queryRefData() {
        RefData refData = new RefData();
        openDB();
        try {
            String sql = "select a.id,a.timestamp,a.f_odds,a.f_oddc,a.f_odaxis,a.f_odse,a.f_osds,a.f_osdc,a.f_osaxis,a.f_osse,f_interpupil from results a order by id desc limit 0,1";
            Cursor cursor = mDB.rawQuery(sql, null);
            if (cursor.moveToLast()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
                String ods = cursor.getString(cursor.getColumnIndex("f_odds"));
                String odc = cursor.getString(cursor.getColumnIndex("f_oddc"));
                String oda = cursor.getString(cursor.getColumnIndex("f_odaxis"));
                String odse = cursor.getString(cursor.getColumnIndex("f_odse"));
                String oss = cursor.getString(cursor.getColumnIndex("f_osds"));
                String osc = cursor.getString(cursor.getColumnIndex("f_osdc"));
                String osa = cursor.getString(cursor.getColumnIndex("f_osaxis"));
                String osse = cursor.getString(cursor.getColumnIndex("f_osse"));
                String pd = cursor.getString(cursor.getColumnIndex("f_interpupil"));

                refData.setId(id);
                refData.setTimestamp(timestamp);
                RefData.EyeData r = new RefData.EyeData();
                r.setS(ods);
                r.setC(odc);
                if (!TextUtils.isEmpty(oda)) {
                    oda = oda.replace("@", "");
                    oda = oda.replace("°", "");
                }
                r.setA(oda);
                r.setSe(odse);
                refData.setOd(r);
                RefData.EyeData l = new RefData.EyeData();
                l.setS(oss);
                l.setC(osc);
                if (!TextUtils.isEmpty(osa)) {
                    osa = osa.replace("@", "");
                    osa = osa.replace("°", "");
                }
                l.setA(osa);
                l.setSe(osse);
                refData.setOs(l);
                refData.setPd(pd);

            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDB.close();

        String used = CacheDiskStaticUtils.getString(sn);
        int refId = refData.getId();
        if (tempId == refId && "1".equals(used)) {
            return null;
        }
        tempId = refId;
        CacheDiskStaticUtils.put(sn, "0");
        return refData;
    }
}
