package com.suraku.trafficalarm.data.extensions;

import com.suraku.trafficalarm.models.Event;

/**
 * Enum for event types
 */

public enum EventLevel
{
    DEBUG(0),
    ERROR(1),
    LOW(2),
    MED(3),
    HIGH(4);

    private final int number;
    public int getNumber() { return number; }

    public static EventLevel getLevel(int val) {
        for (EventLevel level : EventLevel.values()) {
            if (level.getNumber() == val) {
                return level;
            }
        }
        return null;
    }

    EventLevel(int number) { this.number = number; }
}
