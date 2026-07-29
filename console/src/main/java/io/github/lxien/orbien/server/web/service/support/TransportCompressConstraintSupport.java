package io.github.lxien.orbien.server.web.service.support;

import io.github.lxien.orbien.core.transport.compress.CompressionType;
import io.github.lxien.orbien.server.web.common.exception.BizException;
import io.github.lxien.orbien.server.web.dto.transport.TransportCompressConstraints;

import java.util.List;

public final class TransportCompressConstraintSupport {

    private static final List<String> ALLOWED_ALGORITHMS = List.of("snappy", "lz4", "zstd");
    private static final CompressionType DEFAULT_ALGORITHM = CompressionType.DEFAULT;

    private TransportCompressConstraintSupport() {
    }

    public static TransportCompressConstraints build() {
        TransportCompressConstraints constraints = new TransportCompressConstraints();
        constraints.setCompressEditable(true);
        constraints.setAlgorithmEditable(true);
        constraints.setAllowedAlgorithms(ALLOWED_ALGORITHMS);
        return constraints;
    }

    public static void validate(Boolean compress, String compressAlgorithm) {
        if (compress == null) {
            throw new BizException("compress 不能为空");
        }
        if (!compress) {
            return;
        }
        String normalized = normalizeAlgorithm(compressAlgorithm);
        if (!ALLOWED_ALGORITHMS.contains(normalized)) {
            throw new BizException("不支持的压缩算法，仅支持 snappy / lz4 / zstd");
        }
    }

    /**
     * 表单回显用的算法值：关闭压缩时仍返回默认算法
     */
    public static String resolveStoredAlgorithm(Boolean compress, CompressionType compressAlgorithm) {
        if (!Boolean.TRUE.equals(compress)) {
            return DEFAULT_ALGORITHM.toConfigValue();
        }
        if (compressAlgorithm == null || !compressAlgorithm.isCompressed()) {
            return DEFAULT_ALGORITHM.toConfigValue();
        }
        return compressAlgorithm.toConfigValue();
    }

    /**
     * 实际生效的算法值：关闭压缩时为 none
     */
    public static String resolveEffectiveAlgorithm(Boolean compress, CompressionType compressAlgorithm) {
        if (!Boolean.TRUE.equals(compress)) {
            return CompressionType.NONE.toConfigValue();
        }
        return resolveStoredAlgorithm(true, compressAlgorithm);
    }

    public static String normalizeAlgorithm(String compressAlgorithm) {
        if (compressAlgorithm == null || compressAlgorithm.isBlank()) {
            return DEFAULT_ALGORITHM.toConfigValue();
        }
        return compressAlgorithm.trim().toLowerCase();
    }

    public static CompressionType toStorageValue(Boolean compress, String compressAlgorithm) {
        if (!Boolean.TRUE.equals(compress)) {
            return CompressionType.NONE;
        }
        return CompressionType.of(normalizeAlgorithm(compressAlgorithm));
    }
}
