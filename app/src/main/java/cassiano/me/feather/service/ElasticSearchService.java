package cassiano.me.feather.service;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.HttpEntity;

/**
 * Created by matheus on 6/7/16.
 */

public class ElasticSearchService {

    private static final String BASE_URL = "http://featheres.cassiano.me:9200/%s/%s";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String index, String action, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(index, action), params, responseHandler);
    }

    public static void post(Context context,
                     String index, String action,
                     HttpEntity entity, AsyncHttpResponseHandler responseHandler) {

        client.post(context, getAbsoluteUrl(index, action), entity, "application/json", responseHandler);
    }

    public static void cancelRequests() {
        client.cancelAllRequests(true);
    }

    private static String getAbsoluteUrl(String index, String action) {
        return String.format(BASE_URL, index, action);
    }
}
