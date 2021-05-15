package com.sapient.publicis.service.exchange;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableMap;
import com.sapient.publicis.exception.WeatherServiceException;

@Service
@ConditionalOnExpression("#{'${reactiveMode}'!= 'true' }")
public class RESTTemplateHandler implements WeatherRestExchange {

	@Value("${apiKey}")
	private String apiKey;

	@Value("${weatherAPIVersion}")
	private String weatherAPIVersion;

	@Autowired
	private RestTemplate restTemplate;

	private Map<String, String> globalMap;

	@PostConstruct
	public void init() {
		globalMap = ImmutableMap.of("APP_KEY", apiKey, "DEST_API_VERSION", weatherAPIVersion);
	}

	@Override
	public <T> T retreive(final Class<T> type, final Map<String, ?> map) {
		final Map<String, Object> mergedMap = new HashMap<>(map);
		mergedMap.putAll(globalMap);
		final ResponseEntity<T> response = restTemplate.getForEntity(BASE_URL, type, mergedMap);

		final T body = response.getBody();

		if (body == null) {
			throw new WeatherServiceException("No record found");
		}

		return body;
	}

}