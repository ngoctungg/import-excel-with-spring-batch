package com.demo.import_excel.service.batch.step;

import com.demo.import_excel.repository.entity.TempEntity;
import org.springframework.batch.item.ItemProcessor;

public class Processor implements ItemProcessor<TempEntity, TempEntity> {
    @Override
    public TempEntity process(TempEntity item) throws Exception {
        if(item.getId() == null) throw new Exception("asd");
        return item;
    }
}
