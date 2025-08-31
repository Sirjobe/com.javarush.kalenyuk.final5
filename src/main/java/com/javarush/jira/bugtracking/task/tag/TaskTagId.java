package com.javarush.jira.bugtracking.task.tag;

import java.io.Serializable;
import java.util.Objects;

public class TaskTagId implements Serializable {
    private Long taskId;
    private String tag;

    public TaskTagId() {
    }

    public TaskTagId(Long taskId, String tag) {
        this.taskId = taskId;
        this.tag = tag;
    }

    // Геттеры, сеттеры, equals и hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskTagId that = (TaskTagId) o;
        return Objects.equals(taskId, that.taskId) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, tag);
    }
}