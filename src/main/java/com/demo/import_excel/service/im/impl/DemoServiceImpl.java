package com.demo.import_excel.service.im.impl;

import com.demo.import_excel.repository.entity.TempEntity;
import com.demo.import_excel.repository.jpa.TempEntityRepository;
import com.demo.import_excel.service.im.DemoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DemoServiceImpl implements DemoService {


    @Value("${file.upload.path}")
    private String filePath;

    private TempEntityRepository tempEntityRepository;

    public DemoServiceImpl(TempEntityRepository tempEntityRepository) {
        this.tempEntityRepository = tempEntityRepository;
    }

    @Override
    public String storeFileToDisk(MultipartFile file) throws IOException {
        Path path = Paths.get(filePath+"/"+file.getOriginalFilename());
        Files.write(path, file.getBytes(), java.nio.file.StandardOpenOption.CREATE);
        return path.toAbsolutePath().toString();
    }

    @Override
    public void saveFileToDB(String fileName) throws Exception {
        ExampleEventUserModel exampleEventUserModel = new ExampleEventUserModel();
        List<TempEntity> tempEntities = exampleEventUserModel.processOneSheet(fileName);
        tempEntityRepository.saveAll(tempEntities);
    }
}
