package vn.iotstar.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import vn.iotstar.service.impl.CloudinaryServiceImpl;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() {
        cloudinaryService = new CloudinaryServiceImpl(cloudinary);
    }

    @Test
    void upload_whenFileIsEmpty_throwsIllegalArgumentException() {
        MockMultipartFile emptyFile = new MockMultipartFile("image", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> cloudinaryService.upload(emptyFile));
    }

    @Test
    void upload_whenFileNotImage_throwsIllegalArgumentException() {
        MockMultipartFile textFile = new MockMultipartFile(
                "image", "note.txt", "text/plain", "hello".getBytes()
        );
        assertThrows(IllegalArgumentException.class, () -> cloudinaryService.upload(textFile));
    }

    @Test
    void upload_whenCloudinaryNotConfigured_returnsDemoSampleResult() {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "photo.png", "image/png", new byte[]{1, 2, 3}
        );
        // cloudinary.config is null or blank
        CloudinaryUploadResult result = cloudinaryService.upload(imageFile);
        assertNotNull(result);
        assertEquals("sample", result.publicId());
        assertTrue(result.url().contains("sample.jpg"));
    }

    @Test
    void delete_whenPublicIdIsSample_doesNotCallDestroy() {
        cloudinaryService.delete("sample");
        verifyNoInteractions(cloudinary);
    }

    @Test
    void delete_whenPublicIdIsNullorBlank_doesNotCallDestroy() {
        cloudinaryService.delete(null);
        cloudinaryService.delete("   ");
        verifyNoInteractions(cloudinary);
    }

    @Test
    void delete_whenCloudinaryFails_doesNotThrowException() throws IOException {
        Cloudinary customCloudinary = spy(new Cloudinary(Map.of(
                "cloud_name", "test_cloud",
                "api_key", "test_key",
                "api_secret", "test_secret"
        )));
        CloudinaryServiceImpl service = new CloudinaryServiceImpl(customCloudinary);
        when(customCloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq("pub_fail"), any())).thenThrow(new RuntimeException("Cloudinary API network timeout"));

        assertDoesNotThrow(() -> service.delete("pub_fail"));
    }
}
