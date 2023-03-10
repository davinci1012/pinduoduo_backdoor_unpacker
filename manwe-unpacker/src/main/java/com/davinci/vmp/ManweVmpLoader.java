package com.davinci.vmp;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ManweVmpLoader {

    public static void main(String[] args) throws Throwable {
        String firmwarePath = "/tmp/mw1.bin";
        ManweVmpDataInputStream inputStream = new ManweVmpDataInputStream(Files.newInputStream(Paths.get(firmwarePath)));
        ManweVmpDex manweVmpDex = new ManweVmpDex(inputStream);
        System.out.printf("Load %d class%n", manweVmpDex.manweVmpClazzes.length);
        if (inputStream.available() != 0) {
            throw new RuntimeException(String.format("%d bytes remaining", inputStream.available()));
        }
        inputStream.close();
        if (Files.notExists(Paths.get("/tmp/final_java/"))) {
            new File("/tmp/final_java/").mkdirs();
        }
        manweVmpDex.writeClazzes("/tmp/final_java/");
    }
}
