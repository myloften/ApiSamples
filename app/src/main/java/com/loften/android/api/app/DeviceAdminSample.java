package com.loften.android.api.app;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.loften.android.api.R;

import java.util.List;

public class DeviceAdminSample extends PreferenceActivity {

    // Miscellaneous utilities and definitions
    private static final String TAG = "DeviceAdminSample";

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    private static final int REQUEST_CODE_START_ENCRYPTION = 2;

    private static final long MS_PER_MINUTE = 60 * 1000;
    private static final long MS_PER_HOUR = 60 * MS_PER_MINUTE;
    private static final long MS_PER_DAY = 24 * MS_PER_HOUR;

    // The following keys are used to find each preference item
    private static final String KEY_ENABLE_ADMIN = "key_enable_admin";
    private static final String KEY_DISABLE_CAMERA = "key_disable_camera";
    private static final String KEY_DISABLE_NOTIFICATIONS = "key_disable_notifications";
    private static final String KEY_DISABLE_UNREDACTED = "key_disable_unredacted";
    private static final String KEY_DISABLE_TRUST_AGENTS = "key_disable_trust_agents";
    private static final String KEY_TRUST_AGENT_COMPONENT = "key_trust_agent_component";
    private static final String KEY_TRUST_AGENT_FEATURES = "key_trust_agent_features";
    private static final String KEY_DISABLE_KEYGUARD_WIDGETS = "key_disable_keyguard_widgets";
    private static final String KEY_DISABLE_KEYGUARD_SECURE_CAMERA
            = "key_disable_keyguard_secure_camera";
    private static final String KEY_DISABLE_FINGERPRINT = "key_disable_fingerprint";
    private static final String KEY_DISABLE_REMOTE_INPUT = "key_disable_remote_input";

    private static final String KEY_CATEGORY_QUALITY = "key_category_quality";
    private static final String KEY_SET_PASSWORD = "key_set_password";
    private static final String KEY_RESET_PASSWORD = "key_reset_password";
    private static final String KEY_QUALITY = "key_quality";
    private static final String KEY_MIN_LENGTH = "key_minimum_length";
    private static final String KEY_MIN_LETTERS = "key_minimum_letters";
    private static final String KEY_MIN_NUMERIC = "key_minimum_numeric";
    private static final String KEY_MIN_LOWER_CASE = "key_minimum_lower_case";
    private static final String KEY_MIN_UPPER_CASE = "key_minimum_upper_case";
    private static final String KEY_MIN_SYMBOLS = "key_minimum_symbols";
    private static final String KEY_MIN_NON_LETTER = "key_minimum_non_letter";

    private static final String KEY_CATEGORY_EXPIRATION = "key_category_expiration";
    private static final String KEY_HISTORY = "key_history";
    private static final String KEY_EXPIRATION_TIMEOUT = "key_expiration_timeout";
    private static final String KEY_EXPIRATION_STATUS = "key_expiration_status";

    private static final String KEY_CATEGORY_LOCK_WIPE = "key_category_lock_wipe";
    private static final String KEY_MAX_TIME_SCREEN_LOCK = "key_max_time_screen_lock";
    private static final String KEY_MAX_FAILS_BEFORE_WIPE = "key_max_fails_before_wipe";
    private static final String KEY_LOCK_SCREEN = "key_lock_screen";
    private static final String KEY_WIPE_DATA = "key_wipe_data";
    private static final String KEY_WIP_DATA_ALL = "key_wipe_data_all";

    private static final String KEY_CATEGORY_ENCRYPTION = "key_category_encryption";
    private static final String KEY_REQUIRE_ENCRYPTION = "key_require_encryption";
    private static final String KEY_ACTIVATE_ENCRYPTION = "key_activate_encryption";

