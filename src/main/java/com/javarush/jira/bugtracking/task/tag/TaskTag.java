package com.javarush.jira.bugtracking.task.tag;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_tag")
@IdClass(TaskTagId.class)
@Getter
@Setter
@NoArgsConstructor
public class TaskTag {
    @Id
    @Column(name = "task_id", nullable = false)
    @NotNull
    private Long taskId;

    @Id
    @Column(name = "tag", nullable = false, length = 32)
    @NotNull
    @Size(max = 32)
    private String tag;

    public TaskTag(Long taskId, String tag) {
        this.taskId = taskId;
        this.tag = tag;
    }
}