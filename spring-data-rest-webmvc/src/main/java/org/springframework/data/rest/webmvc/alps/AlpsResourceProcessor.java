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

import static org.springframework.hateoas.core.DummyInvocationUtils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.config.MetadataConfiguration;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.util.Assert;

/**
 * {@link ResourceProcessor} to add a {@code profile} link to the root resource to point to the ALPS resources in case
 * the support for ALPS is activated.
 * 
 * @author Oliver Gierke
 */
public class AlpsResourceProcessor implements ResourceProcessor<RepositoryLinksResource> {

	private static final String PROFILE_REL = "profile";

	private final MetadataConfiguration metadataConfiguration;

	/**
	 * Creates a new {@link AlpsResourceProcessor} with the given {@link MetadataConfiguration}.
	 * 
	 * @param metadataConfiguration must not be {@literal null}.
	 */
	@Autowired
	public AlpsResourceProcessor(MetadataConfiguration metadataConfiguration) {

		Assert.notNull(metadataConfiguration, "MetadataConfiguration must not be null!");

		this.metadataConfiguration = metadataConfiguration;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceProcessor#process(org.springframework.hateoas.ResourceSupport)
	 */
	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resource) {

		if (metadataConfiguration.alpsEnabled()) {
			resource.add(ControllerLinkBuilder.linkTo(methodOn(AlpsController.class).alps()).withRel(PROFILE_REL));
		}

		return resource;
	}
}
