package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.FeedResponse;

public interface FeedService {

    FeedResponse getFeed(String cursor, int size);
}
