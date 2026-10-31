# EBS Mobile PDF – Android Belge Tarayıcı

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Build-Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle">
  <img src="https://img.shields.io/badge/Android%20SDK-26%2B-34A853?style=for-the-badge&logo=android" alt="Android SDK 26+">
  <img src="https://img.shields.io/badge/License-Apache--2.0-blue?style=for-the-badge" alt="Apache 2.0">
</p>

**EBS Mobile PDF**, Android cihazlarda belge ve sayfaları kamerayla taramak veya galeriden görsel olarak içe aktarmak, sayfaları düzenlemek ve tek bir PDF dosyasında birleştirmek için geliştirilmiş Java tabanlı bir uygulamadır.

Uygulama; tekli fotoğraf çekimi, seri tarama, çoklu görsel seçimi, sayfa önizleme/sıralama ve arka planda PDF üretimi işlevlerini bir araya getirir.

## ✨ Özellikler

- 📷 Android kamera ile tek sayfa fotoğraf çekimi
- 📸 Özel kamera ekranı üzerinden seri belge tarama
- 🖼️ Galeriden birden fazla görsel seçme
- 🔎 Sayfaları tam ekran görüntüleme ve sağa/sola kaydırarak gezinme
- ↕️ Sayfa sırasını yukarı/aşağı taşıyarak düzenleme
- 🗑️ Tek bir sayfayı veya tüm tarama listesini temizleme
- 📄 Görselleri çok sayfalı PDF belgesine dönüştürme
- ⚙️ PDF oluşturmayı arka planda yürütme ve işlem ilerlemesini izleme
- 💾 Oluşturulan PDF dosyasını cihazda saklama ve paylaşım/erişim için FileProvider kullanma
- 🔄 Uygulama yeniden açıldığında sayfa listesini geri yükleme

## 🧰 Teknoloji kartları

