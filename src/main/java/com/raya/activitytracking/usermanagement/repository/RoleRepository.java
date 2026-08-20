package com.raya.activitytracking.usermanagement.repository;
import com.raya.activitytracking.usermanagement.entity.Role_;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role_,Integer> {

}
