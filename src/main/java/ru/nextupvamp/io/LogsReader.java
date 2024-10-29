package ru.nextupvamp.io;

import lombok.SneakyThrows;
import ru.nextupvamp.data.LogData;
import ru.nextupvamp.handlers.log_handlers.LogsHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class LogsReader implements AutoCloseable {
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @SneakyThrows
    public Stream<LogData> readFromFileAsStream(Path file, LogsHandler handler) {
        inputStream = Files.newInputStream(file);
        initReaders(inputStream);

        return bufferedReader.lines().map(handler::parseLogLineData);
    }

    @SneakyThrows
    public Stream<LogData> readFromUriAsStream(URI uri, LogsHandler handler) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .build();
        inputStream = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
        initReaders(inputStream);

        return bufferedReader.lines().map(handler::parseLogLineData);
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
        inputStreamReader.close();
        bufferedReader.close();
        httpClient.close();
    }

    private void initReaders(InputStream inputStream) {
        inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        bufferedReader = new BufferedReader(inputStreamReader);
    }
}