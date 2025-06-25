package com.example.evalrh.service.test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TestService {
    private final JdbcTemplate jdbcTemplate;

    public TestService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Lists all employees from the 'tabEmployee' table.
     * @return A list of maps, where each map represents an employee row.
     */
    public List<Map<String, Object>> getAllEmployees() {
        return jdbcTemplate.queryForList("SELECT * FROM tabEmployee");
    }

    /**
     * Retrieves a single employee by their ID.
     * @param id The ID of the employee to retrieve.
     * @return A map representing the employee row, or null if not found.
     */
    public Map<String, Object> getEmployeeById(Long id) {
        String sql = "SELECT * FROM tabEmployee WHERE id = ?";
        try {
            return jdbcTemplate.queryForMap(sql, id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // Handle the case where no employee with the given ID is found
            return null;
        }
    }

    /**
     * Inserts a new employee into the 'tabEmployee' table.
     * @param name The name of the employee.
     * @param position The position of the employee.
     * @return The number of rows affected (should be 1 for a successful insert).
     */
    public int insertEmployee(String name, String position) {
        String sql = "INSERT INTO tabEmployee (name, position) VALUES (?, ?)";
        return jdbcTemplate.update(sql, name, position);
    }

    /**
     * Updates an existing employee's information.
     * @param id The ID of the employee to update.
     * @param newName The new name for the employee.
     * @param newPosition The new position for the employee.
     * @return The number of rows affected (should be 1 for a successful update).
     */
    public int updateEmployee(Long id, String newName, String newPosition) {
        String sql = "UPDATE tabEmployee SET name = ?, position = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newName, newPosition, id);
    }

    /**
     * Deletes an employee from the 'tabEmployee' table by their ID.
     * @param id The ID of the employee to delete.
     * @return The number of rows affected (should be 1 for a successful deletion).
     */
    public int deleteEmployee(Long id) {
        String sql = "DELETE FROM tabEmployee WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}