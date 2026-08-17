package com.example

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

class FootballApp : Application(), ImageLoaderFactory {

  override fun newImageLoader(): ImageLoader {
    return ImageLoader.Builder(this)
      .memoryCachePolicy(CachePolicy.ENABLED)
      .memoryCache {
        MemoryCache.Builder(this)
          .maxSizePercent(0.20)
          .build()
      }
      .diskCachePolicy(CachePolicy.ENABLED)
      .diskCache {
        DiskCache.Builder()
          .directory(cacheDir.resolve("image_cache"))
          .maxSizeBytes(30 * 1024 * 1024) // 30MB
          .build()
      }
      .bitmapConfig(Bitmap.Config.ARGB_8888)
      .allowHardware(true)
      .crossfade(true)
      .build()
  }
}
