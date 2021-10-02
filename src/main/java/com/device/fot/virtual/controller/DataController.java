package com.device.fot.virtual.controller;

import com.device.fot.virtual.model.Data;
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
    private static boolean running = false, canSaveData = false;
    private Thread thread;

    private static final LinkedBlockingQueue<Data> BUFFER = new LinkedBlockingQueue();

    private DataController() {
        this("data.csv", 256);
    }

    private DataController(String fileName, int bufferSize) {
        this.fileName = fileName;
        this.bufferSize = bufferSize;
    }

    public void setCanSaveData(boolean canSaveData) {
        DataController.canSaveData = canSaveData;
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
        if (canSaveData) {
            BUFFER.put(data);
        }
    }

    private void write(List<String> lines) {
        try (var w = Files.newBufferedWriter(Path.of(fileName), StandardOpenOption.WRITE)) {
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
        DataController.running = true;
        List<String> lines = new ArrayList(this.bufferSize);
        while (running) {
            try {
                lines.add(BUFFER.take().toString());
                if (lines.size() >= bufferSize) {
                    this.write(lines);
                    lines.clear();
                }
            } catch (InterruptedException ex) {
                this.write(lines);
                running = false;
            }
        }
    }

}
