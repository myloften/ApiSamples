// IRemoteService.aidl
package com.loften.android.api.app;

import com.loften.android.api.app.IRemoteServiceCallback;

interface IRemoteService {

    void registerCallback(IRemoteServiceCallback cb);

    void unregisterCallback(IRemoteServiceCallback cb);
}
