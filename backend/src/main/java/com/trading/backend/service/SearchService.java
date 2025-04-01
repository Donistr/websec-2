package com.trading.backend.service;

import com.trading.backend.messages.SearchRequest;
import com.trading.backend.messages.SearchResponse;

public interface SearchService {

    SearchResponse search(SearchRequest request);

}
