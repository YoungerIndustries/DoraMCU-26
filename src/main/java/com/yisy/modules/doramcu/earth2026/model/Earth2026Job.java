package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a job title available on a planet.
 * <p>
 * Jobs define the roles characters can hold, with associated salary ranges,
 * required skills, and career progression paths. Similar to Traveller's
 * career system but using modern job titles and economic models.
 * </p>
 */
@Entity
@Table(name = "earth2026_jobs", indexes = {
        @Index(name = "idx_earth2026_job_title", columnList = "title"),
        @Index(name = "idx_earth2026_job_category", columnList = "category"),
        @Index(name = "idx_earth2026_job_planet", columnList = "planetId")
})
public class Earth2026Job {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Job title (e.g., "Software Engineer", "CEO", "Police Officer").
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Job category (e.g., "Technology", "Government", "Military", "Finance", "Healthcare").
     */
    @Column(nullable = false, length = 100)
    private String category;

    /**
     * Job level (1-10, higher = more senior).
     */
    @Column(name = "job_level", nullable = false)
    private Integer jobLevel;

    /**
     * Minimum salary in USD (annual).
     */
    @Column(name = "min_salary", nullable = false)
    private Integer minSalary;

    /**
     * Maximum salary in USD (annual).
     */
    @Column(name = "max_salary", nullable = false)
    private Integer maxSalary;

    /**
     * Required minimum character level.
     */
    @Column(name = "min_char_level", nullable = false)
    private Integer minCharLevel;

    /**
     * Required minimum INT score.
     */
    @Column(name = "min_int")
    private Integer minInt;

    /**
     * Required minimum STR score.
     */
    @Column(name = "min_str")
    private Integer minStr;

    /**
     * Required minimum CHA score.
     */
    @Column(name = "min_cha")
    private Integer minCha;

    /**
     * XP awarded per turn while holding this job.
     */
    @Column(name = "xp_per_turn", nullable = false)
    private Integer xpPerTurn;

    /**
     * Planet this job is available on.
     */
    @Column(name = "planet_id", nullable = false)
    private Long planetId;

    /**
     * Country this job is available in (nullable for planet-wide jobs).
     */
    @Column(name = "country_id")
    private Long countryId;

    /**
     * Whether this job is currently available.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Description of the job responsibilities.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Benefits description (e.g., "Health insurance, 401k, stock options").
     */
    @Column(length = 500)
    private String benefits;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Earth2026Job() {
    }

    public Earth2026Job(String title, String category, Integer jobLevel, Long planetId) {
        this.title = title;
        this.category = category;
        this.jobLevel = jobLevel;
        this.planetId = planetId;
        this.minSalary = 30000;
        this.maxSalary = 80000;
        this.minCharLevel = 1;
        this.xpPerTurn = 10;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getJobLevel() {
        return jobLevel;
    }

    public void setJobLevel(Integer jobLevel) {
        this.jobLevel = jobLevel;
    }

    public Integer getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(Integer minSalary) {
        this.minSalary = minSalary;
    }

    public Integer getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(Integer maxSalary) {
        this.maxSalary = maxSalary;
    }

    public Integer getMinCharLevel() {
        return minCharLevel;
    }

    public void setMinCharLevel(Integer minCharLevel) {
        this.minCharLevel = minCharLevel;
    }

    public Integer getMinInt() {
        return minInt;
    }

    public void setMinInt(Integer minInt) {
        this.minInt = minInt;
    }

    public Integer getMinStr() {
        return minStr;
    }

    public void setMinStr(Integer minStr) {
        this.minStr = minStr;
    }

    public Integer getMinCha() {
        return minCha;
    }

    public void setMinCha(Integer minCha) {
        this.minCha = minCha;
    }

    public Integer getXpPerTurn() {
        return xpPerTurn;
    }

    public void setXpPerTurn(Integer xpPerTurn) {
        this.xpPerTurn = xpPerTurn;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Long planetId) {
        this.planetId = planetId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Calculate average salary for this job.
     *
     * @return average annual salary
     */
    public int getAverageSalary() {
        return (minSalary + maxSalary) / 2;
    }

    @Override
    public String toString() {
        return "Earth2026Job{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", level=" + jobLevel +
                ", salary=" + minSalary + "-" + maxSalary +
                '}';
    }
}
