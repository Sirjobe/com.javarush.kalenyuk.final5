package com.javarush.jira.bugtracking.task.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks/{taskId}/tags")
public class TaskTagController {
    private final TaskTagService taskTagService;

    @GetMapping
    public List<String> getTags(@PathVariable Long taskId) {
        return taskTagService.getTagsForTask(taskId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addTag(@PathVariable Long taskId, @RequestBody String tag) {
        taskTagService.addTagToTask(taskId, tag);
    }

    @DeleteMapping("/{tag}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable Long taskId, @PathVariable String tag) {
        taskTagService.removeTagFromTask(taskId, tag);
    }
}