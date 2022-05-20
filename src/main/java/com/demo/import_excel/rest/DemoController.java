package com.demo.import_excel.rest;

import com.demo.import_excel.service.im.DemoService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/im")
public class DemoController {

    private DemoService demoService;

    private final JobLauncher jobLauncher;


    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    private final JobRegistry jobRegistry;

    public DemoController(DemoService demoService,
                          JobLauncher jobLauncher,
                          JobOperator jobOperator,
                          JobExplorer jobExplorer, JobRegistry jobRegistry) {
        this.demoService = demoService;
        this.jobLauncher = jobLauncher;
        this.jobOperator = jobOperator;
        this.jobExplorer = jobExplorer;
        this.jobRegistry = jobRegistry;
    }

    @PostMapping("/handle")
    public ResponseEntity<?> imData(@RequestParam("file") MultipartFile file) throws Exception {
        String fileToDisk = demoService.storeFileToDisk(file);
        demoService.saveFileToDB(fileToDisk);
        return ResponseEntity.ok(fileToDisk);
    }

    @PostMapping("/batch")
    public ResponseEntity<?> imDataBatch(@RequestParam("file") MultipartFile file) throws Exception {
        String fileToDisk = demoService.storeFileToDisk(file);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileName", fileToDisk)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        Job job = jobRegistry.getJob("processJob");

        JobExecution run = jobLauncher.run(job, jobParameters);
        return ResponseEntity.ok(run.getStatus());
    }

    @GetMapping("/reset/{id}")
    public ResponseEntity<?> resetBatch(@PathVariable("id") Long id) throws JobInstanceAlreadyCompleteException, NoSuchJobException, NoSuchJobExecutionException, JobParametersInvalidException, JobRestartException {
        final Long restartId = jobOperator.restart(id);
        JobExecution jobExecution = jobExplorer.getJobExecution(restartId);
        if (jobExecution == null){
            return ResponseEntity.badRequest().build();

        }
        return ResponseEntity.ok(jobExecution.getStatus());
    }

}
