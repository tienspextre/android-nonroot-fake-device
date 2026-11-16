# Hướng Dẫn Tự Triển Khai Lại Toàn Bộ Giải Pháp Cho Samsung Galaxy Note 4

Tài liệu này lưu lại toàn bộ các bước kỹ thuật, công cụ và mã nguồn đã được thực hiện trên chiếc **Samsung Galaxy Note 4 (SM-N910V - Android 9 Pie PixelExperience)** để bạn có thể tự làm lại bất cứ khi nào cài lại ROM mới mà **hoàn toàn không cần root máy**.

---

## 📁 1. Vị Trí Lưu Trữ Mã Nguồn & Công Cụ

Toàn bộ công cụ và mã nguồn đã được sao lưu vĩnh viễn tại thư mục:
`/home/vorza/note4_project_backup/`

Gồm có:
* **`DeviceIDManager_v3.apk`**: File APK hoàn chỉnh đã được ký khóa nền tảng (sẵn sàng nạp vào máy).
* **`devid_pro/`**: Thư mục mã nguồn Java (`MainActivity.java`), tài nguyên (`res/`) và file cấu hình (`AndroidManifest.xml`).
* **`platform.jks`**: Keystore chứa chứng chỉ nền tảng AOSP Testkey (password: `android`, alias: `platform`) dùng để cấp quyền `UID 1000 (System)`.
* **`patch_teleservice.py`**: Script Python tự động tiêm hook vào `PhoneInterfaceManager.smali`.
* **`build_and_deploy.sh`**: Script tự động biên dịch từ mã nguồn Java thành file APK đã ký chuẩn.
* **Bộ công cụ Jar**: `apktool.jar`, `baksmali.jar`, `smali.jar`, `r8.jar`, `uber-apk-signer.jar`.

---

## ⚙️ 2. Nguyên Lý Kỹ Thuật (Tại Sao Không Cần Root?)

1. **Khóa ký hệ thống (Platform Testkey):**
   * Bản ROM PixelExperience sử dụng bộ khóa ký thử nghiệm tiêu chuẩn của AOSP (`platform.pk8` + `platform.x509.pem`).
   * Bất kỳ ứng dụng nào được đặt trong `/system/priv-app/` và được ký bởi khóa `platform.jks` này với thuộc tính `android:sharedUserId="android.uid.system"` sẽ được Android tự động trao **toàn bộ đặc quyền hệ thống (UID 1000)** mà không cần nhị phân `su` (Magisk).

2. **Cơ chế Fake Android ID toàn hệ thống:**
   * Mặc định từ Android 8.0 trở lên, Android chia mỗi app một mã SSAID riêng biệt thông qua hàm `isNewSsaidSetting` trong `SettingsProvider.apk`.
   * Khi sửa hàm này luôn trả về `false (0)`, toàn bộ ứng dụng trên máy sẽ đọc chung một mã `android_id` toàn cục trong `Settings.Secure`.

3. **Cơ chế Fake IMEI phần cứng:**
   * Trong `TeleService.apk` (tiến trình quản lý sóng và điện thoại của Android), lớp `PhoneInterfaceManager` chịu trách nhiệm trả về IMEI khi bất kỳ app nào gọi `TelephonyManager.getDeviceId()` hoặc `getImei()`.
   * Chúng ta tiêm lệnh đọc giá trị `fake_imei` từ `Settings.System`. Khi có giá trị, hệ thống sẽ trả về IMEI mới ngay lập tức mà không cần khởi động lại máy.

---

## 🛠️ 3. Quy Trình Thực Hiện Từng Bước (Khi Cài Lại ROM)

### Bước 1: Patch `SettingsProvider.apk` (Hủy phân lập Android ID)
1. Đưa Note 4 vào TWRP Recovery:
   ```bash
   adb reboot recovery
   ```
2. Mount phân vùng hệ thống dạng đọc/ghi:
   ```bash
   adb shell "mount /system 2>/dev/null; mount -o remount,rw /system 2>/dev/null"
   ```
