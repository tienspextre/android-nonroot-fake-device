package com.sec.deviceid;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    // Core Identifiers
    private EditText etAndroidId;
    private EditText etImei;
    private EditText etSerial;

    // Carrier & SIM
    private EditText etCarrierName;
    private EditText etCarrierNumeric;
    private EditText etPhoneNumber;
    private EditText etIccid;
    private EditText etImsi;

    // Mock Location
    private EditText etLatitude;
    private EditText etLongitude;
    private LocationManager locationManager;
    private boolean isMockingGps = false;

    // Display DPI
    private EditText etDpi;

    // Device Preset
    private TextView tvCurrentModel;

    // Wipe apps
    private EditText etCustomPackage;
    private final List<CheckBox> appCheckboxes = new ArrayList<>();

    // CPU & zRAM
    private TextView tvCpuStatus;

    private final SecureRandom random = new SecureRandom();
    private SharedPreferences prefs;

    private static final String[][] TARGET_APPS = {
        {"Trình duyệt Chrome", "com.android.chrome"},
        {"Facebook", "com.facebook.katana"},
        {"Facebook Lite", "com.facebook.lite"},
        {"TikTok", "com.zhiliaoapp.musically"},
        {"TikTok Lite", "com.zhiliaoapp.musically.go"},
        {"Shopee", "com.shopee.vn"},
        {"Zalo", "com.zing.zalo"},
        {"Telegram", "org.telegram.messenger"}
    };

    // Device presets: {Name, Model, Brand, Manufacturer, Board, Product, ImeiPrefix}
    private static final String[][] DEVICE_PRESETS = {
        {"Samsung Galaxy S10+", "SM-G975F", "samsung", "samsung", "universal9820", "beyond2ltexx", "35848507"},
        {"Google Pixel 4 XL", "Pixel 4 XL", "google", "Google", "coral", "coral", "35678908"},
        {"Samsung Galaxy Note 9", "SM-N960F", "samsung", "samsung", "universal9810", "crownltexx", "86043204"},
        {"Xiaomi Redmi Note 8", "Redmi Note 8", "Xiaomi", "Xiaomi", "ginkgo", "ginkgo", "86423903"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Device ID Changer Pro");
        prefs = getSharedPreferences("device_profiles", MODE_PRIVATE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#101418"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 45, 35, 60);

        // Header Title
        TextView tvTitle = new TextView(this);
        tvTitle.setText("Thiết Lập ID Thiết Bị Pro");
        tvTitle.setTextSize(22);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#00E676"));
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(tvTitle);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("✓ Quyền Hệ Thống (UID 1000) • 100% Không Cần Root • Farm Tool");
        tvSubtitle.setTextSize(12);
        tvSubtitle.setTextColor(Color.parseColor("#69F0AE"));
        tvSubtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        tvSubtitle.setPadding(0, 8, 0, 25);
        root.addView(tvSubtitle);

        // ================= SECTION 1: ANDROID ID =================
        addSectionHeader(root, "1. ANDROID ID (SSAID TOÀN HỆ THỐNG)");
        etAndroidId = createEditText();
        root.addView(etAndroidId);

        LinearLayout rowIdBtns = new LinearLayout(this);
        rowIdBtns.setOrientation(LinearLayout.HORIZONTAL);
        rowIdBtns.setPadding(0, 10, 0, 20);
        Button btnRandomId = createButton("🎲 Sinh Ngẫu Nhiên", "#1976D2");
        btnRandomId.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { etAndroidId.setText(generateRandomHex(16)); }
        });
        rowIdBtns.addView(btnRandomId, createWeightParams());
        rowIdBtns.addView(createSpacer(12));
        Button btnApplyId = createButton("💾 Áp Dụng ID", "#2E7D32");
        btnApplyId.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyAndroidId(); }
        });
        rowIdBtns.addView(btnApplyId, createWeightParams());
        root.addView(rowIdBtns);

        // ================= SECTION 2: IMEI & MEID =================
        addSectionHeader(root, "2. IMEI & MEID (PHẦN CỨNG TOÀN HỆ THỐNG)");
        etImei = createEditText();
        etImei.setInputType(InputType.TYPE_CLASS_NUMBER);
        root.addView(etImei);

        LinearLayout rowImeiBtns = new LinearLayout(this);
        rowImeiBtns.setOrientation(LinearLayout.HORIZONTAL);
        rowImeiBtns.setPadding(0, 10, 0, 10);
        Button btnRandomImei = createButton("🎲 Sinh IMEI (Luhn)", "#0288D1");
        btnRandomImei.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { etImei.setText(generateRandomImei()); }
        });
        rowImeiBtns.addView(btnRandomImei, createWeightParams());
        rowImeiBtns.addView(createSpacer(12));
        Button btnApplyImei = createButton("💾 Áp Dụng IMEI", "#2E7D32");
        btnApplyImei.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyImei(); }
        });
        rowImeiBtns.addView(btnApplyImei, createWeightParams());
        root.addView(rowImeiBtns);

        Button btnResetImei = createButton("↺ Khôi Phục IMEI Gốc", "#C62828");
        btnResetImei.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { resetImei(); }
        });
        LinearLayout.LayoutParams pReset = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pReset.setMargins(0, 0, 0, 20);
        root.addView(btnResetImei, pReset);

        // ================= SECTION 3: SERIAL NUMBER =================
        addSectionHeader(root, "3. MÃ SERIAL PHẦN CỨNG (SERIAL NUMBER)");
        etSerial = createEditText();
        root.addView(etSerial);

        LinearLayout rowSerialBtns = new LinearLayout(this);
        rowSerialBtns.setOrientation(LinearLayout.HORIZONTAL);
        rowSerialBtns.setPadding(0, 10, 0, 20);
        Button btnRandomSerial = createButton("🎲 Sinh Serial Mới", "#00838F");
        btnRandomSerial.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { etSerial.setText(generateRandomAlphanumeric(10)); }
        });
        rowSerialBtns.addView(btnRandomSerial, createWeightParams());
        rowSerialBtns.addView(createSpacer(12));
        Button btnApplySerial = createButton("💾 Áp Dụng Serial", "#2E7D32");
        btnApplySerial.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applySerial(); }
        });
        rowSerialBtns.addView(btnApplySerial, createWeightParams());
        root.addView(rowSerialBtns);

        // ================= SECTION 4: DEVICE MODEL PRESET =================
        addSectionHeader(root, "4. GIẢ LẬP DÒNG MÁY (DEVICE MODEL PRESETS)");
        tvCurrentModel = new TextView(this);
        tvCurrentModel.setText("Dòng máy hiện tại: " + android.os.Build.MODEL + " (" + android.os.Build.BRAND + ")");
        tvCurrentModel.setTextColor(Color.LTGRAY);
        tvCurrentModel.setTextSize(13);
        tvCurrentModel.setPadding(0, 0, 0, 10);
        root.addView(tvCurrentModel);

        LinearLayout rowPresets1 = new LinearLayout(this);
        rowPresets1.setOrientation(LinearLayout.HORIZONTAL);
        rowPresets1.setPadding(0, 0, 0, 8);
        Button btnPreset1 = createButton("📱 Galaxy S10+", "#37474F");
        btnPreset1.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { selectPreset(0); }
        });
        rowPresets1.addView(btnPreset1, createWeightParams());
        rowPresets1.addView(createSpacer(10));
        Button btnPreset2 = createButton("📱 Pixel 4 XL", "#37474F");
        btnPreset2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { selectPreset(1); }
        });
        rowPresets1.addView(btnPreset2, createWeightParams());
        root.addView(rowPresets1);

        LinearLayout rowPresets2 = new LinearLayout(this);
        rowPresets2.setOrientation(LinearLayout.HORIZONTAL);
        rowPresets2.setPadding(0, 0, 0, 20);
        Button btnPreset3 = createButton("📱 Galaxy Note 9", "#37474F");
        btnPreset3.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { selectPreset(2); }
        });
        rowPresets2.addView(btnPreset3, createWeightParams());
        rowPresets2.addView(createSpacer(10));
        Button btnPreset4 = createButton("📱 Redmi Note 8", "#37474F");
        btnPreset4.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { selectPreset(3); }
        });
        rowPresets2.addView(btnPreset4, createWeightParams());
        root.addView(rowPresets2);

        // ================= SECTION 5: CARRIER & SIM SPOOFER =================
        addSectionHeader(root, "5. NHÀ MẠNG & SIM ẢO (CARRIER & SIM SPOOFER)");
        LinearLayout carrierCard = new LinearLayout(this);
        carrierCard.setOrientation(LinearLayout.VERTICAL);
        carrierCard.setBackgroundColor(Color.parseColor("#182128"));
        carrierCard.setPadding(20, 15, 20, 15);

        LinearLayout rowCarrierPills = new LinearLayout(this);
        rowCarrierPills.setOrientation(LinearLayout.HORIZONTAL);
        rowCarrierPills.setPadding(0, 0, 0, 12);
        Button btnViettel = createButton("Viettel", "#B71C1C");
        btnViettel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { selectCarrier("Viettel", "45204", "098"); }
        });
        rowCarrierPills.addView(btnViettel, createWeightParams());
        rowCarrierPills.addView(createSpacer(8));
        Button btnVina = createButton("VinaPhone", "#0D47A1");
        btnVina.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { selectCarrier("VN VINAPHONE", "45202", "091"); }
        });
        rowCarrierPills.addView(btnVina, createWeightParams());
        rowCarrierPills.addView(createSpacer(8));
        Button btnMobi = createButton("MobiFone", "#E65100");
        btnMobi.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { selectCarrier("MobiFone", "45201", "090"); }
        });
        rowCarrierPills.addView(btnMobi, createWeightParams());
        carrierCard.addView(rowCarrierPills);

        addInputLabel(carrierCard, "Tên nhà mạng & Mã mạng (MCC/MNC):");
        LinearLayout rowCarrierInputs = new LinearLayout(this);
        rowCarrierInputs.setOrientation(LinearLayout.HORIZONTAL);
        etCarrierName = createEditText();
        etCarrierName.setHint("Tên nhà mạng");
        rowCarrierInputs.addView(etCarrierName, createWeightParams());
        rowCarrierInputs.addView(createSpacer(10));
        etCarrierNumeric = createEditText();
        etCarrierNumeric.setHint("MCC/MNC (vd 45204)");
        rowCarrierInputs.addView(etCarrierNumeric, createWeightParams());
        carrierCard.addView(rowCarrierInputs);

        addInputLabel(carrierCard, "Số điện thoại ảo (Line 1 Number):");
        etPhoneNumber = createEditText();
        etPhoneNumber.setHint("vd 0984123456");
        carrierCard.addView(etPhoneNumber);

        addInputLabel(carrierCard, "Số Serial thẻ SIM (ICCID 19 số):");
        etIccid = createEditText();
        etIccid.setHint("vd 8984040012345678901");
        carrierCard.addView(etIccid);

        addInputLabel(carrierCard, "Mã thuê bao (IMSI 15 số):");
        etImsi = createEditText();
        etImsi.setHint("vd 452041234567890");
        carrierCard.addView(etImsi);

        LinearLayout rowSimActions = new LinearLayout(this);
        rowSimActions.setOrientation(LinearLayout.HORIZONTAL);
        rowSimActions.setPadding(0, 15, 0, 0);
        Button btnRandomSim = createButton("🎲 Sinh Bộ SIM", "#00897B");
        btnRandomSim.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { randomizeSimPackage(); }
        });
        rowSimActions.addView(btnRandomSim, createWeightParams());
        rowSimActions.addView(createSpacer(10));
        Button btnApplySim = createButton("💾 Áp Dụng SIM & Mạng", "#2E7D32");
        btnApplySim.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyCarrierAndSim(); }
        });
        rowSimActions.addView(btnApplySim, createWeightParams());
        carrierCard.addView(rowSimActions);

        root.addView(carrierCard);
        root.addView(createSpacer(20));

        // ================= SECTION 6: FAKE GPS / MOCK LOCATION =================
        addSectionHeader(root, "6. ĐỊNH VỊ VỊ TRÍ ẢO (FAKE GPS / MOCK LOCATION)");
        LinearLayout gpsCard = new LinearLayout(this);
        gpsCard.setOrientation(LinearLayout.VERTICAL);
        gpsCard.setBackgroundColor(Color.parseColor("#182128"));
        gpsCard.setPadding(20, 15, 20, 15);

        LinearLayout rowCities = new LinearLayout(this);
        rowCities.setOrientation(LinearLayout.HORIZONTAL);
        rowCities.setPadding(0, 0, 0, 12);
        Button btnHanoi = createButton("Hà Nội", "#455A64");
        btnHanoi.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setGpsInputs(21.028511, 105.854167); }
        });
        rowCities.addView(btnHanoi, createWeightParams());
        rowCities.addView(createSpacer(6));
        Button btnHcm = createButton("TP.HCM", "#455A64");
        btnHcm.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setGpsInputs(10.823099, 106.629664); }
        });
        rowCities.addView(btnHcm, createWeightParams());
        rowCities.addView(createSpacer(6));
        Button btnDanang = createButton("Đà Nẵng", "#455A64");
        btnDanang.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setGpsInputs(16.054407, 108.202167); }
        });
        rowCities.addView(btnDanang, createWeightParams());
        rowCities.addView(createSpacer(6));
        Button btnCantho = createButton("Cần Thơ", "#455A64");
        btnCantho.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setGpsInputs(10.045162, 105.746857); }
        });
        rowCities.addView(btnCantho, createWeightParams());
        gpsCard.addView(rowCities);

        LinearLayout rowCoords = new LinearLayout(this);
        rowCoords.setOrientation(LinearLayout.HORIZONTAL);
        etLatitude = createEditText();
        etLatitude.setHint("Vĩ độ (Lat)");
        etLatitude.setText("21.028511");
        rowCoords.addView(etLatitude, createWeightParams());
        rowCoords.addView(createSpacer(10));
        etLongitude = createEditText();
        etLongitude.setHint("Kinh độ (Lng)");
        etLongitude.setText("105.854167");
        rowCoords.addView(etLongitude, createWeightParams());
        gpsCard.addView(rowCoords);

        LinearLayout rowGpsActions = new LinearLayout(this);
        rowGpsActions.setOrientation(LinearLayout.HORIZONTAL);
        rowGpsActions.setPadding(0, 12, 0, 0);
        Button btnStartGps = createButton("📍 BẬT VỊ TRÍ ẢO", "#00897B");
        btnStartGps.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startMockLocation(); }
        });
        rowGpsActions.addView(btnStartGps, createWeightParams());
        rowGpsActions.addView(createSpacer(10));
        Button btnStopGps = createButton("🛑 DỪNG VỊ TRÍ ẢO", "#C62828");
        btnStopGps.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { stopMockLocation(); }
        });
        rowGpsActions.addView(btnStopGps, createWeightParams());
        gpsCard.addView(rowGpsActions);

        root.addView(gpsCard);
        root.addView(createSpacer(20));

        // ================= SECTION 7: DISPLAY & DPI SPOOFER =================
        addSectionHeader(root, "7. MÀN HÌNH & MẬT ĐỘ HIỂN THỊ (DPI SPOOFER)");
        LinearLayout dpiCard = new LinearLayout(this);
        dpiCard.setOrientation(LinearLayout.VERTICAL);
        dpiCard.setBackgroundColor(Color.parseColor("#182128"));
        dpiCard.setPadding(20, 15, 20, 15);

        LinearLayout rowDpiPresets = new LinearLayout(this);
        rowDpiPresets.setOrientation(LinearLayout.HORIZONTAL);
        rowDpiPresets.setPadding(0, 0, 0, 12);
        Button btnDpi320 = createButton("320 DPI (HD)", "#37474F");
        btnDpi320.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyDpi(320); }
        });
        rowDpiPresets.addView(btnDpi320, createWeightParams());
        rowDpiPresets.addView(createSpacer(8));
        Button btnDpi420 = createButton("420 DPI (FHD)", "#37474F");
        btnDpi420.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyDpi(420); }
        });
        rowDpiPresets.addView(btnDpi420, createWeightParams());
        rowDpiPresets.addView(createSpacer(8));
        Button btnDpi560 = createButton("560 DPI (2K)", "#37474F");
        btnDpi560.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyDpi(560); }
        });
        rowDpiPresets.addView(btnDpi560, createWeightParams());
        dpiCard.addView(rowDpiPresets);

        LinearLayout rowCustomDpi = new LinearLayout(this);
        rowCustomDpi.setOrientation(LinearLayout.HORIZONTAL);
        etDpi = createEditText();
        etDpi.setHint("Nhập DPI tùy chọn");
        etDpi.setInputType(InputType.TYPE_CLASS_NUMBER);
        rowCustomDpi.addView(etDpi, createWeightParams());
        rowCustomDpi.addView(createSpacer(10));
        Button btnApplyCustomDpi = createButton("💾 Đổi DPI", "#2E7D32");
        btnApplyCustomDpi.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    int d = Integer.parseInt(etDpi.getText().toString().trim());
                    applyDpi(d);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "DPI không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rowCustomDpi.addView(btnApplyCustomDpi, createWeightParams());
        dpiCard.addView(rowCustomDpi);

        Button btnResetDpi = createButton("↺ Khôi Phục DPI Gốc", "#78909C");
        btnResetDpi.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { resetDpi(); }
        });
        LinearLayout.LayoutParams pDpiReset = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pDpiReset.setMargins(0, 10, 0, 0);
        dpiCard.addView(btnResetDpi, pDpiReset);

        root.addView(dpiCard);
        root.addView(createSpacer(20));

        // ================= SECTION 8: WIPE APP DATA =================
        addSectionHeader(root, "8. LÀM SẠCH ỨNG DỤNG (WIPE DATA / SESSION)");
        TextView tvWipeDesc = new TextView(this);
        tvWipeDesc.setText("Chọn các app cần xóa sạch dữ liệu (như mới tải từ CH Play):");
        tvWipeDesc.setTextSize(12);
        tvWipeDesc.setTextColor(Color.LTGRAY);
        tvWipeDesc.setPadding(0, 0, 0, 10);
        root.addView(tvWipeDesc);

        LinearLayout appListContainer = new LinearLayout(this);
        appListContainer.setOrientation(LinearLayout.VERTICAL);
        appListContainer.setBackgroundColor(Color.parseColor("#182128"));
        appListContainer.setPadding(20, 15, 20, 15);

        for (String[] appInfo : TARGET_APPS) {
            CheckBox cb = new CheckBox(this);
            cb.setText(appInfo[0] + " (" + appInfo[1] + ")");
            cb.setTextColor(Color.WHITE);
            cb.setTag(appInfo[1]);
            appListContainer.addView(cb);
            appCheckboxes.add(cb);
        }

        etCustomPackage = createEditText();
        etCustomPackage.setHint("Hoặc nhập package name khác (tùy chọn)");
        etCustomPackage.setHintTextColor(Color.GRAY);
        etCustomPackage.setTextSize(13);
        appListContainer.addView(etCustomPackage);
        root.addView(appListContainer);

        Button btnWipeSelected = createButton("🧹 XÓA DỮ LIỆU CÁC APP ĐÃ CHỌN", "#E65100");
        btnWipeSelected.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { wipeSelectedApps(); }
        });
        LinearLayout.LayoutParams wipeParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wipeParams.setMargins(0, 12, 0, 20);
        root.addView(btnWipeSelected, wipeParams);

        // ================= SECTION 9: GOOGLE ADS ID =================
        addSectionHeader(root, "9. ĐỊNH DANH QUẢNG CÁO GOOGLE (GAID)");
        Button btnResetGaid = createButton("⚡ ĐẶT LẠI MÃ QUẢNG CÁO GOOGLE (GAID)", "#5C6BC0");
        btnResetGaid.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { resetGaid(); }
        });
        LinearLayout.LayoutParams gaidParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        gaidParams.setMargins(0, 8, 0, 20);
        root.addView(btnResetGaid, gaidParams);

        // ================= SECTION 10: PROFILE MANAGER =================
        addSectionHeader(root, "10. QUẢN LÝ HỒ SƠ ĐA TÀI KHOẢN (PROFILE MANAGER)");
        LinearLayout rowProfiles = new LinearLayout(this);
        rowProfiles.setOrientation(LinearLayout.HORIZONTAL);
        rowProfiles.setPadding(0, 6, 0, 10);
        Button btnSaveP1 = createButton("💾 Lưu Hồ Sơ 1", "#00695C");
        btnSaveP1.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { saveProfile(1); }
        });
        rowProfiles.addView(btnSaveP1, createWeightParams());
        rowProfiles.addView(createSpacer(8));
        Button btnLoadP1 = createButton("📂 Nạp Hồ Sơ 1", "#00897B");
        btnLoadP1.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { loadProfile(1); }
        });
        rowProfiles.addView(btnLoadP1, createWeightParams());
        root.addView(rowProfiles);

        LinearLayout rowProfiles2 = new LinearLayout(this);
        rowProfiles2.setOrientation(LinearLayout.HORIZONTAL);
        rowProfiles2.setPadding(0, 0, 0, 10);
        Button btnSaveP2 = createButton("💾 Lưu Hồ Sơ 2", "#4527A0");
        btnSaveP2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { saveProfile(2); }
        });
        rowProfiles2.addView(btnSaveP2, createWeightParams());
        rowProfiles2.addView(createSpacer(8));
        Button btnLoadP2 = createButton("📂 Nạp Hồ Sơ 2", "#5E35B1");
        btnLoadP2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { loadProfile(2); }
        });
        rowProfiles2.addView(btnLoadP2, createWeightParams());
        root.addView(rowProfiles2);

        LinearLayout rowProfiles3 = new LinearLayout(this);
        rowProfiles3.setOrientation(LinearLayout.HORIZONTAL);
        rowProfiles3.setPadding(0, 0, 0, 25);
        Button btnSaveP3 = createButton("💾 Lưu Hồ Sơ 3", "#C2185B");
        btnSaveP3.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { saveProfile(3); }
        });
        rowProfiles3.addView(btnSaveP3, createWeightParams());
        rowProfiles3.addView(createSpacer(8));
        Button btnLoadP3 = createButton("📂 Nạp Hồ Sơ 3", "#D81B60");
        btnLoadP3.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { loadProfile(3); }
        });
        rowProfiles3.addView(btnLoadP3, createWeightParams());
        root.addView(rowProfiles3);

        // ================= SECTION 11: 1-CLICK ALL =================
        addSectionHeader(root, "11. ĐỔI MỚI TOÀN DIỆN 1-CHẠM (SUPER RANDOM ALL)");
        Button btnQuickAll = createButton("🚀 ĐỔI MỚI TOÀN BỘ VÀ LÀM SẠCH APP", "#D84315");
        btnQuickAll.setTextSize(16);
        btnQuickAll.setTypeface(null, Typeface.BOLD);
        btnQuickAll.setPadding(0, 35, 0, 35);
        btnQuickAll.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // 1. Android ID
                etAndroidId.setText(generateRandomHex(16));
                applyAndroidId();

                // 2. IMEI
                etImei.setText(generateRandomImei());
                applyImei();

                // 3. Serial
                etSerial.setText(generateRandomAlphanumeric(10));
                applySerial();

                // 4. Random Carrier & SIM
                randomizeSimPackage();
                applyCarrierAndSim();

                // 5. Random GPS (Vietnam bounding box: 10.5 - 21.0, 105.0 - 108.5)
                double rLat = 10.5 + (random.nextDouble() * 10.5);
                double rLng = 105.0 + (random.nextDouble() * 3.5);
                setGpsInputs(rLat, rLng);
                startMockLocation();

                // 7. Wipe Selected Apps
                wipeSelectedApps();

                Toast.makeText(MainActivity.this, "✓ ĐÃ ĐỔI MỚI TOÀN DIỆN VÀ LÀM SẠCH APP THÀNH CÔNG!", Toast.LENGTH_LONG).show();
            }
        });
        root.addView(btnQuickAll);

        // ================= SECTION 12: CPU TURBO & ZRAM =================
        addSectionHeader(root, "12. HIỆU NĂNG CPU & ZRAM TURBO");
        tvCpuStatus = new TextView(this);
        tvCpuStatus.setText("Đang tải thông số CPU & RAM...");
        tvCpuStatus.setTextSize(12);
        tvCpuStatus.setTextColor(Color.parseColor("#81D4FA"));
        tvCpuStatus.setPadding(0, 5, 0, 15);
        root.addView(tvCpuStatus);

        LinearLayout rowCpuBtns = new LinearLayout(this);
        rowCpuBtns.setOrientation(LinearLayout.HORIZONTAL);
        rowCpuBtns.setPadding(0, 0, 0, 15);

        Button btnBoostCpu = createButton("🚀 ÉP XUNG CPU TURBO", "#00C853");
        btnBoostCpu.setTypeface(null, Typeface.BOLD);
        btnBoostCpu.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyCpuTurbo(); }
        });
        rowCpuBtns.addView(btnBoostCpu, createWeightParams());
        rowCpuBtns.addView(createSpacer(8));

        Button btnEnableZram = createButton("💾 BẬT ZRAM (RAM ẢO)", "#0288D1");
        btnEnableZram.setTypeface(null, Typeface.BOLD);
        btnEnableZram.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyZram(); }
        });
        rowCpuBtns.addView(btnEnableZram, createWeightParams());
        root.addView(rowCpuBtns);

        scrollView.addView(root);
        setContentView(scrollView);

        loadCurrentValues();
        resetBattery();
    }

    private void addSectionHeader(LinearLayout root, String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(13);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.parseColor("#FFD54F"));
        tv.setPadding(0, 15, 0, 8);
        root.addView(tv);
    }

    private void addInputLabel(LinearLayout root, String label) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(11);
        tv.setTextColor(Color.parseColor("#B0BEC5"));
        tv.setPadding(0, 6, 0, 3);
        root.addView(tv);
    }

    private EditText createEditText() {
        EditText et = new EditText(this);
        et.setTextSize(14);
        et.setTextColor(Color.WHITE);
        et.setBackgroundColor(Color.parseColor("#263238"));
        et.setPadding(20, 20, 20, 20);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        return et;
    }

    private Button createButton(String text, String colorHex) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(12);
        b.setBackgroundColor(Color.parseColor(colorHex));
        b.setPadding(10, 16, 10, 16);
        return b;
    }

    private View createSpacer(int width) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(width, 1));
        return v;
    }

    private LinearLayout.LayoutParams createWeightParams() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
    }

    private void loadCurrentValues() {
        try {
            ContentResolver cr = getContentResolver();
            String currentId = Settings.Secure.getString(cr, Settings.Secure.ANDROID_ID);
            if (currentId != null) etAndroidId.setText(currentId);

            String fakeImei = Settings.System.getString(cr, "fake_imei");
            if (fakeImei != null && !fakeImei.isEmpty()) {
                etImei.setText(fakeImei);
            } else {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                try {
                    String devId = tm.getDeviceId();
                    if (devId != null) etImei.setText(devId);
                } catch (Exception ignored) {}
            }

            String fakeSerial = Settings.System.getString(cr, "fake_serial");
            if (fakeSerial != null && !fakeSerial.isEmpty()) {
                etSerial.setText(fakeSerial);
            } else {
                etSerial.setText(android.os.Build.SERIAL);
            }

            String fakeCarrier = Settings.System.getString(cr, "fake_carrier_name");
            if (fakeCarrier != null && !fakeCarrier.isEmpty()) {
                etCarrierName.setText(fakeCarrier);
            } else {
                etCarrierName.setText("Viettel");
            }

            String fakeNumeric = Settings.System.getString(cr, "fake_carrier_numeric");
            if (fakeNumeric != null && !fakeNumeric.isEmpty()) {
                etCarrierNumeric.setText(fakeNumeric);
            } else {
                etCarrierNumeric.setText("45204");
            }

            String fakePhone = Settings.System.getString(cr, "fake_phone_number");
            if (fakePhone != null && !fakePhone.isEmpty()) {
                etPhoneNumber.setText(fakePhone);
            } else {
                etPhoneNumber.setText("098" + (1000000 + random.nextInt(9000000)));
            }

            String fakeIccid = Settings.System.getString(cr, "fake_iccid");
            if (fakeIccid != null && !fakeIccid.isEmpty()) {
                etIccid.setText(fakeIccid);
            } else {
                etIccid.setText(generateRandomIccid());
            }

            String fakeImsi = Settings.System.getString(cr, "fake_imsi");
            if (fakeImsi != null && !fakeImsi.isEmpty()) {
                etImsi.setText(fakeImsi);
            } else {
                etImsi.setText("45204" + (1000000000L + (long)(random.nextDouble() * 9000000000L)));
            }

            updateCpuStatus();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyAndroidId() {
        String val = etAndroidId.getText().toString().trim();
        if (val.length() != 16) {
            Toast.makeText(this, "Android ID phải có 16 ký tự hex!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Settings.Secure.putString(getContentResolver(), Settings.Secure.ANDROID_ID, val);
            Toast.makeText(this, "✓ Đã áp dụng Android ID: " + val, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void applyImei() {
        String val = etImei.getText().toString().trim();
        if (val.length() < 14 || val.length() > 16) {
            Toast.makeText(this, "IMEI phải có 15 chữ số!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Settings.System.putString(getContentResolver(), "fake_imei", val);
            Toast.makeText(this, "✓ Đã áp dụng IMEI: " + val, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void resetImei() {
        try {
            Settings.System.putString(getContentResolver(), "fake_imei", "");
            loadCurrentValues();
        resetBattery();
            Toast.makeText(this, "✓ Đã xóa Fake IMEI, khôi phục gốc", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applySerial() {
        String val = etSerial.getText().toString().trim();
        if (val.isEmpty()) return;
        try {
            Settings.System.putString(getContentResolver(), "fake_serial", val);
            Runtime.getRuntime().exec(new String[]{"setprop", "ro.serialno", val});
            Toast.makeText(this, "✓ Đã lưu Serial mới: " + val, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectPreset(int index) {
        String[] p = DEVICE_PRESETS[index];
        tvCurrentModel.setText("Dòng máy giả lập: " + p[1] + " (" + p[2] + ")");
        etImei.setText(generateRandomImeiWithPrefix(p[6]));
        etSerial.setText(generateRandomAlphanumeric(10));
        applyImei();
        applySerial();
        Toast.makeText(this, "✓ Đã nạp Profile dòng máy: " + p[0], Toast.LENGTH_SHORT).show();
    }

    private void selectCarrier(String name, String numeric, String phonePrefix) {
        etCarrierName.setText(name);
        etCarrierNumeric.setText(numeric);
        etPhoneNumber.setText(phonePrefix + (1000000 + random.nextInt(9000000)));
        etImsi.setText(numeric + (1000000000L + (long)(random.nextDouble() * 9000000000L)));
        applyCarrierAndSim();
    }

    private void randomizeSimPackage() {
        String[] carriers = {"Viettel", "VN VINAPHONE", "MobiFone"};
        String[] numerics = {"45204", "45202", "45201"};
        String[] phonePrefixes = {"098", "091", "090"};
        int idx = random.nextInt(carriers.length);

        etCarrierName.setText(carriers[idx]);
        etCarrierNumeric.setText(numerics[idx]);
        etPhoneNumber.setText(phonePrefixes[idx] + (1000000 + random.nextInt(9000000)));
        etIccid.setText(generateRandomIccid());
        etImsi.setText(numerics[idx] + (1000000000L + (long)(random.nextDouble() * 9000000000L)));
    }

    private void applyCarrierAndSim() {
        String name = etCarrierName.getText().toString().trim();
        String num = etCarrierNumeric.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String iccid = etIccid.getText().toString().trim();
        String imsi = etImsi.getText().toString().trim();

        try {
            ContentResolver cr = getContentResolver();
            Settings.System.putString(cr, "fake_carrier_name", name);
            Settings.System.putString(cr, "fake_carrier_numeric", num);
            Settings.System.putString(cr, "fake_phone_number", phone);
            Settings.System.putString(cr, "fake_iccid", iccid);
            Settings.System.putString(cr, "fake_imsi", imsi);

            Runtime.getRuntime().exec(new String[]{"setprop", "gsm.operator.alpha", name});
            Runtime.getRuntime().exec(new String[]{"setprop", "gsm.operator.numeric", num});
            Runtime.getRuntime().exec(new String[]{"setprop", "gsm.sim.operator.alpha", name});
            Runtime.getRuntime().exec(new String[]{"setprop", "gsm.sim.operator.numeric", num});
            Runtime.getRuntime().exec(new String[]{"setprop", "gsm.operator.iso-country", "vn"});
            Runtime.getRuntime().exec(new String[]{"setprop", "gsm.sim.operator.iso-country", "vn"});

            Toast.makeText(this, "✓ Đã áp dụng SIM: " + name + " • " + phone, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi áp dụng SIM: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setGpsInputs(double lat, double lng) {
        etLatitude.setText(String.format("%.6f", lat));
        etLongitude.setText(String.format("%.6f", lng));
    }

    private void startMockLocation() {
        try {
            double lat = Double.parseDouble(etLatitude.getText().toString().trim());
            double lng = Double.parseDouble(etLongitude.getText().toString().trim());

            try {
                locationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                    false, false, false, false, true, true, true, 1, 1);
            } catch (Exception ignored) {}

            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

            Location loc = new Location(LocationManager.GPS_PROVIDER);
            loc.setLatitude(lat);
            loc.setLongitude(lng);
            loc.setAltitude(15.0);
            loc.setAccuracy(3.0f);
            loc.setTime(System.currentTimeMillis());
            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc);
            isMockingGps = true;
            Toast.makeText(this, "✓ Đã kích hoạt GPS ảo tại: " + lat + ", " + lng, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi bật GPS ảo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void stopMockLocation() {
        try {
            if (locationManager != null) {
                locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
            }
            isMockingGps = false;
            Toast.makeText(this, "✓ Đã tắt GPS ảo, khôi phục vị trí thật", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tắt GPS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyDpi(int density) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"wm", "density", String.valueOf(density)});
            p.waitFor();
            Toast.makeText(this, "✓ Đã đổi mật độ hiển thị thành " + density + " DPI", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đổi DPI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetDpi() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"wm", "density", "reset"});
            p.waitFor();
            Toast.makeText(this, "✓ Đã khôi phục DPI mặc định", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi reset DPI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetBattery() {
        try {
            Runtime.getRuntime().exec(new String[]{"dumpsys", "battery", "reset"}).waitFor();
            Toast.makeText(this, "✓ Đã khôi phục Pin thật", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi reset pin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void wipeSelectedApps() {
        int count = 0;
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        try {
            Method clearMethod = null;
            for (Method m : ActivityManager.class.getDeclaredMethods()) {
                if (m.getName().equals("clearApplicationUserData")) {
                    clearMethod = m;
                    break;
                }
            }

            for (CheckBox cb : appCheckboxes) {
                if (cb.isChecked()) {
                    String pkg = (String) cb.getTag();
                    if (clearMethod != null) {
                        clearMethod.invoke(am, pkg, null);
                        count++;
                    }
                }
            }

            String custom = etCustomPackage.getText().toString().trim();
            if (!custom.isEmpty() && clearMethod != null) {
                clearMethod.invoke(am, custom, null);
                count++;
            }

            if (count > 0) {
                Toast.makeText(this, "✓ Đã xóa sạch dữ liệu " + count + " ứng dụng!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Chưa chọn ứng dụng nào để xóa", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi làm sạch app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void resetGaid() {
        try {
            Intent intent = new Intent("com.google.android.gms.settings.ADS_PRIVACY");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e1) {
            try {
                Intent fallback = new Intent();
                fallback.setClassName("com.google.android.gms", "com.google.android.gms.ads.settings.AdsSettingsActivity");
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fallback);
            } catch (Exception e2) {
                Toast.makeText(this, "Không thể mở trang Ads: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfile(int slot) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("p" + slot + "_id", etAndroidId.getText().toString().trim());
        ed.putString("p" + slot + "_imei", etImei.getText().toString().trim());
        ed.putString("p" + slot + "_serial", etSerial.getText().toString().trim());
        ed.putString("p" + slot + "_carrier", etCarrierName.getText().toString().trim());
        ed.putString("p" + slot + "_numeric", etCarrierNumeric.getText().toString().trim());
        ed.putString("p" + slot + "_phone", etPhoneNumber.getText().toString().trim());
        ed.putString("p" + slot + "_iccid", etIccid.getText().toString().trim());
        ed.putString("p" + slot + "_imsi", etImsi.getText().toString().trim());
        ed.putString("p" + slot + "_lat", etLatitude.getText().toString().trim());
        ed.putString("p" + slot + "_lng", etLongitude.getText().toString().trim());
        ed.apply();
        Toast.makeText(this, "✓ Đã lưu toàn bộ thông số vào Hồ Sơ " + slot + "!", Toast.LENGTH_SHORT).show();
    }

    private void loadProfile(int slot) {
        String id = prefs.getString("p" + slot + "_id", "");
        if (id.isEmpty()) {
            Toast.makeText(this, "Hồ Sơ " + slot + " chưa có dữ liệu!", Toast.LENGTH_SHORT).show();
            return;
        }

        etAndroidId.setText(id);
        etImei.setText(prefs.getString("p" + slot + "_imei", ""));
        etSerial.setText(prefs.getString("p" + slot + "_serial", ""));
        etCarrierName.setText(prefs.getString("p" + slot + "_carrier", "Viettel"));
        etCarrierNumeric.setText(prefs.getString("p" + slot + "_numeric", "45204"));
        etPhoneNumber.setText(prefs.getString("p" + slot + "_phone", ""));
        etIccid.setText(prefs.getString("p" + slot + "_iccid", ""));
        etImsi.setText(prefs.getString("p" + slot + "_imsi", ""));
        etLatitude.setText(prefs.getString("p" + slot + "_lat", "21.028511"));
        etLongitude.setText(prefs.getString("p" + slot + "_lng", "105.854167"));

        applyAndroidId();
        applyImei();
        applySerial();
        applyCarrierAndSim();
        startMockLocation();

        Toast.makeText(this, "✓ Đã nạp và kích hoạt toàn bộ Hồ Sơ " + slot + "!", Toast.LENGTH_SHORT).show();
    }

    private String generateRandomHex(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(Integer.toHexString(random.nextInt(16)));
        }
        return sb.toString();
    }

    private String generateRandomAlphanumeric(int len) {
        String chars = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateRandomImei() {
        String[] prefixes = {"86043204", "35848507", "86423903", "35678908"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        return generateRandomImeiWithPrefix(prefix);
    }

    private String generateRandomImeiWithPrefix(String prefix) {
        StringBuilder sb = new StringBuilder(prefix);
        while (sb.length() < 14) {
            sb.append(random.nextInt(10));
        }
        int checkDigit = computeLuhn(sb.toString());
        sb.append(checkDigit);
        return sb.toString();
    }

    private String generateRandomIccid() {
        StringBuilder sb = new StringBuilder("898404");
        while (sb.length() < 19) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private int computeLuhn(String digits) {
        int sum = 0;
        boolean alternate = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(digits.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

    private void setSystemProperty(String key, String val) {
        try {
            Class<?> sp = Class.forName("android.os.SystemProperties");
            Method set = sp.getMethod("set", String.class, String.class);
            set.invoke(null, key, val);
        } catch (Exception e) {
            android.util.Log.e("PROPERTIES", "Failed setting " + key + ": " + e.getMessage());
        }
    }

    private boolean writeSysfsFile(String path, String value) {
        try {
            java.io.File file = new java.io.File(path);
            if (!file.exists()) return false;
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            fos.write(value.getBytes());
            fos.flush();
            fos.close();
            android.util.Log.i("SYSFS_WRITE", "Success: " + path + " -> " + value);
            return true;
        } catch (Exception e) {
            android.util.Log.e("SYSFS_WRITE", "Fail: " + path + " (" + e.getMessage() + ")");
            return false;
        }
    }

    private String readSysfsFile(String path) {
        try {
            java.io.File file = new java.io.File(path);
            if (!file.exists()) return "";
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
            String line = br.readLine();
            br.close();
            return line != null ? line.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private void updateCpuStatus() {
        if (tvCpuStatus == null) return;
        StringBuilder sb = new StringBuilder();

        // Check MediaTek policy0 & policy4
        if (new java.io.File("/sys/devices/system/cpu/cpufreq/policy0").exists()) {
            String p0Cur = readSysfsFile("/sys/devices/system/cpu/cpufreq/policy0/scaling_cur_freq");
            String p0Min = readSysfsFile("/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq");
            String p4Cur = readSysfsFile("/sys/devices/system/cpu/cpufreq/policy4/scaling_cur_freq");
            String p4Min = readSysfsFile("/sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq");
            sb.append("⚡ Helio P22 (8 Nhân):\n");
            sb.append("• 4 Nhân Lớn (Cortex-A53): ").append(formatFreq(p0Cur)).append(" (Sàn: ").append(formatFreq(p0Min)).append(")\n");
            sb.append("• 4 Nhân Nhỏ (Cortex-A53): ").append(formatFreq(p4Cur)).append(" (Sàn: ").append(formatFreq(p4Min)).append(")\n");
        } else if (new java.io.File("/sys/devices/system/cpu/cpu0/cpufreq").exists()) {
            String c0Cur = readSysfsFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            String c0Min = readSysfsFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
            sb.append("⚡ Snapdragon 805 (4 Nhân):\n");
            sb.append("• Xung hiện tại: ").append(formatFreq(c0Cur)).append(" (Sàn: ").append(formatFreq(c0Min)).append(")\n");
        }

        // Check Swap / zRAM
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("/proc/meminfo"));
            String line;
            String swapTotal = "0 kB";
            String swapFree = "0 kB";
            while ((line = br.readLine()) != null) {
                if (line.startsWith("SwapTotal:")) swapTotal = line.substring(10).trim();
                if (line.startsWith("SwapFree:")) swapFree = line.substring(9).trim();
            }
            br.close();
            sb.append("💾 Bộ nhớ zRAM: ").append(swapTotal).append(" (Trống: ").append(swapFree).append(")");
        } catch (Exception ignored) {
            sb.append("💾 Bộ nhớ zRAM: 0 MB");
        }

        tvCpuStatus.setText(sb.toString());
    }

    private String formatFreq(String freqKhz) {
        if (freqKhz == null || freqKhz.isEmpty()) return "N/A";
        try {
            long khz = Long.parseLong(freqKhz);
            double ghz = khz / 1000000.0;
            return String.format(java.util.Locale.US, "%.2f GHz", ghz);
        } catch (Exception e) {
            return freqKhz + " kHz";
        }
    }

    private void applyCpuTurbo() {
        new Thread(() -> {
            int count = 0;
            // 1. MediaTek Helio P22
            if (new java.io.File("/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq").exists()) {
                if (writeSysfsFile("/sys/devices/system/cpu/cpufreq/policy0/scaling_governor", "performance")) count++;
                if (writeSysfsFile("/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq", "1617000")) count++;
                if (writeSysfsFile("/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq", "2001000")) count++;
            }
            if (new java.io.File("/sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq").exists()) {
                if (writeSysfsFile("/sys/devices/system/cpu/cpufreq/policy4/scaling_governor", "performance")) count++;
                if (writeSysfsFile("/sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq", "1102000")) count++;
                if (writeSysfsFile("/sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq", "1500000")) count++;
            }

            // 2. Snapdragon
            for (int i = 0; i < 4; i++) {
                writeSysfsFile("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_min_freq", "1190400");
                writeSysfsFile("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_governor", "interactive");
            }
            writeSysfsFile("/sys/devices/system/cpu/cpufreq/interactive/hispeed_freq", "2265600");
            writeSysfsFile("/sys/devices/system/cpu/cpufreq/interactive/go_hispeed_load", "65");

            // 3. Oppo / Realme Game Mode & Touch Boost
            setSystemProperty("persist.sys.phh.oppo.gaming_mode", "1");

            final int resCount = count;
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "✓ Đã áp dụng CPU Turbo (Hiệu năng cao)!", Toast.LENGTH_SHORT).show();
                updateCpuStatus();
            });
        }).start();
    }

    private void applyZram() {
        new Thread(() -> {
            boolean disksizeWritten = false;
            try {
                writeSysfsFile("/sys/block/zram0/comp_algorithm", "lz4");
                disksizeWritten = writeSysfsFile("/sys/block/zram0/disksize", "1073741824");
                writeSysfsFile("/proc/sys/vm/swappiness", "100");

                // Execute mkswap & swapon
                Process p1 = Runtime.getRuntime().exec(new String[]{"/system/bin/mkswap", "/dev/block/zram0"});
                p1.waitFor();
                Process p2 = Runtime.getRuntime().exec(new String[]{"/system/bin/swapon", "/dev/block/zram0"});
                p2.waitFor();
            } catch (Exception e) {
                android.util.Log.e("ZRAM", "Error: " + e.getMessage());
            }

            final boolean success = disksizeWritten;
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, success ? "✓ Đã gửi lệnh cấu hình zRAM 1GB!" : "Đã gửi lệnh kích hoạt zRAM!", Toast.LENGTH_SHORT).show();
                updateCpuStatus();
            });
        }).start();
    }
}
