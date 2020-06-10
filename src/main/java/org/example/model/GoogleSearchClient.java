package org.example.model;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class GoogleSearchClient {

    private final String searchUrl;

    public GoogleSearchClient(String clientKey, String apiKey) {
        this.searchUrl = "https://www.googleapis.com/customsearch/v1" + "?key=" + clientKey + "&cx=" + apiKey;
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

}
