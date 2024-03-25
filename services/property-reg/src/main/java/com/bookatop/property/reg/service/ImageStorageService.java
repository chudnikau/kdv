package com.bookatop.property.reg.service;

import com.bookatop.property.reg.exception.ImageStorageServiceException;
import com.bookatop.storage.Storage;
import com.bookatop.storage.exceptios.ImageStorageException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Service
public class ImageStorageService {

    public static final String IMAGE_IS_NOT_REMOVED = "Image is not removed";

    private final Logger logger = LoggerFactory.getLogger(ImageStorageService.class);

    public static final int ZERO_LEADERS_COUNT = 10;

    public static final String DEF_IMAGE_EXTENSION = ".jpg";

    public static final String USER_FRAGMENT_DELIMITER = "_";

    @Value("${images.upload.limit.size}")
    private long limitUploadFileSize;

    public static final String NOT_ALLOWED_FILE_SIZE = "Not allowed file size";

    private final Storage storage;

    public ImageStorageService(Storage storage) {
        this.storage = storage;
    }

    private String hashMD5(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        MessageDigest md5Instance = MessageDigest.getInstance("MD5");

        int nread;

        byte[] buffer = new byte[1024];
        while ((nread = inputStream.read(buffer)) != -1) {
            md5Instance.update(buffer, 0, nread);
        }

        return DatatypeConverter.printHexBinary(md5Instance.digest()).toLowerCase();
    }

    private String filePath(String hash) {
        return hash.substring(0, 2) + File.separator + hash.substring(2, 4);
    }

    private String fileName(long userId, String hash) {
        String f = hash.substring(4);
        String s = StringUtils.leftPad(String.valueOf(userId), ZERO_LEADERS_COUNT, "0");
        return f + USER_FRAGMENT_DELIMITER + s + DEF_IMAGE_EXTENSION;
    }

    public String storeImage(long userId, InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();

            if (bytes.length == 0 || bytes.length > limitUploadFileSize)
                throw new ImageStorageServiceException(NOT_ALLOWED_FILE_SIZE);

            String hash = hashMD5(new ByteArrayInputStream(bytes));

            /* Making file path from hash result
               md5: 0471d6dfa018defa5e0293cf77e49e61
               path: /04/71
             */
            String filePath = filePath(hash);

            /* Making file name
               md5(file): 0471d6dfa018defa5e0293cf77e49e61
               filename (path is discarded): d6dfa018defa5e0293cf77e49e61_0000000001.jpg
               Fragment 0000000001 consists of zero-leaders and userId
             */
            String fileName = fileName(userId, hash);

            logger.debug("Store the image {} {}", filePath, fileName);

            return storage.storeFile(filePath, fileName, new ByteArrayInputStream(bytes));

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new ImageStorageServiceException(e.getMessage());
        }
    }

    private boolean isFileBelongUserId(long userId, String fileUrn) {
        try {
            File fFile = new File(fileUrn);
            String fName = fFile.getName();
            String fUserPart = fName.substring(fName.indexOf(USER_FRAGMENT_DELIMITER) + 1);
            String constructedUserPart = StringUtils.leftPad(String.valueOf(userId), ZERO_LEADERS_COUNT, "0") + DEF_IMAGE_EXTENSION;
            return fUserPart.equals(constructedUserPart);
        } catch (Exception e) {
            return false;
        }
    }

    public void removeImage(long userId, String fileUrn) {
        boolean fileBelongUserId = isFileBelongUserId(userId, fileUrn);

        if (Objects.nonNull(fileUrn) && fileBelongUserId) {
            try {
                storage.removeFile(fileUrn);

                logger.debug("The image {} has been removed", fileUrn);

                return;
            } catch (ImageStorageException | IOException e) {
                throw new ImageStorageServiceException(IMAGE_IS_NOT_REMOVED);
            }
        }
        throw new ImageStorageServiceException(IMAGE_IS_NOT_REMOVED);
    }
}
