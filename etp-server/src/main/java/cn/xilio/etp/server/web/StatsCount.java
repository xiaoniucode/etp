package cn.xilio.etp.server.web;

public record StatsCount(
        Integer clientTotal,
        Integer onlineClient,
        Integer mappingTotal,
        Integer startMapping
) {
}
