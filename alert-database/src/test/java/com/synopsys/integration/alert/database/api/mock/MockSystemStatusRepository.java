package com.synopsys.integration.alert.database.api.mock;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.synopsys.integration.alert.database.system.SystemStatus;
import com.synopsys.integration.alert.database.system.SystemStatusRepository;

public class MockSystemStatusRepository implements SystemStatusRepository {

    //Only methods that are used by a test are currently implemented, all others are left default.
    SystemStatus systemStatus = new SystemStatus(Boolean.FALSE, new Date());

    @Override
    public List<SystemStatus> findAll() {
        return List.of(systemStatus);
    }

    @Override
    public List<SystemStatus> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<SystemStatus> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<SystemStatus> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(SystemStatus entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends SystemStatus> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends SystemStatus> S save(S entity) {
        this.systemStatus = new SystemStatus(entity.isInitialConfigurationPerformed(), entity.getStartupTime());

        return (S) this.systemStatus;
    }

    @Override
    public <S extends SystemStatus> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<SystemStatus> findById(Long aLong) {
        return Optional.ofNullable(this.systemStatus);
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends SystemStatus> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<SystemStatus> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public SystemStatus getOne(Long aLong) {
        return null;
    }

    @Override
    public <S extends SystemStatus> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends SystemStatus> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends SystemStatus> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends SystemStatus> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends SystemStatus> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends SystemStatus> boolean exists(Example<S> example) {
        return false;
    }
}
