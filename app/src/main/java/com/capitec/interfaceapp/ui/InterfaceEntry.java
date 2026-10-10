package com.capitec.interfaceapp.ui;

public final class InterfaceEntry {
    public final String manager;
    public final String method;
    public final String returnType;
    public final String parameters;
    public final Object target;
    public int index;
    public String source;

    public InterfaceEntry(String manager, String method, String returnType, String parameters, Object target) {
        this.manager = manager;
        this.method = method;
        this.returnType = returnType;
        this.parameters = parameters == null ? "" : parameters;
        this.target = target;
        this.index = 0;
        this.source = "";
    }

    public String title() { return manager + "." + method; }
    public String indexedTitle() { return index > 0 ? "#" + index + " " + title() : title(); }
    public String subtitle() {
        String input = parameters.length() == 0 ? "Input: None" : "Input: " + parameters;
        String sourceText = source == null || source.length() == 0 ? "" : "  |  Source: " + source;
        return returnType + "  |  " + input + sourceText;
    }
    public String translatedName() {
        String description = InterfaceDescriptions.descriptionFor(manager, method);
        return description.length() == 0 ? translateManager(manager) + " - " + translateMethod(method) : description;
    }

    private static String translateManager(String manager) {
        if ("AirplaneManager".equals(manager)) return "飞行模式";
        if ("BatteryManager".equals(manager)) return "电池";
        if ("BluetoothManager".equals(manager)) return "蓝牙";
        if ("BootManager".equals(manager)) return "启动/重启";
        if ("DeviceInfoManager".equals(manager)) return "设备信息";
        if ("FlashlightManager".equals(manager)) return "手电筒";
        if ("GpsManager".equals(manager)) return "GPS";
        if ("LogManager".equals(manager)) return "日志";
        if ("NfcManager".equals(manager)) return "NFC";
        if ("PhysicalKeyboardManager".equals(manager)) return "物理键盘";
        if ("PrimaryDisplayManager".equals(manager)) return "主屏";
        if ("PrintManager".equals(manager)) return "打印";
        if ("ScanManager".equals(manager)) return "扫码";
        if ("SecondaryDisplayManager".equals(manager)) return "副屏";
        if ("SecurityManager".equals(manager)) return "安全策略";
        if ("SettingsManager".equals(manager)) return "系统设置";
        if ("SimManager".equals(manager)) return "SIM/蜂窝";
        if ("SoftwareManager".equals(manager)) return "软件/应用";
        if ("SoundManager".equals(manager)) return "声音";
        if ("SystemManager".equals(manager)) return "系统UI";
        if ("TimeManager".equals(manager)) return "时间";
        if ("VirtualKeyboardManager".equals(manager)) return "虚拟键盘";
        if ("WifiManager".equals(manager)) return "Wi-Fi";
        return manager;
    }

    private static String translateMethod(String method) {
        if (method.startsWith("is")) return "查询是否" + splitCamel(method.substring(2));
        if (method.startsWith("has")) return "查询是否具备" + splitCamel(method.substring(3));
        if (method.startsWith("get")) return "获取" + splitCamel(method.substring(3));
        if (method.startsWith("set")) return "设置" + splitCamel(method.substring(3));
        if (method.startsWith("allow")) return "允许/禁止" + splitCamel(method.substring(5));
        if (method.startsWith("toggle")) return "开关" + splitCamel(method.substring(6));
        if (method.startsWith("enable")) return "启用/禁用" + splitCamel(method.substring(6));
        if (method.startsWith("show")) return "显示/隐藏" + splitCamel(method.substring(4));
        if (method.startsWith("add")) return "添加" + splitCamel(method.substring(3));
        if (method.startsWith("remove")) return "移除" + splitCamel(method.substring(6));
        if (method.startsWith("start")) return "启动" + splitCamel(method.substring(5));
        if (method.startsWith("stop")) return "停止" + splitCamel(method.substring(4));
        if (method.startsWith("restart")) return "重启" + splitCamel(method.substring(7));
        if (method.startsWith("cancel")) return "取消" + splitCamel(method.substring(6));
        if (method.startsWith("delete")) return "删除" + splitCamel(method.substring(6));
        if (method.startsWith("select")) return "选择" + splitCamel(method.substring(6));
        if (method.startsWith("reset")) return "重置" + splitCamel(method.substring(5));
        if (method.startsWith("downloadAndInstall")) return "下载并安装" + splitCamel(method.substring(18));
        if ("shutdown".equals(method)) return "关机";
        if ("factoryReset".equals(method)) return "恢复出厂设置";
        if ("installApp".equals(method)) return "安装应用";
        if ("uninstallApp".equals(method)) return "卸载应用";
        if ("killApp".equals(method)) return "结束应用";
        if ("connectWifi".equals(method)) return "连接 Wi-Fi";
        if ("disconnectWifi".equals(method)) return "断开 Wi-Fi";
        if ("forgetWifi".equals(method)) return "忘记 Wi-Fi";
        return splitCamel(method);
    }

    private static String splitCamel(String value) {
        if (value == null || value.length() == 0) return "";
        return value.replaceAll("([a-z])([A-Z])", "$1 $2");
    }
}
