/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.desc

import dev.icerock.moko.parcelize.Parcelable

actual class RawStringDesc actual constructor(val string: String) : StringDesc, Parcelable {
    override fun localized() = string
}