package com.wahid.springbootimportcsvfileapp.config;

import com.wahid.springbootimportcsvfileapp.entity.Contact;
import com.wahid.springbootimportcsvfileapp.listener.StepSkipListener;
import com.wahid.springbootimportcsvfileapp.partition.RowRangePartitioner;
import jdk.jfr.Name;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    private CustomerItemWriter customerItemWriter;


    @Bean
    @StepScope
    public FlatFileItemReader<Contact> itemReader(@Value("#{jobParameters[fullPathFileName]}") String pathToFIle) {
        FlatFileItemReader<Contact> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new FileSystemResource(new File(pathToFIle)));
        flatFileItemReader.setName("CSV-Reader");
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(lineMapper());
        return flatFileItemReader;
    }

    private LineMapper<Contact> lineMapper() {
        DefaultLineMapper<Contact> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);

        lineTokenizer.setNames("firstName", "lastName", "gender", "email", "phoneNumber", "dateOfBirth", "jobTitle");
        lineTokenizer.setIncludedFields(2, 3, 4, 5, 6, 7, 8);


        BeanWrapperFieldSetMapper<Contact> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Contact.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;

    }

    @Bean
    public ContactProcessor processor() {
        return new ContactProcessor();
    }

//    @Bean
//    public RepositoryItemWriter<Contact> writer() {
//        RepositoryItemWriter<Contact> writer = new RepositoryItemWriter<>();
//        writer.setRepository(contactRepository);
//        writer.setMethodName("save");
//        return writer;
//    }

//    @Bean
//    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<Contact> itemReader) {
//        return new StepBuilder("csv-step",jobRepository).
//                <Contact, Contact>chunk(1000,transactionManager)
//                .reader(itemReader)
//                .processor(processor())
//                .writer(customerItemWriter)
//                .listener(skipListener())
//                .taskExecutor(taskExecutor())
//                .build();
//    }

    @Bean
    public RowRangePartitioner partitioner() {
        return new RowRangePartitioner();
    }

    @Bean
    public PartitionHandler partitionHandler(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<Contact> itemReader) {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(4);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slaveStep(jobRepository, transactionManager, itemReader));
        return taskExecutorPartitionHandler;
    }

    @Bean
    @Name("slaveStep")
    public Step slaveStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<Contact> itemReader) {
        return new StepBuilder("slaveStep", jobRepository).<Contact, Contact>chunk(25000, transactionManager)
                .reader(itemReader)
                .processor(processor())
                .writer(customerItemWriter)
                .listener(skipListener())
                .build();
    }

    @Bean
    public Step masterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<Contact> itemReader) {
        return new StepBuilder("masterSTep", jobRepository).
                partitioner("slaveStep", partitioner())
                .partitionHandler(partitionHandler(jobRepository, transactionManager, itemReader))
                .gridSize(4)
                .build();
    }

    @Bean
    public Job runJob(JobRepository jobRepository,PlatformTransactionManager transactionManager, FlatFileItemReader<Contact> itemReader) {
        return new JobBuilder("importContacts",jobRepository)
                .flow(masterStep(jobRepository,transactionManager, itemReader)).end().build();
    }

    @Bean
    public SkipPolicy skipPolicy() {
        return new ExceptionSkipPolicy();
    }

    @Bean
    public SkipListener skipListener() {
        return new StepSkipListener();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(5);
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setQueueCapacity(5);
        return taskExecutor;
    }

}
