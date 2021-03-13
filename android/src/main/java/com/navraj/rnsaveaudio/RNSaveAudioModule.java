package com.navraj.rnsaveaudio;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.IOException;
import java.lang.String;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class RNSaveAudioModule extends ReactContextBaseJavaModule {
    public RNSaveAudioModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNSaveAudio";
    }

    @ReactMethod
    public void saveWav(String path, ReadableArray audio, Integer sampleFreq, final Promise promise){
        try {
            short[] newaudio = new short[audio.size()];
            for(int i=0; i<audio.size();i++){
                newaudio[i] = (short) (audio.getInt(i) & 0xFFFF);
            }
            boolean result = SaveFile(path, newaudio, sampleFreq);
            promise.resolve(result);
        } catch (Exception ex) {
            promise.reject("ERR_UNEXPECTED_EXCEPTION", ex);
        }
    }
    private boolean SaveFile(String path, short[] rawData, int sampleFreq) throws Exception{

        DataOutputStream output = null;
        boolean ret = true;
        byte[] data = get16BitPcm(rawData);
        try {
            output = new DataOutputStream(new FileOutputStream(path));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + data.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, sampleFreq); // sample rate
            writeInt(output, sampleFreq * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, data.length); // subchunk 2 size

            // Audio data (conversion big endian -> little endian)
            ByteBuffer buffer = ByteBuffer.allocate(2*rawData.length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            for(short i: rawData){
                buffer.putShort(i);
            }
            byte[] bytes = buffer.array();
            output.write(bytes);

        } catch (Exception err) {
            Log.e("Exception here in SaveFile :( ", err)
            return ret;
        } finally {
            if (output != null) {
                output.close();
                ret = false;
            }
            return ret;
        }
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    private byte[] get16BitPcm(short[] data) {
        byte[] resultData = new byte[2 * data.length];
        int iter = 0;
        for (double sample : data) {
            short maxSample = (short)((sample * Short.MAX_VALUE));
            resultData[iter++] = (byte)(maxSample & 0x00ff);
            resultData[iter++] = (byte)((maxSample & 0xff00) >>> 8);
        }
        return resultData;
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }
}
