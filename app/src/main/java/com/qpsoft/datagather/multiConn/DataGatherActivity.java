package com.qpsoft.datagather.multiConn;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.kyleduo.switchbutton.SwitchButton;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qpsoft.datagather.AppConfig;
import com.qpsoft.datagather.CommonUtil;
import com.qpsoft.datagather.EyeChartData;
import com.qpsoft.datagather.EyeChartMessageEvent;
import com.qpsoft.datagather.MyTimeTask;
import com.qpsoft.datagather.ProviderMessageEvent;
import com.qpsoft.datagather.ProviderUtil;
import com.qpsoft.datagather.R;
import com.qpsoft.datagather.RefData;
import com.qpsoft.datagather.oldRef.RefractionData;
import com.qpsoft.datagather.oldRef.RefractionMessageEvent;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.net.ssl.KeyManagerFactory;

import fi.iki.elonen.NanoHTTPD;
import fynn.app.PromptDialog;
import io.reactivex.functions.Consumer;


public class DataGatherActivity extends AppCompatActivity {
    private TextView mTvTopbarLeft;
    private TextView mTvTopbarTitle;
    private TextView mTvTopbarRight;

    private RecyclerView rvHoldDevice;
    private BaseQuickAdapter<HoldDevice, BaseViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datagather);

        EventBus.getDefault().register(this);

        mTvTopbarLeft = findViewById(R.id.tvTopbarLeft);
        mTvTopbarTitle = findViewById(R.id.tvTopbarTitle);
        mTvTopbarRight = findViewById(R.id.tvTopbarRight);

        mTvTopbarLeft.setVisibility(View.GONE);
        mTvTopbarTitle.setText("数据采集V"+ AppUtils.getAppVersionName());
        mTvTopbarRight.setVisibility(View.VISIBLE);
        mTvTopbarRight.setText("添加设备");

        rvHoldDevice = findViewById(R.id.rvHoldDevice);
        rvHoldDevice.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        rvHoldDevice.setAdapter(mAdapter = new BaseQuickAdapter<HoldDevice, BaseViewHolder>(R.layout.item_datagather) {
            @Override
            protected void convert(@NonNull BaseViewHolder helper, final HoldDevice item) {
                helper.setText(R.id.tvName, item.getName());
                helper.setText(R.id.tvIp, "IP："+item.getIp()+" Port："+item.getPort());
                SwitchButton sbStatus = helper.getView(R.id.sbStatus);
                final ImageView ivConnStatus = helper.getView(R.id.ivConnStatus);
                if (item.isConnStatus()) {
                    ivConnStatus.setImageResource(R.drawable.icon_link);
                } else {
                    ivConnStatus.setImageResource(R.drawable.icon_unlink);
                }
                if (item.isOpen()) {
                    sbStatus.setCheckedNoEvent(true);
                    if (item.getDeviceType() == HoldDeviceType.Wel) {
                        getAuth(item.getIp(), item.getSn());
                    }
                } else {
                    sbStatus.setCheckedNoEvent(false);
                    if (item.getDeviceType() == HoldDeviceType.Wel) {
                        stopTimer(item.getSn());
                    }
                }
                if(item.getDeviceType() == HoldDeviceType.Suo) {
                    socketServer.setSn(item.getSn(), item.isCloud());
                }
                if(item.getDeviceType() == HoldDeviceType.EyeChart) {
                    tempServer.setSn(item.getSn(), item.isCloud());
                }
                helper.addOnClickListener(R.id.llQrCode);
                helper.addOnClickListener(R.id.llEdit);
                sbStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            if (item.getDeviceType() == HoldDeviceType.Wel) {
                                getAuth(item.getIp(), item.getSn());
                            } else if(item.getDeviceType() == HoldDeviceType.Suo) {
                                socketServer.setSn(item.getSn(), item.isCloud());
                            } else if(item.getDeviceType() == HoldDeviceType.EyeChart) {
                                tempServer.setSn(item.getSn(), item.isCloud());
                            }
                        } else {
                            if (item.getDeviceType() == HoldDeviceType.Wel) {
                                stopTimer(item.getSn());
                            } else if(item.getDeviceType() == HoldDeviceType.Suo) {
                                //socketServer.close();
                            }
                            item.setConnStatus(false);
                            ivConnStatus.setImageResource(R.drawable.icon_unlink);
                        }

                        item.setOpen(isChecked);
                        DataGatherUtils.editHoldDevice(item);
                    }
                });


                helperMap.put(item.getSn(), helper);
                itemMap.put(item.getSn(), item);
            }
        });

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                HoldDevice holdDevice = mAdapter.getItem(position);
                switch (view.getId()){
                    case R.id.llQrCode:
                        showQrCode(holdDevice.getSn(), holdDevice.getName(), holdDevice.getDeviceType());
                        break;
                    case R.id.llEdit:
                        startActivity(new Intent(DataGatherActivity.this, AddHoldDeviceActivity.class)
                                .putExtra("holdDevice", holdDevice));
                        break;
                }
            }
        });

        mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, final int position) {
                new PromptDialog.Builder(DataGatherActivity.this)
                        .setTitle("提示")
                        .setMessage("您确定要删除吗？")
                        .setButton1("取消", new PromptDialog.OnClickListener() {

                            @Override
                            public void onClick(Dialog dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setButton2("确定", new PromptDialog.OnClickListener() {

                            @Override
                            public void onClick(Dialog dialog, int which) {
                                dialog.dismiss();
                                DataGatherUtils.delHoldDevice(mAdapter.getItem(position));
                                stopAllTimer();
                                helperMap.clear();
                                itemMap.clear();
                                mAdapter.setNewData(DataGatherUtils.getHoldDeviceList());
                            }
                        }).show();
                return true;
            }
        });


        mTvTopbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DataGatherActivity.this, AddHoldDeviceActivity.class));
            }
        });

        startService();
    }

    private void startService() {
        //init suo
        socketServer = new MultiServer();
        socketServer.init();

        //init eyechart
        tempServer = new TempServer();
        tempServer.init(server, mAsyncServer);

        openHttpServer();
        //openHttpsServer();
    }

    private void showQrCode(String sn, String name, HoldDeviceType deviceType) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_qrcode, null, false);
        new PromptDialog.Builder(DataGatherActivity.this)
                .setTitle("设备二维码")
                .setView(dialogView)
                .setCanceledOnTouchOutside(true)
                .show();

        ImageView ivQrCode = dialogView.findViewById(R.id.ivQrCode);
        TextView tvSn = dialogView.findViewById(R.id.tvSn);
        TextView tvDownload = dialogView.findViewById(R.id.tvDownload);

        //String host = "https://device.qpsc365.com:"+listenPortHttps;
        String host = "http://"+NetworkUtils.getIPAddress(true)+":"+listenPort;

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("dataId", sn);
        jsonObj.put("type", "验光仪");
        jsonObj.put("name", name); 
        if (deviceType == HoldDeviceType.Wel) {
            //jsonObj.put("endpoint", "https://"+NetworkUtils.getIPAddress(true)+":"+listenPortHttps+"/refWelData?dataId="+sn);
            jsonObj.put("endpoint", host+"/refWelData?dataId="+sn);
        } else if (deviceType == HoldDeviceType.Suo) {
            //jsonObj.put("endpoint", "https://"+NetworkUtils.getIPAddress(true)+":"+listenPortHttps+"/refSuoData?dataId="+sn);
            jsonObj.put("endpoint", host+"/refSuoData?dataId="+sn);
        } else if (deviceType == HoldDeviceType.EyeChart) {
            jsonObj.put("type", "视力表");
            //jsonObj.put("endpoint", "https://"+NetworkUtils.getIPAddress(true)+":"+listenPortHttps+"/xingKangChart?dataId="+sn);
            jsonObj.put("endpoint", host+"/xingKangChart?dataId="+sn);
        }
        String txtStr = jsonObj.toJSONString();
        LogUtils.e("qrcode-----------"+txtStr);
        Bitmap qrBitmap = CodeUtils.createImage(txtStr, 300, 300, null);

        ivQrCode.setImageBitmap(qrBitmap);

        String regex = "(.{5})";
        String snResult = sn.replaceAll(regex,"$1-");
        tvSn.setText(snResult);

        tvDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String host = "https://device.qpsc365.com:"+listenPortHttps;
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("dataId", sn);
                jsonObj.put("type", "验光仪");
                jsonObj.put("name", name);
                if (deviceType == HoldDeviceType.Wel) {
                    jsonObj.put("endpoint", host+"/refWelData?dataId="+sn);
                } else if (deviceType == HoldDeviceType.Suo) {
                    jsonObj.put("endpoint", host+"/refSuoData?dataId="+sn);
                } else if (deviceType == HoldDeviceType.EyeChart) {
                    jsonObj.put("type", "视力表");
                    jsonObj.put("endpoint", host+"/xingKangChart?dataId="+sn);
                }
                String txtStr = jsonObj.toJSONString();
                LogUtils.e("qrcode-----------"+txtStr);
                Bitmap qrBitmap = CodeUtils.createImage(txtStr, 300, 300, null);
                RxPermissions rxPermissions = new RxPermissions(DataGatherActivity.this);
                rxPermissions
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(@NonNull Boolean aBoolean) throws Exception {
                                if (aBoolean) {
                                    CommonUtil.saveBitmap2file(qrBitmap, deviceType, DataGatherActivity.this);
                                }else {
                                    ToastUtils.showShort("读写权限未打开");
                                }
                            }
                        });

            }
        });
    }

    private String eventId;
    private String eventT;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ProviderMessageEvent event) {
        LogUtils.e("---------"+event.getEventId());
        LogUtils.e("---------"+event.getEvent());
        eventId = event.getEventId();
        eventT = event.getEvent();
    }

    private RefractionData refSuoData;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RefractionMessageEvent event) {
        String sn = event.getSn();
        refSuoData = event.getRefractionData();
        //send wxa
        if (eventT != null) ProviderUtil.INSTANCE.sendData(eventId, eventT, JSON.toJSONString(refSuoData), DataGatherActivity.this);

        TextView tvData = (TextView) mAdapter.getViewByPosition(rvHoldDevice, getPos(sn), R.id.tvData);
        ImageView ivConnStatus = (ImageView) mAdapter.getViewByPosition(rvHoldDevice, getPos(sn), R.id.ivConnStatus);
        tvData.setVisibility(View.VISIBLE);
        tvData.setText("数据："+refSuoData);
        ivConnStatus.setImageResource(R.drawable.icon_link);
    }

    private EyeChartData xkChartData;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EyeChartMessageEvent event) {
        String sn = event.getSn();
        xkChartData = event.getEyeChartData();
        //send wxa
        if (eventT != null) ProviderUtil.INSTANCE.sendData(eventId, eventT, JSON.toJSONString(xkChartData), DataGatherActivity.this);

        TextView tvData = (TextView) mAdapter.getViewByPosition(rvHoldDevice, getPos(sn), R.id.tvData);
        ImageView ivConnStatus = (ImageView) mAdapter.getViewByPosition(rvHoldDevice, getPos(sn), R.id.ivConnStatus);
        tvData.setVisibility(View.VISIBLE);
        tvData.setText("数据："+xkChartData);
        ivConnStatus.setImageResource(R.drawable.icon_link);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (ActivityUtils.getTopActivity() != DataGatherActivity.this) {
            stopAllTimer();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        List<HoldDevice> holdDeviceList = DataGatherUtils.getHoldDeviceList();
        //Collections.reverse(holdDeviceList);
        mAdapter.setNewData(holdDeviceList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void getAuth(final String deviceIp, final String sn) {
        OkGo.<String>get("https://"+deviceIp)
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
                            String auth = "Digest username=\"" + username  + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + uri + "\", qop=" + qop + ", nc=" + nc
                                    + ", cnonce=\"" + cnonce + "\", response=\"" + res + "\"";

                            authMap.put(sn, auth);

                            setTimer(sn);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
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



    private Map<String, String> authMap = new HashMap<>();
    private void downloadDB(final String sn) {
        String destFileDir = DataGatherActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String destFileName = sn+"_patient.db";
        OkGo.<File>get("https://"+getHoldDevice(sn).getIp()+"/db/patient.db")
                .tag(this)
                .headers("Authorization", authMap.get(sn))
                .execute(new FileCallback(destFileDir, destFileName) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        readDB(response.body().getAbsolutePath(), sn);
                    }
                });

    }

    private Map<String, RefData> refDataMap = new HashMap<>();
    private void readDB(String dbPath, String sn) {
        Log.e("--------", dbPath);
        RefData refWelData = DB2Manager.getInstance(dbPath, sn).queryRefData();
        LogUtils.e("------", refWelData);

        if (refWelData != null && refWelData.getId() != cId) {
            //send wxa
            if (eventT != null) ProviderUtil.INSTANCE.sendData(eventId, eventT, JSON.toJSONString(refWelData), DataGatherActivity.this);
            cId = refWelData.getId();
        }

        TextView tvData = (TextView) mAdapter.getViewByPosition(rvHoldDevice, getPos(sn), R.id.tvData);
        ImageView ivConnStatus = (ImageView) mAdapter.getViewByPosition(rvHoldDevice, getPos(sn), R.id.ivConnStatus);
        tvData.setVisibility(View.VISIBLE);
        tvData.setText("数据："+refWelData);
        ivConnStatus.setImageResource(R.drawable.icon_link);

        if (refWelData != null) {
            refDataMap.put(sn, refWelData);

            if (refWelData.getId() != dataId && getHoldDevice(sn).isCloud()) {
                postPortable(sn, refWelData);
                dataId = refWelData.getId();
            }
        }
    }


    private int dataId;
    private int cId;
    //提交数据到服务器
    private void postPortable(String sn, RefData refWelData) {
        try {
            long timestamp = System.currentTimeMillis()/1000;
            org.json.JSONObject jsonObj = new org.json.JSONObject();
            jsonObj.put("dataId", sn);
            jsonObj.put("timestamp", timestamp+"");
            jsonObj.put("key", EncryptUtils.encryptMD5ToString(sn+":"+timestamp+":"+AppConfig.KEY).toLowerCase());
            jsonObj.put("data", JSON.toJSONString(refWelData));

            OkGo.<String>post(AppConfig.SERVER_URL)
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


    private Map<String, BaseViewHolder> helperMap = new HashMap<>();
    private Map<String, HoldDevice> itemMap = new HashMap<>();
    private int getPos(String sn) {
        return helperMap.get(sn) != null ? helperMap.get(sn).getLayoutPosition() : 0;
    }
    private HoldDevice getHoldDevice(String sn) {
        return itemMap.get(sn);
    }



    private MultiServer socketServer;
    private TempServer tempServer;

    private AsyncHttpServer server = new AsyncHttpServer();
    private AsyncServer mAsyncServer = new AsyncServer();
    private int listenPort = 5000;
    private int listenPortHttps = 8888;

    private MyHttpd2 myHttpd2;
    private MyHttpd myHttpd;

    private void openHttpServer() {
        try {
            myHttpd2 = new MyHttpd2(listenPort);
            myHttpd2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openHttpsServer() {
        try {
            AssetManager am = getAssets();
            //InputStream ins1 = am.open("server.cer");
            InputStream ins2 = am.open("device.qpsc365.com.jks");
            myHttpd = new MyHttpd(8888);

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(ins2, null);

            //读取证书,注意这里的密码必须设置
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "L0xgeOyL".toCharArray());

            myHttpd.makeSecure(NanoHTTPD.makeSSLSocketFactory(keyStore, keyManagerFactory), null);
            myHttpd.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }

    class MyHttpd2 extends NanoHTTPD {

        public MyHttpd2(int port) {
            super(port);
        }

        public MyHttpd2(String hostname, int port) {
            super(hostname, port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            LogUtils.e("--------"+session.getUri());
            if ("/refWelData".equals(session.getUri())) {
                String sn = session.getParms().get("dataId");
                RefData refWelData = refDataMap.get(sn);
                LogUtils.e("vvvvv-------", refWelData);
                //response.send(JSON.toJSONString(refWelData));
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CacheDiskStaticUtils.put(sn, "1");
                        refDataMap.remove(sn);
                    }
                }, 500);
                return NanoHTTPD.newFixedLengthResponse(JSON.toJSONString(refWelData));
                //FileUtils.delete(dbPath);

                //dataTv.setText("");
            } else if ("/refSuoData".equals(session.getUri())) {
                //String sn = request.getQuery().getString("dataId");
                //RefData refSuoData = refDataMap.get(sn);
                LogUtils.e("sssss-------", refSuoData);
                //response.send(JSON.toJSONString(refSuoData));
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refSuoData = null;
                    }
                }, 500);
                return NanoHTTPD.newFixedLengthResponse(JSON.toJSONString(refSuoData));
                //FileUtils.delete(dbPath);
                //dataTv.setText("");
            } else if ("/xingKangChart".equals(session.getUri())) {
                //String sn = request.getQuery().getString("dataId");
                //RefData refSuoData = refDataMap.get(sn);
                LogUtils.e("bbbbb-------", xkChartData);
                //response.send(JSON.toJSONString(xkChartData));
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xkChartData = null;
                    }
                }, 500);
                return NanoHTTPD.newFixedLengthResponse(JSON.toJSONString(xkChartData));
                //FileUtils.delete(dbPath);
                //dataTv.setText("");
            } else if ("/".equals(session.getUri())) {
                return newFixedLengthResponse("test");
            } else {
                return newFixedLengthResponse(null);
            }
        }
    }

    class MyHttpd extends NanoHTTPD {

        public MyHttpd(int port) {
            super(port);
        }

        public MyHttpd(String hostname, int port) {
            super(hostname, port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            LogUtils.e("--------"+session.getUri());
            if ("/refWelData".equals(session.getUri())) {
                String sn = session.getParms().get("dataId");
                RefData refWelData = refDataMap.get(sn);
                LogUtils.e("vvvvv-------", refWelData);
                //response.send(JSON.toJSONString(refWelData));
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CacheDiskStaticUtils.put(sn, "1");
                        refDataMap.remove(sn);
                    }
                }, 500);
                return NanoHTTPD.newFixedLengthResponse(JSON.toJSONString(refWelData));
                //FileUtils.delete(dbPath);

                //dataTv.setText("");
            } else if ("/refSuoData".equals(session.getUri())) {
                //String sn = request.getQuery().getString("dataId");
                //RefData refSuoData = refDataMap.get(sn);
                LogUtils.e("sssss-------", refSuoData);
                //response.send(JSON.toJSONString(refSuoData));
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refSuoData = null;
                    }
                }, 500);
                return NanoHTTPD.newFixedLengthResponse(JSON.toJSONString(refSuoData));
                //FileUtils.delete(dbPath);
                //dataTv.setText("");
            } else if ("/xingKangChart".equals(session.getUri())) {
                //String sn = request.getQuery().getString("dataId");
                //RefData refSuoData = refDataMap.get(sn);
                LogUtils.e("bbbbb-------", xkChartData);
                //response.send(JSON.toJSONString(xkChartData));
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xkChartData = null;
                    }
                }, 500);
                return NanoHTTPD.newFixedLengthResponse(JSON.toJSONString(xkChartData));
                //FileUtils.delete(dbPath);
                //dataTv.setText("");
            } else if ("/".equals(session.getUri())) {
                return newFixedLengthResponse("test");
            } else {
                return newFixedLengthResponse(null);
            }
        }
    }



    private Map<String, MyTimeTask> myTimeTaskMap = new HashMap<>();
    private static final int TIMER = 999;
    private void setTimer(final String sn){
        MyTimeTask task = new MyTimeTask(5000, new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.obj = sn;
                message.what = TIMER;
                mHandler.sendMessage(message);
                //或者发广播，启动服务都是可以的
            }
        });
        task.start();

        myTimeTaskMap.put(sn, task);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TIMER:
                    //在此执行定时操作
                    String sn = (String) msg.obj;
                    downloadDB(sn);
                    break;
                default:
                    break;
            }
        }
    };

    private void stopTimer(String sn){
        LogUtils.e("-------"+myTimeTaskMap);
        MyTimeTask task = myTimeTaskMap.get(sn);
        LogUtils.e("+++++++"+task);
        if (task != null) task.stop();
    }

    private void stopAllTimer(){
        for (String key : myTimeTaskMap.keySet()) {
            stopTimer(key);
        }
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

        if (myHttpd2 != null) {
            myHttpd2.stop();
        }
        if (myHttpd != null) {
            myHttpd.stop();
        }

        stopAllTimer();

        if (socketServer != null) {
            socketServer.close();
        }

        EventBus.getDefault().unregister(this);
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



    @Override
    public void onBackPressed() {
        //exitApp();
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