3. Kéo file APK từ máy ra máy tính:
   ```bash
   adb pull /system/priv-app/SettingsProvider/SettingsProvider.apk /tmp/SettingsProvider.apk
   ```
4. Giải nén DEX sang mã Smali:
   ```bash
   unzip /tmp/SettingsProvider.apk classes.dex -d /tmp/sp/
   java -jar /home/vorza/note4_project_backup/baksmali.jar d /tmp/sp/classes.dex -o /tmp/sp/smali/
   ```
5. Mở file: `/tmp/sp/smali/com/android/providers/settings/SettingsProvider.smali`
   Tìm đến phương thức:
   ```smali
   .method private isNewSsaidSetting(Ljava/lang/String;)Z
   ```
   Sửa nội dung phương thức thành luôn trả về 0:
   ```smali
   .method private isNewSsaidSetting(Ljava/lang/String;)Z
       .registers 3

       const/4 v0, 0x0

       return v0
   .end method
   ```
6. Đóng gói lại DEX và cập nhật vào APK:
   ```bash
   java -jar /home/vorza/note4_project_backup/smali.jar a /tmp/sp/smali/ -o /tmp/sp/classes.dex
   cd /tmp/sp && zip -u /tmp/SettingsProvider.apk classes.dex
   ```
7. Ký lại bằng Platform Key:
   ```bash
   java -jar /home/vorza/note4_project_backup/uber-apk-signer.jar \
     -a /tmp/SettingsProvider.apk \
     --ks /home/vorza/note4_project_backup/platform.jks \
     --ksPass android --ksAlias platform --ksKeyPass android \
     --allowResign --out /tmp/sp/dist/
   ```

---

### Bước 2: Patch `TeleService.apk` (Hook trả về IMEI)
1. Kéo file từ Note 4 ra máy tính:
   ```bash
   adb pull /system/priv-app/TeleService/TeleService.apk /tmp/TeleService.apk
   ```
2. Giải nén DEX sang Smali:
   ```bash
   unzip /tmp/TeleService.apk classes.dex -d /tmp/tele/
   java -jar /home/vorza/note4_project_backup/baksmali.jar d /tmp/tele/classes.dex -o /tmp/tele/smali/
   ```
3. Chạy script tự động tiêm hook vào `PhoneInterfaceManager.smali`:
   ```bash
   python3 /home/vorza/note4_project_backup/patch_teleservice.py /tmp/tele/smali/com/android/phone/PhoneInterfaceManager.smali
   ```
4. Đóng gói lại DEX và cập nhật vào APK:
   ```bash
   java -jar /home/vorza/note4_project_backup/smali.jar a /tmp/tele/smali/ -o /tmp/tele/classes.dex
   cd /tmp/tele && zip -u /tmp/TeleService.apk classes.dex
   ```
5. Ký lại bằng Platform Key:
   ```bash
   java -jar /home/vorza/note4_project_backup/uber-apk-signer.jar \
     -a /tmp/TeleService.apk \
     --ks /home/vorza/note4_project_backup/platform.jks \
     --ksPass android --ksAlias platform --ksKeyPass android \
     --allowResign --out /tmp/tele/dist/
   ```

---

### Bước 3: Biên Dịch Ứng Dụng Điều Khiển (Device ID Changer Pro)
Nếu bạn có chỉnh sửa thêm giao diện hay tính năng trong file `devid_pro/src/com/sec/deviceid/MainActivity.java`, chỉ cần chạy script:
```bash
cd /home/vorza/note4_project_backup
./build_and_deploy.sh
```
File APK mới sẽ được xuất ra ngay tại: `DeviceIDManager_v3.apk`.

---

### Bước 4: Đẩy Toàn Bộ Vào Phân Vùng Hệ Thống Qua TWRP

