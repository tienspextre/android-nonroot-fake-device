import sys

path = sys.argv[1] if len(sys.argv) > 1 else 'PhoneInterfaceManager.smali'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. getDeviceId
t1 = """.method public getDeviceId(Ljava/lang/String;)Ljava/lang/String;
    .registers 7

    .line 3479
    const/4 v0, 0x0"""

r1 = """.method public getDeviceId(Ljava/lang/String;)Ljava/lang/String;
    .registers 7

    iget-object v0, p0, Lcom/android/phone/PhoneInterfaceManager;->mApp:Lcom/android/phone/PhoneGlobals;

    if-eqz v0, :cond_fake_devid_skip

    invoke-virtual {v0}, Lcom/android/phone/PhoneGlobals;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    if-eqz v0, :cond_fake_devid_skip

    const-string v1, "fake_imei"

    invoke-static {v0, v1}, Landroid/provider/Settings$System;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_fake_devid_skip

    return-object v0

    :cond_fake_devid_skip
    .line 3479
    const/4 v0, 0x0"""

assert t1 in content, "Target 1 (getDeviceId) not found!"
content = content.replace(t1, r1, 1)

# 2. getImeiForSlot
t2 = """.method public getImeiForSlot(ILjava/lang/String;)Ljava/lang/String;
    .registers 7

    .line 1836
    invoke-static {p1}, Lcom/android/internal/telephony/PhoneFactory;->getPhone(I)Lcom/android/internal/telephony/Phone;"""

r2 = """.method public getImeiForSlot(ILjava/lang/String;)Ljava/lang/String;
    .registers 7

    iget-object v0, p0, Lcom/android/phone/PhoneInterfaceManager;->mApp:Lcom/android/phone/PhoneGlobals;

    if-eqz v0, :cond_fake_imei_skip

    invoke-virtual {v0}, Lcom/android/phone/PhoneGlobals;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    if-eqz v0, :cond_fake_imei_skip

    const-string v1, "fake_imei"

    invoke-static {v0, v1}, Landroid/provider/Settings$System;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_fake_imei_skip

    return-object v0

    :cond_fake_imei_skip
    .line 1836
    invoke-static {p1}, Lcom/android/internal/telephony/PhoneFactory;->getPhone(I)Lcom/android/internal/telephony/Phone;"""

assert t2 in content, "Target 2 (getImeiForSlot) not found!"
content = content.replace(t2, r2, 1)

# 3. getMeidForSlot
t3 = """.method public getMeidForSlot(ILjava/lang/String;)Ljava/lang/String;
    .registers 7

    .line 1850
    invoke-static {p1}, Lcom/android/internal/telephony/PhoneFactory;->getPhone(I)Lcom/android/internal/telephony/Phone;"""

r3 = """.method public getMeidForSlot(ILjava/lang/String;)Ljava/lang/String;
    .registers 7

    iget-object v0, p0, Lcom/android/phone/PhoneInterfaceManager;->mApp:Lcom/android/phone/PhoneGlobals;

    if-eqz v0, :cond_fake_meid_skip

    invoke-virtual {v0}, Lcom/android/phone/PhoneGlobals;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    if-eqz v0, :cond_fake_meid_skip

    const-string v1, "fake_imei"

    invoke-static {v0, v1}, Landroid/provider/Settings$System;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_fake_meid_skip

    return-object v0

    :cond_fake_meid_skip
    .line 1850
    invoke-static {p1}, Lcom/android/internal/telephony/PhoneFactory;->getPhone(I)Lcom/android/internal/telephony/Phone;"""

assert t3 in content, "Target 3 (getMeidForSlot) not found!"
content = content.replace(t3, r3, 1)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Patch applied successfully with Settings$System!")
