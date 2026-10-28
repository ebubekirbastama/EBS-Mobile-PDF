# 📱 EBS Mobile PDF — Android Belge Tarayıcı

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Language-Java-orange?logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Build-Gradle%20Kotlin%20DSL-02303A?logo=gradle&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-blue)
![License](https://img.shields.io/badge/License-MIT-green)

**EBS Mobile PDF**, Android cihazda kamerayla veya galeriden aldığınız görüntüleri sayfa sırasına koyup tek bir PDF belgesine dönüştürmek için hazırlanmış bir belge tarama uygulamasıdır. PDF üretimi arka planda çalışır; hazır dosya Android'in belge kaydetme ekranı üzerinden seçtiğiniz konuma aktarılabilir.

## ✨ Özellikler

- 📷 Kamerayla sayfa fotoğrafı çekme
- 🖼️ Galeriden tek veya birden fazla görsel seçme
- 🔁 Seri çekim akışıyla art arda sayfa ekleme
- ↕️ Sayfaları yukarı/aşağı taşıyarak sıralama
- 🗑️ İstenmeyen sayfaları listeden kaldırma
- 📄 Birden fazla görseli tek PDF dosyasında birleştirme
- ⚙️ PDF üretimini Android WorkManager üzerinden arka planda yürütme ve ilerleme bilgisini gösterme
- 🧭 EXIF yön bilgisinden fotoğraf yönünü düzeltme
- 💾 Sayfa listesini uygulama içinde saklayarak uygulama yeniden açıldığında geri yükleme
- 📤 Android belge seçicisi ile PDF'yi hedef konuma kaydetme

## 🧰 Teknoloji kartları

| Teknoloji | Kullanım |
|---|---|
| Java | Android uygulama mantığı |
| Android SDK | Kamera, medya seçimi, dosya ve PDF işlemleri |
| AndroidX Activity / Core | Activity altyapısı ve güvenli dosya URI'leri |
| WorkManager 2.8.1 | Arka plan PDF üretimi ve iş durumu takibi |
| Android PdfDocument | PDF sayfalarını oluşturma |
| ExifInterface | Fotoğrafların EXIF yön bilgisini okuma |
| Gradle Kotlin DSL | Proje ve bağımlılık yapılandırması |

## 📁 Proje yapısı

```text
EBS-Mobile-PDF/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/ebs/tarayici/
│       │   ├── MainActivity.java    # Arayüz, görsel seçimi, sayfa yönetimi
│       │   └── PdfWorker.java       # Görsellerden PDF üretimi
│       └── res/
│           ├── mipmap-*/            # Uygulama simgeleri
│           ├── values/              # Tema ve kaynaklar
│           └── xml/file_paths.xml   # FileProvider dosya yolları
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .gitignore
├── LICENSE
└── README.md
```

## 🚀 Kurulum

### Gereksinimler

- Android Studio ve Android SDK
- JDK 11 ile uyumlu geliştirme ortamı
- Android 8.0 (API 26) veya üzeri bir cihaz/emülatör
- Gradle bağımlılıklarını indirebilmek için internet bağlantısı

> Depoda Gradle Wrapper dosyaları bulunmuyorsa projeyi Android Studio ile açın; IDE'nin önerdiği Gradle yapılandırmasını kullanın veya yerel Gradle kurulumuyla derleyin.

### Android Studio ile

1. Depoyu klonlayın:

   ```bash
   git clone https://github.com/ebubekirbastama/EBS-Mobile-PDF.git
   ```

2. Android Studio'da **Open** ile klonlanan proje klasörünü açın.
3. Gradle senkronizasyonunun tamamlanmasını bekleyin.
4. Android cihazınızı USB hata ayıklama açık şekilde bağlayın veya bir emülatör başlatın.
5. **Run** ile uygulamayı derleyip çalıştırın.

### APK oluşturma

Android Studio'daki **Build > Build APK(s)** seçeneğini kullanabilir veya proje kökünde Gradle kurulumunuz mevcutsa:

```bash
gradle assembleDebug
```

Oluşan debug APK genellikle `app/build/outputs/apk/debug/app-debug.apk` yolundadır.

## ▶️ Kullanım

1. Uygulamayı açın.
2. **Fotoğraf Çek** ile kameradan bir sayfa ekleyin veya **Galeriden Ekle** ile cihazdaki görselleri seçin.
3. Çok sayfalı belge için **Seri Çekim** akışını kullanarak sonraki sayfaları ekleyin.
4. Sayfaları önizleme listesindeki yukarı/aşağı kontrolleriyle sıralayın; gereksiz sayfaları kaldırın.
5. **Sayfaları PDF Yap** düğmesine dokunun ve arka plan işleminin tamamlanmasını bekleyin.
6. İşlem başarılı olduğunda **Oluşturulan PDF'i Kaydet** düğmesine dokunup hedef konumu seçin.

PDF sayfaları A4'e yakın sabit bir sayfa boyutuna yerleştirilir; görseller sayfa oranı korunarak sığdırılır.

## 🔍 Çalışma akışı

1. Kamera çıktısı uygulamanın özel `files/pages/` dizinine kaydedilir. Galeri görselleri de bu alana kopyalanır.
2. Sayfa yolları uygulama tercihleri içinde saklanır; geçersiz veya artık bulunmayan dosyalar geri yükleme sırasında atlanır.
3. PDF oluşturma isteği, görsel yolları ve hedef dosya adıyla WorkManager kuyruğuna eklenir.
4. `PdfWorker`, görselleri bellek kullanımını azaltmak için uygun ölçekte açar, EXIF dönüşünü uygular ve her görseli PDF sayfasına yerleştirir.
5. İşlem ilerlemesi WorkManager üzerinden izlenir. Başarılı işlemde PDF uygulamanın özel `files/exports/` dizinine yazılır.
6. Kullanıcı Android belge oluşturma arayüzüyle PDF'yi seçtiği konuma kopyalar.

## ⚠️ Teknik sınırlamalar

- Uygulama şu anda görüntüleri PDF'ye dönüştürür; OCR, metin tanıma, otomatik belge kenarı algılama veya perspektif düzeltme içermez.
- PDF sayfa ölçüsü kodda sabitlenmiştir (`595 × 842`); farklı kâğıt boyutları seçilemez.
- Görseller PDF'ye eklenmeden önce örnekleme ile küçültülebilir. Bu bellek tüketimini azaltırken çok yüksek çözünürlüklü belgelerde ayrıntı kaybına neden olabilir.
- Seri çekim, her fotoğraf sonrası kullanıcının onayladığı bir kamera döngüsüdür; otomatik ve kesintisiz kamera kontrolü değildir.
- Galeri aktarımında hata oluşursa işlem kısmen tamamlanmış olabilir; sayfa listesini kontrol edin.
- Uygulama içindeki sayfa dosyaları ve PDF dışa aktarımları uygulamanın özel depolama alanındadır; uygulama kaldırıldığında bu dosyalar silinebilir.
- Manifest'te `targetSdk = 28` kullanılıyor. Güncel Android sürümleri ve mağaza dağıtımı için hedef SDK'nın yükseltilmesi gerekir.
- Kaynakta bazı hatalar kullanıcıya metin olarak iletilir; kapsamlı loglama, otomatik kurtarma ve ayrıntılı hata raporlama sınırlıdır.

## 🔐 Güvenli kullanım

- Fotoğraflar ve PDF'ler kişisel belge veya kimlik bilgileri içerebilir. Dışa aktarım konumunu seçerken dikkatli olun.
- Uygulama, dosya paylaşımı için dışa açık olmayan AndroidX `FileProvider` kullanır; dosyaları genel bir URL üzerinden sunmaz.
- Hassas belgeleri herkese açık klasörlerde tutmayın ve paylaşmadan önce PDF içeriğini kontrol edin.
- Bu depo, dosyaların buluta yüklendiği bir hizmet tanımlamıyor; yine de cihazınızın yedekleme ve paylaşım ayarlarını ayrıca gözden geçirin.
- Bilinmeyen veya güvenilmeyen kaynaklardan gelen APK'ları yüklemeyin.

## 🛠️ Modernizasyon önerileri

- `targetSdk` değerini güncel Android gereksinimlerine göre yükseltin ve modern izin/medya seçici davranışlarını test edin.
- Gradle Wrapper ekleyip Gradle ve Android Gradle Plugin sürümlerini sabitleyin.
- Görüntüleri sıkıştırma kalitesi, PDF sayfa boyutu ve yönlendirme seçeneklerini kullanıcıya açın.
- OCR, otomatik kenar algılama, kırpma, döndürme ve filtre özellikleri ekleyin.
- Büyük belgeler için bellek kullanımını ve iptal/yeniden deneme senaryolarını test edin; kısmi dosyaların temizliğini güçlendirin.
- Sayfa listesini `SharedPreferences` yerine sürümlenebilir bir veri modeliyle saklamayı ve kalıcı işler için daha sağlam durum yönetimini değerlendirin.
- Birim testleri ve cihaz testleri ekleyin; PDF çıktısının sayfa sayısı, yönü ve dosya bütünlüğünü doğrulayın.
- Erişilebilirlik etiketleri, çoklu dil desteği ve hata günlükleme mekanizması ekleyin.

## 📜 Lisans

Depoda bulunan [LICENSE](LICENSE) dosyasına bakın. Lisans koşullarını dağıtım veya yeniden kullanım öncesinde inceleyin.

## 👨‍💻 Geliştirici

**Ebubekir Bastama**

- GitHub: [@ebubekirbastama](https://github.com/ebubekirbastama)
- Proje: [EBS-Mobile-PDF](https://github.com/ebubekirbastama/EBS-Mobile-PDF)

---

<p align="center">EBS Mobile PDF · Android üzerinde pratik belge tarama ve PDF oluşturma</p>
