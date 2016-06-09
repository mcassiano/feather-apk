package cassiano.me.feather.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cassiano.me.feather.R;
import jp.wasabeef.glide.transformations.CropCircleTransformation;


public class ResultAdapter extends BindableAdapter<JsonObject> {

    private JsonArray data;

    public ResultAdapter(Context context, JsonArray data) {
        super(context);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public JsonObject getItem(int position) {
        return data.get(position).getAsJsonObject();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.search_result_item, container, false);
        ResultViewHolder holder = new ResultViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(JsonObject result, int position, View view) {
        ResultViewHolder holder = (ResultViewHolder) view.getTag();

        JsonObject source = result.get("_source").getAsJsonObject();


        if (source.has("wikipedia_entry") && !source.get("wikipedia_entry").isJsonNull()) {
            JsonObject wikipedia_entry = source.get("wikipedia_entry").getAsJsonObject();
            holder.title.setText(wikipedia_entry.get("title").getAsString());
        }
        else {

            if (!source.get("labels").getAsJsonObject().get("ptbr").isJsonNull())
                holder.title.setText(source.get("labels").getAsJsonObject().get("ptbr").getAsString());


            else if (!source.get("labels").getAsJsonObject().get("en").isJsonNull()) {
                holder.title.setText(String.format("%s (%s)",
                        source.get("labels").getAsJsonObject().get("en").getAsString(),
                        getContext().getString(R.string.english)));
            }

            else
                holder.title.setText(R.string.missing_title);
        }


        if (!source.get("descriptions").getAsJsonObject().get("ptbr").isJsonNull()) {
            holder.description.setText(
                    source.get("descriptions").getAsJsonObject().get("ptbr").getAsString());
        }

        else if (!source.get("descriptions").getAsJsonObject().get("en").isJsonNull()) {
            holder.description.setText(
                    String.format("%s (%s)", source.get("descriptions").getAsJsonObject()
                            .get("en").getAsString(), getContext().getString(R.string.english)));
        }

        else
            holder.description.setText(R.string.description_missing);


        holder.score.setText(String.valueOf(result.get("_score").getAsDouble()));

        if (source.has("thumbnail") && !source.get("thumbnail").isJsonNull()) {
            Glide.with(getContext())
                    .load(source.get("thumbnail").getAsString() + "&w=100")
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .into(holder.pic);
        }

        else {
            Glide.clear(holder.pic);
            Glide.with(getContext()).load(R.drawable.avatar_placeholder).into(holder.pic);
        }


    }

    public void add(JsonArray data) {
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    public void clear() {
        this.data = new JsonArray();
        notifyDataSetChanged();
    }

    public static class ResultViewHolder {

        @BindView(R.id.title) TextView title;
        @BindView(R.id.short_description) TextView description;
        @BindView(R.id.score) TextView score;
        @BindView(R.id.pic) ImageView pic;

        public ResultViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}