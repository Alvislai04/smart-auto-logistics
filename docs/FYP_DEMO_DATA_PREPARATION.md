# FYP Demo Data Preparation

Use this procedure shortly before the presentation to reduce operational data
without deleting accounts or database-backed dropdown/configuration values.

## Preserved Data

The cleanup does not modify these tables:

- `app_users`
- `vehicle_models`
- `shipment_party_profiles`
- `user_role_options`
- `simulation_profiles`

It intentionally does not restart identity sequences. Existing Sepolia contract
entries are immutable, so reusing old shipment-event IDs could make future
contract writes fail.

## Retention Rules

- Two demo-rich shipments, prioritizing recorded blockchain proof and tracking
  history
- Up to 50 recent tracking points per retained shipment for a usable map
- Up to 20 recent timeline events per shipment, plus every event required by a
  retained blockchain record
- The latest simulation session for each retained shipment
- Two stock-alert rules
- Two alerts
- Two blockchain records
- Up to two integrity cases, admin notifications, and company notifications
  when matching retained blockchain records exist

Child history is deliberately not limited to two rows. Doing so would leave the
route map and audit timeline too sparse for a meaningful demonstration.

## Procedure

1. In Render, temporarily set both schedulers to `false`:

   ```text
   SIMULATION_SCHEDULER_ENABLED=false
   STOCK_MONITORING_SCHEDULER_ENABLED=false
   ```

2. Wait for the Render redeployment to finish, then keep users out of the
   application while cleanup runs. Suspending the Render service temporarily is
   the safest option because it prevents background writes during cleanup.
3. Open Supabase **SQL Editor**.
4. Run
   `database/maintenance/2026-08-28-preview-fyp-demo-cleanup.sql`.
5. Confirm that shipment ranks `1` and `2` are suitable demonstration records.
6. Open `database/maintenance/2026-08-28-prepare-fyp-demo-data.sql`.
7. Place the cursor anywhere inside the `DO $cleanup$ ... $cleanup$;` block and
   click **Run** once. The retention IDs are held inside that single atomic
   PostgreSQL statement; no temporary database tables are required. Run the
   final row-count query separately afterward if Supabase selects only the
   statement under the cursor.
8. Confirm the result grid shows every account and no more than two rows in each
   top-level operational section.
9. Refresh the application and verify Shipments, Alerts, Stock Monitoring,
   Blockchain Monitor, and both retained shipment-detail pages.
10. Re-enable a scheduler only when its live behavior is needed for the demo.

If PostgreSQL reports a deadlock or lock timeout, the transaction is aborted and
its changes are rolled back. Confirm the Render service is suspended (or both
schedulers are disabled and no users are active), wait 30 seconds, and run the
entire cleanup file again.

The cleanup takes row-level locks on shipments and stock rules selected for
deletion. This prevents the simulation and stock schedulers from inserting a
new dependent row between child cleanup and parent deletion without blocking
ordinary application reads.

## Backup And Blockchain Notes

Before deletion, the cleanup copies all affected tables into the
`fyp_demo_backup_20260828` schema. Keep that schema until the presentation has
finished successfully.

Deleting a `blockchain_records` database row does not and cannot remove its old
Sepolia transaction. The script preserves two database records for the UI and
does not reuse any event IDs, so new records remain safe to create.

## Executed Result

The cleanup completed successfully against Supabase on 2026-08-29. The result
contained 12 preserved accounts, 2 shipments, 2 alerts, 2 blockchain records,
2 simulation sessions, 100 tracking events, 42 shipment events, and 1 existing
stock-alert rule. No matching integrity cases or blockchain notifications
existed for the retained proof records, so those sections correctly remained
empty.
