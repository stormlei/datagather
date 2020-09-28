package com.qpsoft.datagather.multiConn;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qpsoft.datagather.AppConfig;
import com.qpsoft.datagather.EyeChartData;
import com.qpsoft.datagather.EyeChartMessageEvent;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;


public class TempServer {

    private AsyncHttpServer server = new AsyncHttpServer();
    private AsyncServer mAsyncServer = new AsyncServer();

    public void init() {
        server.post("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                try {
                    String userName = "test";
                    String className = "test";
                    String resultData = "{\"json\":{\"status\":0,\"errmsg\":\"\",\"result\":{\"userName\":\""+userName+"\",\"userClass\":\""+className+"\"}}}";
                    response.send(new org.json.JSONObject(resultData));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Multimap resultData = (Multimap) request.getBody().get();
                if (resultData != null) {
                    String right1 = resultData.getString("Right1");
                    String right2 = resultData.getString("Right2");
                    String left1 = resultData.getString("Left1");
                    String left2 = resultData.getString("Left2");
                    String isGlasses = resultData.getString("isGlasses");

                    EyeChartData eyeChartData = new EyeChartData();

                    if ("1".equals(isGlasses)) {
                        eyeChartData.setGlass_od("<4.0".equals(right2) ? "3.9" : right2);
                        eyeChartData.setGlass_os("<4.0".equals(left2) ? "3.9" : left2);
                    } else {
                        eyeChartData.setVision_od("<4.0".equals(right2) ? "3.9" : right2);
                        eyeChartData.setVision_os("<4.0".equals(left2) ? "3.9" : left2);
                    }

                    if (cloud) {
                        try {
                            long timestamp = System.currentTimeMillis()/1000;
                            org.json.JSONObject jsonObj = new org.json.JSONObject();
                            jsonObj.put("dataId", sn);
                            jsonObj.put("timestamp", timestamp+"");
                            jsonObj.put("key", EncryptUtils.encryptMD5ToString(sn+":"+timestamp+":"+ AppConfig.KEY).toLowerCase());
                            jsonObj.put("data", JSON.toJSONString(eyeChartData));
                            //提交数据到服务器
                            OkGo.<String>post(AppConfig.EYECHART_URL)
                                    .tag(this)
                                    .upJson(jsonObj)
                                    .execute(new StringCallback() {
                                        @Override
                                        public void onSuccess(Response<String> response) {


                                        }
                                    });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    LogUtils.e(eyeChartData.toString());

                    EventBus.getDefault().post(new EyeChartMessageEvent(eyeChartData, sn));
                }

            }
        });

        // listen on port 5000
        server.listen(mAsyncServer, 6000);
    }


    private String sn;
    private boolean cloud;

    public void setSn(String snT, boolean isCloud) {
        sn = snT;
        cloud = isCloud;
    }

    public void close(){
        if (server != null) {
            server.stop();
        }

        if (mAsyncServer != null) {
            mAsyncServer.stop();
        }
    }
}
