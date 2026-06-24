-- Basic checks after schema setup

SELECT to_regclass('public.users') AS users_table;
SELECT to_regclass('public.archives') AS archives_table;
SELECT to_regclass('public.community_publications') AS community_publications_table;

-- Required status check constraints
SELECT conname
FROM pg_constraint
WHERE conname IN (
    'chk_archives_status',
    'chk_archive_tasks_status',
    'chk_archive_tasks_progress',
    'chk_archive_messages_role'
)
ORDER BY conname;

-- Required indexes
SELECT indexname
FROM pg_indexes
WHERE schemaname = 'public'
  AND indexname IN (
      'idx_archives_owner_updated_desc',
      'idx_archive_messages_archive_created_asc',
      'idx_archive_tasks_archive_created_desc',
      'idx_community_publications_active_updated_desc',
      'idx_community_publications_owner_created_desc',
      'idx_remix_events_publication_created_desc',
      'uq_archive_tasks_one_image_per_archive'
  )
ORDER BY indexname;

