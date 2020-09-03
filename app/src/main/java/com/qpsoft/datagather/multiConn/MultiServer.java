package com.qpsoft.datagather.multiConn;


import android.text.TextUtils;
import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qpsoft.datagather.AppConfig;
import com.qpsoft.datagather.oldRef.RefractionData;
import com.qpsoft.datagather.oldRef.RefractionMessageEvent;
import com.qpsoft.datagather.oldRef.vendor.suoer.SuoerDataProcess;
import com.qpsoft.datagather.oldRef.vendor.welchallyn.WelchAllynDataProcess;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MultiServer {

    public int port = 9100;//监听的端口号
    private boolean first = true;

    private int HeadLength = 39;


    private Socket socket;

//    public static void main(String[] args) {
//        System.out.println("服务器启动...\n");
//        Server server = new Server();
//        server.init();
//    }


    private String sn;

    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);
                    while (true) {
                        // 一旦有堵塞, 则表示服务器与客户端获得了连接
                        socket = serverSocket.accept();

                        // 处理这次连接
                        System.out.println("新的连接~~~");
                        new HandlerThread(socket);
                    }
                } catch (Exception e) {
                    System.out.println("服务器异常: " + e.getMessage());
                    LogUtils.e("服务器异常: " + e);
                }
            }
        }).start();

    }

    public void setSn(String snT) {
        sn = snT;
    }


    private class HandlerThread implements Runnable {
        //private Socket socket;
        public HandlerThread(Socket client) {
            //socket = client;
            new Thread(this).start();
        }

        public void run() {
            try {
                // 伟伦: 处理客户端数据
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                InputStreamReader input = new InputStreamReader(socket.getInputStream());
                BufferedReader br = new BufferedReader(input);
                List<String> dataList = new ArrayList<>();
                String str;
                while((str = br.readLine()) != null){
                    dataList.add(str);
                }

                RefractionData refractionData = null;
                String vendor = CacheDiskStaticUtils.getString("vendor");
                if (!TextUtils.isEmpty(vendor)) {
                    if ("Wel".equals(vendor)) {
                        refractionData = WelchAllynDataProcess.parse(dataList);
                    } else if("Suo".equals(vendor)) {
                        refractionData = SuoerDataProcess.parse(dataList);
                    }
                } else {
                    refractionData = SuoerDataProcess.parse(dataList);
                }

                long timestamp = System.currentTimeMillis()/1000;
                org.json.JSONObject jsonObj = new org.json.JSONObject();
                jsonObj.put("dataId", sn);
                jsonObj.put("timestamp", timestamp+"");
                jsonObj.put("key", EncryptUtils.encryptMD5ToString(sn+":"+timestamp+":"+ AppConfig.KEY).toLowerCase());
                jsonObj.put("data", JSON.toJSONString(refractionData));
                //提交数据到服务器
                OkGo.<String>post(AppConfig.SERVER_URL)
                        .tag(this)
                        .upJson(jsonObj)
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {


                            }
                        });
                //RefData refractionData = WelchAllynDataProcess.parse(dataList);
                //RefData refractionData = SuoerDataProcess.parse(dataList);
                //System.out.println(refractionData.toString());
                LogUtils.e(refractionData.toString());

                EventBus.getDefault().post(new RefractionMessageEvent(refractionData, sn));

                in.close();
            } catch (Exception e) {
                System.out.println("服务器 run 异常: " + e.getMessage());
            } /*finally {
                if (socket != null) {
                    try {
                        System.out.println("准备关闭连接!");
                        socket.close();
                    } catch (Exception e) {
                        socket = null;
                        System.out.println("服务端 finally 异常:" + e.getMessage());
                    }
                }
            }*/
        }
    }

    public void close(){
        if (socket != null) {
            try {
                System.out.println("准备关闭连接!");
                socket.close();
            } catch (Exception e) {
                socket = null;
                System.out.println("服务端 finally 异常:" + e.getMessage());
            }
        }
    }
}
