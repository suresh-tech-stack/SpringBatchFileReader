package com.conduent.plcl.batch.execute;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.conduent.plcl.batch.common.BatchItemProcessor;
import com.conduent.plcl.batch.common.CustomLineMapper;
import com.conduent.plcl.batch.common.FileArchivingTasklet;
import com.conduent.plcl.batch.common.PLCLBatchException;
import com.conduent.plcl.batch.dao.BatchProcessDao;
import com.conduent.plcl.batch.model.BatchDataSetMapper;
import com.conduent.plcl.batch.model.CustomJobData;
import com.conduent.plcl.batch.model.PlateCorrection;
import com.conduent.plcl.batch.model.PLCLParams;
import com.conduent.plcl.batch.util.BatchUtil;
import com.conduent.vector.pmms.genericservice.vo.AlertRequest;

/**
 * This Class have the all Configuration related to Spring Batch and it's have
 * the all the beans required for batch job .
 * 
 * @author 52058018
 *
 */
@Configuration
@EnableBatchProcessing
@PropertySource(value = "${conf.properties}") // Reading the properties from the external properties file .
@EnableTransactionManagement
public class PlateCorrectionExecute {
	private final static Logger LOGGER = Logger.getLogger(PlateCorrectionExecute.class);
	@Autowired
	Environment env;

	@Autowired
	DataSource dataSource;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	InterceptingJobExecution interceptingJob;

	@Autowired
	BatchProcessDao batchProcessDao;

	@Autowired
	CustomJobData jobData;

	private String fileId;

	private static final String OVERRIDDEN_BY_EXPRESSION = null;

	/**
	 * Search precedence of local properties is based on the value of the property,
	 * It is used by default to support the element in working against the
	 * spring-context-3.1 XSD.
	 * 
	 * @return PropertySourcesPlaceholderConfigurer
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * This Method is call's when the batch job was started and then creates the
	 * bean and start the Job , from this method it will call the step execution of
	 * the Job.
	 * 
	 * @return
	 * @throws PLCLBatchException
	 */
	@Bean
	public Job readFlatFileJob() throws PLCLBatchException {
		LOGGER.debug("Entered into readFlatFileJob method {} ");

		Job job = jobBuilderFactory.get(BatchUtil.JOB_NAME).incrementer(new RunIdIncrementer()).start(step())
				.listener(interceptingJob).next(step2()).build();
		return job;
	}

	/**
	 * Step is a sequential phase of a batch job and contains all of the information
	 * necessary to define and control the actual batch processing. From here it
	 * will call reader and processor methods
	 * 
	 * @return
	 * @throws PLCLBatchException
	 */
	@Bean
	public Step step() throws PLCLBatchException {
		LOGGER.debug("Entered into step process {} ");

		Step step = stepBuilderFactory.get("step")
				.<PlateCorrection, PlateCorrection>chunk(Integer.parseInt(env.getProperty("la.chunklines")))
				.reader(multiResourceItemReader(OVERRIDDEN_BY_EXPRESSION)).processor(processor())
				.writer(compositeItemWriter()).build();
		return step;
	}

	/**
	 * Creating the custom ItemProcessor Bean
	 * 
	 * @return ItemProcessor
	 */
	@Bean
	public ItemProcessor<PlateCorrection, PlateCorrection> processor() {
		LOGGER.debug("Entered into processor method {} ");
		return new BatchItemProcessor();
	}

	/**
	 * Creating the bean for BatchProcessDao Class
	 * 
	 * @return BatchProcessDao
	 */
	@Bean
	public BatchProcessDao batchProcessDao() {

		return new BatchProcessDao();
	}

	/**
	 * Creating the Actual reader with appropriate LineMapper
	 * 
	 * @return FlatFileItemReader
	 */
	@Bean
	public FlatFileItemReader<PlateCorrection> reader() {
		LOGGER.debug("Entered into reader method {} ");
		// Create reader instance
		FlatFileItemReader<PlateCorrection> reader = new FlatFileItemReader<PlateCorrection>();
		reader.setLineMapper(lineMapper());
		reader.setLinesToSkip(Integer.parseInt(env.getProperty("la.skiplines1")));
		return reader;
	}

