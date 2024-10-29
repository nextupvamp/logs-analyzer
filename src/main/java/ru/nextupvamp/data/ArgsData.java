package ru.nextupvamp.data;

import ru.nextupvamp.io.formatters.TextFormatter;

import java.time.ZonedDateTime;
import java.util.regex.Pattern;

public record ArgsData(
        PathsData paths,
        ZonedDateTime from,
        ZonedDateTime to,
        TextFormatter format,
        String filterField,
        Pattern filterValuePattern
) {
}
