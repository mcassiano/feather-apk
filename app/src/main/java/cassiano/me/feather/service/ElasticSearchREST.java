package cassiano.me.feather.service;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.UnsupportedEncodingException;

import cassiano.me.feather.service.handlers.ElasticSearchResponseHandler;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;

/**
 * Created by matheus on 6/7/16.
 */

public class ElasticSearchREST {

    private String index = "";

    public void searchAutocomplete(Context context, String query, final ElasticSearchResponseHandler handler) {
        search(context, index, "_search?size=5", query, handler);
    }

    public void searchDocuments(Context context, String query, final ElasticSearchResponseHandler handler) {
        search(context, index, "_search?size=10", query, handler);
    }


    private void search(Context context, String index, String action, String query, final ElasticSearchResponseHandler handler) {

        ByteArrayEntity entity = new ByteArrayEntity(query.getBytes());
        ElasticSearchService.post(context, index, action, entity, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                handler.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String str;

                try {
                    str = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    str = null;
                }

                if (str == null) {
                    onFailure(statusCode, headers, responseBody, new Exception("Failed parsing response."));
                    return;
                }

                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(str);

                handler.onSuccess(jsonElement);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                handler.onFailed();
            }
        });
    }

    public void cancelRequests() {
        ElasticSearchService.cancelRequests();
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
