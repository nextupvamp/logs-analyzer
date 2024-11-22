package ru.nextupvamp.data;

import lombok.Builder;

import java.time.ZonedDateTime;

@SuppressWarnings("checkstyle:RecordComponentNumber")
@Builder
public record LogData(
    String remoteAddress,
    String remoteUser,
    ZonedDateTime timeLocal,
    String requestMethod,
    String requestResource,
    String requestHttpVersion,
    short status,
    long bytesSent,
    String httpReferer,
    String httpUserAgent
) {
}