Khi máy đang ở màn hình TWRP Recovery:
```bash
# 1. Mount hệ thống đọc ghi
adb shell "mount /system 2>/dev/null; mount -o remount,rw /system 2>/dev/null"

# 2. Đẩy SettingsProvider đã patch
adb push /tmp/sp/dist/*aligned-signed.apk /system/priv-app/SettingsProvider/SettingsProvider.apk

# 3. Đẩy TeleService đã patch
adb push /tmp/tele/dist/*aligned-signed.apk /system/priv-app/TeleService/TeleService.apk

# 4. Đẩy app Device ID Changer Pro
adb shell "mkdir -p /system/priv-app/DeviceIDManager"
adb push /home/vorza/note4_project_backup/DeviceIDManager_v3.apk /system/priv-app/DeviceIDManager/DeviceIDManager.apk

# 5. Phân quyền chuẩn 644 (Owner: root, Permissions: -rw-r--r--)
adb shell "chmod 644 /system/priv-app/SettingsProvider/SettingsProvider.apk"
adb shell "chmod 644 /system/priv-app/TeleService/TeleService.apk"
adb shell "chmod 644 /system/priv-app/DeviceIDManager/DeviceIDManager.apk"

# 6. Xóa thư mục bộ nhớ đệm cũ (để hệ thống tái tạo lại bộ nhớ đệm mới)
adb shell "rm -rf /system/priv-app/SettingsProvider/oat /system/priv-app/TeleService/oat /system/priv-app/DeviceIDManager/oat"
adb shell "mount /data 2>/dev/null; rm -rf /data/dalvik-cache /cache/dalvik-cache; sync"

# 7. Khởi động lại máy vào Android
adb reboot
```

---

## 📱 4. Kiểm Tra Sau Khi Khởi Động
1. Ứng dụng **"Device ID Changer Pro"** sẽ xuất hiện trên màn hình chính.
2. Mở ứng dụng:
   * Bấm **🎲 Sinh Ngẫu Nhiên** $\rightarrow$ **💾 Áp Dụng ID**
   * Bấm **🎲 Sinh IMEI** $\rightarrow$ **💾 Áp Dụng IMEI**
3. Mở Cài đặt của máy $\rightarrow$ **Giới thiệu về điện thoại (About Phone)**:
   * Số IMEI sẽ lập tức hiển thị giá trị mới vừa được áp dụng.
4. Kiểm tra các app ngân hàng hoặc app bảo mật: Máy hoàn toàn sạch, SafetyNet / Play Protect không phát hiện root vì không có bất kỳ file su hay Magisk nào tồn tại trên thiết bị.

---

## 🚀 5. Tối Ưu Tốc Độ Xử Lý & Kích Hoạt Turbo CPU / GPU (100% Không Root)

Để Galaxy Note 4 đạt tốc độ phản hồi tối đa, loại bỏ hoàn toàn giật lag và micro-stutter:

### Bước 1: Kích hoạt Turbo 4 Nhân CPU & Ép Xung Phản Hồi (Tự động khi mở máy)
File script đã tạo sẵn tại: `/home/vorza/note4_project_backup/98cpu_boost`.
Trong TWRP Recovery, chỉ cần đẩy file vào `init.d`:
```bash
adb shell "mount -o remount,rw /system"
adb push /home/vorza/note4_project_backup/98cpu_boost /system/etc/init.d/98cpu_boost
adb shell "chmod 0755 /system/etc/init.d/98cpu_boost && chown root:shell /system/etc/init.d/98cpu_boost"
```
* **Tác dụng:**
  * Ngăn `mpdecision` tắt nhân, ép **cả 4 nhân CPU0 - CPU3 luôn Online**.
  * Nâng xung sàn (Min Freq) từ **300 MHz lên 1.19 GHz**.
  * Nâng xung tăng tốc tức thì (Hispeed Freq) lên **2.26 GHz**.
  * Nâng xung sàn GPU Adreno 420 từ **240 MHz lên 389 MHz** (Powerlevel 3).

### Bước 2: Tối ưu tải màn hình GPU (Hạ từ 2K về Full HD)
Chạy trực tiếp qua ADB shell (không cần recovery):
```bash
adb shell "wm size 1080x1920 && wm density 420"
```
*(Nếu muốn hoàn tác về 2K gốc: `adb shell "wm size reset && wm density reset"`).*

