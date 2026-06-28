package com.serialtracker.backend.service;

import com.serialtracker.backend.entity.MovieList;
import com.serialtracker.backend.entity.MovieListItem;

import java.util.List;

public interface MovieListService {

    MovieList createList(String ownerUsername, String name, String description, boolean isPublic);

    MovieList renameList(Long listId, String actingUsername, String newName, String newDescription);

    void deleteList(Long listId, String actingUsername);

    MovieListItem addShow(Long listId, String actingUsername, int showId);

    void removeShow(Long listId, String actingUsername, int showId);

    void reorderShows(Long listId, String actingUsername, List<Integer> orderedShowIds);

    List<MovieList> getListsOwnedBy(String ownerUsername);

    MovieList getList(Long listId);

    List<MovieListItem> getItems(Long listId);
}
