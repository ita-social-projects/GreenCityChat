package greencity.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import greencity.constant.ErrorMessage;
import greencity.dto.ChatFileDto;
import greencity.enums.FilesType;
import greencity.exception.exceptions.FileNotSavedException;
import greencity.service.AzureFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
public class AzureFileServiceImpl implements AzureFileService {
    private final String connectionString;
    private final String containerName;
    private static final String WAV = ".wav";

    /**
     * constructor.
     */
    public AzureFileServiceImpl(@Autowired PropertyResolver propertyResolver) {
        this.connectionString = propertyResolver.getProperty("azure.connection.string");
        this.containerName = propertyResolver.getProperty("azure.container.name");
    }

    @Override
    public ChatFileDto saveFile(MultipartFile multipartFile, FilesType fileType) {
        ChatFileDto chatFileDto = uploadFile(multipartFile, multipartFile.getOriginalFilename());
        chatFileDto.setFileType(fileType);
        return chatFileDto;
    }

    @Override
    public ChatFileDto saveVoiceMessage(MultipartFile multipartFile) {
        ChatFileDto chatFileDto = uploadFile(multipartFile, WAV);
        chatFileDto.setFileType(FilesType.AUDIO);
        return chatFileDto;
    }

    @Override
    public void deleteFile(String fileName) {
        BlobClient blobClient = containerClient().getBlobClient(fileName);
        blobClient.delete();
    }

    private BlobContainerClient containerClient() {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString).buildClient();
        return serviceClient.getBlobContainerClient(containerName);
    }

    private ChatFileDto uploadFile(MultipartFile file, String fileName) {
        final String blob = UUID.randomUUID().toString();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            file.getInputStream().transferTo(baos);
        } catch (IOException e) {
            throw new FileNotSavedException(ErrorMessage.FILE_NOT_SAVED);
        }

        byte[] fileBytes = baos.toByteArray();

        BlobContainerClient containerClient = containerClient();
        String blobName = blob + fileName;
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            blobClient.upload(inputStream, fileBytes.length, true);
        } catch (IOException e) {
            throw new FileNotSavedException(ErrorMessage.FILE_NOT_SAVED);
        }

        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentType(file.getContentType());
        blobClient.setHttpHeaders(headers);

        return ChatFileDto.builder()
            .fileName(blobClient.getBlobName())
            .fileUrl(blobClient.getBlobUrl())
            .build();
    }
}
