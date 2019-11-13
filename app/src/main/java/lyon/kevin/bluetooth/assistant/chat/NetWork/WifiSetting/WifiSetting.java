package lyon.kevin.bluetooth.assistant.chat.NetWork.WifiSetting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.List;

import lyon.kevin.bluetooth.assistant.chat.Log;


/**
 * Created by i_chihhsuanwang on 2016/12/22.
 */

public class WifiSetting {
    private WifiManager wifiManager;
    public String TAG = "WifiSetting";
    public static final String WIFI_SERVICE = "wifi";

    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_WPA2, WIFICIPHER_NOPASS, WIFICIPHER_INVALID, IEEE8021XEAP
    }


    @SuppressLint("WrongConstant")
    public void initWifi(Context context) {
        if (context != null) {
            wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        }else{
            Log.e(TAG,"initWifi fail context = null");
        }
    }

    public boolean getwifistatus() {
        boolean wifistatus = false;
        if (wifiManager.isWifiEnabled()) {
            wifistatus = true;
        }
        return wifistatus;
    }

    public boolean setwifistatus(boolean switchstatus) {
        Log.v(TAG, "staus = " + switchstatus);
        wifiManager.setWifiEnabled(switchstatus);
        Log.v(TAG, "wifi staus = " + wifiManager.isWifiEnabled());
        return wifiManager.isWifiEnabled();
    }

    public List<ScanResult> wifiscan() {
        List<String> wifilist= new ArrayList<String>();
        wifiManager.startScan();
        //偵測周圍的Wi-Fi環境(因為會有很多組Wi-Fi，所以型態為List)
        List<ScanResult> mWifiScanResultList = wifiManager.getScanResults();
        //手機內已存的Wi-Fi資訊(因為會有很多組Wi-Fi，所以型態為List)
        //mWifiConfigurationList = wifiManager.getConfiguredNetworks();
        //目前已連線的Wi-Fi資訊
        //mWifiInfo = wifiManager.getConnectionInfo();
        WifiInfo wifiInf = wifiManager.getConnectionInfo();
        String ssid = wifiInf.getSSID().toString();
        ArrayList<ScanResult> wifiSSId = new ArrayList<>();
        ScanResult choweScanResult = null;
        for (int i = 0; i < mWifiScanResultList.size(); i++) {
            //手機目前周圍的Wi-Fi環境
            Log.v("TAG", "SSID = " + mWifiScanResultList.get(i).SSID + ",強度 = " + mWifiScanResultList.get(i).level);
            String wifiSSID = mWifiScanResultList.get(i).SSID.toString();
            if(ssid!=null) {
                if (ssid.contains(wifiSSID)) {
                    choweScanResult = mWifiScanResultList.get(i);
                } else {
                    wifiSSId.add(mWifiScanResultList.get(i));
                }
            }else
                wifilist.add(mWifiScanResultList.get(i).SSID.toString());
//            SSID (Wi-Fi名稱) = mWifiScanResultList.get(i).SSID ;
//            LEVEL (Wi-Fi訊號強弱) = mWifiScanResultList.get(i).level);
        }
        if(wifiSSId.size()>0){
            mWifiScanResultList = new ArrayList<>();
            if(choweScanResult!=null)
                mWifiScanResultList.add(choweScanResult);
            for(int i=0;i<wifiSSId.size();i++){
                mWifiScanResultList.add(wifiSSId.get(i));
            }
        }


        return mWifiScanResultList;
//        for(int i = 0 ; i < mWifiConfigurationList.size() ; i++ )
//        {
//            //手機內已儲存(已連線過)的Wi-Fi資訊
//            SSID (Wi-Fi名稱) = mWifiConfigurationList.get(i).SSID ;
//            NETWORKID (Wi-Fi連線ID) = mWifiConfigurationList.get(i).networkId ;
//        }
    }

    public WifiCipherType selectWifiCipherType(String type){
        WifiCipherType wifitype  = null;
       if (type.indexOf("NOPASS") != -1){
           wifitype =  WifiCipherType.WIFICIPHER_NOPASS;
       }else if (type.indexOf("WEP") != -1){
           wifitype = WifiCipherType.WIFICIPHER_WEP;
       }else if (type.indexOf("WPA") != -1) {
           wifitype = WifiCipherType.WIFICIPHER_WPA;
           if (type.indexOf("WPA2") != -1) {
               wifitype = WifiCipherType.WIFICIPHER_WPA2;
           }
       }
        return wifitype;
    }

    public boolean checkWEPmode(String password){
        if (password.length() != 5 || password.length() != 13 || password.length() != 10 || password.length() != 26)
            return false;
        else
            return true;
    }


    public static boolean isHexWepKey(String password){
        if (password.length() != 10 || password.length() != 26 )
            return false;
        else
            return true;
    }

    public boolean addNetwork(WifiConfiguration wcg) { // 添加一个网络配置并连接
        int wcgID = wifiManager.addNetwork(wcg);
        boolean b = wifiManager.enableNetwork(wcgID, true);
        System.out.println("addNetwork--" + wcgID);
        System.out.println("enableNetwork--" + b);
        return b;
    }

    public static WifiConfiguration createWifiConfiguration(String ssid, String password, WifiCipherType type) {
        WifiConfiguration newWifiConfiguration = new WifiConfiguration();
        newWifiConfiguration.allowedAuthAlgorithms.clear();
        newWifiConfiguration.allowedGroupCiphers.clear();
        newWifiConfiguration.allowedKeyManagement.clear();
        newWifiConfiguration.allowedPairwiseCiphers.clear();
        newWifiConfiguration.allowedProtocols.clear();
        newWifiConfiguration.SSID = "\"" + ssid + "\"";
        Log.d("createWifiConfiguration","ssid:"+ssid+",password:"+password+",type:"+type);
        if(type==null)
            return newWifiConfiguration;
        switch (type) {
            case WIFICIPHER_NOPASS:
                newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case IEEE8021XEAP:
                break;
            case WIFICIPHER_WEP:
                if (!TextUtils.isEmpty(password)) {
                    if (isHexWepKey(password)) {
                        newWifiConfiguration.wepKeys[0] = password;
                    } else {
                        newWifiConfiguration.wepKeys[0] = "\"" + password + "\"";
                    }
                }
                newWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                newWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                newWifiConfiguration.wepTxKeyIndex = 0;
                break;
            case WIFICIPHER_WPA:
                newWifiConfiguration.preSharedKey = "\"" + password + "\"";
                newWifiConfiguration.hiddenSSID = true;
                newWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                newWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                newWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                newWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                newWifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                break;
            case WIFICIPHER_WPA2:
                newWifiConfiguration.preSharedKey = "\"" + password + "\"";
                newWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                newWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                newWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                newWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                newWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                newWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                newWifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                break;
            default:
                return null;
        }
        return newWifiConfiguration;
    }

