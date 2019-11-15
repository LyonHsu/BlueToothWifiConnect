package lyon.kevin.bluetooth.assistant.chat;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import lyon.kevin.bluetooth.assistant.chat.NetWork.WifiSetting.RecyclerAdapter;
import lyon.kevin.bluetooth.assistant.chat.NetWork.WifiSetting.WifiMenu;
import lyon.kevin.bluetooth.assistant.chat.NetWork.WifiSetting.WifiSetting;
import lyon.kevin.bluetooth.assistant.chat.NetWork.tool.Permission;

public class MainActivity extends Activity {
    String TAG = MainActivity.class.getSimpleName();
    private TextView status,wifiStatus;
    private Button btnConnect,btnWifiSet;
    private ListView listView;
    private Dialog dialog;
    private TextInputLayout inputLayout;
    private  ChatAdapter chatAdapter;//ArrayAdapter<String>
    private ArrayList<HashMap<String,String>> chatMessages;
    private BluetoothAdapter bluetoothAdapter;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private ChatController chatController;
    private BluetoothDevice connectingDevice;
    private ArrayAdapter<String> discoveredDevicesAdapter;
    WifiSetting wifiSetting;
    Context context;

    final String TYPE = "TYPE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewsByIds();

        context = this;
        wifiSetting = new WifiSetting();
        wifiSetting.initWifi(MainActivity.this);

