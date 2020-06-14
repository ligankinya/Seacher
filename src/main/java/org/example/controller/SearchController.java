package org.example.controller;

import org.example.model.DomainCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    private final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private ApplicationContext context;

    @RequestMapping(value = "/search", params = {"query"}, method = RequestMethod.GET)
    public ResponseEntity<?> search(@RequestParam MultiValueMap<String, String> params) {

        List<String> searchWords = params.get("query");
        if (searchWords == null || searchWords.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parameter 'query' is not set");
        }

        Map<String, Long> occurrencesMap = context.getBean(DomainCounter.class).getWordOccurrencesMap(searchWords);
        return ResponseEntity.ok(occurrencesMap);
    }
}
