package com.capitec.interfaceapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.capitec.interfaceapp.api.managers.bootmanager.DayOfWeek;
import com.capitec.interfaceapp.api.managers.bootmanager.RebootSchedule;
import com.capitec.interfaceapp.api.managers.printmanager.PrintAlign;
import com.capitec.interfaceapp.api.managers.printmanager.PrintBarcode;
import com.capitec.interfaceapp.api.managers.printmanager.PrintColumn;
import com.capitec.interfaceapp.api.managers.printmanager.PrintImage;
import com.capitec.interfaceapp.api.managers.printmanager.PrintQrCode;
import com.capitec.interfaceapp.api.managers.printmanager.PrintText;
import com.capitec.interfaceapp.api.managers.printmanager.Typeface;
import com.capitec.interfaceapp.api.managers.simmanager.ApnConfig;
import com.capitec.interfaceapp.api.managers.timemanager.TimeFormat;
import com.capitec.interfaceapp.runtime.CapitecInterfaces;
import com.capitec.interfaceapp.runtime.InterfaceResult;
import com.capitec.interfaceapp.runtime.UnsupportedInterfaceException;
import com.capitec.interfaceapp.runtime.ValueFormatter;
import com.kozen.terminalmanager.TerminalManager;
import com.capitec.interfaceapp.ui.InterfaceEntry;
import com.capitec.interfaceapp.ui.InterfaceRegistry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AutoTestActivity extends Activity {
    private TextView output;
    private CapitecInterfaces interfaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        output = new TextView(this);
        output.setTextSize(12f);
        output.setPadding(18, 18, 18, 18);
        output.setMovementMethod(new ScrollingMovementMethod());
        setContentView(output, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        if (getIntent().getBooleanExtra("showResultOnly", false)) {
            showLastResult();
            return;
        }
        interfaces = new CapitecInterfaces(this);
        new Thread(this::runAutoTests, "capitec-auto-tests").start();
    }

    private void showLastResult() {
        File dir = getExternalFilesDir(null);
        if (dir == null) dir = getFilesDir();
        File file = new File(dir, "capitec-auto-test-results.json");
        StringBuilder builder = new StringBuilder("Capitec automated test result\n\n");
        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[4096];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, count);
            }
        } catch (Exception exception) {
            builder.append("Unable to read last result: ").append(exception);
        }
        output.setText(builder.toString());
    }

    private void runAutoTests() {
        JSONArray results = new JSONArray();
        List<InterfaceEntry> entries = InterfaceRegistry.all(interfaces);
        String onlyManager = getIntent().getStringExtra("onlyManager");
        String onlyMethods = getIntent().getStringExtra("onlyMethods");
        int executed = 0;
        int passed = 0;
        int failed = 0;
        int unsupported = 0;
        int prerequisiteFailed = 0;
        int skipped = 0;
        publish("Capitec automated test started\nEntries: " + entries.size() + "\n");
        publish("SDK status: " + interfaces.terminalStatus());
        sleepQuietly(3000L);
        for (InterfaceEntry entry : entries) {
            if (!matchesRequestedScope(entry, onlyManager, onlyMethods)) {
                continue;
            }
            JSONObject item = new JSONObject();
            try {
                item.put("index", entry.index);
                item.put("manager", entry.manager);
                item.put("method", entry.method);
                item.put("methodId", methodId(entry));
                item.put("title", entry.title());
                item.put("parameters", entry.parameters);
                item.put("returnType", entry.returnType);
                item.put("source", entry.source);
                item.put("documentSource", "DOC/device-evaluation-KOZEN(1).pdf");
                item.put("sampleInput", sampleInputText(entry));
                item.put("sampleInputSource", sampleInputSource(entry));
                item.put("externalVerificationKey", externalVerificationKey(entry));
                item.put("evidenceMode", evidenceMode(entry));
                String skipReason = skipReason(entry);
                if (skipReason.length() > 0) {
                    skipped++;
                    item.put("status", "SKIPPED");
                    item.put("reason", skipReason);
                    results.put(item);
                    continue;
                }
                executed++;
                Object value = execute(entry);
                InterfaceResult result = interfaces.lastResult();
                item.put("status", "PASS");
                item.put("route", result == null ? "" : String.valueOf(result.route));
                item.put("resultMessage", result == null ? "" : result.message);
                item.put("value", ValueFormatter.format(value));
                item.put("externalEvidence", externalEvidenceHint(entry));
                if (requiresManualReview(entry)) {
                    item.put("status", "PASS_MANUAL_REVIEW");
                    item.put("manualReviewRequired", true);
                    item.put("manualReviewPrompt", manualReviewPrompt(entry));
                }
                passed++;
            } catch (Throwable throwable) {
                Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
                boolean isUnsupported = cause instanceof UnsupportedInterfaceException;
                boolean isPrerequisiteFailed = isDevicePrerequisiteFailure(entry, cause);
                if (isPrerequisiteFailed) {
                    prerequisiteFailed++;
                } else if (isUnsupported) {
                    unsupported++;
                } else {
                    failed++;
                }
                try {
                    item.put("status", isPrerequisiteFailed ? "DEVICE_PREREQUISITE_FAILED" : (isUnsupported ? "UNSUPPORTED" : "FAIL"));
                    item.put("errorType", cause.getClass().getSimpleName());
                    item.put("errorMessage", String.valueOf(cause.getMessage()));
                    item.put("externalEvidence", externalEvidenceHint(entry));
                } catch (Exception ignored) {
                }
            }
            results.put(item);
            if ((executed + skipped) % 10 == 0) {
                publish("Progress: executed=" + executed + " passed=" + passed + " prerequisiteFailed=" + prerequisiteFailed + " unsupported=" + unsupported + " failed=" + failed + " skipped=" + skipped);
            }
        }
        writeResultFile(results, executed, passed, prerequisiteFailed, unsupported, failed, skipped);
    }

    private boolean matchesRequestedScope(InterfaceEntry entry, String onlyManager, String onlyMethods) {
        if (onlyManager != null && onlyManager.trim().length() > 0 && !entry.manager.equals(onlyManager.trim())) {
            return false;
        }
        if (onlyMethods == null || onlyMethods.trim().length() == 0) {
            return true;
        }
        String method = entry.method.trim();
        for (String candidate : onlyMethods.split(",")) {
            if (method.equals(candidate.trim())) {
                return true;
            }
        }
        return false;
    }

    private Object execute(InterfaceEntry entry) throws Exception {
        preparePrerequisite(entry);
        Method method = findMethod(entry);
        Object[] args = argsFor(entry, method.getParameterTypes());
        Object value = method.invoke(entry.target, args);
        interfaces.recordSuccess(entry.title(), entry.source, value);
        return value;
    }

    private void preparePrerequisite(InterfaceEntry entry) {
        String id = entry.manager + "." + entry.method;
        if (id.equals("SecondaryDisplayManager.setBotImage")
                || id.equals("SecondaryDisplayManager.showImage")
                || id.equals("SecondaryDisplayManager.showImageList")
                || id.equals("SecondaryDisplayManager.showVideo")) {
            try {
                interfaces.secondaryDisplayManager.powerOn(true);
            } catch (Throwable throwable) {
                publish("Unable to power on secondary display before " + id + ": " + throwable);
            }
        }
    }

    private Method findMethod(InterfaceEntry entry) throws NoSuchMethodException {
        int count = parameterCount(entry.parameters);
        for (Method method : entry.target.getClass().getMethods()) {
            if (method.getName().equals(entry.method) && method.getParameterTypes().length == count) {
                return method;
            }
        }
        for (Method method : entry.target.getClass().getMethods()) {
            if (method.getName().equals(entry.method)) {
                return method;
            }
        }
        throw new NoSuchMethodException(entry.title());
    }

    private Object[] argsFor(InterfaceEntry entry, Class<?>[] types) {
        String[] descriptors = parameterDescriptors(entry.parameters);
        Map<String, String> samples = sampleInputs(entry);
        Object[] values = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            String name = i < descriptors.length ? parameterName(descriptors[i]) : "arg" + i;
            values[i] = valueFor(types[i], name, samples);
        }
        return values;
    }

    private Object valueFor(Class<?> type, String name, Map<String, String> samples) {
        String raw = samples.get(name);
        if (type == boolean.class) return parseBoolean(raw);
        if (type == int.class) return intValueFor(name, raw);
        if (type == long.class) return parseLong(raw, 10000L);
        if (type == String.class) return raw == null ? "" : raw;
        if (type == Date.class) return parseDate(raw, "toTimestamp".equals(name) ? "2025-01-31" : "2025-01-01");
        if (isCallbackType(type)) return callbackProxy(type);
        if (type == ApnConfig.class) return new ApnConfig(
                parseInt(samples.get("id"), 123),
                samples.get("name"),
                samples.get("value"),
                nullValue(samples.get("type"))
        );
        if (type == RebootSchedule.class) {
            RebootSchedule schedule = new RebootSchedule();
            schedule.hour = parseInt(samples.get("hour"), 1);
            schedule.minute = parseInt(samples.get("minute"), 15);
            schedule.days = new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY};
            return schedule;
        }
        if (type.isEnum()) return enumValue(type, raw);
        if (type == Typeface.class) return Typeface.NORMAL;
        if (type == PrintText.class) return new PrintText("Capitec test line", PrintAlign.LEFT, 14, false);
        if (type == PrintBarcode.class) return new PrintBarcode("1234567890", 384, 100, PrintAlign.CENTER);
        if (type == PrintQrCode.class) return new PrintQrCode("Capitec", 160, 160, PrintAlign.CENTER);
        if (type == PrintImage.class) return new PrintImage(null, 40, 40, PrintAlign.CENTER);
        if (type.isArray()) return arrayValue(type.getComponentType(), name, samples);
        return null;
    }

    private boolean isCallbackType(Class<?> type) {
        return type.isInterface() && "Callback".equals(type.getSimpleName());
    }

    private Object callbackProxy(Class<?> type) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if ("onResult".equals(method.getName()) && args != null && args.length == 1) {
                publish("Callback: " + ValueFormatter.format(args[0]));
            }
            return null;
        });
    }

    private Object arrayValue(Class<?> component, String name, Map<String, String> samples) {
        if (component == String.class) {
            String raw = samples.get(name);
            String[] values = raw == null || raw.length() == 0
                    ? new String[]{"android.permission.READ_PHONE_STATE"}
                    : raw.split("\\|");
            Object array = Array.newInstance(component, values.length);
            for (int i = 0; i < values.length; i++) Array.set(array, i, values[i]);
            return array;
        }
        if (component == PrintColumn.class) {
            return new PrintColumn[]{
                    new PrintColumn("Column 1", PrintAlign.LEFT, 14, false, 33),
                    new PrintColumn("Column 2", PrintAlign.CENTER, 14, false, 33),
                    new PrintColumn("Column 3", PrintAlign.RIGHT, 14, false, 34)
            };
        }
        return Array.newInstance(component, 0);
    }

    private Map<String, String> sampleInputs(InterfaceEntry entry) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String descriptor : parameterDescriptors(entry.parameters)) {
            String trimmed = descriptor.trim();
            String name = parameterName(trimmed);
            values.put(name, sampleValue(entry, name, trimmed));
        }
        if (entry.parameters.contains("ApnConfig")) {
            values.put("id", "123");
            values.put("name", "MyAPN");
            values.put("value", "internet.provider.com");
            values.put("type", "null");
        }
        if (entry.parameters.contains("RebootSchedule")) {
            values.put("hour", "1");
            values.put("minute", "15");
            values.put("days", "MONDAY|WEDNESDAY");
        }
        if (entry.parameters.contains("PrintBarcode")) {
            values.remove("barcode");
            values.put("code", "1234567890");
            values.put("width", "384");
            values.put("height", "100");
            values.put("align", "CENTER");
        }
        return values;
    }

    private String sampleValue(InterfaceEntry entry, String name, String descriptor) {
        String id = entry.manager + "." + entry.method;
        if (descriptor.startsWith("boolean ")) return booleanSampleValue(id, name);
        if ("packageName".equals(name)) return getPackageName();
        if ("key".equals(name)) return "ro.build.version.sdk";
        if ("ssid".equals(name)) return stringExtra("wifiSsid", "MyWiFi");
        if ("password".equals(name)) return stringExtra("wifiPassword", "password123");
        if ("timeZone".equals(name)) return java.util.TimeZone.getDefault().getID();
        if ("time".equals(name)) return String.valueOf(System.currentTimeMillis());
        if ("seconds".equals(name)) return id.endsWith("setLockTimeout") ? "60" : "120";
        if ("percentage".equals(name)) return "80";
        if ("delayMs".equals(name)) return "3000";
        if ("simSlot".equals(name)) return activeSimSlot();
        if ("subId".equals(name)) return activeDataSubscriptionId();
        if ("apnId".equals(name)) return "123";
        if ("fromTimestamp".equals(name)) return "2025-01-01";
        if ("toTimestamp".equals(name)) return "2025-01-31";
        if ("interval".equals(name)) return "10000";
        if ("filePath".equals(name)) return "/data/Syslog/logcat/SystemLog_20260106030053.log";
        if ("title".equals(name)) return "Capitec Test";
        if ("message".equals(name)) return "Test notification";
        if ("text".equals(name)) return "Starting...";
        if ("path".equals(name) || "apkPath".equals(name) || name.endsWith("Path")) return samplePath(id);
        if ("format".equals(name)) return "HOUR_24";
        if ("tile".equals(name)) return "AIRPLANE";
        if ("labelType".equals(name)) return "BOOT";
        if ("security".equals(name)) return "WPA2_PSK";
        if ("setting".equals(name)) return "NETWORK_PROVIDED";
        if ("priority".equals(name)) return "PRIORITY_HIGH_ACCURACY";
        if ("cameraType".equals(name)) return "REAR";
        if ("theme".equals(name) && descriptor.startsWith("Theme ")) return "LIGHT";
        if ("theme".equals(name)) return "NORMAL";
        if ("permissions".equals(name)) return "android.permission.READ_PHONE_STATE";
        if ("lpaString".equals(name)) return "LPA:1$telkomsel-idn.rsp.instant-connectivity.com$QZU3T-PB9HF-FPVGK-K2H72";
        if ("verificationCode".equals(name)) return "123456";
        if (descriptor.startsWith("int ") || descriptor.startsWith("long ")) return "0";
        return "";
    }

    private String stringExtra(String name, String fallback) {
        String value = getIntent().getStringExtra(name);
        return value == null || value.length() == 0 ? fallback : value;
    }

    private String booleanSampleValue(String id, String name) {
        if (id.equals("AirplaneManager.toggleAirplaneMode")) return "false";
        if (id.equals("FlashlightManager.toggleFlashlight")) return "false";
        if (id.equals("SoftwareManager.silenceNotifications")) return "false";
        if (id.equals("SoftwareManager.enableService")) return "true";
        if (id.equals("PrimaryDisplayManager.powerOn") || id.equals("SecondaryDisplayManager.powerOn")) return "true";
        if (id.startsWith("SystemManager.show")) return "true";
        if (id.startsWith("SystemManager.allow")) return "true";
        if (id.startsWith("SecurityManager.allow")) return "true";
        if (id.startsWith("SettingsManager.allow")) return "true";
        if (id.startsWith("VirtualKeyboardManager.allow")) return "true";
        if (id.equals("SecurityManager.setDeveloperMode")) return "false";
        if (id.equals("BatteryManager.setShowBatteryPercentage")) return "true";
        if (id.equals("SoundManager.setTouchSounds")) return "true";
        if (id.equals("GpsManager.toggleGPS")) return "true";
        if (id.equals("WifiManager.toggleWifi")) return "true";
        if ("visible".equals(name) || "allowed".equals(name) || "allow".equals(name) || "show".equals(name)) return "true";
        if ("enabled".equals(name) || "active".equals(name) || "on".equals(name) || "optimized".equals(name)) return "true";
        return "false";
    }

    private String samplePath(String id) {
        if (id.equals("SecondaryDisplayManager.setBotImage")) {
            return interfaceInputPath("secondary-display/boot_logo.png");
        }
        if (id.equals("SecondaryDisplayManager.showVideo")) {
            return interfaceInputPath("secondary-display/xgplayer_demo_360p_10s.mp4");
        }
        if (id.equals("SecondaryDisplayManager.showImageList")) {
            return interfaceInputPath("secondary-display/pix_step_1.png")
                    + "|"
                    + interfaceInputPath("secondary-display/pix_step_2.png");
        }
        if (id.equals("SecondaryDisplayManager.showImage")) {
            return interfaceInputPath("secondary-display/payment_start.png");
        }
        if (id.endsWith("setBotImage") || id.endsWith("setBootImage")) return interfaceInputPath("boot/boot_image.bmp");
        if (id.endsWith("setBootAnimation")) {
            return stringExtra("bootAnimationPath", interfaceInputPath("boot/bootanimation.zip"));
        }
        if (id.endsWith("setShutdownAnimation")) return "/sdcard/shutdown-animation.zip";
        if (id.endsWith("showVideo")) return "/sdcard/promo.mp4";
        if (id.endsWith("showImage") || id.endsWith("showImageList")) return "/sdcard/welcome.png";
        if (id.endsWith("setFont")) return interfaceInputPath("printer/demo_font.ttf");
        if (id.endsWith("installApp")) return "/sdcard/app.apk";
        if (id.endsWith("updateFirmware")) return "/sdcard/firmware.img";
        return "/sdcard/test.apk";
    }

    private String activeDataSubscriptionId() {
        try {
            int subId = android.telephony.SubscriptionManager.getDefaultDataSubscriptionId();
            return String.valueOf(subId);
        } catch (Throwable ignored) {
            return "-1";
        }
    }

    private String activeSimSlot() {
        try {
            int subId = android.telephony.SubscriptionManager.getDefaultDataSubscriptionId();
            android.telephony.SubscriptionManager manager = (android.telephony.SubscriptionManager) getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
            android.telephony.SubscriptionInfo info = manager == null ? null : manager.getActiveSubscriptionInfo(subId);
            return String.valueOf(info == null ? -1 : info.getSimSlotIndex());
        } catch (Throwable ignored) {
            return "-1";
        }
    }

    private String interfaceInputPath(String relativePath) {
        boolean sharedBootAnimation = "boot/bootanimation.zip".equals(relativePath);
        File root = sharedBootAnimation
                ? new File("/xapex/custom/tmp")
                : new File(getExternalFilesDir(null) == null ? getFilesDir() : getExternalFilesDir(null), "interface-inputs");
        File target = sharedBootAnimation
                ? new File(root, "boot-animation.zip")
                : new File(root, relativePath);
        if (!sharedBootAnimation && target.exists() && target.length() > 0) {
            return target.getAbsolutePath();
        }
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        String assetPath = "capitec-interface-inputs/" + relativePath;
        try (java.io.InputStream input = getAssets().open(assetPath);
             java.io.FileOutputStream output = new java.io.FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            output.flush();
            if (sharedBootAnimation) {
                target.setReadable(true, false);
                target.setWritable(true, false);
            }
            return target.getAbsolutePath();
        } catch (Exception exception) {
            publish("Unable to prepare interface input asset " + assetPath + ": " + exception);
            return target.getAbsolutePath();
        }
    }

    private String sampleInputText(InterfaceEntry entry) {
        Map<String, String> values = sampleInputs(entry);
        return values.isEmpty() ? "None" : values.toString();
    }

    private String sampleInputSource(InterfaceEntry entry) {
        return entry.parameters == null || entry.parameters.trim().length() == 0
                ? "Document usage example has no parameters"
                : "Generated from document parameter names and visible usage examples";
    }

    private String methodId(InterfaceEntry entry) {
        return entry.manager + "." + entry.method;
    }

    private String skipReason(InterfaceEntry entry) {
        String id = entry.manager + "." + entry.method;
        if (id.equals("BootManager.restart")
                || id.equals("BootManager.shutdown")
                || id.equals("BootManager.factoryReset")) return "Destructive power/reset action.";
        if (id.startsWith("BootManager.set")) {
            if (getIntent().getBooleanExtra("allowBootAssetMutation", false)) {
                return "";
            }
            return "Persistent boot asset mutation.";
        }
        if (id.startsWith("SoftwareManager.install")
                || id.startsWith("SoftwareManager.uninstall")
                || id.startsWith("SoftwareManager.kill")
                || id.startsWith("SoftwareManager.restart")
                || id.startsWith("SoftwareManager.start")
                || id.startsWith("SoftwareManager.update")) return "App or firmware lifecycle mutation.";
        if (id.startsWith("SimManager.add")
                || id.startsWith("SimManager.delete")
                || id.startsWith("SimManager.select")
                || id.startsWith("SimManager.reset")
                || id.startsWith("SimManager.activate")
                || id.startsWith("SimManager.download")) return "SIM/APN/eSIM mutation.";
        if (id.startsWith("WifiManager.add")
                || id.startsWith("WifiManager.connect")
                || id.startsWith("WifiManager.disconnect")
                || id.startsWith("WifiManager.forget")) {
            if (getIntent().getBooleanExtra("allowWifiMutation", false)
                    && (!id.startsWith("WifiManager.forget") || getIntent().getBooleanExtra("allowWifiForget", false))) {
                return "";
            }
            return "Wi-Fi profile mutation requires known local network parameters.";
        }
        if (id.startsWith("SystemManager.setLauncher")
                || id.startsWith("SystemManager.add")
                || id.startsWith("SystemManager.remove")) return "Launcher/desktop mutation can disrupt the test environment.";
        if (id.equals("SecurityManager.setDeveloperMode")) return "Developer-mode mutation can disconnect ADB and block host-side evidence collection.";
        if (id.startsWith("SecurityManager.grant")) return "Runtime permission mutation requires a separate before/after permission matrix.";
        if (id.startsWith("SettingsManager.setSettingsPasscode")) return "Settings passcode mutation requires an operator-provided passcode policy.";
        if (id.equals("PrintManager.setFont")) {
            return "Printer font change requires a real font file and persistent printer configuration.";
        }
        return "";
    }

    private boolean isDevicePrerequisiteFailure(InterfaceEntry entry, Throwable cause) {
        String id = methodId(entry);
        String message = String.valueOf(cause.getMessage());
        return (id.startsWith("VirtualKeyboardManager.")
                && (message.contains("Default input method")
                || message.contains("LatinIME")
                || message.contains("private preferences")
                || message.contains("same-UID")
                || message.contains("permission")))
                || id.startsWith("PrintManager.")
                && (message.contains("Lack of paper")
                || message.contains("NO_PAPER")
                || message.contains("paper")
                || message.contains("width and height"))
                || id.equals("SystemManager.postNotification")
                && (message.contains("POST_NOTIFICATIONS")
                || message.contains("Notifications are disabled"));
    }

    private boolean requiresManualReview(InterfaceEntry entry) {
        String id = methodId(entry);
        return id.startsWith("PrintManager.")
                && !id.equals("PrintManager.hasPrinter");
    }

    private String manualReviewPrompt(InterfaceEntry entry) {
        String id = methodId(entry);
        if (id.equals("PrintManager.addTextLine")) return "Confirm the paper contains text: Capitec test line.";
        if (id.equals("PrintManager.addBarcode")) return "Confirm the paper contains a CODE_128 barcode for 1234567890.";
        if (id.equals("PrintManager.addQrCode")) return "Confirm the paper contains a QR code for Capitec.";
        if (id.equals("PrintManager.addEmptyLine")) return "Confirm the printer fed one blank line.";
        return "Confirm the physical printer output matches the interface call.";
    }

    private String evidenceMode(InterfaceEntry entry) {
        if (skipReason(entry).length() > 0) return "not executed; skip reason is evidence";
        String id = entry.manager + "." + entry.method;
        if (id.contains("Display") || id.contains("Brightness") || id.contains("Notification") || id.contains("Alert")) {
            return "ADB screenshot plus UI result";
        }
        if (id.contains("Wifi") || id.contains("Bluetooth") || id.contains("NFC") || id.contains("GPS") || id.contains("Airplane")) {
            return "ADB dumpsys/settings plus UI result";
        }
        return "UI screenshot plus Android-side independent observation when available";
    }

    private String externalEvidenceHint(InterfaceEntry entry) {
        return "Host runner should capture screencap, uiautomator dump, dumpsys/settings snapshots, and this JSON artifact.";
    }

    private String externalVerificationKey(InterfaceEntry entry) {
        String id = methodId(entry);
        if (id.equals("DeviceInfoManager.getSerialNumber")) return "getprop:ro.serialno";
        if (id.equals("DeviceInfoManager.getModel")) return "getprop:ro.product.name";
        if (id.equals("DeviceInfoManager.getBrand")) return "getprop:ro.product.brand";
        if (id.equals("DeviceInfoManager.getManufacturer")) return "getprop:ro.product.manufacturer";
        if (id.equals("DeviceInfoManager.getAndroidVersion")) return "getprop:ro.build.version.release";
        if (id.equals("DeviceInfoManager.getRomVersion")) return "getprop:ro.product.internal_version";
        if (id.equals("DeviceInfoManager.getFirmwareVersion")) return "getprop:ro.platform.security.version + ro.build.version.sdk + ro.ap.version + ro.vendor.md_apps.load_verno";
        if (id.equals("DeviceInfoManager.getFirmwareId")) return "getprop:ro.product.internal_version";
        if (id.equals("DeviceInfoManager.getSystemProperty")) return "getprop:ro.build.version.sdk";
        if (id.equals("DeviceInfoManager.getMac")) return "xcheng:XcDeviceManager.getDeviceWifiMac";
        if (id.equals("DeviceInfoManager.getIPv4Address")) return "ip:-4 addr show scope global";
        if (id.equals("PrimaryDisplayManager.getResolution")) return "wm:size";
        if (id.equals("PrimaryDisplayManager.getScreenBrightness")) return "settings:system:screen_brightness";
        if (id.equals("PrimaryDisplayManager.getScreenTimeout")) return "settings:system:screen_off_timeout";
        if (id.equals("BluetoothManager.hasBluetooth") || id.equals("BluetoothManager.isBluetoothActive")) return "dumpsys:bluetooth_manager";
        if (id.equals("GpsManager.hasGPS") || id.equals("GpsManager.isGPSActive")) return "cmd:location";
        if (id.equals("NfcManager.hasNFC") || id.equals("NfcManager.isNFCActive")) return "dumpsys:nfc";
        if (id.equals("WifiManager.getWifiNetworks") || id.equals("WifiManager.getWifiDataUsageStats")) return "dumpsys:wifi";
        if (id.equals("TimeManager.getTime")) return "host-adb-date-ms";
        if (id.equals("TimeManager.getTimeZone")) return "getprop:persist.sys.timezone";
        return "";
    }

    private void writeResultFile(JSONArray results, int executed, int passed, int prerequisiteFailed, int unsupported, int failed, int skipped) {
        try {
            JSONObject root = new JSONObject();
            root.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(new Date()));
            root.put("deviceModel", android.os.Build.MODEL);
            root.put("deviceBrand", android.os.Build.BRAND);
            root.put("androidRelease", android.os.Build.VERSION.RELEASE);
            root.put("androidSdk", android.os.Build.VERSION.SDK_INT);
            root.put("declaredEntries", results.length());
            root.put("executed", executed);
            root.put("passed", passed);
            root.put("prerequisiteFailed", prerequisiteFailed);
            root.put("unsupported", unsupported);
            root.put("failed", failed);
            root.put("skipped", skipped);
            root.put("terminalDeviceInfoSnapshot", terminalDeviceInfoSnapshot());
            root.put("results", results);
            File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            File file = new File(dir, "capitec-auto-test-results.json");
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write(root.toString(2));
            }
            publish("DONE\nexecuted=" + executed + " passed=" + passed + " prerequisiteFailed=" + prerequisiteFailed + " unsupported=" + unsupported + " failed=" + failed + " skipped=" + skipped + "\n" + file.getAbsolutePath());
        } catch (Exception exception) {
            publish("Failed to write result file: " + exception);
        }
    }

    private JSONObject terminalDeviceInfoSnapshot() {
        JSONObject snapshot = new JSONObject();
        try {
            com.kozen.terminalmanager.deviceinfo.IDeviceInfoManager manager =
                    TerminalManager.INSTANCE.getDeviceInfoManager();
            snapshot.put("getSdkServiceVersion", manager.getSdkServiceVersion());
            snapshot.put("getSerialNo", manager.getSerialNo());
            snapshot.put("getVendorName", manager.getVendorName());
            snapshot.put("getDeviceModel", manager.getDeviceModel());
            snapshot.put("getOsVersion", manager.getOsVersion());
            snapshot.put("getKernelVersion", manager.getKernelVersion());
            snapshot.put("getMcuVersion", manager.getMcuVersion());
            snapshot.put("getHardwareVersion", manager.getHardwareVersion());
            snapshot.put("getEmvKernelVersion", manager.getEmvKernelVersion());
            snapshot.put("getTUSN", manager.getTUSN());
            snapshot.put("getCSN", manager.getCSN());
            snapshot.put("getImei", ValueFormatter.format(manager.getImei()));
            snapshot.put("getImsi", ValueFormatter.format(manager.getImsi()));
        } catch (Throwable throwable) {
            try {
                snapshot.put("errorType", throwable.getClass().getSimpleName());
                snapshot.put("errorMessage", String.valueOf(throwable.getMessage()));
            } catch (Exception ignored) {
            }
        }
        return snapshot;
    }

    private void sleepQuietly(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private int parameterCount(String parameters) {
        return parameters == null || parameters.trim().length() == 0 ? 0 : parameters.split(",").length;
    }

    private String[] parameterDescriptors(String parameters) {
        return parameters == null || parameters.trim().length() == 0 ? new String[0] : parameters.split(",");
    }

    private String parameterName(String descriptor) {
        String[] pieces = descriptor.trim().split("\\s+");
        return pieces.length == 0 ? "" : pieces[pieces.length - 1].replace("[]", "");
    }

    private boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
    }

    private int intValueFor(String name, String value) {
        if ("format".equals(name)) {
            return timeFormatValue(value);
        }
        return parseInt(value, 0);
    }

    private int timeFormatValue(String value) {
        if (value == null || value.trim().length() == 0) {
            return TimeFormat.HOUR_24;
        }
        String normalized = value.trim();
        if ("LOCALE_DEFAULT".equals(normalized)) return TimeFormat.LOCALE_DEFAULT;
        if ("HOUR_12".equals(normalized)) return TimeFormat.HOUR_12;
        if ("HOUR_24".equals(normalized)) return TimeFormat.HOUR_24;
        return parseInt(normalized, TimeFormat.HOUR_24);
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value == null ? "" : value.trim()); } catch (Exception ignored) { return fallback; }
    }

    private long parseLong(String value, long fallback) {
        try { return Long.parseLong(value == null ? "" : value.trim()); } catch (Exception ignored) { return fallback; }
    }

    private Date parseDate(String value, String fallback) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value == null || value.length() == 0 ? fallback : value);
        } catch (Exception ignored) {
            return new Date();
        }
    }

    private String nullValue(String value) {
        return value == null || "null".equalsIgnoreCase(value) ? null : value;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object enumValue(Class<?> type, String raw) {
        Object[] values = type.getEnumConstants();
        if (values == null || values.length == 0) return null;
        if (raw != null) {
            for (Object value : values) {
                if (((Enum) value).name().equalsIgnoreCase(raw)) return value;
            }
        }
        return values[0];
    }

    private void publish(String text) {
        runOnUiThread(() -> output.setText(output.getText() + "\n" + text));
    }
}
