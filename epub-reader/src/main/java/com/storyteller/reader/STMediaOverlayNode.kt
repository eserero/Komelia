package com.storyteller.reader

/*
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

import org.readium.r2.shared.publication.Locator
import java.io.Serializable
import org.readium.r2.shared.util.Url

data class Clip(
    val audioResource: String,
    val fragmentId: String,
    val start: Double,
    val end: Double,
)

class MediaOverlayNode(
    val text: Url, // an URI possibly finishing by a fragment (textFile#id)
    val audio: Url?, // an URI possibly finishing by a simple timer (audioFile#t=start,end)
    val children: List<MediaOverlayNode> = listOf(),
    val role: List<String> = listOf(),
    val locator: Locator? = null
) : Serializable {

    val audioFile: String?
        get() = audio?.path?.substringBefore('#')
    val audioTime: String?
        get() = audio?.fragment ?: audio?.path?.substringAfter('#', "")
    val textFile: String
        get() = text.removeFragment().path!!
    val fragmentId: String?
        get() = text.fragment
    val clip: Clip?
        get() {
            val audio = audio ?: throw Exception("audio")
            val rawPath = audio.path ?: throw Exception("audioFile")
            // Url.fromEpubHref falls back to fromDecodedPath for filenames with spaces,
            // which percent-encodes '#' as '%23' in the path instead of treating it as a
            // URI fragment separator. Android's Uri.getPath() then decodes '%23' back to '#',
            // so removeFragment() is a no-op. Split the path string directly to be safe.
            val audioFile = rawPath.substringBefore('#')
            val times = audio.fragment ?: rawPath.substringAfter('#', "")
            val (start, end) = parseTimer(times)
            return Clip(audioFile, fragmentId ?: return null, start, end)
        }

    private fun parseTimer(times: String): Pair<Double, Double> {
        //  Remove "t=" prefix if present
        val netTimes = if (times.startsWith("t=")) times.substring(2) else times
        val parts = netTimes.split(',')
        val startTimer = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val endTimer = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
        return Pair(startTimer, endTimer)
    }
}
