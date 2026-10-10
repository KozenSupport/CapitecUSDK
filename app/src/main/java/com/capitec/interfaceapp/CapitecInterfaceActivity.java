package com.capitec.interfaceapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import com.capitec.interfaceapp.runtime.InterfaceLogcatLogger;
import com.capitec.interfaceapp.runtime.InterfaceResult;
import com.capitec.interfaceapp.runtime.UnsupportedInterfaceException;
import com.capitec.interfaceapp.runtime.ValueFormatter;
import com.capitec.interfaceapp.ui.InterfaceEntry;
import com.capitec.interfaceapp.ui.InterfaceRegistry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CapitecInterfaceActivity extends Activity {
    private static final int REQUEST_POST_NOTIFICATIONS = 1001;
    private enum Page { MANAGERS, FUNCTIONS, DETAIL }
    private static final int COLOR_PASS_BG = Color.rgb(230, 244, 234);
    private static final int COLOR_PASS_TEXT = Color.rgb(30, 122, 68);
    private static final int COLOR_FAIL_BG = Color.rgb(252, 232, 230);
    private static final int COLOR_FAIL_TEXT = Color.rgb(163, 58, 50);
    private static final int COLOR_WARN_BG = Color.rgb(254, 247, 224);
    private static final int COLOR_WARN_TEXT = Color.rgb(176, 96, 0);
    private static final int COLOR_UNSET_BG = Color.rgb(242, 244, 247);
    private static final int COLOR_UNSET_TEXT = Color.rgb(88, 96, 105);
    private static final int COLOR_NORMAL_BG = Color.WHITE;
    private static final int COLOR_TITLE = Color.rgb(16, 35, 31);
    private static final String PREF_PAGE = "last_page";
    private static final String PREF_MANAGER = "last_manager";
    private static final String PREF_ENTRY = "last_entry";
    private static final String PREF_MANAGER_POSITION = "last_manager_position";
    private static final String PREF_FUNCTION_POSITION_PREFIX = "last_function_position#";
    private static final String EXPOSURE_DECISIONS_FILE = "interface-exposure-decisions.json";

    private CapitecInterfaces interfaces;
    private List<InterfaceEntry> allEntries;
    private final List<String> managerNames = new ArrayList<>();
    private final Map<String, List<InterfaceEntry>> byManager = new LinkedHashMap<>();
    private final List<Object> visibleRows = new ArrayList<>();
    private final Map<String, Spinner> enumInputs = new LinkedHashMap<>();
    private final Map<String, AutoResult> autoResults = new HashMap<>();

    private Page page = Page.MANAGERS;
    private String selectedManager;
    private InterfaceEntry selectedEntry;

    private TextView titleText;
    private TextView statusText;
    private TextView detailText;
    private TextView resultText;
    private volatile String latestCallbackResult;
    private EditText inputText;
    private LinearLayout enumInputContainer;
    private EditText failureReasonText;
    private CheckBox confirmCheck;
    private Button backButton;
    private Button pinAdapterTestButton;
    private Button runButton;
    private Button passButton;
    private Button prerequisiteButton;
    private Button failButton;
    private Button languageButton;
    private Button exposurePendingButton;
    private Button exposureAllowButton;
    private Button exposureDenyButton;
    private LinearLayout manualResultRow;
    private LinearLayout exposureDecisionRow;
    private ListView listView;
    private RowAdapter adapter;
    private SharedPreferences manualStatus;
    private SharedPreferences uiPreferences;
    private String autoSummaryText = "Auto: no result file";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(applySavedLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capitec_interface);
        requestNotificationPermissionIfNeeded();

        findViewById(R.id.strategyText).setVisibility(View.GONE);
        titleText = findViewById(R.id.titleText);
        statusText = findViewById(R.id.statusText);
        detailText = findViewById(R.id.detailText);
        resultText = findViewById(R.id.resultText);
        inputText = findViewById(R.id.inputText);
        enumInputContainer = findViewById(R.id.enumInputContainer);
        failureReasonText = findViewById(R.id.failureReasonText);
        confirmCheck = findViewById(R.id.confirmCheck);
        backButton = findViewById(R.id.backButton);
        pinAdapterTestButton = findViewById(R.id.pinAdapterTestButton);
        runButton = findViewById(R.id.runButton);
        passButton = findViewById(R.id.passButton);
        prerequisiteButton = findViewById(R.id.prerequisiteButton);
        failButton = findViewById(R.id.failButton);
        languageButton = findViewById(R.id.languageButton);
        exposurePendingButton = findViewById(R.id.exposurePendingButton);
        exposureAllowButton = findViewById(R.id.exposureAllowButton);
        exposureDenyButton = findViewById(R.id.exposureDenyButton);
        manualResultRow = findViewById(R.id.manualResultRow);
        exposureDecisionRow = findViewById(R.id.exposureDecisionRow);
        listView = findViewById(R.id.capabilityList);
        resultText.setMovementMethod(new ScrollingMovementMethod());

        interfaces = new CapitecInterfaces(this);
        manualStatus = getSharedPreferences("manual_function_status", MODE_PRIVATE);
        uiPreferences = getSharedPreferences("ui_preferences", MODE_PRIVATE);
        allEntries = InterfaceRegistry.all(interfaces);
        loadAutoResults();
        loadExposureDecisions();
        saveExposureDecisions();
        groupManagers();

        adapter = new RowAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> handleRowClick(visibleRows.get(position)));
        backButton.setOnClickListener(v -> navigateBack());
        pinAdapterTestButton.setOnClickListener(v ->
                startActivity(new Intent(this, PinAdapterTestActivity.class)));
        runButton.setOnClickListener(v -> runSelected());
        passButton.setOnClickListener(v -> markSelected("PASS"));
        prerequisiteButton.setOnClickListener(v -> markSelected("DEVICE_PREREQUISITE_FAILED"));
        failButton.setOnClickListener(v -> markSelected("FAIL"));
        exposurePendingButton.setOnClickListener(v -> markExposure("PENDING"));
        exposureAllowButton.setOnClickListener(v -> markExposure("ALLOW"));
        exposureDenyButton.setOnClickListener(v -> markExposure("DENY"));
        languageButton.setOnClickListener(v -> toggleLanguage());

        restoreLastUiState();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_POST_NOTIFICATIONS
            );
        }
    }

    @Override
    public void onBackPressed() {
        if (page == Page.MANAGERS) {
            super.onBackPressed();
            return;
        }
        navigateBack();
    }

    @Override
    protected void onPause() {
        saveEnumSelections();
        super.onPause();
    }

    private void groupManagers() {
        managerNames.clear();
        byManager.clear();
        for (InterfaceEntry entry : allEntries) {
            List<InterfaceEntry> entries = byManager.get(entry.manager);
            if (entries == null) {
                entries = new ArrayList<>();
                byManager.put(entry.manager, entries);
                managerNames.add(entry.manager);
            }
            entries.add(entry);
        }
        Collections.sort(managerNames, String.CASE_INSENSITIVE_ORDER);
        for (List<InterfaceEntry> entries : byManager.values()) {
            Collections.sort(entries, new Comparator<InterfaceEntry>() {
                @Override
                public int compare(InterfaceEntry left, InterfaceEntry right) {
                    int method = left.method.compareToIgnoreCase(right.method);
                    if (method != 0) return method;
                    return left.parameters.compareToIgnoreCase(right.parameters);
                }
            });
        }
    }

    private void handleRowClick(Object row) {
        if (row instanceof String) {
            int position = visibleRows.indexOf(row);
            saveManagerSelection((String) row, position);
            showFunctions((String) row);
            return;
        }
        if (row instanceof InterfaceEntry) {
            int position = visibleRows.indexOf(row);
            saveFunctionSelection((InterfaceEntry) row, position);
            showDetail((InterfaceEntry) row);
        }
    }

    private void showManagers() {
        page = Page.MANAGERS;
        selectedManager = null;
        selectedEntry = null;
        visibleRows.clear();
        visibleRows.addAll(managerNames);
        titleText.setText(R.string.screen_title);
        pinAdapterTestButton.setVisibility(View.VISIBLE);
        detailText.setVisibility(View.GONE);
        inputText.setVisibility(View.GONE);
        enumInputContainer.setVisibility(View.GONE);
        enumInputContainer.removeAllViews();
        enumInputs.clear();
        failureReasonText.setVisibility(View.GONE);
        confirmCheck.setVisibility(View.GONE);
        runButton.setVisibility(View.GONE);
        exposureDecisionRow.setVisibility(View.GONE);
        manualResultRow.setVisibility(View.GONE);
        resultText.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.GONE);
        statusText.setText(getString(
                R.string.catalog_status_template,
                allEntries.size(),
                managerNames.size()
        ));
        adapter.notifyDataSetChanged();
        listView.setSelection(uiPreferences.getInt(PREF_MANAGER_POSITION, 0));
    }

    private void showFunctions(String manager) {
        page = Page.FUNCTIONS;
        selectedManager = manager;
        selectedEntry = null;
        visibleRows.clear();
        visibleRows.addAll(byManager.get(manager));
        titleText.setText(manager);
        pinAdapterTestButton.setVisibility(View.GONE);
        statusText.setText(getString(R.string.manager_status_template, visibleRows.size()));
        detailText.setVisibility(View.GONE);
        inputText.setVisibility(View.GONE);
        enumInputContainer.setVisibility(View.GONE);
        enumInputContainer.removeAllViews();
        enumInputs.clear();
        failureReasonText.setVisibility(View.GONE);
        confirmCheck.setVisibility(View.GONE);
        runButton.setVisibility(View.GONE);
        exposureDecisionRow.setVisibility(View.GONE);
        manualResultRow.setVisibility(View.GONE);
        resultText.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        listView.setSelection(uiPreferences.getInt(PREF_FUNCTION_POSITION_PREFIX + manager, 0));
    }

    private void showDetail(InterfaceEntry entry) {
        page = Page.DETAIL;
        selectedManager = entry.manager;
        selectedEntry = entry;
        visibleRows.clear();
        titleText.setText("#" + entry.index + " " + entry.method);
        pinAdapterTestButton.setVisibility(View.GONE);
        statusText.setText(supportStatusText(entry));
        detailText.setText(getString(
                R.string.function_detail_template,
                entry.title(),
                entry.translatedName(),
                parameterDescription(entry),
                returnDescription(entry),
                expectedResult(entry),
                implementationMethod(entry),
                supportStatusText(entry)
        ));
        applyStatusColor(detailText, statusFor(entry));
        detailText.setVisibility(View.VISIBLE);
        inputText.setText(defaultInputFor(entry));
        configureEnumInputs(entry);
        inputText.setVisibility(hasTextInputParameter(entry) ? View.VISIBLE : View.GONE);
        failureReasonText.setText(failureReasonFor(entry));
        failureReasonText.setVisibility(View.VISIBLE);
        confirmCheck.setChecked(false);
        confirmCheck.setText(booleanControlLabel(entry, false));
        confirmCheck.setOnCheckedChangeListener((buttonView, isChecked) ->
                confirmCheck.setText(booleanControlLabel(entry, isChecked)));
        confirmCheck.setVisibility(entry.parameters.contains("boolean") ? View.VISIBLE : View.GONE);
        runButton.setVisibility(View.VISIBLE);
        exposureDecisionRow.setVisibility(View.VISIBLE);
        manualResultRow.setVisibility(View.VISIBLE);
        resultText.setText(readyToRunText(entry));
        applyStatusColor(resultText, statusFor(entry));
        resultText.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        backButton.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void navigateBack() {
        saveEnumSelections();
        if (page == Page.DETAIL) {
            uiPreferences.edit()
                    .putString(PREF_PAGE, "FUNCTIONS")
                    .putString(PREF_MANAGER, selectedManager == null ? "" : selectedManager)
                    .apply();
            showFunctions(selectedManager);
            return;
        }
        uiPreferences.edit().putString(PREF_PAGE, "MANAGERS").apply();
        showManagers();
    }

    private void runSelected() {
        if (selectedEntry == null) {
            resultText.setText(R.string.select_interface);
            return;
        }
        try {
            latestCallbackResult = null;
            Method method = findMethod(selectedEntry);
            saveEnumSelections();
            Object[] args = argsFor(selectedEntry, method.getParameterTypes());
            Object value = method.invoke(selectedEntry.target, args);
            interfaces.recordSuccess(selectedEntry.title(), selectedEntry.source, value);
            InterfaceResult result = interfaces.lastResult();
            String rendered = result == null
                    ? selectedEntry.title() + "\nReturn: " + ValueFormatter.format(value)
                    : result.render();
            if ("BootManager".equals(selectedEntry.manager)
                    && "setBootAnimation".equals(selectedEntry.method)
                    && args.length > 0) {
                rendered += "\n" + getString(R.string.boot_animation_execution_result)
                        + "\n" + getString(R.string.boot_animation_vendor_return)
                        + "\n" + getString(R.string.boot_animation_interface_return)
                        + "\nPrepared source: " + args[0]
                        + "\nInstalled target: /xapex/custom/bootanimation.zip"
                        + "\n" + getString(R.string.boot_animation_reboot_check);
            }
            String callbackResult = latestCallbackResult;
            resultText.setText(callbackResult == null ? rendered : rendered + "\n" + callbackResult);
        } catch (Exception exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            InterfaceLogcatLogger.failure(this, selectedEntry.title(), cause);
            if (cause instanceof UnsupportedInterfaceException) {
                InterfaceResult result = interfaces.recordUnsupported(selectedEntry.title(), (UnsupportedInterfaceException) cause);
                resultText.setText(result.render());
            } else {
                resultText.setText(getString(R.string.result_failed_template, cause.getClass().getSimpleName(), cause.getMessage()));
            }
        }
    }

    private void markSelected(String status) {
        if (selectedEntry == null) {
            resultText.setText(R.string.select_interface);
            return;
        }
        String previousResult = resultText.getText() == null ? "" : resultText.getText().toString();
        saveEnumSelections();
        SharedPreferences.Editor editor = manualStatus.edit().putString(statusKey(selectedEntry), status);
        if ("FAIL".equals(status) || "DEVICE_PREREQUISITE_FAILED".equals(status)) {
            editor.putString(reasonKey(selectedEntry), failureReasonText.getText().toString().trim());
        } else {
            editor.remove(reasonKey(selectedEntry));
        }
        if ("FAIL".equals(status)) {
            editor.putString(exposureKey(selectedEntry), "DENY");
        }
        editor.apply();
        if ("FAIL".equals(status)) {
            saveExposureDecisions();
        }
        showDetail(selectedEntry);
        resultText.setText(manualConfirmationText(selectedEntry, previousResult));
        applyStatusColor(resultText, status);
    }

    private void markExposure(String status) {
        if (selectedEntry == null) {
            resultText.setText(R.string.select_interface);
            return;
        }
        manualStatus.edit().putString(exposureKey(selectedEntry), status).apply();
        saveExposureDecisions();
        showDetail(selectedEntry);
        resultText.setText(getString(R.string.exposure_saved, supportStatusText(selectedEntry)));
    }

    private String manualConfirmationText(InterfaceEntry entry, String previousResult) {
        StringBuilder builder = new StringBuilder();
        builder.append(entry.indexedTitle())
                .append("\n")
                .append(getString(R.string.manual_label))
                .append(": ")
                .append(manualStatusText(entry));
        if (previousResult != null && previousResult.trim().length() > 0) {
            builder.append("\n\nResult:\n").append(previousResult.trim());
            return builder.toString();
        }
        AutoResult autoResult = autoResultFor(entry);
        if (autoResult != null) {
            builder.append("\n\n").append(autoResult.detailText());
        }
        return builder.toString();
    }

    private void toggleLanguage() {
        String current = uiPreferences.getString("language", "en");
        uiPreferences.edit().putString("language", "zh".equals(current) ? "en" : "zh").apply();
        recreate();
    }

    static Context applySavedLocale(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("ui_preferences", MODE_PRIVATE);
        String language = preferences.getString("language", "en");
        Locale locale = "zh".equals(language) ? Locale.SIMPLIFIED_CHINESE : Locale.ENGLISH;
        Locale.setDefault(locale);
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    private Method findMethod(InterfaceEntry entry) throws NoSuchMethodException {
        String[] parameterDescriptors = parameterDescriptors(entry.parameters);
        for (Method method : entry.target.getClass().getMethods()) {
            if (method.getName().equals(entry.method)
                    && method.getParameterTypes().length == parameterDescriptors.length
                    && parameterTypesMatch(method.getParameterTypes(), parameterDescriptors)) {
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

    private boolean parameterTypesMatch(Class<?>[] types, String[] parameterDescriptors) {
        if (types.length != parameterDescriptors.length) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            String expected = parameterTypeName(parameterDescriptors[i]);
            if (expected.length() == 0) {
                continue;
            }
            String actual = types[i].isArray()
                    ? types[i].getComponentType().getSimpleName() + "[]"
                    : types[i].getSimpleName();
            if (!actual.equals(expected)) {
                return false;
            }
        }
        return true;
    }

    private int parameterCount(String parameters) {
        if (parameters == null || parameters.trim().length() == 0) {
            return 0;
        }
        return parameters.split(",").length;
    }

    private String parameterTypeName(String descriptor) {
        if (descriptor == null) {
            return "";
        }
        String trimmed = descriptor.trim();
        int space = trimmed.indexOf(' ');
        return space < 0 ? trimmed : trimmed.substring(0, space).trim();
    }

    private Object[] argsFor(InterfaceEntry entry, Class<?>[] types) {
        String[] parameterDescriptors = parameterDescriptors(entry.parameters);
        Map<String, String> inputValues = parseInputValues(inputText.getText().toString());
        inputValues.putAll(enumInputValues());
        Object[] values = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            String parameterName = i < parameterDescriptors.length ? parameterName(parameterDescriptors[i]) : "";
            values[i] = valueFor(types[i], parameterName, inputValues);
        }
        return values;
    }

    private Map<String, String> enumInputValues() {
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, Spinner> entry : enumInputs.entrySet()) {
            Object selected = entry.getValue().getSelectedItem();
            if (selected != null) {
                values.put(entry.getKey(), String.valueOf(selected));
            }
        }
        return values;
    }

    private Object valueFor(Class<?> type, String parameterName, Map<String, String> inputValues) {
        String rawValue = inputValues.get(parameterName);
        if (type == boolean.class) return parseBooleanInput(rawValue, confirmCheck.isChecked());
        if (type == int.class) return intValueFor(parameterName, rawValue);
        if (type == long.class) return parseLongInput(rawValue, 60000L);
        if (type == String.class) return rawValue == null ? "" : rawValue;
        if (type.isEnum()) return enumValueFor(type, rawValue);
        if (type == Date.class) return dateFromInput(rawValue, parameterName);
        if (isCallbackType(type)) return callbackProxy(type);
        if (type == ApnConfig.class) return apnConfigFrom(inputValues);
        if (type == RebootSchedule.class) return rebootScheduleFrom(inputValues);
        if (type == Typeface.class) return new Typeface(rawValue == null || rawValue.length() == 0 ? "NORMAL" : rawValue);
        if (type == PrintText.class) return new PrintText("Capitec test line", PrintAlign.LEFT, 14, false);
        if (type == PrintBarcode.class) return new PrintBarcode("1234567890", 384, 100, PrintAlign.CENTER);
        if (type == PrintQrCode.class) return new PrintQrCode("Capitec", 160, 160, PrintAlign.CENTER);
        if (type == PrintImage.class) return new PrintImage(null, 40, 40, PrintAlign.CENTER);
        if (type.isArray()) return arrayValueFor(type.getComponentType(), parameterName, inputValues);
        return null;
    }

    private int intValueFor(String parameterName, String rawValue) {
        if ("format".equals(parameterName)) {
            return timeFormatValue(rawValue);
        }
        return parseIntInput(rawValue, 0);
    }

    private int timeFormatValue(String value) {
        if (value == null || value.trim().length() == 0) {
            return TimeFormat.HOUR_24;
        }
        String normalized = value.trim();
        if ("LOCALE_DEFAULT".equals(normalized)) return TimeFormat.LOCALE_DEFAULT;
        if ("HOUR_12".equals(normalized)) return TimeFormat.HOUR_12;
        if ("HOUR_24".equals(normalized)) return TimeFormat.HOUR_24;
        return parseIntInput(normalized, TimeFormat.HOUR_24);
    }

    private Date dateFromInput(String value, String parameterName) {
        String raw = value == null || value.trim().length() == 0
                ? ("toTimestamp".equals(parameterName) ? "2025-01-31" : "2025-01-01")
                : value.trim();
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(raw);
        } catch (Exception ignored) {
            return new Date("toTimestamp".equals(parameterName) ? 1738281600000L : 1735689600000L);
        }
    }

    private RebootSchedule rebootScheduleFrom(Map<String, String> inputValues) {
        RebootSchedule schedule = new RebootSchedule();
        schedule.hour = parseIntInput(inputValues.get("hour"), 1);
        schedule.minute = parseIntInput(inputValues.get("minute"), 15);
        schedule.days = new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY};
        return schedule;
    }

    private Object arrayValueFor(Class<?> componentType, String parameterName, Map<String, String> inputValues) {
        if (componentType == String.class) {
            String rawValue = inputValues.get(parameterName);
            if (rawValue == null || rawValue.trim().length() == 0) {
                rawValue = "permissions".equals(parameterName)
                        ? "android.permission.READ_PHONE_STATE"
                        : "/sdcard/welcome.png";
            }
            return rawValue.split("\\|");
        }
        if (componentType == PrintColumn.class) {
            return new PrintColumn[]{
                    new PrintColumn("Column 1", PrintAlign.LEFT, 14, false, 33),
                    new PrintColumn("Column 2", PrintAlign.CENTER, 14, false, 33),
                    new PrintColumn("Column 3", PrintAlign.RIGHT, 14, false, 34)
            };
        }
        return java.lang.reflect.Array.newInstance(componentType, 0);
    }

    private ApnConfig apnConfigFrom(Map<String, String> inputValues) {
        return new ApnConfig(
                parseIntInput(inputValues.get("id"), 0),
                stringInput(inputValues, "name"),
                firstStringInput(inputValues, "value", "apn"),
                stringInput(inputValues, "type")
        );
    }

    private String stringInput(Map<String, String> inputValues, String key) {
        String value = inputValues.get(key);
        return value == null || "null".equalsIgnoreCase(value.trim()) ? null : value;
    }

    private String firstStringInput(Map<String, String> inputValues, String firstKey, String secondKey) {
        String value = stringInput(inputValues, firstKey);
        return value == null ? stringInput(inputValues, secondKey) : value;
    }

    @SuppressWarnings({"rawtypes"})
    private Object enumValueFor(Class<?> type, String rawValue) {
        Object[] values = type.getEnumConstants();
        if (values == null || values.length == 0) {
            return null;
        }
        if (rawValue != null && rawValue.trim().length() > 0) {
            String normalized = rawValue.trim();
            for (Object value : values) {
                if (((Enum) value).name().equalsIgnoreCase(normalized)) {
                    return value;
                }
            }
        }
        return values[0];
    }

    private boolean isCallbackType(Class<?> type) {
        return type.isInterface() && "Callback".equals(type.getSimpleName());
    }

    private Object callbackProxy(Class<?> type) {
        final String callbackTitle = selectedEntry == null ? "Callback" : selectedEntry.title();
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if ("onResult".equals(method.getName()) && args != null && args.length == 1) {
                String callbackResult = "Callback result: " + ValueFormatter.format(args[0]);
                latestCallbackResult = callbackResult;
                runOnUiThread(() -> {
                    String current = resultText.getText() == null ? "" : resultText.getText().toString();
                    if (!current.contains(callbackResult)) {
                        resultText.setText(callbackTitle + "\n" + callbackResult);
                    }
                });
            }
            return null;
        });
    }

    private boolean parseBooleanInput(String value, boolean fallback) {
        if (value == null || value.trim().length() == 0) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.US);
        if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "on".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized) || "off".equals(normalized)) {
            return false;
        }
        return fallback;
    }

    private int parseIntInput(String value, int fallback) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private long parseLongInput(String value, long fallback) {
        try {
            return Long.parseLong(value == null ? "" : value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void configureEnumInputs(InterfaceEntry entry) {
        enumInputContainer.removeAllViews();
        enumInputs.clear();
        Method method;
        try {
            method = findMethod(entry);
        } catch (NoSuchMethodException ignored) {
            enumInputContainer.setVisibility(View.GONE);
            return;
        }
        Class<?>[] types = method.getParameterTypes();
        String[] descriptors = parameterDescriptors(entry.parameters);
        for (int i = 0; i < types.length; i++) {
            String parameterName = i < descriptors.length ? parameterName(descriptors[i]) : "arg" + i;
            if (isTimeFormatParameter(entry, parameterName)) {
                addNamedEnumInput(parameterName, timeFormatNames());
                continue;
            }
            if (types[i] == Typeface.class) {
                addNamedEnumInput(parameterName, java.util.Arrays.asList("SMALL", "NORMAL", "LARGE"));
                continue;
            }
            if (!types[i].isEnum()) {
                continue;
            }
            addEnumInput(parameterName, types[i]);
        }
        enumInputContainer.setVisibility(enumInputs.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private boolean isTimeFormatParameter(InterfaceEntry entry, String parameterName) {
        return "TimeManager".equals(entry.manager)
                && "setTimeFormat".equals(entry.method)
                && "format".equals(parameterName);
    }

    private List<String> timeFormatNames() {
        List<String> names = new ArrayList<>();
        names.add("LOCALE_DEFAULT");
        names.add("HOUR_12");
        names.add("HOUR_24");
        return names;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addEnumInput(String parameterName, Class<?> enumType) {
        addNamedEnumInput(parameterName, enumNames(enumType));
    }

    private void addNamedEnumInput(String parameterName, List<String> names) {
        TextView label = new TextView(this);
        label.setText(parameterName);
        label.setTextColor(COLOR_TITLE);
        label.setTextSize(13f);
        label.setPadding(0, 4, 0, 2);
        enumInputContainer.addView(label, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        String saved = uiPreferences.getString(enumPreferenceKey(parameterName), "");
        if (saved.length() == 0 && selectedEntry != null) {
            saved = defaultInputValuesFor(selectedEntry).get(parameterName);
        }
        if (saved.length() > 0) {
            int position = adapter.getPosition(saved);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
        enumInputContainer.addView(spinner, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        enumInputs.put(parameterName, spinner);
    }

    @SuppressWarnings("rawtypes")
    private List<String> enumNames(Class<?> enumType) {
        Object[] constants = enumType.getEnumConstants();
        List<String> names = new ArrayList<>();
        if (constants == null) {
            return names;
        }
        for (Object constant : constants) {
            names.add(((Enum) constant).name());
        }
        return names;
    }

    private String defaultInputFor(InterfaceEntry entry) {
        if (entry.parameters == null || entry.parameters.length() == 0) {
            return "";
        }
        Map<String, String> defaults = defaultInputValuesFor(entry);
        StringBuilder builder = new StringBuilder();
        for (String descriptor : parameterDescriptors(entry.parameters)) {
            String trimmed = descriptor.trim();
            if (trimmed.startsWith("boolean ")) {
                continue;
            }
            if (isEnumDescriptor(trimmed) || isCallbackDescriptor(trimmed)) {
                continue;
            }
            if (isAutoSampleObjectDescriptor(trimmed)) {
                continue;
            }
            String name = parameterName(trimmed);
            if (builder.length() > 0) {
                builder.append(", ");
            }
            if ("config".equals(name) && trimmed.startsWith("ApnConfig ")) {
                builder.append(apnConfigInput(defaults));
                continue;
            }
            if (trimmed.startsWith("RebootSchedule ")) {
                builder.append("hour=").append(defaults.get("hour"))
                        .append(", minute=").append(defaults.get("minute"))
                        .append(", days=").append(defaults.get("days"));
                continue;
            }
            String value = defaults.get(name);
            if (value == null) {
                value = defaultValueForParameter(entry, name, trimmed);
            }
            if (value == null) {
                continue;
            }
            builder.append(name).append("=").append(value);
        }
        return builder.toString();
    }

    private Map<String, String> defaultInputValuesFor(InterfaceEntry entry) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String descriptor : parameterDescriptors(entry.parameters)) {
            String trimmed = descriptor.trim();
            String name = parameterName(trimmed);
            if (trimmed.startsWith("RebootSchedule ")) {
                values.put("hour", "1");
                values.put("minute", "15");
                values.put("days", "MONDAY|WEDNESDAY");
                continue;
            }
            if ("config".equals(name) && trimmed.startsWith("ApnConfig ")) {
                values.put("id", "123");
                values.put("name", "MyAPN");
                values.put("value", "internet.provider.com");
                values.put("type", "null");
                continue;
            }
            values.put(name, defaultValueForParameter(entry, name, trimmed));
        }
        return values;
    }

    private String apnConfigInput(Map<String, String> defaults) {
        return "id=" + defaults.get("id")
                + ", name=" + defaults.get("name")
                + ", value=" + defaults.get("value")
                + ", type=" + defaults.get("type");
    }

    private String defaultValueForParameter(InterfaceEntry entry, String name, String descriptor) {
        String id = entry.manager + "." + entry.method;
        if ("packageName".equals(name)) return getPackageName();
        if ("key".equals(name)) return "ro.build.version.sdk";
        if ("passcode".equals(name)) return "123456";
        if ("ssid".equals(name)) return "MyWiFi";
        if ("password".equals(name)) return "password123";
        if ("timeZone".equals(name)) return "GMT+2";
        if ("time".equals(name)) return "1641038400000";
        if ("seconds".equals(name)) return id.endsWith("setLockTimeout") ? "60" : "120";
        if ("percentage".equals(name)) return "80";
        if ("delayMs".equals(name)) return "3000";
        if ("simSlot".equals(name)) return activeSimSlot();
        if ("subId".equals(name)) return activeDataSubscriptionId();
        if ("apnId".equals(name)) return "123";
        if ("fromTimestamp".equals(name)) return "2025-01-01";
        if ("toTimestamp".equals(name)) return "2025-01-31";
        if ("interval".equals(name)) return "10000";
        if ("lpaString".equals(name)) return "LPA:1$telkomsel-idn.rsp.instant-connectivity.com$QZU3T-PB9HF-FPVGK-K2H72";
        if ("verificationCode".equals(name)) return "123456";
        if ("filePath".equals(name)) return "/data/Syslog/logcat/SystemLog_20260106030053.log";
        if ("title".equals(name)) return "Capitec Test";
        if ("message".equals(name)) return "Test notification";
        if ("text".equals(name)) return "Starting...";
        if ("theme".equals(name) && descriptor.startsWith("Theme ")) return "DARK";
        if ("theme".equals(name) && descriptor.startsWith("FontSize ")) return "NORMAL";
        if ("theme".equals(name) && descriptor.startsWith("DisplaySize ")) return "NORMAL";
        if ("path".equals(name) || "apkPath".equals(name) || name.endsWith("Path")) return defaultPathFor(id);
        if ("format".equals(name)) return "HOUR_24";
        if ("tile".equals(name)) return "AIRPLANE";
        if ("labelType".equals(name)) return "BOOT";
        if ("security".equals(name)) return "WPA2_PSK";
        if ("setting".equals(name)) return "NETWORK_PROVIDED";
        if ("priority".equals(name)) return "PRIORITY_HIGH_ACCURACY";
        if ("displaySize".equals(name)) return "NORMAL";
        if ("fontSize".equals(name)) return "NORMAL";
        if ("cameraType".equals(name)) return "REAR";
        if ("permissions".equals(name)) return "android.permission.READ_PHONE_STATE";
        if (descriptor.startsWith("int ") || descriptor.startsWith("long ")) return "0";
        return "";
    }

    private String defaultPathFor(String id) {
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
        if (id.endsWith("setBootAnimation")) return interfaceInputPath("boot/bootanimation.zip");
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
            return String.valueOf(android.telephony.SubscriptionManager.getDefaultDataSubscriptionId());
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
            return target.getAbsolutePath();
        }
    }

    private String parameterDescription(InterfaceEntry entry) {
        if (entry.parameters.length() == 0) {
            return getString(R.string.none);
        }
        StringBuilder builder = new StringBuilder(entry.parameters);
        if (entry.parameters.contains("boolean")) {
            builder.append("\n").append(getString(R.string.boolean_input_description, firstBooleanParameterName(entry.parameters)));
        }
        if (hasEnumParameter(entry)) {
            builder.append("\n").append(getString(R.string.enum_input_description));
        }
        if (hasTextInputParameter(entry)) {
            builder.append("\n").append(getString(R.string.key_value_input_description));
        }
        return builder.toString();
    }

    private String returnDescription(InterfaceEntry entry) {
        return "void".equals(entry.returnType) ? getString(R.string.return_void) : entry.returnType;
    }

    private String expectedResult(InterfaceEntry entry) {
        if ("BootManager".equals(entry.manager) && "setBootAnimation".equals(entry.method)) {
            return getString(R.string.expected_boot_animation);
        }
        String pdfExpected = pdfExpectedExample(entry);
        if (pdfExpected.length() > 0) {
            return pdfExpected;
        }
        if (entry.source.startsWith("Unsupported")) {
            return getString(R.string.expected_unsupported);
        }
        if ("void".equals(entry.returnType)) {
            return getString(R.string.expected_void);
        }
        if ("boolean".equals(entry.returnType)) {
            return getString(R.string.expected_boolean);
        }
        return getString(R.string.expected_value, entry.returnType);
    }

    private String readyToRunText(InterfaceEntry entry) {
        if ("BootManager".equals(entry.manager) && "setBootAnimation".equals(entry.method)) {
            return getString(R.string.ready_boot_animation);
        }
        return getString(R.string.ready_to_run);
    }

    private String pdfExpectedExample(InterfaceEntry entry) {
        String id = entry.manager + "." + entry.method;
        if (id.equals("DeviceInfoManager.getSerialNumber")) return "PDF example: \"AA61A432592600121\"";
        if (id.equals("DeviceInfoManager.getModel")) return "PDF example: \"P8\"";
        if (id.equals("DeviceInfoManager.getBrand")) return "PDF example: \"KOZEN\"";
        if (id.equals("DeviceInfoManager.getManufacturer")) return "PDF example: \"KOZEN\"";
        if (id.equals("DeviceInfoManager.getAndroidVersion")) return "PDF example: \"15\"";
        if (id.equals("DeviceInfoManager.getRomVersion")) return "PDF example: \"r2351_kozenos_combo_V0.0.1_20251101\"";
        if (id.equals("DeviceInfoManager.getFirmwareVersion")) return "PDF example: \"6801.35.0001.383.019\"";
        if (id.equals("DeviceInfoManager.getFirmwareId")) return "PDF example: \"r2351_kozenos_combo_V0.0.1_20251101\"";
        if (id.equals("DeviceInfoManager.getSystemProperty")) return "PDF example for ro.build.version.sdk: \"353\"";
        if (id.equals("DeviceInfoManager.getMac")) return "PDF example: \"00:1B:44:11:3A:B7\"";
        if (id.equals("DeviceInfoManager.getIPv4Address")) return "PDF example: \"192.168.1.100\"";
        return "";
    }

    private String implementationMethod(InterfaceEntry entry) {
        if (entry.source == null || entry.source.length() == 0) {
            return getString(R.string.implementation_unknown);
        }
        return entry.source;
    }

    private String supportStatusText(InterfaceEntry entry) {
        String status = manualStatus.getString(exposureKey(entry), "PENDING");
        if ("ALLOW".equals(status)) {
            return getString(R.string.support_allowed);
        }
        if ("DENY".equals(status)) {
            return getString(R.string.support_not_allowed);
        }
        return getString(R.string.support_pending);
    }

    private String booleanControlLabel(InterfaceEntry entry, boolean checked) {
        return getString(R.string.boolean_input_label, firstBooleanParameterName(entry.parameters), checked ? "true" : "false");
    }

    private boolean hasNonBooleanParameter(String parameters) {
        for (String descriptor : parameterDescriptors(parameters)) {
            if (!descriptor.trim().startsWith("boolean ")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTextInputParameter(InterfaceEntry entry) {
        Method method;
        try {
            method = findMethod(entry);
        } catch (NoSuchMethodException ignored) {
            return hasNonBooleanParameter(entry.parameters);
        }
        Class<?>[] types = method.getParameterTypes();
        String[] descriptors = parameterDescriptors(entry.parameters);
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            if (type == boolean.class || type.isEnum() || isCallbackType(type) || type.isArray()) {
                continue;
            }
            String parameterName = i < descriptors.length ? parameterName(descriptors[i]) : "arg" + i;
            if (isTimeFormatParameter(entry, parameterName)) {
                continue;
            }
            if (isAutoSampleObjectType(type)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean isAutoSampleObjectType(Class<?> type) {
        return type == Typeface.class
                || type == PrintText.class
                || type == PrintBarcode.class
                || type == PrintQrCode.class
                || type == PrintImage.class;
    }

    private boolean hasEnumParameter(InterfaceEntry entry) {
        try {
            Class<?>[] types = findMethod(entry).getParameterTypes();
            String[] descriptors = parameterDescriptors(entry.parameters);
            for (int i = 0; i < types.length; i++) {
                String parameterName = i < descriptors.length ? parameterName(descriptors[i]) : "arg" + i;
                if (isTimeFormatParameter(entry, parameterName)) {
                    return true;
                }
                Class<?> type = types[i];
                if (type.isEnum() || type == Typeface.class) {
                    return true;
                }
            }
        } catch (NoSuchMethodException ignored) {
            for (String descriptor : parameterDescriptors(entry.parameters)) {
                if (isEnumDescriptor(descriptor.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEnumDescriptor(String descriptor) {
        return descriptor.startsWith("Theme ")
                || descriptor.startsWith("FontSize ")
                || descriptor.startsWith("DisplaySize ")
                || descriptor.startsWith("Priority ")
                || descriptor.startsWith("WifiSecurityType ")
                || descriptor.startsWith("TimeZoneSyncSetting ")
                || descriptor.startsWith("DayOfWeek ")
                || descriptor.startsWith("QuickSettingTile ")
                || descriptor.startsWith("SystemLabel ");
    }

    private boolean isCallbackDescriptor(String descriptor) {
        return descriptor.startsWith("Callback ");
    }

    private boolean isAutoSampleObjectDescriptor(String descriptor) {
        return descriptor.startsWith("CameraType ")
                || descriptor.startsWith("Typeface ")
                || descriptor.startsWith("PrintText ")
                || descriptor.startsWith("PrintBarcode ")
                || descriptor.startsWith("PrintQrCode ")
                || descriptor.startsWith("PrintImage ");
    }

    private String firstBooleanParameterName(String parameters) {
        for (String descriptor : parameterDescriptors(parameters)) {
            String trimmed = descriptor.trim();
            if (trimmed.startsWith("boolean ")) {
                return parameterName(trimmed);
            }
        }
        return "value";
    }

    private String[] parameterDescriptors(String parameters) {
        if (parameters == null || parameters.trim().length() == 0) {
            return new String[0];
        }
        return parameters.split(",");
    }

    private String parameterName(String descriptor) {
        String[] pieces = descriptor.trim().split("\\s+");
        return pieces.length == 0 ? "" : pieces[pieces.length - 1].replace("[]", "");
    }

    private Map<String, String> parseInputValues(String rawInput) {
        Map<String, String> values = new HashMap<>();
        if (rawInput == null || rawInput.trim().length() == 0) {
            return values;
        }
        String[] pairs = rawInput.split("[,\\n]");
        for (String pair : pairs) {
            int separator = pair.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = pair.substring(0, separator).trim();
            String value = pair.substring(separator + 1).trim();
            if (key.length() > 0) {
                values.put(key, value);
            }
        }
        return values;
    }

    private String statusKey(InterfaceEntry entry) {
        return entry.manager + "#" + entry.method + "#" + entry.parameters;
    }

    private String exposureKey(InterfaceEntry entry) {
        return statusKey(entry) + "#exposure";
    }

    private String exposureKey(String manager, String method, String parameters) {
        return manager + "#" + method + "#" + (parameters == null ? "" : parameters) + "#exposure";
    }

    private String methodId(InterfaceEntry entry) {
        return entry.manager + "." + entry.method;
    }

    private String statusFor(InterfaceEntry entry) {
        String manual = rawManualStatus(entry);
        if (!"UNSET".equals(manual)) {
            return manual;
        }
        AutoResult autoResult = autoResultFor(entry);
        if (autoResult != null) {
            return autoResult.status;
        }
        return "UNSET";
    }

    private AutoResult autoResultFor(InterfaceEntry entry) {
        return autoResults.get(methodId(entry));
    }

    private String reasonKey(InterfaceEntry entry) {
        return statusKey(entry) + "#reason";
    }

    private String failureReasonFor(InterfaceEntry entry) {
        return manualStatus.getString(reasonKey(entry), "");
    }

    private String rawManualStatus(InterfaceEntry entry) {
        return manualStatus.getString(statusKey(entry), "UNSET");
    }

    private String manualStatusText(InterfaceEntry entry) {
        String status = rawManualStatus(entry);
        if (!"FAIL".equals(status) && !"DEVICE_PREREQUISITE_FAILED".equals(status)) {
            return status;
        }
        String reason = failureReasonFor(entry);
        if (reason.length() == 0) {
            return status;
        }
        return status + " - " + reason;
    }

    private String autoStatusText(InterfaceEntry entry) {
        AutoResult result = autoResultFor(entry);
        return result == null ? getString(R.string.auto_no_result) : result.summaryText();
    }

    private StatusCounts statusCounts(String manager) {
        StatusCounts counts = new StatusCounts();
        List<InterfaceEntry> entries = byManager.get(manager);
        if (entries == null) {
            return counts;
        }
        counts.total = entries.size();
        for (InterfaceEntry entry : entries) {
            String status = statusFor(entry);
            if ("PASS".equals(status) || "PASS_MANUAL_REVIEW".equals(status)) {
                counts.pass++;
            } else if ("FAIL".equals(status)) {
                counts.fail++;
            } else if ("DEVICE_PREREQUISITE_FAILED".equals(status)) {
                counts.prerequisiteFailed++;
            } else if ("UNSUPPORTED".equals(status)) {
                counts.unsupported++;
            } else if ("SKIPPED".equals(status)) {
                counts.skipped++;
            } else {
                counts.unset++;
            }
        }
        return counts;
    }

    private String managerStatus(String manager) {
        StatusCounts counts = statusCounts(manager);
        if (counts.fail > 0) return "FAIL";
        if (counts.prerequisiteFailed > 0 || counts.unsupported > 0) return "DEVICE_PREREQUISITE_FAILED";
        if (counts.skipped > 0) return "SKIPPED";
        if (counts.total > 0 && counts.pass == counts.total) return "PASS";
        return "UNSET";
    }

    private int statusBackground(String status) {
        if ("PASS".equals(status) || "PASS_MANUAL_REVIEW".equals(status)) return COLOR_PASS_BG;
        if ("FAIL".equals(status)) return COLOR_FAIL_BG;
        if ("DEVICE_PREREQUISITE_FAILED".equals(status) || "UNSUPPORTED".equals(status) || "SKIPPED".equals(status)) return COLOR_WARN_BG;
        return COLOR_UNSET_BG;
    }

    private int statusTextColor(String status) {
        if ("PASS".equals(status) || "PASS_MANUAL_REVIEW".equals(status)) return COLOR_PASS_TEXT;
        if ("FAIL".equals(status)) return COLOR_FAIL_TEXT;
        if ("DEVICE_PREREQUISITE_FAILED".equals(status) || "UNSUPPORTED".equals(status) || "SKIPPED".equals(status)) return COLOR_WARN_TEXT;
        return COLOR_UNSET_TEXT;
    }

    private void applyStatusColor(TextView view, String status) {
        view.setBackgroundColor(statusBackground(status));
        view.setTextColor(statusTextColor(status));
    }

    private void saveManagerSelection(String manager, int position) {
        uiPreferences.edit()
                .putString(PREF_PAGE, "FUNCTIONS")
                .putString(PREF_MANAGER, manager)
                .putInt(PREF_MANAGER_POSITION, Math.max(position, 0))
                .apply();
    }

    private void saveFunctionSelection(InterfaceEntry entry, int position) {
        uiPreferences.edit()
                .putString(PREF_PAGE, "DETAIL")
                .putString(PREF_MANAGER, entry.manager)
                .putString(PREF_ENTRY, statusKey(entry))
                .putInt(PREF_FUNCTION_POSITION_PREFIX + entry.manager, Math.max(position, 0))
                .apply();
    }

    private void saveEnumSelections() {
        if (selectedEntry == null || enumInputs.isEmpty()) {
            return;
        }
        SharedPreferences.Editor editor = uiPreferences.edit();
        for (Map.Entry<String, Spinner> entry : enumInputs.entrySet()) {
            Object selected = entry.getValue().getSelectedItem();
            if (selected != null) {
                editor.putString(enumPreferenceKey(entry.getKey()), String.valueOf(selected));
            }
        }
        editor.apply();
    }

    private String enumPreferenceKey(String parameterName) {
        String entryKey = selectedEntry == null ? "" : statusKey(selectedEntry);
        return "enum#" + entryKey + "#" + parameterName;
    }

    private void restoreLastUiState() {
        String savedPage = uiPreferences.getString(PREF_PAGE, "MANAGERS");
        String savedManager = uiPreferences.getString(PREF_MANAGER, "");
        String savedEntryKey = uiPreferences.getString(PREF_ENTRY, "");
        if ("DETAIL".equals(savedPage) && savedEntryKey.length() > 0) {
            InterfaceEntry entry = findEntryByKey(savedEntryKey);
            if (entry != null) {
                showDetail(entry);
                return;
            }
        }
        if (savedManager.length() > 0 && byManager.containsKey(savedManager)) {
            showFunctions(savedManager);
            return;
        }
        showManagers();
    }

    private InterfaceEntry findEntryByKey(String key) {
        for (InterfaceEntry entry : allEntries) {
            if (statusKey(entry).equals(key)) {
                return entry;
            }
        }
        return null;
    }

    private void loadAutoResults() {
        autoResults.clear();
        File dir = getExternalFilesDir(null);
        if (dir == null) {
            dir = getFilesDir();
        }
        File file = new File(dir, "capitec-auto-test-results.json");
        if (!file.exists()) {
            autoSummaryText = getString(R.string.auto_no_result);
            return;
        }
        try {
            JSONObject root = new JSONObject(readText(file));
            JSONArray results = root.optJSONArray("results");
            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject item = results.optJSONObject(i);
                    if (item == null) {
                        continue;
                    }
                    AutoResult result = AutoResult.from(item);
                    if (result.methodId.length() > 0) {
                        autoResults.put(result.methodId, result);
                    }
                }
            }
            autoSummaryText = getString(
                    R.string.auto_summary_template,
                    root.optString("timestamp", ""),
                    root.optInt("declaredEntries", autoResults.size()),
                    root.optInt("executed", 0),
                    root.optInt("passed", 0),
                    root.optInt("prerequisiteFailed", 0),
                    root.optInt("unsupported", 0),
                    root.optInt("failed", 0),
                    root.optInt("skipped", 0)
            );
        } catch (Exception exception) {
            autoSummaryText = getString(R.string.auto_load_failed, exception.getClass().getSimpleName(), exception.getMessage());
        }
    }

    private void loadExposureDecisions() {
        File file = firstExistingExposureFile();
        if (file == null) {
            return;
        }
        try {
            JSONObject root = new JSONObject(readText(file));
            JSONArray decisions = root.optJSONArray("decisions");
            if (decisions == null) {
                return;
            }
            SharedPreferences.Editor editor = manualStatus.edit();
            boolean changed = false;
            for (int i = 0; i < decisions.length(); i++) {
                JSONObject item = decisions.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                String decision = item.optString("decision", "PENDING");
                if (!isExposureDecision(decision)) {
                    continue;
                }
                String key = exposureKey(
                        item.optString("manager", ""),
                        item.optString("method", ""),
                        item.optString("parameters", "")
                );
                if (!manualStatus.contains(key)) {
                    editor.putString(key, decision);
                    changed = true;
                }
            }
            if (changed) {
                editor.apply();
            }
        } catch (Exception ignored) {
        }
    }

    private void saveExposureDecisions() {
        try {
            JSONObject root = new JSONObject();
            JSONArray decisions = new JSONArray();
            int pending = 0;
            int allow = 0;
            int deny = 0;
            for (InterfaceEntry entry : allEntries) {
                String decision = manualStatus.getString(exposureKey(entry), "PENDING");
                if (!isExposureDecision(decision)) {
                    decision = "PENDING";
                }
                if ("ALLOW".equals(decision)) {
                    allow++;
                } else if ("DENY".equals(decision)) {
                    deny++;
                } else {
                    pending++;
                }
                JSONObject item = new JSONObject();
                item.put("manager", entry.manager);
                item.put("method", entry.method);
                item.put("parameters", entry.parameters);
                item.put("function", entry.title());
                item.put("description", entry.translatedName());
                item.put("decision", decision);
                decisions.put(item);
            }
            JSONObject statistics = new JSONObject();
            statistics.put("total", allEntries.size());
            statistics.put("pending", pending);
            statistics.put("allow", allow);
            statistics.put("deny", deny);
            root.put("updatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            root.put("packageName", getPackageName());
            root.put("statistics", statistics);
            root.put("decisions", decisions);
            String payload = root.toString(2);
            for (File file : exposureDecisionFiles()) {
                try {
                    writeText(file, payload);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isExposureDecision(String value) {
        return "PENDING".equals(value) || "ALLOW".equals(value) || "DENY".equals(value);
    }

    private File firstExistingExposureFile() {
        for (File file : exposureDecisionFiles()) {
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    private List<File> exposureDecisionFiles() {
        List<File> files = new ArrayList<>();
        File publicDir = new File(android.os.Environment.getExternalStorageDirectory(), "CapitecBank");
        files.add(new File(publicDir, EXPOSURE_DECISIONS_FILE));
        File appExternalDir = getExternalFilesDir(null);
        if (appExternalDir != null) {
            files.add(new File(appExternalDir, EXPOSURE_DECISIONS_FILE));
        }
        files.add(new File(getFilesDir(), EXPOSURE_DECISIONS_FILE));
        return files;
    }

    private String readText(File file) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[4096];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, count);
            }
        }
        return builder.toString();
    }

    private void writeText(File file, String value) throws Exception {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(value);
        }
    }

    private final class RowAdapter extends BaseAdapter {
        @Override public int getCount() { return visibleRows.size(); }
        @Override public Object getItem(int position) { return visibleRows.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_capability, parent, false);
            }
            Object row = getItem(position);
            TextView title = view.findViewById(R.id.capabilityTitle);
            TextView subtitle = view.findViewById(R.id.capabilitySubtitle);
            if (row instanceof String) {
                String manager = (String) row;
                StatusCounts counts = statusCounts(manager);
                SourceCounts sourceCounts = sourceCounts(manager);
                String status = managerStatus(manager);
                view.setBackgroundColor(statusBackground(status));
                title.setText("#" + (position + 1) + " " + manager);
                title.setTextColor(statusTextColor(status));
                subtitle.setText(getString(
                        R.string.manager_row_simple_subtitle,
                        counts.total,
                        sourceCounts.unimplemented
                ));
                subtitle.setTextColor(statusTextColor(status));
            } else {
                InterfaceEntry entry = (InterfaceEntry) row;
                String status = statusFor(entry);
                view.setBackgroundColor(statusBackground(status));
                title.setText("#" + entry.index + " " + entry.method);
                title.setTextColor(statusTextColor(status));
                subtitle.setText(entry.translatedName() + "\n" + supportStatusText(entry));
                subtitle.setTextColor(statusTextColor(status));
            }
            return view;
        }
    }

    private static final class AutoResult {
        final String methodId;
        final String status;
        final String source;
        final String sampleInput;
        final String value;
        final String errorMessage;
        final String reason;
        final String manualReviewPrompt;

        private AutoResult(
                String methodId,
                String status,
                String source,
                String sampleInput,
                String value,
                String errorMessage,
                String reason,
                String manualReviewPrompt
        ) {
            this.methodId = methodId;
            this.status = status;
            this.source = source;
            this.sampleInput = sampleInput;
            this.value = value;
            this.errorMessage = errorMessage;
            this.reason = reason;
            this.manualReviewPrompt = manualReviewPrompt;
        }

        static AutoResult from(JSONObject item) {
            String methodId = item.optString("methodId");
            if (methodId.length() == 0) {
                methodId = item.optString("manager") + "." + item.optString("method");
            }
            return new AutoResult(
                    methodId,
                    item.optString("status", "UNSET"),
                    item.optString("source", ""),
                    item.optString("sampleInput", ""),
                    item.optString("value", ""),
                    item.optString("errorMessage", ""),
                    item.optString("reason", ""),
                    item.optString("manualReviewPrompt", "")
            );
        }

        String summaryText() {
            StringBuilder builder = new StringBuilder(status);
            if (value.length() > 0) {
                builder.append(" value=").append(compactText(value));
            }
            if (errorMessage.length() > 0) {
                builder.append(" error=").append(compactText(errorMessage));
            }
            if (reason.length() > 0) {
                builder.append(" reason=").append(compactText(reason));
            }
            if (manualReviewPrompt.length() > 0) {
                builder.append(" review=").append(compactText(manualReviewPrompt));
            }
            return builder.toString();
        }

        String detailText() {
            StringBuilder builder = new StringBuilder();
            builder.append("Auto: ").append(status);
            if (source.length() > 0) builder.append("\nSource: ").append(source);
            if (sampleInput.length() > 0) builder.append("\nSample input: ").append(sampleInput);
            if (value.length() > 0) builder.append("\nObserved value: ").append(compactText(value));
            if (errorMessage.length() > 0) builder.append("\nError: ").append(compactText(errorMessage));
            if (reason.length() > 0) builder.append("\nSkip reason: ").append(compactText(reason));
            if (manualReviewPrompt.length() > 0) builder.append("\nManual review: ").append(compactText(manualReviewPrompt));
            return builder.toString();
        }

        private static String compactText(String text) {
            if (text == null || text.length() <= 180) {
                return text == null ? "" : text;
            }
            if (text.startsWith("[") && text.endsWith("]")) {
                String body = text.substring(1, text.length() - 1);
                String[] items = body.length() == 0 ? new String[0] : body.split(", ");
                StringBuilder builder = new StringBuilder("[");
                int visible = Math.min(items.length, 4);
                for (int i = 0; i < visible; i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    builder.append(items[i]);
                }
                if (items.length > visible) {
                    builder.append(", ... total ").append(items.length).append(" items");
                }
                return builder.append("]").toString();
            }
            return text.substring(0, 177) + "...";
        }
    }

    private static final class StatusCounts {
        int total;
        int pass;
        int fail;
        int prerequisiteFailed;
        int unsupported;
        int skipped;
        int unset;
    }

    private SourceCounts sourceCounts(String manager) {
        SourceCounts counts = new SourceCounts();
        List<InterfaceEntry> entries = manager == null ? allEntries : byManager.get(manager);
        if (entries == null) {
            return counts;
        }
        for (InterfaceEntry entry : entries) {
            if (entry.source.startsWith("Unsupported")) {
                counts.unimplemented++;
            } else if (entry.source.startsWith("TerminalManagerLib")) {
                counts.terminal++;
            } else if (entry.source.startsWith("xcheng-framework.jar")) {
                counts.xcheng++;
            } else if (entry.source.startsWith("Android")) {
                counts.androidApi++;
            }
        }
        return counts;
    }

    private static final class SourceCounts {
        int unimplemented;
        int terminal;
        int xcheng;
        int androidApi;
    }
}
