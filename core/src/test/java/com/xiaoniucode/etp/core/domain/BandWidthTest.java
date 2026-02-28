package com.xiaoniucode.etp.core.domain;

public class BandWidthTest {
    public static void main(String[] args) {
        BandwidthConfig bw1 = new BandwidthConfig("1M", "512K", "512K");
        System.out.println(bw1);
        System.out.println("有效入口: " + bw1.getEffectiveInLimit() + " bytes");
        System.out.println("有效出口: " + bw1.getEffectiveOutLimit() + " bytes");

        //只有总限制
        BandwidthConfig bw2 = new BandwidthConfig("2M", null, null);
        System.out.println(bw2);

        //只有入口限制
        BandwidthConfig bw3 = new BandwidthConfig(null, "1M", null);
        System.out.println(bw3);

        //不限速
        BandwidthConfig bw4 = new BandwidthConfig();
        System.out.println(bw4);

        //动态修改
        BandwidthConfig bw5 = new BandwidthConfig();
        bw5.setLimit("1M");
        bw5.setLimitIn("512K");
        System.out.println(bw5);
    }
}