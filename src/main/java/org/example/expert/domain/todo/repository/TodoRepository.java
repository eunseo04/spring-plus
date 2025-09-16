package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryQuery {

    @EntityGraph(attributePaths = "user")
    @Query(""" 
            SELECT t
            FROM Todo t
            WHERE (:weather IS NULL OR t.weather = :weather)
                       AND(:startTime IS NULL OR t.modifiedAt >= :startTime)
                       AND(:endTime IS NULL OR t.modifiedAt <= :endTime)
            ORDER BY t.modifiedAt DESC
           """)
    Page<Todo> findAllByWeatherOrderByModifiedAtDesc(Pageable pageable,
                                                     @Param("weather") String weather,
                                                     @Param("startTime")LocalDateTime startTime,
                                                     @Param("endTime")LocalDateTime endTime);
}
