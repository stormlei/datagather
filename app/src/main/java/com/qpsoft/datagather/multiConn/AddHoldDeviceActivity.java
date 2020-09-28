package com.qpsoft.datagather.multiConn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qpsoft.datagather.R;


public class AddHoldDeviceActivity extends AppCompatActivity {
    private TextView mTvTopbarLeft;
    private TextView mTvTopbarTitle;

    private RadioGroup rgDeviceType;
    private RadioButton rbWel;
    private RadioButton rbSuo;
    private RadioButton rbXingKang;
    private EditText edtName;
    private EditText edtIp;
    private SwitchButton sbCloudStatus;

    private TextView tvSave;

    private HoldDeviceType deviceType = HoldDeviceType.Wel;

    private HoldDevice holdDevice;

    private boolean cloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_holddevice);

        holdDevice = (HoldDevice) getIntent().getSerializableExtra("holdDevice");

        mTvTopbarLeft = findViewById(R.id.tvTopbarLeft);
        mTvTopbarTitle = findViewById(R.id.tvTopbarTitle);

        rgDeviceType = findViewById(R.id.rgDeviceType);
        rbWel = findViewById(R.id.rbWel);
        rbSuo = findViewById(R.id.rbSuo);
        rbXingKang = findViewById(R.id.rbXingKang);
        edtName = findViewById(R.id.edtName);
        edtIp = findViewById(R.id.edtIp);
        sbCloudStatus = findViewById(R.id.sbCloudStatus);

        tvSave = findViewById(R.id.tvSave);

        mTvTopbarTitle.setText("添加采集设备");

        if (holdDevice != null) {
            rbWel.setEnabled(false);
            rbSuo.setEnabled(false);
            rbXingKang.setEnabled(false);
            if (holdDevice.getDeviceType() == HoldDeviceType.Wel) {
                rbWel.setChecked(true);
                deviceType = HoldDeviceType.Wel;
            } else if (holdDevice.getDeviceType() == HoldDeviceType.Suo) {
                rbSuo.setChecked(true);
                deviceType = HoldDeviceType.Suo;
            } else if (holdDevice.getDeviceType() == HoldDeviceType.EyeChart) {
                rbXingKang.setChecked(true);
                deviceType = HoldDeviceType.EyeChart;
            }
            edtName.setText(holdDevice.getName());
            edtIp.setText(holdDevice.getIp());

            mTvTopbarTitle.setText("编辑采集设备");
        }


        rgDeviceType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.rbWel:
                        deviceType = HoldDeviceType.Wel;
                        edtIp.setText("");
                        break;
                    case R.id.rbSuo:
                        deviceType = HoldDeviceType.Suo;
                        edtIp.setText("");
                        break;
                    case R.id.rbXingKang:
                        deviceType = HoldDeviceType.EyeChart;
                        edtIp.setText(NetworkUtils.getIPAddress(true));
                        break;
                }
            }
        });

        sbCloudStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                cloud = isChecked;
            }
        });


        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holdDevice == null) {
                    getSn();
                } else {
                    holdDevice.setIp(edtIp.getText().toString().trim());
                    holdDevice.setDeviceType(deviceType);
                    if (deviceType == HoldDeviceType.Wel) {
                        holdDevice.setSsl(true);
                        holdDevice.setPort(443);
                    } else if (deviceType == HoldDeviceType.Suo) {
                        holdDevice.setSsl(false);
                        holdDevice.setPort(80);
                    } else if (deviceType == HoldDeviceType.EyeChart) {
                        holdDevice.setSsl(false);
                        holdDevice.setPort(6000);
                    }
                    holdDevice.setName(edtName.getText().toString().trim());
                    holdDevice.setCloud(cloud);

                    boolean isSuccess = DataGatherUtils.editHoldDevice(holdDevice);
                    if (isSuccess) {
                        ToastUtils.showShort("编辑成功");
                        finish();
                    } else {
                        ToastUtils.showShort("该设备已存在");
                    }
                }

            }
        });


        mTvTopbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getSn() {
        OkGo.<String>get("https://commservice.qingpai365.com/genid/v1/")
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject bodyJsonObj = JSON.parseObject(response.body());
                        int code = bodyJsonObj.getIntValue("code");
                        if (code == 0) {
                            JSONArray dataJsonArray = bodyJsonObj.getJSONArray("data");
                            if (dataJsonArray.size() > 0) {
                                String sn = dataJsonArray.getString(0);

                                HoldDevice createHoldDevice = new HoldDevice();
                                createHoldDevice.setIp(edtIp.getText().toString().trim());
                                createHoldDevice.setDeviceType(deviceType);
                                if (deviceType == HoldDeviceType.Wel) {
                                    createHoldDevice.setSsl(true);
                                    createHoldDevice.setPort(443);
                                } else if (deviceType == HoldDeviceType.Suo) {
                                    createHoldDevice.setSsl(false);
                                    createHoldDevice.setPort(80);
                                } else if (deviceType == HoldDeviceType.EyeChart) {
                                    createHoldDevice.setSsl(false);
                                    createHoldDevice.setPort(6000);
                                }
                                createHoldDevice.setName(edtName.getText().toString().trim());
                                createHoldDevice.setSn(sn);
                                createHoldDevice.setOpen(true);
                                createHoldDevice.setCloud(cloud);

                                boolean isSuccess = DataGatherUtils.addHoldDevice(createHoldDevice);
                                if (isSuccess) {
                                    ToastUtils.showShort("添加成功");
                                    finish();
                                } else {
                                    ToastUtils.showShort("该设备已存在");
                                }
                            }

                        } else {
                            String message = bodyJsonObj.getString("message");
                            ToastUtils.showShort(message);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ToastUtils.showShort("服务器错误，请联系客服");
                    }
                });
    }
}
