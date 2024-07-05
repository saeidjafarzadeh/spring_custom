package org.spring.boot.spring_custom.conroller;

import org.spring.boot.spring_custom.spring.annotations.SpringBootApplication;
import org.spring.boot.spring_custom.spring.container.SpringInitialize;


public class Main {

    @SpringBootApplication
    public static void main(String[] args) {
        SpringInitialize.initializer("org/spring/boot/spring_custom");

    }
}
