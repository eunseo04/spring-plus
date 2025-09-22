package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.example.expert.domain.todo.entity.QTodo.todo;

@RequiredArgsConstructor
public class TodoRepositoryQueryImpl implements TodoRepositoryQuery {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long id) {
//        "SELECT t FROM Todo t "
//        "LEFT JOIN t.user "
//        "WHERE t.id = :todoId"
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(todo.id.eq(id))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> findAllByTitleOrNicknameOrderByCreatedAtDesc(Pageable pageable,
                                                                   String title,
                                                                   String nickname,
                                                                   LocalDateTime startTime,
                                                                   LocalDateTime endTime){
        List<TodoSearchResponse> result = queryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        todo.managers.size(),
                        todo.comments.size()
                ))
                .from(todo)
                .where((todo.user.nickname.eq(nickname)
                        .or(todo.title.containsIgnoreCase(title)))
                        .and(todo.createdAt.between(startTime, endTime)))
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPQLQuery<Long> count = queryFactory
                .select(todo.count())
                .from(todo)
                .where((todo.user.nickname.eq(nickname)
                        .or(todo.title.containsIgnoreCase(title)))
                        .and(todo.createdAt.between(startTime, endTime)));

        return PageableExecutionUtils.getPage(result, pageable, count::fetchOne);
    }
}