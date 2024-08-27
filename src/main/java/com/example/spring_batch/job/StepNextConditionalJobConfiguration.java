package com.example.spring_batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
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
@Configuration
@RequiredArgsConstructor
public class StepNextConditionalJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job stepNextConditionalJob() {
        return new JobBuilder("stepNextConditionalJob", jobRepository)
            .start(conditionalJobStep1()) // Job은 step1()을 가장 먼저 실행한다.
            .on("FAILED") // step1()의 상태가 "FAILED"가 된다면
            .to(conditionalJobStep3()) // step3()를 실행한다.
            .from(conditionalJobStep1()) // 조건부 정의부, 이전에 실행된 step으로부터 새로운 조건과 흐름을 설정한다.
            .on("*") // "FAILED"를 제외한 모든 경우
            .to(conditionalJobStep2()) // step2()를 실행한다.
            .next(conditionalJobStep3()) // step2()가 끝나면 step3()를 실행한다.
            .end() // FlowBuilder 종료 = Job의 실행 흐름 종료
            .build();
    }

    @Bean
    public Step conditionalJobStep1() {
        return new StepBuilder("step1", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> This is stepNextConditionalJob Step1");

                /**
                 * ExitStatus != BatchStatus
                 * ExitStatus : Step이 "완료된 후" Step의 상태를 의미
                 * BatchStatus : Step의 결과를 Spring에 기록할 때, 사용하는 Enum
                 */
//                contribution.setExitStatus(ExitStatus.FAILED); // Step의 상태를 "FAILED"로 지정한다. 이에 맞춰서 FlowBuilder가 분기처리 된다.

                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

    @Bean
    public Step conditionalJobStep2() {
        return new StepBuilder("step2", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> This is stepNextConditionalJob Step2");
                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

    @Bean
    public Step conditionalJobStep3() {
        return new StepBuilder("step3", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> This is stepNextConditionalJob Step3");
                return RepeatStatus.FINISHED;
            }, transactionManager).build();
    }

}
