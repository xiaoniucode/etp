package io.github.lxien.orbien.server.web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件共享权限与限额
 */
@Data
@Entity
@Table(name = "file_share_limits")
@NoArgsConstructor
public class FileShareLimitsDO {
    /**
     * 代理ID
     */
    @Id
    @Column(name = "proxy_id")
    private String proxyId;
    /**
     * 共享根路径
     */
    @Column(name = "root_path")
    private String rootPath;
    /**
     * 最大上传大小（字节）
     */
    @Column(name = "max_upload_size")
    private Long maxUploadSize;
    /**
     * 是否允许上传
     */
    @Column(name = "allow_upload")
    private Boolean allowUpload;
    /**
     * 是否允许删除
     */
    @Column(name = "allow_delete")
    private Boolean allowDelete;
    /**
     * 是否允许创建目录
     */
    @Column(name = "allow_mkdir")
    private Boolean allowMkdir;
    /**
     * 是否允许移动
     */
    @Column(name = "allow_move")
    private Boolean allowMove;
    /**
     * 是否允许重命名
     */
    @Column(name = "allow_rename")
    private Boolean allowRename;

    public FileShareLimitsDO(String proxyId) {
        this.proxyId = proxyId;
    }
}
