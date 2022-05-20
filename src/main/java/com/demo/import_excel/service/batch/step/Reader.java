package com.demo.import_excel.service.batch.step;

import com.demo.import_excel.repository.entity.TempEntity;
import com.demo.import_excel.service.im.impl.ExampleEventUserModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.demo.import_excel.service.batch.step.Reader.Validator.validateExceedSize;
import static com.demo.import_excel.service.batch.step.Reader.Validator.validateNull;

@Log4j2
public class Reader implements ItemReader<TempEntity> {

    private List<TempEntity> tempEntities;

    public Reader(List<TempEntity> tempEntities) {
        this.tempEntities = Collections.synchronizedList(new LinkedList<>(tempEntities));

    }

    @Override
    public TempEntity read() throws Exception {
        if (tempEntities.isEmpty()) {
            return null;
        }
        return tempEntities.remove(0);
    }

    interface Validator extends Function<List<TempEntity>, Boolean> {
        static Validator validateNull() {
            return CollectionUtils::isEmpty;
        }

        static Validator validateExceedSize(int index) {
            return tempEntities -> tempEntities.size() >= index;
        }

        default Validator and(Validator other) {
            return tempEntities -> this.apply(tempEntities) && other.apply(tempEntities);
        }
    }
}
