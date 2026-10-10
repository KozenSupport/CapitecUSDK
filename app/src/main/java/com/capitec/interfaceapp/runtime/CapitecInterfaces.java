package com.capitec.interfaceapp.runtime;

import android.content.Context;

import com.capitec.interfaceapp.CapitecApplication;
import com.capitec.interfaceapp.api.managers.airplanemanager.IAirplaneManager;
import com.capitec.interfaceapp.api.managers.batterymanager.IBatteryManager;
import com.capitec.interfaceapp.api.managers.bluetoothmanager.IBluetoothManager;
import com.capitec.interfaceapp.api.managers.bootmanager.IBootManager;
import com.capitec.interfaceapp.api.managers.deviceinfomanager.IDeviceInfoManager;
import com.capitec.interfaceapp.api.managers.flashlightmanager.IFlashlightManager;
import com.capitec.interfaceapp.api.managers.gpsmanager.IGpsManager;
import com.capitec.interfaceapp.api.managers.logmanager.ILogManager;
import com.capitec.interfaceapp.api.managers.nfcmanager.INfcManager;
import com.capitec.interfaceapp.api.managers.physicalkeyboardmanager.IPhysicalKeyboardManager;
import com.capitec.interfaceapp.api.managers.primarydisplaymanager.IPrimaryDisplayManager;
import com.capitec.interfaceapp.api.managers.printmanager.IPrintManager;
import com.capitec.interfaceapp.api.managers.scanmanager.IScanManager;
import com.capitec.interfaceapp.api.managers.secondarydisplaymanager.ISecondaryDisplayManager;
import com.capitec.interfaceapp.api.managers.securitymanager.ISecurityManager;
import com.capitec.interfaceapp.api.managers.sensorsmanager.ISensorsManager;
import com.capitec.interfaceapp.api.managers.settingsmanager.ISettingsManager;
import com.capitec.interfaceapp.api.managers.simmanager.ISimManager;
import com.capitec.interfaceapp.api.managers.softwaremanager.ISoftwareManager;
import com.capitec.interfaceapp.api.managers.soundmanager.ISoundManager;
import com.capitec.interfaceapp.api.managers.systemmanager.ISystemManager;
import com.capitec.interfaceapp.api.managers.timemanager.ITimeManager;
import com.capitec.interfaceapp.api.managers.virtualkeyboardmanager.IVirtualKeyboardManager;
import com.capitec.interfaceapp.api.managers.wifimanager.IWifiManager;
import com.capitec.interfaceapp.impl.AirplaneManager;
import com.capitec.interfaceapp.impl.BatteryManager;
import com.capitec.interfaceapp.impl.BluetoothManager;
import com.capitec.interfaceapp.impl.BootManager;
import com.capitec.interfaceapp.impl.DeviceInfoManager;
import com.capitec.interfaceapp.impl.FlashlightManager;
import com.capitec.interfaceapp.impl.GpsManager;
import com.capitec.interfaceapp.impl.LogManager;
import com.capitec.interfaceapp.impl.NfcManager;
import com.capitec.interfaceapp.impl.PhysicalKeyboardManager;
import com.capitec.interfaceapp.impl.PrimaryDisplayManager;
import com.capitec.interfaceapp.impl.PrintManager;
import com.capitec.interfaceapp.impl.ScanManager;
import com.capitec.interfaceapp.impl.SecondaryDisplayManager;
import com.capitec.interfaceapp.impl.SecurityManager;
import com.capitec.interfaceapp.impl.SensorsManager;
import com.capitec.interfaceapp.impl.SettingsManager;
import com.capitec.interfaceapp.impl.SimManager;
import com.capitec.interfaceapp.impl.SoftwareManager;
import com.capitec.interfaceapp.impl.SoundManager;
import com.capitec.interfaceapp.impl.SystemManager;
import com.capitec.interfaceapp.impl.TimeManager;
import com.capitec.interfaceapp.impl.VirtualKeyboardManager;
import com.capitec.interfaceapp.impl.WifiManager;

