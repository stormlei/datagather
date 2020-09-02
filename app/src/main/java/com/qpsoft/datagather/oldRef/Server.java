package com.qpsoft.datagather.oldRef;


import android.text.TextUtils;

import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.LogUtils;
import com.qpsoft.datagather.oldRef.vendor.suoer.SuoerDataProcess;
import com.qpsoft.datagather.oldRef.vendor.welchallyn.WelchAllynDataProcess;
import org.greenrobot.eventbus.EventBus;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static final int PORT = 9100;//监听的端口号
    private boolean first = true;

    private int HeadLength = 39;


    private Socket socket;

//    public static void main(String[] args) {
//        System.out.println("服务器启动...\n");
//        Server server = new Server();
//        server.init();
//    }

    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(PORT);
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


                LogUtils.e("---------", dataList+"");
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

                //RefData refractionData = WelchAllynDataProcess.parse(dataList);
                //RefData refractionData = SuoerDataProcess.parse(dataList);
                //System.out.println(refractionData.toString());
                LogUtils.e(refractionData.toString());

                EventBus.getDefault().post(new RefractionMessageEvent(refractionData, ""));

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
