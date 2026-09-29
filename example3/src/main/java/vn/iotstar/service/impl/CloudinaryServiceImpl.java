package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    private boolean isConfigured() {
        if (cloudinary == null || cloudinary.config == null) {
            return false;
        }
        String cloudName = cloudinary.config.cloudName;
        String apiKey = cloudinary.config.apiKey;
        String apiSecret = cloudinary.config.apiSecret;
        return cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank();
    }

    @Override
    public CloudinaryUploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn ảnh");
        }

        String type = file.getContentType();
        if (type == null || !type.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ cho phép file hình ảnh");
        }

        if (!isConfigured()) {
            log.warn("Cloudinary chưa được cấu hình. Sử dụng demo image placeholder.");
            return new CloudinaryUploadResult(
                    "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                    "sample"
            );
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder", "shop/products")
            );
            return new CloudinaryUploadResult(
                    String.valueOf(result.get("secure_url")),
                    String.valueOf(result.get("public_id"))
            );
        } catch (Exception e) {
            log.error("Cloudinary upload failed: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Upload ảnh lên Cloudinary thất bại: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank() || "sample".equalsIgnoreCase(publicId)) {
            return;
        }
        if (!isConfigured()) {
            log.debug("Cloudinary chưa được cấu hình. Bỏ qua xóa publicId [{}]", publicId);
            return;
        }
        try {
            cloudinary.uploader().destroy(
                    publicId, Map.of("resource_type", "image")
            );
        } catch (Exception e) {
            log.warn("Xóa ảnh Cloudinary thất bại cho publicId [{}]: {}", publicId, e.getMessage());
            // Safe cleanup: do not throw exception, log warning instead
        }
    }
}
