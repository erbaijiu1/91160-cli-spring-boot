package com.erbaijiu.config;

import cn.hutool.core.collection.CollUtil;
import com.ejlchina.data.jackson.JacksonDataConvertor;
import com.ejlchina.json.JSONKit;
import com.erbaijiu.client.MainClient;
import com.erbaijiu.common.constant.SystemConstant;
import com.erbaijiu.common.cookie.CookieManager;
import com.erbaijiu.common.proxy.SwitchProxySelector;
import com.erbaijiu.common.retrofit.BasicTypeConverterFactory;
import com.erbaijiu.common.retrofit.BodyCallAdapterFactory;
import com.erbaijiu.common.retrofit.ResponseCallAdapterFactory;
import com.erbaijiu.interceptor.LoggingInterceptor;
import com.erbaijiu.interceptor.MainClientInterceptor;
import com.erbaijiu.interceptor.ProxyInterceptor;
import com.erbaijiu.interceptor.RetryInterceptor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author pengpan
 */
@Configuration
public class RetrofitConfiguration {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new ProxyInterceptor())
                .addInterceptor(new MainClientInterceptor())
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new RetryInterceptor())
                .proxySelector(new SwitchProxySelector())
                .followRedirects(false)
                .cookieJar(new CookieManager())
                .connectionPool(new ConnectionPool(200, 2, TimeUnit.MINUTES))
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public Retrofit retrofit(OkHttpClient okHttpClient, ObjectMapper objectMapper) {
        return new Retrofit.Builder()
                .baseUrl(SystemConstant.DOMAIN)
                .client(okHttpClient)
                .addCallAdapterFactory(new BodyCallAdapterFactory())
                .addCallAdapterFactory(new ResponseCallAdapterFactory())
                .addConverterFactory(new BasicTypeConverterFactory())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();
    }

    @Bean
    public Void initJSONKit(ObjectMapper objectMapper) {
        JSONKit.init(new JacksonDataConvertor(objectMapper));
        return null;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Bean
    public MainClient mainClient(Retrofit retrofit) {
        return retrofit.create(MainClient.class);
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        Cache cache = new ConcurrentMapCache("KEY_LIST");
        List<Cache> caches = CollUtil.newArrayList(cache);
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
