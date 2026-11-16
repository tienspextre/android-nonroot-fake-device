# Hướng Dẫn Tối Ưu Tốc Độ & Kích Hoạt 1GB zRAM Cho Realme C2 (RMX1941)

Tài liệu này ghi lại các bước tối ưu hóa hiệu năng, kích hoạt 1GB zRAM và tăng tốc CPU trên **Realme C2 (MediaTek Helio P22 MT6765 - Android 10 Phh-Treble GSI)** hoàn toàn **100% KHÔNG CẦN ROOT**.

---

## 🚀 1. Các Tối Ưu Đã Thực Hiện Thành Công

1. **Kích hoạt 1 GB zRAM (RAM ảo nén siêu tốc):**
   * **Trước:** `Swap = 0 MB` (hoàn toàn không có swap). RAM khả dụng chỉ còn 25 MB khiến máy liên tục bị treo đơ do tiến trình LMK diệt app vòng lặp.
   * **Hiện tại:** **`SwapTotal = 1048572 kB` (1024 MB zRAM)** hoạt động tự động khi khởi động máy.
   * **RAM khả dụng thực tế:** Tăng vọt từ **25 MB lên ~900 MB**!

2. **Ép xung 8 nhân CPU MediaTek Helio P22:**
   * **Cụm 4 nhân lớn (Big Cluster):** Đạt trần xung nhịp **2.001 GHz**.
   * **Cụm 4 nhân nhỏ (Little Cluster):** Đạt trần xung nhịp **1.500 GHz**.
   * Tự động bật chế độ **Oppo Touchscreen Gaming Mode** (`persist.sys.phh.oppo.gaming_mode = 1`) để tăng tần số lấy mẫu cảm ứng.

3. **Tối ưu đồ họa GPU PowerVR (Hạ từ 720p xuống 540x1170 @ 240 DPI):**
   * Giảm **44% khối lượng điểm ảnh** GPU phải xử lý.
   * Giữ nguyên tỷ lệ chuẩn các nút bấm, icon và font chữ.

4. **Đóng băng bloatware Google ăn RAM:**
   * `com.google.android.googlequicksearchbox` (~560 MB RAM)
   * `com.google.android.projection.gearhead` (~83 MB RAM)
   * `com.google.android.setupwizard`, `feedback`, `printservice`.

5. **Biên dịch mã máy AOT (`speed` mode):**
   * `Launcher3`, `SystemUI`, `Settings`, `Bàn phím Gboard`.

---

## 🛠️ 2. Quy Trình Kỹ Thuật (Khi Cài Lại Máy)

### Bước 1: Kích hoạt zRAM 1GB & CPU Turbo qua TWRP
1. Khởi động vào TWRP:
   ```bash
   adb reboot recovery
   ```
2. Mount phân vùng hệ thống dạng đọc ghi:
   ```bash
   adb shell "mkdir -p /system_root && mount /dev/block/platform/bootdevice/by-name/system /system_root"
   ```
3. Đẩy tệp kịch bản `swap_enable.sh` và `phh-prop-handler.sh`:
   ```bash
   adb push /home/vorza/note4_project_backup/swap_enable.sh /system_root/system/etc/swap_enable.sh
   adb push /tmp/phh-prop-handler.sh /system_root/system/bin/phh-prop-handler.sh
   adb push /tmp/vndk.rc /system_root/system/etc/init/vndk.rc
   adb shell "chmod 0755 /system_root/system/etc/swap_enable.sh /system_root/system/bin/phh-prop-handler.sh /system_root/system/etc/init/vndk.rc"
   adb shell "sync && umount /system_root"
   adb reboot
   ```

### Bước 2: Tối ưu màn hình qua ADB
```bash
adb shell "wm size 540x1170 && wm density 240"
adb shell "settings put global window_animation_scale 0.5"
adb shell "settings put global transition_animation_scale 0.5"
adb shell "settings put global animator_duration_scale 0.5"
```

### Bước 3: Đóng băng các app Google ngốn RAM
```bash
adb shell "pm disable-user --user 0 com.google.android.googlequicksearchbox"
adb shell "pm disable-user --user 0 com.google.android.projection.gearhead"
adb shell "pm disable-user --user 0 com.google.android.setupwizard"
adb shell "pm disable-user --user 0 com.google.android.feedback"
adb shell "pm disable-user --user 0 com.google.android.printservice.recommendation"
```

### Bước 4: Biên dịch AOT Speed Mode
```bash
adb shell "cmd package compile -m speed -f com.android.launcher3"
adb shell "cmd package compile -m speed -f com.android.systemui"
adb shell "cmd package compile -m speed -f com.android.settings"
adb shell "cmd package compile -m speed -f com.google.android.inputmethod.latin"
```
