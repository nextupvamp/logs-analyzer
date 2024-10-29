package ru.nextupvamp.data;

import java.time.ZonedDateTime;

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
