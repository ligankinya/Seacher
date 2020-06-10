package org.example.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.model.GoogleSearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class SearchController {

    private final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @RequestMapping(value = "/search", params = {"query"}, method = RequestMethod.GET)
    public ResponseEntity<?> search(@RequestParam MultiValueMap<String, String> params) {

        List<String> searchWords = params.get("query");
        if (searchWords == null || searchWords.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parameter 'query' is not set");
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(searchWords.size(), 5));
        List<Future<Set<String>>> futureList = new ArrayList<>();
        for (String searchWord : searchWords) {
            Future<Set<String>> future = executor.submit(new SearchTask(searchWord.trim()));
            futureList.add(future);
        }

        Map<String, Long> map = getWordOccurrencesMap(futureList);
        executor.shutdown();

        return ResponseEntity.ok(map);
    }

    private Map<String, Long> getWordOccurrencesMap(List<Future<Set<String>>> futureList) {
        List<String> allDomens = new ArrayList<>();
        for (Future<Set<String>> fut : futureList) {
            try {
                Set<String> res = fut.get(5, TimeUnit.SECONDS);
                allDomens.addAll(res);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Search error");
            }
        }
        return allDomens.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }


    class SearchTask implements Callable<Set<String>> {

        private final String searchWord;

        public SearchTask(String searchWord) {
            this.searchWord = searchWord;
        }

        @Override
        public Set<String> call() throws Exception {
            GoogleSearchClient searchClient = new GoogleSearchClient("AIzaSyC2yPqJZ5N97D3V3ivrTzHYvNOndf0c5cU", "002612626294721388421:losrhiqpkqu");
            String json = searchClient.getJsonSearchResult(searchWord);
            return extractSecondDomen(json);
        }

        //google format
        private Set<String> extractSecondDomen(String json) {
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
}
