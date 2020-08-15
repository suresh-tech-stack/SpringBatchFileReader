package com.conduent.plcl.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.conduent.plcl.batch.execute.InterceptingJobExecution;
import com.conduent.plcl.batch.execute.PlateCorrectionExecute;
import com.conduent.plcl.batch.model.PlateCorrection;
import com.conduent.plcl.batch.model.PLCLParams;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(value = "classpath:test-config.properties")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class })
@SpringBootTest(classes = { SingleJobLauncherTestUtils.class, PlateCorrectionExecute.class,
		PlateCorrectionBatch.class })

public class PLCLIntegrationTest {

	@Autowired
	private PlateCorrectionExecute config;
	@Autowired
	private JobOperator jobOperator;
	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private JobExplorer jobExplorer;

	private SimpleJobLauncher jobLauncher;

	@Autowired
	private InterceptingJobExecution interJobExecution;

	@Autowired
	@Lazy
	private JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier(value = "singleJobLauncherTestUtils")
	private JobLauncherTestUtils singleJobLauncherTestUtils;

	public void setJdbcTemplate(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Test
	@Ignore
	public void testRecordCount() throws Exception {
		boolean actual = getRecordCount();
		System.out.println("job Completed ........");
		assertEquals(true, actual);
	}

	@Test
	@Ignore
	public void testReadFlatFileJob() throws Exception {
		Job job = null;
		job = config.readFlatFileJob();
		assertNotNull(job);
	}

	@Test
	@Ignore
	public void testLineMapper() throws Exception {
		LineMapper<PlateCorrection> lineMapper = null;
		lineMapper = config.lineMapper();
		assertNotNull(lineMapper);
	}

	@Test
	@Ignore
	public void testProcessor() throws Exception {
		ItemProcessor<PlateCorrection, PlateCorrection> process = null;
		process = config.processor();
		assertNotNull(process);
	}

	@Test
	@Ignore
	public void testFileTokenizer() throws Exception {
		LineTokenizer tokenizer = null;
		tokenizer = config.fileTokenizer();
		assertNotNull(tokenizer);
	}

	public boolean getRecordCount() {
		boolean result = false;
		Number num = jdbcTemplate.queryForObject(
				"select count(*) from  T_STG_PLATE_CRRCTN_LIST_BATCH where job_name = 'PLCL'", Long.class);
		if (num.intValue() > 0) {
			result = true;
		}
		return result;
	}
}

@Component(value = "singleJobLauncherTestUtils")
class SingleJobLauncherTestUtils extends JobLauncherTestUtils {
	@Autowired
	@Override
	public void setJob(@Qualifier(value = "readFlatFileJob") Job job) {
		super.setJob(job);
	}
}
