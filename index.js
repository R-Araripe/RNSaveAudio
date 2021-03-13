import { NativeModules } from "react-native";
const { RNSaveAudio } = NativeModules;

export default {
    saveWav: (path, audio, sampleFreq) =>
        RNSaveAudio.saveWav(path, audio, sampleFreq),
};

//module.exports = NativeModules.RNSaveAudio;
