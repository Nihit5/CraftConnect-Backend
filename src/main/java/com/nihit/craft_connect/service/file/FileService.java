package com.nihit.craft_connect.service.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FileService {
    String uploadAttachment(MultipartFile multipartFile, String location);
}
