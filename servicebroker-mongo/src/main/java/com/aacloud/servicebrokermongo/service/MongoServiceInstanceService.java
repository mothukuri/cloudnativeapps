package com.aacloud.servicebrokermongo.service;

import com.aacloud.servicebrokermongo.exception.MongoServiceException;
import com.aacloud.servicebrokermongo.model.ServiceInstance;
import com.aacloud.servicebrokermongo.repository.MongoServiceInstanceRepository;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.*;

import org.springframework.cloud.servicebroker.model.instance.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

/**
 * Mongo impl to manage service instances.  Creating a service does the following:
 * creates a new database,
 * saves the ServiceInstance info to the Mongo repository.
 *
 * @author sgreenberg@pivotal.io
 */
@Service
public class MongoServiceInstanceService implements ServiceInstanceService {

  private MongoAdminService mongo;

  private MongoServiceInstanceRepository repository;

  @Autowired
  public MongoServiceInstanceService(MongoAdminService mongo, MongoServiceInstanceRepository repository) {
    this.mongo = mongo;
    this.repository = repository;
  }

  @Override
  public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
    // make sure we haven't provisioned this before (check broker database)
    ServiceInstance instance = repository.findById(request.getServiceInstanceId()).get();
    if (instance != null) {
      throw new ServiceInstanceExistsException(request.getServiceInstanceId(), request.getServiceDefinitionId());
    }

    instance = new ServiceInstance(request);

    if (mongo.databaseExists(instance.getServiceInstanceId())) {
      // ensure the instance is empty
      mongo.deleteDatabase(instance.getServiceInstanceId());
    }

    MongoDatabase db = mongo.createDatabase(instance.getServiceInstanceId());
    if (db == null) {
      throw new ServiceBrokerException("Failed to create new DB instance: " + instance.getServiceInstanceId());
    }
    //save to broker database for record keeping
    repository.save(instance);

    return CreateServiceInstanceResponse.builder().build();
  }

  @Override
  public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
    return GetLastServiceOperationResponse.builder().operationState(OperationState.SUCCEEDED).build();
  }

  ServiceInstance getServiceInstance(String id) {
    return repository.findById(id).get();
  }

  @Override
  public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) throws MongoServiceException {
    String instanceId = request.getServiceInstanceId();
    //locate record in broker database
    ServiceInstance instance = repository.findById(instanceId).get();
    if (instance == null) {
      throw new ServiceInstanceDoesNotExistException(instanceId);
    }
    // delete mongo database
    mongo.deleteDatabase(instanceId);
    // delete record from broker database
    repository.deleteById(instanceId);
    return DeleteServiceInstanceResponse.builder().build();
  }

  @Override
  public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
    String instanceId = request.getServiceInstanceId();
    ServiceInstance instance = repository.findById(instanceId).get();
    if (instance == null) {
      throw new ServiceInstanceDoesNotExistException(instanceId);
    }

    repository.deleteById(instanceId);
    ServiceInstance updatedInstance = new ServiceInstance(request);
    repository.save(updatedInstance);
    return UpdateServiceInstanceResponse.builder().build();
  }

}