	/**
	 * Creating the MultiResourceItemReader to read multiple files
	 * 
	 * @param pathToFile
	 * @return MultiResourceItemReader
	 * @throws PLCLBatchException
	 */
	@Bean
	@StepScope
	public MultiResourceItemReader<PlateCorrection> multiResourceItemReader(
			@Value("#{jobParameters[fileName]}") String pathToFile) throws PLCLBatchException {

		MultiResourceItemReader<PlateCorrection> resourceItemReader = new MultiResourceItemReader<PlateCorrection>();
		this.fileId = jobData.getFileIdVlaue();
		batchProcessDao.setResources(pathToFile);
		resourceItemReader.setResources(batchProcessDao.getMultiResources());
		resourceItemReader.setDelegate(reader());
		return resourceItemReader;
	}

	/**
	 * While reading the file this method will set appropriate record data to the
	 * model object
	 * 
	 * @return LineMapper
	 */
	@Bean
	public LineMapper<PlateCorrection> lineMapper() {
		LOGGER.debug("Entered into lineMapper Method {} ");
		CustomLineMapper<PlateCorrection> lineMapper = new CustomLineMapper<PlateCorrection>(
				Integer.parseInt(env.getProperty("la.skipLines")));
		BeanWrapperFieldSetMapper<PlateCorrection> fieldSetMapper = new BeanWrapperFieldSetMapper<PlateCorrection>();
		fieldSetMapper.setTargetType(PlateCorrection.class);
		lineMapper.setLineTokenizer(fileTokenizer());
		lineMapper.setFieldSetMapper(batchFieldSetMapper());
		return lineMapper;
	}

