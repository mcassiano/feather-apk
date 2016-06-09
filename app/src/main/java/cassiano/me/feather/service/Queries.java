package cassiano.me.feather.service;

/**
 * Created by matheus on 6/7/16.
 */

public class Queries {

    public static String getAutocompleteQuery(String term) {

        String query = "{\n" +
                "    \"query\": {\n" +
                "        \"match\": {\n" +
                "            \"wikipedia_entry.title.autocomplete\": \"%s\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        return String.format(query, term);

    }

    public static String getSearchQuery(String term) {

        String query = "{\n" +
                "  \"aggs\": {\n" +
                "    \"catAggGnd\": {\n" +
                "      \"significant_terms\": {\n" +
                "        \"field\": \"wikipedia_entry.categories.raw\",\n" +
                "        \"size\": 3,\n" +
                "        \"gnd\": {\n" +
                "          \"background_is_superset\": false\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"catAggChi\": {\n" +
                "      \"significant_terms\": {\n" +
                "        \"field\": \"wikipedia_entry.categories.raw\",\n" +
                "        \"size\": 3,\n" +
                "        \"chi_square\": {\n" +
                "          \"background_is_superset\": false,\n" +
                "          \"include_negatives\": true\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"catAggMI\": {\n" +
                "      \"significant_terms\": {\n" +
                "        \"field\": \"wikipedia_entry.categories.raw\",\n" +
                "        \"size\": 3,\n" +
                "        \"mutual_information\": {\n" +
                "          \"background_is_superset\": false,\n" +
                "          \"include_negatives\": true\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"query\": {\n" +
                "    \"filtered\": {\n" +
                "      \"query\": {\n" +
                "        \"bool\": {\n" +
                "          \"should\": [{\n" +
                "            \"match\": {\n" +
                "              \"labels.ptbr\": {\n" +
                "                \"query\": \"%1$s\"\n" +
                "              }\n" +
                "            }\n" +
                "          }, {\n" +
                "            \"match\": {\n" +
                "              \"labels.en\": {\n" +
                "                \"query\": \"%1$s\"\n" +
                "              }\n" +
                "            }\n" +
                "          }, {\n" +
                "            \"match\": {\n" +
                "              \"aliases.en\": {\n" +
                "                \"query\": \"%1$s\"\n" +
                "              }\n" +
                "            }\n" +
                "          }, {\n" +
                "            \"match\": {\n" +
                "              \"aliases.ptbr\": {\n" +
                "                \"query\": \"%1$s\"\n" +
                "              }\n" +
                "            }\n" +
                "          }]\n" +
                "        }\n" +
                "      },\n" +
                "      \"filter\": {\n" +
                "        \"or\": {\n" +
                "          \"filters\": [{\n" +
                "            \"exists\": {\n" +
                "              \"field\": \"labels.ptbr\"\n" +
                "            }\n" +
                "          }, {\n" +
                "            \"exists\": {\n" +
                "              \"field\": \"wikipedia_links.ptwiki\"\n" +
                "            }\n" +
                "          }]\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        return String.format(query, term);
    }
}
