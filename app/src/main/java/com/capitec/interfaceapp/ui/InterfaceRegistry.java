package com.capitec.interfaceapp.ui;

import com.capitec.interfaceapp.runtime.CapitecInterfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class InterfaceRegistry {
    private InterfaceRegistry() { }

    public static List<InterfaceEntry> all(CapitecInterfaces interfaces) {
        List<InterfaceEntry> entries = new ArrayList<>();
        entries.add(new InterfaceEntry("SensorsManager", "getAccelerometer", "AccelerometerData", "", interfaces.sensorsManager));
        entries.add(new InterfaceEntry("AirplaneManager", "isAirplaneMode", "boolean", "", interfaces.airplaneManager));
        entries.add(new InterfaceEntry("AirplaneManager", "toggleAirplaneMode", "void", "boolean active", interfaces.airplaneManager));
        entries.add(new InterfaceEntry("BatteryManager", "setBatteryOptimized", "void", "String packageName, boolean optimized", interfaces.batteryManager));
        entries.add(new InterfaceEntry("BatteryManager", "setShowBatteryPercentage", "void", "boolean show", interfaces.batteryManager));
        entries.add(new InterfaceEntry("BatteryManager", "getBatteryHealth", "int", "", interfaces.batteryManager));
        entries.add(new InterfaceEntry("BluetoothManager", "hasBluetooth", "boolean", "", interfaces.bluetoothManager));
        entries.add(new InterfaceEntry("BluetoothManager", "isBluetoothActive", "boolean", "", interfaces.bluetoothManager));
        entries.add(new InterfaceEntry("BluetoothManager", "toggleBluetooth", "void", "boolean enabled", interfaces.bluetoothManager));
        entries.add(new InterfaceEntry("BootManager", "restart", "void", "", interfaces.bootManager));
        entries.add(new InterfaceEntry("BootManager", "shutdown", "void", "", interfaces.bootManager));
        entries.add(new InterfaceEntry("BootManager", "factoryReset", "void", "", interfaces.bootManager));
        entries.add(new InterfaceEntry("BootManager", "setBootImage", "void", "String path", interfaces.bootManager));
        entries.add(new InterfaceEntry("BootManager", "setBootAnimation", "void", "String path", interfaces.bootManager));
        entries.add(new InterfaceEntry("BootManager", "setShutdownAnimation", "void", "String path", interfaces.bootManager));
        entries.add(new InterfaceEntry("BootManager", "getDailyRebootSchedule", "RebootSchedule", "", interfaces.bootManager));
        entries.add(new InterfaceEntry("BootManager", "setDailyRebootSchedule", "void", "RebootSchedule schedule", interfaces.bootManager));
        entries.add(new InterfaceEntry("BootManager", "cancelDailyRebootSchedule", "void", "", interfaces.bootManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getSerialNumber", "String", "", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getModel", "String", "", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getBrand", "String", "", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getManufacturer", "String", "", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getAndroidVersion", "String", "", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getRomVersion", "String", "", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getFirmwareVersion", "String", "", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getFirmwareId", "String", "", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getSystemProperty", "String", "String key", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getMac", "String", "", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("DeviceInfoManager", "getIPv4Address", "String", "", interfaces.deviceInfoManager));
        entries.add(new InterfaceEntry("FlashlightManager", "hasFlashlight", "boolean", "", interfaces.flashlightManager));
        entries.add(new InterfaceEntry("FlashlightManager", "isFlashlightActive", "boolean", "", interfaces.flashlightManager));
        entries.add(new InterfaceEntry("FlashlightManager", "toggleFlashlight", "void", "boolean enabled", interfaces.flashlightManager));
        entries.add(new InterfaceEntry("GpsManager", "hasGPS", "boolean", "", interfaces.gpsManager));
        entries.add(new InterfaceEntry("GpsManager", "isGPSActive", "boolean", "", interfaces.gpsManager));
        entries.add(new InterfaceEntry("GpsManager", "toggleGPS", "void", "boolean enabled", interfaces.gpsManager));
        entries.add(new InterfaceEntry("GpsManager", "getLastLocation", "GpsLocation", "", interfaces.gpsManager));
        entries.add(new InterfaceEntry("GpsManager", "getCurrentLocation", "GpsLocation", "", interfaces.gpsManager));
        entries.add(new InterfaceEntry("GpsManager", "startTracking", "void", "Priority priority, long interval, Callback callback", interfaces.gpsManager));
        entries.add(new InterfaceEntry("GpsManager", "stopTracking", "void", "", interfaces.gpsManager));
        entries.add(new InterfaceEntry("LogManager", "getLogs", "List<LogFile>", "", interfaces.logManager));
        entries.add(new InterfaceEntry("LogManager", "getLogContents", "FileInputStream", "String filePath", interfaces.logManager));
        entries.add(new InterfaceEntry("NfcManager", "hasNFC", "boolean", "", interfaces.nfcManager));
        entries.add(new InterfaceEntry("NfcManager", "isNFCActive", "boolean", "", interfaces.nfcManager));
        entries.add(new InterfaceEntry("NfcManager", "toggleNFC", "void", "boolean enabled", interfaces.nfcManager));
        entries.add(new InterfaceEntry("PhysicalKeyboardManager", "hasPhysicalKeyboard", "KeyboardType", "", interfaces.physicalKeyboardManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "isPowerOn", "boolean", "", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "powerOn", "void", "boolean on", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "getResolution", "DisplayResolution", "", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "getScreenBrightness", "int", "", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "setScreenBrightness", "void", "int percentage", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "getScreenTimeout", "int", "", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "setScreenTimeout", "void", "int seconds", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "getTheme", "Theme", "", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "setTheme", "void", "Theme theme", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "getFontSize", "FontSize", "", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "setFontSize", "void", "FontSize theme", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "getDisplaySize", "DisplaySize", "", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrimaryDisplayManager", "setDisplaySize", "void", "DisplaySize theme", interfaces.primaryDisplayManager));
        entries.add(new InterfaceEntry("PrintManager", "hasPrinter", "boolean", "", interfaces.printManager));
        entries.add(new InterfaceEntry("PrintManager", "setFont", "void", "Typeface typeface", interfaces.printManager));
        entries.add(new InterfaceEntry("PrintManager", "setFont", "void", "String path", interfaces.printManager));
        entries.add(new InterfaceEntry("PrintManager", "addEmptyLine", "void", "", interfaces.printManager));
        entries.add(new InterfaceEntry("PrintManager", "addTextLine", "void", "PrintText textLine", interfaces.printManager));
        entries.add(new InterfaceEntry("PrintManager", "addColumns", "void", "PrintColumn[] columns", interfaces.printManager));
        entries.add(new InterfaceEntry("PrintManager", "addBarcode", "void", "PrintBarcode barcode", interfaces.printManager));
        entries.add(new InterfaceEntry("PrintManager", "addQrCode", "void", "PrintQrCode qrCode", interfaces.printManager));
        entries.add(new InterfaceEntry("PrintManager", "addImage", "void", "PrintImage image", interfaces.printManager));
        entries.add(new InterfaceEntry("ScanManager", "hasScanner", "boolean", "", interfaces.scanManager));
        entries.add(new InterfaceEntry("ScanManager", "startScanner", "void", "CameraType cameraType, Callback onResult", interfaces.scanManager));
        entries.add(new InterfaceEntry("ScanManager", "stopScanner", "void", "", interfaces.scanManager));
        entries.add(new InterfaceEntry("SecondaryDisplayManager", "hasSecondDisplay", "boolean", "", interfaces.secondaryDisplayManager));
        entries.add(new InterfaceEntry("SecondaryDisplayManager", "isPowerOn", "boolean", "", interfaces.secondaryDisplayManager));
        entries.add(new InterfaceEntry("SecondaryDisplayManager", "powerOn", "void", "boolean on", interfaces.secondaryDisplayManager));
        entries.add(new InterfaceEntry("SecondaryDisplayManager", "getResolution", "DisplayResolution", "", interfaces.secondaryDisplayManager));
        entries.add(new InterfaceEntry("SecondaryDisplayManager", "setBotImage", "void", "String path", interfaces.secondaryDisplayManager));
        entries.add(new InterfaceEntry("SecondaryDisplayManager", "showImage", "void", "String path", interfaces.secondaryDisplayManager));
        entries.add(new InterfaceEntry("SecondaryDisplayManager", "showImageList", "void", "String[] path, int delayMs", interfaces.secondaryDisplayManager));
        entries.add(new InterfaceEntry("SecondaryDisplayManager", "showVideo", "void", "String path", interfaces.secondaryDisplayManager));
        entries.add(new InterfaceEntry("SecondaryDisplayManager", "getBrightness", "int", "", interfaces.secondaryDisplayManager));
        entries.add(new InterfaceEntry("SecondaryDisplayManager", "setBrightness", "void", "int percentage", interfaces.secondaryDisplayManager));
        entries.add(new InterfaceEntry("SecurityManager", "isSafeMode", "boolean", "", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "allowSafeMode", "void", "boolean enabled", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "allowUSB", "void", "boolean enabled", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "allowScreenshot", "void", "boolean enabled", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "isDeveloperMode", "boolean", "", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "setDeveloperMode", "void", "boolean enabled", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "getDeclaredPermissions", "String[]", "String packageName", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "grantPermission", "void", "String packageName, String[] permissions, boolean enabled", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "grantWriteSettings", "void", "String packageName, boolean enabled", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "allowLockScreen", "void", "boolean enabled", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "getLockTimeout", "int", "", interfaces.securityManager));
        entries.add(new InterfaceEntry("SecurityManager", "setLockTimeout", "void", "int seconds", interfaces.securityManager));
        entries.add(new InterfaceEntry("SettingsManager", "setSettingsPasscode", "void", "String passcode", interfaces.settingsManager));
        entries.add(new InterfaceEntry("SettingsManager", "allowAirplaneMode", "void", "boolean allowed", interfaces.settingsManager));
        entries.add(new InterfaceEntry("SettingsManager", "allowNetworkHotspot", "void", "boolean allowed", interfaces.settingsManager));
        entries.add(new InterfaceEntry("SettingsManager", "allowDataSaver", "void", "boolean allowed", interfaces.settingsManager));
        entries.add(new InterfaceEntry("SettingsManager", "allowSearch", "void", "boolean allow", interfaces.settingsManager));
        entries.add(new InterfaceEntry("SettingsManager", "allowForceStop", "void", "boolean allow", interfaces.settingsManager));
        entries.add(new InterfaceEntry("SettingsManager", "allowStorageAndCache", "void", "boolean allow", interfaces.settingsManager));
        entries.add(new InterfaceEntry("SimManager", "getSimSlots", "SimSlotType[]", "", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "getImei", "String", "int simSlot", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "getImsi", "String", "int simSlot", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "getIccid", "String", "int simSlot", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "getEid", "String", "int simSlot", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "activateSim", "void", "int simSlot, boolean enabled", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "setDataSim", "void", "int simSlot", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "getApnList", "ApnListItem[]", "int simSlot", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "getApnConfig", "ApnConfig", "int simSlot", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "addApn", "void", "int simSlot, ApnConfig config", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "deleteApn", "void", "int simSlot, ApnConfig config", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "selectApn", "void", "int simSlot, int apnId", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "resetApnConfig", "void", "int simSlot", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "getMobileDataUsageStats", "List<DataUsageStats>", "int subId, Date fromTimestamp, Date toTimestamp", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "downloadAndInstallESim", "void", "int simSlot, String lpaString, Callback callback", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "downloadAndInstallESim", "void", "int simSlot, String lpaString, String verificationCode, Callback callback", interfaces.simManager));
        entries.add(new InterfaceEntry("SimManager", "getESimProfiles", "List<SubscriptionInfo>", "int simSlot", interfaces.simManager));
        entries.add(new InterfaceEntry("SoftwareManager", "killApp", "void", "String packageName", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "restartApp", "void", "String packageName", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "startApp", "void", "String packageName", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "getInstalledApps", "List<String>", "boolean includeSystem", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "installApp", "void", "String path, Callback progress", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "uninstallApp", "void", "String packageName", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "allowUninstall", "void", "String packageName, boolean allow", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "updateFirmware", "void", "String path", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "enableManufacturerAppStore", "void", "boolean enabled", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "enableManufacturerAgent", "void", "boolean enabled", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "enableDuraSpeed", "void", "boolean enabled", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "silenceNotifications", "void", "String packageName, boolean enabled", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "enableService", "void", "String packageName, boolean enabled", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "restartAppWhenANR", "void", "String packageName, boolean enabled", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoftwareManager", "postAlert", "void", "String title, String message", interfaces.softwareManager));
        entries.add(new InterfaceEntry("SoundManager", "getTouchSounds", "boolean", "", interfaces.soundManager));
        entries.add(new InterfaceEntry("SoundManager", "setTouchSounds", "void", "boolean enabled", interfaces.soundManager));
        entries.add(new InterfaceEntry("SystemManager", "showStatusBar", "void", "boolean visible", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "showNavigationBar", "void", "boolean visible", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "showNavigationBarBackButton", "void", "boolean visible", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "showNavigationBarHomeButton", "void", "boolean visible", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "showNavigationBarRecentsButton", "void", "boolean visible", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "allowNotificationDrawer", "void", "boolean enabled", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "allowAppDrawer", "void", "boolean enabled", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "showQuickSettingTile", "void", "QuickSettingTile tile, boolean visible", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "setLauncher", "void", "String packageName", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "getLauncher", "String", "", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "addDesktopShortcut", "void", "String packageName", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "removeDesktopShortcut", "void", "String packageName", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "interceptVolumeKey", "void", "boolean enabled", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "setSystemString", "void", "SystemLabel labelType, String text", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "allowNoSimAlert", "void", "boolean enabled", interfaces.systemManager));
        entries.add(new InterfaceEntry("SystemManager", "postNotification", "void", "String title, String message", interfaces.systemManager));
        entries.add(new InterfaceEntry("TimeManager", "getTime", "long", "", interfaces.timeManager));
        entries.add(new InterfaceEntry("TimeManager", "setTime", "void", "long time", interfaces.timeManager));
        entries.add(new InterfaceEntry("TimeManager", "getTimeZone", "String", "", interfaces.timeManager));
        entries.add(new InterfaceEntry("TimeManager", "setTimeZone", "void", "String timeZone", interfaces.timeManager));
        entries.add(new InterfaceEntry("TimeManager", "setTimeAutomatically", "void", "TimeZoneSyncSetting setting", interfaces.timeManager));
        entries.add(new InterfaceEntry("TimeManager", "setTimeZoneAutomatically", "void", "boolean enabled", interfaces.timeManager));
        entries.add(new InterfaceEntry("TimeManager", "setTimeFormat", "void", "int format", interfaces.timeManager));
        entries.add(new InterfaceEntry("VirtualKeyboardManager", "allowKeyboardContacts", "void", "boolean enabled", interfaces.virtualKeyboardManager));
        entries.add(new InterfaceEntry("VirtualKeyboardManager", "allowKeyboardSuggestions", "void", "boolean enabled", interfaces.virtualKeyboardManager));
        entries.add(new InterfaceEntry("VirtualKeyboardManager", "allowKeyboardVoice", "void", "boolean enabled", interfaces.virtualKeyboardManager));
        entries.add(new InterfaceEntry("VirtualKeyboardManager", "allowKeyboardEmoji", "void", "boolean enabled", interfaces.virtualKeyboardManager));
        entries.add(new InterfaceEntry("VirtualKeyboardManager", "allowKeyboardDictionary", "void", "boolean enabled", interfaces.virtualKeyboardManager));
        entries.add(new InterfaceEntry("VirtualKeyboardManager", "allowKeyboardWordPrediction", "void", "boolean enabled", interfaces.virtualKeyboardManager));
        entries.add(new InterfaceEntry("WifiManager", "toggleWifi", "void", "boolean enabled", interfaces.wifiManager));
        entries.add(new InterfaceEntry("WifiManager", "getWifiNetworks", "WifiNetwork[]", "", interfaces.wifiManager));
        entries.add(new InterfaceEntry("WifiManager", "addWifiNetwork", "void", "String ssid, String password, WifiSecurityType security", interfaces.wifiManager));
        entries.add(new InterfaceEntry("WifiManager", "disconnectWifi", "void", "", interfaces.wifiManager));
        entries.add(new InterfaceEntry("WifiManager", "connectWifi", "void", "String ssid", interfaces.wifiManager));
        entries.add(new InterfaceEntry("WifiManager", "forgetWifi", "void", "String ssid", interfaces.wifiManager));
        entries.add(new InterfaceEntry("WifiManager", "getWifiDataUsageStats", "List<DataUsageStats>", "Date fromTimestamp, Date toTimestamp", interfaces.wifiManager));
        Collections.sort(entries, new Comparator<InterfaceEntry>() {
            @Override
            public int compare(InterfaceEntry left, InterfaceEntry right) {
                int manager = left.manager.compareToIgnoreCase(right.manager);
                if (manager != 0) return manager;
                int method = left.method.compareToIgnoreCase(right.method);
                if (method != 0) return method;
                return left.parameters.compareToIgnoreCase(right.parameters);
            }
        });
        for (int i = 0; i < entries.size(); i++) {
            InterfaceEntry entry = entries.get(i);
            entry.index = i + 1;
            entry.source = sourceFor(entry);
        }
        return entries;
    }

    private static String sourceFor(InterfaceEntry entry) {
        String id = entry.manager + "." + entry.method + "#" + entry.parameters;
        if (isFinancialExempt(id)) {
            return "FinancialLib";
        }
        if (isComponentExempt(id)) {
            return "ComponentLib";
        }
        if (isXchengFramework(id)) {
            return "xcheng-framework.jar";
        }
        if (isUnsupported(id)) {
            return "Unsupported/Reserved";
        }
        if (isTerminalManagerLib(id)) {
            return "TerminalManagerLib";
        }
        return "Android System API";
    }

    private static boolean isXchengFramework(String id) {
        return id.equals("AirplaneManager.isAirplaneMode#")
                || id.equals("AirplaneManager.toggleAirplaneMode#boolean active")
                || id.equals("BatteryManager.setShowBatteryPercentage#boolean show")
                || id.equals("BatteryManager.getBatteryHealth#")
                || id.equals("BluetoothManager.isBluetoothActive#")
                || id.equals("BluetoothManager.toggleBluetooth#boolean enabled")
                || id.equals("BootManager.factoryReset#")
                || id.equals("BootManager.setBootImage#String path")
                || id.equals("BootManager.setBootAnimation#String path")
                || id.equals("DeviceInfoManager.getFirmwareId#")
                || id.equals("DeviceInfoManager.getRomVersion#")
                || id.equals("DeviceInfoManager.getSystemProperty#String key")
                || id.equals("DeviceInfoManager.getMac#")
                || id.equals("GpsManager.isGPSActive#")
                || id.equals("GpsManager.toggleGPS#boolean enabled")
                || id.equals("PrimaryDisplayManager.isPowerOn#")
                || id.equals("PrimaryDisplayManager.powerOn#boolean on")
                || id.equals("PrimaryDisplayManager.getResolution#")
                || id.equals("PrimaryDisplayManager.getScreenBrightness#")
                || id.equals("PrimaryDisplayManager.setScreenBrightness#int percentage")
                || id.equals("PrimaryDisplayManager.getScreenTimeout#")
                || id.equals("PrimaryDisplayManager.setDisplaySize#DisplaySize theme")
                || id.equals("SecurityManager.isSafeMode#")
                || id.equals("SecurityManager.getLockTimeout#")
                || id.equals("SecurityManager.setLockTimeout#int seconds")
                || id.equals("SettingsManager.setSettingsPasscode#String passcode")
                || id.equals("SettingsManager.allowNetworkHotspot#boolean allowed")
                || id.equals("SettingsManager.allowSearch#boolean allow")
                || id.equals("SettingsManager.allowStorageAndCache#boolean allow")
                || id.equals("SimManager.setDataSim#int simSlot")
                || id.equals("SimManager.resetApnConfig#int simSlot")
                || id.equals("SoftwareManager.getInstalledApps#boolean includeSystem")
                || id.equals("SoftwareManager.allowUninstall#String packageName, boolean allow")
                || id.equals("SoftwareManager.silenceNotifications#String packageName, boolean enabled")
                || id.equals("SoftwareManager.enableService#String packageName, boolean enabled")
                || id.equals("SystemManager.showStatusBar#boolean visible")
                || id.equals("SystemManager.showNavigationBar#boolean visible")
                || id.equals("SystemManager.showNavigationBarBackButton#boolean visible")
                || id.equals("SystemManager.showNavigationBarHomeButton#boolean visible")
                || id.equals("SystemManager.showNavigationBarRecentsButton#boolean visible")
                || id.equals("SystemManager.allowNotificationDrawer#boolean enabled")
                || id.equals("SystemManager.showQuickSettingTile#QuickSettingTile tile, boolean visible")
                || id.equals("SystemManager.setLauncher#String packageName")
                || id.equals("SystemManager.getLauncher#")
                || id.equals("SystemManager.addDesktopShortcut#String packageName")
                || id.equals("SystemManager.removeDesktopShortcut#String packageName")
                || id.equals("SystemManager.interceptVolumeKey#boolean enabled")
                || id.equals("TimeManager.setTimeFormat#int format")
                || id.equals("WifiManager.toggleWifi#boolean enabled")
                || id.equals("WifiManager.getWifiNetworks#")
                || id.equals("WifiManager.addWifiNetwork#String ssid, String password, WifiSecurityType security")
                || id.equals("WifiManager.disconnectWifi#")
                || id.equals("WifiManager.connectWifi#String ssid")
                || id.equals("WifiManager.forgetWifi#String ssid")
                ;
    }

    private static boolean isFinancialExempt(String id) {
        return id.equals("PrintManager.hasPrinter#")
                || id.equals("PrintManager.setFont#Typeface typeface")
                || id.equals("PrintManager.setFont#String path")
                || id.equals("PrintManager.addEmptyLine#")
                || id.equals("PrintManager.addTextLine#PrintText textLine")
                || id.equals("PrintManager.addColumns#PrintColumn[] columns")
                || id.equals("PrintManager.addBarcode#PrintBarcode barcode")
                || id.equals("PrintManager.addQrCode#PrintQrCode qrCode")
                || id.equals("PrintManager.addImage#PrintImage image")
                || id.equals("NfcManager.toggleNFC#boolean enabled")
                || id.equals("ScanManager.startScanner#CameraType cameraType, Callback onResult")
                || id.equals("ScanManager.stopScanner#");
    }

    private static boolean isComponentExempt(String id) {
        return id.equals("SecondaryDisplayManager.hasSecondDisplay#")
                || id.equals("SecondaryDisplayManager.isPowerOn#")
                || id.equals("SecondaryDisplayManager.powerOn#boolean on")
                || id.equals("SecondaryDisplayManager.getResolution#")
                || id.equals("SecondaryDisplayManager.setBotImage#String path")
                || id.equals("SecondaryDisplayManager.showImage#String path")
                || id.equals("SecondaryDisplayManager.showImageList#String[] path, int delayMs")
                || id.equals("SecondaryDisplayManager.showVideo#String path")
                || id.equals("SecondaryDisplayManager.getBrightness#")
                || id.equals("SecondaryDisplayManager.setBrightness#int percentage");
    }

    private static boolean isTerminalManagerLib(String id) {
        return id.equals("BootManager.restart#")
                || id.equals("BootManager.shutdown#")
                || id.equals("BootManager.cancelDailyRebootSchedule#")
                || id.equals("DeviceInfoManager.getSerialNumber#")
                || id.equals("GpsManager.getCurrentLocation#")
                || id.equals("GpsManager.startTracking#Priority priority, long interval, Callback callback")
                || id.equals("GpsManager.stopTracking#")
                || id.equals("LogManager.getLogs#")
                || id.equals("LogManager.getLogContents#String filePath")
                || id.equals("PrimaryDisplayManager.setScreenTimeout#int seconds")
                || id.equals("SecurityManager.grantPermission#String packageName, String[] permissions, boolean enabled")
                || id.equals("SecurityManager.isDeveloperMode#")
                || id.equals("SettingsManager.allowDataSaver#boolean allowed")
                || id.equals("SimManager.getImei#int simSlot")
                || id.equals("SimManager.getImsi#int simSlot")
                || id.equals("SimManager.addApn#int simSlot, ApnConfig config")
                || id.equals("SimManager.getApnList#int simSlot")
                || id.equals("SimManager.getApnConfig#int simSlot")
                || id.equals("SimManager.deleteApn#int simSlot, ApnConfig config")
                || id.equals("SimManager.selectApn#int simSlot, int apnId")
                || id.equals("SoftwareManager.installApp#String path, Callback progress")
                || id.equals("SoftwareManager.uninstallApp#String packageName")
                || id.equals("SoftwareManager.updateFirmware#String path")
                || id.equals("TimeManager.getTime#")
                || id.equals("TimeManager.setTime#long time")
                || id.equals("TimeManager.getTimeZone#")
                || id.equals("TimeManager.setTimeZone#String timeZone")
                || id.equals("TimeManager.setTimeAutomatically#TimeZoneSyncSetting setting")
                || id.equals("TimeManager.setTimeZoneAutomatically#boolean enabled")
                || id.equals("WifiManager.getWifiDataUsageStats#Date fromTimestamp, Date toTimestamp");
    }

    private static boolean isUnsupported(String id) {
        return id.startsWith("VirtualKeyboardManager.")
                || id.equals("BootManager.factoryReset#")
                || id.equals("BootManager.setBootImage#String path")
                || id.equals("BootManager.setShutdownAnimation#String path")
                || id.equals("BootManager.getDailyRebootSchedule#")
                || id.equals("BootManager.setDailyRebootSchedule#RebootSchedule schedule")
                || id.equals("PrimaryDisplayManager.setDisplaySize#DisplaySize theme")
                || id.equals("SecurityManager.allowLockScreen#boolean enabled")
                || id.equals("SecurityManager.allowSafeMode#boolean enabled")
                || id.equals("SecurityManager.allowScreenshot#boolean enabled")
                || id.equals("SecurityManager.allowUSB#boolean enabled")
                || id.equals("SecurityManager.setDeveloperMode#boolean enabled")
                || id.equals("SettingsManager.setSettingsPasscode#String passcode")
                || id.equals("SettingsManager.allowNetworkHotspot#boolean allowed")
                || id.equals("SettingsManager.allowSearch#boolean allow")
                || id.equals("SettingsManager.allowForceStop#boolean allow")
                || id.equals("SettingsManager.allowStorageAndCache#boolean allow")
                || id.equals("SimManager.activateSim#int simSlot, boolean enabled")
                || id.equals("SimManager.setDataSim#int simSlot")
                || id.equals("SimManager.getMobileDataUsageStats#int subId, Date fromTimestamp, Date toTimestamp")
                || id.equals("SimManager.resetApnConfig#int simSlot")
                || id.equals("SimManager.downloadAndInstallESim#int simSlot, String lpaString, Callback callback")
                || id.equals("SimManager.downloadAndInstallESim#int simSlot, String lpaString, String verificationCode, Callback callback")
                || id.equals("SoftwareManager.enableManufacturerAppStore#boolean enabled")
                || id.equals("SoftwareManager.enableManufacturerAgent#boolean enabled")
                || id.equals("SoftwareManager.enableDuraSpeed#boolean enabled")
                || id.equals("SoftwareManager.restartAppWhenANR#String packageName, boolean enabled")
                || id.equals("SystemManager.showNavigationBarBackButton#boolean visible")
                || id.equals("SystemManager.showNavigationBarHomeButton#boolean visible")
                || id.equals("SystemManager.showNavigationBarRecentsButton#boolean visible")
                || id.equals("SystemManager.allowAppDrawer#boolean enabled")
                || id.equals("SystemManager.showQuickSettingTile#QuickSettingTile tile, boolean visible")
                || id.equals("SystemManager.interceptVolumeKey#boolean enabled")
                || id.equals("SystemManager.setSystemString#SystemLabel labelType, String text")
                || id.equals("SystemManager.allowNoSimAlert#boolean enabled");
    }
}
