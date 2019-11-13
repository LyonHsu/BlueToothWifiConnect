package lyon.kevin.bluetooth.assistant.chat.NetWork.WifiSetting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

import lyon.kevin.bluetooth.assistant.chat.Log;
import lyon.kevin.bluetooth.assistant.chat.NetWork.tool.Alert;
import lyon.kevin.bluetooth.assistant.chat.NetWork.tool.Permission;
import lyon.kevin.bluetooth.assistant.chat.R;


/**
 * Created by i_chihhsuanwang on 2016/12/29.
 */

public class WifiMenu extends Activity {
    String TAG=WifiMenu.class.getName();
    private RecyclerAdapter mAdapter;
    private RecyclerView Recycler;
    private TextView wifiName, wifiPassword, wifiSecuritytype;
    private EditText nameEdit, passwordEdit;
    private Button wifiConnect;
    TextView ipAddressShow;
    Context context;
    WifiManager wifiManager;
    ImageButton backButton,reButton;
    WifiSetting wifiSetting;

    ProgressDialog progressDialog;
    final int GETIP=1;
    final int SHOW_IP_ADDRESS=2;
    List<ScanResult> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_menu_setting);
        setActivityLayout();
        this.context=this;
        wifiSetting = new WifiSetting();
        wifiSetting.initWifi(WifiMenu.this);
        checkWifiStatus();
        ipAddressShow = (TextView)findViewById(R.id.ipAddressShow);
        ipAddressShow.setText(getLocalIpAddress(this));


        backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        reButton = (ImageButton)findViewById(R.id.reButton);
        reButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wifiSetting!=null) {
                    list = new ArrayList<>();
                    list = wifiSetting.wifiscan();
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


    }
    @SuppressLint("WifiManagerLeak")
    public String getLocalIpAddress(Context context) {

        String ip =  "no connect wifi!";
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiManager.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        ip= String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));

        Log.i(TAG, "***** IP="+ ip);


        return "Wifi:"+ip+"\n ("+wifiInf.getSSID().toString()+") connected\n"+wifiInf.getBSSID();
    }

    private void setActivityLayout(){
        Recycler  = (RecyclerView) findViewById(R.id.recyclerView);
        Recycler.setLayoutManager(new LinearLayoutManager(this));
        wifiName = (TextView) findViewById(R.id.nameedit);
        wifiPassword = (TextView) findViewById(R.id.passwordedit);
        wifiSecuritytype = (TextView) findViewById(R.id.securitytype);
        nameEdit = (EditText) findViewById(R.id.nameedit);
        passwordEdit = (EditText) findViewById(R.id.passwordedit);
        wifiConnect = (Button) findViewById(R.id.wifiConnectButton);
        wifiConnect.setOnClickListener(BtnWifiConnectOnClickListener);
    }

    private View.OnClickListener BtnWifiConnectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            progressDialog = ProgressDialog.show(context,
                    "連線中(connecting...)", "請等待...(Please wait)", true);

            handler.postDelayed(runnable,3*1000);
        }
    };

    private Runnable runnable = new Runnable() {
        public void run() {
            progressDialog.dismiss();
            final WifiConfiguration tempConfig = isExsits2(nameEdit.getText().toString());
            if (tempConfig != null) {
                handler.sendEmptyMessage(GETIP);
            } else {
                handler.sendEmptyMessage(SHOW_IP_ADDRESS);
            }
        }
    };

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GETIP:
                    ipAddressShow = (TextView) findViewById(R.id.ipAddressShow);
                    ipAddressShow.setText(getLocalIpAddress(context));
                    finish();
                    break;
                case SHOW_IP_ADDRESS:
                    if (wifiSetting.addNetwork(WifiSetting.createWifiConfiguration(nameEdit.getText().toString(), passwordEdit.getText().toString(), wifiSetting.selectWifiCipherType(wifiSecuritytype.getText().toString())))) {
                        ipAddressShow = (TextView) findViewById(R.id.ipAddressShow);
                        ipAddressShow.setText(getLocalIpAddress(getApplicationContext()));
                        finish();
                    } else {
                        ipAddressShow = (TextView) findViewById(R.id.ipAddressShow);
                        ipAddressShow.setText("no connect wifi!");
                    }
                    break;
            }
        }
    };


    private void checkWifiStatus(){
        boolean wifistatus = wifiSetting.getwifistatus();
        Log.d(TAG,"checkWifiStatus() wifistatus:"+wifistatus);
        if (wifistatus){
            checkWifiPermissionStatus();
        }else{
            Alert.showAlert(WifiMenu.this, getString(R.string.wifititle), getString(R.string.wifioffmassage), "安安");
        }

    }

    private void checkWifiPermissionStatus(){
        Log.d(TAG,"checkWifiPermissionStatus()");
        Permission permission = new Permission();
        if (permission.checBluetoothPermission(WifiMenu.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})) {
            list = wifiSetting.wifiscan();
            Log.d(TAG,"checkWifiPermissionStatus wifi scan num:"+list.size());
            mAdapter = new RecyclerAdapter(list);//这里的getyourDatas()返回的是String类型的数组
            Recycler.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onItemClick(View view, int position, ScanResult data) {

                    WifiInfo wifiInf = wifiManager.getConnectionInfo();
                    wifiName.setText(data.SSID.toString());

                    WifiConfiguration tempConfig = isExsits(data.SSID);
                    if (tempConfig != null) {
                        if(!tempConfig.preSharedKey.isEmpty()) {
                            Log.d(TAG, "password preSharedKey :" + tempConfig.preSharedKey);
                            wifiPassword.setText("**********");
                        }
                        else if(!tempConfig.wepKeys[0].isEmpty()) {
                            wifiPassword.setText("**********");
                            Log.d(TAG, "password wepKeys :" + tempConfig.wepKeys[0]);
                        }
                        else {
                            wifiPassword.setText("");
                            Log.d(TAG, "password :" + "");
                        }
                    }else{
                        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
                        Toast.makeText(WifiMenu.this,"click:"+position, Toast.LENGTH_SHORT).show();
                    }
                    wifiSecuritytype.setText(data.capabilities.toString());

                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
                    Toast.makeText(WifiMenu.this,"click:"+position, Toast.LENGTH_SHORT).show();


                    passwordEdit.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            passwordEdit.requestFocus();
                            imm.showSoftInput(passwordEdit, 0);
                        }
                    }, 100);
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    Toast.makeText(WifiMenu.this,"longclick:"+position, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Log.e(TAG,"no Permission");
        }
    }

    // 查看以前是否也配置过这个网络
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
//                wifiManager.disconnect();
//                wifiManager.enableNetwork(existingConfig.networkId, true);
//                wifiManager.reconnect();
                return existingConfig;
            }
        }
        return null;
    }
    // 查看以前是否也配置过这个网络
    private WifiConfiguration isExsits2(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(existingConfig.networkId, true);
                wifiManager.reconnect();
                return existingConfig;
            }
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG,"key:"+keyCode+", KeyEvent:"+event);

        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
