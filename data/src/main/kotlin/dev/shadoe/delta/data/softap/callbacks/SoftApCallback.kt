package dev.shadoe.delta.data.softap.callbacks

import android.net.TetheringManagerHidden
import android.net.TetheringManagerHidden.TETHERING_WIFI
import android.net.wifi.ISoftApCallback
import android.net.wifi.IWifiManager
import android.net.wifi.SoftApCapability
import android.net.wifi.SoftApInfo
import android.net.wifi.SoftApState
import android.net.wifi.WifiClient
import android.os.Build
import androidx.annotation.RequiresApi
import dev.rikka.tools.refine.Refine
import dev.shadoe.delta.api.SoftApCapabilities
import dev.shadoe.delta.api.SoftApSecurityType.SECURITY_TYPE_OPEN
import dev.shadoe.delta.api.SoftApSecurityType.SECURITY_TYPE_WPA2_PSK
import dev.shadoe.delta.api.SoftApSecurityType.SECURITY_TYPE_WPA3_SAE
import dev.shadoe.delta.api.SoftApSecurityType.SECURITY_TYPE_WPA3_SAE_TRANSITION
import dev.shadoe.delta.api.SoftApSpeedType
import dev.shadoe.delta.api.SoftApSpeedType.BandType
import dev.shadoe.delta.data.softap.internal.TetheringEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class SoftApCallback(
  private val tetheringEventListener: TetheringEventListener,
  private val wifiManager: IWifiManager,
) : ISoftApCallback.Stub() {
  override fun onStateChanged(state: SoftApState?) {
    state ?: return
    Refine.unsafeCast<TetheringManagerHidden.TetheringRequest?>(
        state.tetheringRequest
      )
      ?.parcel
      ?.tetheringType
      ?.let { it == TETHERING_WIFI }
      .takeIf { it == true }
      .let { tetheringEventListener.onEnabledStateChanged(state.state) }
  }

  @Deprecated("Removed in API 35")
  override fun onStateChanged(state: Int, failureReason: Int) {
    tetheringEventListener.onEnabledStateChanged(state)
  }

  /** Results in a no-op because already [TetheringEventCallback] handles it */
  override fun onConnectedClientsOrInfoChanged(
    infos: Map<String?, SoftApInfo?>?,
    clients: Map<String?, List<WifiClient?>?>?,
    isBridged: Boolean,
    isRegistration: Boolean,
  ) {}

  /** Results in a no-op because already [TetheringEventCallback] handles it */
  @Deprecated("Removed in API 31")
  override fun onConnectedClientsChanged(clients: List<WifiClient?>?) {}

  /** Results in a no-op because already [TetheringEventCallback] handles it */
  @Deprecated("Removed in API 31")
  override fun onInfoChanged(softApInfo: SoftApInfo?) {}

  /** Results in a no-op because already [TetheringEventCallback] handles it */
  @Deprecated("Removed in API 31")
  override fun onInfoListChanged(softApInfoList: List<SoftApInfo?>?) {}

  /**
   * Gets supported bands, max client limit and other capabilities and update
   * state.
   */
  override fun onCapabilityChanged(capability: SoftApCapability?) {
    capability ?: return
    runBlocking {
      launch(Dispatchers.Unconfined) {
        tetheringEventListener.onSoftApCapabilitiesChanged(
          SoftApCapabilities(
            maxSupportedClients = capability.maxSupportedClients,
            clientForceDisconnectSupported =
              capability.areFeaturesSupported(
                SoftApCapability.SOFTAP_FEATURE_CLIENT_FORCE_DISCONNECT
              ),
            isMacAddressCustomizationSupported =
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                capability.areFeaturesSupported(
                  SoftApCapability.SOFTAP_FEATURE_MAC_ADDRESS_CUSTOMIZATION
                )
              } else {
                false
              },
            supportedFrequencyBands =
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                querySupportedFrequencyBands(capability)
              } else {
                querySupportedFrequencyBands(wifiManager)
              },
            supportedSecurityTypes = querySupportedSecurityTypes(capability),
          )
        )
      }
    }
  }

  /**
   * This function is only called by Android when a device is not in allow or
   * block list when
   * `SoftApConfiguration.Builder.setClientControlByUserEnabled(true)` is set,
   * else blocked devices are silently blocked The devices in block list aren't
   * sent via this callback, only devices that aren't in either list are sent
   * for the purpose of updating our ACL
   *
   * Also, this callback is called when maximum connection limit is reached.
   *
   * TODO: think about how to use this in the going forward.
   */
  override fun onBlockedClientConnecting(
    client: WifiClient?,
    blockedReason: Int,
  ) {
    println("blocked client ${client?.macAddress} $blockedReason")
  }

  override fun onClientsDisconnected(
    info: SoftApInfo?,
    clients: List<WifiClient?>?,
  ) {}

  private suspend fun querySupportedSecurityTypes(
    capability: SoftApCapability
  ) =
    withContext(Dispatchers.Unconfined) {
      val saeSupport =
        capability.areFeaturesSupported(
          SoftApCapability.SOFTAP_FEATURE_WPA3_SAE
        )
      val types = mutableListOf(SECURITY_TYPE_WPA2_PSK, SECURITY_TYPE_OPEN)
      if (saeSupport) {
        types.addAll(
          index = 0,
          elements =
            listOf(SECURITY_TYPE_WPA3_SAE, SECURITY_TYPE_WPA3_SAE_TRANSITION),
        )
      }
      types.toList()
    }

  private suspend fun querySupportedFrequencyBands(wifiManager: IWifiManager) =
    withContext(Dispatchers.Unconfined) {
      @BandType val bands = mutableListOf(SoftApSpeedType.BAND_2GHZ)
      if (wifiManager.is5GHzBandSupported) {
        bands += SoftApSpeedType.BAND_5GHZ
      }
      if (wifiManager.is6GHzBandSupported) {
        bands += SoftApSpeedType.BAND_6GHZ
      }
      bands.toList()
    }

  @RequiresApi(Build.VERSION_CODES.S)
  private suspend fun querySupportedFrequencyBands(
    capability: SoftApCapability
  ) =
    withContext(Dispatchers.Unconfined) {
      mapOf(
          SoftApSpeedType.BAND_2GHZ to
            SoftApCapability.SOFTAP_FEATURE_BAND_24G_SUPPORTED,
          SoftApSpeedType.BAND_5GHZ to
            SoftApCapability.SOFTAP_FEATURE_BAND_5G_SUPPORTED,
          SoftApSpeedType.BAND_6GHZ to
            SoftApCapability.SOFTAP_FEATURE_BAND_6G_SUPPORTED,
        )
        .filter {
          capability.run {
            areFeaturesSupported(it.value) and
              getSupportedChannelList(it.key).isNotEmpty()
          }
        }
        .keys
        .toList()
    }
}
