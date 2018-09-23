package com.aacloud.servicebrokermongo.service;

import com.aacloud.servicebrokermongo.model.ServiceInstanceBinding;
import com.aacloud.servicebrokermongo.repository.MongoServiceInstanceBindingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * Mongo impl to bind services.  Binding a service does the following:
 * creates a new user in the database (currently uses a default pwd of "password"),
 * saves the ServiceInstanceBinding info to the Mongo repository.
 *
 * @author sgreenberg@pivotal.io
 */
@Service
public class MongoServiceInstanceBindingService implements ServiceInstanceBindingService {

	private MongoAdminService mongo;

	private MongoServiceInstanceBindingRepository bindingRepository;

	@Autowired
	public MongoServiceInstanceBindingService(MongoAdminService mongo,
											  MongoServiceInstanceBindingRepository bindingRepository) {
		this.mongo = mongo;
		this.bindingRepository = bindingRepository;
	}

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {

		String bindingId = request.getBindingId();
		String serviceInstanceId = request.getServiceInstanceId();

		ServiceInstanceBinding binding = bindingRepository.findById(bindingId).get();
		if (binding != null) {
			throw new ServiceInstanceBindingExistsException(serviceInstanceId, bindingId);
		}

		String password = "password";

		mongo.createUser(serviceInstanceId, bindingId, password);

		Map<String, Object> credentials =
				Collections.singletonMap("uri", mongo.getConnectionString(serviceInstanceId, bindingId, password));

		binding = new ServiceInstanceBinding(bindingId, serviceInstanceId, credentials, null, request.getAppGuid());
		bindingRepository.save(binding);
		return CreateServiceInstanceAppBindingResponse.builder().credentials(credentials).build();
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
		String bindingId = request.getBindingId();
		ServiceInstanceBinding binding = getServiceInstanceBinding(bindingId);

		if (binding == null) {
			throw new ServiceInstanceBindingDoesNotExistException(bindingId);
		}

		mongo.deleteUser(binding.getServiceInstanceId(), bindingId);
		bindingRepository.deleteById(bindingId);
	}

	ServiceInstanceBinding getServiceInstanceBinding(String id) {
		return bindingRepository.findById(id).get();
	}

}
