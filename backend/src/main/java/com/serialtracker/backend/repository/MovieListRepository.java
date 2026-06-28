package com.serialtracker.backend.repository;

import com.serialtracker.backend.entity.MovieList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieListRepository extends JpaRepository<MovieList, Long> {

    // All lists belonging to one user (used for "my lists").
    List<MovieList> findByOwnerId(Long ownerId);

    // All lists belonging to one user that are public (used when viewing
    // someone else's profile — we only ever show their public lists).
    List<MovieList> findByOwnerIdAndIsPublicTrue(Long ownerId);
}
