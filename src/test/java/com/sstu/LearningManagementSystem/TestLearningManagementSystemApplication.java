package com.sstu.LearningManagementSystem;

import org.springframework.boot.SpringApplication;

public class TestLearningManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(LearningManagementSystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
