package com.suraku.trafficalarm.data.extensions;

/**
 * Enum for method switching
 */

public enum ListMethod
{
    FIND_ITEM_POSITION(0),
    INSERT_AT_POSITION(1),
    REMOVE_AT_POSITION(2);

    private final int number;
    public int getNumber() { return number; }

    ListMethod(int number) {
        this.number = number;
    }
}
