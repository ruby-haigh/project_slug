package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
