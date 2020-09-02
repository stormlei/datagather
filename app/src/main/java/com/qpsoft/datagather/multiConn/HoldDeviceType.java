package com.qpsoft.datagather.multiConn;

public enum HoldDeviceType {
    Wel(0),
    Suo(1);

    private int value = 0;
    HoldDeviceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
