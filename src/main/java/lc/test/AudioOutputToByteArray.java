package lc.test;


import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

import java.io.ByteArrayOutputStream;

/**
 * @author liucheng
 * @version 1.0
 * @description: TODO
 * @date 2023/8/3 18:26
 */

public class AudioOutputToByteArray implements AudioProcessor {
    private boolean isDone = false;
    private byte[] out = null;
    private ByteArrayOutputStream bos;

    public AudioOutputToByteArray() {
        bos = new ByteArrayOutputStream();
    }

    public ByteArrayOutputStream getBos() {
        return bos;
    }

    public byte[] getData() {
        while (!isDone && out == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }

        return out;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {

        bos.write(audioEvent.getByteBuffer(), 0, audioEvent.getByteBuffer().length);
        return true;
    }

    @Override
    public void processingFinished() {
        out = bos.toByteArray().clone();
        bos = null;
        isDone = true;
    }
}
