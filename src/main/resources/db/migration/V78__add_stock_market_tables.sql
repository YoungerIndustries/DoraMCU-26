-- V78: Add stock market tables

CREATE TABLE stock_listings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    corporation_id BIGINT NOT NULL UNIQUE,
    ticker_symbol VARCHAR(10) NOT NULL UNIQUE,
    total_shares BIGINT NOT NULL,
    public_shares BIGINT NOT NULL,
    initial_price DECIMAL(15, 2) NOT NULL,
    current_price DECIMAL(15, 2) NOT NULL,
    day_high DECIMAL(15, 2),
    day_low DECIMAL(15, 2),
    volume_24h BIGINT NOT NULL DEFAULT 0,
    market_cap DECIMAL(20, 2),
    dividend_yield DOUBLE,
    ipo_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_price_update TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_listings_active ON stock_listings(is_active);

CREATE TABLE stock_holdings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    owner_type VARCHAR(20) NOT NULL,
    stock_listing_id BIGINT NOT NULL,
    shares_owned BIGINT NOT NULL,
    average_buy_price DECIMAL(15, 2) NOT NULL,
    total_dividends_received DECIMAL(15, 2) NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_holdings_owner ON stock_holdings(owner_id, owner_type);
CREATE INDEX idx_stock_holdings_stock ON stock_holdings(stock_listing_id);

CREATE TABLE stock_trades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_listing_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    shares_traded BIGINT NOT NULL,
    price_per_share DECIMAL(15, 2) NOT NULL,
    total_value DECIMAL(20, 2) NOT NULL,
    market_tax DECIMAL(20, 2) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_trades_listing ON stock_trades(stock_listing_id);
CREATE INDEX idx_stock_trades_buyer ON stock_trades(buyer_id);
CREATE INDEX idx_stock_trades_seller ON stock_trades(seller_id);
CREATE INDEX idx_stock_trades_timestamp ON stock_trades(timestamp);

CREATE TABLE stock_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_listing_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    order_type VARCHAR(10) NOT NULL,
    order_style VARCHAR(10) NOT NULL,
    limit_price DECIMAL(15, 2),
    quantity BIGINT NOT NULL,
    filled_quantity BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE INDEX idx_stock_orders_listing ON stock_orders(stock_listing_id);
CREATE INDEX idx_stock_orders_player ON stock_orders(player_id);
CREATE INDEX idx_stock_orders_status ON stock_orders(status);
CREATE INDEX idx_stock_orders_expires ON stock_orders(expires_at);

CREATE TABLE dividend_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_listing_id BIGINT NOT NULL,
    amount_per_share DECIMAL(15, 2) NOT NULL,
    total_paid DECIMAL(20, 2) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    quarter VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dividend_payments_listing ON dividend_payments(stock_listing_id);
CREATE INDEX idx_dividend_payments_quarter ON dividend_payments(quarter);
CREATE INDEX idx_dividend_payments_date ON dividend_payments(payment_date);
