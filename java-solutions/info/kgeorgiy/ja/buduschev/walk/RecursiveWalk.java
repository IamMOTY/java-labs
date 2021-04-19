package info.kgeorgiy.ja.buduschev.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitResult.CONTINUE;

public class RecursiveWalk {
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Invalid count of arguments");
        } else
            try {
                final Path inputFile = Path.of(args[0]);
                final Path outputFile = Path.of(args[1]);
                if (outputFile.getParent() != null) {
                    Files.createDirectories(outputFile.getParent());
                }
                Files.createFile(outputFile);

                if (Files.isSameFile(inputFile, outputFile)) {
                    System.err.println("Arguments is the same files.");
                } else
                    try (final BufferedReader reader = Files.newBufferedReader(inputFile);
                         final BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
                        for (String path : reader.lines().collect(Collectors.toList())) {
                            try {
                                Files.walkFileTree(Path.of(path), new PrintHash(writer));
                            } catch (final IOException | InvalidPathException e) {
                                printHash(writer, 0, path);
                            }
                        }
                    }

            } catch (final InvalidPathException e) {
                System.err.printf("Invalid path in arguments - %s%n", e.getMessage());
            } catch (final AccessDeniedException e) {
                System.err.printf("Access denied - %s%n", e.getMessage());
            } catch (final NoSuchFileException e) {
                System.err.printf("No such file - %s%n", e.getMessage());
            } catch (final FileNotFoundException e) {
                System.err.printf("File not found - %s%n", e.getMessage());
            } catch (final FileSystemException e) {
                System.err.printf("Filesystem error - %s%n", e.getMessage());
            } catch (final SecurityException e) {
                System.err.printf("Security error - %s%n", e.getMessage());
            } catch (final IOException e) {
                System.err.printf("IOException - %s%n", e);
            }

    }

    private static void printHash(Writer writer, long hash, String path) throws IOException {
        writer.write(String.format("%016x %s%n", hash, path));
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
                    printHash(output, hash, file.toString());
                } catch (IOException e) {
                    System.err.printf("Can't write to file -%s%n", e.getMessage());
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return super.visitFileFailed(file, exc);
        }
    }

    public static class HashFunctions {
        final static int BUFFER_SIZE = 4096;


        public static long PJW(InputStream input) throws IOException {
            long hash = 0;
            long high;
            byte[] buff = new byte[BUFFER_SIZE];
            int size;
            while ((size = input.read(buff)) != -1)
                for (int i = 0; i < size; i++) {
                    hash = (hash << 8) + (buff[i] & 0xFF);
                    if ((high = (hash & 0xFF00_0000_0000_0000L)) != 0) {
                        hash ^= high >> 48;
                        hash &= ~high;
                    }
                }
            return hash;
        }


    }

}
