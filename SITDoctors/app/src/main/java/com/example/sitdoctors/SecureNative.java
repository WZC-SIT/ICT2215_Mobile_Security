package com.example.sitdoctors;

public class SecureNative {
    static {
        System.loadLibrary("native-lib");
    }

    public static native String getDecryptedKey();
}
