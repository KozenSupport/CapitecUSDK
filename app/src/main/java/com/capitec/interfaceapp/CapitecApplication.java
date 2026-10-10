package com.capitec.interfaceapp;

import android.app.Application;
import android.content.Context;

import com.kozen.component_client.ComponentEngine;
import com.kozen.financial.engine.FinancialEngine;
import com.kozen.terminalmanager.TerminalManager;

public final class CapitecApplication extends Application {
    private static String terminalStatus = "not initialized";
    private static String financialStatus = "not initialized";
    private static String componentStatus = "not initialized";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeVendorSdks(this);
    }

    public static synchronized String sdkStatus() {
        return "Terminal=" + terminalStatus + "; Financial=" + financialStatus + "; Component=" + componentStatus;
    }

    private static synchronized void initializeVendorSdks(Context context) {
        Context appContext = context.getApplicationContext();
        try {
            TerminalManager.INSTANCE.init(appContext, (code, message) -> terminalStatus = code + " " + message);
        } catch (Throwable throwable) {
            terminalStatus = failureStatus(throwable);
        }
        try {
            FinancialEngine.INSTANCE.init(appContext, (code, message) -> financialStatus = code + " " + message);
        } catch (Throwable throwable) {
            financialStatus = failureStatus(throwable);
        }
        try {
            ComponentEngine.INSTANCE.init(appContext, (code, message) -> componentStatus = code + " " + message);
        } catch (Throwable throwable) {
            componentStatus = failureStatus(throwable);
        }
    }

    private static String failureStatus(Throwable throwable) {
        return "init failed: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
    }
}
