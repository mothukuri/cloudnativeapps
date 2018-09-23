package com.aacloud.servicebrokermongo.repository;

import com.aacloud.servicebrokermongo.model.ServiceInstance;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for ServiceInstance objects
 * 
 * @author sgreenberg@pivotal.io
 *
 */
public interface MongoServiceInstanceRepository extends MongoRepository<ServiceInstance, String> {

}