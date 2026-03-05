// Copyright (C) 2021  ArchieMeng <archiemeng@protonmail.com>
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//        limitations under the License.

#include <android/asset_manager_jni.h>
#include <android/bitmap.h>
#include <android/log.h>

#include <jni.h>

#include <string>
#include <vector>
#include <mutex>

// ncnn
#include "layer.h"
#include "net.h"
#include "benchmark.h"
#include "waifu2x/waifu2x.h"
#include "realcugan/realcugan.h"

static ncnn::UnlockedPoolAllocator g_blob_pool_allocator;
static ncnn::PoolAllocator g_workspace_pool_allocator;

static std::mutex engine_mutex;
static Waifu2x* waifu2x = 0;
static RealCUGAN* realcugan = 0;
static int current_engine = 0; // 0 for waifu2x, 1 for realcugan
static auto gpu_mode = true;

extern "C" {
static const char *TAG = "Upscaler-ncnn-Vulkan";
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "JNI_OnLoad");
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "JNI_OnUnload");
}
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_github_snd_1r_komelia_infra_ncnn_NcnnUpscaler_createGpuInstance(JNIEnv *env, jobject thiz) {
    return ncnn::create_gpu_instance();
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_snd_1r_komelia_infra_ncnn_NcnnUpscaler_destroyGpuInstance(JNIEnv *env, jobject thiz) {
    ncnn::destroy_gpu_instance();
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_github_snd_1r_komelia_infra_ncnn_NcnnUpscaler_init(JNIEnv *env, jobject thiz,
                                            jint engine_type,
                                            jint gpu_id, jboolean tta_mode, jint num_threads) {
    std::lock_guard<std::mutex> lock(engine_mutex);
    current_engine = engine_type;
    gpu_mode = gpu_id != -1;
    int gpuid = gpu_id;
    if (gpu_id == -1) gpuid = ncnn::get_default_gpu_index();

    if (gpu_mode && (gpuid < 0 || gpuid >= ncnn::get_gpu_count())) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "invalid gpu_id %d, falling back to CPU", gpuid);
        gpu_mode = false;
        gpuid = -1;
    }

    if (current_engine == 0) {
        if (waifu2x) {
            delete waifu2x;
            waifu2x = 0;
        }
        waifu2x = new Waifu2x(gpuid, tta_mode, num_threads);
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "waifu2x class: %p successfully initialized", waifu2x);
    } else {
        if (realcugan) {
            delete realcugan;
            realcugan = 0;
        }
        realcugan = new RealCUGAN(gpuid, tta_mode, num_threads);
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "realcugan class: %p successfully initialized", realcugan);
    }

    if (gpu_mode) {
        ncnn::VulkanDevice* device = ncnn::get_gpu_device(gpuid);
        if (!device) {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "GPU device %d not found, falling back to CPU", gpuid);
            gpu_mode = false;
            if (current_engine == 0) waifu2x->tilesize = -1;
            else realcugan->tilesize = -1;
            return -1;
        }

        uint32_t heap_budget_size = device->get_heap_budget();
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "heap budget size: %u", heap_budget_size);
        int tilesize = 0;
        if (heap_budget_size > 3900) {
            tilesize = 128; // Capped at 128 for Android stability
        } else if (heap_budget_size > 1000) {
            tilesize = 128;
        } else if (heap_budget_size > 250) {
            tilesize = 64;
        } else {
            tilesize = 32;
        }
        if (current_engine == 0) waifu2x->tilesize = tilesize;
        else realcugan->tilesize = tilesize;
    } else {
        if (current_engine == 0) {
            if (waifu2x) waifu2x->tilesize = -1;
        } else {
            if (realcugan) realcugan->tilesize = -1;
        }
    }
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_github_snd_1r_komelia_infra_ncnn_NcnnUpscaler_load(JNIEnv *env, jobject thiz,
                                            jobject asset_manager,
                                            jstring param_path, jstring model_path) {
    std::lock_guard<std::mutex> lock(engine_mutex);
    const char* parampath_chars = env->GetStringUTFChars(param_path, JNI_FALSE);
    const char* modelpath_chars = env->GetStringUTFChars(model_path, JNI_FALSE);
    std::string parampath(parampath_chars);
    std::string modelpath(modelpath_chars);
    env->ReleaseStringUTFChars(param_path, parampath_chars);
    env->ReleaseStringUTFChars(model_path, modelpath_chars);

    int ret = -1;
    if (current_engine == 0) {
        if (!waifu2x) return -1;
        int scale = waifu2x->scale;
        int noise = waifu2x->noise;
        int prepadding = 0;
        if (modelpath.find("models-cunet") != std::string::npos)
        {
            if (noise == -1) prepadding = 18;
            else if (scale == 1) prepadding = 28;
            else if (scale == 2) prepadding = 18;
        }
        else if (modelpath.find("models-upconv_7_anime_style_art_rgb") != std::string ::npos
        || modelpath.find("models-upconv_7_photo") != std::string::npos)
        {
            prepadding = 7;
        }
        else
        {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "unknown model dir type");
        }
        waifu2x->prepadding = prepadding;
        if (asset_manager) ret = waifu2x->load(env, asset_manager, parampath, modelpath);
        else ret = waifu2x->load(parampath, modelpath);
    } else {
        if (!realcugan) return -1;
        // RealCUGAN prepadding: 18 for up2x-conservative
        realcugan->prepadding = 18;
        if (asset_manager) ret = realcugan->load(env, asset_manager, parampath, modelpath);
        else ret = realcugan->load(parampath, modelpath);
    }
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_github_snd_1r_komelia_infra_ncnn_NcnnUpscaler_process(JNIEnv *env, jobject thiz, jobject in_bitmap,
                                               jobject out_bitmap) {
    std::lock_guard<std::mutex> lock(engine_mutex);
    void *in_bytes, *out_bytes;
    AndroidBitmapInfo in_info, out_info;
    AndroidBitmap_lockPixels(env, in_bitmap, &in_bytes);
    AndroidBitmap_lockPixels(env, out_bitmap, &out_bytes);

    AndroidBitmap_getInfo(env, in_bitmap, &in_info);
    AndroidBitmap_getInfo(env, out_bitmap, &out_info);

    const ncnn::Mat in = ncnn::Mat(in_info.width, in_info.height, (void *) in_bytes, (size_t) 4, 4);
    ncnn::Mat out = ncnn::Mat(out_info.width, out_info.height, (void *) out_bytes, (size_t) 4, 4);

    int ret = 0;
    if (current_engine == 0) {
        if (waifu2x) {
            if (!gpu_mode) {
                waifu2x->tilesize = in_info.width > in_info.height ? in_info.width : in_info.height;
            }
            ret = waifu2x->process(in, out);
            if (ret != 0) __android_log_print(ANDROID_LOG_ERROR, TAG, "waifu2x process failed: %d", ret);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "waifu2x not initialized");
            ret = -1;
        }
    } else {
        if (realcugan) {
            if (!gpu_mode) {
                realcugan->tilesize = in_info.width > in_info.height ? in_info.width : in_info.height;
            }
            ret = realcugan->process(in, out);
            if (ret != 0) __android_log_print(ANDROID_LOG_ERROR, TAG, "realcugan process failed: %d", ret);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "realcugan not initialized");
            ret = -1;
        }
    }

    AndroidBitmap_unlockPixels(env, out_bitmap);
    AndroidBitmap_unlockPixels(env, in_bitmap);

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "upscale done");
    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_snd_1r_komelia_infra_ncnn_NcnnUpscaler_setScale(JNIEnv *env, jobject thiz, jint scale) {
    std::lock_guard<std::mutex> lock(engine_mutex);
    if (current_engine == 0) {
        if (waifu2x) waifu2x->scale = scale > 1 ? 2 : 1;
    } else {
        if (realcugan) realcugan->scale = scale;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_snd_1r_komelia_infra_ncnn_NcnnUpscaler_setNoise(JNIEnv *env, jobject thiz, jint noise) {
    std::lock_guard<std::mutex> lock(engine_mutex);
    if (current_engine == 0) {
        if (waifu2x) waifu2x->noise = noise;
    } else {
        if (realcugan) realcugan->noise = noise;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_github_snd_1r_komelia_infra_ncnn_NcnnUpscaler_getTileSize(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engine_mutex);
    if (current_engine == 0) return waifu2x ? waifu2x->tilesize : 0;
    else return realcugan ? realcugan->tilesize : 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_snd_1r_komelia_infra_ncnn_NcnnUpscaler_setTileSize(JNIEnv *env, jobject thiz, jint tile_size) {
    std::lock_guard<std::mutex> lock(engine_mutex);
    if(tile_size >= 32) {
        if (current_engine == 0) {
            if (waifu2x) waifu2x->tilesize = tile_size;
        } else {
            if (realcugan) realcugan->tilesize = tile_size;
        }
    } else {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "tile size too short: cannot be %d", tile_size);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_snd_1r_komelia_infra_ncnn_NcnnUpscaler_release(JNIEnv *env, jobject thiz) {
    std::lock_guard<std::mutex> lock(engine_mutex);
    if (waifu2x) {
        delete waifu2x;
        waifu2x = 0;
    }
    if (realcugan) {
        delete realcugan;
        realcugan = 0;
    }
}
