#include <jni.h>
#include <string.h>
#include <android/log.h>

#define LOG_TAG "NativeDecrypt"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

JNIEXPORT jstring JNICALL
Java_com_example_sitdoctors_SecureNative_getDecryptedKey(JNIEnv *env, jobject thiz) {
    // Hardcoded decrypted result (simulate actual AES)
    const char* decrypted = "104ad0a84da13d4"; // Replace with your actual secret
    return (*env)->NewStringUTF(env, decrypted);
}
