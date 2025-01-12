/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.dataconnect.gradle.plugin

import java.io.Serializable

// The following command was used to generate the `serialVersionUID` constants for each class.
// serialver -classpath \
//   plugin/build/classes/kotlin/main:$(find $HOME/.gradle/wrapper/dists -name
// gradle-core-api-8.5.jar -printf '%p:') \
// com.google.firebase.dataconnect.gradle.plugin.DataConnectExecutableInput\${VerificationInfo,File,RegularFile,Version}

sealed interface DataConnectExecutable {

  data class VerificationInfo(val fileSizeInBytes: Long, val sha512DigestHex: String) :
    Serializable {

    companion object {
      fun forVersion(version: String): VerificationInfo =
        when (version) {
          "1.3.4" ->
            VerificationInfo(
              fileSizeInBytes = 24_125_592L,
              sha512DigestHex =
                "3ec9317db593ebeacfea9756cdd08a02849296fbab67f32f3d811a766be6ce2506f" +
                  "c7a0cf5f5ea880926f0c4defa5ded965268f5dfe5d07eb80cef926f216c7e"
            )
          "1.3.5" ->
            VerificationInfo(
              fileSizeInBytes = 24_146_072L,
              sha512DigestHex =
                "630391e3c50568cca36e562e51b300e673fa7190c0cae0475a03e4af4003babe711" +
                  "98c5b0309ecd261b3a3362e8c4d49bdb6cbc6f2b2d3297444112a018a0c10"
            )
          "1.3.6" ->
            VerificationInfo(
              fileSizeInBytes = 24_785_048L,
              sha512DigestHex =
                "77b2fd79a8a70e47defb1592a092c63642fda6c33715f1977d7a44daed3d7e181c3" +
                  "870aad0fee7b035aabea7778a244135ab3e633247ccd5f937105f6d495a26"
            )
          "1.3.7" ->
            VerificationInfo(
              fileSizeInBytes = 24_928_408L,
              sha512DigestHex =
                "99d9774f3b29a6845f0e096893d1205e69b6f8654797a3fc7d54d22e8f7059d1b65" +
                  "49ae23b8e8f18c952c1c7d25a07b0b8b29a957abd97e1a79c703448497cef"
            )
          "1.3.8" ->
            VerificationInfo(
              fileSizeInBytes = 24_940_696L,
              sha512DigestHex =
                "aea3583ebe1a36938eec5164de79405951ddf05b70a857ddb4f346f1424666f1d96" +
                  "989a5f81326c7e2aef4a195d31ff356fdf2331ed98fa1048c4bd469cbfd97"
            )
          else ->
            throw DataConnectGradleException(
              "3svd27ch8y",
              "File size and SHA512 digest is not known for version: $version"
            )
        }
    }
  }

  data class File(val file: java.io.File, val verificationInfo: VerificationInfo?) :
    DataConnectExecutable

  data class RegularFile(
    val file: org.gradle.api.file.RegularFile,
    val verificationInfo: VerificationInfo?
  ) : DataConnectExecutable

  data class Version(val version: String, val verificationInfo: VerificationInfo?) :
    DataConnectExecutable {
    companion object {
      fun forVersionWithDefaultVerificationInfo(version: String): Version {
        val verificationInfo = DataConnectExecutable.VerificationInfo.forVersion(version)
        return Version(version, verificationInfo)
      }
    }
  }
}
