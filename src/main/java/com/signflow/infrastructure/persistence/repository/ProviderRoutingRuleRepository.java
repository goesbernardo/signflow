package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.ProviderRoutingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderRoutingRuleRepository extends JpaRepository<ProviderRoutingRuleEntity, Long> {
    List<ProviderRoutingRuleEntity> findAllByUserIdAndActiveTrueOrderByPriorityAsc(String userId);
    List<ProviderRoutingRuleEntity> findAllByUserIdOrderByPriorityAsc(String userId);
}
