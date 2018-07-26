package com.suraku.trafficalarm.data.storage;

import android.content.Context;
import android.util.Log;

/**
 * Factory to determine relevant provider usage
 */

public final class DataStorageFactory
{
    public static IBaseStorageProvider getProvider(Class providerInterface, Context context)
    {
        if (providerInterface == null || providerInterface == ILocalStorageProvider.class) {
            return new LocalStorageProvider(context);
        }
        else if (providerInterface == IServerStorageProvider.class) {
            return new ServerStorageProvider(context);
        }

        Log.d("APP", "DataStorageFactory_getProvider - unable to determine provider.");
        return null;
    }

    public static ILocalStorageProvider getProvider(Context context, Class dbModelType)
    {
        ILocalStorageProvider provider = (ILocalStorageProvider) DataStorageFactory.getProvider(
                ILocalStorageProvider.class, context
        );

        if (provider != null) {
            provider.setModelClass(dbModelType);
        }

        return provider;
    }
}
