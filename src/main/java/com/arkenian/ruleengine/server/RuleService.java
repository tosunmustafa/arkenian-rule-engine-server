package com.arkenian.ruleengine.server;

import com.arkenian.ruleengine.model.Subject;
import com.arkenian.ruleengine.server.runner.RuleRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheResult;
import java.util.Collection;
import java.util.Collections;

@Service
public class RuleService {

    //private final static Logger logger = LogManager.getLogger(RuleService.class);

    @Value("#{T(org.springframework.util.StringUtils).commaDelimitedListToSet('${arkenian.rules.exclude}')}")
    private Collection<String> exclusions;

    @Autowired
    private IOService ioService;


    @CacheResult(cacheName = "subject-conform-set")
    public Collection<Long> getConformingSet(@CacheKey Long oid) {
        Collection<Long> complySet;
        try {
            long cp0 = System.nanoTime();
            Subject subject = ioService.getSubject(oid);
            long cp1 = System.nanoTime();
            complySet = RuleRunner.execute(subject, exclusions);
            long cp2 = System.nanoTime();
            //logger.trace("ObjectId:{} deserialize:{}ns execution:{}ns", oid, cp1 - cp0, cp2 - cp1);
        } catch (Exception e) {
            //logger.error("", e);
            complySet = Collections.emptyList();
        }
        return complySet;
    }
}
