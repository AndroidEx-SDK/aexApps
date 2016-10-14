LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13
LOCAL_PACKAGE_NAME := aexSettings
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_CERTIFICATE := platform
LOCAL_STATIC_JAVA_LIBRARIES := aexlib
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)
