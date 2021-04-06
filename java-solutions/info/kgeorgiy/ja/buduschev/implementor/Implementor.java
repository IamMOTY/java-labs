package info.kgeorgiy.ja.buduschev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;


/**
 * Provides implementation of given class token, and write result as {@code .java} file or compile {@code .jar} file
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Create parent directories of path if they absent
     *
     * @param path required path
     * @throws IOException if an I/O error occurs
     */
    protected static void resolvePath(final Path path) throws IOException {
        final Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    /**
     * Create parent directories which required to write implementation of class token
     *
     * @param token class token of target
     * @param root  path of directory for root
     * @return full path for class
     * @throws ImplerException if an I/O error occurs
     */
    private static Path resolveSource(final Class<?> token, Path root) throws ImplerException {
        Path path = getPath(token, root);
        try {
            resolvePath(path);
        } catch (IOException e) {
            throw new ImplerException("Can't create source file " + e.getLocalizedMessage());
        }
        return path;
    }

    /**
     * Return path to target class, regarding given {@code root} path, package directories added if required
     *
     * @param token class token of target
     * @param root  path of directory for root
     * @return full path for class
     */
    private static Path getPath(Class<?> token, Path root) {
        return root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(String.join("", CodeGenerator.getSimpleName(token), ".java"));
    }

    /**
     * Validate can be {@code token} implemented
     *
     * @param token class token for validation
     * @return true, if can be implemented, false otherwise
     */
    private static boolean validateToken(final Class<?> token) {
        final int modifiers = token.getModifiers();
        return !token.isPrimitive() &&
                !token.isArray() &&
                !Modifier.isFinal(modifiers) &&
                !Modifier.isPrivate(modifiers) &&
                token != Enum.class;
    }

    /**
     * Converts given string to {@code UTF-32}
     *
     * @param s string to convert
     * @return converted string
     */
    private static String toUTF32(final String s) {
        return s.chars().mapToObj(c -> String.format("\\u%04X", c)).collect(Collectors.joining());
    }


    /**
     * Produces code implementing class or interface specified by provided {@code token}.
     * <p>
     * Generated class classes name should be same as classes name of the type token with {@code Impl} suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * {@code root} directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to {@code $root/java/util/ListImpl.java}
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     *                                                                 generated.
     */
    public static void createImplementation(Class<?> token, Path root) throws ImplerException {
        Path sourcePath = resolveSource(token, root);
        if (!validateToken(token)) {
            throw new ImplerException("Invalid token");
        }
        try (final BufferedWriter out = Files.newBufferedWriter(sourcePath)) {
            out.write(toUTF32(CodeGenerator.genImplementation(token)));
            // :NOTE: message/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        createImplementation(token, root);
    }

    /**
     * Returns path of {@code token} as {@link String}
     *
     * @param token class witch path required
     * @return {@code token} path as {@link String}
     * @throws ImplerException if any URI errors occurs
     */
    private static String getClassPath(Class<?> token) throws ImplerException {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new ImplerException(e.getLocalizedMessage());
        }
    }

    /**
     * Produces <var>.jar</var> file implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class classes name should be same as classes name of the type token with <var>Impl</var> suffix
     * added.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    public static void createJarImplementation(final Class<?> token, final Path jarFile) throws ImplerException {
        final Path tempDir;
        try {
            resolvePath(jarFile);
            tempDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "_");
        } catch (final IOException e) {
            throw new ImplerException(e.getLocalizedMessage());
        }

        try {
            createImplementation(token, tempDir);
            final String file = getPath(token, tempDir).toString();
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new ImplerException("Could not find java compiler");
            }
            final String[] args = {file, "-cp", String.join(File.pathSeparator, tempDir.toString(), getClassPath(token))};
            final int exitCode = compiler.run(null, null, null, args);
            if (exitCode != 0) {
                throw new ImplerException("An error occurred while compiling implementation");
            }
            final Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            // :NOTE: utf8
            try (final JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
                final String name = String.join(
                        "",
                        token.getPackageName().replace('.', '/'),
                        "/",
                        CodeGenerator.getSimpleName(token),
                        ".class"
                );
                jarOutputStream.putNextEntry(new ZipEntry(name));
                Files.copy(Paths.get(tempDir.toString(), name), jarOutputStream);
            } catch (final IOException e) {
                throw new ImplerException(e.getLocalizedMessage());
            }
        } finally {
            try {
                Files.walkFileTree(tempDir, new CleanerFileVisitor());
            } catch (final IOException e) {
                System.err.println("Error while deleting temp directory");
            }
        }
    }

    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        createJarImplementation(token, jarFile);
    }

    /**
     * Implementation for console application.
     * Pass {@code <class-name> <path-to-save>}.
     * Pass {@code -jar <class-name> <path-to-save>}.
     *
     * @param args console arguments
     */
    public static void main(final String[] args) {
        if (args == null) {
            System.err.println("Arguments is null");
            return;
        }

        if (args.length != 2 && args.length != 3) {
            System.err.println("Invalid count of arguments");
        }

        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Arguments contains null value");
            return;
        }

        if (args.length == 3 && !args[0].equals("-jar")) {
            System.err.println("Invalid parameters count must be -jar <class-name>  <jar-path>");
            return;
        }

        try {
            if (args.length == 2) {
                createImplementation(Class.forName(args[0]), Path.of(args[1]));
            } else {
                createJarImplementation(Class.forName(args[1]), Path.of(args[2]));
            }
        } catch (final ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        } catch (final InvalidPathException e) {
            System.err.println("Error while creating path: " + e.getMessage());
        } catch (final ImplerException e) {
            System.err.println("Error while creating implementation: " + e.getMessage());
        }
    }


    /**
     * File visitor, witch deletes whole directory.
     */
    private static class CleanerFileVisitor extends SimpleFileVisitor<Path> {
        /**
         * Delete visited files.
         *
         * @param file  a reference to the file
         * @param attrs the file's basic attributes
         * @return continue key
         * @throws IOException if an I/O error occurs
         */
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Delete directory after entries in the directory have been deleted.
         *
         * @param dir a reference to the directory
         * @param exc null if the iteration of the directory completes without an error; otherwise the I/O exception that caused the iteration of the directory to complete prematurely
         * @return continue key
         * @throws IOException - if an I/O error occurs
         */
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Class provide methods for generation java code of implementation given class token
     */
    public static class CodeGenerator {
        /**
         * {@link String} constant for empty string
         */
        private static final String EMPTY = "";
        /**
         * {@link String} constant for space
         */
        private static final String SPACE = " ";
        /**
         * {@link String} constant for semicolon
         */
        private static final String SEMICOLON = ";";
        /**
         * {@link String} constant for comma
         */
        private static final String COMMA = ", ";
        /**
         * {@link String} constant for empty class
         */
        private static final String CLASS = "class";
        /**
         * {@link String} constant for implements
         */
        private static final String IMPLEMENTS = "implements";
        /**
         * {@link String} constant for throws
         */
        private static final String THROWS = "throws";
        /**
         * {@link String} constant for extends
         */
        private static final String EXTENDS = "extends";
        /**
         * {@link String} constant for return
         */
        private static final String RETURN = "return";
        /**
         * {@link String} constant for package
         */
        private static final String PACKAGE = "package";
        /**
         * {@link String} constant for public
         */
        private static final String PUBLIC = "public";
        /**
         * {@link String} constant for super
         */
        private static final String SUPER = "super";
        /**
         * {@link String} constant for {
         */
        private static final String START_BLOCK = "{";
        /**
         * {@link String} constant for }
         */
        private static final String END_BLOCK = "}";

        /**
         * Returns the simple name of implementation of the underlying class as given in the source code.
         *
         * @param token the underlying class
         * @return the simple name of implementation
         */
        public static String getSimpleName(final Class<?> token) {
            return token.getSimpleName() + "Impl";
        }

        /**
         * Returns the package of underlying class token
         *
         * @param token underlying class
         * @return package as string
         */
        private static String genPackage(final Class<?> token) {
            final String packageName = token.getPackageName();
            return packageName.equals(EMPTY) ? EMPTY : String.join(SPACE, PACKAGE, packageName, SEMICOLON);
        }

        /**
         * Returns the class declaration of implementation of the underlying class as given in the source code.
         *
         * @param token the underlying class
         * @return class declaration as {@link String}
         */
        private static String genClassDeclaration(final Class<?> token) {
            return String.join(
                    SPACE,
                    PUBLIC,
                    CLASS,
                    getSimpleName(token),
                    (token.isInterface()) ? IMPLEMENTS : EXTENDS,
                    token.getCanonicalName()
            );
        }

        /**
         * Returns string with arguments of the underlying {@code executable}.
         *
         * @param executable the underlying executable
         * @param onTypes    enable types of arguments if required
         * @return arguments of executable as {@link String}
         */
        private static String genArguments(final Executable executable, final boolean onTypes) {
            return String.join(
                    EMPTY,
                    "(",
                    Arrays.stream(executable.getParameters()).map(
                            parameter -> (onTypes ?
                                    parameter.getType().getCanonicalName() + SPACE :
                                    EMPTY) +
                                    parameter.getName()
                    )
                            .collect(Collectors.joining(COMMA)),
                    ")"
            );
        }

        /**
         * Returns string with exception of the underlying {@code executable}.
         *
         * @param executable the underlying executable
         * @return exceptions of executable as {@link String}
         */
        private static String genExceptions(final Executable executable) {
            Class<?>[] exceptions = executable.getExceptionTypes();
            if (exceptions.length == 0) {
                return EMPTY;
            }
            return String.join(
                    SPACE,
                    THROWS,
                    Arrays.stream(exceptions).map(Class::getCanonicalName).collect(Collectors.joining(COMMA))
            );
        }

        /**
         * Returns string with declaration of the underlying {@code executable}.
         *
         * @param executable      the underlying executable
         * @param beforeArguments string to be put before arguments
         * @return declaration of executable as {@link String}
         */
        private static String genExecutableDeclaration(final Executable executable, String beforeArguments) {
            return String.join(
                    SPACE,
                    PUBLIC,
                    beforeArguments,
                    genArguments(executable, true),
                    genExceptions(executable)
            );
        }

        /**
         * Returns string representation of given {@code constructor}.
         *
         * @param constructor the underlying constructor
         * @return arguments of executable as {@link String}
         */
        private static String genConstructor(final Constructor<?> constructor) {
            return String.join(
                    SPACE,
                    genExecutableDeclaration(constructor, getSimpleName(constructor.getDeclaringClass())),
                    START_BLOCK,
                    String.join(EMPTY, SUPER, genArguments(constructor, false), SEMICOLON),
                    END_BLOCK
            );
        }

        /**
         * Returns string with constructors of underlying {@code token} class
         *
         * @param token underlying token
         * @return string with constructors
         * @throws ImplerException if cannot create at least one constructor
         */
        private static String genConstructors(final Class<?> token) throws ImplerException {
            if (token.isInterface()) {
                return EMPTY;
            }
            List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                    .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers())).collect(Collectors.toList());
            if (constructors.isEmpty()) {
                throw new ImplerException("No accessible constructor");
            }
            return constructors.stream().map(CodeGenerator::genConstructor).collect(Collectors.joining(SPACE));
        }

        /**
         * Returns default value of underlying class token as string
         *
         * @param token underlying class token
         * @return default value as {@link String}
         */
        private static String genDefaultValue(final Class<?> token) {
            if (!token.isPrimitive()) {
                return null;
            } else if (token.equals(void.class)) {
                return "";
            } else if (token.equals(boolean.class)) {
                return "false";
            }
            return "0";
        }

        /**
         * Returns string with implementation of underlying {@code method}
         *
         * @param method underlying method
         * @return string with method
         */
        private static String genMethod(final Method method) {
            return String.join(
                    SPACE,
                    genExecutableDeclaration(
                            method,
                            String.join(SPACE, method.getReturnType().getCanonicalName(), method.getName())
                    ),
                    START_BLOCK,
                    String.join(SPACE, RETURN, genDefaultValue(method.getReturnType()), SEMICOLON),
                    END_BLOCK
            );
        }

        /**
         * Converts array of {@link Method} to {@link Set} of {@link MethodWrapper}
         *
         * @param methods array of methods
         * @return {@link Set} of {@link MethodWrapper}
         */
        private static Set<MethodWrapper> wrapMethods(Method[] methods) {
            return Arrays.stream(methods).map(MethodWrapper::new).collect(Collectors.toSet());
        }

        /**
         * Returns string with methods implementation of underlying {@code token} class
         *
         * @param token underlying token
         * @return string with methods
         */
        private static String genMethods(Class<?> token) {
            Set<CodeGenerator.MethodWrapper> methods = wrapMethods(token.getMethods());
            while (token != null) {
                methods.addAll(wrapMethods(token.getDeclaredMethods()));
                token = token.getSuperclass();
            }
            return methods.stream().
                    map(CodeGenerator.MethodWrapper::getMethod)
                    .filter(method -> Modifier.isAbstract(method.getModifiers()))
                    .map(CodeGenerator::genMethod)
                    .collect(Collectors.joining(SPACE));
        }

        /**
         * Generates string with implementation of underlying class {@code token}
         *
         * @param token underlying class
         * @return string with implementation
         * @throws ImplerException if occurs any error while implementation
         */
        public static String genImplementation(final Class<?> token) throws ImplerException {
            return String.join(
                    SPACE,
                    genPackage(token),
                    genClassDeclaration(token),
                    START_BLOCK,
                    genConstructors(token),
                    genMethods(token),
                    END_BLOCK
            );
        }

        /**
         * Class to compare methods by signature
         */
        private static class MethodWrapper {
            /**
             * containing method field
             */
            private final Method method;

            /**
             * Creates new instances of this class
             *
             * @param method base method
             */
            public MethodWrapper(Method method) {
                this.method = method;
            }

            /**
             * Getter for method
             *
             * @return returns stored method
             */
            public Method getMethod() {
                return method;
            }

            @Override
            public int hashCode() {
                return method.getName().hashCode() ^
                        Arrays.hashCode(method.getParameterTypes());
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null || getClass() != obj.getClass()) {
                    return false;
                }

                MethodWrapper other = (MethodWrapper) obj;
                return method.getName().equals(other.method.getName()) &&
                        Arrays.equals(method.getParameterTypes(), other.method.getParameterTypes());
            }
        }

    }

}
