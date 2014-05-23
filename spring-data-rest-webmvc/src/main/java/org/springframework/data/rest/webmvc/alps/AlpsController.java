/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.rest.webmvc.alps;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.MetadataConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.hateoas.alps.Alps;
import org.springframework.hateoas.alps.Descriptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Controller exposing semantic documentation for the resources exposed using the Application Level Profile Semantics
 * format.
 * 
 * @author Oliver Gierke
 * @see http://alps.io
 */
@Controller
@RequestMapping("/alps")
public class AlpsController {

	private final Repositories repositories;
	private final ResourceMappings mappings;
	private final MetadataConfiguration configuration;

	/**
	 * Creates a new {@link AlpsController} for the given {@link Repositories},
	 * {@link RootResourceInformationToAlpsDescriptorConverter} and {@link ResourceMappings}.
	 * 
	 * @param repositories must not be {@literal null}.
	 * @param mappings must not be {@literal null}.
	 * @param configuration must not be {@literal null}.
	 */
	public AlpsController(Repositories repositories, ResourceMappings mappings, MetadataConfiguration configuration) {

		Assert.notNull(repositories, "Repositories must not be null!");
		Assert.notNull(mappings, "ResourceMappings must not be null!");
		Assert.notNull(configuration, "MetadataConfiguration must not be null!");

		this.repositories = repositories;
		this.mappings = mappings;
		this.configuration = configuration;
	}

	/**
	 * Exposes the allowed HTTP methods for the ALPS resources.
	 * 
	 * @return
	 */
	@RequestMapping(value = { "", "/{repository}" }, method = OPTIONS)
	HttpEntity<?> alpsOptions() {

		verifyAlpsEnabled();

		HttpHeaders headers = new HttpHeaders();
		headers.setAllow(Collections.singleton(HttpMethod.GET));

		return new ResponseEntity<Object>(headers, HttpStatus.OK);
	}

	/**
	 * Exposes a resource to contain descriptors pointing to the discriptors for individual resources.
	 * 
	 * @return
	 */
	@RequestMapping(method = GET)
	HttpEntity<Alps> alps() {

		verifyAlpsEnabled();

		List<Descriptor> descriptors = new ArrayList<Descriptor>();

		for (Class<?> domainType : repositories) {

			UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequestUri();
			ResourceMetadata mapping = mappings.getMappingFor(domainType);
			String href = builder.pathSegment(mapping.getRel()).build().toUriString();

			if (mapping.isExported()) {
				descriptors.add(Alps.descriptor().name(mapping.getRel()).href(href).build());
			}
		}

		Alps alps = Alps.alps().//
				descriptors(descriptors).//
				build();

		return new ResponseEntity<Alps>(alps, HttpStatus.OK);
	}

	/**
	 * Exposes an ALPS resource to describe an individual repository resource.
	 * 
	 * @param information
	 * @return
	 */
	@RequestMapping(value = "/{repository}", method = GET)
	HttpEntity<RootResourceInformation> descriptor(RootResourceInformation information) {

		verifyAlpsEnabled();

		return new ResponseEntity<RootResourceInformation>(information, HttpStatus.OK);
	}

	private void verifyAlpsEnabled() {

		if (!configuration.alpsEnabled()) {
			throw new ResourceNotFoundException();
		}
	}
}
