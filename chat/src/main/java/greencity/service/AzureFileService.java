package greencity.service;

import greencity.dto.ChatMessageDto;
import org.springframework.web.multipart.MultipartFile;

public interface AzureFileService {
    /**
     * Method save file in azure storage.
     * 
     * @param multipartFile of{@link MultipartFile} return file url
     */
    ChatMessageDto saveFile(MultipartFile multipartFile);

    /**
     * Method save voice message file in azure storage.
     * 
     * @param multipartFile of{@link MultipartFile} return file url
     */
    ChatMessageDto saveVoiceMessage(MultipartFile multipartFile);

    /**
     * Method delete file from azure storage.
     * 
     * @param fileName of{@link String}
     */
    void deleteFile(String fileName);
}
