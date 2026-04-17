# JSON -> PostgreSQL 迁移指引（v1）

对应 `数据库设计.md` 第 8 节。

## 数据映射

- `auth.json` -> `users`, `sessions`
- `workspace.json` -> `archives`, `archive_messages`, `archive_tasks`, `archive_model_assets`
- `community.json` -> `community_publications`, `remix_events`

## 建议迁移顺序

1. `users`
2. `sessions`
3. `archives`
4. `archive_messages`
5. `archive_tasks`
6. `archive_model_assets`
7. `community_publications`
8. `remix_events`

## 一致性校验 SQL（示例）

```sql
-- 1) archive 必须有 owner
SELECT a.id
FROM archives a
LEFT JOIN users u ON u.id = a.owner_id
WHERE u.id IS NULL;

-- 2) publication 必须对应 archive
SELECT cp.id
FROM community_publications cp
LEFT JOIN archives a ON a.id = cp.archive_id
WHERE a.id IS NULL;

-- 3) remix_count 与事件条数核对
SELECT cp.id, cp.remix_count, COALESCE(ev.cnt, 0) AS actual_count
FROM community_publications cp
LEFT JOIN (
    SELECT publication_id, COUNT(*) AS cnt
    FROM remix_events
    GROUP BY publication_id
) ev ON ev.publication_id = cp.id
WHERE cp.remix_count <> COALESCE(ev.cnt, 0);
```

