package com.qpsoft.datagather;

import android.content.ContentValues
import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.qpsoft.datagather.contentprovider.InvokeChannelConstants

object ProviderUtil {
    fun sendData(eventId: String, event: String, data: String, context: Context) {
        val cv = ContentValues();
        cv.apply {
            put(InvokeChannelConstants.Key.EVENT_ID, eventId)
            put(InvokeChannelConstants.Key.EVENT, event)
            put(InvokeChannelConstants.Key.DATA, data)
        }
        try {
            context.contentResolver.insert(InvokeChannelConstants.ContentProvider.Cli2WMPF.URI_NOTIFY_INVOKE_CHANNEL_EVENT, cv)
            LogUtils.i("TAG", "send message success, content: event success")
        } catch (e: Exception) {
            LogUtils.e("TAG", "callback invoke channel error")
        }
    }
}
