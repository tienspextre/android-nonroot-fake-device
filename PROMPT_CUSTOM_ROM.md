# PROMPT MẪU: CUSTOM ROM FAKE DEVICE KHÔNG ROOT & TỐI ƯU HIỆU NĂNG

Bạn có thể copy toàn bộ đoạn prompt dưới đây để giao việc cho AI Agent khi cắm một chiếc điện thoại Android mới (đã mở khóa Bootloader và có TWRP Recovery) qua ADB:

---

```markdown
Tôi đang kết nối một thiết bị Android mới qua ADB và muốn bạn thực hiện custom lại hệ thống để phục vụ **Fake Device ID / IMEI toàn hệ thống** và **Tối ưu hiệu năng tối đa** theo kiến trúc đã làm thành công trước đây.

### 1. RÀNG BUỘC BẮT BUỘC (QUAN TRỌNG NHẤT)
- **TUYỆT ĐỐI KHÔNG ROOT** trên hệ điều hành Android khi khởi động (Không cài Magisk, không cài KernelSU, không để tồn tại nhị phân `su` trong `/system/bin` hay `/system/xbin`). Máy phải sạch hoàn toàn để vượt qua kiểm tra bảo mật của các app ngân hàng, ví điện tử, Shopee, v.v.
- Mọi thao tác chỉnh sửa phân vùng `/system` hoặc `/vendor` phải thực hiện thông qua **TWRP Recovery** (`adb reboot recovery`, mount phân vùng RW).
- **KHÔNG** tắt OEM Unlock hoặc khóa lại Bootloader.
- Tham khảo bộ công cụ, mã nguồn và tài liệu mẫu có sẵn tại `/home/vorza/note4_project_backup/` (gồm `apktool.jar`, `baksmali.jar`, `smali.jar`, `r8.jar`, `uber-apk-signer.jar`, `platform.jks`, `patch_teleservice.py`, `devid_pro/`, `HUONG_DAN_NOTE4.md`, `HUONG_DAN_REALME_C2.md`).

---

### 2. CÁC BƯỚC CẦN BẠN THỰC HIỆN

#### Bước A: Khảo sát thiết bị mới
1. Kiểm tra `adb devices -l`, phiên bản Android, kiến trúc CPU (`arm`/`arm64`), chip SoC, dung lượng RAM/zRAM (`cat /proc/meminfo`, `cat /proc/swaps`), cấu trúc phân vùng (A-only hay A/B, System-as-Root).
2. Kiểm tra bộ khóa ký của ROM hiện tại (kiểm tra chữ ký của `/system/priv-app/SettingsProvider/SettingsProvider.apk` và `TeleService.apk` xem ROM có dùng AOSP Testkey khớp với `/home/vorza/note4_project_backup/platform.jks` hay không). Báo cáo kết quả khảo sát cho tôi trước khi sửa sâu.

#### Bước B: Custom ROM Fake Device (Không cần Root)
1. **Hủy phân lập Android ID (SSAID) trên Android 8+:**
   - Kéo `SettingsProvider.apk` từ `/system/priv-app/SettingsProvider/`, dịch ngược `classes.dex` bằng `baksmali.jar`.
   - Tìm phương thức `isNewSsaidSetting(Ljava/lang/String;)Z` trong `SettingsProvider.smali` và sửa thành luôn trả về `0 (false)` để mọi ứng dụng dùng chung một mã `android_id` toàn cục trong `Settings.Secure`.
   - Đóng gói lại và ký bằng `platform.jks`.
2. **Hook Fake IMEI / MEID / DeviceId tại tầng Telephony:**
   - Kéo `TeleService.apk` từ `/system/priv-app/TeleService/`, dịch ngược `PhoneInterfaceManager.smali`.
   - Kiểm tra cấu trúc hàm `getDeviceId`, `getImeiForSlot`, `getMeidForSlot` (tùy biến lại `patch_teleservice.py` cho khớp chính xác số thanh ghi và line code của ROM mới).
   - Tiêm hook đọc giá trị `fake_imei` từ `Settings.System` để trả về IMEI giả ngay lập tức không cần khởi động lại máy.
   - Đóng gói lại và ký bằng `platform.jks`.
3. **Cài đặt App điều khiển quyền Hệ thống (`UID 1000`):**
   - Cập nhật/biên dịch `DeviceIDManager` (thuộc tính `android:sharedUserId="android.uid.system"`, ký bằng `platform.jks`) và đẩy vào `/system/priv-app/DeviceIDManager/DeviceIDManager.apk`.
   - Xóa các thư mục cache `oat/` của các APK vừa sửa và `dalvik-cache` qua TWRP trước khi reboot.

#### Bước C: Tối ưu hiệu năng & Bộ nhớ (Không cần Root)
1. **Kích hoạt hoặc mở rộng zRAM (Swap nén):**
   - Kiểm tra `SwapTotal`. Nếu đang bằng 0 hoặc quá thấp, tạo script kích hoạt zRAM (ví dụ 1GB–2GB tùy dung lượng RAM thực tế) chạy tự động khi boot xong (`sys.boot_completed=1`).
   - *Lưu ý SELinux:* Không chạy ghi sysfs/proc từ app `UID 1000` nếu Android 10+ chặn quyền SELinux (`EACCES`). Hãy chạy từ script `init.d` hoặc tiêm vào file `.rc` trong `/system/etc/init/` dưới context root/init của hệ thống.
2. **Tối ưu CPU / GPU Turbo:**
   - Viết script boot-time giữ các nhân CPU hoạt động ổn định, tối ưu xung sàn (`scaling_min_freq`), `hispeed_freq` và GPU powerlevel phù hợp với SoC của máy mới (tránh quá nhiệt nhưng phản hồi mượt).
3. **Tối ưu hiển thị & Tốc độ giao diện:**
   - Đề xuất mức hạ độ phân giải hợp lý (`wm size` / `wm density`) để giảm tải GPU nếu màn hình gốc độ phân giải cao mà chip yếu.
   - Giảm `window_animation_scale`, `transition_animation_scale`, `animator_duration_scale` xuống `0.5`.
4. **Đóng băng Bloatware & Biên dịch AOT:**
   - Liệt kê các app nền ngốn RAM (như `com.google.android.googlequicksearchbox`, `setupwizard`, `feedback`...) và dùng `pm disable-user --user 0` để giải phóng RAM.
   - Chạy `cmd package compile -m speed -f` cho `SystemUI`, `Launcher`, `Settings` và `com.sec.deviceid`.

#### Bước D: Kiểm tra & Bàn giao
- Xác minh `which su` không tồn tại, Fake Android ID & IMEI đổi được tức thì trong `About Phone`, Swap/zRAM hoạt động, và lưu lại toàn bộ quy trình vào một file hướng dẫn `.md` mới trong `/home/vorza/note4_project_backup/`.
```
