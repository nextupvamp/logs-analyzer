package ru.nextupvamp.handlers.log_handlers;

import ru.nextupvamp.data.LogData;

public interface LogsHandler {
    LogData parseLogLineData(String line);
}
