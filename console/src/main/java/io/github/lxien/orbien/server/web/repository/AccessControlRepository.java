package io.github.lxien.orbien.server.web.repository;
import io.github.lxien.orbien.server.web.entity.AccessControlDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessControlRepository extends JpaRepository<AccessControlDO, String> {
    void deleteByProxyIdIn(List<String> ids);
}
