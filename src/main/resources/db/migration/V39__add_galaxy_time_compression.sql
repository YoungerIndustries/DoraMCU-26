-- V39: Add time compression fields to galaxies table for Earth 2026 master clock support
-- These fields enable each galaxy to have its own time flow relative to the Earth 2026 master clock

ALTER TABLE galaxies ADD COLUMN time_compression_ratio DOUBLE NOT NULL DEFAULT 1.0;
ALTER TABLE galaxies ADD COLUMN timezone VARCHAR(64) DEFAULT 'UTC';
ALTER TABLE galaxies ADD COLUMN epoch_offset_seconds BIGINT DEFAULT 0;
