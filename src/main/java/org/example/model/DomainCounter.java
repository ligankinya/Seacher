package org.example.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DomainCounter {

    private final Logger logger = LoggerFactory.getLogger(DomainCounter.class);

    @Autowired
    private ApplicationContext context;

    public Map<String, Long> getWordOccurrencesMap(List<String> searchWords) {
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(searchWords.size(), 5));
        List<Future<Set<String>>> futureList = new ArrayList<>();
        for (String searchWord : searchWords) {
            Future<Set<String>> future = executor.submit(new SearchTask(searchWord.trim()));
            futureList.add(future);
        }
        Map<String, Long> map = collectToMap(futureList);
        executor.shutdown();
        return map;
    }

    private Map<String, Long> collectToMap(List<Future<Set<String>>> futureList) {
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
        public Set<String> call() {
            SearchClient searchClient = context.getBean(GoogleSearchClient.class);
            String json = searchClient.getJsonSearchResult(searchWord);
            return searchClient.extractSecondDomen(json);
        }
    }

}
