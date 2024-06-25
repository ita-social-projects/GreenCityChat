package greencity.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import greencity.enums.FilesType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.PropertyResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AzureFileServiceImplTest {
    private @Value("azure.connection.string") String connectionString;
    private @Value("azure.container.name") String containerName;
    @InjectMocks
    private AzureFileServiceImpl azureFileService;
    @Mock
    private BlobClient blobClient;
    @Mock
    private PropertyResolver propertyResolver;
    @Mock
    private BlobServiceClientBuilder clientBuilder;
    @Mock
    private BlobServiceClient clientService;
    @Mock
    private BlobContainerClient containerClient;
    @Mock
    private MultipartFile multipartFile;

    @Test
    void saveFileTest() throws Exception {
        MultipartFile newMultipartFile = new MockMultipartFile("test.txt", "test".getBytes(StandardCharsets.UTF_8));
        InputStream inputStream = newMultipartFile.getInputStream();

        when(multipartFile.getOriginalFilename()).thenReturn(newMultipartFile.getOriginalFilename());
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(newMultipartFile.getSize());
        when(clientBuilder.connectionString(eq(connectionString))).thenReturn(clientBuilder);
        when(clientBuilder.buildClient()).thenReturn(clientService);
        when(clientService.getBlobContainerClient(eq(containerName))).thenReturn(containerClient);
        UUID uuid = UUID.randomUUID();
        when(containerClient.getBlobClient(eq(uuid + newMultipartFile.getOriginalFilename()))).thenReturn(blobClient);
        try (MockedStatic<UUID> mockUUID = mockStatic(UUID.class)) {
            mockUUID.when(UUID::randomUUID).thenReturn(uuid);
            azureFileService.saveFile(multipartFile, FilesType.FILE);

            verify(blobClient).upload(eq(inputStream), eq(newMultipartFile.getSize()));
        }
    }

    @Test
    void saveVoiceMessageTest() throws Exception {
        MultipartFile newMultipartFile = new MockMultipartFile("test.txt", "test".getBytes(StandardCharsets.UTF_8));
        InputStream inputStream = newMultipartFile.getInputStream();

        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(newMultipartFile.getSize());
        when(clientBuilder.connectionString(eq(connectionString))).thenReturn(clientBuilder);
        when(clientBuilder.buildClient()).thenReturn(clientService);
        when(clientService.getBlobContainerClient(eq(containerName))).thenReturn(containerClient);
        UUID uuid = UUID.randomUUID();
        when(containerClient.getBlobClient(eq(uuid + ".wav"))).thenReturn(blobClient);
        try (MockedStatic<UUID> mockUUID = mockStatic(UUID.class)) {
            mockUUID.when(UUID::randomUUID).thenReturn(uuid);
            azureFileService.saveVoiceMessage(multipartFile);

            verify(blobClient).upload(eq(inputStream), eq(newMultipartFile.getSize()));
        }
    }

    @Test
    void deleteTest() {
        String filename = "testFilename.txt";

        when(clientBuilder.connectionString(eq(connectionString))).thenReturn(clientBuilder);
        when(clientBuilder.buildClient()).thenReturn(clientService);
        when(clientService.getBlobContainerClient(eq(containerName))).thenReturn(containerClient);
        when(containerClient.getBlobClient(eq(filename))).thenReturn(blobClient);

        azureFileService.deleteFile(filename);

        verify(blobClient).delete();
    }

}
