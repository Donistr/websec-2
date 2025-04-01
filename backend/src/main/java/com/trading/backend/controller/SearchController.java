package com.trading.backend.controller;

import com.trading.backend.messages.SearchRequest;
import com.trading.backend.messages.SearchResponse;
import com.trading.backend.service.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule/search")
@AllArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public SearchResponse search(@RequestBody SearchRequest request) {
        return searchService.search(request);
    }

}
