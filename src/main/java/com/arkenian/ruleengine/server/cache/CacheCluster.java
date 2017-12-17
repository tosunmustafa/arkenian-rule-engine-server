package com.arkenian.ruleengine.server.cache;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.Collection;

public class CacheCluster {
    public static void main(String[] args) {
        Config config = new Config().setInstanceName("arkenian-re-hazelcast-instance");
        config.getManagementCenterConfig().setEnabled(true);
        config.getManagementCenterConfig().setUrl("http://localhost:8080/mancenter");
        MutableConfiguration<Integer, Collection> mutableConfiguration = new MutableConfiguration<Integer, Collection>()
                .setStatisticsEnabled(true)
                .setManagementEnabled(true)
                .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.TEN_MINUTES));
        HazelcastServerCachingProvider.createCachingProvider(Hazelcast.newHazelcastInstance(config)).getCacheManager().createCache("customer-ag-eligibility", mutableConfiguration);
    }
}
