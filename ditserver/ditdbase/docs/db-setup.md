# PostgreSQL 建库说明

本文档对应 `数据库设计.md` 的 v1 结构，目标数据库为 PostgreSQL。

## 1. 文件结构

- `db/schema/000-init.sql`：一键初始化入口脚本
- `db/schema/001_extensions.sql`：扩展安装（`pgcrypto`）
- `db/schema/010_tables.sql`：核心表
- `db/schema/020_constraints.sql`：业务约束与补充外键
- `db/schema/030_indexes.sql`：查询索引与首轮传图唯一索引
- `db/schema/040_triggers.sql`：`updated_at`、发布可见性联动、remix 计数触发器
- `scripts/verify/verify_schema.sql`：初始化后的快速校验

## 2. 执行建库

```bash
psql "$DATABASE_URL" -f db/schema/000-init.sql
```

> 说明：`000-init.sql` 使用 `psql` 的 `\i` 指令，建议在项目根目录执行。

## 3. 执行校验

```bash
psql "$DATABASE_URL" -f scripts/verify/verify_schema.sql
```

## 4. 约束实现说明

- 单存档单作品：`archive_model_assets.archive_id` 主键。
- 任务进度范围：`archive_tasks.progress` 限制在 `0..100`。
- 状态值约束：`archives.status`、`archive_tasks.status`、`archive_messages.role`。
- 首轮传图约束：
  - 唯一索引 `uq_archive_tasks_one_image_per_archive` 保证每个存档最多 1 条带图任务。
  - 触发器 `trg_archive_tasks_first_round_image` 保证有历史任务后不可再传图。
- 社区发布联动：
  - 触发器 `trg_publication_sync_archive_visibility` 保证发布者是存档 owner。
  - 依据 `community_publications.is_active` 自动同步 `archives.is_private`。
- remix 计数：`remix_events` 插入/删除自动维护 `community_publications.remix_count`。

