# دستیار صوتی فارسی - Persian AI Voice Assistant

یک دستیار صوتی هوشمند برای Android که با زبان فارسی کار می‌کند. کاربر می‌تواند با دستیار صحبت کند،، درخواست‌های مختلف بدهد (تماس، پیام، آلارم، جستجو، محاسبه، و غیره)، و پاسخ صوتی فارسی دریافت کند.

---

## معرفی

این پروژه یک دستیار صوتی پیشرفته برای گوشی‌های Android است که:

- 🎤 **گوش می‌دهد** - صدای فارسی کاربر را تشخیص می‌دهد (fa-IR)
- 🧠 **فکر می‌کند** - با استفاده از LLM (OpenRouter/OpenAI) منظور را می‌فهمد
- 🛠️ **عمل می‌کند** - ابزارهای مختلف (تماس، پیام، آلارم، جستجو، ...) را اجرا می‌کند
- 🔊 **پاسخ می‌دهد** - پاسخ صوتی فارسی برمی‌گرداند
- 💾 **به‌خاطر می‌سپارد** - اطلاعات مهم را در حافظه بلندمدت ذخیره می‌کند

---

## Features

### تشخیص گفتار فارسی
- پشتیبانی از زبان فارسی (fa-IR)
- نتایج Partial و Final
- Streaming با انیمیشن Listening

### پاسخ صوتی فارسی
- Android TextToSpeech
- قابل تنظیم سرعت و زیر و بمی صدا
- قابلیت Interrupt (متوقف کردن پاسخ)

### ابزارها (Tools)
- 🧮 ماشین حساب
- 📞 تماس تلفنی (با تأیید)
- 💬 پیام کوتاه (با تأیید)
- 📧 جستجوی مخاطب
- ⏰ آلارم و یادآور
- 📅 رویداد تقویم
- 🌐 باز کردن مرورگر
- 🔍 جستجوی وب
- ☀️ وضعیت هوا
- 🔋 وضعیت باتری
- 🕐 ساعت و تاریخ شمسی
- 📱 باز کردن برنامه‌ها

### LLM Provider
- **OpenRouter** (پیش‌فرض) - پشتیبانی از تمام مدل‌ها (Claude، GPT-4، Llama، و غیره)
- قابل تنظیم در Settings
- API Key در EncryptedSharedPreferences

### حافظه (Memory)
- Conversation Memory
- Long Term Memory (مثلاً نام همسر)
- User Preferences
- Task History

### امنیت
- API Key در EncryptedSharedPreferences
- تأیید کاربر برای عملیات حساس
- حداقل Permission
- لاگ‌های امن (بدون اطلاعات حسشاس)

### شخصی‌سازی
- تم تاریک/روشن
- سرعت و زیر و بمی صدا
- زبان (فارسی، انگلیسی)
- Auto Speak
- Wake Word (در حال توسعه)

---

## Architecture

```
             ┌───────────────┐
             │     User      │
             └───────┬───────┘
                     │ Voice
                     ▼
             ┌───────────────┐
             │ SpeechToText  │
             └───────┬───────┘
                     │
                     ▼
             ┌───────────────┐
             │ Normalizer    │
             └───────┬───────┘
                     │
              ┌──────┴──────┐
              ▼             ▼
       Local Command      Agent Engine
              │             │
              │             ▼
              │          LLM Router
              │             │
              │             ▼
              │         Tool Calling
              │             │
              └──────┬──────┘
                     ▼
              Tool Execution
                     │
                     ▼
                 Result
                     │
                     ▼
              Response Builder
                     │
                     ▼
                TextToSpeech
                     │
                     ▼
                   User
```

### ساختار ماژولار

```
PersianVoiceAssistant/
├── app/                      # اپ اصلی
├── core/
│   ├── common/                # Logger، PersianDate، Errors
│   ├── database/              # Room
│   ├── network/               # Retrofit، OkHttp
│   ├── ai/                    # LLM، Agent Engine، Conversation Engine
│   ├── voice/                 # STT، TTS
│   └── permissions/           # Permission Manager
├── feature/
│   ├── home/
│   ├── conversation/
│   ├── settings/
│   ├── memory/
│   └── tools/
├── domain/
│   ├── model/
│   ├── repository/            # Interfaces
│   └── usecase/
├── data/
│   ├── repository/            # Implementations
│   ├── datasource/            # Room DAOs
│   └── model/                 # Entities
└── tools/
    ├── phone/
    ├── contacts/
    ├── sms/
    ├── calendar/
    ├── alarm/
    ├── browser/
    ├── weather/
    ├── calculator/
    └── device/
```

### معماری

```
UI (Compose)
   ↓
ViewModel
   ↓
UseCase
   ↓
Repository (Interface)
   ↓
DataSource / AI / Android APIs
```

---

## نصب و Build

### پیش‌نیازها
- JDK 17
- Android SDK 35
- Gradle 8.10.2 (wrapper داخل پروژه)

### Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Test
./gradlew test

