package com.arkenian.ruleengine.server;

import com.arkenian.ruleengine.model.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collection;

@SpringBootApplication
//@EnableCaching
@RestController("/")
@ComponentScan(basePackages = {"com.arkenian.ruleengine.server"})
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Value("${arkenian.server.path}")
    private String nodePath;

    @Autowired
    private IOService ioService;

    @Autowired
    private RuleService ruleService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/getConformingSet/{subjectId}")
    public ResponseEntity<Collection<Long>> getConformingSet(@PathVariable("subjectId") Long subjectId) {
        return new ResponseEntity<>(ruleService.getConformingSet(subjectId), HttpStatus.OK);
    }

    @GetMapping("/get/{subjectId}")
    public Subject getSubject(@PathVariable("subjectId") Long subjectId) throws IOException {
        return ioService.getSubject(subjectId);
    }

    @PutMapping("/set")
    public ResponseEntity<String> createSubject(@RequestBody Subject subject) {
        ResponseEntity<String> re;
        try {
            ioService.createSubject(subject);
            re = new ResponseEntity<>("OK", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Unable to persist subject. Subject Id:{}", subject.getOid(), e);
            re = new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return re;
    }
}
