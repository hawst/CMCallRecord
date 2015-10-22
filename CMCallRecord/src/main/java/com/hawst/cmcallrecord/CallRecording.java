package com.hawst.cmcallrecord;

import android.content.Context;
import android.content.res.XModuleResources;
import android.os.Build;
import android.telephony.ServiceState;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findField;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class CallRecording implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {
	private static String MODULE_PATH = null;

	public static final String PACKAGE_DIALER = "com.android.dialer";
	public static final String PACKAGE_ANDROID = "android";
	public static final String PACKAGE_TELEPHONY = "com.android.phone";

	public static final String CLASS_CALL_RECORDER_SERVICE = "com.android.services.callrecorder.CallRecorderService";
	public static final String CLASS_DC_TRACKER = "com.android.internal.telephony.dataconnection.DcTracker";
	public static final String CLASS_SERVICE_STATE = "android.telephony.ServiceState";

	public static final String FUNC_IS_ENABLED = "isEnabled";
	public static final String FUNC_CREATE_ALL_APN_LIST = "createAllApnList";
	public static final String FUNC_SET_RIL_DATA_RADIO_TECH = "setRilDataRadioTechnology";

	public static final String RES_CALL_RECORDING_ENABLED = "call_recording_enabled";
	public static final String RES_CALL_RECORDING_AUDIO_SOURCE = "call_recording_audio_source";

	public static final String RES_ENABLED_NETWORKS_CHOICES = "enabled_networks_choices";
	public static final String RES_ENABLED_NETWORKS_4G_CHOICES = "enabled_networks_4g_choices";
	public static final String RES_ENABLED_NETWORKS_VALUES = "enabled_networks_values";

	public static final String RES_TYPE_BOOL = "bool";
	public static final String RES_TYPE_INTEGER = "integer";
	public static final String RES_TYPE_ARRAY = "array";

	public static final boolean CALL_RECORD_FLAG_VALUE = true;
	public static final int CALL_RECORD_SOURCE_VALUE = 4;

	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
		MODULE_PATH = startupParam.modulePath;
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		if (PACKAGE_ANDROID.equals(lpparam.packageName)) {

			/*findAndHookMethod(CLASS_DC_TRACKER, lpparam.classLoader,
					FUNC_CREATE_ALL_APN_LIST, new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							Object dcTracker = param.thisObject;
							Field mAllApnSettings = findField(dcTracker.getClass().getSuperclass(), "mAllApnSettings");
							if (mAllApnSettings != null) {
								if (mAllApnSettings.get(dcTracker) == null) {
									mAllApnSettings.set(dcTracker, new ArrayList());
								}
							}
						}
					});*/

			findAndHookMethod(CLASS_SERVICE_STATE, lpparam.classLoader,
					FUNC_SET_RIL_DATA_RADIO_TECH, Integer.class, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							Field mRilDataRadioTechnology = findField(param.thisObject.getClass(), "mRilDataRadioTechnology");
							if (mRilDataRadioTechnology != null) {
								int type = (int)mRilDataRadioTechnology.get(param.thisObject);
								if (type == 102)
									mRilDataRadioTechnology.set(param.thisObject, 2);
							}
						}
					});
		}
		if (PACKAGE_DIALER.equals(lpparam.packageName)) {

			findAndHookMethod(CLASS_CALL_RECORDER_SERVICE, lpparam.classLoader,
					FUNC_IS_ENABLED, Context.class,
					XC_MethodReplacement.returnConstant(true));
		}
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam)
			throws Throwable {

		if (PACKAGE_DIALER.equals(resparam.packageName)) {

			resparam.res.setReplacement(PACKAGE_DIALER, RES_TYPE_BOOL, RES_CALL_RECORDING_ENABLED, CALL_RECORD_FLAG_VALUE);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				resparam.res.setReplacement(PACKAGE_DIALER, RES_TYPE_INTEGER, RES_CALL_RECORDING_AUDIO_SOURCE, CALL_RECORD_SOURCE_VALUE);
			}
		}
		else if (PACKAGE_TELEPHONY.equals(resparam.packageName)) {

			XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
			resparam.res.setReplacement(PACKAGE_TELEPHONY, RES_TYPE_ARRAY, RES_ENABLED_NETWORKS_CHOICES, modRes.fwd(R.array.enabled_networks_choices));
			resparam.res.setReplacement(PACKAGE_TELEPHONY, RES_TYPE_ARRAY, RES_ENABLED_NETWORKS_4G_CHOICES, modRes.fwd(R.array.enabled_networks_4g_choices));
			resparam.res.setReplacement(PACKAGE_TELEPHONY, RES_TYPE_ARRAY, RES_ENABLED_NETWORKS_VALUES, modRes.fwd(R.array.enabled_networks_values));
		}
	}

}