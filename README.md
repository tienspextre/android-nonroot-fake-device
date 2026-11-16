# Android Non-Root Custom ROM: Fake Device & Performance Toolkit

Bộ công cụ, mã nguồn và Prompt mẫu để tùy biến các máy Android (Custom ROM / GSI với TWRP Recovery) phục vụ **Fake Device ID / IMEI toàn hệ thống** và **Tối ưu hiệu năng (CPU/GPU/zRAM)** mà **hoàn toàn KHÔNG CẦN ROOT** (không Magisk, không `su`), đảm bảo sạch sẽ với ứng dụng ngân hàng, ví điện tử, Shopee, v.v.

## Cấu trúc thư mục

- [`PROMPT_CUSTOM_ROM.md`](PROMPT_CUSTOM_ROM.md): Prompt chuẩn để giao cho AI Agent thực hiện quy trình tương tự trên các thiết bị mới.
- [`docs/HUONG_DAN_NOTE4.md`](docs/HUONG_DAN_NOTE4.md): Tài liệu chi tiết thực tế trên Samsung Galaxy Note 4 (`SM-N910V`, Android 9 Pie).
- [`docs/HUONG_DAN_REALME_C2.md`](docs/HUONG_DAN_REALME_C2.md): Tài liệu tối ưu hiệu năng & 1GB zRAM trên Realme C2 (`RMX1941`, Android 10 GSI).
- [`devid_pro/`](devid_pro/): Mã nguồn Java & tài nguyên của ứng dụng **Device ID Changer Pro** (`UID 1000 System`).
- [`DeviceIDManager_v3.apk`](DeviceIDManager_v3.apk): Bản APK đã biên dịch và ký sẵn bằng khóa nền tảng AOSP Testkey.
- [`patch_teleservice.py`](patch_teleservice.py): Script tiêm hook đọc `fake_imei` vào `PhoneInterfaceManager.smali` của `TeleService.apk`.
- [`build_and_deploy.sh`](build_and_deploy.sh): Script biên dịch lại `devid_pro/` thành `DeviceIDManager_v3.apk`.
- [`98cpu_boost`](98cpu_boost) & [`swap_enable.sh`](swap_enable.sh): Các script mẫu tối ưu CPU/GPU và kích hoạt zRAM lúc khởi động không cần root.
- Bộ công cụ Java đi kèm: `apktool.jar`, `baksmali.jar`, `smali.jar`, `r8.jar`, `uber-apk-signer.jar`, `platform.jks`.

## Cách sử dụng cho máy mới

1. Khởi động máy mới vào **TWRP Recovery** và kết nối ADB với máy tính.
2. Mở [`PROMPT_CUSTOM_ROM.md`](PROMPT_CUSTOM_ROM.md), copy phần nội dung Prompt và gửi cho AI Agent để Agent tự động khảo sát thiết bị, patch `SettingsProvider.apk` / `TeleService.apk`, cài `DeviceIDManager` và tối ưu CPU/zRAM phù hợp.
