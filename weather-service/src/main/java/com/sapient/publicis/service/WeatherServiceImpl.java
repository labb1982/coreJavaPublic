package com.sapient.publicis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.sapient.publicis.model.in.WeatherProcessingRequest;
import com.sapient.publicis.model.in.WeatherProcessingResponse;
import com.sapient.publicis.model.out.WeatherResponse;
import com.sapient.publicis.service.exchange.WeatherRestExchange;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

	@Autowired
	private WeatherRestExchange restTemplateHandler;

	@Cacheable(value = "weatherCache", key = "#weatherProcessingRequest.getCacheKey")
	public WeatherProcessingResponse process(final WeatherProcessingRequest weatherProcessingRequest) {
		log.error("not returning from cache");
		final WeatherResponse body = restTemplateHandler.retreive(WeatherResponse.class,
				ImmutableMap.of("cityList", weatherProcessingRequest.getLocations()));

		final DateWiseSummaryStatistics dateWiseSummaryStatistics = body.getList().stream()
				.filter(a -> a.isInExpectedDuration(weatherProcessingRequest)).collect(DateWiseSummaryStatistics::new,
						DateWiseSummaryStatistics::accept, DateWiseSummaryStatistics::combine);

		return new WeatherProcessingResponse(dateWiseSummaryStatistics.getDateWiseWeatherReport());
	}
}