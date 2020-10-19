package com.qpsoft.datagather;

public class ProviderMessageEvent {
    private String eventId;
    private String event;

    public ProviderMessageEvent(String eventId, String event) {
        this.eventId = eventId;
        this.event = event;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
