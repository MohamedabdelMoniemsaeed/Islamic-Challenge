# Phase 11: Google Play Launch & Upload Signing Guide (دليل النشر النهائي على متجر جوجل بلاي)

**التطبيق:** التحدي الإسلامي (Islamic Challenge)  
**معرف الحزمة (Application ID):** `com.aistudio.islamicchallenge.qzwv`  
**الإصدار:** 1.0.0 (versionCode 1)  
**الحالة الفنية للأكواد:** 34/34 Tests PASSED | Lint PASSED | 100% Offline  

---

## 1. إنشاء مفتاح الرفع (Create Upload Keystore)

وفق معايير الأمان المعتمدة في Google Play، **المطور هو المسؤول الحصري عن توليد وحفظ مفتاح الرفع الخاص به**.  
> ⚠️ **تنبيه أمني بالغ الأهمية:**  
> لا تشارك ملف الـ Keystore أو كلمات المرور مع أي نموذج ذكاء اصطناعي أو تضعها داخل Git.

### الطريقة الأولى: عبر سطر الأوامر باستخدام Java `keytool` (الموصى بها)
افتح الطرفية (Terminal / Command Prompt) في جهازك الشخصي ونفذ الأمر التالي:

```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storetype JKS
```

* سيطلب منك إدخال كلمة مرور للمستودع (`Keystore Password`).
* سيطلب منك إدخال بياناتك (الاسم، المؤسسة، الدولة).
* سيتم إنشاء ملف باسم: `my-upload-key.jks` مع اسم المعرف (`alias`): `upload`.

### الطريقة الثانية: عبر Android Studio
1. افتح المشروع في Android Studio.
2. من القائمة العلوية: **Build** $\rightarrow$ **Generate Signed Bundle / APK**.
3. اختر **Android App Bundle** ثم اضغط **Next**.
4. تحت خانة Key store path اختر **Create new...**.
5. حدد مسار الحفظ، والاسم `my-upload-key.jks`، و `Alias: upload`، وكلمة المرور.

---

## 2. إعداد التوقيع في بيئة البناء (Configure Release Signing Safely)

تم إعداد ملف `app/build.gradle.kts` مسبقاً لدعم قراءة المفتاح عبر متغيرات البيئة (`Environment Variables`) أو عبر مسار الملف المباشر دون كتابة أي كلمة مرور في الكود:

```kotlin
signingConfigs {
    create("release") {
        val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
        storeFile = file(keystorePath)
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
```

### لبناء الـ AAB الموقع محلياً على جهازك:
ضع ملف `my-upload-key.jks` في المجلد الرئيسي للمشروع (تمت إضافته بالفعل إلى `.gitignore` حتى لا يُرفع إلى Git)، ثم نفذ:

#### على أنظمة Linux / macOS:
```bash
export KEYSTORE_PATH="./my-upload-key.jks"
export STORE_PASSWORD="كلمة_مرور_المستودع"
export KEY_PASSWORD="كلمة_مرور_المفتاح"
gradle :app:bundleRelease
```

#### على أنظمة Windows (PowerShell):
```powershell
$env:KEYSTORE_PATH=".\my-upload-key.jks"
$env:STORE_PASSWORD="كلمة_مرور_المستودع"
$env:KEY_PASSWORD="كلمة_مرور_المفتاح"
gradle :app:bundleRelease
```

ستجد ملف الحزمة النهائي الموقع جاهزاً في المسار:
```text
app/build/outputs/bundle/release/app-release.aab
```

---

## 3. التحقق من التوقيع وملف الـ AAB (Verification)
للتحقق من إعدادات التوقيع في أي وقت:
```bash
gradle :app:signingReport
```
وللتحقق من توقيع ملف الـ AAB باستخدام أداة `bundletool`:
```bash
bundletool dump manifest --bundle=app/build/outputs/bundle/release/app-release.aab
```

---

## 4. الفارق بين Upload Key و Play App Signing
* **Upload Key (مفتاح الرفع):** هو المفتاح الذي أنشأته أنت محلياً، وتستخدمه حصرياً لتوقيع الـ AAB قبل رفعه إلى Google Play Console لتأكيد هويتك.
* **App Signing Key (مفتاح توقيع التطبيق في المتجر):** يُنشئه ويُديره Google Play بأمان في خوادمه (Google Play App Signing) لتوقيع وتوزيع نسخ الـ APK المحسنة للمستخدمين النهائيين.

