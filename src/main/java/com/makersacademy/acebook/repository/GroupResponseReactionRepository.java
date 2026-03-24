package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.GroupResponseReaction;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface GroupResponseReactionRepository extends CrudRepository<GroupResponseReaction, Long> {

    // all reactions for one response
    List<GroupResponseReaction> findByGroupResponseId(Long groupResponseId);

    // check whether this user already reacted to this response
    Optional<GroupResponseReaction> findByGroupResponseIdAndUserId(Long groupResponseId, Long userId);

    // all reactions made by one user
    List<GroupResponseReaction> findByUserId(Long userId);
}