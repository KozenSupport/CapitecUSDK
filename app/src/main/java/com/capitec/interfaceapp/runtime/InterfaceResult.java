package com.capitec.interfaceapp.runtime;

public final class InterfaceResult {
    public final BackendRoute route;
    public final String status;
    public final boolean success;
    public final String title;
    public final String message;
    public final Object value;

    private InterfaceResult(BackendRoute route, String status, boolean success, String title, String message, Object value) {
        this.route = route;
        this.status = status;
        this.success = success;
        this.title = title == null ? "" : title;
        this.message = message == null ? "" : message;
        this.value = value;
    }

    public static InterfaceResult success(BackendRoute route, String title, String message, Object value) {
        return new InterfaceResult(route, "PASS", true, title, message, value);
    }

    public static InterfaceResult unsupported(String title, String message) {
        return new InterfaceResult(BackendRoute.UNSUPPORTED, UnsupportedInterfaceException.STATUS_UNSUPPORTED, false, title, message, UnsupportedInterfaceException.STATUS_UNSUPPORTED);
    }

    public String render() {
        String valueText = "\nValue: " + ValueFormatter.format(value);
        return title + "\nStatus: " + status + "\nRoute: " + route + "\nSuccess: " + success + "\nReason: " + message + valueText;
    }
}
