package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.memberships gm LEFT JOIN FETCH gm.user WHERE g.id = :groupId")
    Group findByIdWithMembers(@Param("groupId") Long groupId);


}