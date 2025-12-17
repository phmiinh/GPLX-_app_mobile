# Hướng dẫn lấy Gemini API Key

## 📋 Các bước lấy API Key mới

### 1. Truy cập Google AI Studio
- Mở trình duyệt và vào: **https://aistudio.google.com/app/apikey**
- Đăng nhập bằng tài khoản Google của bạn

### 2. Tạo API Key
- Click nút **"Create API Key"** hoặc **"Get API key"**
- Chọn project Google Cloud (hoặc tạo project mới nếu chưa có)
- Copy API key được tạo (có dạng: `AIzaSy...`)

### 3. Cập nhật vào local.properties
- Mở file `local.properties` trong thư mục gốc của project
- Tìm dòng: `GEMINI_API_KEY=...`
- Thay thế bằng key mới:
  ```
  GEMINI_API_KEY=YOUR_NEW_API_KEY_HERE
  ```
- Lưu file

### 4. Rebuild App
- Trong Android Studio/Cursor: **File → Sync Project with Gradle Files**
- Hoặc chạy: **Build → Rebuild Project**

### 5. Test
- Mở app → Vào **Bookmarks** → Chọn một câu hỏi
- Nhấn nút **"Tạo giải thích"** trong màn hình chi tiết
- Nếu thành công, bạn sẽ thấy giải thích từ AI
- Nếu lỗi, kiểm tra lại key trong `local.properties`

## ⚠️ Lưu ý quan trọng

1. **Không commit `local.properties` lên Git**
   - File này đã có trong `.gitignore`
   - Chứa thông tin nhạy cảm

2. **API Key có thể hết hạn**
   - Nếu key hết hạn, tạo key mới và cập nhật lại
   - Google có thể giới hạn số lượng request/ngày

3. **Bảo mật**
   - Không chia sẻ API key công khai
   - Chỉ dùng cho development/testing
   - Production nên dùng backend server để gọi API

4. **Quota & Billing**
   - Google AI Studio có free tier với giới hạn
   - Kiểm tra quota tại: https://aistudio.google.com/app/apikey

## 🔧 Troubleshooting

### Lỗi: "GEMINI_API_KEY is empty"
- Kiểm tra `local.properties` có đúng format không
- Đảm bảo không có khoảng trắng thừa
- Rebuild project sau khi sửa

### Lỗi: "Gemini API call failed with code 403"
- API key không hợp lệ hoặc hết hạn
- Tạo key mới và cập nhật lại

### Lỗi: "Gemini API call failed with code 429"
- Đã vượt quá quota/rate limit
- Đợi một lúc rồi thử lại
- Hoặc nâng cấp plan trên Google Cloud

## 📚 Tài liệu tham khảo
- Google AI Studio: https://aistudio.google.com/
- Gemini API Docs: https://ai.google.dev/docs
- Pricing: https://ai.google.dev/pricing

