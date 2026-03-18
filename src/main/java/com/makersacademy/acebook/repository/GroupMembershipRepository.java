package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
}
