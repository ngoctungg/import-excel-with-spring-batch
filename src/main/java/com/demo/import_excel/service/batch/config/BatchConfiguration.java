package com.demo.import_excel.service.batch.config;

import com.demo.import_excel.repository.entity.TempEntity;
import com.demo.import_excel.repository.jpa.TempEntityRepository;
import com.demo.import_excel.service.batch.listener.JobCompletionListener;
import com.demo.import_excel.service.batch.step.Processor;
import com.demo.import_excel.service.batch.step.Reader;
import com.demo.import_excel.service.batch.step.Writer;
import com.demo.import_excel.service.im.impl.ExampleEventUserModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
@Log4j2
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;


    private final StepBuilderFactory stepBuilderFactory;

    public BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch_");
    }

    @Bean(name = "importExcelJob")
    public Job processJob(Step orderStep) {
        return jobBuilderFactory.get("processJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .start(orderStep)
                .build();
    }

    @Bean
    public Step loadStep(@Qualifier("readerFromExcel") ItemReader<TempEntity> readerFromExcel,
                         @Qualifier("writerToDatabase") ItemWriter<TempEntity> writerToDatabase,
                         @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        return stepBuilderFactory.get("orderStep")

                .<TempEntity, TempEntity>chunk(500)
                .reader(readerFromExcel)
                .processor(new Processor())
                .writer(writerToDatabase)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionListener();
    }

    @Bean
    @StepScope
    public ItemReader<TempEntity> readerFromExcel(@Value("#{jobParameters['fileName']}") String fileName) {
        ExampleEventUserModel exampleEventUserModel = new ExampleEventUserModel();
        List<TempEntity> tempEntities = new ArrayList<>();
        try {
            tempEntities = exampleEventUserModel.processOneSheet(fileName);
        }catch (Exception e){
            log.error(e);
        }
//        ListItemReader<TempEntity> tempEntityListItemReader = new ListItemReader<>(tempEntities);
        return new Reader(tempEntities);
    }

    @Bean
    @StepScope
    public ItemWriter<TempEntity> writerToDatabase(TempEntityRepository tempEntityRepository) {
        return new Writer(tempEntityRepository);
    }


}
