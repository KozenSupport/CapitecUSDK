package com.capitec.interfaceapp.runtime;

public final class InterfaceResultRecorder {
    private static final String TERMINAL_MANAGER_LIB = "TerminalManagerLib_1.3.0.2_release.aar";
    private static final String FINANCIAL_LIB = "FinancialLib_1.5.0_release.aar";
    private static final String COMPONENT_LIB = "ComponentLib_1.3.0_release.aar";
    private static final String XCHENG_FRAMEWORK = "xcheng-framework.jar";

    private final LastResultStore results;

    public InterfaceResultRecorder(LastResultStore results) {
        this.results = results;
    }

    public void terminalSuccess(String method, Object value) {
        success(BackendRoute.TERMINAL_MANAGER_LIB, method, TERMINAL_MANAGER_LIB, value);
    }

    public void financialSuccess(String method, Object value) {
        success(BackendRoute.FINANCIAL_LIB, method, FINANCIAL_LIB, value);
    }

    public void componentSuccess(String method, Object value) {
        success(BackendRoute.COMPONENT_LIB, method, COMPONENT_LIB, value);
    }

    public void xchengSuccess(String method, Object value) {
        success(BackendRoute.XCHENG_FRAMEWORK, method, XCHENG_FRAMEWORK, value);
    }

    public void androidSuccess(String method, String note, Object value) {
        success(BackendRoute.ANDROID_SYSTEM_API, method, note, value);
    }

    public UnsupportedInterfaceException terminalFailure(String method, Throwable throwable) {
        return unsupported(method, "TerminalManagerLib call failed: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
    }

    public UnsupportedInterfaceException financialFailure(String method, Throwable throwable) {
        return unsupported(method, "FinancialLib call failed: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
    }

    public UnsupportedInterfaceException componentFailure(String method, Throwable throwable) {
        return unsupported(method, "ComponentLib call failed: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
    }

    public UnsupportedInterfaceException xchengFailure(String method, Throwable throwable) {
        return unsupported(method, "xcheng-framework.jar call failed: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
    }

    public UnsupportedInterfaceException unsupported(String method, String note) {
        UnsupportedInterfaceException exception = new UnsupportedInterfaceException(method, note);
        results.set(InterfaceResult.unsupported(method, note));
        return exception;
    }

    private void success(BackendRoute route, String method, String message, Object value) {
        results.set(InterfaceResult.success(route, method, message, value));
    }
}
