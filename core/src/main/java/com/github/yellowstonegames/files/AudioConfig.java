package com.github.yellowstonegames.files;

import com.anyicomplex.gdx.svm.CollectForGDXJsonSerialization;

@CollectForGDXJsonSerialization
public class AudioConfig {

    public boolean soundfxOn = true;
    public boolean musicOn = true;
    public float soundfxVolume = 1f;
    public float musicVolume = 1f;

    public AudioConfig() {}
}
