package com.web.appts.repositories;

import com.web.appts.entities.ArchivedOrders;
import com.web.appts.entities.OrderPreserves;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPreservesRepo extends JpaRepository<OrderPreserves, Long> {
}
