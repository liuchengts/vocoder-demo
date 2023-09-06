package lc.test;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.ZeroCrossingRateProcessor;
import be.tarsos.dsp.effects.DelayEffect;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.resample.RateTransposer;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import javax.sound.sampled.*;
import java.io.*;

import static lc.test.SoundEnum.DASHU;

/**
 * @author liucheng
 * @version 1.0
 * @description: TODO
 * @date 2023/8/3 18:18
 */
public class Test {
    static String path = System.getProperty("user.dir");
    static String sourcePath = String.join(File.separator, path, "音乐.mp3");
    static String targetPath = String.join(File.separator, path, "target.mp3");

    public static void main(String[] args) throws Exception {
//        record();
        //这里返回的是pcm格式的音频
        byte[] bytes = speechPitchShiftMp3(sourcePath, 1, 1);
////
////        //如果需要转成wav则需要给pcmBytes增加一个头部信息
////        //TarsosDSP中也有输出Wav格式音频的处理器，这里没有使用。
        byte[] wavHeader = pcm2wav(bytes);
        OutputStream wavOutPut = new FileOutputStream(targetPath);
        wavOutPut.write(wavHeader);
        wavOutPut.write(bytes);
        wavOutPut.flush();
        wavOutPut.close();
//
//        // 对于各种声音类型，以及所需添加的处理器，还有处理器参数代码，将在本文最后给出。
//        //如果需要转mp3格式的，也可以给我留言，我会加上。
    }

    public static void record() throws LineUnavailableException, IOException, EncoderException {
        File source = new File(sourcePath);
        File target = new File(targetPath);
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(128000);
        audio.setChannels(2);
        audio.setSamplingRate(44100);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("mp3");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(source), target, attrs);
        System.out.println("完成");
    }

    /**
     * 变声
     *
     * @param speedFactor 变速率 (0,2) 大于1为加快语速，小于1为放慢语速
     * @param rateFactor  音调变化率 (0,2) 大于1为降低音调（深沉），小于1为提升音调（尖锐）
     * @return 变声后的MP3数据输入流
     */
    public static byte[] speechPitchShiftMp3(String fileUrl, double rateFactor, double speedFactor) throws IOException, UnsupportedAudioFileException {

        WaveformSimilarityBasedOverlapAdd w = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.speechDefaults(rateFactor, 16000));
        int inputBufferSize = w.getInputBufferSize();
        int overlap = w.getOverlap();

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(fileUrl, 16000, inputBufferSize, overlap);
        w.setDispatcher(dispatcher);
        dispatcher.addAudioProcessor(w);

        /** 采样率转换器。 使用插值更改采样率, 与时间拉伸器一起可用于音高转换。 **/
        dispatcher.addAudioProcessor(new RateTransposer(speedFactor));
        AudioOutputToByteArray out = new AudioOutputToByteArray();


        /** 声音速率转换器 -- 失败 **/
        /*SoundTouchRateTransposer soundTouchRateTransposer = new SoundTouchRateTransposer(2);
        soundTouchRateTransposer.setDispatcher(dispatcher);
        dispatcher.addAudioProcessor(soundTouchRateTransposer);*/

        /** 正弦波发生器 -- 无反应 **/
        /*SineGenerator sineGenerator = new SineGenerator(0.5, 0.5);
        dispatcher.addAudioProcessor(sineGenerator);*/

        /** 音调转换器 -- 无效果 **/
//        dispatcher.addAudioProcessor(new PitchShifter(0.1,16000,448,overlap));

        /** 制粒机使用颗粒合成回放样本。方法可用于控制播放速率，音高，颗粒大小， -- 无效果 **/
//        dispatcher.addAudioProcessor(new OptimizedGranulator(16000, 448));

        /** 噪音产生器 -- 有效果 **/
//        dispatcher.addAudioProcessor(new NoiseGenerator(0.2   ));

        /** 增益处理器  增益为1，则无任何反应。 增益大于1表示音量增加a -- 有反应 **/
//        dispatcher.addAudioProcessor(new GainProcessor(10));

        /**镶边效果 -- 有反应 **/
//        dispatcher.addAudioProcessor(new FlangerEffect(64, 0.3, 16000, 16000));// 回声效果
//        dispatcher.addAudioProcessor(new FlangerEffect(1 << 4, 0.8, 8000, 2000));// 感冒
//        dispatcher.addAudioProcessor(new ZeroCrossingRateProcessor());//感冒

        /** 淡出 --声音慢慢变小 **/
//        dispatcher.addAudioProcessor(new FadeOut(5));

        /** 淡入-- 声音慢慢变大 **/
//        dispatcher.addAudioProcessor(new FadeIn(5));

        /** 在信号上添加回声效果。echoLength以秒为单位  elay回声的衰减，介于0到1之间的值。1表示无衰减，0表示立即衰减 **/
//        dispatcher.addAudioProcessor(new DelayEffect(0.2, 0.24, 12000));

        /** 调幅噪声 -- 将声音转换为噪声**/
//        dispatcher.addAudioProcessor(new AmplitudeModulatedNoise());

        /** 振幅LFO -- 声音波动 **/
//        dispatcher.addAudioProcessor(new AmplitudeLFO());

        dispatcher.addAudioProcessor(new DelayEffect(0.8, 0.5, 12000));
        dispatcher.addAudioProcessor(new DelayEffect(0.5, 0.3, 8000));

        dispatcher.addAudioProcessor(out);
        dispatcher.run();


//        return new ByteArrayInputStream(out.getData());
        return out.getData();
    }


    public static byte[] pcm2wav(byte[] bytes) throws IOException {
        //填入参数，比特率等等。这里用的是16位单声道 8000 hz
        WaveHeader header = new WaveHeader();
        //长度字段 = 内容的大小（PCMSize) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
        header.fileLength = bytes.length + (44 - 8);
        header.FmtHdrLeth = 16;
        header.BitsPerSample = 16;
        header.Channels = 1;
        header.FormatTag = 0x0001;
        header.SamplesPerSec = 16000;
        header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
        header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
        header.DataHdrLeth = bytes.length;

        byte[] h = header.getHeader();
        assert h.length == 44; //WAV标准，头部应该是44字节
        return h;
    }


}
