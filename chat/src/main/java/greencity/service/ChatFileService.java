package greencity.service;

import java.io.IOException;

public interface ChatFileService {
    /**
     * {@inheritDoc}
     */
    String save(String encodedString) throws IOException;

    /**
     * {@inheritDoc}
     */
    String saveFileAndGetFileName(byte[] fileName, String fileType) throws IOException;

    /**
     * {@inheritDoc}
     */
    String getUniqueName(String originalName);

    /**
     * {@inheritDoc}
     */
    byte[] getByteArrayFromFile(String fileName) throws IOException;

    /**
     * {@inheritDoc}
     */
    String getFilteredFileType(String mediaType);
}
