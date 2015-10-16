package com.hawst.cmcallrecord;

import android.content.Context;
import android.os.Build;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class CallRecording implements IXposedHookLoadPackage, IXposedHookInitPackageResources {

	public static final String DIALER = "com.android.dialer";
	public static final String CALL_RECORDING_SERVICE = "com.android.services.callrecorder.CallRecorderService";

	public static final String CALL_RECORD_FUNC_IS_ENABLED = "isEnabled";
	public static final String CALL_RECORD_FLAG = "call_recording_enabled";
	public static final String CALL_RECORD_SOURCE = "call_recording_audio_source";

	public static final String RES_BOOL = "bool";
	public static final String RES_INTEGER = "integer";

	public static final boolean CALL_RECORD_FLAG_VALUE = true;
	public static final int CALL_RECORD_SOURCE_VALUE = 4;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		if (DIALER.equals(lpparam.packageName)) {
			findAndHookMethod(CALL_RECORDING_SERVICE, lpparam.classLoader,
					CALL_RECORD_FUNC_IS_ENABLED, Context.class,
					XC_MethodReplacement.returnConstant(true));
		}
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam)
			throws Throwable {

		if (DIALER.equals(resparam.packageName)) {

			resparam.res.setReplacement(DIALER, RES_BOOL, CALL_RECORD_FLAG, CALL_RECORD_FLAG_VALUE);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				resparam.res.setReplacement(DIALER, RES_INTEGER, CALL_RECORD_SOURCE, CALL_RECORD_SOURCE_VALUE);
			}
		}
	}

}