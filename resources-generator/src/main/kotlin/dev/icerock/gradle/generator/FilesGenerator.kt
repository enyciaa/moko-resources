/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.android.AndroidFilesGenerator
import dev.icerock.gradle.generator.common.CommonFilesGenerator
import dev.icerock.gradle.generator.apple.AppleFilesGenerator
import dev.icerock.gradle.generator.jvm.JvmFilesGenerator
import org.gradle.api.file.FileTree
import java.io.File

abstract class FilesGenerator(
    private val inputFileTree: FileTree
) : MRGenerator.Generator {

    override val inputFiles: Iterable<File> get() = inputFileTree.files
    override val resourceClassName = ClassName("dev.icerock.moko.resources", "FileResource")
    override val mrObjectName: String = "files"

    override fun generate(resourcesGenerationDir: File, objectBuilder: TypeSpec.Builder): TypeSpec {
        val fileSpecs = inputFileTree.map { file ->
            FileSpec(
                key = processKey(file.nameWithoutExtension),
                file = file
            )
        }.sortedBy { it.key }
        val typeSpec = createTypeSpec(fileSpecs, objectBuilder)
        generateResources(resourcesGenerationDir, fileSpecs)
        return typeSpec
    }

    private fun createTypeSpec(keys: List<FileSpec>, objectBuilder: TypeSpec.Builder): TypeSpec {
        @Suppress("SpreadOperator")
        objectBuilder.addModifiers(*getClassModifiers())

        extendObjectBodyAtStart(objectBuilder)

        keys.forEach { objectBuilder.addProperty(generateFileProperty(it)) }
        extendObjectBodyAtEnd(objectBuilder)
        return objectBuilder.build()
    }

    override fun getImports(): List<ClassName> = emptyList()

    private fun generateFileProperty(
        fileSpec: FileSpec
    ): PropertySpec {
        @Suppress("SpreadOperator")
        return PropertySpec.builder(fileSpec.key, resourceClassName)
            .addModifiers(*getPropertyModifiers())
            .apply {
                getPropertyInitializer(fileSpec)?.let { initializer(it) }
            }
            .build()
    }

    protected open fun generateResources(
        resourcesGenerationDir: File,
        files: List<FileSpec>
    ) {
    }

    protected fun processKey(key: String): String {
        return key.replace("-", "_")
    }

    abstract fun getClassModifiers(): Array<KModifier>

    abstract fun getPropertyModifiers(): Array<KModifier>

    abstract fun getPropertyInitializer(fileSpec: FileSpec): CodeBlock?

    data class FileSpec(
        val key: String,
        val file: File
    )

    class Feature(private val info: SourceInfo) : ResourceGeneratorFeature<FilesGenerator> {

        private val fileTree = info.commonResources.matching {
            it.include("MR/files/**")
        }

        override fun createCommonGenerator() = CommonFilesGenerator(fileTree)

        override fun createIosGenerator() = AppleFilesGenerator(fileTree)

        override fun createAndroidGenerator() = AndroidFilesGenerator(
            fileTree,
            info.androidRClassPackage
        )

        override fun createJvmGenerator() = JvmFilesGenerator(fileTree)
    }
}
