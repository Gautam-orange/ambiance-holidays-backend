-- V10 — Add inclusions/exclusions to transfer pricing tiers.
-- Operationally similar to the cars.includes / cars.excludes columns:
-- one bullet point per array element, surfaced in the customer-facing
-- "What's Included" block on the transfer detail page and edited from
-- the admin Transfer Pricing form.

ALTER TABLE transfer_pricing_tiers
    ADD COLUMN IF NOT EXISTS includes TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS excludes TEXT[] NOT NULL DEFAULT '{}';