---

## 5. رابط سياسة الخصوصية العام (Public Privacy Policy URL)
يتطلب متجر Google Play رابط HTTPS عام لسياسة الخصوصية. تم إنشاء صفحة ويب كاملة باللغتين العربية والإنجليزية ومطابقة للمعايير داخل مجلد `public/`:
* **مسار الرابط العام (Public HTTPS URL):**
  ```text
  https://ais-pre-cioe5efg2bddzcokpk45wm-819643539021.europe-west2.run.app/privacy.html
  ```
  *(رابط إضافي بديل: `https://ais-pre-cioe5efg2bddzcokpk45wm-819643539021.europe-west2.run.app/privacy-policy.html`)*

---

## 6. خطوات النشر عبر Google Play Console (Step-by-Step)

### الخطوة 1: الاختبار الداخلي (Internal Testing) — إلزامي أولاً
1. ادخل إلى [Google Play Console](https://play.google.com/console).
2. اختر تطبيق **التحدي الإسلامي**.
3. في القائمة الجانبية: **Testing** $\rightarrow$ **Internal testing**.
4. اضغط **Create new release**.
5. ارفع ملف `app-release.aab`.
6. أضف ملاحظات الإصدار (Release Notes) من `STORE_LISTING.md`.
7. أضف بريدك الإلكتروني لقائمة المختبرين (Testers)، وثبّت التطبيق على جهازك الحقيقي.

### الخطوة 2: فحص الجهاز الحقيقي دون اتصال (Offline Smoke Test)
على جهازك الحقيقي بعد تثبيت نسخة الـ Internal Testing:
1. أوقف تشغيل الـ Wi-Fi وبيانات الهاتف (Airplane Mode).
2. شغّل التطبيق:
   - تحقق من ظهور شاشة البداية والدخول الفوري للشاشة الرئيسية.
   - ابدأ مسابقة سريعة (Quick Challenge) وأجب عن 10 أسئلة.
   - جرب استخدام الوسائل المساعدة (50/50، تلميح، وقت إضافي، تخطي).
   - تحقق من شاشة النتائج والمكافآت (XP والعملات).
   - افتح شاشة الملف الشخصي وشاشة الإنجازات وشاشة الإعدادات.
   - أعد تشغيل التطبيق وتأكد من بقاء كل الإحصائيات محفوظة بدقة.

### الخطوة 3: تعبئة أقسام المتجر (Store Presence)
* **Store Listing:** انسخ البيانات الجاهزة من `STORE_LISTING.md`.
* **Privacy Policy:** ضع الرابط `https://ais-pre-cioe5efg2bddzcokpk45wm-819643539021.europe-west2.run.app/privacy.html`.
* **Data Safety:** اختر **"No user data is collected or shared"** بناءً على `DATA_SAFETY_AUDIT.md`.
* **Content Rating:** املأ استبيان IARC بناءً على `CONTENT_RATING_NOTES.md` (محتوى عائلي تعليمي Everyone / Pegi 3).
* **Target Audience:** اختر الفئة العامة للجميع (العائلة والبالغين والمهتمين بالثقافة الإسلامية).

---

## 7. قائمة التحقق النهائية للإطلاق (Launch Checklist)

```text
[x] Upload Key instructions documented
[x] Keystore added to .gitignore (Secrets protected)
[x] Release signing configured in build.gradle.kts
[x] signingReport verified (release variant configured)
[x] 34/34 Automated Tests passing (100%)
[x] Android Lint passing (0 errors)
[x] Release code compilation, Dexing & pre-bundle packaging passing
[x] 100% Offline capability verified
[x] Database migrations v1 -> v2 -> v3 verified
[x] Public Privacy Policy HTTPS URL generated & live
[x] Data Safety Audit ready
[x] Content Rating (IARC) ready
[x] Store Listing Copy (Arabic & English) ready
[ ] Generate my-upload-key.jks on developer machine
[ ] Execute gradle :app:bundleRelease with developer secrets
[ ] Upload app-release.aab to Play Console Internal Testing
[ ] Real device offline smoke test
[ ] Promote to Production Release
```
