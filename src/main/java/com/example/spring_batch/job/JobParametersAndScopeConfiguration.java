package com.example.spring_batch.job;

import com.example.spring_batch.step.SimpleJobTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class JobParametersAndScopeConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job scopeJob() {
        return new JobBuilder("scopeJob", jobRepository)
            .start(scopeStep1("20240901"))
            .next(scopeStep2())
            .next(scopeStep3())
            .build();
    }

    @Bean
    @JobScope
    public Step scopeStep1(@Value("#{jobParameters[notInputRequestDate]}") String requestDate) {
        // JJobParameters로 가능한 타입은 String, Long, Double, Date만 가능하다.
        return new StepBuilder("scopeStep1", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> This is scopeStep1");
                log.info(">>>>> requestDate = {}", requestDate);
                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

    @Bean
    public Step scopeStep2() {
        return new StepBuilder("scopeStep2", jobRepository)
            .tasklet(scopeStep2Tasklet(null), transactionManager)
            .build();
    }

    private final SimpleJobTasklet tasklet;

    @Bean
    public Step scopeStep3() {
        return new StepBuilder("scopeStep3", jobRepository)
            .tasklet(tasklet, transactionManager)
            .build();
    }

    @Bean
    @StepScope
    public Tasklet scopeStep2Tasklet(
        @Value("#{jobParameters[inputRequestDate]}") String requestDate) {
        return (contributuon, chunkContext) -> {
            log.info(">>>>> This is scopeStep2");
            log.info(">>>>> requestDate = {}", requestDate);
            return RepeatStatus.FINISHED;
        };
    }
}
