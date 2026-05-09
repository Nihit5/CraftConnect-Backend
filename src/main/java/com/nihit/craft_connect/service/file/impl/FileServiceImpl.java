package com.nihit.craft_connect.service.file.impl;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.constants.MessageConstant;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final CustomMessageSource customMessageSource;
    @Value("${upload-dir}")
    private String attachmentPath;

    @Override
    public String uploadAttachment(MultipartFile multipartFile, String location) {

        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        try {
            String originalFileName = multipartFile.getOriginalFilename();
            String fileExtension = FilenameUtils.getExtension(originalFileName);
            String fileName = UUID.randomUUID() + "." + fileExtension;
            File directory = new File(attachmentPath + File.separator + location);

            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, fileName);
            multipartFile.transferTo(file);
            return file.getAbsolutePath();
        } catch (IOException e) {
            throw new AppException(customMessageSource.get(MessageConstant.CREATE_FILE_FAILED));
        }
    }
}
