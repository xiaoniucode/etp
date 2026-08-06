package io.github.lxien.orbien.server.web.service;

import io.github.lxien.orbien.server.web.common.message.PageQuery;
import io.github.lxien.orbien.server.web.common.message.PageResult;
import io.github.lxien.orbien.server.web.dto.scheduled.ScheduledJobDTO;
import io.github.lxien.orbien.server.web.dto.scheduled.ScheduledJobLogDTO;
import io.github.lxien.orbien.server.web.param.scheduled.ScheduledJobUpdateParam;

import java.util.List;

public interface ScheduledJobService {

    List<ScheduledJobDTO> listAll();

    ScheduledJobDTO getByCode(String jobCode);

    ScheduledJobDTO update(String jobCode, ScheduledJobUpdateParam param);

    ScheduledJobDTO updateEnabled(String jobCode, boolean enabled);

    void runNow(String jobCode);

    void executeJob(String jobCode, boolean manual);

    PageResult<ScheduledJobLogDTO> findLogs(String jobCode, PageQuery pageQuery);

    void deleteLogs(String jobCode, List<Long> ids);

    void seedJobs();
}
