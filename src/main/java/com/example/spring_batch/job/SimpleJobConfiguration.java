package com.example.spring_batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleJobConfiguration {

    private final JobRepository jobRepository; // JobRepository를 사용하여 Job의 실행 내역 관리
    private final PlatformTransactionManager transactionManager;

    // Job이란 배치 작업 전체를 나타내는 하나의 단위이다. -> 배치 작업 전체를 정의하는 객체로, 여러 Step을 포함한다.
    // 여러 개의 Step으로 구성되며, 각각의 Step은 순차적으로 또는 순차적으로 실행된다.
    @Bean
    public Job job() {
        return new JobBuilder("simpleJob1", jobRepository)
//            .start(simpleStep1())
            .start(simpleStep3())
            .next(simpleStep2(null))
            .build();
    }

    // Step이란 Job을 구성하는 하나의 작업 단위이다. -> 배치 작업 내에서 개별 작업 단위를 의미한다.
    // Step은 Tasklet을 실행한다.
//    @Bean
    public Step simpleStep1() {
        return new StepBuilder("simplestep1", jobRepository)
            // Tasklet은 Step에서 실행되는 작업 단위이다.
            // 이 작업이 끝나면 Step이 끝나고, Job의 다음 단계로 넘어간다.
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> This is step1");
                return RepeatStatus.FINISHED;
            }), transactionManager).build();
    }

    @Bean
    @JobScope // JobParameters를 사용하기 위해서 @JobScope를 사용한다.
    public Step simpleStep2(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return new StepBuilder("simplestep2", jobRepository)
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> This is step2");
                log.info(">>>>> requestDate = {}", requestDate);
                return RepeatStatus.FINISHED;
            }), transactionManager).build();
    }

    @Bean
    public Step simpleStep3() {
        return new StepBuilder("simplestep3", jobRepository)
            .tasklet((contribution, chunkContext) -> {
//                throw new IllegalArgumentException("step3에서 예외 발생");

                log.info(">>>>> 예외가 아닌, 정상적으로 배치 작업을 수행한다.");
                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

}
