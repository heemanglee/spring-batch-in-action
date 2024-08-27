package com.example.spring_batch.job;

import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeciderJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job deciderJob() { // JobExecutionDecider를 사용하여 Job의 흐름을 제어
        return new JobBuilder("deciderJob", jobRepository)
            .start(startStep()) // startStep 먼저 실행
            .next(decider()) // decider를 실행한다.
            .from(decider()) // decider 실행 결과에 따른 분기점 설정
            .on("ODD") // decider의 실행 결과가 "ODD"라면
            .to(oddStep()) // oddStep()을 실행한다.
            .from(decider()) // decider 실행 결과에 따른 분기점 설정
            .on("EVEN") // decider의 실행 결과가 "EVEN"라면
            .to(evenStep()) // evenStep()을 실행한다.
            .end() // Job 종료
            .build();
    }

    @Bean
    public Step startStep() {
        return new StepBuilder("startStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> Start!");
                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

    @Bean
    public Step evenStep() {
        return new StepBuilder("evenStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> 짝수입니다.");
                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

    @Bean
    public Step oddStep() {
        return new StepBuilder("oddStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> 홀수입니다.");
                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

    @Bean
    public JobExecutionDecider decider() {
        return new OddDecider();
    }

    static class OddDecider implements JobExecutionDecider {

        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            Random random = new Random();
            int randomNumber = random.nextInt(50) + 1;
            log.info("랜덤 숫자 : {}", randomNumber);

            if (randomNumber % 2 == 0) {
                return new FlowExecutionStatus("EVEN"); // ExitStauts가 아닌 FlowExecutionStatus를 반환한다.
            }
            return new FlowExecutionStatus("ODD");
        }
    }
}
