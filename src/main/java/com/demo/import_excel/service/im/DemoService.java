package com.demo.import_excel.service.im;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DemoService {

    String storeFileToDisk(MultipartFile file) throws IOException;

    void saveFileToDB(String fileName) throws Exception;

}
