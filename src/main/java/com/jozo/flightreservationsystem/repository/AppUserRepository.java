package com.jozo.flightreservationsystem.repository;

import com.jozo.flightreservationsystem.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    // Find user by email
    AppUser findByEmail(String email);
    /*

    // Find user by email (Optional return)
    Optional<AppUser> findOptionalByEmail(String email);

    // Find user by first name
    List<AppUser> findByFirstName(String firstName);

    // Find user by last name
    List<AppUser> findByLastName(String lastName);

    // Find user by first name and last name
    List<AppUser> findByFirstNameAndLastName(String firstName, String lastName);

    // Find users by role
    List<AppUser> findByRole(String role);

    // Find users by creation date range
    List<AppUser> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find users created after specific date
    List<AppUser> findByCreatedAtAfter(LocalDateTime createdAfter);

    // Find users created before specific date
    List<AppUser> findByCreatedAtBefore(LocalDateTime createdBefore);

    // Find users by partial first name match
    List<AppUser> findByFirstNameContainingIgnoreCase(String firstName);

    // Find users by partial last name match
    List<AppUser> findByLastNameContainingIgnoreCase(String lastName);

    // Find users by partial email match
    List<AppUser> findByEmailContainingIgnoreCase(String email);

    // Check if email exists
    boolean existsByEmail(String email);

    // Check if user exists by first name and last name
    boolean existsByFirstNameAndLastName(String firstName, String lastName);

    // Count total users
    long count();

    // Count users by role
    long countByRole(String role);

    // Count users created in date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find users ordered by creation date
    List<AppUser> findByOrderByCreatedAtDesc();

    // Find users by role ordered by creation date
    List<AppUser> findByRoleOrderByCreatedAtDesc(String role);

    // Custom query to find users with specific role and creation date range
    @Query("SELECT u FROM AppUser u WHERE u.role = :role AND u.createdAt BETWEEN :startDate AND :endDate")
    List<AppUser> findUsersByRoleAndDateRange(@Param("role") String role, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);

    // Custom query to find users by partial name search
    @Query("SELECT u FROM AppUser u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<AppUser> findUsersBySearchTerm(@Param("searchTerm") String searchTerm);

     */
}