### Bước 3: Đóng băng các tiến trình Google ngốn RAM
```bash
adb shell "pm disable-user --user 0 com.google.android.googlequicksearchbox"
adb shell "pm disable-user --user 0 com.google.android.apps.turbo"
adb shell "pm disable-user --user 0 com.google.android.setupwizard"
adb shell "pm disable-user --user 0 com.google.android.feedback"
adb shell "pm disable-user --user 0 com.android.printservice.recommendation"
```
Giải phóng thêm ~500 MB RAM thực cho máy.

### Bước 4: Biên dịch mã máy AOT (Speed Mode)
```bash
adb shell "cmd package compile -m speed -f com.sec.deviceid"
adb shell "cmd package compile -m speed -f com.android.settings"
adb shell "cmd package compile -m speed -f com.android.systemui"
adb shell "cmd package compile -m speed -f com.android.vending"
```

---

## 13. Xử lý triệt để lỗi Camera HAL1 & Deadlock MediaServer (Cứu Shopee QR)

### Nguyên nhân:
Trên bản ROM Android 9 (Pie) `trlte`, cảm biến Camera trước (Samsung S5K6D1YX) bị lỗi không map được bộ đệm tham số HAL1 (`init:failed to map SETPARM buffer`). Mỗi khi bất kỳ ứng dụng nào (Shopee, Open Camera, quét mã QR...) yêu cầu danh sách máy ảnh, hệ thống gọi vào Camera trước làm chết và deadlock tiến trình `mediaserver` (`futex_wait_queue_me`), khiến toàn bộ camera bị liệt và các app dùng camera bị treo vô tận.

### Giải pháp đã thực hiện:
Vô hiệu hóa cảm biến Camera trước tại tầng Driver qua TWRP recovery:
1. Vào TWRP recovery (`adb reboot recovery`).
2. Mount phân vùng `/system` dạng RW:
   ```bash
   mount -o remount,rw /system
   ```
3. Đổi tên toàn bộ thư viện cảm biến `s5k6d1yx`:
   ```bash
   mv /system/vendor/lib/libmmcamera_s5k6d1yx.so /system/vendor/lib/libmmcamera_s5k6d1yx.so.bak
   for f in /system/vendor/lib/libchromatix_s5k6d1yx_*.so; do mv "$f" "${f}.bak"; done
   ```
4. Unmount và khởi động lại:
   ```bash
   sync && umount /system && reboot
   ```

### Kết quả:
- Hệ thống Android chỉ nhận diện duy nhất **1 Camera (Camera sau - Sony IMX240)**.
- Tiến trình `mediaserver` luôn ở trạng thái lành lặn (`binder_thread_read`), chấm dứt 100% tình trạng treo máy/treo camera.
- Ứng dụng Shopee mở mượt mà, tính năng quét mã QR hoạt động ổn định.

### 13b. (2026-11-13) Sửa lại: KHÔNG đổi tên lib s5k6d1yx — ẩn camera trước trong HAL
- Đổi tên `libmmcamera_s5k6d1yx.so` làm `mm-qcamera-daemon` báo `sensor_load_library failed` → hủy luôn module sensor → camera sau cũng hỏng (`commitSetBatch: can't set parameters`, `mct_stream_link_preview Null`, preview đen).
- Đã trả các file `s5k6d1yx*.bak` về tên gốc.
- Vá `camera.vendor.apq8084.so` ở offset `0x1e9f0`: `03 46` (mov r3,r0) → `01 20` (movs r0,#1). Constructor `QCamera2Factory` lưu `mNumOfCameras = 1` → Android chỉ thấy camera sau, CameraService không bao giờ mở camera 1 (nên hết deadlock).
- Bản lưu trên máy: `camera.vendor.apq8084.so.orig` (gốc), `.rthdr` (chỉ có bản vá rt-hdr). Label SELinux: `u:object_r:vendor_file:s0`.
