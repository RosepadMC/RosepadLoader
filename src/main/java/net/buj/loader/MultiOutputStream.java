package net.buj.loader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiOutputStream extends OutputStream {

    private final List<OutputStream> streams;

    public MultiOutputStream(OutputStream[] streams) {
        super();
        this.streams = new ArrayList<>(Arrays.asList(streams));
    }

    @Override
    public void write(int b) throws IOException {
        for (int i = 0; i < streams.size(); i++)
            try {
                streams.get(i).write(b);
            } catch (IOException err) {
                System.err.println("Cannot write to stream " + i);
                err.printStackTrace();
                streams.remove(i);
                i--;
            }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (int i = 0; i < streams.size(); i++)
            try {
                streams.get(i).write(b);
            } catch (IOException err) {
                System.err.println("Cannot write to stream " + i);
                err.printStackTrace();
                streams.remove(i);
                i--;
            }
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < streams.size(); i++)
            try {
                streams.get(i).write(b, off, len);
            } catch (IOException err) {
                System.err.println("Cannot write to stream " + i);
                err.printStackTrace();
                streams.remove(i);
                i--;
            }
    }
}
