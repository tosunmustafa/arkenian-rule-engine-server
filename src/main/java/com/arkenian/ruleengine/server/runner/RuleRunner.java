package com.arkenian.ruleengine.server.runner;

import com.arkenian.ruleengine.model.Subject;
import com.arkenian.ruleengine.predicate.rule.inventory.RuleInventory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class RuleRunner {

    private RuleRunner() {
        //hide constructor
    }

    public static Collection<Long> execute(Subject subject) {
        return execute(subject, Collections.emptySet());
    }

    public static Collection<Long> execute(Subject subject, Collection<String> exclusions) {
        Set<Long> eligible = new HashSet<>();
        RuleInventory[] values = RuleInventory.values();
        for (int i = 0; i < values.length; i++) {
            RuleInventory rule = values[i];
            if (!exclusions.contains(rule.getPredicate().getClass().getSimpleName())) {
                if (rule.getPredicate().test(subject))
                    eligible.add(rule.ruleId());
            }
        }
        return eligible;
    }
}
