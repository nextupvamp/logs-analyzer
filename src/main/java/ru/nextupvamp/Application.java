package ru.nextupvamp;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import ru.nextupvamp.data.ArgsData;
import ru.nextupvamp.handlers.ArgsHandler;
import ru.nextupvamp.handlers.log_handlers.LogsStatisticsGatherer;
import ru.nextupvamp.handlers.log_handlers.NginxLogsHandler;
import ru.nextupvamp.io.ReportCreator;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Properties;

@UtilityClass
public class Application {
    @SneakyThrows
    public static void main(String[] args) {
        ArgsHandler argsHandler = new ArgsHandler(args);
        ArgsData argsData = argsHandler.handle();

        LogsStatisticsGatherer statisticsGatherer = new LogsStatisticsGatherer(
                argsData.paths(),
                argsData.from(),
                argsData.to(),
                argsData.filterField(),
                argsData.filterValuePattern(),
                new NginxLogsHandler()
        );

        String fileFormat = argsData.format().getFileFormat();
        Properties properties = new Properties();
        properties.load(Application.class.getClassLoader().getResourceAsStream("application.property"));
        Path reportFile = Paths.get(
                properties.getProperty("report.directory.file.path") + "report" + Instant.now().toEpochMilli()
                        + fileFormat);

        PrintStream printStream = new PrintStream(Files.newOutputStream(reportFile), true, StandardCharsets.UTF_8);
        ReportCreator.createReport(statisticsGatherer.gatherStatistics(), argsData.format(), printStream);
    }
}