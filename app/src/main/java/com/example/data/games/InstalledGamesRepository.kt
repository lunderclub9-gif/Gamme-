package com.example.data.games

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledGameInfo(
    val packageName: String,
    val title: String,
    val isCategorizedAsGame: Boolean,
    val iconBitmap: Bitmap?,
    val isPinned: Boolean = false,
    val lastLaunchedAt: Long = 0L,
    val launchCount: Int = 0,
    val assignedProfileId: Long? = null
)

class InstalledGamesRepository(context: Context) {

    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val selfPackage = appContext.packageName

    suspend fun loadLaunchableApps(): List<InstalledGameInfo> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        val seenPackages = HashSet<String>()
        resolveInfos.mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == selfPackage || !seenPackages.add(pkg)) return@mapNotNull null

            val appInfo = info.activityInfo.applicationInfo
            val label = info.loadLabel(packageManager)?.toString()?.takeIf { it.isNotBlank() } ?: pkg

            val isGameCategory = appInfo.category == ApplicationInfo.CATEGORY_GAME
            @Suppress("DEPRECATION")
            val hasLegacyGameFlag = (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0

            val icon = runCatching {
                drawableToBitmap(info.loadIcon(packageManager))
            }.getOrNull()

            InstalledGameInfo(
                packageName = pkg,
                title = label,
                isCategorizedAsGame = isGameCategory || hasLegacyGameFlag,
                iconBitmap = icon
            )
        }.sortedWith(
            compareByDescending<InstalledGameInfo> { it.isCategorizedAsGame }
                .thenBy { it.title.lowercase() }
        )
    }

    fun launchPackage(packageName: String): Boolean {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            appContext.startActivity(launchIntent)
            true
        }.getOrDefault(false)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 128
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 128
        val safeW = width.coerceAtMost(192)
        val safeH = height.coerceAtMost(192)
        val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
