package com.web.appts;

import com.web.appts.client.OrdersFeignClient;
import com.web.appts.entities.Order;
import com.web.appts.entities.OrderDepartment;
import com.web.appts.repositories.OrderRepo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
		"SPRING_DATASOURCE_URL=jdbc:h2:mem:appts-test;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=OFFSET",
		"SPRING_DATASOURCE_USERNAME=sa",
		"SPRING_DATASOURCE_PASSWORD=",
		"SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver",
		"APP_JWT_SECRET=test-secret",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ApptsApplicationTests {

	@MockBean
	private OrdersFeignClient ordersFeignClient;

	@javax.annotation.Resource
	private OrderRepo orderRepo;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void persistsAnOrderAndItsDepartmentWorkflow() {
		Order order = new Order();
		order.setId(123456);
		order.setOrderNumber("INTEGRATION-ORDER");
		order.setRegel("1");
		order.setCustomerName("Demo customer");

		OrderDepartment department = new OrderDepartment();
		department.setDepId(1);
		department.setDepName("SME");
		department.setStatus("R");
		department.setOrder(order);
		order.setDepartments(Collections.singletonList(department));

		orderRepo.saveAndFlush(order);

		Order persistedOrder = orderRepo
				.findByOrderNumberAndRegel("INTEGRATION-ORDER", "1")
				.orElseThrow(() -> new AssertionError("Persisted order was not found"));

		assertEquals("Demo customer", persistedOrder.getCustomerName());
		assertEquals(1, persistedOrder.getDepartments().size());
		assertEquals("SME", persistedOrder.getDepartments().get(0).getDepName());
		assertEquals("R", persistedOrder.getDepartments().get(0).getStatus());
	}
}