	/**
	 * This method have the itemWriter information for data insertion into the
	 * database . Setting the dataSource and SQL query to insert the data .
	 * 
	 * @return JdbcBatchItemWriter
	 */
	@Bean
	public JdbcBatchItemWriter<PlateCorrection> writer() {
		LOGGER.debug("In BatchConfig {} writer method {} ");
		JdbcBatchItemWriter<PlateCorrection> itemWriter = new JdbcBatchItemWriter<PlateCorrection>();
		itemWriter.setDataSource(dataSource);
		itemWriter.setSql(BatchUtil.getTransDetailsQuery());
		itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<PlateCorrection>());
		return itemWriter;
	}

	/**
	 * LineTokenizer used for reading the CSV formated data inside the file and map
	 * to model object .
	 * 
	 * @return LineTokenizer
	 */
	@Bean
	public LineTokenizer fileTokenizer() {
		LOGGER.debug("Entered into fileTokenizer method {} ");
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setNames(new String[] { "OrigLicState", "OrigLicNumber", "OrigLicType", "AdjLicState",
				"AdjLicNumber", "AdjLicType" });
		lineTokenizer.setIncludedFields(new int[] { 0, 1, 2, 3, 4, 5 });
		return lineTokenizer;
	}

	/**
	 * Creates the custom FieldSetMapper to map the file data with Model object
	 * 
	 * @return FieldSetMapper
	 */
	@Bean
	public FieldSetMapper<PlateCorrection> batchFieldSetMapper() {
		LOGGER.debug("Entered into batchFieldSetMapper method {} to set the appropriate columns for given ranges ");
		return new BatchDataSetMapper();
	}

	/**
	 * Establishes the policy whether to call the open, close, or update methods for
	 * the item writer delegates associated with the CompositeItemWriter .The list
	 * of item writers to use as delegates. Items are written to each of the
	 * delegates.
	 * 
	 * @return
	 */
	@Bean
	@StepScope
	public CompositeItemWriter<PlateCorrection> compositeItemWriter() {
		LOGGER.debug("Entered into compositeItemWriter method to delegates itemwriter to writer method {} ");
		CompositeItemWriter<PlateCorrection> compositeWriter = new CompositeItemWriter<PlateCorrection>();
		compositeWriter.setDelegates(Arrays.asList(writer()));
		return compositeWriter;
	}

	/**
	 * This method is for resuming the job for file reading ,If the job is broken
	 * and when we restarted it will start the process where it's stopped .
	 * 
	 * @param jobOperator
	 * @param jobRepository
	 * @param jobExplorer
	 * @return
	 */
	@Bean
	public ApplicationListener<ContextRefreshedEvent> resumeJobsListener(final JobOperator jobOperator,
			final JobRepository jobRepository, final JobExplorer jobExplorer) {
		LOGGER.debug("Entered into JobListener method {} ");
		// restart jobs that failed due to
		ApplicationListener<ContextRefreshedEvent> applicationListener = new ApplicationListener<ContextRefreshedEvent>() {
			@Override
			public void onApplicationEvent(ContextRefreshedEvent event) {
				Date jvmStartTime = new Date(ManagementFactory.getRuntimeMXBean().getStartTime());
				// get latest job instance
				for (JobInstance instance : jobExplorer.getJobInstances(BatchUtil.JOB_NAME, 0, 1)) {
					// for each of the executions
					for (JobExecution execution : jobExplorer.getJobExecutions(instance)) {
						if (execution.getStatus().equals(BatchStatus.STARTED)
								&& execution.getCreateTime().before(jvmStartTime)) {
							// this job is broken and must be restarted
							execution.setEndTime(new Date());
							execution.setStatus(BatchStatus.FAILED);
							execution.setExitStatus(ExitStatus.FAILED);
							PLCLParams xferFile = new PLCLParams();
							for (StepExecution se : execution.getStepExecutions()) {
								xferFile.setFileName(se.getJobParameters().getString("fileName"));
								xferFile.setFileStatus(BatchUtil.PICKEDUP_CONSTANT);
								xferFile.setFileId(jobData.getFileIdVlaue());
								if (se.getStatus().equals(BatchStatus.STARTED)) {
									se.setEndTime(new Date());
									se.setStatus(BatchStatus.FAILED);
									se.setExitStatus(ExitStatus.FAILED);
									jobRepository.update(se);
								}
								LOGGER.debug("When we restarting ,The File Id is : " + xferFile.getFileId());

							}
							batchProcessDao.updateXFERFILECNTLBYFileId(xferFile, BatchUtil.EXECUTE_CONSTANT);
							jobRepository.update(execution);
							try {
								jobOperator.restart(execution.getId());
							} catch (JobExecutionException e) {
								AlertRequest alertRequest = batchProcessDao.populateAlertMessage(
										BatchUtil.BATCH_PROCESS_NAME, "JobRestart Error Message : " + e.toString(),
										execution.getJobParameters().getString("fileName"));
								batchProcessDao.getPmmsAlert().persistAlerts(alertRequest);
								LOGGER.error("Exception while restarting the Job Execution " + e.getMessage());
								try {
									throw new PLCLBatchException("Exception while restarting the Job Execution " + e);
								} catch (PLCLBatchException e1) {
									LOGGER.error("Exception while restarting the Job Execution :" + e1.getMessage());
								}

							}
						}
					}
				}
			}
		};

		return applicationListener;
	}

	/**
	 * To create the jobRegistryBeanPostProcessor
	 * 
	 * @param jobRegistry
	 * @return
	 */
	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
		LOGGER.debug("Entered into JobRegistry Processor method {} ");
		JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
		jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
		return jobRegistryBeanPostProcessor;
	}

	/**
	 * This is the second execution Step to move the processed file into processed
	 * directory
	 * 
	 * @return
	 */
	@Bean
	public Step step2() {
		Step step = stepBuilderFactory.get("step2").tasklet(fileDelegation()).build();
		return step;
	}

	/**
	 * This is the Step Execution class for moving the successful processed Files .
	 * 
	 * @return FileArchivingTasklet
	 */
	@Bean
	@StepScope
	public FileArchivingTasklet fileDelegation() {
		LOGGER.debug("Entered into fileDelegation  method {} to archive the files into new folder  ");
		FileArchivingTasklet task = new FileArchivingTasklet();
		task.setFileId(fileId);
		task.setResources(batchProcessDao.getMultiResources());
		return task;
	}
}
