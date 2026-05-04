# Peach Payments — Webhook setup

The Peach webhook is a **server-to-server callback** that Peach sends when a
payment finalises. It guarantees we receive the outcome even if the customer
closes the browser before the redirect — which today leaves bookings stuck
in `PENDING` forever.

The endpoint is implemented at:

```
POST {backend-base-url}/api/v1/payments/peach/webhook
```

(The endpoint does NOT trust the body. It re-queries Peach's `/v2/checkout/{id}/status`
API for the authoritative result, then updates the Payment + Booking idempotently.
Replays are safe.)

---

## 1) Local development

Peach cannot reach `http://localhost:8080` directly — you need a public tunnel.

### a. Start the tunnel

You're already using ngrok (per `PEACH_BACKEND_BASE_URL=https://tabby-ageless-ending.ngrok-free.dev/api/v1`).
That same tunnel is what Peach should hit for the webhook.

```bash
ngrok http 8080
```

Note the public HTTPS URL it prints, e.g. `https://tabby-ageless-ending.ngrok-free.dev`.

### b. Set the env var so the backend uses it for `Origin` and shopperResultUrl

```bash
export PEACH_BACKEND_BASE_URL="https://tabby-ageless-ending.ngrok-free.dev/api/v1"
```

(Already in your local setup — verified earlier.)

### c. Configure the webhook URL in Peach dashboard

Go to **Peach Merchant Dashboard → Webhooks** (or "Notification URL" /
"Server-to-server callback" — naming varies by sandbox version).

Set the webhook URL to:

```
https://tabby-ageless-ending.ngrok-free.dev/api/v1/payments/peach/webhook
```

Save. From now on Peach will POST every checkout result to that URL.

### d. Verify

1. Run a sandbox payment through the SPA.
2. Watch the backend log:
   ```bash
   tail -f backend/logs/backend.log | grep -i "peach webhook"
   ```
   You should see:
   ```
   Peach webhook received: checkoutId=xxx merchantTxId=BK-...
   Peach payment SUCCESS for booking BK-... (code=000.100.110)
   ```

---

## 2) Demo / production (`ambianceholidays.ciadmin.in`)

No tunnel needed — the box is already publicly reachable.

### a. Webhook URL to configure in the Peach dashboard

```
https://ambianceholidays.ciadmin.in/api/v1/payments/peach/webhook
```

Add this in the merchant dashboard's webhook / notification URL field, save.

### b. Confirm the reverse proxy forwards `/api/v1/payments/peach/webhook` to the backend

Apache (current setup, per the `Server: Apache` response header) — there should
be a `ProxyPass` rule like:

```apache
ProxyPass        /api/v1/  http://127.0.0.1:8080/api/v1/
ProxyPassReverse /api/v1/  http://127.0.0.1:8080/api/v1/
```

The webhook path falls under `/api/v1/`, so it works automatically. Verify with:

```bash
curl -i -X POST https://ambianceholidays.ciadmin.in/api/v1/payments/peach/webhook \
     -H "Content-Type: application/json" \
     -d '{"id":"test","merchantTransactionId":"NONEXISTENT"}'
```

Expected: `200 OK` with `{"status":"ignored","reason":"payment-not-found"}`.
That confirms the endpoint is reachable and the reverse-proxy is wired.
**Important:** the endpoint is `permitAll` in Spring Security (already configured
under `/payments/peach/**`), so no auth is needed for Peach to POST.

### c. Verify after deploy

Trigger a real payment, then on the prod server:

```bash
grep -i "Peach webhook received" /path/to/backend.log | tail -5
```

If you see entries for your test payment, webhook flow is working.

---

## 3) Security note

The endpoint accepts unauthenticated POSTs (Peach can't authenticate to your
service). It's safe because:

1. **No body trust** — we ignore any result code in the payload.
2. **Re-query** — the controller calls Peach's status API to get the real result.
3. **Idempotent** — already-terminal payments return `ack` without state changes.

If Peach offers signed webhook payloads in the V2 API (HMAC header), wire that
in `PeachPaymentController.webhook()` as a defence-in-depth check. Today the
re-query approach already prevents tampering since we don't trust the body.

---

## 4) What changed in code

- `PeachPaymentController.webhook()` — new `POST /payments/peach/webhook` endpoint
- `PeachPaymentController.retry(...)` — new `POST /payments/peach/retry/{bookingId}` for
  re-attempting a FAILED booking without losing its reference
- `PeachPaymentController.applyResult(...)` — single source of truth for translating
  a Peach result into Payment + Booking state. Used by `/return`, `/status`, `/webhook`.
- `PeachCheckoutService.PENDING_PATTERN` — codes like `000.200.*` no longer get
  marked FAILED; Payment stays PENDING and waits for a later webhook.
- `/return` — now re-verifies via Peach's status API instead of trusting redirect
  query params.
- `BookingResponse` — admin booking detail/list now includes a `payment` summary
  (status, paidAt, peachCheckoutId, resultCode, etc.).
