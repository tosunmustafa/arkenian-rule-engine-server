package com.arkenian.ruleengine.server;


import com.arkenian.ruleengine.model.Subject;
import com.arkenian.ruleengine.utility.IOUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class IOService {

    @Value("${arkenian.server.path}")
    private String nodePath;

    public Subject getSubject(Long oid) throws IOException {
        return IOUtility.getSubject(oid, nodePath);
    }

    public void createSubject(Subject subject) throws IOException {
        IOUtility.createSubject(subject, nodePath);
    }
}

