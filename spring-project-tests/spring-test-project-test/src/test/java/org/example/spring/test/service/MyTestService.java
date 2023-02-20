package org.example.spring.test.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyTestService implements TestService {
        @Override
        public void test() {
            transactionInfo();
        }

        @Transactional
        public void transactionInfo() {

        }
    }