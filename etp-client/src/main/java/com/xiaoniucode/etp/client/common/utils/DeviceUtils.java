package com.xiaoniucode.etp.client.common.utils;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用于获取设备指纹，生成唯一客户端ID
 */
public class DeviceUtils {

    private static final SystemInfo SI = new SystemInfo();

    public static String generate16Id() {
        String id = generateDeviceFingerprint();
        return sha256ToNChars(id,16);
    }

    /**
     * 生成设备指纹
     */
    public static String generateDeviceFingerprint() {
        try {
            StringBuilder sb = new StringBuilder();

            // 1. 计算机系统序列号（主板/整机序列号，区分度最高）
            ComputerSystem cs = SI.getHardware().getComputerSystem();
            String serial = cs.getSerialNumber();
            if (isValid(serial)) {
                sb.append("cs:").append(serial.trim());
            }

            // 2. 处理器唯一标识（ProcessorID 通常很稳定）
            CentralProcessor proc = SI.getHardware().getProcessor();
            String procId = proc.getProcessorIdentifier().getProcessorID();
            if (isValid(procId)) {
                sb.append("|proc:").append(procId.trim());
            }

            // 3. 网络接口 MAC 地址（只取物理、非 loopback、非虚拟的）
            List<String> macs = SI.getHardware().getNetworkIFs(false)
                    .stream()
                    .filter(nif -> {
                        NetworkInterface jni = nif.queryNetworkInterface();
                        if (jni == null) {
                            return false;
                        }
                        try {
                            String mac = nif.getMacaddr();
                            return !jni.isLoopback()
                                    && !jni.isVirtual()
                                    && isValidMac(mac);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .map(NetworkIF::getMacaddr)
                    .filter(DeviceUtils::isValidMac)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            if (!macs.isEmpty()) {
                sb.append("|macs:").append(String.join(",", macs));
            }

            // 4. 操作系统信息
            OperatingSystem os = SI.getOperatingSystem();
            String osInfo = os.getManufacturer() + ":" + os.getFamily();
            if (isValid(osInfo)) {
                sb.append("|os:").append(osInfo);
            }
            String raw = sb.toString().trim();
            if (raw.isEmpty()) {
                return "unknown-device-" + System.nanoTime();
            }
            return sha256(raw);
        } catch (Exception e) {
            return "fp-error-" + e.getClass().getSimpleName();
        }
    }

    private static boolean isValid(String s) {
        return s != null && !s.trim().isEmpty()
                && !s.trim().equalsIgnoreCase("unknown")
                && !s.trim().contains("O.E.M")
                && !s.trim().contains("To be filled");
    }

    private static boolean isValidMac(String mac) {
        if (mac == null || mac.isEmpty()) {
            return false;
        }
        String cleaned = mac.replaceAll("[-:]", "").toLowerCase();
        return cleaned.length() == 12
                && !cleaned.equals("000000000000")
                && !cleaned.equals("ffffffffffff")
                && !cleaned.startsWith("0000");
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) hex.append('0');
                hex.append(s);
            }
            return hex.toString().toUpperCase();
        } catch (NoSuchAlgorithmException ignored) {
            return input.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        }
    }

    private static String sha256ToNChars(String input,int length) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) {
                    hex.append('0');
                }
                hex.append(s);
            }
            String full = hex.toString().toUpperCase();
            return full.substring(0, length);
        } catch (NoSuchAlgorithmException e) {
            return input.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        }
    }
}