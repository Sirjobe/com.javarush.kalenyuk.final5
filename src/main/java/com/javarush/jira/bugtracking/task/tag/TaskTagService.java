package com.javarush.jira.bugtracking.task.tag;

import com.javarush.jira.bugtracking.task.TaskRepository;
import com.javarush.jira.common.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskTagService {
    private final TaskTagRepository taskTagRepository;
    private final TaskRepository taskRepository;

    public List<String> getTagsForTask(Long taskId) {
        checkTaskExists(taskId);
        return taskTagRepository.findAllByTaskId(taskId).stream()
                .map(TaskTag::getTag)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addTagToTask(Long taskId, String tag) {
        checkTaskExists(taskId);
        String validatedTag = validateAndTrimTag(tag);
        if (!taskTagRepository.existsByTaskIdAndTag(taskId, validatedTag)) {
            taskTagRepository.save(new TaskTag(taskId, validatedTag));
            log.info("Added tag '{}' to task {}", validatedTag, taskId);
        } else {
            log.info("Tag '{}' already exists for task {}", validatedTag, taskId);
        }
    }

    @Transactional
    public void removeTagFromTask(Long taskId, String tag) {
        checkTaskExists(taskId);
        String validatedTag = validateAndTrimTag(tag);
        taskTagRepository.deleteByTaskIdAndTag(taskId, validatedTag);
        log.info("Removed tag '{}' from task {}", validatedTag, taskId);
    }

    private void checkTaskExists(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException("Task with id=" + taskId + " not found");
        }
    }

    private String validateAndTrimTag(String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag must not be null");
        }
        String trimmed = tag.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Tag must not be empty");
        }
        if (trimmed.length() > 32) {
            throw new IllegalArgumentException("Tag must not be longer than 32 characters");
        }
        return trimmed;
    }
}