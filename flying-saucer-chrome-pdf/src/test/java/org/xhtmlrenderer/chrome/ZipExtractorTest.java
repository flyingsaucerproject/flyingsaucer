package org.xhtmlrenderer.chrome;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZipExtractorTest {

    @Test
    void extractsFlatZip(@TempDir Path tmp) throws IOException {
        Path zip = tmp.resolve("flat.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            writeEntry(out, "hello.txt", "hello");
            writeEntry(out, "sub/world.txt", "world");
        }

        Path target = tmp.resolve("out");
        new ZipExtractor().extract(zip, target);

        assertThat(Files.readString(target.resolve("hello.txt"))).isEqualTo("hello");
        assertThat(Files.readString(target.resolve("sub/world.txt"))).isEqualTo("world");
    }

    @Test
    void rejectsEntryThatEscapesTargetDir(@TempDir Path tmp) throws IOException {
        Path zip = tmp.resolve("evil.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            writeEntry(out, "../escape.txt", "boom");
        }

        assertThatThrownBy(() -> new ZipExtractor().extract(zip, tmp.resolve("out")))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("escapes target directory");
    }

    @Test
    void createsTargetDirIfMissing(@TempDir Path tmp) throws IOException {
        Path zip = tmp.resolve("z.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            writeEntry(out, "a.txt", "a");
        }
        Path target = tmp.resolve("does/not/exist");

        new ZipExtractor().extract(zip, target);

        assertThat(Files.readString(target.resolve("a.txt"))).isEqualTo("a");
    }

    private static void writeEntry(ZipOutputStream out, String name, String content) throws IOException {
        out.putNextEntry(new ZipEntry(name));
        try (OutputStream s = nonClosing(out)) {
            s.write(content.getBytes(UTF_8));
        }
        out.closeEntry();
    }

    private static OutputStream nonClosing(OutputStream delegate) {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                delegate.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                delegate.write(b, off, len);
            }
        };
    }
}
