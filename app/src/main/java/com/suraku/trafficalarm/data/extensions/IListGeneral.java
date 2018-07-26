package com.suraku.trafficalarm.data.extensions;

/**
 * Generic list methods
 */

public interface IListGeneral
{
    void insertAtPosition(Object model, int position);

    void removeAtPosition(int position);

    int findItemPosition(Object model);
}
