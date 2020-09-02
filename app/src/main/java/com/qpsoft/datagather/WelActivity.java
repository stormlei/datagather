package com.qpsoft.datagather;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qpsoft.datagather.oldRef.RefractionData;
import com.qpsoft.datagather.oldRef.RefractionMessageEvent;
import com.qpsoft.datagather.oldRef.Server;
import com.stealthcopter.networktools.PortScan;
import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.subnet.Device;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;


public class WelActivity extends AppCompatActivity {

    private Server socketServer;

    private EditText ipEdt;
    private Button dataListenBtn;
    private TextView dataTv;
    private ImageView ivQrCode;

    private String host = "https://192.168.0.183";

    private int port = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wel);

        socketServer = new Server();
        socketServer.init();


        ipEdt = findViewById(R.id.ipEdt);
        dataListenBtn = findViewById(R.id.dataListenBtn);
        dataTv = findViewById(R.id.dataTv);
        ivQrCode = findViewById(R.id.ivQrCode);

        findViewById(R.id.dataListenBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                host = "https://"+ipEdt.getText().toString().trim();
                getAuth(host);
                if (NetworkUtils.isConnected()) {
                    showQrCode();
                } else {
                    ToastUtils.showShort("请检查网络连接");
                }
            }
        });


        if (!isListen) openHttpServer();
    }

    private void showQrCode() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("ip", ipEdt.getText().toString().trim());
        jsonObj.put("type", "验光仪");
        jsonObj.put("name", "维伦");
        String ip = NetworkUtils.getIPAddress(true);
        String path = "/refWelData";
        jsonObj.put("endpoint", "http://"+ip+":"+port+path);
        String txtStr = jsonObj.toJSONString();
        Bitmap qrBitmap = CodeUtils.createImage(txtStr, 300, 300, null);
        ivQrCode.setImageBitmap(qrBitmap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }



    private RefractionData refSuoData;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RefractionMessageEvent event) {
        refSuoData = event.getRefractionData();

//        if (!TextUtils.isEmpty(refSuoData.getOd().getDs())) {
//
//        }
        ToastUtils.showLong("传输成功");
//            mDioRightEdt.setText(refractionData.getOd().getDs());
//            mDioLeftEdt.setText(refractionData.getOs().getDs());
//            mAstRightEdt.setText(refractionData.getOd().getDc());
//            mAstLeftEdt.setText(refractionData.getOs().getDc());
//            mAxlRightEdt.setText(refractionData.getOd().getAx());
//            mAxlLeftEdt.setText(refractionData.getOs().getAx());

        dataTv.setText(""+refSuoData);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void getAuth(String host) {
        OkGo.<String>get(host)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (401 == response.code()) {

                            String header = response.headers().get("WWW-Authenticate");
                            String[] headerArr = header.split(", ");
                            Map<String, String> headerMap = new HashMap<>();
                            for (String h : headerArr) {
                                String[] aa = h.split("=");
                                headerMap.put(aa[0], aa[1]);
                            }
                            String realm = headerMap.get("Digest realm").replace("\"", "");
                            String nonce = headerMap.get("nonce").replace("\"", "");
                            String qop = headerMap.get("qop").replace("\"", "");

                            String method = "GET";
                            String uri = "/db/patient.db";
                            String nc = "00000001";
                            String cnonce = random7();
                            String username = "spot";
                            String password = "0000";
                            // 后期变成可配置
                            String a1 = username + ":" + realm + ":" + password;
                            LogUtils.e("dddddd----"+a1);
                            String a2 = method + ":" + uri;

                            String hA1 = md5DigestAsHex(a1);
                            String hA2 = md5DigestAsHex(a2);

                            LogUtils.e("-------"+hA1);
                            LogUtils.e("-------"+hA2);

                            String res = md5DigestAsHex(hA1+":"+nonce+":"+nc+":"+cnonce+":"+qop+":"+hA2);

                            Log.e("-------", realm+"--"+nonce+"--"+qop);
                            auth = "Digest username=\"" + username  + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + uri + "\", qop=" + qop + ", nc=" + nc
                                    + ", cnonce=\"" + cnonce + "\", response=\"" + res + "\"";


                            setTimer();
                        }
                    }
                });

    }


    private String random7() {
        String strRand = "" ;
        for(int i = 0; i < 7; i++){
            strRand += String.valueOf((int)(Math.random() * 10)) ;
        }
        return strRand;
    }



    String auth = "";
    private void downloadDB() {
        String destFileDir = WelActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String destFileName = "patient.db";
        OkGo.<File>get(host+"/db/patient.db")
                .tag(this)
                .headers("Authorization", auth)
                .execute(new FileCallback(destFileDir, destFileName) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        readDB(response.body().getAbsolutePath());
                    }
                });

    }

    private RefData refWelData;
    private void readDB(String dbPath) {
        Log.e("--------", dbPath);
        refWelData = DBManager.getInstance(dbPath).queryRefData();
        LogUtils.e("------", refWelData);
        dataListenBtn.setText("数据监听中...");
        dataTv.setText(""+refWelData);
        dataListenBtn.setEnabled(false);
    }


    private boolean isListen = false;
    private AsyncHttpServer server = new AsyncHttpServer();
    private AsyncServer mAsyncServer = new AsyncServer();

    private void openHttpServer() {
        server.get("/refWelData", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                LogUtils.e("vvvvv-------", refWelData);
                response.send(JSON.toJSONString(refWelData));
                //FileUtils.delete(dbPath);
                CacheDiskStaticUtils.put("used", "1");
                refWelData = null;
                dataTv.setText("");
            }
        });

        server.get("/refSuoData", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                LogUtils.e("vvvvv-------", refSuoData);
                response.send(JSON.toJSONString(refSuoData));
                refSuoData = null;
                dataTv.setText("");
            }
        });

        // listen on port 5000
        server.listen(mAsyncServer, port);

        isListen = true;
    }


    private MyTimeTask task;
    private static final int TIMER = 999;
    private void setTimer(){
        task = new MyTimeTask(3000, new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(TIMER);
                //或者发广播，启动服务都是可以的
            }
        });
        task.start();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TIMER:
                    //在此执行定时操作
                    downloadDB();
                    break;
                default:
                    break;
            }
        }
    };

    private void stopTimer(){
        if (task != null) task.stop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
        }

        if (mAsyncServer != null) {
            mAsyncServer.stop();
        }

        stopTimer();

        socketServer.close();
    }


    public static String md5DigestAsHex(String str) {

        try {
            //获取MD5对象
            MessageDigest instance = MessageDigest.getInstance("MD5");
            //对字符串进行加密，返回字节数组
            byte[] digest = instance.digest(str.getBytes());

            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                //获取字节低八位有效值
                int i = b & 0xff;
                //将整数转换为16进制
                String hexString = Integer.toHexString(i);
                //将长度为1时，补零
                if (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }
                //MD5永远是32位
                sb.append(hexString);
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            //没有该算法时抛出此异常
            e.printStackTrace();

        }
        return "";
    }


    private void pairedDevices() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
                    @Override
                    public void onDeviceFound(Device device) {
                        // Stub: Found subnet device
                        final String ipNo = device.ip;
                        Log.e("tag----", ipNo);
                        // Asynchronously
                        try {
                            PortScan.onAddress(ipNo).setTimeOutMillis(1000).setPort(443).setMethodTCP().doScan(new PortScan.PortListener() {
                                @Override
                                public void onResult(int portNo, boolean open) {
                                    if (open){// Stub: found open port
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Log.e("tag", ipNo);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFinished(ArrayList<Integer> openPorts) {
                                    // Stub: Finished scanning
                                }
                            });
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFinished(ArrayList<Device> devicesFound) {
                        // Stub: Finished scanning
                    }
                });
            }
        }).start();
    }


    @Override
    public void onBackPressed() {
        exitApp();
    }

    private long exitTime = 0;

    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }

    }

}
