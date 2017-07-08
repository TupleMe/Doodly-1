package tuple.me.dtools.fragement;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.io.IOException;
import java.io.RandomAccessFile;

import tuple.me.lily.Contexter;
import tuple.me.lily.util.CommonUtil;


@SuppressWarnings("UnusedDeclaration")
public class OverViewUtils {
    public static SystemStatus status = new SystemStatus();

    public static class SystemStatus {
        public boolean isCharging;
        public int batteryLevel;
        public boolean isChargingViaUsb;
        public boolean isChargingViaAc;
        public int batteryTemperatureLevel;
        public long[] cpu = new long[]{0, 0};
        public long[] memory = new long[]{0, 0};
    }

    private Context context;
    private ActivityManager activityManager;
    private IntentFilter batteryFilter;

    public OverViewUtils(Context context) {
        this.context = context;
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    }

    public void update() {
        status.memory = new long[]{0, 0};
        status.cpu = new long[]{0, 0};
        updateMemoryInfo();
        updateBatteryInfo();
        updateCpuInfo();
    }

    private void updateCpuInfo() {
        float usage = 0;
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" ");

            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            CommonUtil.sleep(250);
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" ");
            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            usage = (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        status.cpu[0] = (long) (usage * 100);
        status.cpu[1] = 100;
    }

    private void updateBatteryInfo() {
        Intent batteryStatus = Contexter.getAppContext().registerReceiver(null, batteryFilter);
        if (batteryStatus != null) {
            int extraStatus = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            status.isCharging = extraStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                    extraStatus == BatteryManager.BATTERY_STATUS_FULL;
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            status.isChargingViaUsb = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            status.isChargingViaAc = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            status.batteryTemperatureLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            status.batteryLevel = (int) (level * 100 / (float) scale);
        }
    }

    private void updateMemoryInfo() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi);
        status.memory[1] = mi.totalMem;
        status.memory[0] = mi.totalMem - mi.availMem;
    }
}
