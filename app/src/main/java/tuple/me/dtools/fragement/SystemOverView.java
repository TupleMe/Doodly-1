package tuple.me.dtools.fragement;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import tuple.me.dtools.R;
import tuple.me.dtools.file.util.FileManagerUtil;
import tuple.me.dtools.view.StorageList;
import tuple.me.dtools.view.bar.BarData;
import tuple.me.dtools.view.bar.BarView;
import tuple.me.lily.Contexter;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.RangeUtils;
import tuple.me.lily.util.async.SimpleJob;
import tuple.me.lily.util.async.doInBackground;
import tuple.me.lily.util.async.onComplete;
import tuple.me.lily.views.LeftRightTextView;

public class SystemOverView extends Fragment implements doInBackground<Void, Void>, onComplete<Void> {
    private TextView cpuUsage;
    private BarView cpuBar;
    private TextView memoryUsage;
    private BarView memoryBar;
    private TextView batteryPercent;
    private BarView batteryBar;
    private StorageList storageList;
    private LeftRightTextView batttaryChargingState;
    private LeftRightTextView batteryChargingMethod;
    private long[] cpu = new long[]{0, 0};
    private long[] memory = new long[]{0, 0};
    private SimpleJob<Void, Void> cpuJob = new SimpleJob<>();
    private Handler mHandler = new Handler();
    private BarData memoryBarData = new BarData(1000);
    private BarData cpuBarData = new BarData(1000);
    private BarData batteryBarData = new BarData(100);
    private BarData.Bar batteryBlock = new BarData.Bar(1).setColorResource(R.color.colorAccent);
    private BarData.Bar memoryBlock = new BarData.Bar(1).setColorResource(R.color.colorAccent);
    private BarData.Bar cpuBlock = new BarData.Bar(1).setColorResource(R.color.colorAccent);
    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private boolean isCharging;
    private int batteryLevel;
    private boolean isChargingViaUsb;
    private boolean isChargingViaAc;
    private int batteryTemperatureLevel;
    private LeftRightTextView batteryTemperature;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.overview_fragment, container, false);
        cpuUsage = (TextView) rootView.findViewById(R.id.percent);
        cpuBar = (BarView) rootView.findViewById(R.id.percent_bar);
        memoryUsage = (TextView) rootView.findViewById(R.id.memory_percent);
        memoryBar = (BarView) rootView.findViewById(R.id.memory_percent_bar);
        batteryTemperature = (LeftRightTextView) rootView.findViewById(R.id.battery_temperature);
        batteryChargingMethod = (LeftRightTextView) rootView.findViewById(R.id.battery_charging_method);
        storageList = (StorageList) rootView.findViewById(R.id.storage_list);
        memoryBarData.add(memoryBlock);
        cpuBarData.add(cpuBlock);
        batteryBarData.add(batteryBlock);
        batteryPercent = (TextView) rootView.findViewById(R.id.battery_percent);
        batteryBar = (BarView) rootView.findViewById(R.id.battery_percent_bar);
        batttaryChargingState = (LeftRightTextView) rootView.findViewById(R.id.battery_charging_state);
        batttaryChargingState.setLeftText(getString(R.string.is_charging));
        batteryChargingMethod.setLeftText(getString(R.string.charging_method));
        batteryTemperature.setLeftText(getString(R.string.temperature));
        batteryChargingMethod.setRightText("");
        batttaryChargingState.setRightText("");
        setStorageList();
        return rootView;
    }

    private void setStorageList() {
        com.arasthel.asyncjob.AsyncJob.doInBackground(new com.arasthel.asyncjob.AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                List<String> storages = FileUtils.getStorageDirectories(Contexter.getAppContext());
                final List<FileManagerUtil.StorageItem> listItems = new ArrayList<>();
                for (String storage : storages) {
                    listItems.add(new FileManagerUtil.StorageItem(storage));
                }
                com.arasthel.asyncjob.AsyncJob.doOnMainThread(new com.arasthel.asyncjob.AsyncJob.OnMainThreadJob() {
                    @Override
                    public void doInUIThread() {
                        storageList.setStorages(listItems);

                    }
                });
            }
        });
    }

    public List<Sensor> getAllSensors() {
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        return sensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    @Override
    public void onResume() {
        super.onResume();
        cpuJob.register(null, this, this);
        mHandler.postDelayed(mStatusChecker, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        cpuJob.unregister();
    }

    @NonNull
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            cpuJob.doAgainIfCompleted();
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Void doInBackground(Void[] parms) throws Exception {
        memory = new long[]{0, 0};
        cpu = new long[]{0, 0};
        ActivityManager activityManager = (ActivityManager) Contexter.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi);
        memory[1] = mi.totalMem;
        memory[0] = mi.totalMem - mi.availMem;
        Intent batteryStatus = Contexter.getAppContext().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isChargingViaUsb = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        isChargingViaAc = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        batteryTemperatureLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryLevel = (int) (level * 100 / (float) scale);
        memoryBlock.cap = memory[0];
        memoryBarData.totalCap = memory[1];
        cpuBlock.cap = cpu[0] = (long) (readUsage() * 100);
        cpuBarData.totalCap = cpu[1] = 100;
        batteryBlock.cap = batteryLevel;
        return null;
    }

    @Override
    public boolean onSuccess(Void result) {
        cpuBar.setBarData(cpuBarData);
        memoryBar.setBarData(memoryBarData);
        batteryBar.setBarData(batteryBarData);
        cpuUsage.setText((int) (RangeUtils.safeDivide(cpu[0], cpu[1]) * 100) + "%");
        memoryUsage.setText((int) (RangeUtils.safeDivide(memory[0], memory[1]) * 100) + "%");
        batteryPercent.setText(batteryLevel + "%");
        batttaryChargingState.setRightText(isCharging ? "True" : "False");
        if (isCharging) {
            if (isChargingViaAc) {
                batteryChargingMethod.setRightText(getString(R.string.ac_charging));
            } else if (isChargingViaUsb) {
                batteryChargingMethod.setRightText(getString(R.string.usb_charging));
            } else {
                batteryChargingMethod.setRightText(getString(R.string.unknown));
            }
        } else {
            batteryChargingMethod.setRightText(getString(R.string.none));
        }
        batteryTemperature.setRightText(RangeUtils.safeDivide(batteryTemperatureLevel, 10) + " °C");
        mHandler.postDelayed(mStatusChecker, 5000);
        return false;
    }

    @Override
    public void onError(Exception exception) {
        Timber.e(exception);
    }

    private float readUsage() {
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

            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}
