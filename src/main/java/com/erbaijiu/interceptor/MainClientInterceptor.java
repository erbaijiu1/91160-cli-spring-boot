package com.erbaijiu.interceptor;

import com.erbaijiu.common.constant.SystemConstant;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author pengpan
 */
public class MainClientInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("User-Agent", SystemConstant.DEFECT_USER_AGENT)
                .addHeader("Referer", SystemConstant.DOMAIN)
                .addHeader("Origin", SystemConstant.DOMAIN)
                .addHeader("Connection", "keep-alive")
                .addHeader("Pragma", "no-cache")
                .addHeader("Cache-Control", "no-cache")
                .build();
        return chain.proceed(newReq);
    }
}
