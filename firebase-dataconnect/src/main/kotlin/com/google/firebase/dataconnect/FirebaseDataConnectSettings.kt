// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.firebase.dataconnect

class FirebaseDataConnectSettings private constructor(private val values: SettingsValues) {

  val hostName: String
    get() = values.hostName

  val port: Int
    get() = values.port

  val sslEnabled: Boolean
    get() = values.sslEnabled

  val builder: Builder
    get() = Builder(this)

  fun build(block: Builder.() -> Unit): FirebaseDataConnectSettings = builder.build(block)

  companion object {
    val defaults
      get() =
        FirebaseDataConnectSettings(
          SettingsValues(
            hostName = "firestore.googleapis.com",
            port = 443,
            sslEnabled = true,
          )
        )
  }

  class Builder internal constructor(initialValues: FirebaseDataConnectSettings) {
    var hostName = initialValues.hostName
    var port = initialValues.port
    var sslEnabled = initialValues.sslEnabled

    fun connectToEmulator() {
      hostName = "10.0.2.2"
      port = 9510
      sslEnabled = false
    }

    fun build(): FirebaseDataConnectSettings =
      FirebaseDataConnectSettings(
        SettingsValues(
          hostName = hostName,
          port = port,
          sslEnabled = sslEnabled,
        )
      )

    fun build(block: Builder.() -> Unit): FirebaseDataConnectSettings = apply(block).build()
  }

  override fun equals(other: Any?) =
    if (other === this) {
      true
    } else if (other !is FirebaseDataConnectSettings) {
      false
    } else {
      values == other.values
    }

  override fun hashCode() = values.hashCode()

  override fun toString() =
    "FirebaseDataConnectSettings{" +
      "hostName=$hostName, " +
      "port=$port, " +
      "sslEnabled=$sslEnabled" +
      "}"
}

// Use a data class internally to store the settings to get the convenience of the equals(),
// hashCode(), and copy() auto-generated methods.
private data class SettingsValues(
  val hostName: String,
  val port: Int,
  val sslEnabled: Boolean,
)