| Teknoloji | Kullanım amacı |
|---|---|
| ![Java](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white) | Android uygulama mantığı |
| ![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white) | Mobil platform ve yaşam döngüsü |
| ![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white) | Proje ve bağımlılık yönetimi |
| ![AndroidX](https://img.shields.io/badge/AndroidX-4285F4?logo=android&logoColor=white) | Activity, Core ve uyumluluk bileşenleri |
| ![WorkManager](https://img.shields.io/badge/WorkManager-4285F4?logo=android&logoColor=white) | Arka plan PDF üretim işi |
| ![Camera2](https://img.shields.io/badge/Camera2-34A853?logo=android&logoColor=white) | Kamera önizleme ve seri çekim |
| ![ExifInterface](https://img.shields.io/badge/ExifInterface-6E6E6E?logo=android&logoColor=white) | Fotoğraf yön bilgisini okuma |
| ![PdfDocument](https://img.shields.io/badge/Android%20PdfDocument-6E6E6E?logo=android&logoColor=white) | PDF sayfalarını oluşturma |

## 📁 Proje yapısı

```text
EBS-Mobile-PDF/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/ebs/tarayici/
│       │   ├── MainActivity.java       # Ana ekran, sayfa listesi ve uygulama akışı
│       │   ├── CameraActivity.java     # Camera2 tabanlı seri çekim ekranı
│       │   └── PdfWorker.java          # Arka planda PDF oluşturma
│       └── res/
│           ├── mipmap-*/               # Uygulama simgeleri
│           ├── values/styles.xml       # Tema
│           └── xml/file_paths.xml      # FileProvider dosya erişim yolları
├── build.gradle.kts                   # Android Gradle eklentisi
├── gradle.properties
├── settings.gradle.kts
├── LICENSE                            # Apache License 2.0
└── README.md
```

## ⚙️ Gereksinimler

- Android Studio
- JDK 11
- Android SDK Platform 34
- Android 8.0 (API 26) veya üzeri cihaz/emülatör
- Gradle Wrapper (projeyi Android Studio ile açtığınızda kullanılabilir)

> Proje ayarlarında `minSdk = 26`, `targetSdk = 28`, `compileSdk = 34` ve uygulama sürümü `2.0` olarak tanımlıdır.

## 🚀 Kurulum

1. Depoyu bilgisayarınıza klonlayın:

   ```bash
   git clone https://github.com/ebubekirbastama/EBS-Mobile-PDF.git
   ```

2. Android Studio üzerinden `EBS-Mobile-PDF` klasörünü açın.
3. Gradle senkronizasyonunun tamamlanmasını bekleyin.
4. Android 8.0 (API 26) veya üzeri bir cihaz/emülatör bağlayın.
5. Uygulamayı **Run** ile derleyip çalıştırın.

Komut satırından derlemek için proje kök dizininde:

```bash
./gradlew assembleDebug
```

Windows üzerinde:

```bat
gradlew.bat assembleDebug
```

Debug APK dosyası normalde `app/build/outputs/apk/debug/` altında oluşturulur.

## ▶️ Kullanım

1. Uygulamayı açın.
2. **MENÜ** üzerinden tek fotoğraf çekebilir veya galeriden görsel ekleyebilirsiniz.
3. Birden fazla sayfa taramak için **SERİ TARAMAYI BAŞLAT** seçeneğini kullanın.
4. Eklenen sayfaları listeden kontrol edin:
   - Küçük resme dokunarak tam ekran inceleyin.
   - Yukarı/aşağı kontrolleriyle sayfa sırasını değiştirin.
   - Silme kontrolüyle istemediğiniz sayfayı kaldırın.
5. **PDF OLUŞTUR** düğmesine basın.
6. İşlem tamamlandığında **PDF'İ KAYDET** seçeneğiyle oluşturulan dosyayı kaydedin.

PDF oluşturulurken işlem devam ettiği sürece sayfa listesini temizleme işlemi engellenir. Böylece devam eden işin kullandığı kaynak görsellerin silinmesi önlenir.

## 🔍 Çalışma akışı

1. **Görsel edinme:** Kamera çekimi veya Android belge seçici aracılığıyla görseller alınır.
2. **Yerel saklama:** Tarama görselleri uygulamanın özel `files/pages` dizinine kaydedilir; sayfa yolları uygulama tercihleri üzerinden korunur.
3. **Sayfa düzenleme:** Liste üzerinden önizleme, sıralama ve silme işlemleri yapılır.
4. **İş kuyruğu:** PDF üretim isteği WorkManager tabanlı `PdfWorker` sınıfına aktarılır.
5. **Görsel işleme:** Görsel boyutları okunur, bellek tüketimini sınırlamak için örnekleme uygulanır ve EXIF yön bilgisine göre döndürme yapılır.
6. **PDF üretimi:** Her görsel A4 ölçülerindeki PDF sayfasına oranı korunarak yerleştirilir.
7. **Dosyayı tamamlama:** Çıktı önce geçici `.partial` dosyasına yazılır; başarılı yazım sonrasında hedef PDF dosyasına taşınır.
8. **Sonuç:** İş tamamlandığında uygulama PDF yolunu alır ve kullanıcıya kaydetme akışını sunar.

## ⚠️ Teknik sınırlamalar

- Proje şu anda **OCR/metin tanıma**, otomatik kenar algılama, perspektif düzeltme veya belge filtreleri sunmuyor; PDF sayfaları fotoğraflardan oluşturuluyor.
- PDF sayfa boyutu kod içinde A4 (595 × 842) olarak sabitlenmiştir.
- Görseller PDF'e eklenirken belirli üst boyut sınırlarına göre küçültülür; bu, büyük belgelerde bellek kullanımını azaltırken ayrıntı kaybına neden olabilir.
- Kamera donanımı, kamera uygulaması ve Android üreticisinin Camera2 uygulaması cihazdan cihaza farklılık gösterebilir.
- Uygulama yapılandırmasında `targetSdk = 28` kullanılıyor. Güncel Android sürümleri ve mağaza dağıtımı hedefleniyorsa hedef SDK yükseltilmeli ve yeni depolama/izin davranışları test edilmelidir.
- Kamera izni Manifest içinde tanımlıdır; izin reddi, kalıcı ret ve ayarlardan izin verme senaryoları ayrıca kapsamlı biçimde test edilmelidir.
- Sayfa görselleri uygulamanın özel depolama alanında tutulur. Uygulama verileri silinirse henüz dışa aktarılmamış taramalar da kaybolabilir.
- Kodda bazı dosya silme ve kamera/bitmap işlemleri hata durumlarında sessizce geçilebiliyor; üretim kullanımında daha ayrıntılı hata kaydı ve kullanıcı geri bildirimi yararlı olacaktır.
- PDF dışa aktarma dizini uygulamanın özel alanındadır; dosyanın başka uygulamalarla paylaşılması veya ortak depolamaya aktarılması için kullanıcıya açık bir dışa aktarma akışı gerekir.

## 🔐 Güvenli kullanım ve gizlilik

- Kamera izni yalnızca belge fotoğrafı çekme işlevi için gereklidir.
- Tarama görselleri uygulamanın özel dosya alanında saklanır; uygulama kodunda uzak sunucuya yükleme akışı bulunmamaktadır.
- Android `FileProvider` dışarıya dosya yolu açmak yerine kontrollü içerik URI'si sağlar. Dosya paylaşımında geçici URI izinleri kullanılmalıdır.
- Manifest'te `allowBackup=false` tanımlıdır.
- Hassas kimlik, sözleşme veya finans belgelerini tararken cihaz kilidi kullanın ve PDF dosyalarını güvenli konumlarda saklayın.
- Uygulamayı yalnızca sahibi olduğunuz veya işleme yetkiniz bulunan belgeler üzerinde kullanın.

## 🛠️ Modernizasyon önerileri

- `targetSdk` değerini güncel Android gereksinimlerine yükseltin; izin ve medya erişimi akışlarını yeni Android sürümlerinde test edin.
- Java kodunu küçük sorumluluklara ayırarak ViewModel ve Repository katmanlarıyla bakım kolaylığı sağlayın.
- Kamera izni için Activity Result API ve güncel kamera yaşam döngüsü yönetimini değerlendirin.
- PDF dışa aktarmada Storage Access Framework (`ACTION_CREATE_DOCUMENT`) kullanarak kullanıcıya kayıt konumu seçtirin.
- OCR, otomatik belge sınırı algılama, perspektif düzeltme, döndürme/kırpma ve siyah-beyaz/kontrast filtreleri ekleyin.
- PDF sayfa boyutu, kenar boşluğu ve görüntü kalitesi ayarlarını kullanıcıya açın.
- WorkManager işine iptal, yeniden deneme ve ayrıntılı hata raporlama davranışları ekleyin.
- Büyük görsel koleksiyonları için küçük resimleri önbelleğe alın ve listeyi RecyclerView ile oluşturun.
- Birim testleri ve cihaz testleriyle sayfa sıralama, EXIF yönü, kesintiye uğrayan PDF üretimi ve dosya erişimini doğrulayın.

## 📄 Lisans

Bu proje depoda bulunan **Apache License 2.0** lisansı altında yayımlanmıştır. Ayrıntılar için [LICENSE](LICENSE) dosyasını inceleyin.

## 👨‍💻 Geliştirici

**Ebubekir Bastama**

- GitHub: [@ebubekirbastama](https://github.com/ebubekirbastama)
- Proje: [EBS-Mobile-PDF](https://github.com/ebubekirbastama/EBS-Mobile-PDF)

---

<p align="center">EBS Mobile PDF • Android belge tarama ve PDF oluşturma uygulaması</p>
