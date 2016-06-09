package cassiano.me.feather.ui;

import android.content.Context;
import android.content.DialogInterface;

import com.afollestad.materialdialogs.MaterialDialog;

import cassiano.me.feather.R;

/**
 * Created by matheus on 6/8/16.
 */

public class Util {

    public static MaterialDialog getProgressBar(Context context, int contentRes, DialogInterface.OnCancelListener handler) {

        return new MaterialDialog.Builder(context)
                .title(R.string.progress_dialog)
                .content(contentRes)
                .cancelable(true)
                .cancelListener(handler)
                .widgetColorRes(R.color.charcoal_grey)
                .progress(true, 0)
                .build();

    }
}
