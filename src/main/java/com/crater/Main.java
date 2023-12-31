package com.crater;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static void compress(String name, File... files) throws IOException {
        try (var out = new SevenZOutputFile(new File(name))) {
            for (File file : files) {
                addToArchiveCompression(out, file, ".");
            }
        }
    }

    private static void addToArchiveCompression(SevenZOutputFile out, File file, String dir) throws IOException {
        var name = dir + File.separator + file.getName();
        if (file.isFile()) {
            System.out.println("will compress file: " + name);
            var entry = out.createArchiveEntry(file, name);
            out.putArchiveEntry(entry);

            var in = new FileInputStream(file);
            var b = new byte[1024];
            var count = 0;
            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
            out.closeArchiveEntry();

        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addToArchiveCompression(out, child, name);
                }
            }
        } else {
            System.out.println(file.getName() + " is not supported");
        }
    }

    public static void decompress(String in, File destination) throws IOException {
        SevenZFile sevenZFile = new SevenZFile(new File(in));
        SevenZArchiveEntry entry;
        while ((entry = sevenZFile.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }
            System.out.println("decompress file: " + entry.getName());
            File curfile = new File(destination, entry.getName());
            File parent = curfile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(curfile);
            byte[] content = new byte[(int) entry.getSize()];
            sevenZFile.read(content, 0, content.length);
            out.write(content);
            out.close();
        }
    }

    public static void readCompress(String sevenZPath) throws IOException {
        var sevenZFile = new SevenZFile(new File(sevenZPath));
        SevenZArchiveEntry entry;
        while ((entry = sevenZFile.getNextEntry()) != null) {
            System.out.println(entry.getName());
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            var command = args[0];
            var sevenZPath = args[1];
            if (command.equals("-c")) {
                var targetPath = args[2];
                compress(sevenZPath, new File(targetPath));
            } else if (command.equals("-dc")) {
                var targetPath = args[2];
                decompress(sevenZPath, new File(targetPath));
            } else if (command.equals("-rc")) {
                readCompress(sevenZPath);
            } else {
                System.out.println("must -c or -dc or -rc");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}