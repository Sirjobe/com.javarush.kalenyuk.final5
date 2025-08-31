package com.javarush.jira.bugtracking.task.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TaskTagRepository extends JpaRepository<TaskTag, TaskTagId> {
    List<TaskTag> findAllByTaskId(Long taskId);

    @Transactional
    void deleteByTaskIdAndTag(Long taskId, String tag);

    boolean existsByTaskIdAndTag(Long taskId, String tag);
}