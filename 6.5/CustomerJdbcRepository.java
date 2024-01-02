package com.sergiotrapiello.cursotesting.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class CustomerJdbcRepository {

	private final Connection conn;

	public CustomerJdbcRepository(DataSource dataSource) {
		try {
			this.conn = dataSource.getConnection();
		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
	}

	public CustomerJdbcRepository(Connection connection) {
		this.conn = connection;
	}

	public Customer get(Integer id) {
		String sql = "SELECT * FROM CUSTOMER WHERE ID = ?";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, id);

			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				return populateCustomer(resultSet);
			}
			return null;
		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
	}

	public List<Customer> list() {
		String sql = "SELECT * FROM CUSTOMER";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			List<Customer> customers = new ArrayList<Customer>();
			while (resultSet.next()) {
				customers.add(populateCustomer(resultSet));
			}
			return customers;
		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
	}

	public Integer save(Customer customer) {
		String sql = "INSERT INTO CUSTOMER (LEGAL_IDENTIFIER, NAME, LASTNAME, EMAIL, PHONE) VALUES (?, ?, ?, ?, ?)";

		try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, customer.getLegalIdentifier());
			statement.setString(2, customer.getName());
			statement.setString(3, customer.getLastName());
			statement.setString(4, customer.getEmail());
			statement.setString(5, customer.getPhoneNumber());

			statement.executeUpdate();

			ResultSet generatedKeys = statement.getGeneratedKeys();
			generatedKeys.next();
			return generatedKeys.getInt(1);

		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
	}

	public void delete(Integer id) {
		String sql = "DELETE FROM CUSTOMER WHERE ID = ?";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
	}

	private Customer populateCustomer(ResultSet resultSet) throws SQLException {
		Customer customer = new Customer();
		customer.setId(resultSet.getInt("id"));
		customer.setLegalIdentifier(resultSet.getString("legal_identifier"));
		customer.setName(resultSet.getString("name"));
		customer.setLastName(resultSet.getString("lastname"));
		customer.setEmail(resultSet.getString("email"));
		customer.setPhoneNumber(resultSet.getString("phone"));
		return customer;
	}

}
