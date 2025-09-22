package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepositoryQuery {
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

    Page<TodoSearchResponse> findAllByTitleOrNicknameOrderByCreatedAtDesc(Pageable pageable,
                                                                          @Param("title") String title,
                                                                          @Param("nickname") String nickname,
                                                                          @Param("startTime") LocalDateTime startTime,
                                                                          @Param("endTime")LocalDateTime endTime);
}
