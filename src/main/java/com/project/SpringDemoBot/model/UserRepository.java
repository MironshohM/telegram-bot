package com.project.SpringDemoBot.model;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository  extends JpaRepository<User,Long> {


}
