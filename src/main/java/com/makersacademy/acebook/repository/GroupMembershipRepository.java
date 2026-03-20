package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupMembership;
import com.makersacademy.acebook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    Optional<GroupMembership> findByUserAndGroup(User user, Group group);
    List<GroupMembership> findByGroup(Group group);

    @Query("SELECT gm.group FROM GroupMembership gm WHERE gm.user = :user")
    List<Group> findGroupsByUser(@Param("user") User user);
}
