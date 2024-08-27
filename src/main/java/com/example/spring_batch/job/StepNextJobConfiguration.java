package com.example.spring_batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class StepNextJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job stepNextJob() {
        return new JobBuilder("stepNextJob", jobRepository)
            // step1 -> step2 -> step3 순차 실행
            .start(nextStep1())
            .next(nextStep2())
            .next(nextStep3())
            .build();
    }

    @Bean
    public Step nextStep1() {
        return new StepBuilder("step1", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> This is Step1");
                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

    @Bean
    public Step nextStep2() {
        return new StepBuilder("step2", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> This is step2");
                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

    @Bean
    public Step nextStep3() {
        return new StepBuilder("step3", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> This is step3");
                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

}