        //check device support bluetooth or not
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }

        //show bluetooth devices dialog when click connect button
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrinterPickDialog();
            }
        });

        //set chat adapter
        chatMessages = new ArrayList<>();
        //new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        chatAdapter = new ChatAdapter(this,chatMessages);
        listView.setAdapter(chatAdapter);
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            setStatus("Connected to: " + connectingDevice.getName());
                            btnConnect.setEnabled(false);
                            btnWifiSet.setVisibility(View.VISIBLE);
                            break;
                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting...");
                            btnConnect.setEnabled(false);
                            btnWifiSet.setVisibility(View.VISIBLE);
                            break;
                        case ChatController.STATE_LISTEN:
                        case ChatController.STATE_NONE:
                            setStatus("Not connected");
                            btnConnect.setEnabled(true);
                            btnWifiSet.setVisibility(View.GONE);
                            chatMessages.clear();
                            chatAdapter.notifyDataSetChanged();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    HashMap<String,String> hashMap = new HashMap();
                    hashMap.put("TYPE","");
                    hashMap.put("MSG","Me: " + writeMessage);
                    chatMessages.add(hashMap);
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);

                    try {
                        JSONObject jsonObject = new JSONObject(readMessage);
                        procressJson(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
                            Toast.LENGTH_SHORT).show();
                    btnConnect.setEnabled(false);
                    btnWifiSet.setVisibility(View.VISIBLE);
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    private void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Bluetooth Devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        //Initializing bluetooth adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        //locate listviews and attatch the adapters
        ListView listView = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        //Handling listview item click event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showWifiConnectDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_wifi);
        dialog.setTitle("Wifi Devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        Permission permission = new Permission();
        RecyclerView Recycler;
        RecyclerAdapter mAdapter;
        List<ScanResult> list = new ArrayList<>();
        Recycler  = (RecyclerView) dialog.findViewById(R.id.recyclerView);
        final WifiManager wifiManager = (WifiManager) getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Recycler.setLayoutManager(new LinearLayoutManager(this));
        if (permission.checBluetoothPermission(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})) {

            list = wifiSetting.wifiscan();
            Log.d(TAG,"checkWifiPermissionStatus wifi scan num:"+list.size());
            mAdapter = new RecyclerAdapter(list);//???getyourDatas()????String?????
            Recycler.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onItemClick(View view, int position, ScanResult data) {

                    WifiInfo wifiInf = wifiManager.getConnectionInfo();
                    String SSIDstr = data.SSID.toString();
                    dialog.dismiss();
                    String wifiType = data.capabilities.toString();
                    showWifiSet(SSIDstr,wifiType);

                    Log.d(TAG,"20191112 SSIDstr:"+SSIDstr);
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    Toast.makeText(MainActivity.this,"longclick:"+position, Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show();
        }else{
            Log.e(TAG,"no Permission");
        }
    }

    private void showWifiSet(final String SSIDstr,final String wifiType){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_wifi_set);
        dialog.setTitle("Wifi");
        TextView SSIDTxt = (TextView)dialog.findViewById(R.id.SSIDTxt);
        final EditText passwordedit = (EditText)dialog.findViewById(R.id.passwordedit);
        Button connectBtn = (Button)dialog.findViewById(R.id.connectBtn);
        TextView wifiSecuritytype = (TextView) dialog.findViewById(R.id.securitytype);

        SSIDTxt.setText(SSIDstr);
        wifiSecuritytype.setText(wifiType);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(passwordedit.getText().toString().isEmpty()){
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("SSID", SSIDstr);
                        jsonObject.put("PASSWD", passwordedit.getText().toString());
                        jsonObject.put("WifiTYPE", "NOPASS");
                        sendMessage(jsonObject.toString());
                    }catch (JSONException e){
                        Log.e(TAG,""+e);
                    }
                }else{
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("SSID", SSIDstr);
                        jsonObject.put("PASSWD", passwordedit.getText().toString());
                        jsonObject.put("WifiTYPE", wifiType);
                        sendMessage(jsonObject.toString());
                    }catch (JSONException e){
                        Log.e(TAG,""+e);
                    }
                }
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void setStatus(String s) {
        status.setText(s);
    }

    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device);
    }

    private void findViewsByIds() {
        status = (TextView) findViewById(R.id.status);
        wifiStatus = (TextView) findViewById(R.id.wifiStatus);
        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnWifiSet = (Button) findViewById(R.id.btn_wifi_set);
        listView = (ListView) findViewById(R.id.list);
        inputLayout = (TextInputLayout) findViewById(R.id.input_layout);
        View btnSend = findViewById(R.id.btn_send);
        wifiStatus.setVisibility(View.GONE);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputLayout.getEditText().getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Please input some texts", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO: here
                    sendMessage(inputLayout.getEditText().getText().toString());
                    inputLayout.getEditText().setText("");
                }
            }
        });

        btnWifiSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWifiConnectDialog();
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    chatController = new ChatController(this, handler);
                } else {
                    Toast.makeText(this, "Bluetooth still disabled, turn off application!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }


    private void sendMessage(String message) {
        if (chatController.getState() != ChatController.STATE_CONNECTED) {
            Toast.makeText(this, "Connection was lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatController.write(send);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            chatController = new ChatController(this, handler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatController != null)
            chatController.stop();
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
            }
        }
    };


    private void procressJson(JSONObject jsonObject){
        final String IP = "IP";
        final String SSID="SSID";
        final String TYPE = "Type";
        final String LOGd="LOGd";
        final String LOGe="LOGe";
        final String LOGi="LOGi";
        final String LOGw="LOGw";
        final String LOGv="LOGv";
        final String MSGTYPE = "MSGTYPE";
        if(jsonObject.has(IP)){
            String IPstr = jsonObject.optString(IP);
            if(IPstr.isEmpty()){
                wifiStatus.setVisibility(View.GONE);
            }else{
                String SSIDstr="";
                if(jsonObject.has(SSID)){
                    SSIDstr=jsonObject.optString(SSID).replace("\\","");
                    wifiStatus.setText(SSID+":"+SSIDstr+"   "+IP+":"+IPstr);
                }else{
                    wifiStatus.setText("Status:"+IPstr);
                }
                wifiStatus.setVisibility(View.VISIBLE);
            }
        }else if(jsonObject.has(TYPE)){
            String Typestr = jsonObject.optString(TYPE);
            HashMap<String,String> hashMap = new HashMap();
            hashMap.put(TYPE,Typestr);
            hashMap.put("MSG",connectingDevice.getName() + ":  " + jsonObject);
            chatMessages.add(hashMap);
            chatAdapter.notifyDataSetChanged();

        } else {
            String Typestr = jsonObject.optString(TYPE,"");
            HashMap<String,String> hashMap = new HashMap();
            hashMap.put(TYPE,Typestr);
            hashMap.put("MSG",connectingDevice.getName() + ":  " + jsonObject);
            chatMessages.add(hashMap);
            chatAdapter.notifyDataSetChanged();
        }
    }
}
