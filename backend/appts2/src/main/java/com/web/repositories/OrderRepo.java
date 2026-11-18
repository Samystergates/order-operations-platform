package com.web.repositories;

import com.web.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Order o WHERE o.id IN :ids")
    int deleteOrdersByIds(@Param("ids") List<Integer> ids);


}
