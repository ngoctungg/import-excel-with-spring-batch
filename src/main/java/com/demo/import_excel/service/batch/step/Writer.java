package com.demo.import_excel.service.batch.step;

import com.demo.import_excel.repository.entity.TempEntity;
import com.demo.import_excel.repository.jpa.TempEntityRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.ItemWriter;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Log4j2
public class Writer implements ItemWriter<TempEntity> {

    private final TempEntityRepository tempEntityRepository;

    public Writer(TempEntityRepository tempEntityRepository) {
        this.tempEntityRepository = tempEntityRepository;
    }

    @Override
    public void write(List<? extends TempEntity> items) throws Exception {
        if(CollectionUtils.isEmpty(items)){
            return;
        }
        log.info("ITEM SIZE:"+items.size());
        tempEntityRepository.saveAll(items);
    }
}
