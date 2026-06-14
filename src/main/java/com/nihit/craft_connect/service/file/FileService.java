package com.nihit.craft_connect.service.file;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface FileService {
    String uploadAttachment(MultipartFile multipartFile);
    byte[] downloadFile(String filePath);
    List<String> loadAllFiles(String location);
    Resource getFileByName(String fileName);
}

