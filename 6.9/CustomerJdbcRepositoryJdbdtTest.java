package com.sergiotrapiello.cursotesting.integration;

import static org.jdbdt.JDBDT.assertInserted;
import static org.jdbdt.JDBDT.builder;
import static org.jdbdt.JDBDT.data;
import static org.jdbdt.JDBDT.database;
import static org.jdbdt.JDBDT.populate;
import static org.jdbdt.JDBDT.restore;
import static org.jdbdt.JDBDT.save;
import static org.jdbdt.JDBDT.table;
import static org.jdbdt.JDBDT.teardown;
import static org.jdbdt.JDBDT.truncate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.h2.jdbcx.JdbcConnectionPool;
import org.jdbdt.Conversion;
import org.jdbdt.DB;
import org.jdbdt.DataSet;
import org.jdbdt.Table;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerJdbcRepositoryJdbdtTest {

	private static CustomerJdbcRepository customerRepository;

	private static DB database;

	private static Table customerTable;

	@BeforeAll
	static void setUp() throws Exception {
		database = database(h2Connection());

		customerRepository = new CustomerJdbcRepository(database.getConnection());

		customerTable = table("CUSTOMER").columns("LEGAL_IDENTIFIER", "NAME", "LASTNAME", "EMAIL", "PHONE")
				.build(database);
		DataSet initialData = builder(customerTable).value("LEGAL_IDENTIFIER", "12345678Z").value("NAME", "Michael")
				.value("LASTNAME", "Jordan").value("EMAIL", "michaeljordan@mail.com").value("PHONE", "611222333")
				.generate(1).data();

		populate(initialData);

		database.getConnection().setAutoCommit(false);
	}


	private static Connection h2Connection() throws SQLException {
		JdbcConnectionPool cp = JdbcConnectionPool
				.create("jdbc:h2:mem:sample;INIT=RUNSCRIPT FROM 'classpath:customers-schema.sql'", "sa", "sa");
		return cp.getConnection();
	}


	@AfterAll
	static void globalTeardown() {
		truncate(customerTable);
		teardown(database, true);
	}

	@BeforeEach
	public void saveState() {
		// Set save point
		save(database);
	}

	@AfterEach
	public void restoreState() {
		// Restore state to save point
		restore(database);
	}


	private static final Conversion<Customer> getConversion() {
		return c -> new Object[] { c.getLegalIdentifier(), c.getName(), c.getLastName(), c.getEmail(),
				c.getPhoneNumber() };
	}

	static DataSet toDataSet(Customer customer) {
		return data(customerTable, getConversion()).row(customer);
	}

	@Test
	void shouldGetCustomer() {

		// GIVEN
		Integer id = 1000;
		Customer expectedCustomer = createDefaultCustomer();

		// WHEN
		Customer resultCustomer = customerRepository.get(id);

		// THEN
		assertNotNull(resultCustomer, "Should have found a customer for the id " + id);
		assertCustomer(expectedCustomer, resultCustomer);
	}

	@Test
	void shouldNotGetCustomer_idNotFound() {

		// GIVEN
		Integer notFoundId = 666;

		// WHEN
		Customer resultCustomer = customerRepository.get(notFoundId);

		// THEN
		assertNull(resultCustomer, "Should NOT have found a customer for the id " + notFoundId);
	}

	@Test
	void shouldListCustomers() {

		// GIVEN
		List<Customer> expectedCustomers = List.of(createDefaultCustomer());

		// WHEN
		List<Customer> resultCustomers = customerRepository.list();

		// THEN
		assertEquals(expectedCustomers.size(), resultCustomers.size(),
				"The number of customers listed is not what expected");
		assertCustomer(expectedCustomers.get(0), resultCustomers.get(0));
	}

	@Test
	void shouldSaveCustomer() {

		// GIVEN
		Customer customer = new Customer();
		customer.setLegalIdentifier("2222222N");
		customer.setName("Sergio");
		customer.setLastName("Garcia");
		customer.setEmail("sergiogarcia@mail.com");

		// WHEN
		customerRepository.save(customer);

		// THEN
		assertInserted(toDataSet(customer));
	}

	private void assertCustomer(Customer expected, Customer actual) {
		assertEquals(expected.getLegalIdentifier(), actual.getLegalIdentifier());
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getLastName(), actual.getLastName());
		assertEquals(expected.getEmail(), actual.getEmail());
		assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber());
	}

	private Customer createDefaultCustomer() {
		Customer customer = new Customer();
		customer.setLegalIdentifier("12345678Z");
		customer.setName("Michael");
		customer.setLastName("Jordan");
		customer.setEmail("michaeljordan@mail.com");
		customer.setPhoneNumber("611222333");
		return customer;
	}
}
