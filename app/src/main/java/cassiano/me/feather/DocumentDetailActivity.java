package cassiano.me.feather;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import cassiano.me.feather.ui.Util;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DocumentDetailActivity extends AppCompatActivity {

    private static final String[] social = {"linkedin", "youtube", "facebook", "twitter", "imdb", "instagram", "website"};

    @BindView(R.id.profile_image)
    ImageView profilePic;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.short_description)
    TextView description;

    @BindView(R.id.paragraph)
    TextView paragraph;

    @BindView(R.id.akas)
    TextView akas;

    @BindView(R.id.social_links)
    LinearLayout socialLinks;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private JsonObject document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_detail);
        ButterKnife.bind(this);

        setupToolbar();
        parseDocument();
        setupProfile();
        setTitle(getString(R.string.title_detail_activity));

    }

    private void setupToolbar() {

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void parseDocument() {
        String documentJsonStr = getIntent().getExtras().getString("document");

        JsonElement obj = new JsonParser().parse(documentJsonStr);
        document = obj.getAsJsonObject();
    }

    private void setupProfile() {

        JsonObject source = document.get("_source").getAsJsonObject();

        if (source.has("thumbnail")
                && !source.get("thumbnail").isJsonNull()) {

            Glide.with(this)
                    .load(source.get("thumbnail").getAsString() + "&w=100")
                    .placeholder(R.drawable.avatar_placeholder_img)
                    .bitmapTransform(new CropCircleTransformation(this))
                    .into(profilePic);

        }

        String title = getString(R.string.missing_title);

        if (source.has("wikipedia_entry") && !source.get("wikipedia_entry").isJsonNull()) {
            JsonObject wikipedia_entry = source.get("wikipedia_entry").getAsJsonObject();
            title = wikipedia_entry.get("title").getAsString();

            JsonArray jsonArray = wikipedia_entry
                    .get("text").getAsJsonObject()
                    .get("Intro").getAsJsonArray();

            String desc = "";

            for (int i = 0; i < jsonArray.size(); i++) {

                if (i < 3) {
                    JsonObject parag = jsonArray.get(i).getAsJsonObject();
                    desc += parag.get("text").getAsString() + " ";
                }
                else
                    i = jsonArray.size();
            }

            paragraph.setText(desc);
        }
        else {

            if (!source.get("labels").getAsJsonObject().get("ptbr").isJsonNull())
                title = (source.get("labels").getAsJsonObject().get("ptbr").getAsString());


            else if (!source.get("labels").getAsJsonObject().get("en").isJsonNull()) {
                title = String.format("%s (%s)",
                        source.get("labels").getAsJsonObject().get("en").getAsString(),
                        getString(R.string.english));
            }

        }

        this.title.setText(title);

        if (!source.get("descriptions").getAsJsonObject().get("ptbr").isJsonNull()) {
            description.setText(
                    source.get("descriptions").getAsJsonObject().get("ptbr").getAsString());
        }

        else if (!source.get("descriptions").getAsJsonObject().get("en").isJsonNull()) {
            description.setText(
                    String.format("%s (%s)", source.get("descriptions").getAsJsonObject()
                            .get("en").getAsString(), getString(R.string.english)));
        }

        else
            description.setText(R.string.description_missing);


        for (final String link: social) {

            if (source.has(link) && !source.get(link).isJsonNull()) {

                final String id = source.get(link).getAsString();
                String capitalized = Util.capitalize(link);

                TextView txv = new TextView(this);
                txv.setText(capitalized);
                txv.setCompoundDrawablesWithIntrinsicBounds(getIcon(link), 0, 0, 0);
                txv.setCompoundDrawablePadding(5);
                txv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String url = getSocialURL(link, id);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));

                        startActivity(intent);
                    }
                });

                socialLinks.addView(txv);
            }

            JsonObject aliases = source.get("aliases").getAsJsonObject();
            HashSet<String> aliasesList = new HashSet<>();

            if (aliases.has("en") && !aliases.get("en").isJsonNull()) {

                for (JsonElement alias : aliases.get("en").getAsJsonArray()) {
                    aliasesList.add(alias.getAsString());
                }

            }

            if (aliases.has("ptbr") && !aliases.get("ptbr").isJsonNull()) {

                for (JsonElement alias : aliases.get("ptbr").getAsJsonArray()) {
                    aliasesList.add(alias.getAsString());
                }

            }

            akas.setText(String.format(getString(R.string.akas), Joiner.on(", ").skipNulls().join(aliasesList)));

        }


    }

    private String getSocialURL(String link, String id) {


        String urlFormat = "%s";

        switch (link) {

            case "linkedin":
                urlFormat = "https://www.linkedin.com/in/%s";
                break;

            case "youtube":
                urlFormat = "https://www.youtube.com/channel/%s";
                break;

            case "facebook":
                urlFormat = "https://fb.com/%s";
                break;

            case "twitter":
                urlFormat = "https://twitter.com/%s";
                break;

            case "instagram":
                urlFormat = "https://instagram.com/%s";
                break;

            case "imdb":
                urlFormat = "https://tranquil-depths-10042.herokuapp.com/%s";
                break;

            case "website":
                urlFormat = "%s";
                break;

        }

        return String.format(urlFormat, id);

    }

    private int getIcon(String link) {

        int ico = 0;

        switch (link) {

            case "linkedin":
                ico = R.drawable.linkedin;
                break;

            case "youtube":
                ico = R.drawable.youtube;
                break;

            case "facebook":
                ico = R.drawable.youtube;
                break;

            case "twitter":
                ico = R.drawable.youtube;
                break;

            case "instagram":
                ico = R.drawable.instagram;
                break;

            case "imdb":
                ico = R.drawable.film_font_awesome;
                break;

            case "website":
                ico = R.drawable.website;
                break;

        }

        return ico;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
