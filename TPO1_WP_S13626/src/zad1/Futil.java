package zad1;

import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.*;

public class Futil {
    public static void processDir(String dirName, String resultFileName) {
        Path path = Paths.get(dirName);
        Charset inCharset = Charset.forName("CP1250");
        Charset outCharset = Charset.forName("UTF-8");
        
        try (FileChannel fcout = new FileOutputStream(resultFileName).getChannel()) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    FileInputStream fis = new FileInputStream(file.toFile());
                    FileChannel fcin = fis.getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate((int)fcin.size());
                    
                    fcin.read(buffer);
                    buffer.flip();

                    CharBuffer dekoder = inCharset.decode(buffer);
                    buffer = outCharset.encode(dekoder);

                    fcout.write(buffer);
                    fcin.close();
                    fis.close();
                    return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
				}
            });


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}