/**
 wificonfiguration有6个子类，详细的可以看api或搜一下，网上很多，我这里主要讲一下，配置不同的加密方式，6个子类分别要怎么设置：

 Wificonfiguration.AuthAlgorthm：他有三个参数：LEAP（这个对于普通的无线路由设置没有用，不用管，我也不太懂，希望有大神来解释下)、OPEN、SHARED。
 OPEN：当你要连接WPA/WPA2的加密的wifi信号时，配置这个
 SHARED：当你要连接WEP方式加密的wifi信号时，配置这个

 Wificonfiguration.Groupcipher：他有四个参数：CCMP、TKIP、WEP104、WEP40
 CCMP：对应的是加密算法AES，当无线信号是用此算法时，配置这个
 TKIP：对应的是加密算法TKIP，当无线信号用此算法时，配置这个
 WEP104、WEP40：当是WEP方式加密的时候，两个都配置

 Wificonfiguration.KeyMgmt：他有4个参数：IEE8021x、NONE、WPA_EAP、WPA_PSK
 IEE8021x：这个我个人认为是当使用的加密时WEP_EAP的方式的时候要配置，没有测试过，希望有人测试下
 NONE：当不是WPA方式时，配置这个参数
 WPA_EAP：很明显，WPA_EAP方式时配置
 WPA_PSK：这个和上面那个一样，WPA_PSK时配置

 Wificonfiguration.PairwiseCipher：他有三个参数：CCMP、NONE、TKIP，这个和上面Groupcipher是一样的，NONE就是没有选任何一个加密算法时配置就好
 Wificonfiguration.Protocol：他有两个参数：RSN、WPA
 RSN：当无线信号选择的加密是WPA2时，配置这个
 WPA：当无线信号选择的加密时WPA时，配置这个
 Wificonfiguration.status：这个我都是设置true，具体的也不太清楚。
 */

}
