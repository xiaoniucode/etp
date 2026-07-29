package io.github.lxien.orbien.server.web.entity;

import io.github.lxien.orbien.server.web.entity.converter.ScheduledJobRunStatusConverter;
import io.github.lxien.orbien.server.web.entity.converter.ScheduledJobTriggerTypeConverter;
import io.github.lxien.orbien.server.web.enums.ScheduledJobRunStatus;
import io.github.lxien.orbien.server.web.enums.ScheduledJobTriggerType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务执行日志
 */
@Data
@Entity
@Table(name = "scheduled_job_log", indexes = {
        @Index(name = "idx_scheduled_job_log_code", columnList = "job_code"),
        @Index(name = "idx_scheduled_job_log_started", columnList = "started_at")
})
public class ScheduledJobLogDO {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 任务编码
     */
    @Column(name = "job_code", nullable = false, length = 64)
    private String jobCode;
    /**
     * 触发类型
     */
    @Convert(converter = ScheduledJobTriggerTypeConverter.class)
    @Column(name = "trigger_type", nullable = false)
    private ScheduledJobTriggerType triggerType;
    /**
     * 开始时间
     */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    /**
     * 结束时间
     */
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    /**
     * 执行状态
     */
    @Convert(converter = ScheduledJobRunStatusConverter.class)
    @Column(nullable = false)
    private ScheduledJobRunStatus status;
    /**
     * 影响记录数
     */
    @Column(name = "affected_count")
    private Integer affectedCount;
    /**
     * 执行信息
     */
    @Column(columnDefinition = "TEXT")
    private String message;
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
