package com.conduent.plcl.batch.common;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.validator.SpringValidator;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.conduent.plcl.batch.model.PlateCorrection;

/**
 * This is Spring class to auto validation file
 * 
 * @author 52058018
 *
 */
@Configuration
public class ValidationConfiguration {
	private final static Logger LOGGER = Logger.getLogger(ValidationConfiguration.class);

	@Bean
	public Validator validator() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public ItemProcessor<PlateCorrection, PlateCorrection> validatingItemProcessor(Validator validator) {
		LOGGER.debug("In ValidationConfiguration :: Enter into validatingItemProcessor method {} ");
		SpringValidator<PlateCorrection> springValidator = new SpringValidator<>();
		springValidator.setValidator(validator);
		ValidatingItemProcessor<PlateCorrection> ValidatingItemProcessor = new ValidatingItemProcessor<>();
		ValidatingItemProcessor.setValidator(springValidator);

		return ValidatingItemProcessor;
	}
}