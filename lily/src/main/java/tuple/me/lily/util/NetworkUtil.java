package tuple.me.lily.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

import tuple.me.lily.Contexter;

/**
 * Created by gokul-4192 on 0025 25-Feb-17.
 */
public class NetworkUtil {
    public static boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) Contexter.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() &&
                networkInfo.isConnected();
    }

    public static boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) Contexter.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static long ipToLong(String ipAddress) {
        ipAddress = ipAddress.replace(" ", "");
        String[] ipAddressInArray = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {

            int power = 3 - i;
            result += (Integer.parseInt(ipAddressInArray[i]) % 256 * Math.pow(256, power));

        }
        return result;
    }

    @NonNull
    public static String longToIp(Long ip) {

        return ((ip >> 24) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);

    }

    @Nullable
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean isApOn() {
        try {
            final WifiManager wifiManager = (WifiManager) Contexter.getAppContext().getSystemService(Context.WIFI_SERVICE);
            final int apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);
            if (apState == 13) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isWifiOn() {
        try {
            final WifiManager wifiManager = (WifiManager) Contexter.getAppContext().getSystemService(Context.WIFI_SERVICE);
            return wifiManager.isWifiEnabled();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isWifiOnAndConnected() {
        try {
            final WifiManager wifiManager = (WifiManager) Contexter.getAppContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info.getNetworkId() != -1) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setWifiState(boolean status) {
        WifiManager wifiManager = (WifiManager) Contexter.getAppContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (status && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        } else if (!status && wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }

    }

    public static boolean turnOnAp(String ssid) {

        WifiConfiguration netConfig = new WifiConfiguration();
        netConfig.SSID = ssid;
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        try {
            WifiManager wifiManager = (WifiManager) Contexter.getAppContext()
                    .getSystemService(Context.WIFI_SERVICE);
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) setWifiApMethod.invoke(wifiManager, netConfig, true);
        } catch (Exception e) {
            e.printStackTrace();
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            final ComponentName cn = new ComponentName(
                    "com.android.settings",
                    "com.android.settings.TetherSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Contexter.getAppContext().startActivity(intent);
            return false;
        }
    }

    public static boolean isPortAvailable(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
        } finally {
            SafeClose.safeClose(ds);
            SafeClose.safeClose(ss);
        }
        return false;
    }
}
