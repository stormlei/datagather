package com.qpsoft.datagather;

import com.blankj.utilcode.util.LogUtils;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class MyHttpd extends NanoHTTPD {

    public MyHttpd(int port) {
        super(port);
    }

    public MyHttpd(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
//        String msg = "<html><body><h1>Hello server</h1>\n";
//        Map<String, String> parms = session.getParms();
//        if (parms.get("username") == null) {
//            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
//        } else {
//            msg += "<p>Hello, " + parms.get("username") + "!</p>";
//        }
//        return newFixedLengthResponse( msg + "</body></html>\n" );

        LogUtils.e("-------"+session.getUri());
        return newFixedLengthResponse("hello");
    }
}
