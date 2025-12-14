package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.SearchResponse;

public interface SearchService {

    SearchResponse search(String query, int size);
}
