package ru.nextupvamp.handlers;

import com.beust.jcommander.JCommander;
import lombok.SneakyThrows;
import ru.nextupvamp.data.Args;
import ru.nextupvamp.data.HandledArgsData;
import ru.nextupvamp.data.PathsData;
import ru.nextupvamp.io.formatters.MarkdownFormatter;
import ru.nextupvamp.io.formatters.TextFormats;
import ru.nextupvamp.io.formatters.TextFormatter;

import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgsHandler {
    private static final Set<Character> SPECIAL_SYMBOLS = new HashSet<>();
    private static final Pattern URI_PATTERN = Pattern.compile("^(http|https|ftp|file)://.*");

    static {
        SPECIAL_SYMBOLS.add('*');
        SPECIAL_SYMBOLS.add('?');
        SPECIAL_SYMBOLS.add('{');
        SPECIAL_SYMBOLS.add('[');
    }

    private final String[] args;

    public ArgsHandler(String[] args) {
        this.args = args;
    }

    @SuppressWarnings({"checkstyle:ModifiedControlVariable", "checkstyle:CyclomaticComplexity"})
    public HandledArgsData handle() {
        Args parsedArgs = new Args();
        JCommander.newBuilder()
            .addObject(parsedArgs)
            .build()
            .parse(args);

        PathsData handledPaths = handlePaths(parsedArgs.paths());
        ZonedDateTime handledFrom = returnNullOrResult(parsedArgs.from(), ZonedDateTime::parse);
        ZonedDateTime handledTo = returnNullOrResult(parsedArgs.to(), ZonedDateTime::parse);
        TextFormatter formatter = getFormatter(parsedArgs.format());
        String filterField = returnNullOrResult(parsedArgs.filterField(), it -> it);
        Pattern filterValuePattern = returnNullOrResult(parsedArgs.filterValue(), Pattern::compile);
        String reportFileName = returnNullOrResult(parsedArgs.reportFileName(), it -> it);

        if (handledPaths.paths().isEmpty() && handledPaths.uris().isEmpty()) {
            throw new IllegalArgumentException("Missing paths");
        }
        if (filterField != null && filterValuePattern == null) {
            throw new IllegalArgumentException("Excepted filter value");
        }
        if (filterField == null && filterValuePattern != null) {
            throw new IllegalArgumentException("Excepted filter field");
        }
        if (handledFrom != null && handledTo != null) {
            if (handledFrom.isAfter(handledTo)) {
                throw new IllegalArgumentException("From date is after To date");
            }
            if (handledTo.isBefore(handledFrom)) {
                throw new IllegalArgumentException("To date is before From date");

            }
        }

        return HandledArgsData.builder()
            .paths(handledPaths)
            .from(handledFrom)
            .to(handledTo)
            .format(formatter)
            .filterField(filterField)
            .filterValuePattern(filterValuePattern)
            .reportFileName(reportFileName)
            .build();
    }

    private PathsData handlePaths(List<String> parsedPaths) {
        List<URI> uris = new ArrayList<>();
        List<Path> paths = new ArrayList<>();

        for (var currentPath : parsedPaths) {
            Matcher matcher = URI_PATTERN.matcher(currentPath);
            if (matcher.matches()) {
                uris.add(URI.create(currentPath));
            } else {
                // if path doesn't contain any glob symbols
                // that mean we deal with a single file
                if (findFirstGlobSymbolIndex(currentPath) == -1) {
                    paths.add(Paths.get(currentPath));
                } else {
                    findFilePathsByGlobPattern(currentPath, paths);
                }
            }
        }
        return new PathsData(uris, paths);
    }

    @SneakyThrows
    private void findFilePathsByGlobPattern(String currentPath, List<Path> paths) {
        // find the root dir that will be the root
        // of file tree that we'll walk through
        Path rootDir = extractRootDir(currentPath);
        // extract the glob pattern of filepath that we
        // are searching to
        String pattern = extractPattern(currentPath);
        // now walk through our file tree, visit all the
        // files on the way and check if they match
        // the pattern
        Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"
                    + pattern);
                Path relativePath = rootDir.relativize(file);
                if (matcher.matches(relativePath)) {
                    paths.add(file.toAbsolutePath());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private TextFormatter getFormatter(String formatString) {
        if (formatString == null) {
            return new MarkdownFormatter();
        }

        TextFormats[] availableTextFormats = TextFormats.values();
        for (TextFormats currentTextFormats : availableTextFormats) {
            if (currentTextFormats.format().equalsIgnoreCase(formatString)) {
                return currentTextFormats.formatter();
            }
        }

        throw new IllegalArgumentException("Unknown format: " + formatString);
    }

    private <T, R> R returnNullOrResult(T object, Function<T, R> function) {
        if (object == null) {
            return null;
        }

        return function.apply(object);
    }

    private Path extractRootDir(String pathString) {
        int specialSymbolIndex = findFirstGlobSymbolIndex(pathString);

        if (specialSymbolIndex == -1) {
            return Paths.get(pathString);
        }

        int separatorBeforeSpecialSymbolIndex =
            getFirstSeparatorBeforeSpecialSymbolIndex(pathString, specialSymbolIndex);

        if (separatorBeforeSpecialSymbolIndex == -1) {
            return Paths.get(pathString);
        }

        String editedPathString = pathString.substring(0, separatorBeforeSpecialSymbolIndex);
        Path currentDirectory = Paths.get(".").toAbsolutePath();

        return Paths.get(currentDirectory.toString(), editedPathString).normalize();
    }

    private int getFirstSeparatorBeforeSpecialSymbolIndex(String pathString, int specialSymbolIndex) {
        int separatorBeforeSpecialSymbolIndex = -1;
        String separator = FileSystems.getDefault().getSeparator();
        for (int i = specialSymbolIndex; i >= 0; --i) {
            if (pathString.startsWith(separator, i)) {
                separatorBeforeSpecialSymbolIndex = i;
                break;
            }
        }
        return separatorBeforeSpecialSymbolIndex;
    }

    private String extractPattern(String pathString) {
        int specialSymbolIndex = findFirstGlobSymbolIndex(pathString);

        if (specialSymbolIndex == -1) {
            return pathString;
        }

        int separatorBeforeSpecialSymbolIndex =
            getFirstSeparatorBeforeSpecialSymbolIndex(pathString, specialSymbolIndex);

        if (separatorBeforeSpecialSymbolIndex == -1) {
            return pathString;
        }

        return pathString.substring(separatorBeforeSpecialSymbolIndex + 1);
    }

    private int findFirstGlobSymbolIndex(String pathString) {
        int specialSymbolIndex = -1;
        for (int i = 0; i != pathString.length(); ++i) {
            char ch = pathString.charAt(i);
            if (SPECIAL_SYMBOLS.contains(ch)) {
                specialSymbolIndex = i;
                break;
            }
        }

        return specialSymbolIndex;
    }
}
