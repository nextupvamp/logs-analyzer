package ru.nextupvamp.io;

import org.junit.jupiter.api.Test;
import ru.nextupvamp.handlers.ArgsHandler;
import ru.nextupvamp.data.HandledArgsData;
import ru.nextupvamp.data.statistics.LogsStatistics;
import ru.nextupvamp.handlers.loghandlers.NginxLogsHandler;
import ru.nextupvamp.handlers.loghandlers.NginxLogsStatisticsGatherer;

import java.io.*;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportCreatorTest {
    public final OutputStream BAOS = new ByteArrayOutputStream();
    public final PrintStream BAOSPrintStream = new PrintStream(BAOS);

    @Test
    public void testADocReportCreate() {
        String[] args = {
            "--path",
            LogsReaderTest.TEST_DIR_PATH + LogsReaderTest.SEPARATOR + "logs1.txt",
            "--format",
            "adoc"
        };

        ArgsHandler argsHandler = new ArgsHandler(args);
        HandledArgsData handledArgsData = argsHandler.handle();
        NginxLogsStatisticsGatherer nginxLogsStatisticsGatherer = NginxLogsStatisticsGatherer.builder()
            .paths(handledArgsData.paths())
            .from(handledArgsData.from())
            .to(handledArgsData.to())
            .filterField(handledArgsData.filterField())
            .filterValuePattern(handledArgsData.filterValuePattern())
            .logsHandler(new NginxLogsHandler())
            .build();
        LogsStatistics ls = nginxLogsStatisticsGatherer.gatherStatistics();

        new ReportCreator(BAOSPrintStream, handledArgsData.format(), ls).createReport();

        String report = BAOS.toString();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull(
                    ReportCreatorTest.class.getClassLoader().getResourceAsStream("report_example.adoc"))
            )
        );

        String expected = br.lines().collect(Collectors.joining(System.lineSeparator()));

        assertEquals(expected, report);
    }

    @Test
    public void testMarkdownReportCreate() {
        String[] args = {
            "--path",
            LogsReaderTest.TEST_DIR_PATH + LogsReaderTest.SEPARATOR + "logs1.txt"
        };

        ArgsHandler argsHandler = new ArgsHandler(args);
        HandledArgsData handledArgsData = argsHandler.handle();
        NginxLogsStatisticsGatherer nginxLogsStatisticsGatherer = NginxLogsStatisticsGatherer.builder()
            .paths(handledArgsData.paths())
            .from(handledArgsData.from())
            .to(handledArgsData.to())
            .filterField(handledArgsData.filterField())
            .filterValuePattern(handledArgsData.filterValuePattern())
            .logsHandler(new NginxLogsHandler())
            .build();
        LogsStatistics ls = nginxLogsStatisticsGatherer.gatherStatistics();

        new ReportCreator(BAOSPrintStream, handledArgsData.format(), ls).createReport();

        String report = BAOS.toString();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull(
                    ReportCreatorTest.class.getClassLoader().getResourceAsStream("report_example.md"))
            )
        );

        String expected = br.lines().collect(Collectors.joining(System.lineSeparator()));
        assertEquals(expected, report);
    }
}
