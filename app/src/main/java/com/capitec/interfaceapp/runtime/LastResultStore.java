package com.capitec.interfaceapp.runtime;

public final class LastResultStore {
    private InterfaceResult result;

    public void set(InterfaceResult result) {
        this.result = result;
    }

    public InterfaceResult get() {
        return result;
    }
}