public final class CapitecInterfaces {
    private final Context appContext;
    private final LastResultStore results = new LastResultStore();
    private final InterfaceResultRecorder resultRecorder = new InterfaceResultRecorder(results);

    public final IAirplaneManager airplaneManager;
    public final IBatteryManager batteryManager;
    public final IBluetoothManager bluetoothManager;
    public final IBootManager bootManager;
    public final IDeviceInfoManager deviceInfoManager;
    public final IFlashlightManager flashlightManager;
    public final IGpsManager gpsManager;
    public final ILogManager logManager;
    public final INfcManager nfcManager;
    public final IPhysicalKeyboardManager physicalKeyboardManager;
    public final IPrimaryDisplayManager primaryDisplayManager;
    public final IPrintManager printManager;
    public final IScanManager scanManager;
    public final ISecondaryDisplayManager secondaryDisplayManager;
    public final ISecurityManager securityManager;
    public final ISensorsManager sensorsManager;
    public final ISettingsManager settingsManager;
    public final ISimManager simManager;
    public final ISoftwareManager softwareManager;
    public final ISoundManager soundManager;
    public final ISystemManager systemManager;
    public final ITimeManager timeManager;
    public final IVirtualKeyboardManager virtualKeyboardManager;
    public final IWifiManager wifiManager;

    public CapitecInterfaces(Context context) {
        appContext = context.getApplicationContext();
        airplaneManager = new AirplaneManager(appContext);
        batteryManager = new BatteryManager(appContext);
        bluetoothManager = new BluetoothManager(appContext);
        bootManager = new BootManager(appContext);
        deviceInfoManager = new DeviceInfoManager(appContext);
        flashlightManager = new FlashlightManager(appContext);
        gpsManager = new GpsManager(appContext);
        logManager = new LogManager(appContext);
        nfcManager = new NfcManager(appContext);
        physicalKeyboardManager = new PhysicalKeyboardManager(appContext);
        primaryDisplayManager = new PrimaryDisplayManager(appContext);
        printManager = new PrintManager(appContext);
        scanManager = new ScanManager(appContext);
        secondaryDisplayManager = new SecondaryDisplayManager(appContext);
        securityManager = new SecurityManager(appContext);
        sensorsManager = new SensorsManager(appContext);
        settingsManager = new SettingsManager(appContext);
        simManager = new SimManager(appContext);
        softwareManager = new SoftwareManager(appContext);
        soundManager = new SoundManager(appContext);
        systemManager = new SystemManager(appContext);
        timeManager = new TimeManager(appContext);
        virtualKeyboardManager = new VirtualKeyboardManager(appContext);
        wifiManager = new WifiManager(appContext);
    }

    public String terminalStatus() {
        return CapitecApplication.sdkStatus();
    }

    public InterfaceResult lastResult() {
        return results.get();
    }

    public void recordSuccess(String title, String source, Object value) {
        if (source == null) {
            resultRecorder.androidSuccess(title, "", value);
            return;
        }
        if (source.startsWith("TerminalManagerLib")) {
            resultRecorder.terminalSuccess(title, value);
            return;
        }
        if (source.startsWith("FinancialLib")) {
            resultRecorder.financialSuccess(title, value);
            return;
        }
        if (source.startsWith("ComponentLib")) {
            resultRecorder.componentSuccess(title, value);
            return;
        }
        if (source.startsWith("xcheng-framework.jar")) {
            resultRecorder.xchengSuccess(title, value);
            return;
        }
        if (source.startsWith("Unsupported")) {
            resultRecorder.unsupported(title, source);
            return;
        }
        resultRecorder.androidSuccess(title, source, value);
    }

    public InterfaceResult recordUnsupported(String title, UnsupportedInterfaceException exception) {
        resultRecorder.unsupported(title, exception.note);
        return lastResult();
    }
}
