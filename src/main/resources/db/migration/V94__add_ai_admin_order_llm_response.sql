-- Add llm_response column to ai_admin_orders for storing LLM analysis results

ALTER TABLE ai_admin_orders
    ADD COLUMN llm_response TEXT NULL;
