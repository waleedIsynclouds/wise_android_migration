package com.example.utils

object RFModuleTypeHelper {
    @JvmStatic
    fun isHXJNBIoTRFType(rfType: Int): Boolean {
        return rfType == RFModuleType.HXJNBDX || rfType == RFModuleType.HXJNBMQTT || rfType == RFModuleType.HXJNBLWM2M
    }

    @JvmStatic
    fun isHXJWiFiRFType(rfType: Int): Boolean {
        return rfType == RFModuleType.HXJWIFI || rfType == RFModuleType.HXJWIFIZJJX
    }

    @JvmStatic
    fun isCat1RFType(rfType: Int): Boolean {
        return rfType == RFModuleType.HXJCat1
    }
}
