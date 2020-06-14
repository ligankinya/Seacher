package org.example.model;

import java.util.Set;

public interface SearchClient {

    String getJsonSearchResult(String searchWord);

    Set<String> extractSecondDomen(String json);

}
