package io.github.lxien.orbien.server.web.entity;

import io.github.lxien.orbien.server.web.entity.converter.ScheduledJobRunStatusConverter;
import io.github.lxien.orbien.server.web.enums.ScheduledJobRunStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 定时任务
 */
@Data
@Entity
@Table(name = "scheduled_job")
public class ScheduledJobDO {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 任务编码
     */
    @Column(name = "job_code", nullable = false, unique = true, length = 64)
    private String jobCode;
    /**
     * 任务名称
     */
    @Column(name = "job_name", nullable = false, length = 128)
    private String jobName;
    /**
     * 任务描述
     */
    @Column(length = 512)
    private String description;
    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    /**
     * Cron表达式
     */
    @Column(name = "cron_expression", nullable = false, length = 64)
    private String cronExpression;
    /**
     * 任务参数JSON
     */
    @Column(name = "params_json", columnDefinition = "TEXT")
    private String paramsJson;
    /**
     * 最近执行时间
     */
    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;
    /**
     * 最近执行状态
     */
    @Convert(converter = ScheduledJobRunStatusConverter.class)
    @Column(name = "last_run_status")
    private ScheduledJobRunStatus lastRunStatus = ScheduledJobRunStatus.NOT_RUN;
    /**
     * 最近执行结果
     */
    @Column(name = "last_run_message", length = 512)
    private String lastRunMessage;
    /**
     * 下次执行时间
     */
    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt;
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
