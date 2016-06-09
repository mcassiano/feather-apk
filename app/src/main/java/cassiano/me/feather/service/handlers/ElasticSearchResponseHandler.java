package cassiano.me.feather.service.handlers;

import com.google.gson.JsonElement;

/**
 * Created by matheus on 6/7/16.
 */

public interface ElasticSearchResponseHandler {

    void onSuccess(JsonElement result);
    void onFailed();
    void onStart();
}
