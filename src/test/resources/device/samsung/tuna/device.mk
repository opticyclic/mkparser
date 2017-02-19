$(call inherit-product, frameworks/native/build/phone-xhdpi-1024-dalvik-heap.mk)
$(call inherit-product-if-exists, vendor/nxp/pn544/nxp-pn544-fw-vendor.mk)
$(call inherit-product, hardware/ti/omap4/omap4.mk)
# $(call inherit-product-if-exists, vendor/ti/omap4/omap4-vendor.mk)
# $(call inherit-product-if-exists, vendor/samsung/tuna/tuna-vendor.mk)

$(call inherit-product-if-exists, hardware/broadcom/wlan/bcmdhd/firmware/bcm4330/device-bcm.mk)
