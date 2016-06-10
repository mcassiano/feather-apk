package cassiano.me.feather.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cassiano.me.feather.R;
import cassiano.me.feather.service.ElasticSearchREST;
import cassiano.me.feather.service.Queries;
import cassiano.me.feather.service.handlers.ElasticSearchResponseHandler;
import cassiano.me.feather.ui.adapter.ResultAdapter;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private ElasticSearchREST client;

    @BindView(R.id.search_view)
    MaterialSearchView searchView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.nothing_found_state)
    RelativeLayout nothingFoundView;

    @BindView(R.id.error_state_view)
    RelativeLayout errorStateView;

    @BindView(R.id.search_results)
    ListView searchResultsView;

    @BindView(R.id.query_container)
    TextView queryContainer;

    @BindView(R.id.spinner)
    Spinner catAggType;

    @BindView(R.id.aggsResult)
    LinearLayout aggsResult;

    private boolean running;

    private MaterialDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setUpProgressBar();
        setupElasticSearchClient();
        setupSearchView();
    }

    private void setUpProgressBar() {

        progress = Util.getProgressBar(this, R.string.loading_results, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                queryContainer.setText(String.format(getString(R.string.query_s), getString(R.string.empty)));
                running = false;
                client.cancelRequests();
            }
        });
        queryContainer.setText(String.format(getString(R.string.query_s), getString(R.string.empty)));
    }

    private void setupSearchView() {
        running = false;

        nothingFoundView.setVisibility(View.VISIBLE);

        searchView.setVoiceSearch(false);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                queryContainer.setText(String.format(getString(R.string.query_s), query));
                progress.show();
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                fetchAutoComplete(newText);

                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                nothingFoundView.setVisibility(View.GONE);
            }

            @Override
            public void onSearchViewClosed() {

            }
        });
    }

    private void performSearch(String query) {

        client.searchDocuments(this, Queries.getSearchQuery(query), new ElasticSearchResponseHandler() {
            @Override
            public void onSuccess(JsonElement result) {

                progress.dismiss();
                errorStateView.setVisibility(View.INVISIBLE);

                int total = result
                        .getAsJsonObject()
                        .get("hits")
                        .getAsJsonObject()
                        .get("total")
                        .getAsInt();

                if (total <= 0) {
                    clearAdapter();
                    aggsResult.removeAllViews();
                    catAggType.setAdapter(null);
                    nothingFoundView.setVisibility(View.VISIBLE);
                    return;
                }

                Double maxScore = result.
                        getAsJsonObject()
                        .get("hits")
                        .getAsJsonObject()
                        .get("max_score").getAsDouble();

                JsonArray objs = result
                        .getAsJsonObject()
                        .get("hits")
                        .getAsJsonObject()
                        .get("hits")
                        .getAsJsonArray();

                final ResultAdapter adapter = new ResultAdapter(MainActivity.this, objs, maxScore);

                searchResultsView.setAdapter(adapter);

                searchResultsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(MainActivity.this, DocumentDetailActivity.class);
                        intent.putExtra("document", adapter.getItem(position).toString());

                        startActivity(intent);
                    }
                });

                JsonObject aggregations = result.getAsJsonObject().get("aggregations").getAsJsonObject();

                final String[] aggsTypesId = {"entity_descriptors", "catAggGnd", "catAggMI", "catAggChi"};
                final String[] aggsTypes = {"Entidade (TF-IDF)", "Categoria (GND)", "Categoria (MI)", "Categoria (Chi)"};
                final HashMap<String, JsonArray> aggBuckets = new HashMap<>();

                for (String aggType : aggsTypesId) {
                    JsonObject agg = aggregations.get(aggType).getAsJsonObject();
                    JsonArray buckets = agg.get("buckets").getAsJsonArray();
                    aggBuckets.put(aggType, buckets);
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, aggsTypes);

                catAggType.setAdapter(spinnerAdapter);
                catAggType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String type = aggsTypesId[position];
                        JsonArray buckets = aggBuckets.get(type);

                        aggsResult.removeAllViews();

                        for (int i = 0; i < buckets.size(); i++) {

                            String key = buckets.get(i).getAsJsonObject().get("key").getAsString();
                            Double score = buckets.get(i).getAsJsonObject().get("score").getAsDouble();

                            TextView tx = new TextView(MainActivity.this);
                            tx.setText(String.format(Locale.getDefault(), "%s (%.4f)", key, score));

                            aggsResult.addView(tx);

                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });


            }

            @Override
            public void onFailed() {
                nothingFoundView.setVisibility(View.INVISIBLE);
                errorStateView.setVisibility(View.VISIBLE);

                clearAdapter();

            }

            @Override
            public void onStart() {

            }
        });

    }

    private void clearAdapter() {
        if (searchResultsView.getAdapter() != null)
            ((ResultAdapter) searchResultsView.getAdapter()).clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchview_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
            if (searchResultsView.getAdapter().getCount() <= 0)
                nothingFoundView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    private void fetchAutoComplete(String term) {

        if (running) {
            running = false;
            client.cancelRequests();
        }

        client.searchAutocomplete(this, Queries.getAutocompleteQuery(term), new ElasticSearchResponseHandler() {
            @Override
            public void onSuccess(JsonElement result) {

                running = false;
                Gson gson = new Gson();

                JsonArray objs = result
                        .getAsJsonObject()
                        .get("hits")
                        .getAsJsonObject()
                        .get("hits")
                        .getAsJsonArray();

                List<JsonObject> tmp;
                tmp = gson.fromJson(objs, new TypeToken<List<JsonObject>>(){}.getType());

                ArrayList<String> titles = new ArrayList<>();

                for (JsonObject hit : tmp) {

                    String title = hit.get("_source")
                            .getAsJsonObject().get("wikipedia_entry")
                            .getAsJsonObject().get("title")
                            .getAsString();

                    titles.add(title);

                }

                String[] titlesArray = titles.toArray(new String[0]);

                searchView.setSuggestions(titlesArray);

            }

            @Override
            public void onFailed() {
                running = false;
                progress.dismiss();

            }

            @Override
            public void onStart() {

                running = true;
            }
        });


    }

    private void setupElasticSearchClient() {
        client = new ElasticSearchREST();
        client.setIndex("feather_v3");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


}
