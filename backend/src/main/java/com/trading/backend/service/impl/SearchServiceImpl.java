package com.trading.backend.service.impl;

import com.trading.backend.messages.SearchElement;
import com.trading.backend.messages.SearchExternalApiRequest;
import com.trading.backend.messages.SearchExternalApiResponse;
import com.trading.backend.messages.SearchRequest;
import com.trading.backend.messages.SearchResponse;
import com.trading.backend.model.ScheduleType;
import com.trading.backend.service.SearchService;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final String CSRF_TOKEN_URL = "https://ssau.ru/rasp";

    private static final String CSRF_TOKEN_HEADER_NAME = "X-CSRF-TOKEN";

    private static final String CSRF_TOKEN_SELECT_HTML = "meta[name=csrf-token]";

    private static final String CSRF_TOKEN_ATTRIBUTE = "content";

    private static final String URL = "https://ssau.ru/rasp/search";

    private static final String GROUP_ELEMENT_TYPE = "group";

    private static final String TEACHER_ELEMENT_TYPE = "staff";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SearchResponse search(SearchRequest request) {
        List<SearchExternalApiResponse> response = restTemplate.exchange(
                URL,
                HttpMethod.POST,
                new HttpEntity<>(
                        new SearchExternalApiRequest(request.getText()),
                        createHeaders()
                ),
                new ParameterizedTypeReference<List<SearchExternalApiResponse>>() {}
        ).getBody();

        SearchResponse result = new SearchResponse();
        for (SearchExternalApiResponse element : response) {
            String elementUrl = element.getUrl();
            int startIndex = elementUrl.indexOf('?') + 1;
            int endIndex = elementUrl.indexOf('I');

            String elementType = elementUrl.substring(startIndex, endIndex);

            Long id = Long.valueOf(elementUrl.substring(elementUrl.indexOf('=') + 1));
            ScheduleType type;
            if (elementType.equals(GROUP_ELEMENT_TYPE)) {
                type = ScheduleType.GROUP;
            } else if (elementType.equals(TEACHER_ELEMENT_TYPE)) {
                type = ScheduleType.TEACHER;
            } else {
                throw new RuntimeException("неизвестный тип элемента");
            }

            result.getElements().add(new SearchElement(element.getText(), type, id));
        }

        return result;
    }

    private HttpHeaders createHeaders() {
        ResponseEntity<String> response = restTemplate.getForEntity(CSRF_TOKEN_URL, String.class);

        HttpHeaders headers = new HttpHeaders();
        response.getHeaders().get(HttpHeaders.SET_COOKIE).forEach(cookie -> headers.add(HttpHeaders.COOKIE, cookie));

        Document doc = Jsoup.parse(response.getBody());
        String token = doc.select(CSRF_TOKEN_SELECT_HTML).attr(CSRF_TOKEN_ATTRIBUTE);
        headers.add(CSRF_TOKEN_HEADER_NAME, token);

        return headers;
    }

}
