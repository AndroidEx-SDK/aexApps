package com.tencent.devicedemo;

/**
 * Created by xinshuhao on 16/7/24.
 */
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.tencent.devicedemo.interfac.NetworkCallBack;

import java.util.Map;
import java.util.Set;


import static com.lidroid.xutils.http.client.HttpRequest.HttpMethod.GET;
import static com.lidroid.xutils.http.client.HttpRequest.HttpMethod.POST;

public class XUtilsNetwork<T> {
    /**
     * Get请求
     *
     * @param url
     * @param networkCallBack
     */
    private static XUtilsNetwork xUtilsNetwork;

    public static XUtilsNetwork getInstance() {
        if (xUtilsNetwork == null) {
            xUtilsNetwork = new XUtilsNetwork();
        }
        return xUtilsNetwork;
    }

    private void doGetRequest(String url,
                              Map<String, Object> params,
                              final NetworkCallBack<T> networkCallBack) {
        // HttpUtils请求数据
        HttpUtils httpUtils = BaseApplication.getApplication().getHttpUtils();

        RequestParams requestParams = null;
        if (params != null && !params.isEmpty()) {
            requestParams = new RequestParams();

            // 获取参数集合
            Set<Map.Entry<String, Object>> set = params.entrySet();

            // 循环参数，设置到RequestParams
            for (Map.Entry<String, Object> entry : set) {
                // post method - addQueryStringParameter
                requestParams.addQueryStringParameter(entry.getKey(), entry.getValue() + "");
            }
        }

        // 异步
        httpUtils.send(GET, url, requestParams, new RequestCallBack<T>() {
            @Override
            public void onSuccess(ResponseInfo<T> responseInfo) {
                networkCallBack.onSuccess(responseInfo.result);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                networkCallBack.onFailure(s);
            }
        });
    }

    /**
     * Post请求
     *
     * @param url             请求的URL地址
     * @param params          是从外部传递的参数
     * @param networkCallBack
     */
    private void doPostRequest(String url,
                               Map<String, Object> params,
                               final NetworkCallBack<T> networkCallBack) {
        // HttpUtils请求数据
        HttpUtils httpUtils = BaseApplication.getApplication().getHttpUtils();

        RequestParams requestParams = null;
        if (params != null && !params.isEmpty()) {
            requestParams = new RequestParams();

            // 获取参数集合
            Set<Map.Entry<String, Object>> set = params.entrySet();

            // 循环参数，设置到RequestParams
            for (Map.Entry<String, Object> entry : set) {
                // post method - addBodyParameter
                requestParams.addBodyParameter(entry.getKey(), entry.getValue() + "");
            }
        }

        httpUtils.send(POST, url, requestParams, new RequestCallBack<T>() {
            @Override
            public void onSuccess(ResponseInfo<T> responseInfo) {
                networkCallBack.onSuccess(responseInfo.result);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                networkCallBack.onFailure(s);
            }
        });
    }

    public void getBgBanners(NetworkCallBack<T> gtRequestCallBack) {
        doGetRequest("http://idting.com/V2/Home/logoCarousel?type=2", null, gtRequestCallBack);

    }

}