    // DevicePolicyManager 设备管理器，比如锁屏、恢复出厂设置、设置密码、强制清除密码，修改密码、设置屏幕灯光渐暗时间间隔等操作
    DevicePolicyManager mDPM;
    ComponentName mDeviceAdminSample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDPM = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        mDeviceAdminSample = new ComponentName(this, DeviceAdminSampleReceiver.class);
    }

    /**
     * 为PreferenceActivity加载Headers
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.device_admin_headers, target);
    }

    /**
     * 判断设备管理器是否被激活（即我们是否在设备管理器激活界面点击了激活按钮）
     */
    private boolean isActiveAdmin() {
        return mDPM.isAdminActive(mDeviceAdminSample);
    }

    /**
     * 验证fragment，防止PreferenceActivity在exported时被恶意攻击，此方法必须重写，否则会报错
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return GeneralFragment.class.getName().equals(fragmentName)
                || QualityFragment.class.getName().equals(fragmentName)
                || ExpirationFragment.class.getName().equals(fragmentName)
                || LockWipeFragment.class.getName().equals(fragmentName)
                || EncryptionFragment.class.getName().equals(fragmentName);
    }

    /**
     * 这里先定义了一个基类AdminSampleFragment，它做了两件事：
     * 1.提供context实例变量和DevicePolicyManager对象
     * 2.针对多个fragment中存在的set password按钮的点击事件进行处理
     */
    public static class AdminSampleFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        protected DeviceAdminSample mActivity;
        protected DevicePolicyManager mDPM;
        protected ComponentName mDeviceAdminSample;
        protected boolean mAdminActive;

        private PreferenceScreen mSetPassword;
        private EditTextPreference mResetPassword;

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mActivity = (DeviceAdminSample) getActivity();
            mDPM = mActivity.mDPM;
            mDeviceAdminSample = mActivity.mDeviceAdminSample;
            mAdminActive = mActivity.isActiveAdmin();

            mResetPassword = (EditTextPreference) findPreference(KEY_RESET_PASSWORD);
            mSetPassword = (PreferenceScreen) findPreference(KEY_SET_PASSWORD);

            if (mResetPassword != null) {
                mResetPassword.setOnPreferenceChangeListener(this);
            }

            if (mSetPassword != null) {
                mSetPassword.setOnPreferenceClickListener(this);
            }

        }

        /**
         * 根据Fragment的生命周期，当我们从激活界面返回到Fragment主界面时，会重新调用此方法，
         * 我们在此方法中根据设备管理器的激活状态来决定复选框的状态
         */
        @Override
        public void onResume() {
            super.onResume();
            mAdminActive = mActivity.isActiveAdmin();
            reloadSummaries();
            if (mResetPassword != null) {
                mResetPassword.setEnabled(mAdminActive);
            }
        }

        /**
         * 实时显示内容变更的信息
         */
        protected void reloadSummaries() {
            if (mSetPassword != null) {
                if (mAdminActive) {
                    boolean sufficient = mDPM.isActivePasswordSufficient();
                    mSetPassword.setSummary(sufficient ?
                            R.string.password_sufficient : R.string.password_insufficient);
                } else {
                    mSetPassword.setSummary(null);
                }
            }
        }

        protected void postReloadSummaries() {
            getView().post(new Runnable() {
                @Override
                public void run() {
                    reloadSummaries();
                }
            });
        }

        /**
         * https://blog.google/topics/connected-workspaces/keeping-android-safe-security-enhancements-nougat/
         * 7.0减少设备管理器的权利，这里重置密码需7.0以下
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            if (mResetPassword != null && preference == mResetPassword) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    doResetPassword((String) o);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (mSetPassword != null && preference == mSetPassword) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                startActivity(intent);
                return true;
            }
            return false;
        }

        /**
         * 避免自动化测试
         * 此方法很危险，请谨慎使用
         */
        private void doResetPassword(String newPassword) {
            if (alertIfMonkey(mActivity, R.string.monkey_reset_password)) {
                return;
            }
            mDPM.resetPassword(newPassword, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            String message = mActivity.getString(R.string.reset_password_warning, newPassword);
            builder.setMessage(message);
            builder.setPositiveButton(R.string.reset_password_ok, null);
            builder.show();
        }

        protected String localGlobalSummary(Object local, Object global) {
            return getString(R.string.status_local_global, local, global);
        }
    }

    /**
     * 常规设置
     * 重点是：setCameraDisabled与setKeyguardDisabledFeatures相对应配置
     */
    public static class GeneralFragment extends AdminSampleFragment
            implements Preference.OnPreferenceChangeListener {

        private CheckBoxPreference mEnableCheckbox;
        private CheckBoxPreference mDisableCameraCheckbox;
        private CheckBoxPreference mDisableKeyguardWidgetsCheckbox;
        private CheckBoxPreference mDisableKeyguardSecureCameraCheckbox;
        private CheckBoxPreference mDisableKeyguardNotificationCheckbox;
        private CheckBoxPreference mDisableKeyguardTrustAgentCheckbox;
        private CheckBoxPreference mDisableKeyguardUnredactedCheckbox;
        private EditTextPreference mTrustAgentComponent;
        private EditTextPreference mTrustAgentFeatures;
        private CheckBoxPreference mDisableKeyguardFingerprintCheckbox;
        private CheckBoxPreference mDisableKeyguardRemoteInputCheckbox;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.device_admin_general);
            mEnableCheckbox = (CheckBoxPreference) findPreference(KEY_ENABLE_ADMIN);
            mEnableCheckbox.setOnPreferenceChangeListener(this);

            mDisableCameraCheckbox = (CheckBoxPreference) findPreference(KEY_DISABLE_CAMERA);
            mDisableCameraCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardWidgetsCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_KEYGUARD_WIDGETS);
            mDisableKeyguardWidgetsCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardSecureCameraCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_KEYGUARD_SECURE_CAMERA);
            mDisableKeyguardSecureCameraCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardNotificationCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_NOTIFICATIONS);
            mDisableKeyguardNotificationCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardUnredactedCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_UNREDACTED);
            mDisableKeyguardUnredactedCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardFingerprintCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_FINGERPRINT);
            mDisableKeyguardFingerprintCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardRemoteInputCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_REMOTE_INPUT);
            mDisableKeyguardRemoteInputCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardTrustAgentCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_TRUST_AGENTS);
            mDisableKeyguardTrustAgentCheckbox.setOnPreferenceChangeListener(this);

            mTrustAgentComponent =
                    (EditTextPreference) findPreference(KEY_TRUST_AGENT_COMPONENT);
            mTrustAgentComponent.setOnPreferenceChangeListener(this);

            mTrustAgentFeatures =
                    (EditTextPreference) findPreference(KEY_TRUST_AGENT_FEATURES);
            mTrustAgentFeatures.setOnPreferenceChangeListener(this);
        }


        @Override
        public void onResume() {
            super.onResume();
            mEnableCheckbox.setChecked(mAdminActive);
            enableDeviceCapabilitiesArea(mAdminActive);

            if (mAdminActive) {
                //设置摄像头
                mDPM.setCameraDisabled(mDeviceAdminSample, mDisableCameraCheckbox.isChecked());
                //设置锁屏相关
                mDPM.setKeyguardDisabledFeatures(mDeviceAdminSample, createKeyguardDisabledFlag());
                reloadSummaries();
            }
        }

        /**
         * 在DevicePolicyManager setKeyguardDisabledFeatures设置相对应的配置
         */
        int createKeyguardDisabledFlag() {
            int flags = DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE;
            flags |= mDisableKeyguardWidgetsCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL : 0;
            flags |= mDisableKeyguardSecureCameraCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA : 0;
            flags |= mDisableKeyguardNotificationCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS : 0;
            flags |= mDisableKeyguardUnredactedCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS : 0;
            flags |= mDisableKeyguardTrustAgentCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS : 0;
            flags |= mDisableKeyguardFingerprintCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT : 0;
            flags |= mDisableKeyguardRemoteInputCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT : 0;
            return flags;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            if (super.onPreferenceChange(preference, o)) {
                return true;
            }
            if (preference == mEnableCheckbox) {
                boolean value = (Boolean) o;
                if (value != mAdminActive) {
                    if (value) {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                mActivity.getString(R.string.add_admin_extra_app_text));
                        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
                        // 返回false不更新复选框的状态直到我们真正激活设备管理器
                        return false;
                    } else {
                        mDPM.removeActiveAdmin(mDeviceAdminSample);
                        enableDeviceCapabilitiesArea(false);
                        mAdminActive = false;
                    }
                }
            } else if (preference == mDisableCameraCheckbox) {
                boolean value = (Boolean) o;
                mDPM.setCameraDisabled(mDeviceAdminSample, value);

                postReloadSummaries();
            } else if (preference == mDisableKeyguardWidgetsCheckbox
                    || preference == mDisableKeyguardSecureCameraCheckbox
                    || preference == mDisableKeyguardNotificationCheckbox
                    || preference == mDisableKeyguardUnredactedCheckbox
                    || preference == mDisableKeyguardTrustAgentCheckbox
                    || preference == mDisableKeyguardFingerprintCheckbox
                    || preference == mDisableKeyguardRemoteInputCheckbox
                    || preference == mTrustAgentComponent
                    || preference == mTrustAgentFeatures) {
                postUpdateDpmDisableFeatures();
                postReloadSummaries();
            }
            return true;
        }

        private void postUpdateDpmDisableFeatures() {
            getView().post(new Runnable() {
                @Override
                public void run() {
                    mDPM.setKeyguardDisabledFeatures(mDeviceAdminSample,
                            createKeyguardDisabledFlag());
                    String component = mTrustAgentComponent.getText();
                    if (component != null) {
                        ComponentName agent = ComponentName.unflattenFromString(component);
                        if (agent != null) {
                            String featureString = mTrustAgentFeatures.getText();
                            if (featureString != null) {
                                PersistableBundle bundle = new PersistableBundle();
                                bundle.putStringArray("features", featureString.split(","));
                                mDPM.setTrustAgentConfiguration(mDeviceAdminSample, agent, bundle);
                            }
                        } else {
                            Log.w(TAG, "Invalid component: " + component);
                        }
                    }
                }
            });
        }

        @Override
        protected void reloadSummaries() {
            super.reloadSummaries();
            String cameraSummary = getString(mDPM.getCameraDisabled(mDeviceAdminSample)
                    ? R.string.device_capabilities_disabled : R.string.device_capabilities_enabled);
            mDisableCameraCheckbox.setSummary(cameraSummary);

            int disabled = mDPM.getKeyguardDisabledFeatures(mDeviceAdminSample);

            String keyguardWidgetSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL) != 0 ?
                            R.string.device_capabilities_disabled : R.string.device_capabilities_enabled);
            mDisableKeyguardWidgetsCheckbox.setSummary(keyguardWidgetSummary);

            String keyguardSecureCameraSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA) != 0 ?
                            R.string.device_capabilities_disabled : R.string.device_capabilities_enabled);
            mDisableKeyguardSecureCameraCheckbox.setSummary(keyguardSecureCameraSummary);

            String keyguardSecureNotificationsSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS) != 0 ?
                            R.string.device_capabilities_disabled
                            : R.string.device_capabilities_enabled);
            mDisableKeyguardNotificationCheckbox.setSummary(keyguardSecureNotificationsSummary);

            String keyguardUnredactedSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS) != 0
                            ? R.string.device_capabilities_disabled
                            : R.string.device_capabilities_enabled);
            mDisableKeyguardUnredactedCheckbox.setSummary(keyguardUnredactedSummary);

            String keyguardEnableTrustAgentSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS) != 0 ?
                            R.string.device_capabilities_disabled
                            : R.string.device_capabilities_enabled);
            mDisableKeyguardTrustAgentCheckbox.setSummary(keyguardEnableTrustAgentSummary);

            String keyguardEnableFingerprintSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT) != 0 ?
                            R.string.device_capabilities_disabled
                            : R.string.device_capabilities_enabled);
            mDisableKeyguardFingerprintCheckbox.setSummary(keyguardEnableFingerprintSummary);

            String keyguardEnableRemoteInputSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT) != 0 ?
                            R.string.device_capabilities_disabled
                            : R.string.device_capabilities_enabled);
            mDisableKeyguardRemoteInputCheckbox.setSummary(keyguardEnableRemoteInputSummary);

            final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            final boolean trustDisabled =
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS) != 0;
            String component = prefs.getString(mTrustAgentComponent.getKey(), null);
            mTrustAgentComponent.setSummary(component);
            mTrustAgentComponent.setEnabled(trustDisabled);

            String features = prefs.getString(mTrustAgentFeatures.getKey(), null);
            mTrustAgentFeatures.setSummary(features);
            mTrustAgentFeatures.setEnabled(trustDisabled);
        }

        /**
         * 根据设备管理状态 是否可以设置设备功能（禁用/启动）
         */
        private void enableDeviceCapabilitiesArea(boolean enabled) {
            mDisableCameraCheckbox.setEnabled(enabled);
            mDisableKeyguardWidgetsCheckbox.setEnabled(enabled);
            mDisableKeyguardSecureCameraCheckbox.setEnabled(enabled);
            mDisableKeyguardNotificationCheckbox.setEnabled(enabled);
            mDisableKeyguardUnredactedCheckbox.setEnabled(enabled);
            mDisableKeyguardTrustAgentCheckbox.setEnabled(enabled);
            mDisableKeyguardFingerprintCheckbox.setEnabled(enabled);
            mDisableKeyguardRemoteInputCheckbox.setEnabled(enabled);
            mTrustAgentComponent.setEnabled(enabled);
            mTrustAgentFeatures.setEnabled(enabled);
        }

    }

    /**
     * 锁屏密码
     */
    public static class QualityFragment extends AdminSampleFragment
            implements Preference.OnPreferenceChangeListener {
        final static int[] mPasswordQualityValues = new int[]{
                DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED,
                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX,
                DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC,
                DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC,
                DevicePolicyManager.PASSWORD_QUALITY_COMPLEX
        };

        final static String[] mPasswordQualityValueStrings = new String[]{
                String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED),
                String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_SOMETHING),
                String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_NUMERIC),
                String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX),
                String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC),
                String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC),
                String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_COMPLEX)
        };

        private PreferenceCategory mQualityCategory;
        private ListPreference mPasswordQuality;
        private EditTextPreference mMinLength;
        private EditTextPreference mMinLetters;
        private EditTextPreference mMinNumeric;
        private EditTextPreference mMinLowerCase;
        private EditTextPreference mMinUpperCase;
        private EditTextPreference mMinSymbols;
        private EditTextPreference mMinNonLetter;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.device_admin_quality);

            mQualityCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_QUALITY);
            mPasswordQuality = (ListPreference) findPreference(KEY_QUALITY);
            mMinLength = (EditTextPreference) findPreference(KEY_MIN_LENGTH);
            mMinLetters = (EditTextPreference) findPreference(KEY_MIN_LETTERS);
            mMinNumeric = (EditTextPreference) findPreference(KEY_MIN_NUMERIC);
            mMinLowerCase = (EditTextPreference) findPreference(KEY_MIN_LOWER_CASE);
            mMinUpperCase = (EditTextPreference) findPreference(KEY_MIN_UPPER_CASE);
            mMinSymbols = (EditTextPreference) findPreference(KEY_MIN_SYMBOLS);
            mMinNonLetter = (EditTextPreference) findPreference(KEY_MIN_NON_LETTER);

            mPasswordQuality.setOnPreferenceChangeListener(this);
            mMinLength.setOnPreferenceChangeListener(this);
            mMinLetters.setOnPreferenceChangeListener(this);
            mMinNumeric.setOnPreferenceChangeListener(this);
            mMinLowerCase.setOnPreferenceChangeListener(this);
            mMinUpperCase.setOnPreferenceChangeListener(this);
            mMinSymbols.setOnPreferenceChangeListener(this);
            mMinNonLetter.setOnPreferenceChangeListener(this);

            mPasswordQuality.setEntryValues(mPasswordQualityValueStrings);
        }

        @Override
        public void onResume() {
            super.onResume();
            mQualityCategory.setEnabled(mAdminActive);
        }

        @Override
        protected void reloadSummaries() {
            super.reloadSummaries();

            int local, global;
            local = mDPM.getPasswordQuality(mDeviceAdminSample);
            global = mDPM.getPasswordQuality(null);
            mPasswordQuality.setSummary(
                    localGlobalSummary(qualityValueToString(local), qualityValueToString(global)));
            local = mDPM.getPasswordMinimumLength(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumLength(null);
            mMinLength.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumLetters(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumLetters(null);
            mMinLetters.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumNumeric(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumNumeric(null);
            mMinNumeric.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumLowerCase(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumLowerCase(null);
            mMinLowerCase.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumUpperCase(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumUpperCase(null);
            mMinUpperCase.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumSymbols(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumSymbols(null);
            mMinSymbols.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumNonLetter(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumNonLetter(null);
            mMinNonLetter.setSummary(localGlobalSummary(local, global));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (super.onPreferenceChange(preference, newValue)) {
                return true;
            }
            String valueString = (String) newValue;
            if (TextUtils.isEmpty(valueString)) {
                return false;
            }
            int value = 0;
            try {
                value = Integer.parseInt(valueString);
            } catch (NumberFormatException nfe) {
                String warning = mActivity.getString(R.string.number_format_warning, valueString);
                Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show();
            }
            if (preference == mPasswordQuality) {
                mDPM.setPasswordQuality(mDeviceAdminSample, value);
            } else if (preference == mMinLength) {
                mDPM.setPasswordMinimumLength(mDeviceAdminSample, value);
            } else if (preference == mMinLetters) {
                mDPM.setPasswordMinimumLetters(mDeviceAdminSample, value);
            } else if (preference == mMinNumeric) {
                mDPM.setPasswordMinimumNumeric(mDeviceAdminSample, value);
            } else if (preference == mMinLowerCase) {
                mDPM.setPasswordMinimumLowerCase(mDeviceAdminSample, value);
            } else if (preference == mMinUpperCase) {
                mDPM.setPasswordMinimumUpperCase(mDeviceAdminSample, value);
            } else if (preference == mMinSymbols) {
                mDPM.setPasswordMinimumSymbols(mDeviceAdminSample, value);
            } else if (preference == mMinNonLetter) {
                mDPM.setPasswordMinimumNonLetter(mDeviceAdminSample, value);
            }
            // Delay update because the change is only applied after exiting this method.
            postReloadSummaries();
            return true;

        }

        private String qualityValueToString(int quality) {
            for (int i = 0; i < mPasswordQualityValues.length; i++) {
                if (mPasswordQualityValues[i] == quality) {
                    String[] qualities =
                            mActivity.getResources().getStringArray(R.array.password_qualities);
                    return qualities[i];
                }
            }
            return "(0x" + Integer.toString(quality, 16) + ")";
        }
    }

    /**
     * 过期密码
     */
    public static class ExpirationFragment extends AdminSampleFragment
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        private PreferenceCategory mExpirationCategory;
        private EditTextPreference mHistory;
        private EditTextPreference mExpirationTimeout;
        private PreferenceScreen mExpirationStatus;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.device_admin_expiration);

            mExpirationCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_EXPIRATION);
            mHistory = (EditTextPreference) findPreference(KEY_HISTORY);
            mExpirationTimeout = (EditTextPreference) findPreference(KEY_EXPIRATION_TIMEOUT);
            mExpirationStatus = (PreferenceScreen) findPreference(KEY_EXPIRATION_STATUS);

            mHistory.setOnPreferenceChangeListener(this);
            mExpirationTimeout.setOnPreferenceChangeListener(this);
            mExpirationStatus.setOnPreferenceClickListener(this);

        }

        @Override
        public void onResume() {
            super.onResume();
            mExpirationCategory.setEnabled(mAdminActive);
        }

        @Override
        protected void reloadSummaries() {
            super.reloadSummaries();

            int local, global;
            local = mDPM.getPasswordHistoryLength(mDeviceAdminSample);
            global = mDPM.getPasswordHistoryLength(null);
            mHistory.setSummary(localGlobalSummary(local, global));

            long localLong, globalLong;
            localLong = mDPM.getPasswordExpirationTimeout(mDeviceAdminSample);
            globalLong = mDPM.getPasswordExpirationTimeout(null);
            mExpirationTimeout.setSummary(localGlobalSummary(
                    localLong / MS_PER_MINUTE, globalLong / MS_PER_MINUTE));

            String expirationStatus = getExpirationStatus();
            mExpirationStatus.setSummary(expirationStatus);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (super.onPreferenceChange(preference, newValue)) {
                return true;
            }
            String valueString = (String) newValue;
            if (TextUtils.isEmpty(valueString)) {
                return false;
            }
            int value = 0;
            try {
                value = Integer.parseInt(valueString);
            } catch (NumberFormatException nfe) {
                String warning = mActivity.getString(R.string.number_format_warning, valueString);
                Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show();
            }
            if (preference == mHistory) {
                mDPM.setPasswordHistoryLength(mDeviceAdminSample, value);
            } else if (preference == mExpirationTimeout) {
                mDPM.setPasswordExpirationTimeout(mDeviceAdminSample, value * MS_PER_MINUTE);
            }
            // 更新状态
            postReloadSummaries();
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (super.onPreferenceClick(preference)) {
                return true;
            }
            if (preference == mExpirationStatus) {
                String expirationStatus = getExpirationStatus();
                mExpirationStatus.setSummary(expirationStatus);
                return true;
            }
            return false;
        }

        private String getExpirationStatus() {
            // expirations are absolute;  convert to relative for display
            long localExpiration = mDPM.getPasswordExpiration(mDeviceAdminSample);
            long globalExpiration = mDPM.getPasswordExpiration(null);
            long now = System.currentTimeMillis();

            // local expiration
            String local;
            if (localExpiration == 0) {
                local = mActivity.getString(R.string.expiration_status_none);
            } else {
                localExpiration -= now;
                String dms = timeToDaysMinutesSeconds(mActivity, Math.abs(localExpiration));
                if (localExpiration >= 0) {
                    local = mActivity.getString(R.string.expiration_status_future, dms);
                } else {
                    local = mActivity.getString(R.string.expiration_status_past, dms);
                }
            }

            // global expiration
            String global;
            if (globalExpiration == 0) {
                global = mActivity.getString(R.string.expiration_status_none);
            } else {
                globalExpiration -= now;
                String dms = timeToDaysMinutesSeconds(mActivity, Math.abs(globalExpiration));
                if (globalExpiration >= 0) {
                    global = mActivity.getString(R.string.expiration_status_future, dms);
                } else {
                    global = mActivity.getString(R.string.expiration_status_past, dms);
                }
            }
            return mActivity.getString(R.string.status_local_global, local, global);
        }

    }

    /**
     * wipe和锁屏
     */
    public static class LockWipeFragment extends AdminSampleFragment
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        private PreferenceCategory mLockWipeCategory;
        private EditTextPreference mMaxTimeScreenLock;
        private EditTextPreference mMaxFailures;
        private PreferenceScreen mLockScreen;
        private PreferenceScreen mWipeData;
        private PreferenceScreen mWipeAppData;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.device_admin_lock_wipe);

            mLockWipeCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_LOCK_WIPE);
            mMaxTimeScreenLock = (EditTextPreference) findPreference(KEY_MAX_TIME_SCREEN_LOCK);
            mMaxFailures = (EditTextPreference) findPreference(KEY_MAX_FAILS_BEFORE_WIPE);
            mLockScreen = (PreferenceScreen) findPreference(KEY_LOCK_SCREEN);
            mWipeData = (PreferenceScreen) findPreference(KEY_WIPE_DATA);
            mWipeAppData = (PreferenceScreen) findPreference(KEY_WIP_DATA_ALL);

            mMaxTimeScreenLock.setOnPreferenceChangeListener(this);
            mMaxFailures.setOnPreferenceChangeListener(this);
            mLockScreen.setOnPreferenceClickListener(this);
            mWipeData.setOnPreferenceClickListener(this);
            mWipeAppData.setOnPreferenceClickListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            mLockWipeCategory.setEnabled(mAdminActive);
        }

        @Override
        protected void reloadSummaries() {
            super.reloadSummaries();

            long localLong, globalLong;
            localLong = mDPM.getMaximumTimeToLock(mDeviceAdminSample);
            globalLong = mDPM.getMaximumTimeToLock(null);
            mMaxTimeScreenLock.setSummary(localGlobalSummary(
                    localLong / MS_PER_MINUTE, globalLong / MS_PER_MINUTE));

            int local, global;
            local = mDPM.getMaximumFailedPasswordsForWipe(mDeviceAdminSample);
            global = mDPM.getMaximumFailedPasswordsForWipe(null);
            mMaxFailures.setSummary(localGlobalSummary(local, global));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (super.onPreferenceChange(preference, newValue)) {
                return true;
            }
            String valueString = (String) newValue;
            if (TextUtils.isEmpty(valueString)) {
                return false;
            }
            int value = 0;
            try {
                value = Integer.parseInt(valueString);
            } catch (NumberFormatException nfe) {
                String warning = mActivity.getString(R.string.number_format_warning, valueString);
                Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show();
            }
            if (preference == mMaxTimeScreenLock) {
                mDPM.setMaximumTimeToLock(mDeviceAdminSample, value * MS_PER_MINUTE);
            } else if (preference == mMaxFailures) {
                if (alertIfMonkey(mActivity, R.string.monkey_wipe_data)) {
                    return true;
                }
                mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdminSample, value);
            }
            // Delay update because the change is only applied after exiting this method.
            postReloadSummaries();
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (super.onPreferenceClick(preference)) {
                return true;
            }
            if (preference == mLockScreen) {
                if (alertIfMonkey(mActivity, R.string.monkey_lock_screen)) {
                    return true;
                }
                mDPM.lockNow();
                return true;
            } else if (preference == mWipeData || preference == mWipeAppData) {
                if (alertIfMonkey(mActivity, R.string.monkey_wipe_data)) {
                    return true;
                }
                promptForRealDeviceWipe(preference == mWipeAppData);
                return true;
            }
            return false;
        }

        /**
         * Wiping data is real, so we don't want it to be easy.  Show two alerts before wiping.
         */
        private void promptForRealDeviceWipe(final boolean wipeAllData) {
            final DeviceAdminSample activity = mActivity;

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.wipe_warning_first);
            builder.setPositiveButton(R.string.wipe_warning_first_ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            if (wipeAllData) {
                                builder.setMessage(R.string.wipe_warning_second_full);
                            } else {
                                builder.setMessage(R.string.wipe_warning_second);
                            }
                            builder.setPositiveButton(R.string.wipe_warning_second_ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            boolean stillActive = mActivity.isActiveAdmin();
                                            if (stillActive) {
                                                mDPM.wipeData(wipeAllData
                                                        ? DevicePolicyManager.WIPE_EXTERNAL_STORAGE : 0);
                                            }
                                        }
                                    });
                            builder.setNegativeButton(R.string.wipe_warning_second_no, null);
                            builder.show();
                        }
                    });
            builder.setNegativeButton(R.string.wipe_warning_first_no, null);
            builder.show();
        }
    }

    /**
     * 加密
     */
    public static class EncryptionFragment extends AdminSampleFragment
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        private PreferenceCategory mEncryptionCategory;
        private CheckBoxPreference mRequireEncryption;
        private PreferenceScreen mActivateEncryption;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.device_admin_encryption);

            mEncryptionCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_ENCRYPTION);
            mRequireEncryption = (CheckBoxPreference) findPreference(KEY_REQUIRE_ENCRYPTION);
            mActivateEncryption = (PreferenceScreen) findPreference(KEY_ACTIVATE_ENCRYPTION);

            mRequireEncryption.setOnPreferenceChangeListener(this);
            mActivateEncryption.setOnPreferenceClickListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            mEncryptionCategory.setEnabled(mAdminActive);
            mRequireEncryption.setChecked(mDPM.getStorageEncryption(mDeviceAdminSample));
        }

        /**
         * Update the summaries of each item to show the local setting and the global setting.
         */
        @Override
        protected void reloadSummaries() {
            super.reloadSummaries();

            boolean local, global;
            local = mDPM.getStorageEncryption(mDeviceAdminSample);
            global = mDPM.getStorageEncryption(null);
            mRequireEncryption.setSummary(localGlobalSummary(local, global));

            int deviceStatusCode = mDPM.getStorageEncryptionStatus();
            String deviceStatus = statusCodeToString(deviceStatusCode);
            String status = mActivity.getString(R.string.status_device_encryption, deviceStatus);
            mActivateEncryption.setSummary(status);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (super.onPreferenceChange(preference, newValue)) {
                return true;
            }
            if (preference == mRequireEncryption) {
                boolean newActive = (Boolean) newValue;
                mDPM.setStorageEncryption(mDeviceAdminSample, newActive);
                // Delay update because the change is only applied after exiting this method.
                postReloadSummaries();
                return true;
            }
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (super.onPreferenceClick(preference)) {
                return true;
            }
            if (preference == mActivateEncryption) {
                if (alertIfMonkey(mActivity, R.string.monkey_encryption)) {
                    return true;
                }
                // Check to see if encryption is even supported on this device (it's optional).
                if (mDPM.getStorageEncryptionStatus() ==
                        DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(R.string.encryption_not_supported);
                    builder.setPositiveButton(R.string.encryption_not_supported_ok, null);
                    builder.show();
                    return true;
                }
                // Launch the activity to activate encryption.  May or may not return!
                Intent intent = new Intent(DevicePolicyManager.ACTION_START_ENCRYPTION);
                startActivityForResult(intent, REQUEST_CODE_START_ENCRYPTION);
                return true;
            }
            return false;
        }

        private String statusCodeToString(int newStatusCode) {
            int newStatus = R.string.encryption_status_unknown;
            switch (newStatusCode) {
                case DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED:
                    newStatus = R.string.encryption_status_unsupported;
                    break;
                case DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE:
                    newStatus = R.string.encryption_status_inactive;
                    break;
                case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING:
                    newStatus = R.string.encryption_status_activating;
                    break;
                case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE:
                    newStatus = R.string.encryption_status_active;
                    break;
            }
            return mActivity.getString(newStatus);
        }
    }

    /**
     * Simple converter used for long expiration times reported in mSec.
     */
    private static String timeToDaysMinutesSeconds(Context context, long time) {
        long days = time / MS_PER_DAY;
        long hours = (time / MS_PER_HOUR) % 24;
        long minutes = (time / MS_PER_MINUTE) % 60;
        return context.getString(R.string.status_days_hours_minutes, days, hours, minutes);
    }

    /**
     * 如果用户是通过monkey(压测)发布警报和通知调用者，这可以防止自动测试框架进行危险的操作
     */
    private static boolean alertIfMonkey(Context context, int stringId) {
        if (ActivityManager.isUserAMonkey()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(stringId);
            builder.setPositiveButton(R.string.monkey_ok, null);
            builder.show();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 该类继承自 BroadcastReceiver 。 从源码可以看到，其实就是实现了一个OnReceive方法，该方法中根据不同的Action，
     * 执行相应的操作。 比如，如果激活成功，那么Action就是ACTION_DEVICE_ADMIN_ENABLED， 据此调用 onEnabled 方法
     */
    public static class DeviceAdminSampleReceiver extends DeviceAdminReceiver {
        void showToast(Context context, String msg) {
            String status = context.getString(R.string.admin_receiver_status, msg);
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == ACTION_DEVICE_ADMIN_DISABLE_REQUESTED) {
                abortBroadcast();
            }
            super.onReceive(context, intent);
        }

        @Override
        public void onEnabled(Context context, Intent intent) {
            super.onEnabled(context, intent);
            showToast(context, context.getString(R.string.admin_receiver_status_enabled));
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            return context.getString(R.string.admin_receiver_status_disable_warning);
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
            super.onDisabled(context, intent);
            showToast(context, context.getString(R.string.admin_receiver_status_disabled));
        }

        @Override
        public void onPasswordChanged(Context context, Intent intent, UserHandle user) {
            super.onPasswordChanged(context, intent, user);
            showToast(context, context.getString(R.string.admin_receiver_status_pw_changed));
        }

        /**
         * 当密码过期时，通过DevicePolicyManager的getPasswordExpiration方法获取过期的时间与当前时间对比。
         */
        @Override
        public void onPasswordExpiring(Context context, Intent intent, UserHandle user) {
            super.onPasswordExpiring(context, intent, user);
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(
                    Context.DEVICE_POLICY_SERVICE);
            long expr = dpm.getPasswordExpiration(
                    new ComponentName(context, DeviceAdminSampleReceiver.class));
            long delta = expr - System.currentTimeMillis();
            boolean expired = delta < 0L;
            String message = context.getString(expired ?
                    R.string.expiration_status_past : R.string.expiration_status_future);
            showToast(context, message);
        }

        @Override
        public void onPasswordFailed(Context context, Intent intent, UserHandle user) {
            super.onPasswordFailed(context, intent, user);
            showToast(context, context.getString(R.string.admin_receiver_status_pw_failed));
        }

        @Override
        public void onPasswordSucceeded(Context context, Intent intent, UserHandle user) {
            super.onPasswordSucceeded(context, intent, user);
            showToast(context, context.getString(R.string.admin_receiver_status_pw_succeeded));
        }
    }
}
