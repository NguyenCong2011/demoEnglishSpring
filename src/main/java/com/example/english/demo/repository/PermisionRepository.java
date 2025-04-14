package com.example.english.demo.repository;

import com.example.english.demo.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PermisionRepository extends JpaRepository<Permission,String>{

}
