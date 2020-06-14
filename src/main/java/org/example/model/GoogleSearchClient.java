package org.example.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@Scope(value= ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GoogleSearchClient implements SearchClient {

    private final String searchUrl;

    @Autowired
    public GoogleSearchClient(@Value("${search.client.url}") String url, @Value("${search.client.key}") String clientKey, @Value("${search.client.apiKey}") String apiKey) {
        this.searchUrl = url + "?key=" + clientKey + "&cx=" + apiKey;
    }

    //query without additional params
    public String getJsonSearchResult(String searchWord) {
        String query = searchUrl + "&q=" + searchWord;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(query))) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //google format
    public Set<String> extractSecondDomen(String json) {
        if (json != null && !json.isEmpty()) {
            Set<String> result = new HashSet<>();
            for (JsonElement jsonElement : JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("items")) {
                JsonElement linkElement = ((JsonObject) jsonElement).get("link");
                if (!linkElement.isJsonNull()) {
                    String link = linkElement.getAsString();
                    int ind = link.indexOf("//") + 2;
                    link = link.substring(ind, link.indexOf('/', ind));
                    result.add(link.startsWith("www.") ? link.substring(4) : link);
                }
            }
            return result;
        }
        return Collections.emptySet();
    }

}
