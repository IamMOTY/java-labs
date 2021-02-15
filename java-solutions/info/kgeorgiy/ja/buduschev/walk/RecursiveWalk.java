package info.kgeorgiy.ja.buduschev.walk;


import info.kgeorgiy.ja.buduschev.utils.HashFunctions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class RecursiveWalk {
    public static void main(String[] args) {
        if (args.length < 2) return;
        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(Path.of(args[1]), StandardCharsets.UTF_8)) {
            for (String path : reader.lines().collect(Collectors.toList())) {
                Files.walkFileTree(Path.of(path), new RecursiveWalk.PrintHash(writer));
            }
        } catch (InvalidPathException e) {
            System.err.printf("Invalid path - %s", e.getMessage());
        } catch (AccessDeniedException e) {
            System.err.printf("Access denied - %s", e.getMessage());
        } catch (NoSuchFileException e) {
            System.err.printf("No such file - %s", e.getMessage());
        } catch (FileNotFoundException e) {
            System.err.printf("File not found - %s", e.getMessage());
        } catch (FileSystemException e) {
            System.err.printf("Filesystem error - %s", e.getMessage());
        } catch (IOException e) {
            System.err.printf("IOException - %s",e);
        }

    }

    private static class PrintHash extends SimpleFileVisitor<Path> {
        private final BufferedWriter output;

        public PrintHash(BufferedWriter output) {
            this.output = output;
        }

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attr) {
            long hash = 0;
            try (InputStream reader =
                         Files.newInputStream(file)) {
                hash = HashFunctions.PJW(reader);
            } catch (IOException ignored) {
                hash = 0;
            } finally {
                try {
                    output.write(String.format("%016x %s%n", hash, file));
                } catch (IOException e) {
                    System.err.printf("Can't write to file -%s%n", e.getMessage());
                    return TERMINATE;
                }
            }

            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            output.write(String.format("%016x %s%n", 0, file));
            return super.visitFileFailed(file, exc);
        }
    }
}
