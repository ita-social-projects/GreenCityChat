package greencity.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import greencity.constant.ErrorMessage;
import greencity.dto.ChatMessageDto;
import greencity.exception.exceptions.FileNotSavedException;
import greencity.service.AzureFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ChatMessageDto saveFile(MultipartFile multipartFile) {
        final String blob = UUID.randomUUID().toString();
        String blobName = blob + multipartFile.getOriginalFilename();
        BlobClient blobClient = containerClient().getBlobClient(blobName);
        try {
            blobClient.upload(multipartFile.getInputStream(), multipartFile.getSize());
        } catch (IOException e) {
            throw new FileNotSavedException(ErrorMessage.FILE_NOT_SAVED);
        }

        return new ChatMessageDto();
    }

    @Override
    public ChatMessageDto saveVoiceMessage(MultipartFile multipartFile) {
        final String blob = UUID.randomUUID().toString();
        String blobName = blob + WAV;
        BlobClient blobClient = containerClient().getBlobClient(blobName);
        try {
            blobClient.upload(multipartFile.getInputStream(), multipartFile.getSize());
        } catch (IOException e) {
            throw new FileNotSavedException(ErrorMessage.FILE_NOT_SAVED);
        }
        return new ChatMessageDto();
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
}
