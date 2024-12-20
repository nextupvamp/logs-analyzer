package ru.nextupvamp.data;

import lombok.Builder;
import ru.nextupvamp.io.formatters.TextFormatter;

import java.time.ZonedDateTime;
import java.util.regex.Pattern;

@Builder
public record HandledArgsData(
    PathsData paths,
    ZonedDateTime from,
    ZonedDateTime to,
    TextFormatter format,
    String filterField,
    Pattern filterValuePattern,
    String reportFileName
) {
}
