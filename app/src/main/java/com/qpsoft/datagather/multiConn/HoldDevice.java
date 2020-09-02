package com.qpsoft.datagather.multiConn;

import java.io.Serializable;

public class HoldDevice implements Serializable {
    private boolean ssl;
    private String ip;
    private int port;
    private HoldDeviceType deviceType;
    private String name;
    private String sn;
    private boolean connStatus;
    private boolean open = true;

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HoldDeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(HoldDeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public boolean isConnStatus() {
        return connStatus;
    }

    public void setConnStatus(boolean connStatus) {
        this.connStatus = connStatus;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public String toString() {
        return "HoldDevice{" +
                "ssl=" + ssl +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", deviceType=" + deviceType +
                ", name='" + name + '\'' +
                ", sn='" + sn + '\'' +
                ", connStatus=" + connStatus +
                ", open=" + open +
                '}';
    }
}