# Lint
./gradlew lint
```

### نصب روی دستگاه

```bash
./gradlew installDebug
```

---

## تنظیمات

### اولین اجرا
1. اپ را باز کنید
2. مجوز Microphone را تأیید کنید
3. به **Settings → AI Provider** بروید
4. API Key OpenRouter را وارد کنید
5. Model را انتخاب کنید (مثلاً `anthropic/claude-3.5-sonnet`)

### دریافت OpenRouter API Key
به https://openrouter.ai مراجعه کنید و یک حساب بسازید.

---

## LLM Providers

### OpenRouter (توصیه‌شده)
- Base URL: `https://openrouter.ai/api/v1`
- Model: `anthropic/claude-3.5-sonnet`, `openai/gpt-4o`, و غیره
- پشتیبانی از streaming

### OpenAI
- Base URL: `https://api.openai.com/v1`
- Model: `gpt-4o-mini`, `gpt-4o`

### سایر (OpenAI-compatible)
هر Provider که از OpenAI API format پشتیبانی کند، قابل استفاده است.

---

## Permissions

اپ فقط مجوزهای زیر را درخواست می‌کند:

- **Microphone** - برای تشخیص گفتار (در اولین اجرا)
- **Internet** - برای ارتباط با LLM
- **Contacts** - در زمان نیاز (جستجوی مخاطب)
- **Phone** - در زمان نیاز (تماس)
- **SMS** - در زمان نیاز (ارسال پیام)
- **Calendar** - در زمان نیاز (ایجاد رویداد)
- **Location** - در زمان نیاز (وضعیت هوا)
- **Notifications** - برای نوتیفیکیشن

هیچ‌گاه همه مجوزها یکجا درخواست نمی‌شوند.

---

## Voice

- **زبان پیش‌فرض:** فارسی (fa-IR)
- **تشخیص گفتار:** Android SpeechRecognizer
- **پاسخ صوتی:** Android TextToSpeech
- **Streaming:** پشتیبانی از نتایج Partial

---

## Tools

تمام قابلیت‌ها از طریق Tool System قابل دسترسی هستند. هر Tool دارای:
- `id` (مثل `phone.call`)
- `description` (برای LLM)
- `parameters` (JSON Schema)
- `riskLevel` (LOW/MEDIUM/HIGH)

برای اضافه کردن Tool جدید:
1. کلاس خود را implement `AssistantTool` کنید
2. در `ToolModule.bindXxx` آن را با `@IntoSet` ثبت کنید
3. به‌صورت خودکار در Registry ثبت می‌شود

---

## Database

- **Type:** Room + SQLite
- **Tables:**
  - `conversations`
  - `messages`
  - `memories`
  - `user_preferences`
  - `tasks`
  - `tool_executions`

- **Migration:** از طریق `PersianVoiceAssistantDatabase.Companion` قابل اضافه شدن
- **Backup:** غیرفعال (`android:allowBackup="false"`)

---

## Testing

```bash
# Unit Tests
./gradlew test

# Instrumented Tests
./gradlew connectedAndroidTest

# با Coverage
./gradlew testDebugUnitTestCoverage
```

### Test Coverage
- `PersianNormalizerTest`
- `KeywordIntentDetectorTest`
- `CalculatorToolTest`
- `ToolRegistryTest`

---

## Troubleshooting

### میکروفون کار نمی‌کند
- مجوز Microphone را در Settings دستگاه بررسی کنید
- اپ را مجدد راه‌اندازی کنید

### TTS فارسی نصب نیست
- به Google Play → "Google TTS" → دانلود صدای فارسی
- در TTS Engine Settings دستگاه فعال کنید

### LLM پاسخ نمی‌دهد
- API Key را در Settings بررسی کنید
- اتصال اینترنت را بررسی کنید
- در Settings → AI Provider، Provider را تغییر دهید

---

## Roadmap

### نسخه 1.0 (فعلی)
- ✅ تشخیص گفتار فارسی
- ✅ پاسخ صوتی فارسی
- ✅ OpenRouter LLM
- ✅ ابزارهای اصلی
- ✅ حافظه بلندمدت
- ✅ تأیید کاربر

### نسخه 1.1
- ⏳ Wake Word ("سلام دستیار")
- ⏳ Streaming پاسخ LLM
- ⏳ Vision (دوربین)
- ⏳ Vision API

### نسخه 2.0
- ⏳ Local LLM (Offline)
- ⏳ Local STT
- ⏳ WhatsApp/Telegram Integration
- ⏳ Home Automation

### آینده
- Wear OS
- Android Auto
- Smart Home
- AI Agent Manager Integration

---

## مجوز

MIT License - استفاده آزاد با حفظ Copyright

---

## تشکر

از تمام کتابخانه‌های Open Source استفاده‌شده سپاسگزاریم:
- Jetpack Compose
- Hilt
- Room
- Retrofit
- Kotlin Coroutines
- Material 3
- و ده‌ها کتابخانه دیگر

ساخته‌شده با ❤️ برای فارسی‌زبانان