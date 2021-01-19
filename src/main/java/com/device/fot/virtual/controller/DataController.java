package com.device.fot.virtual.controller;

import com.device.fot.virtual.model.Data;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Uellington Damasceno
 */
public class DataController implements Runnable {

    private static DataController fileController = new DataController();

    private String fileName;
    private int bufferSize = 256;
    private boolean running = false;
    private Thread thread;

    private static final LinkedBlockingQueue<Data> BUFFER = new LinkedBlockingQueue();

    private DataController() {
        this("data.csv", 256);
    }

    private DataController(String fileName, int bufferSize) {
        this.fileName = fileName;
        this.bufferSize = bufferSize;
    }

    public static synchronized DataController getInstance() {
        return fileController;
    }

    public void start() {
        if (this.thread == null || !running) {
            this.thread = new Thread(this);
            this.thread.setDaemon(true);
            this.thread.setName("FILE/WRITER");
            this.thread.start();
        }
    }

    public void createAndSetDataFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        this.fileName = fileName;
    }

    public static void put(Data data) throws InterruptedException {
        BUFFER.put(data);
    }

    private void write(List<String> lines) {
        try (BufferedWriter w = Files.newBufferedWriter(Path.of(fileName), StandardOpenOption.WRITE)) {
            lines.forEach(line -> {
                try {
                    w.write(line);
                    w.newLine();
                } catch (IOException ex) {
                    Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        this.running = true;
        List<String> lines = new ArrayList(this.bufferSize);
        int count = 0;
        while (running) {
            try {
                Data data = BUFFER.take();
                lines.add(data.toString());
                count++;
                if (count >= bufferSize) {
                    this.write(lines);
                    lines.clear();
                    count = 0;
                }
            } catch (InterruptedException ex) {
                this.write(lines);
                running = false;
            }
        }
    }

}
