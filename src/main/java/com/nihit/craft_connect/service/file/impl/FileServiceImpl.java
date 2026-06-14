package com.nihit.craft_connect.service.file.impl;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.constants.MessageConstant;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final CustomMessageSource customMessageSource;
    @Value("${upload-dir}")
    private String attachmentPath;
    private final UserDetailConfig userDetailConfig;
    @Override
    public String uploadAttachment(MultipartFile multipartFile) {

        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        try {
            String originalFileName = multipartFile.getOriginalFilename();
            String fileExtension = FilenameUtils.getExtension(originalFileName);
            String fileName = UUID.randomUUID() + "." + fileExtension;
            File directory = new File(attachmentPath);

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
    @Override
    public byte[] downloadFile(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            throw new AppException(
                    customMessageSource.get(MessageConstant.FILE_NOT_FOUND)
            );
        }

        try {
            File file = new File(filePath);

            if (!file.exists() || !file.isFile()) {
                throw new AppException(
                        customMessageSource.get(MessageConstant.FILE_NOT_FOUND)
                );
            }

            return Files.readAllBytes(file.toPath());

        } catch (IOException e) {
            throw new AppException(
                    customMessageSource.get(MessageConstant.FILE_DOWNLOAD_FAILED)
            );
        }
    }


    @Override
    public List<String> loadAllFiles(String location) {

        File folder = new File(attachmentPath + File.separator + location);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new InvalidPathException(
                    customMessageSource.get(MessageConstant.ERROR_FILE_NOT_EXIST),
                    folder.getAbsolutePath()
            );
        }

        try (Stream<Path> pathStream = Files.list(folder.toPath())) {

            return pathStream
                    .filter(path -> !Files.isDirectory(path))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new AppException(
                    customMessageSource.get(MessageConstant.ERROR_FILE_NOT_EXIST)
            );
        }
    }

    @Override
    public Resource getFileByName(String fileName) {

        try {
            Path filePath = Paths.get(attachmentPath, fileName).normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new AppException("File not found");
            }

            return resource;

        } catch (Exception e) {
            throw new AppException("File download failed");
        }
    }
}
