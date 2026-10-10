package com.capitec.interfaceapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.capitec.interfaceapp.api.managers.physicalkeyboardmanager.PinKeyCoordInfo;
import com.capitec.interfaceapp.impl.PhysicalKeyboardManager;
import com.kozen.financial.constant.ConstantEmv.POIEmvCoreManager;
import com.kozen.financial.constant.ConstantEmv.POIEmvCoreManager.EmvPinConstraints;
import com.kozen.financial.constant.ConstantSecurity;
import com.kozen.financial.engine.FinancialEngine;
import com.kozen.financial.pinpad.PinpadInputCallback;

import java.util.Arrays;

/** Manual online-PIN test using the coordinate adapter and the FinancialServiceDemo parameters. */
public final class PinAdapterTestActivity extends Activity {
    private static final long TEST_TIMEOUT_MS = 60000;
    private static final String TEST_PAN = "6217680206725660";
    private static final int TEST_TPK_INDEX = 12;
    private static final String[] KEY_IDS = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "-21", "0", "-40", "-35"
    };

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final TextView[] keyViews = new TextView[KEY_IDS.length];
    private PhysicalKeyboardManager manager;
    private TextView statusTitle;
    private TextView resultText;
    private Button startButton;
    private Button cancelButton;
    private boolean active;
    private boolean prepared;
    private boolean visible = true;
    private int generation;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CapitecInterfaceActivity.applySavedLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        manager = new PhysicalKeyboardManager(this);

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setPadding(dp(16), dp(12), dp(16), dp(16));
        screen.setBackgroundColor(Color.rgb(245, 247, 246));
        screen.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(dp(16), dp(12) + insets.getSystemWindowInsetTop(),
                    dp(16), dp(16) + insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(screen);

        TextView backButton = new TextView(this);
        backButton.setText(R.string.pin_back);
        backButton.setTextSize(16);
        backButton.setTextColor(Color.rgb(25, 94, 80));
        backButton.setGravity(Gravity.CENTER_VERTICAL);
        backButton.setPadding(dp(4), 0, 0, 0);
        backButton.setOnClickListener(view -> finish());
        screen.addView(backButton, new LinearLayout.LayoutParams(-1, dp(40)));

        TextView heading = new TextView(this);
        heading.setText(R.string.pin_title);
        heading.setTextSize(24);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setTextColor(Color.rgb(16, 35, 31));
        screen.addView(heading);

        TextView implementation = new TextView(this);
        implementation.setText(R.string.pin_implementation);
        implementation.setTextSize(12);
        implementation.setTextColor(Color.rgb(25, 94, 80));
        implementation.setPadding(dp(12), dp(8), dp(12), dp(8));
        implementation.setBackground(rounded(Color.rgb(225, 239, 235), 12));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-1, -2);
        badgeParams.setMargins(0, dp(10), 0, dp(14));
        screen.addView(implementation, badgeParams);

        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(dp(16), dp(14), dp(16), dp(14));
        statusCard.setBackground(rounded(Color.WHITE, 16));
        statusTitle = new TextView(this);
        statusTitle.setText(R.string.pin_initializing);
        statusTitle.setTextSize(18);
        statusTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        statusTitle.setTextColor(Color.rgb(16, 35, 31));
        statusCard.addView(statusTitle);

        resultText = new TextView(this);
        resultText.setText(R.string.pin_please_wait);
        resultText.setTextSize(14);
        resultText.setTextColor(Color.rgb(79, 94, 90));
        resultText.setPadding(0, dp(6), 0, 0);
        resultText.setTextIsSelectable(true);
        statusCard.addView(resultText);
        screen.addView(statusCard, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout actions = new LinearLayout(this);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-1, dp(52));
        actionsParams.setMargins(0, dp(12), 0, dp(12));
        screen.addView(actions, actionsParams);

        startButton = new Button(this);
        startButton.setText(R.string.pin_retry);
        startButton.setEnabled(false);
        startButton.setOnClickListener(view -> startTest());
        actions.addView(startButton, new LinearLayout.LayoutParams(0, -1, 1));

        cancelButton = new Button(this);
        cancelButton.setText(R.string.pin_cancel);
        cancelButton.setEnabled(false);
        cancelButton.setOnClickListener(view -> stopTest(R.string.pin_user_cancelled));
        actions.addView(cancelButton, new LinearLayout.LayoutParams(0, -1, 1));

        TextView keyboardLabel = new TextView(this);
        keyboardLabel.setText(R.string.pin_keyboard);
        keyboardLabel.setTextSize(13);
        keyboardLabel.setTextColor(Color.rgb(79, 94, 90));
        screen.addView(keyboardLabel);

        for (int row = 0; row < 4; row++) {
            LinearLayout keyRow = new LinearLayout(this);
            screen.addView(keyRow, new LinearLayout.LayoutParams(-1, 0, 1));
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                int labelId = index == 9 ? R.string.pin_cancel
                        : index == 11 ? R.string.pin_delete : 0;
                TextView key = createKey(labelId == 0 ? KEY_IDS[index] : getString(labelId),
                        false, labelId != 0);
                keyViews[index] = key;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1);
                params.setMargins(dp(3), dp(3), dp(3), dp(3));
                keyRow.addView(key, params);
            }
        }
        TextView confirm = createKey(getString(R.string.pin_confirm), true, false);
        keyViews[12] = confirm;
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(-1, dp(60));
        confirmParams.setMargins(dp(3), dp(3), dp(3), 0);
        screen.addView(confirm, confirmParams);
        screen.post(this::prepareTest);
    }

    private TextView createKey(String label, boolean confirm, boolean action) {
        TextView key = new TextView(this);
        key.setText(label);
        key.setTextSize(24);
        key.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        key.setTextColor(confirm ? Color.WHITE : Color.rgb(16, 35, 31));
        key.setGravity(Gravity.CENTER);
        key.setBackground(rounded(confirm ? Color.rgb(20, 108, 92)
                : action ? Color.rgb(225, 239, 235) : Color.WHITE, 12));
        return key;
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private void prepareTest() {
        if (active) return;
        prepared = false;
        startButton.setEnabled(false);
        setStatus(getString(R.string.pin_initializing), getString(R.string.pin_please_wait));
        new Thread(() -> {
            String failure = null;
            try {
                if (FinancialEngine.INSTANCE.getPinpadManager() == null
                        || FinancialEngine.INSTANCE.getSecurityManager() == null) {
                    failure = getString(R.string.pin_sdk_not_ready);
                } else {
                    byte[] kcv = new byte[8];
                    int code = FinancialEngine.INSTANCE.getSecurityManager().getKCV(
                            TEST_TPK_INDEX, ConstantSecurity.PED_TPK, kcv);
                    Arrays.fill(kcv, (byte) 0);
                    if (code != 0) {
                        failure = getString(R.string.pin_key_unavailable, code);
                    }
                }
            } catch (Throwable error) {
                failure = getString(R.string.pin_environment_failed,
                        error.getClass().getSimpleName(), String.valueOf(error.getMessage()));
            }
            final String result = failure;
            mainHandler.post(() -> {
                if (!visible) return;
                if (result != null) {
                    setStatus(getString(R.string.pin_init_failed), result);
                    return;
                }
                prepared = true;
                startTest();
            });
        }, "pin-test-preflight").start();
    }

    private void startTest() {
        if (active) return;
        if (!prepared || !visible) return;
        PinKeyCoordInfo[] coords = new PinKeyCoordInfo[KEY_IDS.length];
        for (int i = 0; i < coords.length; i++) {
            View key = keyViews[i];
            int[] location = new int[2];
            key.getLocationOnScreen(location);
            if (!key.isShown() || key.getWidth() <= 0 || key.getHeight() <= 0) {
                setStatus(getString(R.string.pin_start_failed), getString(R.string.pin_layout_not_ready));
                return;
            }
            // 坐标接口实现：将当前屏幕上可见按键的实际屏幕矩形传入适配层。
            coords[i] = new PinKeyCoordInfo(
                    location[0], location[1], key.getWidth(), key.getHeight(), KEY_IDS[i]);
        }

        Bundle config = new Bundle();
        config.putInt(EmvPinConstraints.PIN_TYPE, POIEmvCoreManager.PIN_ONLINE_PIN);
        config.putByteArray(EmvPinConstraints.PIN_LENGTH_LIMIT,
                new byte[]{0, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        config.putInt(EmvPinConstraints.PIN_TIMEOUT, 60);
        config.putString(EmvPinConstraints.PIN_CARD, TEST_PAN);
        config.putInt(EmvPinConstraints.PIN_KEY_MODE, ConstantSecurity.ENCRYPTION_MECHANISM_MK_SK);
        config.putInt(EmvPinConstraints.PIN_KEY_ALGORITHM, ConstantSecurity.ENCRYPTION_ALGORITHM_TDES);
        config.putInt(EmvPinConstraints.PIN_KEY_INDEX, TEST_TPK_INDEX);
        config.putInt(EmvPinConstraints.PIN_BLOCK_FORMAT, ConstantSecurity.PINBLOCK_TPK_FMT_ISO9564_0);
        config.putBoolean(EmvPinConstraints.PIN_IS_ORDER, true);
        config.putBoolean(EmvPinConstraints.PIN_BYPASS, false);
        config.putBoolean(EmvPinConstraints.PIN_BYPASS_ALL_TYPE, false);

        final int currentGeneration = ++generation;
        active = true;
        startButton.setEnabled(false);
        cancelButton.setEnabled(true);
        setStatus(getString(R.string.pin_ready), getString(R.string.pin_ready_hint));
        try {
            // 被测实现：坐标重载由 PhysicalKeyboardManager 转译到厂商 View 接口。
            manager.startInputPin(config, coords, new PinpadInputCallback() {
                @Override
                public void onInput(int length, int key) {
                    mainHandler.post(() -> {
                        if (generation == currentGeneration && active) {
                            setStatus(getString(R.string.pin_ready), getString(R.string.pin_input_length, length));
                        }
                    });
                }

                @Override
                public void onPinSuccess(int result, byte[] pinData, String message) {
                    showCallback(currentGeneration,
                            getString(R.string.pin_success_result, result, toHex(pinData)));
                }

                @Override
                public void onPinError(int code, int detail) {
                    showCallback(currentGeneration, getString(R.string.pin_error_result, code, detail));
                }

                @Override
                public void onScreenRotation() {
                    mainHandler.post(() -> {
                        if (generation == currentGeneration && active) stopTest(R.string.pin_rotation);
                    });
                }
            });
            mainHandler.postDelayed(() -> {
                if (active && generation == currentGeneration) stopTest(R.string.pin_timeout);
            }, TEST_TIMEOUT_MS);
        } catch (Throwable error) {
            active = false;
            startButton.setEnabled(prepared);
            cancelButton.setEnabled(false);
            setStatus(getString(R.string.pin_start_failed),
                    getString(R.string.pin_environment_failed,
                            error.getClass().getSimpleName(), String.valueOf(error.getMessage())));
        }
    }

    private void showCallback(int callbackGeneration, String message) {
        mainHandler.post(() -> {
            if (generation != callbackGeneration || !active) return;
            active = false;
            generation++;
            startButton.setEnabled(prepared);
            cancelButton.setEnabled(false);
            int lineBreak = message.indexOf('\n');
            setStatus(lineBreak < 0 ? message : message.substring(0, lineBreak),
                    lineBreak < 0 ? "" : message.substring(lineBreak + 1));
        });
    }

    private void stopTest(int reasonRes) {
        if (!active) return;
        active = false;
        generation++;
        try {
            manager.cancelInputPin();
            setStatus(getString(reasonRes), "");
        } catch (Throwable error) {
            setStatus(getString(R.string.pin_cancel_failed), "");
        }
        startButton.setEnabled(prepared);
        cancelButton.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        visible = true;
        if (prepared && !active) resultText.post(this::startTest);
    }

    @Override
    protected void onStop() {
        visible = false;
        stopTest(R.string.pin_page_left);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private String toHex(byte[] data) {
        if (data == null || data.length == 0) return getString(R.string.pin_no_block);
        char[] digits = "0123456789ABCDEF".toCharArray();
        char[] output = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int value = data[i] & 0xff;
            output[i * 2] = digits[value >>> 4];
            output[i * 2 + 1] = digits[value & 0x0f];
        }
        return new String(output);
    }

    private void setStatus(String title, String detail) {
        statusTitle.setText(title);
        resultText.setText(detail);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
