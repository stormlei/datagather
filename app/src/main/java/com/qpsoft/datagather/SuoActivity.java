package com.qpsoft.datagather;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
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

import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class SuoActivity extends AppCompatActivity {

    private Server socketServer;

    private TextView dataTv;
    private ImageView ivQrCode;

    private int port = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suo);

        socketServer = new Server();
        socketServer.init();


        dataTv = findViewById(R.id.dataTv);
        ivQrCode = findViewById(R.id.ivQrCode);

        if (NetworkUtils.isConnected()) {
            showQrCode();
        } else {
            ToastUtils.showShort("请检查网络连接");
        }


        openHttpServer();
    }

    private void showQrCode() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("type", "验光仪");
        jsonObj.put("name", "索维");
        String ip = NetworkUtils.getIPAddress(true);
        String path = "/refSuoData";
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
//        ToastUtils.showLong("传输成功");
//        mDioRightEdt.setText(refractionData.getOd().getDs());
//        mDioLeftEdt.setText(refractionData.getOs().getDs());
//        mAstRightEdt.setText(refractionData.getOd().getDc());
//        mAstLeftEdt.setText(refractionData.getOs().getDc());
//        mAxlRightEdt.setText(refractionData.getOd().getAx());
//        mAxlLeftEdt.setText(refractionData.getOs().getAx());

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


    private AsyncHttpServer server = new AsyncHttpServer();
    private AsyncServer mAsyncServer = new AsyncServer();

    private void openHttpServer() {

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
